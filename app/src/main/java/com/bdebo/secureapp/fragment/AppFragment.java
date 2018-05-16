package com.bdebo.secureapp.fragment;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bdebo.secureapp.activity.AllThirdPartyApps;
import com.bdebo.secureapp.activity.Register;
import com.bdebo.secureapp.activity.ReplaceAppUninstalledWithInstalledActivity;
import com.bdebo.secureapp.model.Item;
import com.bdebo.secureapp.activity.Login;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.activity.SelectLockPattern;
import com.bdebo.secureapp.helper.ThirdPartyApps;
import com.bdebo.secureapp.helper.ThirdPartyAppsImpl;
import com.bdebo.secureapp.model.User;
import com.bdebo.secureapp.activity.CheckAppPassword;
import com.bdebo.secureapp.model.App;
import com.bdebo.secureapp.model.AppAlreadyAuthorised;
import com.bdebo.secureapp.model.SectionApp;
import com.bdebo.secureapp.service.WindowChangeDetectingService;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;
import com.bdebo.secureapp.view.DividerItemDecoration;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * This fragment class is used to show the Home screen for the App
 */
public class AppFragment extends Fragment implements SearchView.OnQueryTextListener{
    private String TAG = AppFragment.class.getSimpleName();
    private String appPackageName;
    private int LOCK_TYPE_SET=1003,ALL_THIRD_PARTY_APPS=1004,REQUEST_FOR_REPLACE_APP_UNINSTALLED = 1005;
    private EditText appName,appPass;
    private ImageView imageViewShortcut,imageSelectPatternLockType;
    private RecyclerView recyclerView;
    private boolean isAppPresent=false;
    private SearchView mSearchView;
    private boolean checkApp=false;
    private User obj;
    private ProgressDialog progressBar;
    private AlertDialog alertDialog;
    private String lockTypeData;
    private SharedPreferences prefs;
    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton fab;
    private ArrayList<App> m_apps = null;
    private ArrayList<Item> itemArrayList;
    public static List<ApplicationInfo> appsInfo;
    private AppAdapter m_adapter;
    private ThirdPartyApps apps=new ThirdPartyAppsImpl();
    private ShowcaseView showcaseView;
    private SecureUtil util;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home_apps, container, false);
        appsInfo=new ArrayList<ApplicationInfo>();
        setHasOptionsMenu(true);//Without this onOptionsSelected method wont work in fragment
        util = new SecureUtil(getActivity());
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        showHelpOverlay(rootView);
        init(rootView);
        new ReadyTask().execute();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)

        //checkAppInstalledRecently();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (showcaseView.isShown()) {
                        showcaseView.hide();
                    }
                }
                if(!isAccessibilitySettingsOn(getActivity())) {
                    createDialogUsageAccess();
                }
                else {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View dialogLayout = inflater.inflate(R.layout.add_app_dialog, null);
                    addAppDialog(dialogLayout);
                }
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        // Inflate the layout for this fragment
        return rootView;

    }

    void refreshItems() {
        // Load items
        // ...

        // Load complete
        onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        // Update the adapter and notify data set changed
        // ...

        Log.d(TAG,"onItemsLoadComplete:: refreshing adapter!!!");
        getApps();
        m_adapter.notifyDataSetChanged();
        // Stop refresh animation
        mSwipeRefreshLayout.setRefreshing(false);
    }
    /**
     * Initializes the views
     * @param rootView
     */
    private void init(View rootView){
        coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.layoutCoordinator);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.listView1);
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
       // loadProgressBar();
    }

        /**
         * Create the dialog for usage access
         */
        private void createDialogUsageAccess(){
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setMessage("Please turn on the accessibility for SecureApp needed for App Locker to work."+"\n\n"+"Settings-> Accessibility-> SecureApp")
                .setTitle("Turn Accessibility On")
                .setPositiveButton("GO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        prefs.edit().putBoolean(AppConstant.IS_APP_USAGE_ACCESS_OPENED,true).commit();
                        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivityForResult(intent, 2000);
                    }
                });

        // Create the AlertDialog object and return it
        builder.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    getActivity().finish();
                }
                return true;
            }
        });

        builder.show();

    }

    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getActivity().getPackageName() + "/" + WindowChangeDetectingService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(true);

        ImageView closeButton = (ImageView)mSearchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Re load all the apps with app section on clearing search
                new ReadyTask().execute();
                Log.d(TAG, "Re load all the apps with app section on clearing search");
                mSearchView.setQuery("", false);
                //Collapse the action view
                mSearchView.onActionViewCollapsed();
            }
        });
    }

    /**
     * method to get the notification icon
     * @return
     */
    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_launcher_transparent : R.drawable.ic_launcher;
    }

    /**
     * Create local notification if any app is added
     */
    private void createLocalNotification(String appName){
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(getNotificationIcon())
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setSound(alarmSound)
                        .setContentTitle("SecureApp")
                        .setContentText(appName+" is secured now");


        Intent resultIntent = new Intent(getActivity(), Login.class);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        int mNotificationId = 100;
        resultIntent.putExtra(NOTIFICATION_ID,mNotificationId);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        getActivity(),
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotifyMgr =
                (NotificationManager) getActivity().getSystemService(getActivity().NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }

    /**
     * Fetching the apps stored in local sqlite database
     */
    void getApps()
    {
        try {
            m_apps = util.getAllApp(obj.getId());
            itemArrayList=getAllAppsWithSections(m_apps);
            for(App a:m_apps)
                this.m_adapter = new AppAdapter(getActivity(), itemArrayList,this);
            //Thread.sleep(2000);
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
        if(getActivity()==null) {
            return;
        }
        getActivity().runOnUiThread(returnRes);

    }

    /**
     * Method to segregate app sections and their apps
     * @param appsList with app sections and their apps
     * @return list of apps under specified sections
     */
    private ArrayList<Item> getAllAppsWithSections(ArrayList<App> appsList ){

        boolean checkSectionAppPresent=false;
        itemArrayList=new ArrayList<Item>();

        ArrayList<String> appSections=new ArrayList<String>();
        appSections.add("Antivirus");
        appSections.add("Banking");
        appSections.add("Camera");
        appSections.add("Drive");
        appSections.add("Mail");
        appSections.add("Media");
        appSections.add("Messaging");
        appSections.add("Other");
        appSections.add("Shopping");
        appSections.add("Travel");

        for(int i=0;i<appSections.size();i++)
        {
            //Check if section is having any app under it. If not don't add that section.
            for(int j=0;j<appsList.size();j++) {

                if(appSections.get(i).toString().equals(appsList.get(j).getAppSection()))
                {
                    checkSectionAppPresent=true;
                    break;
                }
            }

//          Adding sections if any app under it is present
            if(checkSectionAppPresent==true) {

                SectionApp sectionApp = new SectionApp(appSections.get(i).toString());
                itemArrayList.add(sectionApp);
                checkSectionAppPresent=false;
            }

            for(int j=0;j<appsList.size();j++)
            {
                if(appSections.get(i).toString().equals(appsList.get(j).getAppSection()))
                {
                    itemArrayList.add(new App(appsList.get(j).getId(),appsList.get(j).getUid(),appsList.get(j).getName(),appsList.get(j).getPass(),appsList.get(j).isAppPresent(),appsList.get(j).getAppSection(),appsList.get(j).getAppLockType(),appsList.get(j).getAppPackName()));
                }
            }
        }

        return itemArrayList;
    }

    /**
     * Setting the adapter for the listview
     */
    private Runnable returnRes = new Runnable() {

        @Override
        public void run() {
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            //add ItemDecoration
            //or
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
            //or
            //recyclerView.addItemDecoration(
              //      new DividerItemDecoration(getActivity(), R.drawable.divider));
            callSimpleCallBack();
            recyclerView.setAdapter(m_adapter);

        }
    };


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                m_adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                m_adapter.filter(newText);
                return true;
            }
        });
        return true;
    }

    /**
     * Method to delete the app from ListView and database
     * @param app
     * @param position
     */
    private void deleteAlert(final App app,final  int position)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        alertDialog.setTitle("Application").setMessage("Are you sure you want to remove lock from "+app.getName()+"?");

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.cancel();
                m_adapter.notifyItemRemoved(position + 1);    //notifies the RecyclerView Adapter that data in adapter has been removed at a particular position.
                m_adapter.notifyItemRangeChanged(position, m_adapter.getItemCount());
               // m_adapter.notifyDataSetChanged();
            }
        });
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                itemArrayList.remove(position);
                deleteSectionAppIfSectionIsEmpty(app);
                showSnackBar(app.getName(),position,app);
                m_adapter.notifyDataSetChanged();
            }
        });
        alertDialog.show();
    }

    /**
     * Method to add the app in list view and database
     * @param dialoglayout
     */
    private void addAppDialog(final View dialoglayout){

        RelativeLayout spinLayout=(RelativeLayout) dialoglayout.findViewById(R.id.spinnersSection);
        final Spinner appSection=(Spinner) dialoglayout.findViewById(R.id.spinAppSection);

        spinLayout.setVisibility(View.VISIBLE);

        ArrayList<String> appSectionList=new ArrayList<String>();
        appSectionList.add("Select section");
        appSectionList.add("Antivirus");
        appSectionList.add("Banking");
        appSectionList.add("Camera");
        appSectionList.add("Drive");
        appSectionList.add("Mail");
        appSectionList.add("Media");
        appSectionList.add("Messaging");
        appSectionList.add("Other");
        appSectionList.add("Shopping");
        appSectionList.add("Travel");

        ArrayAdapter adapter = new ArrayAdapter(getActivity(),android.R.layout.simple_spinner_item,appSectionList);
        //ArrayAdapter<String> adapter=new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,appSectionList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        appSection.setAdapter(adapter);
        appSection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!appSection.getSelectedItem().toString().equals("Select section")){
                    TextView tvInvisibleError = (TextView)dialoglayout.findViewById(R.id.txtSpinnerInvisibleError);
                    tvInvisibleError.setVisibility(View.GONE);
                }
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(),R.style.AppCompatAlertDialogStyle);
        dialogBuilder.setView(dialoglayout);
        appName = (EditText) dialoglayout
                .findViewById(R.id.editAppName);
        appPass=(EditText) dialoglayout.findViewById(R.id.editAppPass);
        imageViewShortcut=(ImageView) dialoglayout
                .findViewById(R.id.imageView_Shortcut);

        View viewSeparator = (View) dialoglayout.findViewById(R.id.dividerView);
        imageSelectPatternLockType=(ImageView) dialoglayout.findViewById(R.id.imageView_LockType);

        imageViewShortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(getActivity(),AllThirdPartyApps.class);
                startActivityForResult(intent,ALL_THIRD_PARTY_APPS);

            }
        });

        imageSelectPatternLockType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(),SelectLockPattern.class);
                startActivityForResult(intent,LOCK_TYPE_SET);
                getActivity().overridePendingTransition(R.anim.push_up_in,R.anim.push_down_out);
            }
        });

        appPass.setEnabled(false);
        dialogBuilder.setTitle("Application");
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.cancel();
            }
        });

        dialogBuilder.setPositiveButton("Ok",null);
        alertDialog = dialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button btn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        if(appName.getText().toString().equals(""))
                            appName.setError("App Name cannot be empty");

                        else if(appPass.getText().toString().equals(""))
                            appPass.setError("App Password cannot be empty");

                        else if(appSection.getSelectedItem().toString().equals("Select section"))
                            spinnerSetError("Please select valid option",appSection,dialoglayout);

                        else{
                            appName.setError(null);
                            appPass.setError(null);
                            App app = new App();
                            app.setUid(obj.getId());
                            app.setName(appName.getText().toString());
                            app.setPass(appPass.getText().toString());
                            app.setAppSection(appSection.getSelectedItem().toString());
                            app.setAppLockType(lockTypeData);
                            app.setAppPackName(appPackageName);

                            if (isAppPresent == true)
                                app.setAppPresent(true);

                            AppAlreadyAuthorised appAlreadyAuthorised = new AppAlreadyAuthorised();
                            appAlreadyAuthorised.setId(obj.getId());
                            appAlreadyAuthorised.setAppName(appName.getText().toString());
                            appAlreadyAuthorised.setAppPackName(appPackageName);
                            appAlreadyAuthorised.setIsAppAuthorised(false);
                            appAlreadyAuthorised.setAppOpenThroughSecureApp(false);

                            util.insertRecords(app);
                            util.insertAppAuthorisationRecords(appAlreadyAuthorised);
                            addItemDynamically(app,app.getAppSection());
                            getApps();
                            m_adapter.notifyDataSetChanged();
                            createLocalNotification(app.getName());
                            //Dismiss once everything is OK.
                            alertDialog.dismiss();
                            callToastMessage("Lock enabled for "+app.getName());

                        }
                    }
                });
            }
        });
        alertDialog.show();

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            viewSeparator.setVisibility(View.GONE);
            setAlertTitleColorSeparator();
        }
        isAppPresent=false;
        appPackageName="empty";
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
    /**
     * Setting alert dialog separator color and title color
     */
    private void setAlertTitleColorSeparator(){

        int dividerId = alertDialog.getContext().getResources()
                .getIdentifier("android:id/titleDivider", null, null);
        View divider = alertDialog.findViewById(dividerId);
        divider.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        int textViewId = alertDialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
        TextView tv = (TextView) alertDialog.findViewById(textViewId);
        tv.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

    }
    /**
     * Method to set the error for spinner if no item is selected
     * @param errorMessage
     * @param spnMySpinner
     * @param viewdialogLayout
     */
    public void spinnerSetError(String errorMessage,Spinner spnMySpinner,View viewdialogLayout)
    {
        View view = spnMySpinner.getSelectedView();

        // Set TextView in Secondary Unit spinner to be in error so that red (!) icon
        // appears, and then shake control if in error
        TextView tvListItem = (TextView)view;

        // Set fake TextView to be in error so that the error message appears
        TextView tvInvisibleError = (TextView)viewdialogLayout.findViewById(R.id.txtSpinnerInvisibleError);
        tvInvisibleError.setVisibility(View.VISIBLE);
        // Shake and set error if in error state, otherwise clear error
        if(errorMessage != null)
        {
            tvListItem.setError(errorMessage);
            tvListItem.requestFocus();
            tvInvisibleError.requestFocus();
            tvInvisibleError.setError(errorMessage);
        }
        else
        {
            tvListItem.setError(null);
            tvInvisibleError.setError(null);
            tvInvisibleError.setVisibility(View.GONE);
        }
    }

    /**
     * Method to add item dynamically in ListView
     * @param app
     * @param section
     */
    private void addItemDynamically(App app,String section)
    {
        if(!section.equals("Select section")) {
            int pos = 0;
            for (int i = 0; i < itemArrayList.size(); i++) {
                if (itemArrayList.get(i).isSection()) {
                    SectionApp sectionApp = (SectionApp) itemArrayList.get(i);
                    if (sectionApp.getTitle().equals(section)) {
                        pos = i + 1;
                        break;
                    }
                }
                if (i == itemArrayList.size() - 1) {

                    SectionApp sectionApp = new SectionApp();
                    sectionApp.setTitle(section);
                    itemArrayList.add(sectionApp);
                    pos = itemArrayList.size();
                }
            }
            for (int i = pos; i < itemArrayList.size(); i++) {
                if (itemArrayList.get(i).isSection()) {
                    pos = i;
                    break;
                }
                if (i == itemArrayList.size() - 1) {
                    pos = i + 1;
                    break;
                }
            }
            if (itemArrayList.size() == 0) {
                SectionApp sectionApp = new SectionApp();
                sectionApp.setTitle(app.getAppSection());
                itemArrayList.add(pos, sectionApp);
                pos++;
            }

            itemArrayList.add(pos, app);
        }
    }

    /**
     * Check whether if the all the apps are deleted under particular section
     * @return
     */
    private boolean isSectionEmpty(String sectionName){

        for(int i=0;i<itemArrayList.size();i++) {

            if(!itemArrayList.get(i).isSection()){
                App app = (App) itemArrayList.get(i);
                if(app.getAppSection().equals(sectionName)){
                    return  false;
                }
            }
        }
        return  true;
    }

    /**
     * Method to delete the section if apps are not present under that section
     * @param app
     */
    private void deleteSectionAppIfSectionIsEmpty(App app)
    {
        if(isSectionEmpty(app.getAppSection())) {
            for (int i = 0; i < itemArrayList.size(); i++) {
                if (itemArrayList.get(i).isSection() == true) {
                    SectionApp sectionApp = (SectionApp) itemArrayList.get(i);
                    if (sectionApp.getTitle().equals(app.getAppSection()))
                        itemArrayList.remove(i);
                }
            }
        }
    }

    /**
     * Method to show the progress bar
     */
    public void loadProgressBar() {

        progressBar = new ProgressDialog(getActivity());
        progressBar.setCancelable(true);
        progressBar.setMessage("Retrieving your data ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();

        new Thread(new Runnable() {
            public void run() {
                getApps();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                progressBar.dismiss();

            }
        }).start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(alertDialog != null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
        if(progressBar != null) {
            progressBar.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        super.onPause();

        if(progressBar != null) {
            progressBar.dismiss();
        }
    }

    /**
     * Method to update the table
     * to directly open the app
     * through SecureApp
     * @param appPackName
     * @param isAppOpenThroughSecureApp
     */
    private void updateAppAuthorizedAppOpenThroughSecureApp(String appPackName,boolean isAppOpenThroughSecureApp){
        SecureUtil util = new SecureUtil(getActivity());
        util.open();
        ArrayList<AppAlreadyAuthorised> appAlreadyAuthorisedArrayList = util.getAllAppsAuthorisationInformation();
        for (int i=0;i<appAlreadyAuthorisedArrayList.size();i++){
            if(appAlreadyAuthorisedArrayList.get(i).getAppPackName().equals(appPackName)){
                Log.d(TAG,"updateAppAuthorizedAppOpenThroughSecureApp in appFragment....isAppOpenThroughSecureApp::"+isAppOpenThroughSecureApp);
                AppAlreadyAuthorised appAlreadyAuthorised = appAlreadyAuthorisedArrayList.get(i);
                appAlreadyAuthorised.setAppOpenThroughSecureApp(isAppOpenThroughSecureApp);
                util.updateAppAuthorisationRecords(appAlreadyAuthorised, appAlreadyAuthorisedArrayList.get(i).getId());
            }
        }
        util.close();
    }

    /**
     * Method to open the third party app securely
     * @param employeeArrayList
     * @param position
     */
    private void openThirdPartyApp(ArrayList<Item> employeeArrayList,int position)
    {
        prefs.edit().putBoolean(AppConstant.IS_OTHER_APP_OPENED,true).commit();
        appsInfo=apps.getAllThirdPartyApps(getActivity());
        if(employeeArrayList.get(position).isSection()==false) {
            for (ApplicationInfo a : appsInfo) {
                checkApp = false;
                try {
                    App app=(App) employeeArrayList.get(position);
                    String appNameCheck = (String) getActivity().getPackageManager().getApplicationLabel(getActivity().getPackageManager().getApplicationInfo(a.packageName, PackageManager.GET_META_DATA));
                    if (app.getName().equals(appNameCheck) && app.isAppPresent() == true) {
                        checkApp = true;
                        updateAppAuthorizedAppOpenThroughSecureApp(app.getAppPackName(),true);
                        callToastMessage("Opening " + appNameCheck);

                        Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(a.packageName);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                        getActivity().finish();
                        break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (checkApp == false) {

                App app = (App) employeeArrayList.get(position);
                Log.d(TAG,"openThirdPartyApp():: appPackName::"+app.getAppPackName()+" isAppPresent::"+app.isAppPresent());
                callToastMessage(app+" is not installed currently in your device");
                final String appPackageName = app.getName();
                long modifiedTime = System.currentTimeMillis();
                prefs.edit().putLong("modified_time", modifiedTime);
                //Opening play store with auto search
                Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://search?q=" + appPackageName));
                startActivity(goToMarket);
                getActivity().finish();
            }
        }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data!=null) {
            if (requestCode == ALL_THIRD_PARTY_APPS) {
                String appname = data.getStringExtra(AppConstant.APP_NAME);
                appPackageName = data.getStringExtra(AppConstant.APP_PACKAGE_NAME);
                appName.setText(appname);
                Drawable imageAppIconDrawable = getAppImageIcon(appPackageName);
                imageViewShortcut.setImageDrawable(imageAppIconDrawable);
                isAppPresent=true;
            }
            else if(requestCode == LOCK_TYPE_SET)
            {
                lockTypeData=data.getStringExtra(AppConstant.LOCK_TYPE);
                String pinPasswordPatternData = data.getStringExtra(AppConstant.LOCK_PIN_PASSWORD_PATTERN_DATA);
                appPass.setText(pinPasswordPatternData);
            }
            else if(requestCode == REQUEST_FOR_REPLACE_APP_UNINSTALLED){
                Log.d(TAG,"REQUEST_FOR_REPLACE_APP_UNINSTALLED");
                boolean isAppReleased = data.getBooleanExtra("isAppReleased",false);
                Log.d(TAG,"REQUEST_FOR_REPLACE_APP_UNINSTALLED :: "+isAppReleased);
                getApps();
                m_adapter.notifyDataSetChanged();
            }
            else if(requestCode == 2000){
                Log.d(TAG,"requestCode is 2000");
            }
        }

        else
        {
            Log.d("HomeFragment","data is null onActivityResult...");
        }
    }

    /**
     * Method to get the drawable image icon based on package name
     * @param appPackName
     * @return
     */
    private Drawable getAppImageIcon(String appPackName) {
        Drawable icon = null;
        final List<ApplicationInfo> apps = getActivity().getPackageManager().getInstalledApplications(0);
        for (int i = 0; i < apps.size(); i++) {
            ApplicationInfo packageInfo;

            if ((apps.get(i).flags & ApplicationInfo.FLAG_SYSTEM) != 1 && apps.get(i).packageName.equals(appPackName)) {
                //System app
                try {
                    icon = getActivity().getPackageManager().getApplicationIcon(apps.get(i).packageName);
                    break;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            } else {

                if ((apps.get(i).flags & ApplicationInfo.FLAG_SYSTEM) == 1 && apps.get(i).packageName.equals(appPackName)) {
                    try {
                        icon = getActivity().getPackageManager().getApplicationIcon(apps.get(i).packageName);
                    }
                    catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return icon;
    }

    /**
     * Method to check if the app is installed
     * on the device or not
     * @param appPackName
     * @return
     */
    private boolean isAppInstalled(String appPackName) {
        Log.d(TAG,"isAppInstalled()  appPackName::"+appPackName);
        final List<ApplicationInfo> apps = getActivity().getPackageManager().getInstalledApplications(0);
        for (int i = 0; i < apps.size(); i++) {
            if ((apps.get(i).flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                //System app
                if( apps.get(i).packageName.equals(appPackName)) {
                    try {
                        Log.d(TAG,"isAppInstalled()  returning true");
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                if(apps.get(i).packageName.equals(appPackName)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Method to show Snackbar if the app is deleted
     * in order to restore app
     * @param appName
     * @param pos
     * @param app
     */
    private void showSnackBar(String appName,final int pos,final App app){
        final boolean[] isUndo = {false};

        Snackbar.make(coordinatorLayout, "You have removed security from "+appName, Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isUndo[0] = true;
                        Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "App is restored!", Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                        addItemDynamically(app,app.getAppSection());
                        getApps();
                        m_adapter.notifyDataSetChanged();
                    }
                })
                .setActionTextColor(Color.RED)
                .setCallback(new Snackbar.Callback() {

                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                            // Snackbar closed on its own
                            if (!isUndo[0]) {
                                util.deleteRow(app.getId());
                                deleteAppAuthorizationRecord(app.getAppPackName());
                                deleteSectionAppIfSectionIsEmpty(app);
                                callToastMessage("Deleted Successfully");
                            }
                            else{
                                itemArrayList.add(pos, app);
                            }
                        }
                    }
                    @Override
                    public void onShown(Snackbar snackbar) {
                        Log.d(TAG,"onShown is called");
                    }
                })
                .show();

        if(isUndo[0]){
            Log.d(TAG,"retaining app..."+pos+" "+app.getName());
        }
    }

    /**
     * Show overlay
     * @param parentView
     */
    private void showHelpOverlay(View parentView){
        showcaseView = new ShowcaseView.Builder(getActivity())
                .setTarget( new ViewTarget( ((View) parentView.findViewById(R.id.fab)) ) )
                .singleShot(1000)
                .setContentTitle("Set App Lock")
                .setContentText("To add security to an app please click on the Add button and select the app")
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

    /**
     * This method is used to show the alert for Enabling Advance Security
     * @param instr
     */
    public void showAlert(String instr, final App app,final String appPackageName,final String appName){

        Log.d(TAG,"in showAlert");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setTitle(R.string.label_set_lock_on_recently_app_installed_title).setMessage(instr+" "+app.getName());
        alertDialogBuilder.setNegativeButton("Later", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.cancel();
            }
        });
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Log.d(TAG,"installed app name::"+app.getName());
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

                getApps();
                m_adapter.notifyDataSetChanged();
            }
        });
        // Create the AlertDialog object and return it
        alertDialogBuilder.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                }
                return true;
            }
        });
       alertDialog = alertDialogBuilder.show();
    }

    private void deleteAppAuthorizationRecord(String appPackName){

                AppAlreadyAuthorised appAlreadyAuthorised = getAppAlreadyAuthorisedRecord(appPackName,false);
                if(appAlreadyAuthorised != null) {
                    util.deleteAppAuthorizationRecord(appAlreadyAuthorised.getId());
                    Log.d(TAG,"deleteAppAuthorizationRecord is called");
                }
    }

    private AppAlreadyAuthorised getAppAlreadyAuthorisedRecord(String appPackName,boolean isAppName){
        Log.d(TAG,"getAppAlreadyAuthorisedRecord()::"+appPackName+" isAppName::"+isAppName);
            ArrayList<AppAlreadyAuthorised> appAlreadyAuthorisedArrayList = util.getAllAppsAuthorisationInformation();
            for (int i = 0; i < appAlreadyAuthorisedArrayList.size(); i++) {
                if (!isAppName) {
                    Log.d(TAG,"getAppAlreadyAuthorisedRecord():: "+appPackName+" appAlreadyAuthorisedArrayList.get(i).getAppPackName()::"+appAlreadyAuthorisedArrayList.get(i).getAppPackName());
                    if(appAlreadyAuthorisedArrayList.get(i).getAppPackName().equals(appPackName))
                    return appAlreadyAuthorisedArrayList.get(i);
                }
                else{
                    Log.d(TAG,"getAppAlreadyAuthorisedRecord():: "+appPackName+" appAlreadyAuthorisedArrayList.get(i).getAppName()::"+appAlreadyAuthorisedArrayList.get(i).getAppName());
                    if(appAlreadyAuthorisedArrayList.get(i).getAppName().equals(appPackName))
                        return appAlreadyAuthorisedArrayList.get(i);
                }
            }
        return null;
    }

    /**
     * Adapter to set the app data with the recycler view
     */
    public class AppAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public Context context;
        public ArrayList<Item> appArrayList;
        public ArrayList<Item> filterAppArrayList;
        private boolean search = false;
        private SharedPreferences prefs;
        private Fragment fragment;
        public List<ApplicationInfo> appsInfo;

        public class MyViewHolder1 extends RecyclerView.ViewHolder {
            public TextView sectionView;

            public MyViewHolder1(View view) {
                super(view);
                sectionView = (TextView) view.findViewById(R.id.list_item_section_text);
            }
        }

        public class MyViewHolder2 extends RecyclerView.ViewHolder {
            private TextView name, pass, unInstalledAppName;
            private ImageView imageIcon;
            private Button btnSetApp;
            private LinearLayout linearLayoutAppDetails;
            private LinearLayout linearLayoutUninstalledAppDetails;
            public MyViewHolder2(View itemView) {
                super(itemView);
                name= (TextView) itemView.findViewById(R.id.name);
                pass= (TextView) itemView.findViewById(R.id.pass);
                unInstalledAppName = (TextView) itemView.findViewById(R.id.uninstalledAppName);
                imageIcon = (ImageView) itemView.findViewById(R.id.imageIcon);
                btnSetApp = (Button) itemView.findViewById(R.id.btnSetApp);
                linearLayoutAppDetails= (LinearLayout) itemView.findViewById(R.id.linearLayoutAppDetails);
                linearLayoutUninstalledAppDetails= (LinearLayout) itemView.findViewById(R.id.linearLayoutUninstalledAppDetails);
            }
        }

        public void filter(String text) {
            Log.d(TAG,"filter()");
            final ArrayList<Item> results = new ArrayList<Item>();
            if (filterAppArrayList == null)
                filterAppArrayList = appArrayList;
            if (text != null && text.length() > 0) {
                if (filterAppArrayList != null && filterAppArrayList.size() > 0) {
                    for (int i = 0; i < filterAppArrayList.size(); i++) {
                        if (filterAppArrayList.get(i).isSection() == false) {
                            App app = (App) filterAppArrayList.get(i);
                            if (app.getName().toLowerCase()
                                    .contains(text))
                                results.add(app);
                        }
                    }
                }

                for(int i=0;i<results.size();i++){
                    final Item item = results.get(i);
                    if (item != null) {
                        if (!item.isSection()) {
                            App app = (App)item;
                            Log.d(TAG,"results element: "+app.getName());
                        }
                    }

                }
                search = true;
                appArrayList = results;
                notifyDataSetChanged();
            }
        }

        public AppAdapter(Context context, ArrayList<Item> employeeArrayList,Fragment fragment) {
            this.context = context;
            this.appArrayList = employeeArrayList;
            prefs= PreferenceManager.getDefaultSharedPreferences(context);
            appsInfo=new ArrayList<ApplicationInfo>();
            this.fragment = fragment;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView;
            switch (viewType) {
                case 0:
                    Log.d(TAG,"onCreateViewHolder() list_item_section");
                    itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_section, parent, false);
                    return new AppAdapter.MyViewHolder1(itemView);

                case 1: Log.d(TAG,"onCreateViewHolder() list_item");
                    itemView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item, parent, false);
                    return new AppAdapter.MyViewHolder2(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final Item i = appArrayList.get(position);

            switch (holder.getItemViewType()) {
                case 0:
                    SectionApp si = (SectionApp) i;
                    MyViewHolder1 myViewHolder1 = (AppAdapter.MyViewHolder1)holder;
                    myViewHolder1.sectionView.setText(si.getTitle());
                    myViewHolder1.sectionView.setTextColor(context.getResources().getColor(R.color.colorSecondaryText));
                    myViewHolder1.sectionView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
                    myViewHolder1.sectionView.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
                    break;

                case 1:
                    App app = (App) i;
                    MyViewHolder2 myViewHolder2 = (AppAdapter.MyViewHolder2)holder;
                    myViewHolder2.name.setText(app.getName());
                    myViewHolder2.pass.setText(displayCharactersForPassword(app.getPass().length()));
                    boolean isAppInstalled = isAppInstalled(app.getAppPackName());
                    if(isAppInstalled) {
                        Drawable imageAppIconDrawable = getAppImageIcon(app.getAppPackName());
                        myViewHolder2.imageIcon.setImageDrawable(imageAppIconDrawable);
                        myViewHolder2.btnSetApp.setVisibility(View.GONE);
                        myViewHolder2.linearLayoutAppDetails.setVisibility(View.VISIBLE);
                    }
                    else{
                        myViewHolder2.imageIcon.setImageResource(R.drawable.play_store);
                        myViewHolder2.linearLayoutUninstalledAppDetails.setVisibility(View.VISIBLE);
                        myViewHolder2.linearLayoutAppDetails.setVisibility(View.GONE);
                        myViewHolder2.unInstalledAppName.setText(app.getName());
                    }

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            App app1 = (App)appArrayList.get(position);
                            prefs.edit().putBoolean(AppConstant.IS_APP_SELECTED,true).commit();
                            Intent intent = new Intent(context, CheckAppPassword.class);
                            intent.putExtra(AppConstant.APP_ID,app1.getId());
                            context.startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.push_up_in,R.anim.push_down_out);
                        }
                    });

                    myViewHolder2.btnSetApp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            App app1 = (App)appArrayList.get(position);
                            Intent intent = new Intent(context, ReplaceAppUninstalledWithInstalledActivity.class);
                            intent.putExtra("appId",app1.getId());
                            fragment.startActivityForResult(intent, REQUEST_FOR_REPLACE_APP_UNINSTALLED);
                            getActivity().overridePendingTransition(R.anim.push_up_in,R.anim.push_down_out);
                        }
                    });
                    myViewHolder2.imageIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            openThirdPartyApp(appArrayList, position);
                        }
                    });
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            // Just as an example, return 0 or 2 depending on position
            // Note that unlike in ListView adapters, types don't have to be contiguous
            int viewType = 1; //Default is 1
            final Item i = appArrayList.get(position);
            if (i != null) {
                if (i.isSection()) {
                    viewType = 0; //if zero, it will be a header view
                }
            }
            return viewType;
        }

        @Override
        public int getItemCount() {
            return appArrayList.size();
        }
}

    /**
     * This method is used to attach SimpleCallback object with
     * RecyclerView
     */
    private void callSimpleCallBack(){
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition(); //get position which is swipe

                if(!itemArrayList.get(position).isSection()) {

                if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.RIGHT) {    //if swipe left

                        App app = (App) itemArrayList.get(position);
                        deleteAlert(app, position);
                        Log.d(TAG, "itemArrayList::" + ((App) itemArrayList.get(position)).getName() + " position::" + position);

                    }

                }
                else{
                    getApps();
                    m_adapter.notifyDataSetChanged();
                    Log.d(TAG, "itemArrayList:: cannot delete section header item");
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView); //set swipe to recylcerview*/
    }


    /**
     * Async Task to load data
     */
    private class ReadyTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = new ProgressDialog(getActivity());
            progressBar.setCancelable(true);
            progressBar.setMessage("Retrieving your data ...");
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
            progressBar.setMax(100);
            progressBar.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.dismiss();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Gson gson = new Gson();
            String json=prefs.getString(AppConstant.USER_OBJ,null);
            obj = gson.fromJson(json,User.class);
            getApps();
            return null;
        }
    }
}