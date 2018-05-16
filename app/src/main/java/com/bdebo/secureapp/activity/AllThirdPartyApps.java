package com.bdebo.secureapp.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.bdebo.secureapp.adapter.CustomThirdPartyAppAdapter;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.SecureAppApplication;
import com.bdebo.secureapp.model.App;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to show all the third party apps installed in the device.
 */
public class AllThirdPartyApps extends Activity {

    private static String TAG = AllThirdPartyApps.class.getSimpleName();
    GridView gv,securedGv;
    private TextView textViewSecuredApps,textViewUnsecuredApps;
    Context context;
    public static List<String> prgmNameList,securedPrgmNameList;
    public static List<String> appPackageList,securedAppPackageList;
    public static List<Drawable> prgmImages,securedPrgmImages;
    private static int ALL_THIRD_PARTY_APPS = 1004;
    private ProgressDialog progressBar;
    private ArrayList<App> m_apps;
    private SecureUtil util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shortcut);
        util = new SecureUtil(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = prefs.getString(AppConstant.USER_OBJ, null);
        App obj = gson.fromJson(json, App.class);
        m_apps = util.getAllApp(obj.getId());
        prgmNameList = new ArrayList<String>();
        appPackageList = new ArrayList<String>();
        prgmImages = new ArrayList<Drawable>();
        securedPrgmNameList = new ArrayList<String>();
        securedAppPackageList = new ArrayList<String>();
        securedPrgmImages = new ArrayList<Drawable>();

        textViewSecuredApps = (TextView) findViewById(R.id.textSecuredApplications);
        textViewUnsecuredApps = (TextView) findViewById(R.id.textUnsecuredApplications);
        gv = (GridView) findViewById(R.id.gridThirdApps);
        securedGv = (GridView) findViewById(R.id.gridThirdAppsSecured);
        new FindApps().execute();

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {


                String selectedItem = prgmNameList.get(position).toString();
                String selectedPackageNameItem = appPackageList.get(position).toString();
                Intent intent = new Intent();
                intent.putExtra(AppConstant.APP_NAME, selectedItem);
                intent.putExtra(AppConstant.APP_PACKAGE_NAME, selectedPackageNameItem);
                setResult(ALL_THIRD_PARTY_APPS, intent);
                finish();//finishing activity
            }
        });

        securedGv.setEnabled(false);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!SecureAppApplication.isApplicationVisible()) {
            finish();
            SecureAppApplication.logout(this);
        }
        Log.d(TAG, "onStop is called: ");
    }

    /**
     * This method is used to segregate the unsecured apps and secured apps in list.
     * @param appName
     * @param appPackageName
     */
    private void addAppInListIfNotSecured(String appName, String appPackageName) {

        boolean isAppPresent = false;
        for (int i = 0; i < m_apps.size(); i++) {
            if (m_apps.get(i).getAppPackName().equals(appPackageName)) {
               isAppPresent = true;
            }
        }

        if(!isAppPresent){
            Drawable icon = null;
            try {
                icon = getPackageManager().getApplicationIcon(appPackageName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            prgmImages.add(icon);
            prgmNameList.add(appName);
            appPackageList.add(appPackageName);
        }
        else {
            Drawable icon = null;
            try {
                icon = getPackageManager().getApplicationIcon(appPackageName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            securedPrgmImages.add(icon);
            securedPrgmNameList.add(appName);
            securedAppPackageList.add(appPackageName);
        }
    }

    /**
     * Async task to find the apps being installed or un installed
     * and accordingly add or delete the app record from app and it's database
     */
    private class FindApps extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            progressBar = new ProgressDialog(AllThirdPartyApps.this);
            progressBar.setCancelable(true);
            progressBar.setMessage("Retrieving your data ...");
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
            progressBar.setMax(100);
            progressBar.show();
        }

        @Override
        protected String doInBackground(String... params) {

            final List<ApplicationInfo> apps = getPackageManager().getInstalledApplications(0);
            for (int i = 0; i < apps.size(); i++) {
                ApplicationInfo packageInfo;

                try {
                    if ((apps.get(i).flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                        //Non-System app
                        final String appName = (String) getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(apps.get(i).packageName, PackageManager.GET_META_DATA));
                        final String appPackageName = apps.get(i).packageName;
                        addAppInListIfNotSecured(appName, appPackageName);
                    } else {
                        //System app
                        final String appPackageName = apps.get(i).packageName;
                        if (appPackageName.equals("com.google.android.gm") || appPackageName.equals("com.android.gallery") || appPackageName.equals("com.android.camera") || appPackageName.equals("com.android.mms") || appPackageName.equals(" com.android.providers.telephony") || appPackageName.equals("com.android.providers.contacts")
                                || appPackageName.equals("com.android.dialer") || appPackageName.equals("com.google.android.apps.photos") || appPackageName.equals("com.google.android.apps.docs")
                                || appPackageName.equals("com.motorola.MotGallery2") || appPackageName.equals("com.motorola.camera") || appPackageName.equals("com.google.android.apps.messaging")) {
                            final String appName = (String) getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(apps.get(i).packageName, PackageManager.GET_META_DATA));
                            addAppInListIfNotSecured(appName, appPackageName);
                        }
                    }
                } catch (PackageManager.NameNotFoundException nnfe) {
                    nnfe.printStackTrace();
                }
            }

            return "Executed";
        }
        @Override
        protected void onPostExecute(String result) {
            progressBar.dismiss();
            gv.setAdapter(new CustomThirdPartyAppAdapter(AllThirdPartyApps.this, prgmNameList, prgmImages));
            textViewUnsecuredApps.setVisibility(View.VISIBLE);
            gv.setVisibility(View.VISIBLE);
            if(securedPrgmNameList.size() > 0) {
                textViewSecuredApps.setVisibility(View.VISIBLE);
                securedGv.setVisibility(View.VISIBLE);
                securedGv.setAdapter(new CustomThirdPartyAppAdapter(AllThirdPartyApps.this, securedPrgmNameList, securedPrgmImages));
            }
        }
    }
}