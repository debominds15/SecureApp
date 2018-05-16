package com.bdebo.secureapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bdebo.secureapp.R;
import com.bdebo.secureapp.SecureAppApplication;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;


/**
 * This class is used to set Pin/Password Lock Screen for App/Device
 */
public class SetPINPasswordLock extends ApplicationActivity {

    String TAG=SetPINPasswordLock.class.getSimpleName();
    Button cancelPIN,continuePIN;
    EditText edtAppPIN;
    String appLockType ="";
    TextView appPINHeader;
    Toolbar toolbar;
    int cnt=0;
    int LOCK_TYPE_CODE=1001;
    String password;
    TextView txtWrongPinPasswordLock;
    private RelativeLayout layout_parent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_pin_password_activity);
        init();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        continuePIN.setEnabled(false);
        final Intent intent=getIntent();
        if(intent!=null) {
            appLockType = intent.getStringExtra(AppConstant.LOCK_TYPE);
        }

        if(appLockType.equals("Password")) {
            appPINHeader.setText("Password must be at least 4 characters");
            edtAppPIN.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
            edtAppPIN.setSelection(edtAppPIN.getText().length());
        }
        edtAppPIN.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(edtAppPIN.getText().toString().length()>=4) {
                    continuePIN.setEnabled(true);
                }
                else {
                    continuePIN.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        continuePIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cnt++;
                if(cnt > 1) {
                    if(password.equals(edtAppPIN.getText().toString())) {
                        passIntent(edtAppPIN.getText().toString());
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(edtAppPIN.getWindowToken(), 0);
                        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                        finish();
                    }
                    else{
                        txtWrongPinPasswordLock.setVisibility(View.VISIBLE);
                        edtAppPIN.setText("");
                        txtWrongPinPasswordLock.setText("Password should be same. Please try again");
                    }
                }
                else{
                    password = edtAppPIN.getText().toString();
                    edtAppPIN.setText("");
                }

            }
        });

        layout_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SecureUtil.hideSoftKeyboard(SetPINPasswordLock.this);
            }
        });
        cancelPIN.setOnClickListener(new View.OnClickListener() {
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
        layout_parent=(RelativeLayout) this.findViewById(R.id.layout_parent);
        cancelPIN=(Button) this.findViewById(R.id.btnCancelAppPIN);
        continuePIN=(Button) this.findViewById(R.id.btnAppPINContinue);
        edtAppPIN=(EditText) this.findViewById(R.id.edtAppPIN);
        appPINHeader=(TextView) this.findViewById(R.id.txtPinPatternHeader);
        txtWrongPinPasswordLock = (TextView) findViewById(R.id.txtWrongPinPasswordLock);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
    }

    /**
     * Set the lock details and pass to the previous activity
     * @param data
     */
    private void passIntent(String data)
    {
        Intent setIntent=new Intent();
        setIntent.putExtra(AppConstant.LOCK_DATA,data);
        setResult(LOCK_TYPE_CODE,setIntent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
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
