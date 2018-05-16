package com.bdebo.secureapp.provider;

import android.content.Context;
import android.provider.BaseColumns;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

/**
 * This is the helper class for database operation.
 */
public class DBHelper extends SQLiteOpenHelper {
    /**
     * Holds the database name.
     */
    public static final String DATABASE_NAME = "apptest.db";

    /**
     * Holds the database version number.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Create User Table Query.
     */
    private static final String CREATE_USER_TABLE = "create table " + TableUserMetaData.TABLE_NAME_USER + "(" + TableUserMetaData.U_ID
            + " integer primary key ," + TableUserMetaData.U_NAME + " text," + TableUserMetaData.U_USER_NAME + " text,"
            + TableUserMetaData.U_PASS + " text," + TableUserMetaData.U_SEC_Q + " text," + TableUserMetaData.U_SEC_A
            + " text," + TableUserMetaData.U_SEC_Q2 + " text," + TableUserMetaData.U_SEC_A2
            + " text," + TableUserMetaData.U_UNIQUE_CODE + " integer)";

    /**
     * Create App Table Query.
     */
    private static final String CREATE_APP_TABLE = "create table " + TableAppMetaData.TABLE_NAME + "(" + TableAppMetaData.ID
            + " integer primary key ," + TableAppMetaData.UID + " integer," + TableAppMetaData.NAME
            + " text," + TableAppMetaData.PASS + " text," + TableAppMetaData.IMAGE + " blob,"
            + TableAppMetaData.APP_PRESENT + " INTEGER DEFAULT 0," + TableAppMetaData.APP_SECTION + " text,"
            + TableAppMetaData.APP_LOCK_TYPE + " text," + TableAppMetaData.APPS_AUTHORIZED_APP_PACK_NAME + " text)";

    /**
     * Create Apps Authorized Table Query.
     */
    private static final String CREATE_APPS_AUTHORIZED_TABLE = "create table " + TableAppsAuthorizedMetaData.TABLE_NAME_APPS_AUTHORIZED +
            "(" + TableAppsAuthorizedMetaData.APPS_AUTHORIZED_ID + " integer primary key,"
            + TableAppsAuthorizedMetaData.APPS_AUTHORIZED_APP_NAME + " text,"
            + TableAppsAuthorizedMetaData.APPS_AUTHORIZED_APP_PACK_NAME + " text,"
            + TableAppsAuthorizedMetaData.APPS_AUTHORIZED_IS_APP_ALREADY_AUTHORISED + " INTEGER DEFAULT 0,"
            + TableAppsAuthorizedMetaData.APPS_AUTHORIZED_OPEN_THROUGH_SECURE_APP + " INTEGER DEFAULT 0)";

    /**
     * Create App Lock Screen Query.
     */
    private static final String CREATE_APP_LOCK_SCREEN_TABLE = "create table " + TableAppLockScreenMetaData.TABLE_APP_LOCK_SCREEN +
            "(" + TableAppLockScreenMetaData.APP_THEME_LOCK_SCREEN_ID
            + " integer primary key ," + TableAppLockScreenMetaData.APP_THEME_IMAGE_POS_LOCK_SCREEN + " integer)";

    /**
     * Create Device Lock Screen Query.
     */
    private static final String CREATE_DEVICE_LOCK_SCREEN_TABLE = "create table " + TableDeviceLockScreenMetaData.TABLE_DEVICE_LOCK_SCREEN +
            "(" + TableDeviceLockScreenMetaData.DEVICE_LOCK_SCREEN_ID + " integer primary key ," + TableDeviceLockScreenMetaData.DEVICE_LOCK_PASSWORD
            + " text , " + TableDeviceLockScreenMetaData.DEVICE_LOCK_TYPE + " text ," +
            TableDeviceLockScreenMetaData.IS_DEVICE_LOCK_ENABLED + " INTEGER DEFAULT 0 ,"
            + TableDeviceLockScreenMetaData.DEVICE_THEME_IMAGE_POS_LOCK_SCREEN + " integer)";

