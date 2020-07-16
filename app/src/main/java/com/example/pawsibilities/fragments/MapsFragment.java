package com.example.pawsibilities.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

@RuntimePermissions
public class MapsFragment extends Fragment implements GoogleMap.OnMapLongClickListener {

    private static final String TAG = "MapsFragment";
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private List<Tag> tags;
    private List<Marker> markers;
    private BitmapDescriptor defaultMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
    private final long UPDATE_INTERVAL_IN_SEC = 60000;  /* 60 secs */
    private final long FASTEST_INTERVAL_IN_SEC = 5000; /* 5 secs */

    private final static String KEY_LOCATION = "location";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_maps, container, false);
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
        markers = new ArrayList<>();
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

        queryTags();
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            //Toast.makeText(getContext(), "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            MapsFragmentPermissionsDispatcher.getMyLocationWithPermissionCheck(this);
            MapsFragmentPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);

            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    return markerClicked(marker);
                }
            });
            map.setOnMapLongClickListener(this);

            //getMyLocation();
            //centerOnCurrentLocation();

        } else {
            Toast.makeText(getContext(), "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void queryTags() {
        ParseQuery<Tag> query = ParseQuery.getQuery(Tag.class);
        //query.include(Tag.KEY_DROPPED_BY);
        query.include(Tag.KEY_UPDATED_AT);
        query.setLimit(20);
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
            markers.add(mapMarker);
        }
    }

    private boolean markerClicked(Marker marker) {
        openEditTagDialog((Tag) marker.getTag());
        return true;
    }

    @Override
    public void onMapLongClick(LatLng point) {
        Tag newTag = new Tag();
        newTag.setLocation(new ParseGeoPoint(point.latitude, point.longitude));

        Marker newMarker = map.addMarker(new MarkerOptions()
                .position(point)
                .icon(defaultMarker));
        newMarker.setTag(newTag);

        //openCreateTagDialog(newTag);
        // how to handle submit/cancel/delete of create/edit in same overlay?
        Log.i(TAG, "long press registered");
    }

    private void openEditTagDialog(Tag tag) {
        EditTagDialogFragment editTagDialogFragment = EditTagDialogFragment.newInstance(tag);
        editTagDialogFragment.show(getChildFragmentManager(), "EditTagDialogFragment");
    }

    private void openCreateTagDialog(Tag tag) {
        CreateTagDialogFragment createTagDialogFragment = CreateTagDialogFragment.newInstance(tag);
        createTagDialogFragment.show(getChildFragmentManager(), "CreateTagDialogFragment");
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

        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
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
            //Toast.makeText(getContext(), "GPS location was found!", Toast.LENGTH_SHORT).show();
            centerOnCurrentLocation();
        } else {
            //Toast.makeText(getContext(), "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
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
            // public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission.
            return;
        }

        getFusedLocationProviderClient(getContext()).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        mCurrentLocation = locationResult.getLastLocation();
                        centerOnCurrentLocation();
                    }
                }, Looper.myLooper());
    }
}