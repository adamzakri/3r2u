package com.example.r2u01;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1;
    private CircleImageView profileImageView;
    private TextView userTypeTextView, usernameTextView, emailTextView;
    private Uri selectedImageUri;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private String CurrentUserType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImageView = findViewById(R.id.profileImageView);
        userTypeTextView = findViewById(R.id.userTypeTextView);
        usernameTextView = findViewById(R.id.usernameTextView);
        emailTextView = findViewById(R.id.emailTextView);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            // Load user data from Realtime Firebase and display it
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
            usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String userType = snapshot.child("userType").getValue(String.class);
                        String username = snapshot.child("username").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String imgUrl = snapshot.child("imgUrl").getValue(String.class);

                        // Display the user's data
                        userTypeTextView.setText(userType);
                        usernameTextView.setText(username);
                        emailTextView.setText(email);

                        if (!isFinishing()){
                            // Load and display the profile image using Glide
                            Glide.with(ProfileActivity.this)
                                    .load(imgUrl)
                                    .placeholder(R.drawable.ic_default_image)
                                    .error(R.drawable.ic_default_image)
                                    .into(profileImageView);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle errors if necessary
                }
            });


        }
        profileImageView.setOnClickListener(v -> openGallery());
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
                                    // Handle navigation to AddItemsActivity
                                    Intent addItemsIntent = new Intent(ProfileActivity.this, RecyclerActivity.class);
                                    startActivity(addItemsIntent);
                                    finish();
                                } else if (id == R.id.nav_chats) {
                                    // Handle navigation to AddItemsActivity
                                    Intent addItemsIntent = new Intent(ProfileActivity.this, ChatActivity.class);
                                    startActivity(addItemsIntent);
                                    finish();
                                } else if (id == R.id.nav_logout) {
                                    // Handle user logout and navigation to LoginActivity
                                    FirebaseAuth.getInstance().signOut();
                                    Intent loginIntent = new Intent(ProfileActivity.this, LoginActivity.class);
                                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(loginIntent);
                                    finish(); // Close the current activity
                                } else if (id == R.id.nav_manage_items) {
                                    Intent manageIntent = new Intent(ProfileActivity.this, RecyclerItemsList.class);
                                    startActivity(manageIntent);
                                    finish(); // Close the current activity
                                }else if (id == R.id.nav_profile) {
                                   /* // Handle navigation to ChatActivity
                                    Intent chatsIntent = new Intent(ProfileActivity.this, ProfileActivity.class);
                                    startActivity(chatsIntent);
                                    finish();*/
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
                                    Intent chatsIntent = new Intent(ProfileActivity.this, ChatActivity.class);
                                    startActivity(chatsIntent);
                                    finish();
                                }else if (id == R.id.nav_profile_collector) {
                                   /* // Handle navigation to ChatActivity
                                    Intent chatsIntent = new Intent(ProfileActivity.this, ProfileActivity.class);
                                    startActivity(chatsIntent);
                                    finish();*/
                                }else if (id == R.id.nav_logout_collector) {
                                    FirebaseAuth.getInstance().signOut();
                                    Intent loginIntent = new Intent(ProfileActivity.this, LoginActivity.class);
                                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(loginIntent);
                                    finish();
                                }else if (id == R.id.nav_items) {
                                    // Handle navigation to ChatActivity
                                    Intent chatsIntent = new Intent(ProfileActivity.this, CollectorActivity.class);
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

        // Get the FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String fcmToken = task.getResult();
                        Log.d("FCM Token", "FCM token: " + fcmToken);

                        // Get the reference to the current user's node in the database
                        String cuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(cuid);

                        // Save the FCM token under the 'fcmToken' key
                        userRef.child("fcmToken").setValue(fcmToken)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("FCM Token", "FCM token saved to Firebase");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FCM Token", "Error saving FCM token: " + e.getMessage());
                                });

                    } else {
                        Log.e("FCM Token", "Error getting FCM token: " + task.getException());
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
            profileImageView.setImageURI(selectedImageUri);
            // Upload the image to Firebase Storage and update profile image URL
            uploadImageAndUpdateProfileImage();
        }
    }

    private void uploadImageAndUpdateProfileImage() {
        if (selectedImageUri != null) {
            FirebaseAuth currentUser = FirebaseAuth.getInstance();
            if (currentUser != null) {
                String uid = Objects.requireNonNull(currentUser.getCurrentUser()).getUid();
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images").child(uid + ".jpg");
                storageRef.putFile(selectedImageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            // Image upload successful, get the download URL
                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String imgUrl = uri.toString();

                                // Update the user's profile image URL in the database
                                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                                usersRef.child("imgUrl").setValue(imgUrl)
                                        .addOnSuccessListener(aVoid -> {
                                            // Profile image URL updated successfully
                                            FirebaseUser fireuser = FirebaseAuth.getInstance().getCurrentUser();

                                            assert fireuser != null;
                                            String uid1 = fireuser.getUid();
                                                if (uid1 != null) {
                                                // Load user data from Realtime Firebase and display it
                                                DatabaseReference usersRefs = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                                                    usersRefs.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            String userType = snapshot.child("userType").getValue(String.class);
                                                            String username = snapshot.child("username").getValue(String.class);
                                                            String email = snapshot.child("email").getValue(String.class);
                                                            String imgUrl = snapshot.child("imgUrl").getValue(String.class);

                                                            // Display the user's data
                                                            userTypeTextView.setText(userType);
                                                            usernameTextView.setText(username);
                                                            emailTextView.setText(email);

                                                            // Load and display the profile image using Glide
                                                            Glide.with(ProfileActivity.this)
                                                                    .load(imgUrl)
                                                                    .placeholder(R.drawable.ic_default_image)
                                                                    .error(R.drawable.ic_default_image)
                                                                    .into(profileImageView);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        // Handle errors if necessary
                                                    }
                                                });

                                            }
                                            Toast.makeText(ProfileActivity.this,"Information Profile Changed",Toast.LENGTH_LONG).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Profile image URL update failed
                                            Toast.makeText(ProfileActivity.this,"Information Profile ERROR",Toast.LENGTH_LONG).show();
                                        });
                            });
                        })
                        .addOnFailureListener(e -> {
                            // Image upload failed
                            Toast.makeText(ProfileActivity.this,"Information Profile ERROR",Toast.LENGTH_LONG).show();
                        });
            }
        }
    }
}
