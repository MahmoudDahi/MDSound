package com.DahiApp.mdsound.UI.MainActivity;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.DahiApp.mdsound.Model.Sound;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = "test";
    private MutableLiveData<List<Sound>> liveDataSoundList;

    public MainViewModel(@NonNull Application application) {
        super(application);
    }


    public LiveData<List<Sound>> getSoundList(ContentResolver contentResolver) {
        if (liveDataSoundList == null) {
            liveDataSoundList = new MutableLiveData<>();
            loadSoundList(contentResolver);
        }
        return liveDataSoundList;
    }

    private void loadSoundList(ContentResolver contentResolver) {
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";


        try (Cursor songCursor = contentResolver.query(songUri,
                null, selection, null, null)) {

            if (songCursor != null && songCursor.moveToFirst()) {
                int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int singer = songCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int songDuration = songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int soundAlbum = songCursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                List<Sound> soundList = new ArrayList<>();
                do {
                    String currentTitle = songCursor.getString(songTitle);
                    String currentSinger = songCursor.getString(singer);
                    Long currentDuration = songCursor.getLong(songDuration);
                    Long currentLocation = songCursor.getLong(songLocation);
                    Long albumId = songCursor.getLong(soundAlbum);
                    soundList.add(new Sound(currentTitle, currentSinger, currentLocation, albumId, currentDuration));
                } while (songCursor.moveToNext());
                liveDataSoundList.postValue(soundList);
            }
        }

    }
}
