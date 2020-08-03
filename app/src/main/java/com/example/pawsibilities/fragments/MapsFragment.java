package com.example.pawsibilities.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.pawsibilities.MainActivity;
import com.example.pawsibilities.R;
import com.example.pawsibilities.Tag;
import com.example.pawsibilities.databinding.FragmentMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

@RuntimePermissions
public class MapsFragment extends Fragment implements CreateTagDialogFragment.CreateTagDialogListener,
        EditTagDialogFragment.EditTagDialogListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnCameraIdleListener,
        MainActivity.FabButtonClickListener {

    private static final String TAG = "MapsFragment";
    private FragmentMapsBinding mapsBinding;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private ParseUser user;
    private List<Tag> tags;
    private Bitmap smallMarker;
    private Circle userRadius;

    private final long UPDATE_INTERVAL_IN_SEC = 60000;  /* 60 secs */
    private final long FASTEST_INTERVAL_IN_SEC = 5000; /* 5 secs */

    public final static String KEY_LOCATION = "location";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mapsBinding = FragmentMapsBinding.inflate(inflater, container, false);
        return mapsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
            // is not null.
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        user = ParseUser.getCurrentUser();
        queryUser();

        tags = new ArrayList<>();

        // prepare custom map marker
        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.map_marker);
        Bitmap b = bitmapdraw.getBitmap();
        smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);

        mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
        } else {
            Toasty.error(getContext(), "Map couldn't load!", Toast.LENGTH_SHORT).show();
        }

        ((MainActivity) getActivity()).setListener(this);
    }

    private void updateUserLocation(Location newLocation) {
        mCurrentLocation = newLocation;
        user.put(KEY_LOCATION, new ParseGeoPoint(newLocation.getLatitude(), newLocation.getLongitude()));
        user.saveInBackground();

    }

    private void queryUser() {
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                user = objects.get(0);
            }
        });
    }

    private void displayUserRadius() {
        queryUser();
        userRadius = map.addCircle(new CircleOptions()
                .center(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                .radius(user.getDouble(ProfileFragment.KEY_RADIUS) * 1600) // convert radius from miles to meters
                .strokeWidth(4)
                .strokeColor(ContextCompat.getColor(getContext(), R.color.dusty_blue))
                .fillColor(ContextCompat.getColor(getContext(), R.color.translucent_dusty_blue)));
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            MapsFragmentPermissionsDispatcher.getMyLocationWithPermissionCheck(this);
            MapsFragmentPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);

            queryTags(map.getProjection().getVisibleRegion().latLngBounds);
            map.setOnCameraIdleListener(this);

            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    return markerClicked(marker);
                }
            });
            map.setOnMapLongClickListener(this);

        } else {
            Toasty.error(getContext(), "Map couldn't load!", Toast.LENGTH_SHORT).show();
        }
    }

    private void queryTags(LatLngBounds bounds) {
        tags.clear();

        ParseQuery<Tag> query = ParseQuery.getQuery(Tag.class);
        query.include(Tag.KEY_UPDATED_AT);
        query.whereEqualTo(Tag.KEY_ACTIVE, true);
        query.whereWithinPolygon(Tag.KEY_LOCATION, boundsToBox(bounds));
        query.findInBackground(new FindCallback<Tag>() {
            @Override
            public void done(List<Tag> queried, ParseException e) {
                if (e != null) {
                    Toasty.error(getContext(), "Couldn't load tags", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "issue with getting Tags", e);
                    return;
                }
                tags.addAll(queried);
                displayTags();
            }
        });
    }

    private void displayTags() {
        for (Tag t : tags) {
            if (t.getActive()) {
                ParseGeoPoint pos = t.getLocation();
                Marker mapMarker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(pos.getLatitude(), pos.getLongitude()))
                        .title(t.getName())
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

                mapMarker.setTag(t);
            }
        }
    }

    private List<ParseGeoPoint> boundsToBox(LatLngBounds bounds) {
       List<ParseGeoPoint> corners = new ArrayList<>();
        corners.add(new ParseGeoPoint(bounds.southwest.latitude, bounds.southwest.longitude));
        corners.add(new ParseGeoPoint(bounds.northeast.latitude, bounds.southwest.longitude));
        corners.add(new ParseGeoPoint(bounds.northeast.latitude, bounds.northeast.longitude));
        corners.add(new ParseGeoPoint(bounds.southwest.latitude, bounds.northeast.longitude));
        return corners;
    }

    private boolean markerClicked(Marker marker) {
        openEditTagDialog((Tag) marker.getTag());
        return true;
    }

    @Override
    public void onMapLongClick(LatLng point) {
        Tag newTag = new Tag();
        newTag.setLocation(new ParseGeoPoint(point.latitude, point.longitude));

        openCreateTagDialog(newTag);
    }

    private void openEditTagDialog(Tag tag) {
        FragmentManager fm = getFragmentManager();
        EditTagDialogFragment editTagDialogFragment = EditTagDialogFragment.newInstance(tag);
        editTagDialogFragment.setTargetFragment(this, 300);
        editTagDialogFragment.show(fm, "EditTagDialogFragment");
    }

    public void openCreateTagDialog(Tag tag) {
        FragmentManager fm = getFragmentManager();
        CreateTagDialogFragment createTagDialogFragment = CreateTagDialogFragment.newInstance(tag);
        createTagDialogFragment.setTargetFragment(this, 200);
        createTagDialogFragment.show(fm, "CreateTagDialogFragment");
    }

    @Override
    public void onFinishCreateDialog(Tag newTag) {
        ParseGeoPoint point = newTag.getLocation();
        newTag.setDroppedBy(ParseUser.getCurrentUser());
        newTag.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toasty.error(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "save in background for new tag failed", e);
                } else {
                    Toasty.success(getContext(), "Success!", Toast.LENGTH_SHORT).show();

                    user.put(ProfileFragment.NUM_TAGS_DROPPED, user.getInt(ProfileFragment.NUM_TAGS_DROPPED) + 1);
                    user.saveInBackground();
                }
            }
        });

        Marker newMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(point.getLatitude(), point.getLongitude()))
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
        dropMarkerBounce(newMarker);
        newMarker.setTag(newTag);

        tags.add(newTag);
    }

    @Override
    public void onFinishEditDialog(Tag newTag) {
        newTag.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toasty.error(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Unable to update tag", e);
                } else {
                    Toasty.success(getContext(), "Success!", Toast.LENGTH_SHORT).show();
                    map.clear();
                    displayTags();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MapsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @SuppressWarnings({"MissingPermission"})
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(getContext());
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {

                            updateUserLocation(location);
                            centerOnCurrentLocation();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toasty.error(getContext(), "Coudln't load last GPS location", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error trying to get last GPS location", e);
                    }
                });
    }

    private void centerOnCurrentLocation() {
        if (mCurrentLocation == null) {
            return;
        }

        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude()));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(11);
        map.moveCamera(center);
        map.animateCamera(zoom);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Display the connection status
        if (mCurrentLocation != null) {
            centerOnCurrentLocation();
            displayUserRadius();
        }
        MapsFragmentPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);
    }

    // periodically checks for and updates the user's current location
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_SEC);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL_IN_SEC);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
        settingsClient.checkLocationSettings(locationSettingsRequest);
        //noinspection MissingPermission
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling ActivityCompat#requestPermissions
            // to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
            // to handle the case where the user grants the permission.
            return;
        }

        getFusedLocationProviderClient(getContext()).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        updateUserLocation(locationResult.getLastLocation());

                    }
                }, Looper.myLooper());
    }

    @Override
    public void onCameraIdle() {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;

        map.clear();
        if (mCurrentLocation != null) {
            displayUserRadius();
        }
        queryTags(bounds);
    }

    private void dropMarkerBounce(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        final android.view.animation.Interpolator interpolator = new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // calculate t for bounce based on elapsed time
                float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                // anchor specifies which point on the marker image is anchored to the marker's location
                // on the earth's surface
                // 0.5f means the marker is always centered, and 1.0f + 6 * t changes the vertical height
                // of the marker based on time to create a bounce (only uses number after decimal if > 1)
                marker.setAnchor(0.5f, 1.0f + 6 * t);

                if (t > 0.0) {
                    handler.postDelayed(this, 15);
                } else {
                    marker.showInfoWindow();
                }
            }
        });
    }

    @Override
    public void onFabClicked() {
        Tag newTag = new Tag();
        newTag.setLocation(new ParseGeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));

        openCreateTagDialog(newTag);
    }
}