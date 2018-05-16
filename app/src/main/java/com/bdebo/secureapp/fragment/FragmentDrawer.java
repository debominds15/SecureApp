package com.bdebo.secureapp.fragment;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.bdebo.secureapp.receiver.MyAdmin;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.model.User;
import com.bdebo.secureapp.adapter.MyAdapter;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;

import static com.bdebo.secureapp.util.AppConstant.IS_OTHER_APP_OPENED;

/**
 * This class is the fragment drawer
 */
public class FragmentDrawer extends Fragment{
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;
    private SwitchCompat swt;
    private SecureUtil util;
    //First We Declare Titles And Icons For Our Navigation Drawer List View
    //This Icons And Titles Are holded in an Array as you can see

    private String TITLES[] = {"Home", "Profile","Change Password", "About", "Logout"};
    private int ICONS[] = {R.drawable.home_purple, R.drawable.profile_icon,R.drawable.password_purple, R.drawable.help_purple, R.drawable.logout_purple};

    //Similarly we Create a String Resource for the name and email in the header view
    //And we also create a int resource for profile picture in the header view

    private String NAME = "user";
    private String EMAIL = "user123";
    // Declaring the Toolbar Object
    private SharedPreferences prefs;
    private View containerView;

    private RecyclerView mRecyclerView;                           // Declaring RecyclerView
    private RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    private DrawerLayout Drawer;                                  // Declaring DrawerLayout
    private FragmentDrawerListener drawerListener;
    private  ActionBarDrawerToggle mDrawerToggle;                  // Declaring Action Bar Drawer Toggle

    private static final int ADMIN_INTENT = 15;
    public FragmentDrawer() {

    }

    public void setDrawerListener(FragmentDrawerListener listener) {
        this.drawerListener = listener;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        util = new SecureUtil(getActivity());
        EMAIL=prefs.getString(AppConstant.USER_NAME, null);
        NAME=prefs.getString(AppConstant.NAME,null);
        int id=prefs.getInt(AppConstant.USER_ID,0);
        User user=util.getParticularUser(id);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.drawerList);
        swt = (SwitchCompat)layout.findViewById(R.id.securityToggle);
        swt.setChecked (true);
        initDeviceManager();

        swt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    enableAdmin();
                else
                    disableAdmin();
            }
        });
        String imageByteString = prefs.getString(AppConstant.PROFILE_IMAGE_BYTE_ARRAY,null);
        boolean isImageTakenFromCamera = prefs.getBoolean(AppConstant.IS_IMAGE_CAMERA,false);
        Log.d("FragmentDrawer","isImageTakenFromCamera::"+isImageTakenFromCamera);
        mAdapter = new MyAdapter(getActivity(),TITLES, ICONS, user.getName(), user.getUsername(),imageByteString,isImageTakenFromCamera);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                drawerListener.onDrawerItemSelected(view, position);
                Drawer.closeDrawer(containerView);
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
        return layout;
    }

    /**
     * Set up drawer
     * @param fragmentId
     * @param drawerLayout
     * @param toolbar
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {
        containerView = getActivity().findViewById(fragmentId);
        Drawer = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                toolbar.setAlpha(1 - slideOffset / 2);
            }
        };

        Drawer.setDrawerListener(mDrawerToggle);
        Drawer.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

    }

    /**
     * interface click listener
     */
    private interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    /**
     * Method to enable admin
     */
    private void enableAdmin()
    {
        prefs.edit().putBoolean(IS_OTHER_APP_OPENED,true).commit();
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "SecureApp needs to lock the device if more than 3 unauthorised attempts are made while login.");
        startActivityForResult(intent, ADMIN_INTENT);
    }

    /**
     * Method to disable admin
     */
    private void disableAdmin()
    {
        mDevicePolicyManager.removeActiveAdmin(mComponentName);
    }

    /**
     * Initializes device policy manager
     */
    public void initDeviceManager(){

        mDevicePolicyManager = (DevicePolicyManager)getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(getActivity(), MyAdmin.class);
    }

    /**
     * interface for fragment drawer listener
     */
    public interface FragmentDrawerListener {
        public void onDrawerItemSelected(View view, int position);
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean isAdvancedSecurityChecked = mDevicePolicyManager.isAdminActive(mComponentName);
        if(isAdvancedSecurityChecked==true)
            swt.setChecked(true);
        else
            swt.setChecked(false);

    }
}