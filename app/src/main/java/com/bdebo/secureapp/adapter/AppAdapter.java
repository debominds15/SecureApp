/*
package com.bdebo.secureapp.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bdebo.secureapp.R;
import com.bdebo.secureapp.activity.CheckAppPassword;
import com.bdebo.secureapp.helper.ThirdPartyApps;
import com.bdebo.secureapp.helper.ThirdPartyAppsImpl;
import com.bdebo.secureapp.model.App;
import com.bdebo.secureapp.model.AppAlreadyAuthorised;
import com.bdebo.secureapp.model.Item;
import com.bdebo.secureapp.model.SectionApp;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class AppAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String TAG = AppAdapter.class.getSimpleName();
    public Context context;
    public ArrayList<Item> appArrayList;
    public ArrayList<Item> filterAppArrayList;
    private boolean search = false;
    private SharedPreferences prefs;
    public static List<ApplicationInfo> appsInfo;
    private ThirdPartyApps apps=new ThirdPartyAppsImpl();
    private boolean checkApp = false;

    public class MyViewHolder1 extends RecyclerView.ViewHolder {
        public TextView sectionView;

        public MyViewHolder1(View view) {
            super(view);
            sectionView = (TextView) view.findViewById(R.id.list_item_section_text);
        }
    }

    public class MyViewHolder2 extends RecyclerView.ViewHolder {
        public TextView name, pass;
        ImageView imageIcon;
        public MyViewHolder2(View itemView) {
            super(itemView);
            name= (TextView) itemView.findViewById(R.id.name);
            pass= (TextView) itemView.findViewById(R.id.pass);
            imageIcon = (ImageView) itemView.findViewById(R.id.imageIcon);
        }
    }

*/
/*    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<Item> results = new ArrayList<Item>();
                if (orig == null)
                    orig = appArrayList;
                if (constraint != null) {
                    if (orig != null && orig.size() > 0) {
                        for (int i=0;i<orig.size();i++) {
                            if(orig.get(i).isSection()==false) {
                                App app=(App) orig.get(i);
                                if (app.getName().toLowerCase()
                                        .contains(constraint.toString()))
                                    results.add(app);
                            }
                        }
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                search = true;
                appArrayList = (ArrayList<Item>) results.values;
                notifyDataSetChanged();

            }
        };
    }*//*

    public void filter(String text) {
        Log.d(TAG,"filter()");
        final ArrayList<Item> results = new ArrayList<Item>();
        if (filterAppArrayList == null)
            filterAppArrayList = appArrayList;
        if (text != null) {
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
        */
/*appArrayList.clear();
        if(text.isEmpty()){
            appArrayList.addAll(itemsCopy);
        } else{
            text = text.toLowerCase();
            for(PhoneBookItem item: itemsCopy){
                if(item.name.toLowerCase().contains(text) || item.phone.toLowerCase().contains(text)){
                    appArrayList.add(item);
                }
            }
        }
        notifyDataSetChanged();*//*

    }

    public AppAdapter(Context context, int textViewResourceId, ArrayList<Item> employeeArrayList) {
        this.context = context;
        this.appArrayList = employeeArrayList;
        prefs= PreferenceManager.getDefaultSharedPreferences(context);
        appsInfo=new ArrayList<ApplicationInfo>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;
        switch (viewType) {
            case 0:
                Log.d(TAG,"onCreateViewHolder() list_item_section");
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_section, parent, false);
                    return new MyViewHolder1(itemView);

            case 1: Log.d(TAG,"onCreateViewHolder() list_item");
                    itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
                return new MyViewHolder2(itemView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Item i = appArrayList.get(position);

            switch (holder.getItemViewType()) {
            case 0:
                SectionApp si = (SectionApp) i;
                MyViewHolder1 myViewHolder1 = (MyViewHolder1)holder;
                myViewHolder1.sectionView.setText(si.getTitle());
                myViewHolder1.sectionView.setTextColor(context.getResources().getColor(R.color.colorSecondaryText));
                myViewHolder1.sectionView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
                myViewHolder1.sectionView.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
                break;

            case 1:
                App app = (App) i;
                MyViewHolder2 myViewHolder2 = (MyViewHolder2)holder;
                myViewHolder2.name.setText(app.getName());
                myViewHolder2.pass.setText(displayCharactersForPassword(app.getPass().length()));
                boolean isAppInstalled = isAppInstalled(app.getAppPackName());
                Log.d(TAG,"appName: "+app.getName()+" isAppInstalled:: "+isAppInstalled);
                if(isAppInstalled) {
                    Drawable imageAppIconDrawable = getAppImageIcon(app.getAppPackName());
                    myViewHolder2.imageIcon.setImageDrawable(imageAppIconDrawable);
                }
                else{
                    myViewHolder2.imageIcon.setImageResource(R.drawable.play_store);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        App app1 = (App)appArrayList.get(position);
                        prefs.edit().putBoolean(AppConstant.IS_APP_SELECTED,true).commit();
                        Intent intent = new Intent(context, CheckAppPassword.class);
                        intent.putExtra(AppConstant.APP_ID,app1.getId());
                        context.startActivity(intent);
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
    public int getItemCount() {
        return appArrayList.size();
    }

    */
