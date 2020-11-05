package com.example.myapplication;

import android.app.Application;

import androidx.annotation.NonNull;

import com.example.myapplication.utilities.ConnectivityChangeReceiver;

public class App extends Application {
    private static App mInstance;

    @NonNull
    public static synchronized App getInstance() {
        return mInstance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public void setConnectivityListener(@NonNull ConnectivityChangeReceiver.ConnectivityReceiverListener listener) {
        ConnectivityChangeReceiver.connectivityReceiverListener = listener;
    }


}

