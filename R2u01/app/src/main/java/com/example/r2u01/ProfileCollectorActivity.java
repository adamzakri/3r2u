package com.example.r2u01;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ProfileCollectorActivity extends AppCompatActivity {

    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private String CurrentUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_collector);

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
                                    Intent addItemsIntent = new Intent(ProfileCollectorActivity.this, RecyclerActivity.class);
                                    startActivity(addItemsIntent);
                                    finish();
                                } else if (id == R.id.nav_chats) {
                                    // Handle navigation to AddItemsActivity
                                    Intent addItemsIntent = new Intent(ProfileCollectorActivity.this, ChatActivity.class);
                                    startActivity(addItemsIntent);
                                    finish();
                                } else if (id == R.id.nav_logout) {
                                    // Handle user logout and navigation to LoginActivity
                                    FirebaseAuth.getInstance().signOut();
                                    Intent loginIntent = new Intent(ProfileCollectorActivity.this, LoginActivity.class);
                                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(loginIntent);
                                    finish(); // Close the current activity
                                } else if (id == R.id.nav_manage_items) {
                                    Intent manageIntent = new Intent(ProfileCollectorActivity.this, RecyclerItemsList.class);
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
                                    Intent chatsIntent = new Intent(ProfileCollectorActivity.this, ChatActivity.class);
                                    startActivity(chatsIntent);
                                    finish();
                                }else if (id == R.id.nav_profile_collector) {
                                   /* // Handle navigation to ChatActivity
                                    Intent chatsIntent = new Intent(ProfileCollectorActivity.this, ProfileCollectorActivity.class);
                                    startActivity(chatsIntent);
                                    finish();*/
                                }else if (id == R.id.nav_logout_collector) {
                                    FirebaseAuth.getInstance().signOut();
                                    Intent loginIntent = new Intent(ProfileCollectorActivity.this, LoginActivity.class);
                                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(loginIntent);
                                    finish();
                                }else if (id == R.id.nav_items) {
                                    // Handle navigation to ChatActivity
                                    Intent chatsIntent = new Intent(ProfileCollectorActivity.this, CollectorActivity.class);
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
}