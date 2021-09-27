package com.DahiApp.mdsound.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.DahiApp.mdsound.Model.Sound;
import com.DahiApp.mdsound.R;
import com.DahiApp.mdsound.UI.MainActivity.MainActivity;

import java.util.List;

public class SoundPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {


    private static final String TAG = "test";
    private static final String CHANNEL_ID = "Channel_md_sound";
    private final Binder mBinder = new SoundServiceBinder();
    private MediaPlayer mediaPlayer;
    private List<Sound> soundList;


    public class SoundServiceBinder extends Binder {
        public SoundPlayerService getServices() {
            return SoundPlayerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        switch (intent.getAction()) {

            case Keys.MUSIC_SERVICE_ACTION_PLAY: {
                Log.d(TAG, "onStartCommand: play called");
                if (soundList != null && !soundList.isEmpty()) {
                    play();
                }
                break;
            }
            case Keys.MUSIC_SERVICE_ACTION_PAUSE: {
                Log.d(TAG, "onStartCommand: pause called");
                pause();
                break;
            }
            case Keys.MUSIC_SERVICE_ACTION_STOP: {
                Log.d(TAG, "onStartCommand: stop called");
                stopForeground(true);
                stopSelf();
            }
            case Keys.MUSIC_SERVICE_ACTION_START: {
                Log.d(TAG, "onStartCommand: start called");
                initMediaPlayer();
                break;
            }
            default: {
                stopSelf();
            }
        }

        Log.d(TAG, "onStartCommand: ");
        return START_NOT_STICKY;
    }

    private void showNotification(Sound sound) {


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        //Intent for play button
        Intent pIntent = new Intent(this, SoundPlayerService.class);
        pIntent.setAction(Keys.MUSIC_SERVICE_ACTION_PLAY);

        PendingIntent playIntent = PendingIntent.getService(this, 100, pIntent, 0);

        //Intent for pause button
        Intent psIntent = new Intent(this, SoundPlayerService.class);
        psIntent.setAction(Keys.MUSIC_SERVICE_ACTION_PAUSE);

        PendingIntent pauseIntent = PendingIntent.getService(this, 100, psIntent, 0);

        //Intent for stop button
        Intent sIntent = new Intent(this, SoundPlayerService.class);
        sIntent.setAction(Keys.MUSIC_SERVICE_ACTION_STOP);

        PendingIntent stopIntent = PendingIntent.getService(this, 100, sIntent, 0);

        //Intent for stop button
        Intent mIntent = new Intent(this, MainActivity.class);
        sIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent mainIntent = PendingIntent.getActivity(
                this, 0, mIntent, 0);

        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(),R.layout.notification_large);
        notificationLayoutExpanded.setTextViewText(R.id.notification_title,sound.getTitle());
        notificationLayoutExpanded.setTextViewText(R.id.notification_text,sound.getArtistName());


        builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(mainIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Add media control buttons that invoke intents in your media service
                .addAction(R.drawable.ic_prev, "Previous", playIntent) // #0
                .addAction(R.drawable.ic_pause, "Pause", pauseIntent)  // #1
                .addAction(R.drawable.ic_next, "Next", stopIntent)     // #2
                // Apply the media style template
                .setStyle(new NotificationCompat.MediaStyle()
                        .setMediaSession(mySession))
                .setContentTitle("Wonderful music")
                .setContentText("My Awesome Band")
                .setLargeIcon(albumArtBitmap);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            final NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Sound", importance);
            channel.setDescription("control sound you will play it.");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            final NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        startForeground(123, builder.build());
    }

    public void initMediaPlayer() {
        // ...initialize the MediaPlayer here...
        // ... other initialization here ...
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);


    }

    public void setSoundList(List<Sound> sounds) {
        if (sounds != null && !sounds.isEmpty()) {
            soundList = sounds;
        }
    }


    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void pause() {
        if (isPlaying())
            mediaPlayer.pause();
    }

    public void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        Log.d(TAG, "play: " + mediaPlayer);
    }

    public void playSound(int position) {
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, soundList.get(position).getId());


        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), contentUri);
            mediaPlayer.prepareAsync();
            showNotification(soundList.get(position));
        } catch (Exception e) {
            Log.d(TAG, "playSong: " + e.getMessage() + soundList.get(position).getId());
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(TAG, "onRebind: ");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        if (mediaPlayer != null)
            mediaPlayer.release();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }
}
