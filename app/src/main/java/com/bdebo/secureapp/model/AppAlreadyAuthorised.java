package com.bdebo.secureapp.model;

/**
 * This model class is used to hold about the app being authorised or not
 * through lock screen
 */
public class AppAlreadyAuthorised {
    /**
     * Holds the id of the app
     */
    private int id;
    /**
     * Holds the name of the app
     */
    private String appName;

    /**
     * Holds the name of the app
     */
    private String appPackName;
    /**
     * Holds the flag of the app has been authorised or not
     */
    private boolean isAppAuthorised;

    /**
     * Holds the flag whether the app is opened through SecureApp or not
     */
    boolean isAppOpenThroughSecureApp;

    public AppAlreadyAuthorised() {
    }

    /**
     * Holds the name of the app
     * @return app name
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Holds the id of the app
     * @return id
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
     * Set the name of the app
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * Holds the flag of the app has been authorised or not
     * @return true if app is already authorised through lock screen
     * @return false if not
     */
    public boolean isAppAuthorised() {
        return isAppAuthorised;
    }


    public AppAlreadyAuthorised(int id, String appName,String appPackName, boolean isAppAuthorised, boolean isAppOpenThroughSecureApp) {
        this.id = id;
        this.appName = appName;
        this.appPackName = appPackName;
        this.isAppAuthorised = isAppAuthorised;
        this.isAppOpenThroughSecureApp = isAppOpenThroughSecureApp;
    }

    /**
     * Set the flag of the app has been authorised or not
     */
    public void setIsAppAuthorised(boolean isAppAuthorised) {
        this.isAppAuthorised = isAppAuthorised;
    }

    /**
     * Holds the flag whether the app is opened through SecureApp or not
     * @return true if opened through SecureApp
     * @return false if not
     */
    public boolean isAppOpenThroughSecureApp() {
        return isAppOpenThroughSecureApp;
    }

    /**
     * Set the flag whether the app is opened through SecureApp or not
     */
    public void setAppOpenThroughSecureApp(boolean appOpenThroughSecureApp) {
        isAppOpenThroughSecureApp = appOpenThroughSecureApp;
    }

    /**
     * Holds the flag whether the app is opened through SecureApp or not
     * @return app package name
     */
    public String getAppPackName() {
        return appPackName;
    }
    /**
     * Set the app package name
     */
    public void setAppPackName(String appPackName) {
        this.appPackName = appPackName;
    }
}
