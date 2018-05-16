package com.bdebo.secureapp.helper;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.util.List;

/**
 * Interface to be used while showing third party apps installed in the device.
 */
public interface ThirdPartyApps {

    List<ApplicationInfo> getAllThirdPartyApps(Context context);
}
