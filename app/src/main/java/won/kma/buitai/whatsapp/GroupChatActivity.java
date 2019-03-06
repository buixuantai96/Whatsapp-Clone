package won.kma.buitai.whatsapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageButton imageButtonSendMsg;
    private EditText editTextSendMsgInput;
    private ScrollView scrollView;
    private TextView textViewTextMsg;
    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;
    private ImageButton imageButtonRecord;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference RootReference, GroupNameReference, GroupMsgKeyReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        currentGroupName = getIntent().getExtras().get("groupName").toString();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        RootReference = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);



        InitializeFields();

        GetInforUsers();
        imageButtonSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMsgtoDatabase();
                editTextSendMsgInput.setText("");
                scrollView.fullScroll(scrollView.FOCUS_DOWN);
            }
        });

        editTextSendMsgInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().equals("")){
                    imageButtonRecord.setVisibility(View.GONE);
                    imageButtonSendMsg.setVisibility(View.VISIBLE);
                }else{
                    imageButtonSendMsg.setVisibility(View.GONE);
                    imageButtonRecord.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void SaveMsgtoDatabase(){
        String Msg = editTextSendMsgInput.getText().toString();
        String MsgKey = GroupNameReference.push().getKey();
        if(TextUtils.isEmpty(Msg)){
            Toast.makeText(GroupChatActivity.this, "Please write your Message!", Toast.LENGTH_SHORT).show();
        }
        else{
            Calendar calendarDate =  Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            currentDate = simpleDateFormat.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = simpleTimeFormat.format(calendarTime.getTime());

            HashMap <String, Object> groupMessageKey = new HashMap<>();
            GroupNameReference.updateChildren(groupMessageKey);

            GroupMsgKeyReference = GroupNameReference.child(MsgKey);
            HashMap <String, Object> MsgInforMap = new HashMap<>();
            MsgInforMap.put("name", currentUserName);
            MsgInforMap.put("message", Msg);
            MsgInforMap.put("date", currentDate);
            MsgInforMap.put("time", currentTime);
            GroupMsgKeyReference.updateChildren(MsgInforMap);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        GroupNameReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Neu Group exit
                if (dataSnapshot.exists()){
                    ShowMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Neu Group exit
                if (dataSnapshot.exists()){
                    ShowMessage(dataSnapshot);
                }
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

    private void ShowMessage(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while(iterator.hasNext()){
            String chatDate = ((DataSnapshot)iterator.next()).getValue().toString();
            String Msg = ((DataSnapshot)iterator.next()).getValue().toString();
            String UserName = ((DataSnapshot)iterator.next()).getValue().toString();
            String chatTime = ((DataSnapshot)iterator.next()).getValue().toString();
            textViewTextMsg.append(UserName + " \n " + Msg + " \n " + chatTime + "  " + chatDate + "\n\n");
            scrollView.fullScroll(scrollView.FOCUS_DOWN);
        }
    }

    private void GetInforUsers() {
        RootReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields(){
        toolbar = (Toolbar)findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentGroupName);

        imageButtonSendMsg = (ImageButton)findViewById(R.id.img_btn_send_group_msg);
        editTextSendMsgInput = (EditText)findViewById(R.id.edt_input_group_message);
        scrollView = (ScrollView)findViewById(R.id.scroll);
        textViewTextMsg = (TextView)findViewById(R.id.tv_show_group_message);
        imageButtonRecord = (ImageButton)findViewById(R.id.img_btn_send_group_voice);
    }
}
