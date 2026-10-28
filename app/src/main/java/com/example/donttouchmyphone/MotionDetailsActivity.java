package com.example.donttouchmyphone;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.Gravity;

import androidx.appcompat.app.AppCompatActivity;

public class MotionDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String motionTime = getIntent().getStringExtra("motion_time");

        if (motionTime == null) {
            motionTime = "No motion details available";
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("Motion Details");
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 32);

        TextView status = new TextView(this);
        status.setText("Status: Motion Detected");
        status.setTextSize(20);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 0, 0, 24);

        TextView time = new TextView(this);
        time.setText("Date and time:\n" + motionTime);
        time.setTextSize(18);
        time.setGravity(Gravity.CENTER);

        layout.addView(title);
        layout.addView(status);
        layout.addView(time);

        setContentView(layout);
    }
}