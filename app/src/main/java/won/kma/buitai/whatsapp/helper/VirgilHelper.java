package won.kma.buitai.whatsapp.helper;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.annotations.NotNull;
import com.virgilsecurity.android.ethree.kotlin.interaction.EThree;
import com.virgilsecurity.sdk.crypto.PublicKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class VirgilHelper {

    String authToken;
    String identity;
    public static EThree eThree;
    public ArrayList<PublicKey> listPublicKey;

    public PublicKey decryptKey;

    public VirgilHelper(){}

    final EThree.OnGetTokenCallback onGetTokenCallback = new EThree.OnGetTokenCallback() {
        @NotNull
        @Override public String onGetToken() {
            return getVirgilJwt(authToken);
        }
    };

    final EThree.OnResultListener<EThree> onInitListener = new EThree.OnResultListener<EThree>() {
        @Override public void onSuccess(EThree result) {
            eThree = result;
            eThree.register(onRegisterListener);
        }
        @Override public void onError(@NotNull final Throwable throwable) {
            // Error handling
        }
    };

    public void findPublicKey(String identity){
        this.identity = identity;
        if (eThree == null) {
            return;
        }
        eThree.lookupPublicKeys(Collections.singletonList(identity), lookupKeysListener);
    }

    final EThree.OnResultListener<Map<String, PublicKey>> lookupKeysListener =
            new EThree.OnResultListener<Map<String, PublicKey>>() {
                @Override public void onSuccess(Map<String, PublicKey> result) {
                    listPublicKey = new ArrayList<>(result.values());
                    decryptKey = result.get(identity);
                }
                @Override public void onError(@NotNull Throwable throwable) {
                }
            };

    public void initUser(final Context context) {
        authenticate(new OnResultListener<String>() {
            @Override public void onSuccess(String value) {
                authToken = value;
                EThree.initialize(context,
                        onGetTokenCallback,
                        onInitListener);
            }

            @Override public void onError(final Throwable throwable) {

            }
        });
    }


    void authenticate(
                      final OnResultListener<String> onResultListener) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        @Override public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                onResultListener.onSuccess(task.getResult().getToken());
                            } else {
                                onResultListener.onError(task.getException());
                            }
                        }
                    });
                }
            }
        });
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

    String getVirgilJwt(String authToken) {
        try {
            String url = "https://us-central1-whatsapp-5243a.cloudfunctions.net/api/virgil-jwt";
            URL object = new URL(url);

            HttpURLConnection con = (HttpURLConnection) object.openConnection();
            con.setRequestProperty("Authorization", "Bearer " + authToken);
            con.setRequestMethod("GET");

            StringBuilder sb = new StringBuilder();
            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                JSONObject jsonObject = new JSONObject(sb.toString());

                return jsonObject.getString("token");
            } else {
                throw new RuntimeException("Some connection error");
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            throw new RuntimeException("Some connection error");
        } catch (JSONException e) {
            throw new RuntimeException("Parsing virgil jwt json error");
        }
    }

    private interface OnResultListener<T> {

        void onSuccess(T value);

        void onError(Throwable throwable);
    }
}
