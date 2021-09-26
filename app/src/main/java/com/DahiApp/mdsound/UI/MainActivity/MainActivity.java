package com.DahiApp.mdsound.UI.MainActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.DahiApp.mdsound.Adapter.SoundAdapter;
import com.DahiApp.mdsound.R;
import com.DahiApp.mdsound.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActivityResultLauncher<String> requestPermission =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
                    if (result) {
                        getSoundList();
                    }

                });

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            getSoundList();

        }
    }

    private void getSoundList() {
        mainViewModel.getSoundList(getContentResolver()).observe(this, sounds -> {
            if (sounds != null && !sounds.isEmpty()) {
                SoundAdapter soundAdapter = new SoundAdapter(sounds, this, position -> {

                });
                binding.recycleSound.setAdapter(soundAdapter);
                binding.recycleSound.setLayoutManager(new LinearLayoutManager(this));
            }
        });
    }
}