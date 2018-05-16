package com.bdebo.secureapp.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bdebo.secureapp.R;
import com.bdebo.secureapp.model.AppLockScreen;
import com.bdebo.secureapp.model.Device;
import com.bdebo.secureapp.provider.DBHelper;
import com.bdebo.secureapp.model.User;
import com.bdebo.secureapp.model.App;
import com.bdebo.secureapp.model.AppAlreadyAuthorised;

import net.sqlcipher.SQLException;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;

import static com.bdebo.secureapp.provider.DBHelper.TableAppLockScreenMetaData.APP_THEME_IMAGE_POS_LOCK_SCREEN;
import static com.bdebo.secureapp.provider.DBHelper.TableAppLockScreenMetaData.APP_THEME_LOCK_SCREEN_ID;
import static com.bdebo.secureapp.provider.DBHelper.TableAppLockScreenMetaData.TABLE_APP_LOCK_SCREEN;
import static com.bdebo.secureapp.provider.DBHelper.TableAppMetaData.APP_LOCK_TYPE;
import static com.bdebo.secureapp.provider.DBHelper.TableAppMetaData.APP_PRESENT;
import static com.bdebo.secureapp.provider.DBHelper.TableAppMetaData.APP_SECTION;
import static com.bdebo.secureapp.provider.DBHelper.TableAppMetaData.ID;
import static com.bdebo.secureapp.provider.DBHelper.TableAppMetaData.NAME;
import static com.bdebo.secureapp.provider.DBHelper.TableAppMetaData.PASS;
import static com.bdebo.secureapp.provider.DBHelper.TableAppMetaData.TABLE_NAME;
import static com.bdebo.secureapp.provider.DBHelper.TableAppMetaData.UID;
import static com.bdebo.secureapp.provider.DBHelper.TableAppsAuthorizedMetaData.APPS_AUTHORIZED_APP_NAME;
import static com.bdebo.secureapp.provider.DBHelper.TableAppsAuthorizedMetaData.APPS_AUTHORIZED_APP_PACK_NAME;
import static com.bdebo.secureapp.provider.DBHelper.TableAppsAuthorizedMetaData.APPS_AUTHORIZED_ID;
import static com.bdebo.secureapp.provider.DBHelper.TableAppsAuthorizedMetaData.APPS_AUTHORIZED_IS_APP_ALREADY_AUTHORISED;
import static com.bdebo.secureapp.provider.DBHelper.TableAppsAuthorizedMetaData.APPS_AUTHORIZED_OPEN_THROUGH_SECURE_APP;
import static com.bdebo.secureapp.provider.DBHelper.TableAppsAuthorizedMetaData.TABLE_NAME_APPS_AUTHORIZED;
import static com.bdebo.secureapp.provider.DBHelper.TableDeviceLockScreenMetaData.DEVICE_LOCK_PASSWORD;
import static com.bdebo.secureapp.provider.DBHelper.TableDeviceLockScreenMetaData.DEVICE_LOCK_SCREEN_ID;
import static com.bdebo.secureapp.provider.DBHelper.TableDeviceLockScreenMetaData.DEVICE_LOCK_TYPE;
import static com.bdebo.secureapp.provider.DBHelper.TableDeviceLockScreenMetaData.DEVICE_THEME_IMAGE_POS_LOCK_SCREEN;
import static com.bdebo.secureapp.provider.DBHelper.TableDeviceLockScreenMetaData.IS_DEVICE_LOCK_ENABLED;
import static com.bdebo.secureapp.provider.DBHelper.TableDeviceLockScreenMetaData.TABLE_DEVICE_LOCK_SCREEN;
import static com.bdebo.secureapp.provider.DBHelper.TableUserMetaData.TABLE_NAME_USER;
import static com.bdebo.secureapp.provider.DBHelper.TableUserMetaData.U_NAME;
import static com.bdebo.secureapp.provider.DBHelper.TableUserMetaData.U_PASS;
import static com.bdebo.secureapp.provider.DBHelper.TableUserMetaData.U_SEC_A;
import static com.bdebo.secureapp.provider.DBHelper.TableUserMetaData.U_SEC_A2;
import static com.bdebo.secureapp.provider.DBHelper.TableUserMetaData.U_SEC_Q;
import static com.bdebo.secureapp.provider.DBHelper.TableUserMetaData.U_SEC_Q2;
import static com.bdebo.secureapp.provider.DBHelper.TableUserMetaData.U_UNIQUE_CODE;
import static com.bdebo.secureapp.provider.DBHelper.TableUserMetaData.U_USER_NAME;


