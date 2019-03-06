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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;


public class PhoneLoginActivity extends AppCompatActivity {

    private Button buttonVerify, buttonSendCode;
    private EditText editTextPhoneNumber, editTextCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        InitializeFields();

        buttonSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phoneNumber = editTextPhoneNumber.getText().toString();

                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please Enter Your Phone Number", Toast.LENGTH_SHORT).show();
                } else {
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextCode.setVisibility(View.INVISIBLE);
                buttonVerify.setVisibility(View.INVISIBLE);

                String verificationCode = editTextCode.getText().toString();
                if (TextUtils.isEmpty(verificationCode)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please Enter Verify Code", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("please wait, your code is verifying!");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);

                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number! Please try another Phone Number", Toast.LENGTH_SHORT).show();
                editTextCode.setVisibility(View.INVISIBLE);
                buttonVerify.setVisibility(View.INVISIBLE);
                editTextPhoneNumber.setVisibility(View.VISIBLE);
                buttonSendCode.setVisibility(View.VISIBLE);
            }

            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent, please wait a second!", Toast.LENGTH_SHORT).show();
                editTextCode.setVisibility(View.VISIBLE);
                buttonVerify.setVisibility(View.VISIBLE);
                editTextPhoneNumber.setVisibility(View.INVISIBLE);
                buttonSendCode.setVisibility(View.INVISIBLE);
                loadingBar.dismiss();
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                            SendUsertoMainActivity();
                        } else {
                            String Msg = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Eror: " + Msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void InitializeFields() {
        buttonSendCode = (Button) findViewById(R.id.img_btn_send_vertify_code);
        buttonVerify = (Button) findViewById(R.id.img_btn_vertify);
        editTextCode = (EditText) findViewById(R.id.edt_Verify_code);
        editTextPhoneNumber = (EditText) findViewById(R.id.edt_phone_number);
        loadingBar = new ProgressDialog(PhoneLoginActivity.this);
    }

    private void SendUsertoMainActivity() {
        Intent phoneLoginIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        phoneLoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(phoneLoginIntent);
        finish();
    }

}
