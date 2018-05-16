package com.bdebo.secureapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bdebo.secureapp.R;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;

/**
 * This class is used to authenticate App Lock using SecureApp password
 */
public class ForgotPasswordLockActivity extends Activity {
    private static String TAG = ForgotPasswordLockActivity.class.getSimpleName();
    String securePass;
    Button submitForgotPasswordLock,cancelForgotPasswordLock;
    EditText editSecureAppPasswordLock;
    private int LOCK_SECUREAPP_VERIFICATION = 2001;
    private boolean isCorrect = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_pattern_lock_menu);
        submitForgotPasswordLock = (Button) findViewById(R.id.btnSubmitForgotPasswordLock);
        cancelForgotPasswordLock = (Button) findViewById(R.id.btnForgotPasswordLockCancel);
        editSecureAppPasswordLock = (EditText) findViewById(R.id.edtSecureAppPasswordLock);

        if(editSecureAppPasswordLock.getText().toString().equals(securePass)){
            isCorrect = true;
        }

        cancelForgotPasswordLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent();
                myIntent.putExtra(AppConstant.IS_CANCEL, true);
                setResult(LOCK_SECUREAPP_VERIFICATION, myIntent);
                finish();
            }
        });
        submitForgotPasswordLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editSecureAppPasswordLock.getText().toString().equals(""))
                    editSecureAppPasswordLock.setError("Password cannot be empty");

                else {
                    SecureUtil util = new SecureUtil(ForgotPasswordLockActivity.this);
                    String pass = util.getAppPass(ForgotPasswordLockActivity.this);
                    if(editSecureAppPasswordLock.getText().toString().equals(pass)){
                        isCorrect = true;
                    }
                    Intent myIntent = new Intent();
                    myIntent.putExtra(AppConstant.IS_SECURE_APP_PASS_CORRECT, isCorrect);
                    setResult(LOCK_SECUREAPP_VERIFICATION, myIntent);
                    finish();
                }
            }
        });

    }
}
