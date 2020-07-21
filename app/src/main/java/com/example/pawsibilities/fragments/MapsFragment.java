package com.example.pawsibilities.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

@RuntimePermissions
public class MapsFragment extends Fragment implements CreateTagDialogFragment.CreateTagDialogListener,
        EditTagDialogFragment.EditTagDialogListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnCameraIdleListener {

    private static final String TAG = "MapsFragment";
    private FragmentMapsBinding mapsBinding;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private List<Tag> tags;
    private BitmapDescriptor defaultMarker;

    private final long UPDATE_INTERVAL_IN_SEC = 60000;  /* 60 secs */
    private final long FASTEST_INTERVAL_IN_SEC = 5000; /* 5 secs */

    private final static String KEY_LOCATION = "location";

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

        tags = new ArrayList<>();
        defaultMarker  = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);

        mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
        } else {
            Toast.makeText(getContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }

        mapsBinding.fabCreateTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tag newTag = new Tag();
                newTag.setLocation(new ParseGeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                openCreateTagDialog(newTag);
            }
        });
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
            Toast.makeText(getContext(), "Error - Map was null!!", Toast.LENGTH_SHORT).show();
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
            ParseGeoPoint pos = t.getLocation();
            Marker mapMarker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(pos.getLatitude(), pos.getLongitude()))
                    .title(t.getName())
                    .icon(defaultMarker));

            mapMarker.setTag(t);
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
        EditTagDialogFragment editTagDialogFragment = EditTagDialogFragment.newInstance(tag, mCurrentLocation);
        editTagDialogFragment.setTargetFragment(this, 300);
        editTagDialogFragment.show(fm, "EditTagDialogFragment");
    }

    private void openCreateTagDialog(Tag tag) {
        FragmentManager fm = getFragmentManager();
        CreateTagDialogFragment createTagDialogFragment = CreateTagDialogFragment.newInstance(tag, mCurrentLocation);
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
                    Toast.makeText(getContext(), "Couldn't create tag", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "save in background for new tag failed", e);
                } else {
                    Toast.makeText(getContext(), "Created!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Marker newMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(point.getLatitude(), point.getLongitude()))
                .icon(defaultMarker));
        newMarker.setTag(newTag);

        tags.add(newTag);
    }

    @Override
    public void onFinishEditDialog(Tag newTag) {
        newTag.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getContext(), "Unable to update", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Unable to mark tag outdated", e);
                } else {
                    Toast.makeText(getContext(), "Updated!", Toast.LENGTH_SHORT).show();
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
                            mCurrentLocation = location;
                            centerOnCurrentLocation();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    private void centerOnCurrentLocation() {
        if (mCurrentLocation == null) {
            return;
        }

        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude()));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(13);
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
                        mCurrentLocation = locationResult.getLastLocation();
                    }
                }, Looper.myLooper());
    }

    @Override
    public void onCameraIdle() {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;

        map.clear();
        queryTags(bounds);
    }
}