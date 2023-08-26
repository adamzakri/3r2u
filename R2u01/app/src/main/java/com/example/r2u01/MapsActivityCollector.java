package com.example.r2u01;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.r2u01.databinding.ActivityMapsCollectorBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapsActivityCollector extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsCollectorBinding binding;
    private double latitude,longitude;
    private FusedLocationProviderClient fusedLocationClient;
    private Button backBtn;
    private LatLng userLatLng; // Store user's latitude and longitude in a global variable
    private Marker clickedMarker;
    private Marker userLocationMarker;
    private String itemTitle,itemImgUrl;
    private String ownerEmail,ownerUsername,ownerUserType;
    private Button messageBtn;
    private String ownerUid;
    private Polyline currentPolyline;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private boolean isTrackingLocation = false;
    private PlacesClient placesClient;
    private GeoApiContext geoApiContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_collector);

        backBtn = findViewById(R.id.btnBack);
        messageBtn = findViewById(R.id.messageButton);

        // Initialize Places API client
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
         placesClient = Places.createClient(this);

        // Initialize GeoApiContext for Directions API
         geoApiContext = new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_key))
                .build();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // Update interval in milliseconds (adjust as needed)
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location newLocation = locationResult.getLastLocation();
                if (lastLocation == null || newLocation.distanceTo(lastLocation) >= 20) {
                    lastLocation = newLocation;
                    updatePolyline();
                }
            }
        };

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String uid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        DatabaseReference usersRefs = FirebaseDatabase.getInstance().getReference().child("users").child(uid);


        usersRefs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String CurrentUserType = snapshot.child("userType").getValue(String.class);
                    assert CurrentUserType != null;
                    if (CurrentUserType.equals("Collector")){
                        messageBtn.setVisibility(View.VISIBLE);
                    }else {
                        messageBtn.setVisibility(View.GONE);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors if necessary
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent chatIntent = new Intent(MapsActivityCollector.this, ChattingActivity.class);
                chatIntent.putExtra("selectedUserId", ownerUid);
                chatIntent.putExtra("selectedUsername", ownerUsername);
                chatIntent.putExtra("selectedUserType", ownerUserType);
                startActivity(chatIntent);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    private void updatePolyline() {
        if (currentPolyline != null) {
            currentPolyline.remove();
        }
        if (lastLocation != null && clickedMarker != null) {
            LatLng origin = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            LatLng destination = clickedMarker.getPosition();

            DirectionsApiRequest directionsApiRequest = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.DRIVING)
                    .origin(origin.latitude + "," + origin.longitude)
                    .destination(destination.latitude + "," + destination.longitude);

            directionsApiRequest.setCallback(new PendingResult.Callback<DirectionsResult>() {
                @Override
                public void onResult(DirectionsResult result) {
                    if (result.routes.length > 0 && result.routes[0].overviewPolyline != null) {
                        List<LatLng> decodedPath = PolyUtil.decode(result.routes[0].overviewPolyline.getEncodedPath());

                        PolylineOptions polylineOptions = new PolylineOptions()
                                .addAll(decodedPath)
                                .color(Color.GREEN)
                                .width(18f);

                        runOnUiThread(() -> {
                            currentPolyline = mMap.addPolyline(polylineOptions);

                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
                            builder.include(clickedMarker.getPosition());
                            LatLngBounds bounds = builder.build();
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                        });
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    // Handle failure
                }
            });
        }
    }


    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void showUserCurrentLocation() {
        if (mMap == null) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Start tracking location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Add a marker at the user's current location and move the camera
                        userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        /*userLocationMarker = mMap.addMarker(new MarkerOptions()
                                .position(userLatLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                .title("Your Location"));*/
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    Toast.makeText(this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void handleMarkerSelection(LatLng latLng) {
        // Remove the previously clicked marker (if any)
        if (clickedMarker != null) {
            clickedMarker.remove();
        }

        // Add a new marker at the clicked location with a blue color
        clickedMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Item Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Get the latitude and longitude of the clicked location
        latitude = latLng.latitude;
        longitude = latLng.longitude;

        // You can use latitude and longitude as needed
        //Toast.makeText(this, "Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (isTrackingLocation) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        ownerUid = getIntent().getStringExtra("ownerUid");
        ownerEmail = getIntent().getStringExtra("ownerEmail");
        ownerUsername = getIntent().getStringExtra("ownerUsername");
        ownerUserType = getIntent().getStringExtra("ownerUserType");
        latitude = getIntent().getDoubleExtra("locationLat",0);
        longitude = getIntent().getDoubleExtra("locationLng",0);
        itemTitle = getIntent().getStringExtra("itemTitle");
        itemImgUrl = getIntent().getStringExtra("itemImgUrl");

        if(latitude!=0&&longitude!=0){
            LatLng locationLatlng = new LatLng(latitude,longitude);
            handleMarkerSelection(locationLatlng);
        }

        // Add a marker at the user's current location and move the camera
        showUserCurrentLocation();
        // Set custom InfoWindowAdapter to display item info
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
    }
    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        @Override
        public View getInfoWindow(Marker marker) {
            return null; // Returning null here ensures that the default info window is used.
        }

        @Override
        public View getInfoContents(Marker marker) {
            View infoWindowView = getLayoutInflater().inflate(R.layout.custom_info_windows, null);

            ImageView itemImageView = infoWindowView.findViewById(R.id.itemImageView);
            TextView itemTitleTextView = infoWindowView.findViewById(R.id.itemTitleTextView);

            // Load item image using Glide
            Glide.with(MapsActivityCollector.this)
                    .load(itemImgUrl)
                    .error(R.drawable.ic_default_image)
                    .into(itemImageView);

            itemTitleTextView.setText(itemTitle);

            return infoWindowView;
        }
    }

}