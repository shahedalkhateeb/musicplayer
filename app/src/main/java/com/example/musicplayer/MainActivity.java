package com.example.musicplayer;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnPlay, btnStop;
    TextView tvStatus, tvTime;
    ProgressBar progressBar;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "music_channel",
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        btnPlay = findViewById(R.id.btnPlay);
        btnStop = findViewById(R.id.btnStop);
        tvStatus = findViewById(R.id.tvStatus);
        progressBar = findViewById(R.id.progressBar);
        tvTime = findViewById(R.id.tvTime);

        progressBar.setMax(100);

        btnPlay.setOnClickListener(v -> {
            startService(new Intent(MainActivity.this, MusicService.class));
            tvStatus.setText("الحالة: قيد التشغيل");
        });

        btnStop.setOnClickListener(v -> {
            stopService(new Intent(MainActivity.this, MusicService.class));
            tvStatus.setText("الحالة: متوقف");
            progressBar.setProgress(0);
            tvTime.setText("00:00 / 00:00");
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int progress = intent.getIntExtra("progress", 0);
                int currentMillis = intent.getIntExtra("currentTime", 0);
                int durationMillis = intent.getIntExtra("duration", 0);

                progressBar.setProgress(progress);

                int currentSec = currentMillis / 1000;
                int currentMin = currentSec / 60;
                currentSec = currentSec % 60;

                int durationSec = durationMillis / 1000;
                int durationMin = durationSec / 60;
                durationSec = durationSec % 60;

                String timeFormatted = String.format("%02d:%02d / %02d:%02d",
                        currentMin, currentSec, durationMin, durationSec);

                tvTime.setText(timeFormatted);

                Log.d("MainActivity", "Broadcast received: progress=" + progress + ", time=" + timeFormatted);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, new IntentFilter("UPDATE_PROGRESS"), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(receiver, new IntentFilter("UPDATE_PROGRESS"));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }
}

















