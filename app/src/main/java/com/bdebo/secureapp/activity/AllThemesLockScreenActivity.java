package com.bdebo.secureapp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.bdebo.secureapp.R;
import com.bdebo.secureapp.SecureAppApplication;
import com.bdebo.secureapp.adapter.ThemesAdapter;
import com.bdebo.secureapp.model.AppLockScreen;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;

import java.util.ArrayList;
import java.util.List;

/**
 *This class is used to display list of themes for selecting a particular theme for Lock screen.
 */
public class AllThemesLockScreenActivity extends Activity {
    private static String TAG = AllThemesLockScreenActivity.class.getSimpleName();
    GridView gv;
    Context context;
    public static List<Drawable> prgmImages;
    private static int ALL_THEME_IMAGES=1004;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prgmImages=new ArrayList<Drawable>();

        setContentView(R.layout.themes_select);
        gv=(GridView) findViewById(R.id.gridThemesImages);

        addThemeImages(R.drawable.my_wall);
        addThemeImages(R.drawable.my_wall_1);
        addThemeImages(R.drawable.my_wall2);
        addThemeImages(R.drawable.my_wall3);
        addThemeImages(R.drawable.my_wall4);
        addThemeImages(R.drawable.my_wall5);
        addThemeImages(R.drawable.my_wall6);
        addThemeImages(R.drawable.my_wall7);
        addThemeImages(R.drawable.my_wall8);
        addThemeImages(R.drawable.my_wall9);
        addThemeImages(R.drawable.my_wall10);
        addThemeImages(R.drawable.my_wall11);
        addThemeImages(R.drawable.my_wall12);
        addThemeImages(R.drawable.my_wall13);
        addThemeImages(R.drawable.my_wall14);
        addThemeImages(R.drawable.my_wall15);
        addThemeImages(R.drawable.my_wall16);
        addThemeImages(R.drawable.my_wall17);
        addThemeImages(R.drawable.my_wall18);
        addThemeImages(R.drawable.my_wall19);
        addThemeImages(R.drawable.my_wall20);
        addThemeImages(R.drawable.my_wall21);
        addThemeImages(R.drawable.my_wall22);

        gv.setAdapter(new ThemesAdapter(this,prgmImages));

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                    Intent intent = new Intent();
                    intent.putExtra(AppConstant.IMAGE_NO, position);
                    setResult(ALL_THEME_IMAGES, intent);
                    finish();//finishing activity

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * Add themes in list
     * @param id
     */
    private void addThemeImages(int id){
        Drawable drawable = getResources().getDrawable(id);
        prgmImages.add(drawable);
    }

    @Override
    public void onStop() {
        super.onStop();
        Intent intent = getIntent();
        boolean isDeviceThemeToBeSet = intent.getBooleanExtra(AppConstant.IS_DEVICE_THEME_TO_BE_SET,false);
        if(!SecureAppApplication.isApplicationVisible() && isDeviceThemeToBeSet){
            finish();
            SecureAppApplication.logout(this);
        }
    }
}
