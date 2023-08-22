package com.example.r2u01;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK_FOR_ADD = 1;
    private static final int REQUEST_IMAGE_PICK_FOR_EDIT = 2;
    private static final int REQUEST_LOCATION_PERMISSION = 3;

    private UserSessionManager sessionManager;

    private final ArrayList<ListItem> itemList = new ArrayList<>();
    private CustomAdapter customAdapter;
    private int editPosition = -1;
    private Uri selectedImageUri;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




            RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        customAdapter = new CustomAdapter(itemList, new CustomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Handle item click here
            }

            @Override
            public void onEditClick(int position, ImageView imageView) {
                editPosition = position;
                showEditItemDialog();
            }

            @Override
            public void onDeleteClick(int position) {
                itemList.remove(position);
                customAdapter.notifyItemRemoved(position);
                Toast.makeText(MainActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
            }



        });
        recyclerView.setAdapter(customAdapter);

        Button buttonAdd = findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(v -> showAddItemDialog());

        Button buttonShowMap = findViewById(R.id.buttonShowMap);
        buttonShowMap.setOnClickListener(v -> {
            if (itemList.isEmpty()) {
                Toast.makeText(MainActivity.this, "No items to show on the map", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                intent.putParcelableArrayListExtra("itemList", new ArrayList<>(itemList));
                startActivity(intent);
            }
        });
                // Initialize the UserSessionManager with the context of MainActivity
                sessionManager = new UserSessionManager(this);

        // Find the Log Out button by its ID
        Button buttonLogOut = findViewById(R.id.buttonLogOut);

        // Set the click listener for the Log Out button
        buttonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the logOut method when the button is clicked
                logOut();
            }
        });
    }


    private void showAddItemDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null);
        dialogBuilder.setView(dialogView);

        EditText editTextCode = dialogView.findViewById(R.id.editTextCode);
        EditText editTextTitle = dialogView.findViewById(R.id.editTextTitle);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);
        ImageView imageView = dialogView.findViewById(R.id.imageView);
        Button buttonAddImage = dialogView.findViewById(R.id.buttonAddImage);
        Button buttonDetectLocation = dialogView.findViewById(R.id.buttonDetectLocation);

        buttonAddImage.setOnClickListener(v -> openImagePickerForAddItem());

        buttonDetectLocation.setOnClickListener(v -> {
            // Check if the location permission is granted
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                detectLocation();
            } else {
                // Location permission not granted, request the permission
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            }
        });

        dialogBuilder.setPositiveButton("Add", (dialog, which) -> {
            String title = editTextTitle.getText().toString();
            String description = editTextDescription.getText().toString();
            String code = editTextCode.getText().toString();

            // Check if the code is correct (e.g., "1234")
            if (!code.equals("1234")) {
                Toast.makeText(this, "Incorrect code", Toast.LENGTH_SHORT).show();
                return;}

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please enter title and description", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentLocation != null) {
                itemList.add(new ListItem(title, description, selectedImageUri, currentLocation));
            } else {
                itemList.add(new ListItem(title, description, selectedImageUri));
            }
            customAdapter.notifyItemInserted(itemList.size() - 1);

            Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
        });

        dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void openImagePickerForAddItem() {
        openImagePicker(REQUEST_IMAGE_PICK_FOR_ADD);
    }

    private void showEditItemDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_item, null);
        dialogBuilder.setView(dialogView);

        EditText editTextCode = dialogView.findViewById(R.id.editTextCode);
        EditText editTextTitle = dialogView.findViewById(R.id.editTextTitle);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);
        ImageView imageView = dialogView.findViewById(R.id.imageView);
        Button buttonAddImage = dialogView.findViewById(R.id.buttonAddImage);
        Button buttonDetectLocation = dialogView.findViewById(R.id.buttonDetectLocation);
        Button buttonEdit = dialogView.findViewById(R.id.buttonEdit);

        buttonAddImage.setOnClickListener(v -> openImagePickerForEditItem());

        buttonDetectLocation.setOnClickListener(v -> {
            // Check if the location permission is granted
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                detectLocation();
            } else {
                // Location permission not granted, request the permission
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            }
        });

        ListItem editItem = itemList.get(editPosition);
        editTextTitle.setText(editItem.getTitle());
        editTextDescription.setText(editItem.getDescription());
        loadImageWithGlide(editItem.getImageUri(), imageView);

        buttonEdit.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString();
            String description = editTextDescription.getText().toString();
            String code = editTextCode.getText().toString();

            if (!code.equals("1234")) {
                Toast.makeText(this, "Incorrect code", Toast.LENGTH_SHORT).show();
                return;
            }

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please enter title and description", Toast.LENGTH_SHORT).show();
                return;
            }

            editItem.setTitle(title);
            editItem.setDescription(description);
            editItem.setImageUri(selectedImageUri);
            editItem.setLocation(currentLocation); // Associate the current location with the edited item

            customAdapter.notifyItemChanged(editPosition);

            Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
        });

        dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        Button buttonDelete = dialogView.findViewById(R.id.buttonDelete);

        // Set click listener for the delete button
        buttonDelete.setOnClickListener(v -> {
            itemList.remove(editPosition);
            customAdapter.notifyItemRemoved(editPosition);
            Toast.makeText(MainActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
            alertDialog.dismiss(); // Dismiss the dialog after deleting the item
        });
    }

    private void openImagePickerForEditItem() {
        openImagePicker(REQUEST_IMAGE_PICK_FOR_EDIT);
    }

    private void openImagePicker(int requestCode) {
        Intent pickImageIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pickImageIntent.setType("image/*");
        startActivityForResult(pickImageIntent, requestCode);
    }

    private void loadImageWithGlide(Uri imageUri, ImageView imageView) {
        Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions.centerCropTransform())
                .into(imageView);
    }

    private void detectLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Location permission not granted, request the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Location detection successful
                        currentLocation = location;

                        Toast.makeText(MainActivity.this, "Location detected", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Location detection failed
                    Toast.makeText(MainActivity.this, "Location detection failed", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK_FOR_ADD || requestCode == REQUEST_IMAGE_PICK_FOR_EDIT) {
                if (data != null && data.getData() != null) {
                    selectedImageUri = data.getData();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                detectLocation();
            } else {
                Toast.makeText(MainActivity.this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void logOut() {
        // Clear the user session data using the UserSessionManager
        sessionManager.clearUserSession();

        // Finish the MainActivity and return to the login screen
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

}

