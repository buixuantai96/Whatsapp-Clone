package won.kma.buitai.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button updateProfile;
    private EditText userName, userStatus;
    private CircleImageView userProfilesImage;
    private String currentUserID;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference RootReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        InitlizeFields();
        userName.setEnabled(false);
        currentUserID = firebaseAuth.getInstance().getCurrentUser().getUid();
        RootReference = FirebaseDatabase.getInstance().getReference();

        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdatesProfile();
            }
        });
        RetrieveUserInfor();

    }
    private void UpdatesProfile(){
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();
        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please Enter Your Name!", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setUserStatus)){
            Toast.makeText(this, "Please Enter Your Status!", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("status", setUserStatus);
            RootReference.child("Users").child(currentUserID).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        SendUsertoMainActivity();
                        Toast.makeText(SettingsActivity.this, "Your Profile has been uploaded successfully!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        String Message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error : " + Message, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    private void RetrieveUserInfor(){

        RootReference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check Name change or new User add Information
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))) {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
//                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);
                }
                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
//                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);

                }
                else {
                    userName.setEnabled(true);
                    Toast.makeText(SettingsActivity.this, "Please Enter and Update Your profile !", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendUsertoMainActivity(){
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void InitlizeFields(){
        updateProfile = (Button)findViewById(R.id.btn_update_status);
        userName = (EditText)findViewById(R.id.edt_user_name);
        userStatus = (EditText)findViewById(R.id.edt_user_status);
        userProfilesImage = (CircleImageView)findViewById(R.id.img_profile);
    }
}
