package com.citroncode.statussaver.Adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.citroncode.statussaver.Fragments.FragmentVideos;
import com.citroncode.statussaver.MainActivity;
import com.citroncode.statussaver.R;

import java.io.File;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.ViewHolderKlasse>  {

    Context c;
    FragmentVideos fragment;
    public class ViewHolderKlasse extends RecyclerView.ViewHolder {

        ImageView iv_thumbnail, iv_play, iv_selected;


        public ViewHolderKlasse(View itemView) {
            super(itemView);
            iv_thumbnail = itemView.findViewById(R.id.iv_thumbnail);
            iv_play = itemView.findViewById(R.id.iv_play);
            iv_selected = itemView.findViewById(R.id.iv_checker);
            c = iv_play.getContext();
        }
    }
    public VideosAdapter(FragmentVideos fragment){
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolderKlasse onCreateViewHolder(ViewGroup viewGroup, int i) {

        @SuppressLint("InflateParams")
        View itemView1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_item, null);

        return new ViewHolderKlasse(itemView1);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderKlasse viewHolderKlasse, final int i) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                viewHolderKlasse.iv_play.setVisibility(View.VISIBLE);
                Glide.with(viewHolderKlasse.iv_thumbnail.getContext()).load(MainActivity.filePathsVideos.get(i)).into(viewHolderKlasse.iv_thumbnail);
        }else{
                viewHolderKlasse.iv_play.setVisibility(View.VISIBLE);
                Glide.with(viewHolderKlasse.iv_thumbnail.getContext()).load(getVideoThumbnail(new File(MainActivity.filePathsVideos.get(i)))).into(viewHolderKlasse.iv_thumbnail);
        }
        viewHolderKlasse.itemView.setOnClickListener(v -> {


                if(MainActivity.filePathsVideosChecked.get(i).equals("0")){
                    viewHolderKlasse.iv_selected.setVisibility(View.VISIBLE);
                    MainActivity.filePathsVideosChecked.set(i,"1");
                }else{
                    viewHolderKlasse.iv_selected.setVisibility(View.GONE);
                    MainActivity.filePathsVideosChecked.set(i,"0");
                }

            fragment.checkFAB();
        });

    }
    private Bitmap getVideoThumbnail(File path){
         return ThumbnailUtils.createVideoThumbnail(String.valueOf(path), MediaStore.Images.Thumbnails.MINI_KIND);
    }
    @Override
    public int getItemCount() {
        return MainActivity.filePathsVideos.size();
    }




}