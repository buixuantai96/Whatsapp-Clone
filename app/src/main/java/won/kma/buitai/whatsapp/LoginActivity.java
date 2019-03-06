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

public class LoginActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private Button btnLogin, btnPhoneLogin;
    private EditText edtLoginEmail, edtLoginPassword;
    private TextView tvNewAccount, tvForgetPassword;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();
        InitlizeFields();

        currentUser = firebaseAuth.getCurrentUser();

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
        String Email = edtLoginEmail.getText().toString();
        String Password = edtLoginPassword.getText().toString();
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
                                SendUserToMainActivity();
                                Toast.makeText(LoginActivity.this, "Login...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
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
