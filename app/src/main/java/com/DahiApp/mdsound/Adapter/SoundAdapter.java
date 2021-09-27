package com.DahiApp.mdsound.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DahiApp.mdsound.Model.Sound;
import com.DahiApp.mdsound.Utils.Keys;
import com.DahiApp.mdsound.Utils.OnClickAdapterListener;
import com.DahiApp.mdsound.databinding.ItemSoundBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SoundAdapter extends RecyclerView.Adapter<SoundAdapter.ViewHolder> {
    private final List<Sound> soundList;
    private final OnClickAdapterListener onClickAdapterListener;
    private final Context context;

    public SoundAdapter(List<Sound> soundList, Context context, OnClickAdapterListener onClickAdapterListener) {
        this.soundList = soundList;
        this.onClickAdapterListener = onClickAdapterListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemSoundBinding.inflate(LayoutInflater.from(parent.getContext()), parent
                , false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Sound sound = soundList.get(position);
        Picasso.get().load(Keys.getBitmapForSound(sound.getAlbumId())).fit()
                .into(holder.binding.imageListSong);
        holder.binding.imageListSong.setClipToOutline(true);
        holder.binding.songName.setText(sound.getTitle());
        holder.binding.singerName.setText(sound.getArtistName());

        holder.binding.getRoot().setOnClickListener(view -> onClickAdapterListener.onClickItem(position));
    }

    @Override
    public int getItemCount() {
        return soundList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemSoundBinding binding;

        public ViewHolder(@NonNull ItemSoundBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