/*
  * Method to display characters in asterisk for security
  *//*

    private String displayCharactersForPassword(int length)
    {
        String str="";
        for(int i=0;i<length;i++)
        {
            str=str+"*";
        }
        return str;
    }
    */
/**
     * Method to check if the app is installed
     * on the device or not
     * @param appPackName
     * @return
     *//*

    private boolean isAppInstalled(String appPackName) {
        Log.d(TAG,"isAppInstalled()  appPackName::"+appPackName);
        final List<ApplicationInfo> apps = context.getPackageManager().getInstalledApplications(0);
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

    */
/**
     * Method to get the drawable image icon based on package name
     * @param appPackName
     * @return
     *//*

    private Drawable getAppImageIcon(String appPackName) {
        Drawable icon = null;
        final List<ApplicationInfo> apps = context.getPackageManager().getInstalledApplications(0);
        for (int i = 0; i < apps.size(); i++) {
            ApplicationInfo packageInfo;

            if ((apps.get(i).flags & ApplicationInfo.FLAG_SYSTEM) != 1 && apps.get(i).packageName.equals(appPackName)) {
                //System app
                try {
                    icon = context.getPackageManager().getApplicationIcon(apps.get(i).packageName);
                    break;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            } else {

                if ((apps.get(i).flags & ApplicationInfo.FLAG_SYSTEM) == 1 && apps.get(i).packageName.equals(appPackName)) {
                    try {
                        icon = context.getPackageManager().getApplicationIcon(apps.get(i).packageName);
                    }
                    catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return icon;
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

    */
/**
     * Method to open the third party app securely
     * @param employeeArrayList
     * @param position
     *//*

    private void openThirdPartyApp(ArrayList<Item> employeeArrayList,int position)
    {
        prefs.edit().putBoolean(AppConstant.IS_OTHER_APP_OPENED,true).commit();
        appsInfo=apps.getAllThirdPartyApps(context);
        if(employeeArrayList.get(position).isSection()==false) {
            for (ApplicationInfo a : appsInfo) {
                checkApp = false;
                try {
                    App app=(App) employeeArrayList.get(position);
                    String appNameCheck = (String) context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(a.packageName, PackageManager.GET_META_DATA));
                    if (app.getName().equals(appNameCheck) && app.isAppPresent() == true) {
                        checkApp = true;
                        updateAppAuthorizedAppOpenThroughSecureApp(app.getAppPackName(),true);
                        Toast.makeText(context, "Opening " + appNameCheck, Toast.LENGTH_SHORT).show();

                        Intent intent = context.getPackageManager().getLaunchIntentForPackage(a.packageName);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        context.startActivity(intent);

                        break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (checkApp == false) {

                App app = (App) employeeArrayList.get(position);
                Log.d(TAG,"openThirdPartyApp():: appPackName::"+app.getAppPackName()+" isAppPresent::"+app.isAppPresent());
                Toast.makeText(context, app+" is not installed currently in your device", Toast.LENGTH_SHORT).show();
                final String appPackageName = app.getName();
                long modifiedTime = System.currentTimeMillis();
                prefs.edit().putLong("modified_time", modifiedTime);
                //Opening play store with auto search
                Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://search?q=" + appPackageName));
                context.startActivity(goToMarket);
            }
        }
    }

    */
/**
     * Method to update the table
     * to directly open the app
     * through SecureApp
     * @param appPackName
     * @param isAppOpenThroughSecureApp
     *//*

    private void updateAppAuthorizedAppOpenThroughSecureApp(String appPackName,boolean isAppOpenThroughSecureApp){
        SecureUtil util = new SecureUtil(context);
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

    */
/**
     * Method to delete the app from ListView and database
     * @param app
     * @param position
     *//*

    private void deleteAlert(final App app,final  int position)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle("Application").setMessage("Are you sure you want to remove lock from "+app.getName()+"?");

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.cancel();
            }
        });
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                appArrayList.remove(position);
                deleteSectionAppIfSectionIsEmpty(app);
                //showSnackBar(app.getName(),position,app);
                notifyDataSetChanged();
            }
        });
        alertDialog.show();
    }


   */
