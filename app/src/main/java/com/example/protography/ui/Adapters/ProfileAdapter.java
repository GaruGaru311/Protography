package com.example.protography.ui.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.protography.ui.Fragments.User.TabBookmarksFragment;
import com.example.protography.ui.Fragments.User.TabUploadsFragment;

public class ProfileAdapter extends FragmentStatePagerAdapter {

    private int pageNumber;

    public ProfileAdapter(@NonNull FragmentManager fm, int pageNumber) {
        super(fm);
        this.pageNumber = pageNumber;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                TabUploadsFragment tabUploads = new TabUploadsFragment();
                return tabUploads;
            case 1:
                TabBookmarksFragment tabBookmark = new TabBookmarksFragment();
                return tabBookmark;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {

        return pageNumber;
    }
}
