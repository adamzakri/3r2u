package com.example.r2u01;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditItemActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_MAP_LOCATION = 2;

    private Button btnAddImage;
    private ImageView imageView;
    private EditText etTitle, etDescription;
    private Button btnSelectLocation, btnAddItem;
    private LatLng selectedLocation;
    private double latitude,longitude;
    private String itemId;
    private TextView codeId;
    private ImageButton btnChangeId;
    private Uri selectedImageUri;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private String intentImgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        // Initialize views
        btnAddImage = findViewById(R.id.btnAddImage);
        imageView = findViewById(R.id.imageView);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        btnSelectLocation = findViewById(R.id.btnSelectLocation);
        btnAddItem = findViewById(R.id.btnAddItem);

         intentImgUrl = getIntent().getStringExtra("imgUrl");
        String intentItemId = getIntent().getStringExtra("itemId");
        String intentItemDesc = getIntent().getStringExtra("itemDesc");
        String intentItemTitle = getIntent().getStringExtra("itemTitle");
        Double intentItemLat = getIntent().getDoubleExtra("itemLat",0);
        Double intentItemLng = getIntent().getDoubleExtra("itemLng",0);
        latitude = intentItemLat;
        longitude = intentItemLng;
        TextView tvLocation = findViewById(R.id.tvLocation);
        if (latitude!= 0 && longitude != 0){
            tvLocation.setText("Item Location : "+latitude+","+longitude);
            tvLocation.setVisibility(View.VISIBLE);
        }
        itemId = intentItemId;
        etTitle.setText(intentItemTitle);
        etDescription.setText(intentItemDesc);
        // Load and display the profile image using Glide
        Glide.with(this)
                .load(intentImgUrl) // The profile image URL
                .placeholder(R.drawable.ic_default_image) // Placeholder image while loading
                .error(R.drawable.ic_default_image) // Image to display if loading fails
                .into(imageView);

        codeId = findViewById(R.id.etCode);
        codeId.setText("Your Item ID : "+intentItemId);


        // Set click listener for the "Add Image" button
        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Set click listener for the "Select Location" button
        btnSelectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open a MapActivity to select the location
                Intent mapIntent = new Intent(EditItemActivity.this, MapActivity.class);
                mapIntent.putExtra("locationLat", latitude);
                mapIntent.putExtra("locationLng", longitude);
                startActivityForResult(mapIntent, REQUEST_MAP_LOCATION);
            }
        });

        // Set click listener for the "Add Item" button
        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemToDatabase();
            }
        });

        // Inside onCreate
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

// Set up the ActionBarDrawerToggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        // Enable the toggle button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


// Set a listener for navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_add_items) {
                // Handle navigation to AddItemsActivity
                /*Intent addItemsIntent = new Intent(this, AddItemsActivity.class);
                startActivity(addItemsIntent);*/
            } else if (id == R.id.nav_chats) {
                // Handle navigation to ChatActivity
                Intent chatsIntent = new Intent(this, ChatActivity.class);
                startActivity(chatsIntent);
                finish();
            } else if (id == R.id.nav_logout) {
                // Handle user logout and navigation to LoginActivity
                FirebaseAuth.getInstance().signOut();
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish(); // Close the current activity
            } else if (id == R.id.nav_manage_items) {
                Intent manageIntent = new Intent(this, RecyclerItemsList.class);
                startActivity(manageIntent);
                finish(); // Close the current activity
            }

            drawerLayout.closeDrawers();
            return true;
        });

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String uid = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String imgUrl = snapshot.child("imgUrl").getValue(String.class); // Change "imgUrl" to the actual key

                    updateNavHeader(username, email, imgUrl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors if necessary
            }
        });

    }

    private void updateNavHeader(String username, String email, String imgUrl) {
        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);

        TextView usernameTextView = headerView.findViewById(R.id.usernameTextView);
        TextView emailTextView = headerView.findViewById(R.id.emailTextView);
        ImageView profileImageView = headerView.findViewById(R.id.profileImageView);

        usernameTextView.setText(username);
        emailTextView.setText(email);

        // Load and display the profile image using Glide
        Glide.with(this)
                .load(imgUrl) // The profile image URL
                .placeholder(R.drawable.ic_default_image) // Placeholder image while loading
                .error(R.drawable.ic_default_image) // Image to display if loading fails
                .into(profileImageView);
    }



    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to the ActionBarDrawerToggle, if it returns true, then it has handled the app icon touch event
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
            // TODO: Upload the image to storage and get the imgUrl
        } else if (requestCode == REQUEST_MAP_LOCATION && resultCode == RESULT_OK) {
            latitude = data.getDoubleExtra("latitude", 0);
            longitude = data.getDoubleExtra("longitude", 0);
            selectedLocation = new LatLng(latitude, longitude);
            TextView tvLocation = findViewById(R.id.tvLocation);
            if (latitude!= 0 && longitude != 0){
                tvLocation.setText("Item Location : "+latitude+","+longitude);
                tvLocation.setVisibility(View.VISIBLE);
            }

        }
    }

    private void addItemToDatabase() {
        String title = etTitle.getText().toString();
        String description = etDescription.getText().toString();

        // Upload the image to Firebase Storage
        uploadImageToStorage(title, description);
    }

    private void uploadImageToStorage(final String title, final String description) {
        if (selectedImageUri != null) {
            final String imageFileName = UUID.randomUUID().toString();
            final StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("item_images/" + imageFileName);

            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get the image URL
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imgUrl = uri.toString();
                                    String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                                    Item item = new Item(itemId, title, description, imgUrl, latitude, longitude,uid);

                                    DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("items").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(itemId);
                                    itemsRef.setValue(item);

                                    Toast.makeText(EditItemActivity.this, "Item changed successfully", Toast.LENGTH_SHORT).show();

                                    finish();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle image upload failure
                            Toast.makeText(EditItemActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {

            String uid= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            Item item = new Item(itemId, title, description, intentImgUrl, latitude, longitude,uid);

            DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("items").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(itemId);
            itemsRef.setValue(item);

            Toast.makeText(EditItemActivity.this, "Item changed successfully", Toast.LENGTH_SHORT).show();

            finish();
        }
    }

}
