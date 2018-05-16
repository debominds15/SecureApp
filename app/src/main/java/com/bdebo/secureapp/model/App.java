package com.bdebo.secureapp.model;

/**
 * This model class is used for App details like name,
 * password, uid, section, lock type.
 */
public class App implements Item{

    /**
     * Holds id of the app
     */
    private int id;

    /**
     * Holds id of the user
     */
    private int uid;

    /**
     * Holds the value of the flag
     * whether app is installed in the device or not
     */
    private boolean appPresent;

    /**
     * Holds the app details info like name, pass, lock type, section
     */
    private String appName,appPackName,pass,appSection,appLockType;

    public App(){}

    /**
     * Holds the section type whether Header section or Item section
     * @return
     */
    @Override
    public boolean isSection() {
        return false;
    }

    /**
     * Holds the section type of the app
     * @return
     */
    public String getAppSection() {
        return appSection;
    }

    /**
     * Holds the lock type of the app
     * @return
     */
    public String getAppLockType() {
        return appLockType;
    }

    /**
     * Set the lock type of the app
     */
    public void setAppLockType(String appLockType) {
        this.appLockType = appLockType;
    }

    /**
     * Set the section type of the app
     */
    public void setAppSection(String appSection) {
        this.appSection = appSection;
    }

    public App(int id, int uid, String name, String pass, boolean appPresent, String appSection,String appLockType,String appPackName) {
        this.id = id;
        this.uid = uid;
        this.appName = name;
        this.pass = pass;
        this.appPresent=appPresent;
        this.appSection=appSection;
        this.appLockType=appLockType;
        this.appPackName = appPackName;
    }

    /**
     * Holds the user id
     * @return
     */
    public int getUid() {
        return uid;
    }

    /**
     * Set the user id
     */
    public void setUid(int uid) {
        this.uid = uid;
    }

    /**
     * Holds the id of the app
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * Set the id of the app
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Holds the package name of the app
     * @return
     */
    public String getAppPackName() {
        return appPackName;
    }

    /**
     * Set the package name of the app
     */
    public void setAppPackName(String appPackName) {
        this.appPackName = appPackName;
    }

    /**
     * Holds the name of the app
     * @return
     */
    public String getName() {
        return appName;
    }

    /**
     * Set the package name of the app
     */
    public void setName(String name) {
        this.appName = name;
    }
    /**
     * Holds the password of the app
     * @return
     */

    public String getPass() {
        return pass;
    }

    /**
     * Set the password of the app
     */
    public void setPass(String pass) {
        this.pass = pass;
    }

    /**
     * Holds the flag whether the app is installed in the device or not
     * @return
     */
    public boolean isAppPresent() {
        return appPresent;
    }

    /**
     * Set the flag whether the app is installed in the device or not
     */
    public void setAppPresent(boolean appPresent) {
        this.appPresent = appPresent;
    }
}


