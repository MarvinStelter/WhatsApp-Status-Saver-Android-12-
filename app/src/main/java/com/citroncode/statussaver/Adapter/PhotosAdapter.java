package com.citroncode.statussaver.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.citroncode.statussaver.Fragments.FragmentPhotos;
import com.citroncode.statussaver.MainActivity;
import com.citroncode.statussaver.R;
import java.io.File;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolderKlasse>  {

    Context c;
    FragmentPhotos fragment;
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
    public PhotosAdapter(FragmentPhotos fragment){
        this.fragment = fragment;
    }

    @Override
    public ViewHolderKlasse onCreateViewHolder(ViewGroup viewGroup, int i) {

        View itemView1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_item, null);

        return new ViewHolderKlasse(itemView1);
    }

    @Override
    public void onBindViewHolder(ViewHolderKlasse viewHolderKlasse, final int i) {




        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                viewHolderKlasse.iv_play.setVisibility(View.GONE);
                viewHolderKlasse.iv_thumbnail.setImageBitmap(((MainActivity)viewHolderKlasse.itemView.getContext()).getBitmapOnAndroidQ(Uri.parse(MainActivity.filePathsPhotos.get(i))));
        }else{
                viewHolderKlasse.iv_play.setVisibility(View.GONE);
                Glide.with(viewHolderKlasse.iv_thumbnail.getContext()).load(new File( MainActivity.filePathsPhotos.get(i))).into(viewHolderKlasse.iv_thumbnail);
        }
        viewHolderKlasse.itemView.setOnClickListener(v -> {


                if( MainActivity.filePathsPhotosChecked.get(i) == "0"){
                    viewHolderKlasse.iv_selected.setVisibility(View.VISIBLE);
                    MainActivity.filePathsPhotosChecked.set(i,"1");
                }else{
                    viewHolderKlasse.iv_selected.setVisibility(View.GONE);
                    MainActivity.filePathsPhotosChecked.set(i,"0");
                }

            fragment.checkFAB();
        });

        viewHolderKlasse.itemView.setOnLongClickListener(v -> {
            return true;
        });

    }
    @Override
    public int getItemCount() {
        return MainActivity.filePathsPhotos.size();
    }




}