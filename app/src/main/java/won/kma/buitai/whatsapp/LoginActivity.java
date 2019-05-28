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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import won.kma.buitai.whatsapp.helper.VirgilHelper;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin, btnPhoneLogin;
    private EditText edtLoginEmail, edtLoginPassword;
    private TextView tvNewAccount, tvForgetPassword;
    private ProgressDialog loadingBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference UserRef;

    VirgilHelper virgilHelper = new VirgilHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");


        InitlizeFields();

        tvNewAccount.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                SendUserToRegisterActivity();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AllowUsersLogin();
            }
        });
        btnPhoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToPhoneLoginActivity();
            }
        });
    }

    protected void onStart() {
        super.onStart();
        if (currentUser != null){
            SendUserToMainActivity();
        }

    }

    private void AllowUsersLogin(){
        loadingBar.setTitle("Login");
        loadingBar.setMessage("Please wait a second...!");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        final String Email = edtLoginEmail.getText().toString();
        final String Password = edtLoginPassword.getText().toString();
        if (TextUtils.isEmpty(Email)){
            Toast.makeText(this , "Please Enter your Email!", Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();
        }
        if(TextUtils.isEmpty(Password)){
            Toast.makeText(this, "Please Enter Your Password!", Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();
        }
        else{
            firebaseAuth.signInWithEmailAndPassword(Email, Password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                virgilHelper.initUser(LoginActivity.this);

                                String currentUserID = firebaseAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                UserRef.child(currentUserID).child("device_token")
                                        .setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            //after that allow user login;
                                            SendUserToMainActivity();
                                            Toast.makeText(LoginActivity.this, "Login...", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                            }
                            else{
                                String Message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error : " + Message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });

        }
    }

//    final EThree.OnCompleteListener onRegisterListener = new EThree.OnCompleteListener() {
//        @Override public void onSuccess() {
//            // User private key loaded, ready to end-to-end encrypt!
//        }
//
//        @Override public void onError(@NotNull final Throwable throwable) {
//            // Error handling
//        }
//    };

    private void InitlizeFields(){
        btnLogin = (Button)findViewById(R.id.btn_Login);
        btnPhoneLogin = (Button)findViewById(R.id.btn_Login_Phone);
        edtLoginEmail = (EditText) findViewById(R.id.edt_Login_Email);
        edtLoginPassword = (EditText) findViewById(R.id.edt_Login_Password);
        tvForgetPassword = (TextView) findViewById(R.id.tv_Forget_password);
        tvNewAccount = (TextView) findViewById(R.id.tv_NewAccount);
        loadingBar = new ProgressDialog(this);
    }

    private void SendUserToMainActivity(){
        Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToRegisterActivity(){
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    private void SendUserToPhoneLoginActivity(){
        Intent phoneLoginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
        startActivity(phoneLoginIntent);
    }

}
