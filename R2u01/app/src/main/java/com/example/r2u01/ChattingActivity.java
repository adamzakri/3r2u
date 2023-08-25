package com.example.r2u01;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChattingActivity extends AppCompatActivity {
    private String selectedUserId;
    private String selectedUsername;
    private String selectedImgUrl;

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList = new ArrayList<>();

    private EditText messageEditText;
    private ImageButton sendButton;

    private DatabaseReference messagesRef;
    private String selectedUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        selectedUserId = getIntent().getStringExtra("selectedUserId");
        selectedUsername = getIntent().getStringExtra("selectedUsername");
        selectedImgUrl = getIntent().getStringExtra("selectedImgUrl");
        selectedUserType =  getIntent().getStringExtra("selectedUserType");

        getSupportActionBar().setTitle(selectedUsername);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatAdapter = new ChatAdapter(messageList,FirebaseAuth.getInstance().getCurrentUser().getUid());
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String chatId;
        if (selectedUserType.equals("Collector")){
            chatId = currentUserId + "_" + selectedUserId;
            System.out.println("im collector");
        }else {
            chatId = selectedUserId + "_" + currentUserId;
            System.out.println("im Recycler");
        }

        messagesRef = FirebaseDatabase.getInstance().getReference().child("chats").child(chatId);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendMessage();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                messageList.add(message);
                chatAdapter.notifyDataSetChanged();
                chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void sendMessage() throws JSONException {
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            long timestamp = System.currentTimeMillis();
            Message message = new Message(senderId, messageText, timestamp);

            messagesRef.push().setValue(message);

            messageEditText.setText("");

            // Send FCM notification to the selected user
            sendNotification(selectedUserId, messageText);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void sendNotification(String recipientId,String message) throws JSONException {

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(recipientId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fcmToken = snapshot.child("fcmToken").getValue(String.class);
                    String senderUsername = snapshot.child("username").getValue(String.class);
                    String sendeImgUrl = snapshot.child("imgUrl").getValue(String.class);

                    if (fcmToken != null && !fcmToken.isEmpty()) {

                        try {
                            JSONObject jsonObject = new JSONObject();

                            JSONObject notificationObj = new JSONObject();
                            System.out.println("\uD83E\uDD7A" + senderUsername);
                            notificationObj.put("title", senderUsername);
                            notificationObj.put("body", message);

                            JSONObject dataObj = new JSONObject();
                            dataObj.put("userId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                            dataObj.put("imageUrl", sendeImgUrl);

                            jsonObject.put("notification", notificationObj);
                            jsonObject.put("data", dataObj);
                            jsonObject.put("to", fcmToken );

                            System.out.println("fcm token "+ fcmToken);

                            callApi(jsonObject);

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
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

    void callApi(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer AAAA4TWBPeE:APA91bG00Ghmvan_TE8dkmqAe7TRna0HJonLIMPLuC13SSDotEvt7f_XgnQZqKpGApVImnPzi3b-25aehLYElU-iW_i0hWpy-IPIz4X3mJV6STu0zy4ZhkGShXRCP3yz2ZI-lWwRCZy_")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }

}