/* private void showSnackBar(String appName,final int pos,final App app){
        final boolean[] isUndo = {false};

        Snackbar.make(coordinatorLayout, "You have removed security from "+appName, Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isUndo[0] = true;
                        Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "App is restored!", Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                        addItemDynamically(app,app.getAppSection());
                        notifyDataSetChanged();
                    }
                })
                .setActionTextColor(Color.RED)
                .setCallback(new Snackbar.Callback() {

                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                            // Snackbar closed on its own
                            if (!isUndo[0]) {
                                SecureUtil util = new SecureUtil(context);
                                util.deleteRow(app.getId());
                                deleteAppAuthorizationRecord(app.getAppPackName());
                                deleteSectionAppIfSectionIsEmpty(app);
                                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_LONG).show();
                            }
                            else{
                                appArrayList.add(pos, app);
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
*//*

    private void deleteAppAuthorizationRecord(String appPackName){

        SecureUtil util = new SecureUtil(context);
        AppAlreadyAuthorised appAlreadyAuthorised = getAppAlreadyAuthorisedRecord(appPackName,false);
        if(appAlreadyAuthorised != null) {
            util.deleteAppAuthorizationRecord(appAlreadyAuthorised.getId());
            Log.d(TAG,"deleteAppAuthorizationRecord is called");
        }
    }
    private AppAlreadyAuthorised getAppAlreadyAuthorisedRecord(String appPackName,boolean isAppName){
        SecureUtil util = new SecureUtil(context);
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


    */
/**
     * Method to add item dynamically in ListView
     * @param app
     * @param section
     *//*

    private void addItemDynamically(App app,String section)
    {
        if(!section.equals("Select section")) {
            int pos = 0;
            for (int i = 0; i < appArrayList.size(); i++) {
                if (appArrayList.get(i).isSection()) {
                    SectionApp sectionApp = (SectionApp) appArrayList.get(i);
                    if (sectionApp.getTitle().equals(section)) {
                        pos = i + 1;
                        break;
                    }
                }
                if (i == appArrayList.size() - 1) {

                    SectionApp sectionApp = new SectionApp();
                    sectionApp.setTitle(section);
                    appArrayList.add(sectionApp);
                    pos = appArrayList.size();
                }
            }
            for (int i = pos; i < appArrayList.size(); i++) {
                if (appArrayList.get(i).isSection()) {
                    pos = i;
                    break;
                }
                if (i == appArrayList.size() - 1) {
                    pos = i + 1;
                    break;
                }
            }
            if (appArrayList.size() == 0) {
                SectionApp sectionApp = new SectionApp();
                sectionApp.setTitle(app.getAppSection());
                appArrayList.add(pos, sectionApp);
                pos++;
            }

            appArrayList.add(pos, app);
        }
    }


    */
/**
     * Method to delete the section if apps are not present under that section
     * @param app
     *//*

    private void deleteSectionAppIfSectionIsEmpty(App app)
    {
        if(isSectionEmpty(app.getAppSection())) {
            for (int i = 0; i < appArrayList.size(); i++) {
                if (appArrayList.get(i).isSection() == true) {
                    SectionApp sectionApp = (SectionApp) appArrayList.get(i);
                    if (sectionApp.getTitle().equals(app.getAppSection()))
                        appArrayList.remove(i);
                }
            }
        }
    }

    */
/**
     * Check whether if the all the apps are deleted under particular section
     * @return
     *//*

    private boolean isSectionEmpty(String sectionName){

        for(int i=0;i<appArrayList.size();i++) {

            if(!appArrayList.get(i).isSection()){
                App app = (App) appArrayList.get(i);
                if(app.getAppSection().equals(sectionName)){
                    return  false;
                }
            }
        }
        return  true;
    }



    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition(); //get position which is swipe

            if (direction == ItemTouchHelper.LEFT) {    //if swipe left

                deleteAlert((App) appArrayList.get(position), position);

          */
/*      AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this); //alert for confirm to delete
                builder.setMessage("Are you sure to delete?");    //set message

                builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() { //when click on DELETE
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.notifyItemRemoved(position);    //item removed from recylcerview
                        sqldatabase.execSQL("delete from " + TABLE_NAME + " where _id='" + (position + 1) + "'"); //query for delete
                        list.remove(position);  //then remove item

                        return;
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {  //not removing items if cancel is done
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.notifyItemRemoved(position + 1);    //notifies the RecyclerView Adapter that data in adapter has been removed at a particular position.
                        adapter.notifyItemRangeChanged(position, adapter.getItemCount());   //notifies the RecyclerView Adapter that positions of element in adapter has been changed from position(removed element index to end of list), please update it.
                        return;
                    }
                }).show();  //show alert dialog*//*

            }
        }
    };

}*/
