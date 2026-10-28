package com.example.donttouchmyphone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.app.PendingIntent;
import android.content.Context;

import androidx.core.app.NotificationCompat;

public class MotionService extends Service implements SensorEventListener {

    private static final String CHANNEL_ID = "motion_monitor_channel";
    private static final int FOREGROUND_NOTIFICATION_ID = 1;
    private static final int MOTION_NOTIFICATION_ID = 2;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private HandlerThread sensorThread;
    private Handler sensorHandler;
    private Handler mainHandler;

    private static final float MOVEMENT_THRESHOLD = 2.5f;
    private static final long COOLDOWN_MS = 5000;

    private long lastMotionTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        mainHandler = new Handler(Looper.getMainLooper());

        createNotificationChannel();

        // A foreground service must display a notification.
        startForeground(
                FOREGROUND_NOTIFICATION_ID,
                createForegroundNotification()
        );

        sensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(
                    Sensor.TYPE_ACCELEROMETER
            );
        }

        // Monitor sensor events on a dedicated background thread.
        sensorThread = new HandlerThread("MotionSensorThread");
        sensorThread.start();
        sensorHandler = new Handler(sensorThread.getLooper());

        if (accelerometer != null) {
            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    sensorHandler
            );
        }
    }

    private Notification createForegroundNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Don't Touch My Phone")
                .setContentText("Monitoring phone movement")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Motion Monitoring",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Calculate acceleration magnitude.
        double magnitude = Math.sqrt(
                x * x + y * y + z * z
        );

        // Gravity is approximately 9.81 m/s².
        double movement = Math.abs(magnitude - 9.81);

        long currentTime = System.currentTimeMillis();

        if (movement > MOVEMENT_THRESHOLD
                && currentTime - lastMotionTime > COOLDOWN_MS) {

            lastMotionTime = currentTime;

            // Sensor events run on the sensor thread.
            // Post notification work to the main thread.
            mainHandler.post(() -> showMotionNotification(currentTime));
        }
    }

    private void showMotionNotification(long motionTime) {
        String formattedTime = android.text.format.DateFormat
                .format("dd MMM yyyy, HH:mm:ss", motionTime)
                .toString();

        Intent detailsIntent = new Intent(
                this,
                MotionDetailsActivity.class
        );

        detailsIntent.putExtra("motion_time", formattedTime);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                10,
                detailsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("Motion Detected!")
                        .setContentText(
                                "Your phone moved at " + formattedTime
                        )
                        .setSmallIcon(android.R.drawable.ic_menu_compass)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .build();

        NotificationManager manager =
                (NotificationManager)
                        getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager != null) {
            manager.notify(MOTION_NOTIFICATION_ID, notification);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No action needed when sensor accuracy changes.
    }

    @Override
    public void onDestroy() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        if (sensorThread != null) {
            sensorThread.quitSafely();
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}