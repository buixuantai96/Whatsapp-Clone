package won.kma.buitai.whatsapp;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.gsonparserfactory.GsonParserFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidNetworking.initialize(getApplicationContext());

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        AndroidNetworking.setParserFactory(new GsonParserFactory(gson));

    }
}
