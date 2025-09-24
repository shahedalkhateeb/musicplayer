package com.example.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private Thread progressThread;
    private boolean isPlaying = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isPlaying) {
            try {
                mediaPlayer = new MediaPlayer();
                AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.withyoumusic);
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();

                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    Log.d("MusicService", "Music started. Duration = " + mp.getDuration());
                    isPlaying = true;
                    startForeground(1, createNotification());
                    startProgressThread();
                });

                mediaPlayer.prepareAsync();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return START_STICKY;
    }

    private void startProgressThread() {
        progressThread = new Thread(() -> {
            while (mediaPlayer != null && isPlaying) {
                int duration = mediaPlayer.getDuration();
                int current = mediaPlayer.getCurrentPosition();

                if (duration > 0) {
                    int progress = (int) ((current * 100.0f) / duration);

                    Intent updateIntent = new Intent("UPDATE_PROGRESS");
                    updateIntent.putExtra("progress", progress);
                    updateIntent.putExtra("currentTime", current);
                    updateIntent.putExtra("duration", duration);
                    sendBroadcast(updateIntent);

                    Log.d("MusicService", "Progress = " + progress);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        progressThread.start();
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
        stopForeground(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        Intent stopIntent = new Intent(this, MusicService.class);
        stopIntent.setAction("STOP");
        PendingIntent pendingStop = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, "music_channel")
                .setContentTitle("تشغيل الموسيقى")
                .setContentText("الموسيقى قيد التشغيل")
                .setSmallIcon(R.drawable.ic_music_note)
                .addAction(R.drawable.ic_stop, "إيقاف", pendingStop)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }
}














