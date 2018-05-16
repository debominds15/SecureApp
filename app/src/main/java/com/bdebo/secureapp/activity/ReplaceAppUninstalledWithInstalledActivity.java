package com.bdebo.secureapp.activity;

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
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bdebo.secureapp.R;
import com.bdebo.secureapp.SecureAppApplication;
import com.bdebo.secureapp.model.App;
import com.bdebo.secureapp.model.AppAlreadyAuthorised;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;
import com.bdebo.secureapp.view.DividerItemDecoration;
import com.bdebo.secureapp.view.VerticalSpaceItemDecoration;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is used to replace uninstalled app in the list with any installed app
 * previously which was not secured
 */
public class ReplaceAppUninstalledWithInstalledActivity extends ApplicationActivity{
    private String TAG = ReplaceAppUninstalledWithInstalledActivity.class.getSimpleName();
    private int uninstalledAppPosition = -1;
    RecyclerView recyclerView;
    private int appId;
    private ArrayList<App> m_apps;
    private SecureUtil util;
    private TextView uninstalledAppName;
    private ImageView uninstalledAppIcon;
    private Button btnCancel;
    private Button btnSave;
    private SharedPreferences prefs;
    private static List<Drawable> prgmImages;
    private static List<String> appPackageList;
    private static List<String> prgmNameList;
    private ProgressDialog progressBar;
    private App app;
    private  App obj;
    private int REQUEST_FOR_REPLACE_APP_UNINSTALLED = 1005;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_unistall_app_with_intall_app);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewSetAppFromInstalledApps);
        uninstalledAppName = (TextView) findViewById(R.id.uninstalledAppName);
        uninstalledAppIcon = (ImageView) findViewById(R.id.uninstalledAppIcon);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        util = new SecureUtil(this);
        appId = getIntent().getIntExtra("appId",0);
        Log.d(TAG,"appPosition::"+appId);
        app = getAppFromPosition(appId);
        if(app != null)
        uninstalledAppName.setText(app.getName());
        Log.d(TAG,"appName::"+app.getName());
        Gson gson = new Gson();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String json = prefs.getString(AppConstant.USER_OBJ, null);
        obj = gson.fromJson(json, App.class);
        m_apps = util.getAllApp(obj.getId());
        prgmNameList = new ArrayList<String>();
        appPackageList = new ArrayList<String>();
        prgmImages = new ArrayList<Drawable>();
        new FindApps().execute();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appName = prgmNameList.get(uninstalledAppPosition);
                String appPackageName = appPackageList.get(uninstalledAppPosition);
                updateInDB(appName,appPackageName);
                callToastMessage("Updated successfully");
                Intent intent = new Intent();
                intent.putExtra("isAppReleased",true);
                setResult(REQUEST_FOR_REPLACE_APP_UNINSTALLED, intent);
                finish();
                overridePendingTransition(R.anim.push_down_in,R.anim.push_up_out);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Display Toast message
     * @param msg
     */
    private void callToastMessage(String msg){
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_layout));
        SecureUtil.setToastMessage(this,msg,toastLayout);
    }

    /**
     * Update app info in DB after replacing
     * @param appName
     * @param appPackageName
     */
    private void updateInDB(String appName, String appPackageName){
        AppAlreadyAuthorised appAlreadyAuthorised = getAppAlreadyAuthorisedRecord(app.getName(),true);
        if(appAlreadyAuthorised != null){
            appAlreadyAuthorised.setAppName(appName);
            appAlreadyAuthorised.setAppPackName(appPackageName);
            appAlreadyAuthorised.setIsAppAuthorised(false);
            appAlreadyAuthorised.setAppOpenThroughSecureApp(false);
            util.updateAppAuthorisationRecords(appAlreadyAuthorised,appAlreadyAuthorised.getId());
            Log.d(TAG,"updateAppAuthorisationRecords is called");
        }

        App installedApp = app;
        installedApp.setName(appName);
        installedApp.setAppPackName(appPackageName);
        installedApp.setAppPresent(true);
        util.updateRecord(installedApp,app.getId(),obj.getId());
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
    }

    /**
     * Async task to find the apps being installed or un installed
     * and accordingly add or delete the app record from app and it's database
     */
    private class FindApps extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            progressBar = new ProgressDialog(ReplaceAppUninstalledWithInstalledActivity.this);
            progressBar.setCancelable(true);
            progressBar.setMessage("Retrieving your data ...");
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
            progressBar.setMax(100);
            progressBar.show();
        }

        @Override
        protected String doInBackground(String... params) {

            Log.d(TAG,"doInBackground()::");
            final List<ApplicationInfo> apps = getPackageManager().getInstalledApplications(0);
            for (int i = 0; i < apps.size(); i++) {
                ApplicationInfo packageInfo;

                try {
                    if ((apps.get(i).flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                        //Non-System app
                        final String appName = (String) getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(apps.get(i).packageName, PackageManager.GET_META_DATA));
                        final String appPackageName = apps.get(i).packageName;
                        Log.d(TAG,"doInBackground()::"+appName);
                        addAppInListIfNotSecured(appName, appPackageName);
                    } else {
                        //System app
                        final String appPackageName = apps.get(i).packageName;
                        if (appPackageName.equals("com.google.android.gm") || appPackageName.equals("com.android.gallery") || appPackageName.equals("com.android.camera") || appPackageName.equals("com.android.mms") || appPackageName.equals(" com.android.providers.telephony") || appPackageName.equals("com.android.providers.contacts")
                                || appPackageName.equals("com.android.dialer") || appPackageName.equals("com.google.android.apps.photos") || appPackageName.equals("com.google.android.apps.docs")
                                || appPackageName.equals("com.motorola.MotGallery2") || appPackageName.equals("com.motorola.camera") || appPackageName.equals("com.google.android.apps.messaging")) {
                            final String appName = (String) getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(apps.get(i).packageName, PackageManager.GET_META_DATA));
                            Log.d(TAG,"doInBackground()::"+appName);
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
            Log.d(TAG,"onPostExecute()::");
            recyclerView.setAdapter(new SetAppInstalledAdapter(ReplaceAppUninstalledWithInstalledActivity.this, prgmNameList, prgmImages,appPackageList));
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(ReplaceAppUninstalledWithInstalledActivity.this);
            llm.setAutoMeasureEnabled(false);
            recyclerView.setLayoutManager(llm);
            progressBar.dismiss();
        }
    }

    /**
     * Adapter class to bind apps
     */
    public class SetAppInstalledAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public Context context;
        public List<String> appNameArrayList;
        public List<Drawable> appIconArrayList;
        private List<String> appPackageList;
        private SharedPreferences prefs;
        public List<ApplicationInfo> appsInfo;
        private int lastCheckedPosition = -1;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView appName;
            private ImageView appIcon;
            private RadioButton radioButton;

            public MyViewHolder(View itemView) {
                super(itemView);
                appName = (TextView) itemView.findViewById(R.id.installedAppName);
                appIcon = (ImageView) itemView.findViewById(R.id.installedAppIcon);
                radioButton = (RadioButton) itemView.findViewById(R.id.radioAppSelected);
                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       // lastCheckedPosition = getAdapterPosition();
                        uninstalledAppPosition = getAdapterPosition();
                        //because of this blinking problem occurs so
                        //i have a suggestion to add notifyDataSetChanged();
                        //   notifyItemRangeChanged(0, list.length);//blink list problem
                        Log.d(TAG,"onBindViewHolder():: lastCheckedPosition"+uninstalledAppPosition+" app:"+appNameArrayList.get(uninstalledAppPosition));
                        uninstalledAppName.setText(appNameArrayList.get(uninstalledAppPosition));
                        uninstalledAppIcon.setImageDrawable(appIconArrayList.get(uninstalledAppPosition));
                        btnSave.setEnabled(true);
                        notifyDataSetChanged();

                    }
                });
            }
        }

        public SetAppInstalledAdapter(Context context, List<String> appNameArrayList, List<Drawable> appIconArrayList, List<String> appPackageList) {
            this.context = context;
            this.appNameArrayList = appNameArrayList;
            this.appIconArrayList = appIconArrayList;
            this.appPackageList = appPackageList;
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_installed_apps, parent, false);

            return new SetAppInstalledAdapter.MyViewHolder(itemView);

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            String appNameText = appNameArrayList.get(position);
            Drawable appIconDrawable = appIconArrayList.get(position);

            MyViewHolder myViewHolder = (MyViewHolder) holder;
            myViewHolder.appName.setText(appNameText);
            myViewHolder.appIcon.setImageDrawable(appIconDrawable);
            myViewHolder.radioButton.setChecked(position == uninstalledAppPosition);
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            return appNameArrayList.size();
        }
    }

    /**
     * This method is used to return the AppAlreadyAuthorised object to update in DB for corresponding row
     * @param appPackName
     * @param isAppName
     * @return
     */
    private AppAlreadyAuthorised getAppAlreadyAuthorisedRecord(String appPackName,boolean isAppName){
        ArrayList<AppAlreadyAuthorised> appAlreadyAuthorisedArrayList = util.getAllAppsAuthorisationInformation();
        for (int i = 0; i < appAlreadyAuthorisedArrayList.size(); i++) {
            if (!isAppName) {
                if(appAlreadyAuthorisedArrayList.get(i).getAppPackName().equals(appPackName))
                    return appAlreadyAuthorisedArrayList.get(i);
            }
            else{
                if(appAlreadyAuthorisedArrayList.get(i).getAppName().equals(appPackName))
                    return appAlreadyAuthorisedArrayList.get(i);
            }
        }
        return null;
    }

    /**
     *This method is used to get particular app object
     * from DB
     * @param position
     * @return
     */
    private App getAppFromPosition(int position){
        ArrayList<App> apps = new ArrayList<App>();
        apps = util.getAllAppsFromDB();
        Log.d(TAG,"appSSZE::"+apps.size());
        for(int i=0;i<apps.size();i++){
            Log.d(TAG,"appId:"+apps.get(i).getId()+" id passed::"+position);
            if(position == apps.get(i).getId())
            {
                return apps.get(i);
            }
        }
        return null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if(!SecureAppApplication.isApplicationVisible()){
            overridePendingTransition(R.anim.push_down_in,R.anim.push_up_out);
            finish();
            SecureAppApplication.logout(this);
        }
    }
}
