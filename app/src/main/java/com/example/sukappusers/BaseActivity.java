package com.example.sukappusers;

import android.annotation.SuppressLint;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.lang.reflect.Method;

public class BaseActivity extends AppCompatActivity {

    private BroadcastReceiver internetReceiver;
    private boolean isReceiverRegistered = false;
    private BottomSheetDialog noInternetBottomSheet;
    private StatusBarManager statusBarManager;
    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        internetReceiver = new InternetReceiver();
        registerReceiver(internetReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        isReceiverRegistered = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            statusBarManager = getSystemService(StatusBarManager.class);
        }

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Initialize the bottom sheet
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_offline, null);
        TextView turnOffFlightModeButton = bottomSheetView.findViewById(R.id.btn_fight_off);

        turnOffFlightModeButton.setOnClickListener(v -> {
            // Show the notification drawer
            showNotificationDrawer();
        });


        noInternetBottomSheet = new BottomSheetDialog(this);
        noInternetBottomSheet.setContentView(bottomSheetView);
        noInternetBottomSheet.setCancelable(false);

    }

    private void showNotificationDrawer() {
        try {
            @SuppressLint("WrongConstant") Object statusBarService = getSystemService("statusbar");
            if (statusBarService != null) {
                Class<?> statusBarManagerClass = Class.forName("android.app.StatusBarManager");
                Method expandNotificationsPanelMethod = statusBarManagerClass.getMethod("expandNotificationsPanel");
                expandNotificationsPanelMethod.invoke(statusBarService);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class InternetReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                boolean isInternetConnected = CheckInternet.isInternetConnected(context);
                if (isInternetConnected) {
                    // Internet connected
                    if (noInternetBottomSheet.isShowing()) {
                        noInternetBottomSheet.dismiss();
                    }
                } else {
                    // Internet disconnected
                    showNoInternetBottomSheet();
                }
            }
        }
    }

    private void showNoInternetBottomSheet() {
        if (!isFinishing() && !noInternetBottomSheet.isShowing()) {
            noInternetBottomSheet.show();
        }
    }

    private void showBackOnlineMessage() {
        Toast.makeText(this, "Back Online!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isReceiverRegistered) {
            unregisterReceiver(internetReceiver);
            isReceiverRegistered = false;
        }
        if (noInternetBottomSheet.isShowing()) {
            noInternetBottomSheet.dismiss();
            showBackOnlineMessage();
        }
    }

}
