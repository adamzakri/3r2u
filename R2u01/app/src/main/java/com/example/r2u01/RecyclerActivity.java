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

public class RecyclerActivity extends AppCompatActivity {
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
    private ImageView profileImageView;
    private String uid;
    private String CurrentUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        // Initialize views
        btnAddImage = findViewById(R.id.btnAddImage);
        imageView = findViewById(R.id.imageView);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        btnSelectLocation = findViewById(R.id.btnSelectLocation);
        btnAddItem = findViewById(R.id.btnAddItem);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();
        codeId = findViewById(R.id.etCode);
        itemId = generateRandomItemId();
        codeId.setText("Your Item ID : "+itemId);

        btnChangeId = findViewById(R.id.btnChangeId);
        btnChangeId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemId = generateRandomItemId();
                codeId.setText("Your Item ID : "+itemId);
            }
        });


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
                Intent mapIntent = new Intent(RecyclerActivity.this, MapActivity.class);
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

// ... (get references to other items as needed)
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String uid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        DatabaseReference usersRefs = FirebaseDatabase.getInstance().getReference().child("users").child(uid);


        usersRefs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String imgUrl = snapshot.child("imgUrl").getValue(String.class); // Change "imgUrl" to the actual key
                    CurrentUserType = snapshot.child("userType").getValue(String.class);

                    updateNavHeader(username, email, imgUrl);

                    // Populate the userList with user data from Firebase
                    // For example, using ValueEventListener to fetch users from Firebase
                    if (!CurrentUserType.isEmpty()){
                        if (CurrentUserType.equals("Recycler")){
                            navigationView.inflateMenu(R.menu.nav_menu);
                            // Set a listener for navigation item clicks
                            navigationView.setNavigationItemSelectedListener(item -> {
                                int id = item.getItemId();
                                if (id == R.id.nav_add_items) {
                                    /*// Handle navigation to AddItemsActivity
                                    Intent addItemsIntent = new Intent(RecyclerActivity.this, RecyclerActivity.class);
                                    startActivity(addItemsIntent);*/
                                } else if (id == R.id.nav_chats) {
                                    Intent addItemsIntent = new Intent(RecyclerActivity.this, ChatActivity.class);
                                    startActivity(addItemsIntent);
                                    finish();
                                } else if (id == R.id.nav_logout) {
                                    // Handle user logout and navigation to LoginActivity
                                    FirebaseAuth.getInstance().signOut();
                                    Intent loginIntent = new Intent(RecyclerActivity.this, LoginActivity.class);
                                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(loginIntent);
                                    finish(); // Close the current activity
                                } else if (id == R.id.nav_manage_items) {
                                    Intent manageIntent = new Intent(RecyclerActivity.this, RecyclerItemsList.class);
                                    startActivity(manageIntent);
                                    finish(); // Close the current activity
                                }else if (id == R.id.nav_profile) {
                                    // Handle navigation to ChatActivity
                                    Intent chatsIntent = new Intent(RecyclerActivity.this, ProfileActivity.class);
                                    startActivity(chatsIntent);
                                    finish();
                                }

                                drawerLayout.closeDrawers();
                                return true;
                            });
                        }else {
                            navigationView.inflateMenu(R.menu.nav_menu_collector);
                            // Set a listener for navigation item clicks
                            navigationView.setNavigationItemSelectedListener(item -> {
                                int id = item.getItemId();
                                if (id == R.id.nav_chats_collector) {
                                    // Handle navigation to ChatActivity
                                    Intent chatsIntent = new Intent(RecyclerActivity.this, ChatActivity.class);
                                    startActivity(chatsIntent);
                                    finish();
                                }else if (id == R.id.nav_profile_collector) {
                                    // Handle navigation to ChatActivity
                                    Intent chatsIntent = new Intent(RecyclerActivity.this, ProfileCollectorActivity.class);
                                    startActivity(chatsIntent);
                                    finish();
                                }else if (id == R.id.nav_logout_collector) {
                                    FirebaseAuth.getInstance().signOut();
                                    Intent loginIntent = new Intent(RecyclerActivity.this, LoginActivity.class);
                                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(loginIntent);
                                }else if (id == R.id.nav_items) {
                                    // Handle navigation to ChatActivity
                                    Intent chatsIntent = new Intent(RecyclerActivity.this, CollectorActivity.class);
                                    startActivity(chatsIntent);
                                    finish();
                                }

                                drawerLayout.closeDrawers();
                                return true;
                            });
                        }
                    }
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
        profileImageView = headerView.findViewById(R.id.profileImageView);

        usernameTextView.setText(username);
        emailTextView.setText(email);

        // Check if the activity is not destroyed before loading the image
        if (!isFinishing()) {
            // Load and display the profile image using Glide
            Glide.with(RecyclerActivity.this)
                    .load(imgUrl) // The profile image URL
                    .placeholder(R.drawable.ic_default_image) // Placeholder image while loading
                    .error(R.drawable.ic_default_image) // Image to display if loading fails
                    .into(profileImageView);
        }
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
                                    Item item = new Item(itemId, title, description, imgUrl, selectedLocation.latitude, selectedLocation.longitude,uid);

                                    DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("items").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(itemId);
                                    itemsRef.setValue(item);

                                    Toast.makeText(RecyclerActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();

                                    resetFields();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle image upload failure
                            Toast.makeText(RecyclerActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please insert image item", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateRandomItemId() {
        // Generate random alphabets
        StringBuilder randomAlphabets = new StringBuilder();
        Random random = new Random();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < 4; i++) {
            randomAlphabets.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }

        // Generate random numbers
        StringBuilder randomNumbers = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            randomNumbers.append(random.nextInt(10)); // Generates numbers between 0 and 9
        }

        // Combine alphabets and numbers
        return randomAlphabets.toString() + randomNumbers.toString();
    }
    private void resetFields() {
        // Clear input fields
        etTitle.getText().clear();
        etDescription.getText().clear();
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_default_image));

        // Reset selected location
        selectedLocation = null;
        TextView tvLocation = findViewById(R.id.tvLocation);
        tvLocation.setVisibility(View.GONE);

        // Reset selected image URI
        selectedImageUri = null;

        // Generate and set a new random item ID
        itemId = generateRandomItemId();
        codeId.setText("Your Item ID : " + itemId);
    }
}
