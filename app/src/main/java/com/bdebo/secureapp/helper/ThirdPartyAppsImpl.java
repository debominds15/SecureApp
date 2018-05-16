package com.bdebo.secureapp.helper;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to show the third party apps installed in the device
 * It binds to the adapter to show all the apps in GridView
 */
public class ThirdPartyAppsImpl implements ThirdPartyApps {

    private static String TAG = ThirdPartyAppsImpl.class.getSimpleName();
    private static List<ApplicationInfo> appsInfo;
    @Override
    public List<ApplicationInfo> getAllThirdPartyApps(Context context) {

        appsInfo=new ArrayList<ApplicationInfo>();


        final List<ApplicationInfo> apps = context.getPackageManager().getInstalledApplications(0);
        for (int i=0;i<apps.size();i++) {

            if ((apps.get(i).flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                //System app
                try {

                    appsInfo.add(apps.get(i));

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            else{
                try {

                    final String appPackageName = apps.get(i).packageName;
                      if (appPackageName.equals("com.google.android.gm") || appPackageName.equals("com.android.gallery") || appPackageName.equals("com.android.camera") || appPackageName.equals("com.android.mms") || appPackageName.equals(" com.android.providers.telephony") || appPackageName.equals("com.android.providers.contacts")
                            || appPackageName.equals("com.android.dialer") || appPackageName.equals("com.google.android.apps.photos") || appPackageName.equals("com.google.android.apps.docs")
                            || appPackageName.equals("com.motorola.MotGallery2") || appPackageName.equals("com.motorola.camera") || appPackageName.equals("com.google.android.apps.messaging")) {

                        appsInfo.add(apps.get(i));
                    }
                }
                catch (Exception e){
                    Log.e(TAG,e.getLocalizedMessage());
                }
            }
        }
        return appsInfo;
    }
}
