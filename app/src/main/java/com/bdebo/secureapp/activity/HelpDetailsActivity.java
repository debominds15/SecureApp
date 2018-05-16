package com.bdebo.secureapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.ViewFlipper;

import com.bdebo.secureapp.R;
import com.bdebo.secureapp.SecureAppApplication;
import com.bdebo.secureapp.util.AppConstant;

/**
 * This class is used to show the individual item from Help Screen.
 */
public class HelpDetailsActivity extends ApplicationActivity {

    private String TAG = HelpDetailsActivity.class.getSimpleName();
    private ViewFlipper flip;
    private float initialX;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int position = 0;
        if(intent != null){
            position = intent.getIntExtra(AppConstant.HELP_ITEM_POSITION,0);
        }
        setLayoutView(position);
    }

    /**
     * This method is used to set the layout view as per the item.
     * @param position
     */
    private void setLayoutView(int position){
        switch (position){
            case 0:
                setContentView(R.layout.help_item_app_lock);
                flipAction();
                break;
            case 1:
                setContentView(R.layout.help_item_remove_lock);
                flipAction();
                break;
            case 2:
                setContentView(R.layout.help_item_device_lock);
                flipAction();
                break;
            case 3:
                setContentView(R.layout.help_item_app_not_present);
                flipAction();
                break;
            case 4:
                setContentView(R.layout.help_item_open_app_securely);
                flipAction();
                break;
            case 5:
                setContentView(R.layout.help_item_check_app_password);
                flipAction();
                break;
            case 6:
                setContentView(R.layout.help_item_check_device_password);
                flipAction();
                break;
            case 7:
                setContentView(R.layout.help_item_advance_security);
                flipAction();
                break;
            case 8:
                setContentView(R.layout.help_item_edit_user_details);
                flipAction();
                break;
            case 9:
                setContentView(R.layout.help_item_set_profile_image);
                flipAction();
                break;
            case 10:
                setContentView(R.layout.help_item_customize_app_theme);
                flipAction();
                break;
            case 11:
                setContentView(R.layout.help_item_invisble_pattern);
                flipAction();
                break;
            case 12:
                setContentView(R.layout.help_item_not_present);
                flipAction();
                break;
        }
    }

    /**
     * Implementing touch event for view flipper
     */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

// Getting intitial by event action down
                initialX = event.getX();
                break;

            case MotionEvent.ACTION_UP:

// On action up the flipper will start and showing next item
                float finalX = event.getX();
                if (initialX > finalX) {

// Show items are 4
                    if (flip.getDisplayedChild() == 4)
                        break;

// Flip show next will show next item
                    flip.showNext();
                } else {

// If flip has no items more then it will display previous item
                    if (flip.getDisplayedChild() == 0)
                        break;
                    flip.showPrevious();
                }
                break;
        }
        return false;
    }

    /**
     * This method is used to set the animation for the flip.
     */
    private void flipAction(){
        flip = (ViewFlipper) findViewById(R.id.viewFlipper1);

// Setting IN and OUT animation for view flipper
        flip.setInAnimation(this, R.anim.right_enter);
        flip.setOutAnimation(this, R.anim.left_out);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(!SecureAppApplication.isApplicationVisible()){
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            finish();
            SecureAppApplication.logout(this);
        }
    }
}
