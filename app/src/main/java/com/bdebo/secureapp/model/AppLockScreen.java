package com.bdebo.secureapp.model;

/**
 * Created by Debojyoti on 15-04-2017.
 */
public class AppLockScreen {
    int id;
    int pos;

    public AppLockScreen() {
    }

    public AppLockScreen(int id, int pos) {
        this.id = id;
        this.pos = pos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
