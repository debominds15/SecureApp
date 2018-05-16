package com.bdebo.secureapp.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bdebo.secureapp.fragment.AppFragment;
import com.bdebo.secureapp.fragment.DeviceLock;

/**
 * Adapter class for App/Device Fragment state pager
 */

public class CustomAppPageAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = CustomAppPageAdapter.class.getSimpleName();
    private static final int FRAGMENT_COUNT = 2;
    public CustomAppPageAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new AppFragment();
            case 1:

                return new DeviceLock();
        }
        return null;
    }
    @Override
    public int getCount() {
        return FRAGMENT_COUNT;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Apps";
            case 1:
                return "Device";
        }
        return null;
    }
}