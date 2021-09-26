package com.DahiApp.mdsound.Utils;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;

public class Keys {

    private static final String TAG = "test";

    public static Bitmap getBitmapForSound(Context context, long soundID) {

        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, soundID);
        try {
            return MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(), albumArtUri);

        } catch (IOException e) {
            Log.d(TAG, "getBitmapForSound: " + e.getMessage());
            return null;
        }
    }
}