/**
 * This class is used to do various DB operations
 */
public class SecureUtil {

    private static String TAG = SecureUtil.class.getSimpleName();

    // Database fields
    private SQLiteDatabase database;
    private DBHelper dbHelper;

    public SecureUtil(Context context) {
        dbHelper = new DBHelper(context);
        SQLiteDatabase.loadLibs(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase("mypass123");
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Holds the SecureApp password
     * @param context
     * @return
     */
    public String getAppPass(Context context) {
        SQLiteDatabase.loadLibs(context);
        User user = getAllUsers().get(0);
        return user.getPass();
    }

    /**
     * Read all the apps  from DB for which lock is set
     * @param uid
     * @return
     */
    public ArrayList<App> getAllApp(int uid)
    {
        ArrayList<App> list=new ArrayList<App>();

        open();
        try {
            if (database.isOpen()) {
                Cursor myCursor = database.query(TABLE_NAME, null, null, null, null, null, null);
                App app;
                if (myCursor.getCount() > 0) {
                    for (int i = 0; i < myCursor.getCount(); i++) {
                        myCursor.moveToNext();
                        if (myCursor.getInt(1) == uid) {
                            app = new App(myCursor.getInt(0), myCursor.getInt(1), myCursor.getString(2), myCursor.getString(3), myCursor.getInt(5) > 0, myCursor.getString(6), myCursor.getString(7), myCursor.getString(8));
                            app.setId(myCursor.getInt(0));
                            app.setUid(myCursor.getInt(1));
                            app.setName(myCursor.getString(2));
                            app.setPass(myCursor.getString(3));
                            app.setAppPresent(myCursor.getInt(5) > 0);
                            app.setAppSection(myCursor.getString(6));
                            app.setAppLockType(myCursor.getString(7));
                            app.setAppPackName(myCursor.getString(8));
                            list.add(app);
                        }
                    }

                }
                myCursor.close();
            }
        }catch (Exception e){
            Log.e(TAG,"error: "+e.getLocalizedMessage());
        }
        finally
        {
            if (database != null) {
                database.close();
            }
            close();
        }

        return list;
    }

    /**
     *  Insert the apps info in DB
     * @param app
     */
    public void insertRecords(App app)
    {
        open();
        try {
            ContentValues values = new ContentValues();

            values.put(UID, app.getUid());
            values.put(NAME, app.getName());
            values.put(PASS, app.getPass());
            values.put(APP_PRESENT, app.isAppPresent());
            values.put(APP_SECTION, app.getAppSection());
            values.put(APP_LOCK_TYPE, app.getAppLockType());
            values.put(APPS_AUTHORIZED_APP_PACK_NAME, app.getAppPackName());
            database.insert(TABLE_NAME, null, values);
        }
        catch (Exception e){
            Log.e(TAG,"exception: "+e.getLocalizedMessage());
        }
        finally {
            if (database != null) {
                database.close();
            }
            close();
        }
    }
    /**
     * Remove an app record from DB
     * @param id
     * @return
     */
    public void deleteRow(int id)
    {
        open();
        database.delete(TABLE_NAME, ID+"="+id, null);
        close();
    }

    /**
     * Remove an app authorization record from DB
     * @param id
     * @return
     */
    public void deleteAppAuthorizationRecord(int id)
    {
        open();
        database.delete(TABLE_NAME_APPS_AUTHORIZED, APPS_AUTHORIZED_ID+"="+id, null);
        close();
    }

    /**
     * Update app lock type in DB
     * @param app
     * @param id
     * @param uid
     */
    public void updateRecord(App app,int id,int uid){

        open();
        try{
        ContentValues values=new ContentValues();

        values.put(NAME, app.getName());
        values.put(UID,uid);
        values.put(PASS,app.getPass());
        values.put(APP_PRESENT,app.isAppPresent());
        values.put(APPS_AUTHORIZED_APP_PACK_NAME, app.getAppPackName());
        values.put(APP_LOCK_TYPE,app.getAppLockType());
        database.update(TABLE_NAME, values, ID+"="+id, null);
        }
        catch (Exception e){
            Log.e(TAG,"exception: "+e.getLocalizedMessage());
        }
        finally {
            if (database != null) {
                database.close();
            }
            close();
        }
    }

    /**
     * Read all the apps authorization info from DB
     * apps which are already authorized
     * and apps for which authorization is still left. In this
     * case lock screen will bwe shown
     * @return apps list
     */
    public ArrayList<AppAlreadyAuthorised> getAllAppsAuthorisationInformation()
    {
        ArrayList<AppAlreadyAuthorised> list=new ArrayList<AppAlreadyAuthorised>();
        open();
        try {
            database.beginTransaction();
            if (database.isOpen()) {
                Cursor myCursor = database.query(TABLE_NAME_APPS_AUTHORIZED, null, null, null, null, null, null);
                AppAlreadyAuthorised appAlreadyAuthorised = null;
                while (myCursor != null && myCursor.moveToNext()) {
                        appAlreadyAuthorised = new AppAlreadyAuthorised(myCursor.getInt(0), myCursor.getString(1), myCursor.getString(2), myCursor.getInt(3) > 0, myCursor.getInt(4) > 0);
                        appAlreadyAuthorised.setId(myCursor.getInt(0));
                        appAlreadyAuthorised.setAppName(myCursor.getString(1));
                        appAlreadyAuthorised.setAppPackName(myCursor.getString(2));
                        appAlreadyAuthorised.setIsAppAuthorised(myCursor.getInt(3) > 0);
                        appAlreadyAuthorised.setAppOpenThroughSecureApp(myCursor.getInt(4) > 0);
                        list.add(appAlreadyAuthorised);
                    }

                myCursor.close();
                database.setTransactionSuccessful();
            }
            else{
                Log.e(TAG,"Database is closed!!!");
            }
        }catch (Exception e){
            Log.e(TAG,"error: "+e.getLocalizedMessage());
        }
        finally
        {
            if (database != null) {
                database.endTransaction();
                database.close();
            }

            close();
        }
        return list;
    }

    /**
     * Get App details based on app package name
     * @return App list
     */
    public ArrayList<App> getAllAppsFromDB(){
        ArrayList<App> getAllApps =  new ArrayList<App>();
        open();
        App app;
        Cursor myCursor;
        try {
            if (database.isOpen()) {
                database.beginTransaction();
                myCursor = database.query(TABLE_NAME, null, null, null, null, null, null);

                while (myCursor != null && myCursor.moveToNext()) {

                        app = new App(myCursor.getInt(0), myCursor.getInt(1), myCursor.getString(2), myCursor.getString(3), myCursor.getInt(5) > 0, myCursor.getString(6), myCursor.getString(7), myCursor.getString(8));
                        app.setId(myCursor.getInt(0));
                        app.setUid(myCursor.getInt(1));
                        app.setName(myCursor.getString(2));
                        app.setPass(myCursor.getString(3));
                        app.setAppPresent(myCursor.getInt(5) > 0);
                        app.setAppSection(myCursor.getString(6));
                        app.setAppLockType(myCursor.getString(7));
                        app.setAppPackName(myCursor.getString(8));
                        getAllApps.add(app);
                }
                myCursor.close();
                database.setTransactionSuccessful();
            }
            else{
                Log.e(TAG,"database is closed!!!");
            }

        }
        catch (Exception e){
            Log.e(TAG,"exception: "+e.getLocalizedMessage());
        }
        finally
        {
            if (database != null) {
                database.endTransaction();
                database.close();
            }
            close();
        }
        return getAllApps;
    }
/*    *//**
     * Get App details based on app package name
     * @param appPackName
     * @return App
     *//*
    public App getAppDetailsFromAppPackageName(String appPackName){
        open();
        App app = new App();
        Cursor myCursor;
        try {
            if (database.isOpen()) {
                database.beginTransaction();
                myCursor = database.query(TABLE_NAME, null, null, null, null, null, null);

                while (myCursor != null && myCursor.moveToNext()) {

                    *//*for (int i = 0; i < myCursor.getCount(); i++) {
                        myCursor.moveToNext();*//*

                        if (myCursor.getString(8).equals(appPackName) || appPackName.contains(myCursor.getString(8))) {
                            app = new App(myCursor.getInt(0), myCursor.getInt(1), myCursor.getString(2), myCursor.getString(3), myCursor.getInt(5) > 0, myCursor.getString(6), myCursor.getString(7), myCursor.getString(8));
                            app.setId(myCursor.getInt(0));
                            app.setUid(myCursor.getInt(1));
                            app.setName(myCursor.getString(2));
                            app.setPass(myCursor.getString(3));
                            app.setAppPresent(myCursor.getInt(5) > 0);
                            app.setAppSection(myCursor.getString(6));
                            app.setAppLockType(myCursor.getString(7));
                            app.setAppPackName(myCursor.getString(8));
                            break;
                        }
                    }
                myCursor.close();
                database.setTransactionSuccessful();
                }
            else{
                Log.e(TAG,"database is closed!!!");
            }

        }
        catch (Exception e){
            Log.e(TAG,"exception: "+e.getLocalizedMessage());
        }
        finally
        {
            if (database != null) {
                database.endTransaction();
                database.close();
            }
        }
        return app;
    }*/

    /**
     * Insert apps authorization details in DB which are added for security
     * and assigning false for authorization details
     * @param appAlreadyAuthorised
     */
    public void insertAppAuthorisationRecords(AppAlreadyAuthorised appAlreadyAuthorised)
    {
        open();
        try {
            ContentValues values = new ContentValues();

            values.put(APPS_AUTHORIZED_APP_NAME, appAlreadyAuthorised.getAppName());
            values.put(APPS_AUTHORIZED_APP_PACK_NAME, appAlreadyAuthorised.getAppPackName());
            values.put(APPS_AUTHORIZED_IS_APP_ALREADY_AUTHORISED, appAlreadyAuthorised.isAppAuthorised());
            values.put(APPS_AUTHORIZED_OPEN_THROUGH_SECURE_APP, appAlreadyAuthorised.isAppOpenThroughSecureApp());
            database.insert(TABLE_NAME_APPS_AUTHORIZED, null, values);
            Log.d(TAG,"appAlreadyAuthorised.getAppName()::"+appAlreadyAuthorised.getAppName()+" appAlreadyAuthorised.getAppPackName()::"+appAlreadyAuthorised.getAppPackName());
        }
        catch (Exception e){
            Log.e(TAG,"exception: "+e.getLocalizedMessage());
        }
        finally {
            if (database != null) {
                database.close();
            }
            close();
        }
    }

    /**
     * Update apps authorization details in DB for app is installed which was not installed earlier
     * and assigning false for authorization details
     * @param appAlreadyAuthorised
     */
    public void updateAppAuthorisationRecords(AppAlreadyAuthorised appAlreadyAuthorised,int id)
    {
        open();
        try {
            ContentValues values = new ContentValues();
            Log.d(TAG,"isAppOpenThroughSecureApp()::"+appAlreadyAuthorised.isAppOpenThroughSecureApp());
            values.put(APPS_AUTHORIZED_APP_NAME, appAlreadyAuthorised.getAppName());
            values.put(APPS_AUTHORIZED_APP_PACK_NAME, appAlreadyAuthorised.getAppPackName());
            values.put(APPS_AUTHORIZED_IS_APP_ALREADY_AUTHORISED, appAlreadyAuthorised.isAppAuthorised());
            values.put(APPS_AUTHORIZED_OPEN_THROUGH_SECURE_APP, appAlreadyAuthorised.isAppOpenThroughSecureApp());
          //  database.insert(TABLE_NAME_APPS_AUTHORIZED, null, values);
            database.update(TABLE_NAME_APPS_AUTHORIZED, values, APPS_AUTHORIZED_ID+"="+id, null);
            Log.d(TAG,"updateAppAuthorisationRecords... appAlreadyAuthorised.isAppOpenThroughSecureApp()::"+appAlreadyAuthorised.isAppOpenThroughSecureApp());
        }
        catch (Exception e){
            Log.e(TAG,"exception: "+e.getLocalizedMessage());
        }
        finally {
            if (database != null) {
                database.close();
            }
            close();
        }
    }
    /*
     * Update particular theme for App lock screen
     * @param appLockScreen instance
     * @param id
     */
    public void updateParticularThemeImageForAppLockScreen(AppLockScreen appLockScreen, int id)
    {
        open();
        try {
        ContentValues values=new ContentValues();

        values.put(APP_THEME_IMAGE_POS_LOCK_SCREEN, appLockScreen.getPos());
        database.update(TABLE_APP_LOCK_SCREEN, values, APP_THEME_LOCK_SCREEN_ID+"="+id, null);
        }
        catch (Exception e){
            Log.e(TAG,"exception: "+e.getLocalizedMessage());
        }
        finally {
            if (database != null) {
                database.close();
            }
            close();
        }
    }
    /**
     * Resetting Authorization details of app
     * as false
     */
    public void assignAllAppsAuthorisationFalse()
    {
        ArrayList<AppAlreadyAuthorised> getAllAppsForAuthorization = getAllAppsAuthorisationInformation();
        if(getAllAppsForAuthorization != null) {
            for (int i = 0; i < getAllAppsForAuthorization.size(); i++) {
                AppAlreadyAuthorised appAlreadyAuthorised = new AppAlreadyAuthorised();
                appAlreadyAuthorised.setAppName(getAllAppsForAuthorization.get(i).getAppName());
                appAlreadyAuthorised.setAppPackName(getAllAppsForAuthorization.get(i).getAppPackName());
                appAlreadyAuthorised.setId(getAllAppsForAuthorization.get(i).getId());
                appAlreadyAuthorised.setIsAppAuthorised(false);
                appAlreadyAuthorised.setAppOpenThroughSecureApp(false);
                updateAppAuthorisationRecords(appAlreadyAuthorised, getAllAppsForAuthorization.get(i).getId());
            }
        }

        //Log.d(TAG, "Successfully updated!! "+appAlreadyAuthorised.isAppOpenThroughSecureApp()+" "+appAlreadyAuthorised.isAppAuthorised());

    }


    /**
     * Insert Authorization details of app
     * if app is authorized or not
     * @param appAlreadyAuthorised
     * @param id
     */
    public void insertParticularRecordForAppAuthorisation(AppAlreadyAuthorised appAlreadyAuthorised,int id)
    {
        open();
        try{
        database.beginTransaction();

        ContentValues values=new ContentValues();
        values.put(APPS_AUTHORIZED_APP_NAME, appAlreadyAuthorised.getAppName());
        values.put(APPS_AUTHORIZED_APP_PACK_NAME, appAlreadyAuthorised.getAppPackName());
        values.put(APPS_AUTHORIZED_IS_APP_ALREADY_AUTHORISED,appAlreadyAuthorised.isAppAuthorised());
        values.put(APPS_AUTHORIZED_OPEN_THROUGH_SECURE_APP, appAlreadyAuthorised.isAppOpenThroughSecureApp());
        database.update(TABLE_NAME_APPS_AUTHORIZED, values, "t_id="+id, null);
        database.setTransactionSuccessful();
            Log.d(TAG,"insertParticularRecordForAppAuthorisation()"+appAlreadyAuthorised.getAppName()+" "+id);
        }
        catch (Exception e){
            Log.e(TAG,"exception: "+e.getLocalizedMessage());
        }
        finally {
            if (database != null && database.isOpen()) {
                database.endTransaction();
                database.close();
            }
            close();
        }
        Log.d(TAG, "Successfully updated!! "+appAlreadyAuthorised.isAppOpenThroughSecureApp()+" "+appAlreadyAuthorised.isAppAuthorised());

    }

    /**
     * Fetch the user details
     * @return
     */
    public ArrayList<User> getAllUsers()
    {
        ArrayList<User> list=new ArrayList<User>();
        open();

        try{
        Cursor myCursor=database.query(TABLE_NAME_USER, null, null, null, null, null, null);
        User user;
        if(myCursor.getCount()>0)
        {
            for(int i=0;i<myCursor.getCount();i++)
            {
                myCursor.moveToNext();
                user=new User(myCursor.getInt(0),myCursor.getString(1),myCursor.getString(2),myCursor.getString(3),myCursor.getString(4),myCursor.getString(5),myCursor.getString(6),myCursor.getString(7),myCursor.getInt(8));
                user.setId(myCursor.getInt(0));
                user.setName(myCursor.getString(1));
                user.setUsername(myCursor.getString(2));
                user.setPass(myCursor.getString(3));
                user.setSec_q1(myCursor.getString(4));
                user.setSec_ans1(myCursor.getString(5));
                user.setSec_q2(myCursor.getString(6));
                user.setSec_ans2(myCursor.getString(7));
                user.setUniqueCode(myCursor.getInt(8));
                list.add(user);
            }

        }
            myCursor.close();
        }catch (Exception e){
            Log.e(TAG,"error: "+e.getLocalizedMessage());
        }
        finally
        {
            if (database != null) {
                database.close();
            }
            close();
        }


        return list;
    }

    /**
     * Insert user details after registration is done
     * @param user
     */
    public void insertRecords(User user)
    {
        open();
        try{
        ContentValues values=new ContentValues();

        values.put(U_NAME, user.getName());
        values.put(U_USER_NAME, user.getUsername());
        values.put(U_PASS,user.getPass());
        values.put(U_SEC_Q, user.getSec_q1());
        values.put(U_SEC_A, user.getSec_ans1());
        values.put(U_SEC_Q2, user.getSec_q2());
        values.put(U_SEC_A2, user.getSec_ans2());
        values.put(U_UNIQUE_CODE,user.getUniqueCode());
        database.insert(TABLE_NAME_USER, null, values);
        }
        catch (Exception e){
            Log.e(TAG,"exception: "+e.getLocalizedMessage());
        }
        finally {
            if (database != null) {
                database.close();
            }
        }
    }

    /**
     * Update User details
     * @param u
     * @param id
     */
    public void updateUserDetails(User u, int id)
    {
        open();
        try{
        ContentValues values=new ContentValues();
        values.put(U_NAME, u.getName());
        values.put(U_USER_NAME, u.getUsername());
        values.put(U_UNIQUE_CODE,u.getUniqueCode());
        values.put(U_PASS,u.getPass());
        values.put(U_SEC_Q, u.getSec_q1());
        values.put(U_SEC_A, u.getSec_ans1());
        values.put(U_SEC_Q2, u.getSec_q2());
        values.put(U_SEC_A2, u.getSec_ans2());
        database.update(TABLE_NAME_USER, values, "t_id="+id, null);
        }
        catch (Exception e){
            Log.e(TAG,"exception: "+e.getLocalizedMessage());
        }
        finally {
            if (database != null) {
                database.close();
            }
        }
    }

    /**
     * Fetch user details
     * @param id
     * @return
     */
    public User getParticularUser(int id)
    {
        ArrayList<User> userArrayList=getAllUsers();
        User user;
        for(int i=0;i<userArrayList.size();i++)
        {
            if(userArrayList.get(i).getId()==id)
            {
                user=userArrayList.get(i);
                return user;
            }
        }

        return null;
    }

    /**
     * Fetch App lock screen details
     * @return
     */
    public AppLockScreen getAppLockScreenDetails()
    {
        open();
        AppLockScreen appLockScreen = new AppLockScreen();
        try{
        Cursor myCursor=database.query(TABLE_APP_LOCK_SCREEN, null, null, null, null, null, null);

        if(myCursor.getCount()>0)
        {
            for(int i=0;i<myCursor.getCount();i++)
            {
                myCursor.moveToNext();
                appLockScreen=new AppLockScreen(myCursor.getInt(0),myCursor.getInt(1));
                appLockScreen.setId(myCursor.getInt(0));
                appLockScreen.setPos(myCursor.getInt(1));
            }
            myCursor.close();
        }
        }catch (Exception e){
            Log.e(TAG,"error: "+e.getLocalizedMessage());
        }
        finally
        {
            if (database != null) {
                database.close();
            }
            close();
        }
        return appLockScreen;
    }

    /**
     * Insert app theme image position
     * to set theme for App Lock Screen
     * @param appLockScreen
     */
    public void insertAppThemeImagePos(AppLockScreen appLockScreen)
    {
        open();
        try{
        ContentValues values=new ContentValues();
        values.put(APP_THEME_IMAGE_POS_LOCK_SCREEN, appLockScreen.getPos());
        database.insert(TABLE_APP_LOCK_SCREEN, null, values);

        }catch (Exception e){
            Log.e(TAG,"error: "+e.getLocalizedMessage());
        }
        finally
        {
            if (database != null) {
                database.close();
            }
        }
    }

    /**
     * Fetch Device Lock details
     * @param uid
     * @return
     */
    public Device getDeviceLockDetails(int uid){

        open();
        Device device = null;
        try{

        if (database.isOpen()) {

            Cursor myCursor = database.query(TABLE_DEVICE_LOCK_SCREEN, null, null, null, null, null, null);
            if (myCursor.getCount() > 0) {
                for (int i = 0; i < myCursor.getCount(); i++) {
                    myCursor.moveToNext();
                    if (myCursor.getInt(0) == uid) {
                        device = new Device(myCursor.getInt(0), myCursor.getString(1), myCursor.getString(2), myCursor.getInt(3) > 0, myCursor.getInt(4));
                        device.setId(myCursor.getInt(0));
                        device.setPassword(myCursor.getString(1));
                        device.setLockType(myCursor.getString(2));
                        device.setLockEnabled(myCursor.getInt(3) > 0);
                        device.setImagePosition(myCursor.getInt(4));
                    }
                }

            }
            myCursor.close();
        }
         else{
            Log.e(TAG,"Database is not open");
        }
        }catch (Exception e){
            Log.e(TAG,"error: "+e.getLocalizedMessage());
        }
        finally
        {
            if (database != null) {
                database.close();
            }
            close();
        }
        return device;
    }

    /**
     * Update device lock details in DB
     * @param device
     * @param id
     */
    public void updateDeviceLockDetails(Device device, int id){

        open();
        try {
        ContentValues values=new ContentValues();

        values.put(DEVICE_LOCK_SCREEN_ID, device.getId());
        values.put(DEVICE_LOCK_PASSWORD,device.getPassword());
        values.put(DEVICE_LOCK_TYPE, device.getLockType());
        values.put(IS_DEVICE_LOCK_ENABLED, device.isLockEnabled());
        values.put(DEVICE_THEME_IMAGE_POS_LOCK_SCREEN, device.getImagePosition());
        database.update(TABLE_DEVICE_LOCK_SCREEN, values, DEVICE_LOCK_SCREEN_ID +"="+id, null);

        }catch (Exception e){
            Log.e(TAG,"error: "+e.getLocalizedMessage());
        }
        finally
        {
            if (database != null) {
                database.close();
            }
            close();
        }
    }

    /**
     * Insert device lock details in DB
     * to set Device Lock Screen
     * @param device
     */
    public void insertDeviceLockDetails(Device device)
    {
        open();
        try {
        ContentValues values=new ContentValues();

        values.put(DEVICE_LOCK_SCREEN_ID,device.getId());
        values.put(DEVICE_LOCK_PASSWORD, device.getPassword());
        values.put(DEVICE_LOCK_TYPE, device.getLockType());
        values.put(IS_DEVICE_LOCK_ENABLED, device.isLockEnabled());
        values.put(DEVICE_THEME_IMAGE_POS_LOCK_SCREEN, device.getImagePosition());
        database.insert(TABLE_DEVICE_LOCK_SCREEN, null, values);
        }catch (Exception e){
            Log.e(TAG,"error: "+e.getLocalizedMessage());
        }
        finally
        {
            if (database != null) {
                database.close();
            }
            close();
        }
    }


    /**
     *This method is used to set customized toast message
     * @param msg
     */
    public static void setToastMessage(Context context,String msg,View view){


        TextView toastText = (TextView) view.findViewById(R.id.custom_toast_message);
        toastText.setText(msg);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();
    }

    /**
     * Hide the keyboard
     * @param activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
