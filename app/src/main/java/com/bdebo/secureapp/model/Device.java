package com.bdebo.secureapp.model;

/**
 * This model class is used to hold the device lock details
 */

public class Device {

    /**
     * Holds the id
     */
    private int id;
    /**
     * Holds the device lock screen password
     */
    private String password;
    /**
     * Holds the lock type of the device
     */
    private String lockType;
    /**
     * Holds the flag whether the device lock is enabled or not
     */
    private boolean isLockEnabled;

    /**
     * Holds the image theme position of the lock screen
     */
    private int imagePosition;

    /**
     * Holds the device lock screen password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Holds the id
     */
    public int getId() {
        return id;
    }

    /**
     * Set the id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Holds the flag whether the device lock is enabled or not
     * @return true if the device lock is enabled
     * @return false, if not
     */
    public boolean isLockEnabled() {
        return isLockEnabled;
    }

    /**
     * Set the flag whether the device lock is enabled or not
     */
    public void setLockEnabled(boolean lockEnabled) {
        isLockEnabled = lockEnabled;
    }

    /**
     * Set the device lock screen password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Holds the lock type of the device
     * @return lock type
     */
    public String getLockType() {
        return lockType;
    }

    /**
     * Set the lock type of the device
     */
    public void setLockType(String lockType) {
        this.lockType = lockType;
    }

    /**
     * Holds the image theme position of the lock screen
     * @return position
     */
    public int getImagePosition() {
        return imagePosition;
    }

    /**
     * Set the image theme position of the lock screen
     */
    public void setImagePosition(int imagePosition) {
        this.imagePosition = imagePosition;
    }

    public Device(int id, String password, String lockType, boolean isLockEnabled, int imagePosition) {
        this.id = id;
        this.password = password;
        this.lockType = lockType;
        this.isLockEnabled = isLockEnabled;
        this.imagePosition = imagePosition;
    }

    public Device() {
    }
}
