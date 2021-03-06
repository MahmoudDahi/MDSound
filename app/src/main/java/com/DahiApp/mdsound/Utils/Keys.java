package com.DahiApp.mdsound.Utils;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;

public class Keys {
    public static final String MUSIC_SERVICE_ACTION_START = "com.example.android.start";
    public static final String MUSIC_SERVICE_ACTION_PLAY = "com.example.android.play";
    public static final String MUSIC_SERVICE_ACTION_PREVIOUS = "com.example.android.previous";
    public static final String MUSIC_SERVICE_ACTION_NEXT = "com.example.android.next";
    public static final String MUSIC_SERVICE_ACTION_STOP = "com.example.android.stop";

    public static Uri getUriForSound(long soundID) {

        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");

        return ContentUris.withAppendedId(sArtworkUri, soundID);
    }
}
