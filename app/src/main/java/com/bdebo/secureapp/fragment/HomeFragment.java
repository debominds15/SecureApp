package com.bdebo.secureapp.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bdebo.secureapp.adapter.CustomAppPageAdapter;
import com.bdebo.secureapp.provider.DBHelper;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.activity.AllThemesLockScreenActivity;
import com.bdebo.secureapp.activity.HelpActivity;
import com.bdebo.secureapp.model.Device;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;

/**
 * This class is used to manage App and Device fragment
 * using ViewPager
 */
public class HomeFragment extends Fragment{
    private TabLayout tabLayout;
    private ViewPager mViewPager;
    private String TAG = HomeFragment.class.getSimpleName();
    private SharedPreferences prefs;
    private static int ALL_THEME_IMAGES=1004;
    private  Device device;
    private boolean menuShow = false;
    private SecureUtil util;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);//Without this onOptionsSelected method wont work in fragment
        tableLayout(rootView);
        prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        util = new SecureUtil(getActivity());
        device = util.getDeviceLockDetails(1);
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_device_lock_theme:
                Intent intent = new Intent(getActivity(),AllThemesLockScreenActivity.class);
                intent.putExtra(AppConstant.IS_DEVICE_THEME_TO_BE_SET,true);
                startActivityForResult(intent,ALL_THEME_IMAGES);
                break;
            case R.id.device_action_lock_invisible_pattern:
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                if(item.isChecked()){
                    callToastMessage("Invisible Pattern");
                    prefs.edit().putBoolean(AppConstant.IS_INVISIBLE_PATTERN_SET,true).commit();
                    item.setChecked(true);
                }
                else{
                    callToastMessage("Visible Pattern");
                    prefs.edit().putBoolean(AppConstant.IS_INVISIBLE_PATTERN_SET,false).commit();
                    item.setChecked(false);
                }
                break;

        }
            return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem register = menu.findItem(R.id.action_device_lock_theme);
        MenuItem device_invisible_pattern= menu.findItem(R.id.device_action_lock_invisible_pattern);
        MenuItem search = menu.findItem(R.id.action_search);
        boolean isDeviceLockPasswordEnabled = prefs.getBoolean(AppConstant.IS_DEVICE_LOCK_ENABLED,false);
        if(menuShow) {
            search.setVisible(false);
            Log.d(TAG,"isDeviceLockPasswordEnabled:: "+isDeviceLockPasswordEnabled);
            if (device != null && isDeviceLockPasswordEnabled) {
                register.setVisible(true);
                if (device.getLockType().equals("Pattern")) {
                    boolean isInvisiblePatternSet = prefs.getBoolean(AppConstant.IS_INVISIBLE_PATTERN_SET, true);
                    device_invisible_pattern.setVisible(true);
                    device_invisible_pattern.setChecked(!isInvisiblePatternSet);
                } else {
                    device_invisible_pattern.setVisible(false);
                }
            }
            else{
                register.setVisible(false);
                device_invisible_pattern.setVisible(false);
            }
        }
        else {
            register.setVisible(false);
            device_invisible_pattern.setVisible(false);
        }
    }

    /**
     * Initializes and set TabSelectedListener
     * to take action based on each Tab
     * @param rootView
     */
    private void tableLayout(View rootView) {
        tabLayout = (TabLayout)rootView.findViewById(R.id.tabs);
        mViewPager = (ViewPager)rootView.findViewById(R.id.view_pager);
        mViewPager.setAdapter(new CustomAppPageAdapter(getChildFragmentManager()));
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        mViewPager.setCurrentItem(tab.getPosition());
                        if (tab.getPosition()==0) {
                            menuShow = false;
                            Log.d(TAG,"onTabSelected App");
                        }
                        else if(tab.getPosition()==1){
                            menuShow = true;
                            Log.d(TAG,"onTabSelected Device");
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null) {
           if (requestCode == ALL_THEME_IMAGES) {
                Bundle extras = data.getExtras();
                int position = extras.getInt(AppConstant.IMAGE_NO);

                if(device != null) {
                    device.setImagePosition(position);
                    util.updateDeviceLockDetails(device, 1);
                }
            }
        }
    }

    /**
     * Display Toast message
     * @param msg
     */
    private void callToastMessage(String msg){
        LayoutInflater inflater = getLayoutInflater(null);
        View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) getActivity().findViewById(R.id.custom_toast_layout));
        SecureUtil.setToastMessage(getActivity(),msg,toastLayout);
    }
}