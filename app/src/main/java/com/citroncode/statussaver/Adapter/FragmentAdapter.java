package com.citroncode.statussaver.Adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.citroncode.statussaver.Fragments.FragmentPhotos;
import com.citroncode.statussaver.Fragments.FragmentVideos;

public class FragmentAdapter extends FragmentPagerAdapter {

    public FragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0)
        {
            fragment = new FragmentPhotos();
        }
        else if (position == 1)
        {
            fragment = new FragmentVideos();
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 0)
        {
            title = "Photos";
        }
        else if (position == 1)
        {
            title = "Videos";
        }

        return title;
    }
}