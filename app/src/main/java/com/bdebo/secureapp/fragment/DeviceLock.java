package com.bdebo.secureapp.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bdebo.secureapp.activity.SelectLockPattern;
import com.bdebo.secureapp.activity.CheckAppPassword;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.model.Device;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;


/**
 * Method to show the Device screen
 */
public class DeviceLock extends Fragment {

    private String TAG = DeviceLock.class.getSimpleName();
    private TextView txtDeviceModelName,txtDevicePassword;
    private RelativeLayout relativeLayoutDeviceLock;
    private FloatingActionButton fabDeviceLock;
    private CoordinatorLayout coordinatorLayout;
    private SwitchCompat toggleDeviceLock;
    private int LOCK_TYPE_SET=1003;
    private String lockTypeData;
    private SharedPreferences prefs;
    private boolean isFirstTimePasswordSet = false;
    boolean isDeviceLockPasswordEnabled;
    private ShowcaseView showcaseView;
    private Device device;
    private View rootView;
    private SecureUtil util;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_device_lock, container, false);
        prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        initViews(rootView);
        if(util.getDeviceLockDetails(1) != null) {
            device = util.getDeviceLockDetails(1);
        }
        isDeviceLockPasswordEnabled = prefs.getBoolean(AppConstant.IS_DEVICE_LOCK_ENABLED,false);
        if(isDeviceLockPasswordEnabled){
            toggleDeviceLock.setChecked(true);
            fabDeviceLock.setImageResource(R.drawable.password_check);
            if(device.getPassword() != null)
            txtDevicePassword.setText(displayCharactersForPassword(device.getPassword().length()));
        }
        else{
            toggleDeviceLock.setChecked(false);
            txtDevicePassword.setText(displayCharactersForPassword(5));
            fabDeviceLock.setImageResource(R.drawable.settings_icon);
        }
        String modelName = android.os.Build.MODEL;
        txtDeviceModelName.setText(modelName);
      /*  relativeLayoutDeviceLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(util.getDeviceLockDetails(1) != null) {
                    prefs.edit().putBoolean(AppConstant.IS_APP_SELECTED, false).commit();
                    Intent intent = new Intent(getActivity(), CheckAppPassword.class);
                    startActivity(intent);
                }
                else{
                    Snackbar snackBar = Snackbar.make(coordinatorLayout, "You have not set any device password", Snackbar.LENGTH_SHORT);
                    snackBar.show();
                }
            }
        });*/
        fabDeviceLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(showcaseView != null && showcaseView.isShown()){

                    showcaseView.hide();

                }
                if(util.getDeviceLockDetails(1) != null) {
                    prefs.edit().putBoolean(AppConstant.IS_APP_SELECTED, false).commit();
                    Intent intent = new Intent(getActivity(), CheckAppPassword.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.push_up_in,R.anim.push_down_out);
                }
                else{
                    Intent intent = new Intent(getActivity(), SelectLockPattern.class);
                    intent.putExtra(AppConstant.IS_CALL_FROM_DEVICE_LOCK, true);
                    startActivityForResult(intent, LOCK_TYPE_SET);
                    getActivity().overridePendingTransition(R.anim.push_up_in,R.anim.push_down_out);
                }

                  /*  Intent intent = new Intent(getActivity(), SelectLockPattern.class);
                    intent.putExtra(AppConstant.IS_CALL_FROM_DEVICE_LOCK, true);
                    startActivityForResult(intent, LOCK_TYPE_SET);
*/
            }
        });
        toggleDeviceLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    fabDeviceLock.setVisibility(View.VISIBLE);
                    if(util.getDeviceLockDetails(1) != null) {
                        enableDeviceLock();
                        fabDeviceLock.setImageResource(R.drawable.password_check);
                    }else {
                        showSnackBar( "Please set the password for your device");
                        toggleDeviceLock.setChecked(false);
                    }
                }
                else {
                    fabDeviceLock.setVisibility(View.GONE);
                    disableDeviceLock();
                }
            }
        });
        return rootView;
    }

    /**
     * Method to set the shared preference flag to true
     * for enabling device lock
     */
    private void enableDeviceLock() {
        prefs.edit().putBoolean(AppConstant.IS_DEVICE_LOCK_ENABLED,true).commit();
    }

    /**
     * Method to set the shared preference flag to false
     * for disabling device lock
     */
    private void disableDeviceLock() {
        prefs.edit().putBoolean(AppConstant.IS_DEVICE_LOCK_ENABLED,false).commit();
    }

    /**
     * Initializes views
     * @param view
     */
    private void initViews(View view){
        txtDeviceModelName = (TextView) view.findViewById(R.id.textDeviceModelName);
        txtDevicePassword = (TextView) view.findViewById(R.id.textDevicePass);
        toggleDeviceLock = (SwitchCompat)view.findViewById(R.id.toggleDeviceLock);
        relativeLayoutDeviceLock = (RelativeLayout) view.findViewById(R.id.relativeLayoutDeviceLockDetails);
        fabDeviceLock = (FloatingActionButton) view.findViewById(R.id.fabDeviceLock);
        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.layoutCoordinatorDeviceLock);
        util = new SecureUtil(getActivity());
    }

    /*
     * Method to display characters in asterisk for security
     */
    private String displayCharactersForPassword(int length)
    {
        String str="";
        for(int i=0;i<length;i++)
        {
            str=str+"*";
        }
        return str;
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (visible) {
                Log.d(TAG, "visible:: " + visible);
                boolean isFirstTimeOverlayShown = prefs.getBoolean(AppConstant.IS_FIRST_TIME_OVERLAY_SHOWN, false);
                if (!isFirstTimeOverlayShown) {
                    showHelpOverlay(rootView);
                    prefs.edit().putBoolean(AppConstant.IS_FIRST_TIME_OVERLAY_SHOWN, true).commit();
                }
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem register = menu.findItem(R.id.action_device_lock_theme);
        MenuItem device_invisible_pattern= menu.findItem(R.id.device_action_lock_invisible_pattern);

        if(device  == null){
            register.setVisible(false);
            device_invisible_pattern.setVisible(false);
        }
        else {
            register.setVisible(true);
            if(device.getLockType().equals("Pattern")){
                device_invisible_pattern.setVisible(true);
            }
            else{
                device_invisible_pattern.setVisible(false);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null) {

            if(requestCode == LOCK_TYPE_SET) {
                lockTypeData = data.getStringExtra(AppConstant.LOCK_TYPE);
                String pinPasswordPatternData = data.getStringExtra(AppConstant.LOCK_PIN_PASSWORD_PATTERN_DATA);
                Device device = new Device();
                device.setId(1);
                device.setPassword(pinPasswordPatternData);
                device.setLockType(lockTypeData);
                device.setLockEnabled(isDeviceLockPasswordEnabled);
                device.setImagePosition(1);
                if(util.getDeviceLockDetails(1) != null) {
                    util.updateDeviceLockDetails(device,1);
                }
                else {
                    if(isFirstTimePasswordSet){
                        enableDeviceLock();
                        Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Password set successfully", Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                        isFirstTimePasswordSet = false;
                    }

                    showAlertDialog("Enable Device Lock","Do you want to enable device lock?");
                    util.insertDeviceLockDetails(device);
                }
            }
        }
    }

    /**
     * Display snack bar
     * @param msg
     */
    private void showSnackBar(String msg){

        Snackbar.make(coordinatorLayout,msg, Snackbar.LENGTH_LONG)
                .setAction("SET", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isFirstTimePasswordSet = true;
                        Intent intent=new Intent(getActivity(),SelectLockPattern.class);
                        intent.putExtra(AppConstant.IS_CALL_FROM_DEVICE_LOCK,true);
                        startActivityForResult(intent,LOCK_TYPE_SET);
                        getActivity().overridePendingTransition(R.anim.push_up_in,R.anim.push_down_out);
                    }
                })
                .setActionTextColor(Color.RED)
                .show();
    }

    /**
     * Display Alert Dialog to enable toggle button or not
     * for  Device Lock
     * @param title
     * @param message
     */
    public void showAlertDialog(String title, String message)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(getString(R.string.title_enable), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        enableDeviceLock();
                        toggleDeviceLock.setChecked(true);

                    }
                })
                .setNegativeButton(getString(R.string.title_later), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
        ;

        // Create the AlertDialog object and return it
        builder.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                }
                return true;
            }
        });

        builder.show();

    }

    /**
     * Show overlay
     * @param parentView
     */
    private void showHelpOverlay(View parentView){
        showcaseView = new ShowcaseView.Builder(getActivity())
                .setTarget( new ViewTarget( ((View) parentView.findViewById(R.id.fabDeviceLock)) ) )
                .setContentTitle("Set Device Lock")
                .setContentText("To add security to device please click on the set button")
                .setStyle(R.style.Transparent)
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                        showcaseView.hideButton();
                    }
                })
                .hideOnTouchOutside()
                .build();
    }

}
