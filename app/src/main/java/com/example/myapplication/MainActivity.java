package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.myapplication.utilities.ConnectivityChangeReceiver;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver mNetworkReceiver;


    void Checking() {

        mNetworkReceiver = new ConnectivityChangeReceiver();
        registerReceiver(mNetworkReceiver, new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION
        ));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    void showNetworkMessage(boolean isConnected) {
        if (!isConnected) {
            setContentView(R.layout.activity_main);
            AppCompatButton retry = findViewById(R.id.retry);
            retry.setOnClickListener(v -> recreate());

        }
    }

    private void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterNetworkChanges();
    }
}