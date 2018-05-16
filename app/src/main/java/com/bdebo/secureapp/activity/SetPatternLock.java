package com.bdebo.secureapp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.amnix.materiallockview.MaterialLockView;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.SecureAppApplication;
import com.bdebo.secureapp.util.AppConstant;

import java.util.List;

/**
 * This class is used to set Pattern Lock Screen for App/Device
 */
public class SetPatternLock extends ApplicationActivity {
    private static String TAG = SetPatternLock.class.getSimpleName();
    private TextView patternHeader;
    private MaterialLockView materialLockView;
    private Toolbar toolbar;
    private LinearLayout layoutPatternButtons;
    private Button clearPattern,continuePattern,cancelPattern;
    private final Handler handler = new Handler();
    private long waitTimeMillis=1500;
    private String patternData1,patternData2;
    private int cnt=0;
    private int LOCK_TYPE_CODE=1001;
    private boolean isCorrectPattern = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pattern_lock);
        init();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        materialLockView.setOnPatternListener(new MaterialLockView.OnPatternListener() {
            @Override
            public void onPatternDetected(List<MaterialLockView.Cell> pattern, String SimplePattern) {

                    cancelPattern.setVisibility(View.GONE);
                    layoutPatternButtons.setVisibility(View.VISIBLE);
                    int noOfDotsConnected = SimplePattern.length();
                    if (noOfDotsConnected < 4) {
                        materialLockView.setDisplayMode(com.amnix.materiallockview.MaterialLockView.DisplayMode.Wrong);
                        continuePattern.setEnabled(false);
                        cancelPattern.setVisibility(View.VISIBLE);
                        clearPattern.setVisibility(View.GONE);
                        patternHeader.setTextColor(Color.RED);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Do something after 100ms
                                patternHeader.setText("Connect at least 4 dots. Try again.");
                                materialLockView.clearPattern(); // clear the drawn pattern

                            }
                        }, waitTimeMillis);

                    } else {
                        cnt++;
                        continuePattern.setEnabled(true);
                        clearPattern.setVisibility(View.VISIBLE);
                        patternHeader.setTextColor(getResources().getColor(R.color.colorSecondaryText));

                          if (cnt == 1) {
                            materialLockView.setDisplayMode(com.amnix.materiallockview.MaterialLockView.DisplayMode.Correct);
                            patternData1 = SimplePattern;

                        } else {
                            patternData2 = SimplePattern;
                            if (patternData1.equals(patternData2)) {
                                isCorrectPattern = true;
                                patternHeader.setText("Pattern Recorded");
                            } else {
                                patternHeader.setText("Try again");
                                patternHeader.setTextColor(Color.RED);
                                materialLockView.setDisplayMode(MaterialLockView.DisplayMode.Wrong);
                            }


                        }

                    }
                super.onPatternDetected(pattern, SimplePattern);
            }
        });

        continuePattern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearPattern.setVisibility(View.GONE);
                cancelPattern.setVisibility(View.VISIBLE);

                    if(cnt > 1 && isCorrectPattern) {
                        Intent intent = new Intent(SetPatternLock.this, SelectLockPattern.class);
                        if (patternData1 != null)
                            intent.putExtra(AppConstant.LOCK_DATA, patternData1);
                        setResult(LOCK_TYPE_CODE, intent);
                        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                        finish();
                    }
                    else {
                        //materialLockView.clearPattern();
                        patternHeader.setText("Confirm Pattern");
                        continuePattern.setText("Confirm");
                        patternHeader.setTextColor(getResources().getColor(R.color.colorSecondaryText));
                        materialLockView.clearPattern();
                        continuePattern.setEnabled(false);
                        cancelPattern.setVisibility(View.VISIBLE);
                    }

            }
        });

        clearPattern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialLockView.clearPattern();
                if(cnt > 1){
                    patternHeader.setText("Confirm Pattern");
                }
                else {
                    patternHeader.setText("Draw an unlock pattern");
                }
                patternHeader.setTextColor(getResources().getColor(R.color.colorSecondaryText));
                cancelPattern.setVisibility(View.VISIBLE);
                clearPattern.setVisibility(View.GONE);
            }
        });

        cancelPattern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                finish();
            }
        });

    }

    /**
     * Initializes the views
     */
    private void init(){
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        cancelPattern=(Button) this.findViewById(R.id.btnCancelPattern);
        clearPattern=(Button) this.findViewById(R.id.btnClearAppPattern);
        continuePattern=(Button) this.findViewById(R.id.btnAppPatternContinue);
        materialLockView = (MaterialLockView) findViewById(R.id.pattern);
        patternHeader=(TextView) this.findViewById(R.id.txtPatternHeader);
        layoutPatternButtons=(LinearLayout) this.findViewById(R.id.layoutBtnsInPatternBottom);
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