    private Context mContext;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_APP_TABLE);
        db.execSQL(CREATE_APPS_AUTHORIZED_TABLE);
        db.execSQL(CREATE_APP_LOCK_SCREEN_TABLE);
        db.execSQL(CREATE_DEVICE_LOCK_SCREEN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TableUserMetaData.TABLE_NAME_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TableAppMetaData.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TableAppsAuthorizedMetaData.TABLE_NAME_APPS_AUTHORIZED);
        db.execSQL("DROP TABLE IF EXISTS " + TableAppLockScreenMetaData.TABLE_APP_LOCK_SCREEN);
        db.execSQL("DROP TABLE IF EXISTS " + TableDeviceLockScreenMetaData.TABLE_DEVICE_LOCK_SCREEN);
        onCreate(db);
    }

    /**
     * Class to hold the meta data for User table.
     */
    public static final class TableUserMetaData implements BaseColumns {

        public static final String TABLE_NAME_USER = "user";
        public static final String U_ID = "t_id";
        public static final String U_NAME = "t_name";
        public static final String U_USER_NAME = "t_user_name";
        public static final String U_PASS = "t_pass";
        public static final String U_SEC_Q = "t_sec_q";
        public static final String U_SEC_A = "t_sec_a";
        public static final String U_SEC_Q2 = "t_sec_q2";
        public static final String U_SEC_A2 = "t_sec_a2";
        public static final String U_UNIQUE_CODE = "u_code";
    }

    /**
     * Class to hold the meta data for App table.
     */
    public static final class TableAppMetaData implements BaseColumns {

        public static final String TABLE_NAME = "app";
        public static final String UID = "u_id";
        public static final String ID = "a_id";
        public static final String NAME = "a_name";
        public static final String PASS = "a_pass";
        public static final String IMAGE = "a_image";
        public static final String APP_PRESENT = "app_present";
        public static final String APP_SECTION = "app_section";
        public static final String APP_LOCK_TYPE = "app_lock_type";
        public static final String APPS_AUTHORIZED_APP_PACK_NAME = "t_app_pack_name";
    }


    /**
     * Class to hold the meta data for Apps Authorized table.
     */
    public static final class TableAppsAuthorizedMetaData implements BaseColumns {

        public static final String TABLE_NAME_APPS_AUTHORIZED = "appsAlreadyAuthorised";
        public static final String APPS_AUTHORIZED_ID = "t_id";
        public static final String APPS_AUTHORIZED_APP_NAME = "t_app_name";
        public static final String APPS_AUTHORIZED_APP_PACK_NAME = "t_app_pack_name";
        public static final String APPS_AUTHORIZED_IS_APP_ALREADY_AUTHORISED = "is_app_already_authorised";
        public static final String APPS_AUTHORIZED_OPEN_THROUGH_SECURE_APP = "is_app_open_through_secure_app";
    }

    /**
     * Class to hold the meta data for Apps Lock Screen table.
     */
    public static final class TableAppLockScreenMetaData implements BaseColumns {

        public static final String TABLE_APP_LOCK_SCREEN = "app_lock_details";
        public static final String APP_THEME_LOCK_SCREEN_ID = "app_lock_id";
        public static final String APP_THEME_IMAGE_POS_LOCK_SCREEN = "app_lock_theme_pos";
    }

    /**
     * Class to hold the meta data for Apps Lock Screen table.
     */
    public static final class TableDeviceLockScreenMetaData implements BaseColumns {

        public static final String TABLE_DEVICE_LOCK_SCREEN = "device_lock_details";
        public static final String DEVICE_LOCK_SCREEN_ID = "device_lock_id";
        public static final String DEVICE_LOCK_TYPE = "device_lock_type";
        public static final String DEVICE_LOCK_PASSWORD = "device_lock_password";
        public static final String IS_DEVICE_LOCK_ENABLED = "device_lock_enabled";
        public static final String DEVICE_THEME_IMAGE_POS_LOCK_SCREEN = "device_lock_theme_pos";
    }
}