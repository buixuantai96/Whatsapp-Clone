package won.kma.buitai.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.iid.FirebaseInstanceId;
import com.virgilsecurity.android.ethree.kotlin.interaction.EThree;

import won.kma.buitai.whatsapp.helper.VirgilHelper;


public class RegisterActivity extends AppCompatActivity {
    private Button btnRegister;
    private EditText edtRegisterEmail;
    private EditText edtRegisterPassword;
    private TextView tvHaveAccount;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference RootRef;

    private String Email;
    private String Password;

    private String userID;

    public VirgilHelper virgilHelper = new VirgilHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        virgilHelper = new VirgilHelper(RegisterActivity.this);
        setContentView(R.layout.activity_register);
        firebaseAuth = FirebaseAuth.getInstance();
        InitializeFields();
        RootRef = FirebaseDatabase.getInstance().getReference();


        tvHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUsertoLoginActivity();
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Email = edtRegisterEmail.getText().toString();
                Password = edtRegisterPassword.getText().toString();

                RegisterAccount();
            }
        });
    }

    private void RegisterAccount() {

        // textUtils always return true even String= null or String.lenght() = 0;
        if (TextUtils.isEmpty(Email)) {
            Toast.makeText(this, "Please Enter Your Email...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(Password)) {
            Toast.makeText(this, "Please Enter Your Password...", Toast.LENGTH_SHORT).show();
        }
        //Create Account check firebaseAuth: xem lai phan FirebaseAuth sau
        else {
            loadingBar.setTitle("Creating new Account");
            loadingBar.setMessage("Please waitting! while we are creatting new account...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            firebaseAuth.createUserWithEmailAndPassword(Email, Password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //get Uid and token phoneID was registed from users;
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                String currentUserID = firebaseAuth.getUid();
                                virgilHelper.initUser( RegisterActivity.this);

                                RootRef.child("Users").child(currentUserID).setValue("");
                                RootRef.child("Users").child(currentUserID).child("device_token").setValue(deviceToken);

                                SendUsertoMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account Create Successfully... ", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            } else {
                                String Message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error : " + Message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitializeFields() {
        edtRegisterEmail = (EditText) findViewById(R.id.edt_Register_Email);
        edtRegisterPassword = (EditText) findViewById(R.id.edt_Register_Password);
        tvHaveAccount = (TextView) findViewById(R.id.tv_Have_Account);
        btnRegister = (Button) findViewById(R.id.btn_Create_Account);
        loadingBar = new ProgressDialog(this);
    }

    private void SendUsertoLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void SendUsertoMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    final EThree.OnCompleteListener onRegisterListener = new EThree.OnCompleteListener() {
        @Override
        public void onSuccess() {
            // User private key loaded, ready to end-to-end encrypt!
        }

        @Override
        public void onError(@NotNull final Throwable throwable) {
            // Error handling
        }
    };


}