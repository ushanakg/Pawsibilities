package com.example.pawsibilities;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.pawsibilities.fragments.MapsFragment;
import com.example.pawsibilities.fragments.ProfileFragment;
import com.example.pawsibilities.fragments.TagListFragment;

public class PagerAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 3;
    public static final int MAP = 0;
    public static final int TAG_LIST = 1;
    public static final int PROFILE = 2;

    public PagerAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case TAG_LIST:
                return new TagListFragment();
            case PROFILE:
                return new ProfileFragment();
            case MAP:
            default:
                return new MapsFragment();
        }
    }
}
