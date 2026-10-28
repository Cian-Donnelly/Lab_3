package com.example.donttouchmyphone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private Button startButton;

    private static final int NOTIFICATION_PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        startButton = findViewById(R.id.startButton);

        startButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 33
                    && checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        NOTIFICATION_PERMISSION_REQUEST
                );

            } else {
                startMonitoring();
            }
        });
    }

    private void startMonitoring() {
        Intent serviceIntent =
                new Intent(MainActivity.this, MotionService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        statusText.setText("Monitoring is active");
        startButton.setEnabled(false);

        Toast.makeText(
                this,
                "Motion monitoring started",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            startMonitoring();
        }
    }
}