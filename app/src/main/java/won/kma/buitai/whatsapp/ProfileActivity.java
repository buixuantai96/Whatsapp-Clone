package won.kma.buitai.whatsapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String receiveUserID, senderUserID, currentState;
    private CircleImageView imageViewReciverUserId;
    private TextView textViewProfileName, textViewProfileStatus;
    private Button buttonSendMessageRequest, buttonDeleteMessageRequest;

    private DatabaseReference RootReference, ChatRequestRef, ContactsRef, NotificationRef;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        RootReference = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        firebaseAuth = FirebaseAuth.getInstance();
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        senderUserID = firebaseAuth.getCurrentUser().getUid().toString();
        receiveUserID = getIntent().getExtras().get("visit_user_id").toString();

        InitializeFields();
        RetrieveInfo();


    }

    private void RetrieveInfo() {
        RootReference.child(receiveUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("image")) {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(imageViewReciverUserId);
                    textViewProfileName.setText(userName);
                    textViewProfileStatus.setText(userStatus);
                }
                if (dataSnapshot.exists()) {

                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    ManageChatRequest();

                    textViewProfileName.setText(userName);
                    textViewProfileStatus.setText(userStatus);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                String Error = databaseError.getMessage().toString();
                Toast.makeText(ProfileActivity.this, "Error: " + Error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void ManageChatRequest() {
        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiveUserID)) {
                            String request_type = dataSnapshot.child(receiveUserID).child("request_type").getValue().toString();
                            if (request_type.equals("sent")) {
                                currentState = "request sent";
                                buttonSendMessageRequest.setText(R.string.cancel_send_message);
                            } else if (request_type.equals("received")) {
                                currentState = "request received";
                                buttonSendMessageRequest.setText(R.string.confirm_request_message);

                                buttonDeleteMessageRequest.setVisibility(View.VISIBLE);
                                buttonDeleteMessageRequest.setEnabled(true);
                                buttonDeleteMessageRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        } else {
                            ContactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiveUserID)) {
                                                currentState = "friends";
                                                buttonSendMessageRequest.setText(R.string.unfriend);

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (!senderUserID.equals(receiveUserID)) {
            buttonSendMessageRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonSendMessageRequest.setEnabled(false);
                    if (currentState.equals("new")) {
                        SendChatRequest();
                    }
                    if (currentState.equals("request sent")) {
                        CancelChatRequest();
                    }
                    if (currentState.equals("request received")) {
                        ConfirmChatRequest();
                    }
                    if (currentState.equals("friends")) {
                        RemoveFriend();
                    }
                }
            });
        } else {
            buttonSendMessageRequest.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveFriend() {
        ContactsRef.child(senderUserID).child(receiveUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    ContactsRef.child(receiveUserID).child(senderUserID)
                            .child("request_type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ProfileActivity.this, "Removed sucessfully", Toast.LENGTH_LONG).show();
                                buttonSendMessageRequest.setEnabled(true);
                                currentState = "new";
                                buttonSendMessageRequest.setText(R.string.send_message);
                            }
                        }
                    });
                }
            }
        });
    }

    private void ConfirmChatRequest() {
        ContactsRef.child(senderUserID).child(receiveUserID)
                .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ContactsRef.child(receiveUserID).child(senderUserID)
                            .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ChatRequestRef.child(senderUserID).child(receiveUserID)
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            ChatRequestRef.child(receiveUserID).child(senderUserID)
                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        currentState = "friends";

                                                        buttonDeleteMessageRequest.setEnabled(true);
                                                        buttonDeleteMessageRequest.setVisibility(View.GONE);

                                                        buttonSendMessageRequest.setEnabled(true);
                                                        buttonSendMessageRequest.setText(R.string.unfriend);


                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void CancelChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiveUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ChatRequestRef.child(receiveUserID).child(senderUserID)
                            .child("request_type").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                buttonSendMessageRequest.setEnabled(true);
                                currentState = "new";
                                buttonDeleteMessageRequest.setVisibility(View.GONE);
                                buttonSendMessageRequest.setText(R.string.send_message);
                            }
                        }
                    });
                }
            }
        });
    }

    private void SendChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiveUserID)
                .child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ChatRequestRef.child(receiveUserID).child(senderUserID)
                            .child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                //push notifycation
                                HashMap <String, String> chatNotificationMap = new HashMap<>();
                                chatNotificationMap.put("from", senderUserID);
                                chatNotificationMap.put("type", "request");

                                NotificationRef.child(receiveUserID).push()
                                        .setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            buttonSendMessageRequest.setEnabled(true);
                                            currentState = "request sent";
                                            buttonSendMessageRequest.setText(R.string.cancel_send_message);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

    }

    private void InitializeFields() {
        imageViewReciverUserId = (CircleImageView) findViewById(R.id.image_visit_profile);
        textViewProfileName = (TextView) findViewById(R.id.tv_visit_user_name);
        textViewProfileStatus = (TextView) findViewById(R.id.tv_visit_user_status);
        buttonSendMessageRequest = (Button) findViewById(R.id.btn_send_msg_request);
        buttonDeleteMessageRequest = (Button) findViewById(R.id.btn_delete_chat_request);
        currentState = "new";

    }
}
