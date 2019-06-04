package won.kma.buitai.whatsapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import won.kma.buitai.whatsapp.adapter.MessageAdapter;
import won.kma.buitai.whatsapp.helper.VirgilHelper;
import won.kma.buitai.whatsapp.model.Messages;

public class ChatActivity extends AppCompatActivity {
    private String messageReceiverID, messageReceiveUserName, messageReceiveImage, messageSenderID;
    private TextView textViewUserName, textViewUserLastSeen;
    private CircleImageView imageAvatar;
    private Toolbar chatToolbar;
    private ImageButton imageButtonSendTextMessage;
    private EditText editTextInputMessage;
    private ImageButton imageButtonSendVoiceMessage;

    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private final List<Messages>listMessage = new ArrayList<>();
    private RecyclerView recyclerViewUserMessagesList;


    private DatabaseReference RootRef;
    private FirebaseAuth firebaseAuth;

    public VirgilHelper virgilHelper = new VirgilHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseAuth = FirebaseAuth.getInstance();
        messageSenderID = firebaseAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID = getIntent().getExtras().get("visitorUserID").toString();
        messageReceiveUserName = getIntent().getExtras().get("visitorUserName").toString();
        messageReceiveImage = getIntent().getExtras().get("visitorImageProfile").toString();

        InitializeFields();

        textViewUserName.setText(messageReceiveUserName);
        Picasso.get().load(messageReceiveImage).placeholder(R.drawable.profile_image).into(imageAvatar);

        editTextInputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals("")){
                    imageButtonSendVoiceMessage.setVisibility(View.GONE);
                    imageButtonSendTextMessage.setVisibility(View.VISIBLE);
                }else{
                    imageButtonSendTextMessage.setVisibility(View.GONE);
                    imageButtonSendVoiceMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        imageButtonSendTextMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
    }

    //retrieve Data from Firebase

    @Override
    protected void onStart() {

        super.onStart();
        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);//get data from Model;

                String decryptmessage = messages.getMessage();
                String decryptedText = virgilHelper.eThree.decrypt(decryptmessage, virgilHelper.decryptKey);

                messages.setMessage(decryptedText);
                listMessage.add(messages);

                messageAdapter.notifyDataSetChanged();
                //Scroll to end of bottom
                recyclerViewUserMessagesList.smoothScrollToPosition(recyclerViewUserMessagesList.getAdapter().getItemCount());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void SendMessage(){
        String MessageText = editTextInputMessage.getText().toString();
        if (TextUtils.isEmpty(MessageText)){
            Toast.makeText(ChatActivity.this, "Please enter your Message!...", Toast.LENGTH_SHORT).show();
        }
        else{
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReiciverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();
            String messagePushID = userMessageKeyRef.getKey();

            String encryptedText = virgilHelper.eThree.encrypt(MessageText, virgilHelper.listPublicKey);

            Map mapMessageTextBody = new HashMap();

            mapMessageTextBody.put("message", encryptedText);
            mapMessageTextBody.put("type", "text");
            mapMessageTextBody.put("from", messageSenderID);

            Map messageBodyDetail = new HashMap();
            messageBodyDetail.put(messageSenderRef + "/" + messagePushID, mapMessageTextBody);
            messageBodyDetail.put(messageReiciverRef + "/" + messagePushID, mapMessageTextBody);

            RootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Successfully", Toast.LENGTH_SHORT).show();
                    }
                    editTextInputMessage.setText("");
                }
            });
        }

    }

    private void DisplayUserLastSeen(){
        RootRef.child("Users").child(messageSenderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //show last seen (state)
                if (dataSnapshot.child("userState").hasChild("state")){
                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                    String time = dataSnapshot.child("userState").child("time").getValue().toString();
                    if (state.equals("online")){
                        textViewUserLastSeen.setText("online");
                    }
                    else if(state.equals("offline")){
                        textViewUserLastSeen.setText("Last seen: " + date + " " + time);

                    }
                }
                else{
                    textViewUserLastSeen.setText("offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    private void InitializeFields() {
        textViewUserName = (TextView)findViewById(R.id.tv_custom_username_display);
        textViewUserLastSeen = (TextView)findViewById(R.id.tv_custom_last_seen);
        imageAvatar = (CircleImageView)findViewById(R.id.image_custom_profile_chat);
        chatToolbar = (Toolbar)findViewById(R.id.chat_toolbar);
        imageButtonSendTextMessage = (ImageButton)findViewById(R.id.img_btn_send_chat_msg);
        editTextInputMessage = (EditText)findViewById(R.id.edt_input_chat_message);
        imageButtonSendVoiceMessage = (ImageButton)findViewById(R.id.img_btn_send_chat_voice);
        //set layout RecycleView
        messageAdapter = new MessageAdapter(listMessage);
        recyclerViewUserMessagesList = (RecyclerView)findViewById(R.id.recycleview_message_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewUserMessagesList.setLayoutManager(linearLayoutManager);
        recyclerViewUserMessagesList.setAdapter(messageAdapter);

        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        //Access to custom toolbar;

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionbarView);
    }
}
