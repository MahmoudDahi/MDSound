package com.DahiApp.mdsound.Utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class SoundPlayerService extends Service {

    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}