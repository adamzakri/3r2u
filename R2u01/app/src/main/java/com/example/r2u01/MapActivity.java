package com.example.r2u01;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private FirebaseAuth mAuth;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private  Button backBtn;
    private LatLng userLatLng; // Store user's latitude and longitude in a global variable
    private Marker clickedMarker;
    private Marker userLocationMarker;
    private double latitude,longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        backBtn = findViewById(R.id.btnBack);
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return selected location to RecyclerActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", latitude);
                resultIntent.putExtra("longitude", longitude);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        enableMyLocation();

        latitude = getIntent().getDoubleExtra("locationLat",0);
        longitude = getIntent().getDoubleExtra("locationLng",0);

        if(latitude!=0&&longitude!=0){
            LatLng locationLatlng = new LatLng(latitude,longitude);
            handleMarkerSelection(locationLatlng);
        }
        // Add a marker at the user's current location and move the camera
        showUserCurrentLocation();

        // Set onMapClickListener to listen for map click events
        googleMap.setOnMapClickListener(this);
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void showUserCurrentLocation() {
        if (mMap == null) {
            return;
        }

        // Check for location permission, and request if necessary
        // ... (add code to handle location permission if needed)

        // Get the user's last known location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
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
                .title("Selected Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        // Get the latitude and longitude of the clicked location
        latitude = latLng.latitude;
        longitude = latLng.longitude;

        // You can use latitude and longitude as needed
        Toast.makeText(this, "Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        // Create and show the confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Confirm Selection")
                .setMessage("Do you want to select this location?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User confirmed, add the marker and get latitude and longitude
                        handleMarkerSelection(latLng);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User declined, do nothing
                    }
                })
                .show();

    }

}
