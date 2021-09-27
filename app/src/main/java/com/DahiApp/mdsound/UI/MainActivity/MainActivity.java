package com.DahiApp.mdsound.UI.MainActivity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.DahiApp.mdsound.Adapter.SoundAdapter;
import com.DahiApp.mdsound.Utils.Keys;
import com.DahiApp.mdsound.Utils.SoundPlayerService;
import com.DahiApp.mdsound.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "test_main";
    private ActivityMainBinding binding;
    private MainViewModel mainViewModel;
    private SoundPlayerService soundPlayerService;
    private ActivityResultLauncher<String> requestPermission;
    private boolean mBound = false;
    private final ServiceConnection mServiceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {

            SoundPlayerService.SoundServiceBinder soundServiceBinder =
                    (SoundPlayerService.SoundServiceBinder) iBinder;
            soundPlayerService = soundServiceBinder.getServices();
            mBound = true;
            Log.d(TAG, "onServiceConnected");
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                getSoundList();

            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestPermission =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
                    if (result) {
                        getSoundList();
                    }

                });



        Intent intent = new Intent(MainActivity.this, SoundPlayerService.class);
        intent.setAction(Keys.MUSIC_SERVICE_ACTION_START);
        ContextCompat.startForegroundService(this, intent);
    }

    private void getSoundList() {
        mainViewModel.getSoundList(getContentResolver()).observe(this, sounds -> {
            if (sounds != null && !sounds.isEmpty()) {
                soundPlayerService.setSoundList(sounds);
                SoundAdapter soundAdapter = new SoundAdapter(sounds, this,
                        position -> soundPlayerService.playSound(position));
                binding.recycleSound.setAdapter(soundAdapter);
                binding.recycleSound.setLayoutManager(new LinearLayoutManager(this));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called");
        Intent intent=new Intent(MainActivity.this, SoundPlayerService.class);
        bindService(intent,mServiceCon, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBound){
            unbindService(mServiceCon);
            mBound=false;
        }
    }
}