package com.DahiApp.mdsound.Utils;

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
    private int soundPosition = -1;
    private RemoteViews notificationLayoutExpanded;
    private NotificationCompat.Builder builder;


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

            case Keys.MUSIC_SERVICE_ACTION_PREVIOUS: {
                Log.d(TAG, "onStartCommand: play called");
                previous();
                break;
            }
            case Keys.MUSIC_SERVICE_ACTION_PLAY: {
                Log.d(TAG, "onStartCommand: pause called");
                play();
                break;
            }
            case Keys.MUSIC_SERVICE_ACTION_NEXT: {
                Log.d(TAG, "onStartCommand: next called");
                next();
                break;
            }
            case Keys.MUSIC_SERVICE_ACTION_STOP: {
                Log.d(TAG, "onStartCommand: stop called");
                stopForeground(true);
                stopSelf();
                break;
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


        builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        //Intent for previous button
        Intent prevIntent = new Intent(this, SoundPlayerService.class);
        prevIntent.setAction(Keys.MUSIC_SERVICE_ACTION_PREVIOUS);

        PendingIntent previousIntent = PendingIntent.getService(this, 100, prevIntent, 0);

        //Intent for pause button
        Intent psIntent = new Intent(this, SoundPlayerService.class);
        psIntent.setAction(Keys.MUSIC_SERVICE_ACTION_PLAY);

        PendingIntent playIntent = PendingIntent.getService(this, 100, psIntent, 0);

        //Intent for next button
        Intent nIntent = new Intent(this, SoundPlayerService.class);
        nIntent.setAction(Keys.MUSIC_SERVICE_ACTION_NEXT);

        PendingIntent nextIntent = PendingIntent.getService(this, 100, nIntent, 0);


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

        notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_large);
        notificationLayoutExpanded.setTextViewText(R.id.notification_title, sound.getTitle());
        notificationLayoutExpanded.setTextViewText(R.id.notification_text, sound.getArtistName());

        notificationLayoutExpanded.setImageViewResource(R.id.prev_notification, R.drawable.ic_prev);
        notificationLayoutExpanded.setImageViewResource(R.id.pause_notification, R.drawable.ic_pause);
        notificationLayoutExpanded.setImageViewResource(R.id.next_notification, R.drawable.ic_next);

        notificationLayoutExpanded.setOnClickPendingIntent(R.id.prev_notification, previousIntent);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.pause_notification, playIntent);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.next_notification, nextIntent);


        builder
                .setCustomBigContentView(notificationLayoutExpanded)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(mainIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                // Add media control buttons that invoke intents in your media service
                // Apply the media style template



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
            soundPosition = 0;
            soundList = sounds;
        }
    }


    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void previous() {
        if (soundPosition == -1 || soundPosition == 0)
            return;
        soundPosition--;
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, soundList.get(soundPosition).getId());


        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), contentUri);
            mediaPlayer.prepareAsync();
            showNotification(soundList.get(soundPosition));
        } catch (Exception e) {
            Log.d(TAG, "playSong: " + e.getMessage() + soundList.get(soundPosition).getId());
        }
    }

    public void next() {
        if (soundPosition == -1 || soundPosition == soundList.size())
            return;
        soundPosition++;
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, soundList.get(soundPosition).getId());


        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), contentUri);
            mediaPlayer.prepareAsync();
            showNotification(soundList.get(soundPosition));
        } catch (Exception e) {
            Log.d(TAG, "playSong: " + e.getMessage() + soundList.get(soundPosition).getId());
        }
    }

    public void play() {
        if (mediaPlayer == null)
            return;
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            notificationLayoutExpanded.setImageViewResource(R.id.pause_notification, R.drawable.ic_pause);
        } else {
            mediaPlayer.pause();
            notificationLayoutExpanded.setImageViewResource(R.id.pause_notification, R.drawable.ic_play);
        }
        builder.setCustomBigContentView(notificationLayoutExpanded);
        startForeground(123, builder.build());
        Log.d(TAG, "play: " + mediaPlayer);
    }

    public void playSound(int position) {
        soundPosition = position;
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
        stopForeground(true);
        stopSelf();
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
