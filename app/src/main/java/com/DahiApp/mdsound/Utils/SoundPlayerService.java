package com.DahiApp.mdsound.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.DahiApp.mdsound.Model.Sound;
import com.DahiApp.mdsound.R;
import com.DahiApp.mdsound.UI.MainActivity.MainActivity;

import java.io.IOException;
import java.util.List;

public class SoundPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {


    private static final String TAG = "test";
    private static final String CHANNEL_ID = "Channel_md_sound";
    private final Binder mBinder = new SoundServiceBinder();
    private MediaPlayer mediaPlayer;
    private List<Sound> soundList;
    private int soundPosition = -1;
    private NotificationCompat.Builder builder;
    private MediaSessionCompat mediaSession;

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
                mediaPlayer.reset();
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

        //Intent for home button
        Intent mIntent = new Intent(this, MainActivity.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent mainIntent = PendingIntent.getActivity(
                this, 0, mIntent, 0);


        Bitmap photo;
        try {
            photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Keys.getUriForSound(sound.getAlbumId()));
        } catch (IOException e) {

            photo = BitmapFactory.decodeResource(getResources(), R.drawable.default_sound);
        }

        setBuilderAction(R.drawable.ic_pause);


        MediaMetadataCompat mediaMetadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, sound.getTitle())
                .putString(MediaMetadata.METADATA_KEY_ARTIST, sound.getArtistName())
                .putLong(MediaMetadata.METADATA_KEY_DURATION, mediaPlayer.getDuration())
                .build();
        mediaSession.setMetadata(mediaMetadata);

        int currentPosition = mediaPlayer.getCurrentPosition();
        PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, currentPosition, 1)
                .build();
        mediaSession.setPlaybackState(playbackState);
        mediaSession.setActive(true);

        builder
                .setContentTitle(sound.getTitle())
                .setContentText(sound.getArtistName())
                .setLargeIcon(photo)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken()))
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setColor(getApplication().getResources().getColor(R.color.purple_200))
                .setSmallIcon(R.drawable.md_sound)
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


    private void setBuilderAction(int ic_drawable_pause_play) {
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

        builder.addAction(R.drawable.ic_prev, "prev", previousIntent)
                .addAction(ic_drawable_pause_play, "pause", playIntent)
                .addAction(R.drawable.ic_next, "next", nextIntent)
                .addAction(R.drawable.ic_stop, "next", stopIntent);
    }

    public void initMediaPlayer() {
        // ...initialize the MediaPlayer here...
        // ... other initialization here ...
        if (mediaPlayer != null) {
            return;
        }
        stopForeground(true);
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
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                next();
            }
        });

        mediaSession = new MediaSessionCompat(getApplicationContext(), getPackageName());

    }

    public void setSoundList(List<Sound> sounds) {
        if (sounds != null && !sounds.isEmpty()) {
            soundPosition = 0;
            soundList = sounds;
        }
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
        if (soundPosition == -1 || soundPosition == soundList.size() - 1)
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
        builder.clearActions();
        PlaybackStateCompat playbackState;
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            setBuilderAction(R.drawable.ic_pause);
            int currentPosition = mediaPlayer.getCurrentPosition();
            playbackState = new PlaybackStateCompat.Builder()
                    .setActions(0L)
                    .setState(PlaybackStateCompat.STATE_PLAYING, currentPosition, 1)
                    .build();
        } else {
            mediaPlayer.pause();
            setBuilderAction(R.drawable.ic_play);
            playbackState = new PlaybackStateCompat.Builder()
                    .setActions(0L)
                    .setState(PlaybackStateCompat.STATE_PAUSED, mediaPlayer.getCurrentPosition()
                            , 0)
                    .build();

        }
        mediaSession.setPlaybackState(playbackState);
        startForeground(123, builder.build());
        Log.d(TAG, "play: " + mediaPlayer);
    }

    public void playSound(int position) {
        Log.d(TAG, "playSound: ");
        if (mediaPlayer == null)
            return;
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
        mediaSession.release();
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
