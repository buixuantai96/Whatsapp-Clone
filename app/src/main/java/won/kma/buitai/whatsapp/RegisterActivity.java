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

    String authToken;
    EThree eThree;
    private String userID;

    public VirgilHelper virgilHelper = new VirgilHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        btnRegister.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Email = edtRegisterEmail.getText().toString();
                Password = edtRegisterPassword.getText().toString();

                RegisterAccount();
            }
        });
    }

    private void RegisterAccount(){

        // textUtils always return true even String= null or String.lenght() = 0;
        if(TextUtils.isEmpty(Email)){
            Toast.makeText(this, "Please Enter Your Email...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(Password)){
            Toast.makeText(this, "Please Enter Your Password...", Toast.LENGTH_SHORT).show();
        }
        //Create Account check firebaseAuth: xem lai phan FirebaseAuth sau
        else{
            loadingBar.setTitle("Creating new Account");
            loadingBar.setMessage("Please waitting! while we are creatting new account...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            firebaseAuth.createUserWithEmailAndPassword(Email, Password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                //get Uid and token phoneID was registed from users;
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                String currentUserID = firebaseAuth.getUid();
                                virgilHelper.initUser(Email, Password, RegisterActivity.this, onRegisterListener);

                                RootRef.child("Users").child(currentUserID).setValue("");
                                RootRef.child("Users").child(currentUserID).child("device_token").setValue(deviceToken);

                                SendUsertoMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account Create Successfully... ", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else{
                                String Message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error : " + Message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitializeFields(){
        edtRegisterEmail =(EditText)findViewById(R.id.edt_Register_Email);
        edtRegisterPassword =(EditText)findViewById(R.id.edt_Register_Password);
        tvHaveAccount =(TextView) findViewById(R.id.tv_Have_Account);
        btnRegister =(Button) findViewById(R.id.btn_Create_Account);
        loadingBar = new ProgressDialog(this);
    }

    private void SendUsertoLoginActivity(){
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void SendUsertoMainActivity(){
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    final EThree.OnCompleteListener onRegisterListener = new EThree.OnCompleteListener() {
        @Override public void onSuccess() {
            // User private key loaded, ready to end-to-end encrypt!
        }

        @Override public void onError(@NotNull final Throwable throwable) {
            // Error handling
        }
    };


//
//    final EThree.OnGetTokenCallback onGetTokenCallback = new EThree.OnGetTokenCallback() {
//        @NotNull
//        @Override public String onGetToken() {
//            return getVirgilJwt(authToken);
//        }
//    };
//
//    final EThree.OnResultListener<EThree> onInitListener = new EThree.OnResultListener<EThree>() {
//        @Override public void onSuccess(EThree result) {
//            // So now you have fully initialized and ready to use EThree instance!
//            eThree = result;
//
//            eThree.register(onRegisterListener);
//        }
//
//        @Override public void onError(@NotNull final Throwable throwable) {
//            // Error handling
//            runOnUiThread(new Runnable() {
//                @Override public void run() {
//                    Toast.makeText(RegisterActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    };
//
//    void initUser() {
//        // You start your user authentication/authorization (signUp/signIn) here.
//        authenticate(Email, Password, new OnResultListener<String>() {
//            @Override public void onSuccess(String value) {
//                authToken = value;
//
//                // After you successfully authenticated your user - you have to initialize EThree SDK.
//                // To do this you have to provide context and two listeners.
//                // OnGetTokenCallback should exchange recently received authToken for a Virgil JWT.
//                // OnResultListener<EThree> will give you initialized instance of EThree SDK in onSuccess method.
//                EThree.initialize(RegisterActivity.this,
//                        onGetTokenCallback,
//                        onInitListener);
//            }
//
//            @Override public void onError(final Throwable throwable) {
//                // Error handling
//            }
//        });
//    }
//
//    /**
//     * In this function auth state been tracked. So if the user is already signed in or signin in/up - callback will be called with *user != null*.
//     *
//     * @param identity         is identity of user for authentication
//     * @param onResultListener is a callback where you you will receive authentication token that you can later exchange
//     *                         for a Virgil JWT. Or error if something went wrong.
//     */
//
//    void authenticate(final String identity,
//                      String password,
//                      final OnResultListener<String> onResultListener) {
//        firebaseAuth.createUserWithEmailAndPassword(identity,
//                password)
//                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            FirebaseUser user = firebaseAuth.getCurrentUser();
//
//                            userID = user.getUid();
//
//                            user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//                                @Override public void onComplete(@NonNull Task<GetTokenResult> task) {
//                                    if (task.isSuccessful()) {
//                                        onResultListener.onSuccess(task.getResult().getToken());
//                                    } else {
//                                        onResultListener.onError(task.getException());
//                                    }
//                                }
//                            });
//                        } else {
//                            onResultListener.onError(task.getException());
//                        }
//                    }
//                });
//    }
//
//    /**
//     * This method exchanges provided authToken for a Virgil JWT.
//     *
//     * @param authToken from your authentication system that signals that user is authenticated successfully.
//     *
//     * @return Virgil JWT base64 string representation.
//     */
//    String getVirgilJwt(String authToken) {
//        try {
//            String url = "https://us-central1-whatsapp-5243a.cloudfunctions.net/api/virgil-jwt";
//            URL object = new URL(url);
//
//            HttpURLConnection con = (HttpURLConnection) object.openConnection();
//            con.setRequestProperty("Authorization", "Bearer " + authToken);
//            con.setRequestMethod("GET");
//
//            StringBuilder sb = new StringBuilder();
//            int HttpResult = con.getResponseCode();
//            if (HttpResult == HttpURLConnection.HTTP_OK) {
//                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
//                String line;
//                while ((line = br.readLine()) != null) {
//                    sb.append(line).append("\n");
//                }
//                br.close();
//                JSONObject jsonObject = new JSONObject(sb.toString());
//
//                return jsonObject.getString("token");
//            } else {
//                throw new RuntimeException("Some connection error");
//            }
//        } catch (IOException exception) {
//            exception.printStackTrace();
//            throw new RuntimeException("Some connection error");
//        } catch (JSONException e) {
//            throw new RuntimeException("Parsing virgil jwt json error");
//        }
//    }
//
//    private interface OnResultListener<T> {
//
//        void onSuccess(T value);
//
//        void onError(Throwable throwable);
//    }
}
