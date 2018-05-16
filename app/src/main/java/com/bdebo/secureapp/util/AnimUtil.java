package com.bdebo.secureapp.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.WindowManager;

import com.bdebo.secureapp.R;

/**
 * Created by M1032607 on 1/4/2018.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AnimUtil {


    public static void setSlideAnimation(Activity context){
        Transition enterTansition = TransitionInflater.from(context).inflateTransition(R.transition.slide);
        context.getWindow().setEnterTransition(enterTansition);
        context.getWindow().setAllowEnterTransitionOverlap(false);
    }

    public static void setFadeAnimation(Activity context){
        Transition enterTansition = TransitionInflater.from(context).inflateTransition(R.transition.fade);
        context.getWindow().setEnterTransition(enterTansition);
        context.getWindow().setAllowEnterTransitionOverlap(false);
    }

    public static void setExplodeAnimation(Activity context){
        Transition enterTansition = TransitionInflater.from(context).inflateTransition(R.transition.explode);
        context.getWindow().setEnterTransition(enterTansition);
        context.getWindow().setAllowEnterTransitionOverlap(false);
    }
}
