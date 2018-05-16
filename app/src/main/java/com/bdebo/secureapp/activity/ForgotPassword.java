package com.bdebo.secureapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bdebo.secureapp.R;
import com.bdebo.secureapp.model.User;
import com.bdebo.secureapp.util.AnimUtil;
import com.bdebo.secureapp.util.SecureUtil;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class is used to set the new password for SecureApp
 */
public class ForgotPassword extends AppCompatActivity {

    private static final String TAG = ForgotPassword.class.getSimpleName();
    private Button btnSecQuestions,btnUniqueCode, btnResetPassword;
    private TextView txtSecurityQuestion1,txtSecurityQuestion2;
    private EditText edtSecQuestion1,edtSecQuestion2,edtUniqueCode,edtPasswordReset,edtConfirmPasswordReset;
    private RelativeLayout layoutSecQuestions,layoutUniqueCode,layoutResetPassword,layoutResetPasswordHeader;
    private RadioGroup radioGroup;
    private Toolbar toolbar;
    private ArrayList<User> users;
    private User u;
    private SecureUtil util;
    private LinearLayout layoutParent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.forgot_password);
        init();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.label_forgot_password);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AnimUtil.setFadeAnimation(this);
        }

        layoutParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SecureUtil.hideSoftKeyboard(ForgotPassword.this);
            }
        });

        SQLiteDatabase.loadLibs(this);
        users=util.getAllUsers();
        Iterator<User> itr=users.iterator();
        while(itr.hasNext()) {
            u = itr.next();
        }
    }


    /**
     * Initializes the views
     */
    private void init(){
        layoutParent = (LinearLayout) findViewById(R.id.layout_parent);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroupForgotPassword);
        layoutResetPasswordHeader = (RelativeLayout) findViewById(R.id.layoutResetPasswordHeader);
        layoutSecQuestions = (RelativeLayout) findViewById(R.id.layoutSecurityQuestions);
        layoutUniqueCode = (RelativeLayout) findViewById(R.id.layoutUniqueCode);
        layoutResetPassword = (RelativeLayout) findViewById(R.id.layoutResetPassword);
        txtSecurityQuestion1 = (TextView) findViewById(R.id.txtSecurityQuestion1);
        txtSecurityQuestion2 = (TextView) findViewById(R.id.txtSecurityQuestion2);
        edtSecQuestion1 = (EditText) findViewById(R.id.editSecurityQuestion1ForgotPass);
        edtSecQuestion2 = (EditText) findViewById(R.id.editSecurityQuestion2ForgotPass);
        edtUniqueCode = (EditText) findViewById(R.id.editUniqueCodeForgotPassword);
        edtPasswordReset = (EditText) findViewById(R.id.editPasswordReset);
        edtConfirmPasswordReset = (EditText) findViewById(R.id.editConfirmPasswordReset);
        btnSecQuestions = (Button) findViewById(R.id.btnSecurityQuestionsForgotPassword);
        btnUniqueCode = (Button) findViewById(R.id.btnUniqueCodeForgotPassword);
        btnResetPassword = (Button) findViewById(R.id.btnResetPassword);
        util = new SecureUtil(this);
    }

    /**
     * This method is used to perform the action based on the radio button selected
     * @param view
     */
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioUniqueCode:
                if (checked)
                    // Unique Code based recovery
                    layoutSecQuestions.setVisibility(View.GONE);
                    layoutResetPassword.setVisibility(View.GONE);
                    layoutUniqueCode.setVisibility(View.VISIBLE);
                    btnUniqueCode.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(edtUniqueCode.getText().length() == 4) {
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(edtUniqueCode.getWindowToken(), 0);
                                verifyUniqueCode();
                            }
                            else{
                                edtUniqueCode.setError("Unique code is 4-digit number");
                            }
                        }
                    });

                    break;
            case R.id.radioSecurityQuestions:
                if (checked)
                    // Security Questions based recovery
                    layoutUniqueCode.setVisibility(View.GONE);
                    layoutResetPassword.setVisibility(View.GONE);
                    layoutSecQuestions.setVisibility(View.VISIBLE);
                    verifySecurityQuestions();
                    break;
        }
    }

    /**
     * This method is used to confirm and reset the password for SecureApp
     */
    private void confirmAndResetPassword(){
        String password = edtPasswordReset.getText().toString();
        String confirmPassword = edtConfirmPasswordReset.getText().toString();

        if(password.equals(confirmPassword)){
            u.setPass(password);
            util.updateUserDetails(u,u.getId());
            callToastMessage("Password changed successfully");
            Intent intent=new Intent(this,Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else{
            edtPasswordReset.setText("");
            edtConfirmPasswordReset.setText("");
            edtPasswordReset.setError("Password and Confirm password should be same");
        }
    }

    /**
     * Display Toast message
     * @param msg
     */
    private void callToastMessage(String msg){
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_layout));
        SecureUtil.setToastMessage(this,msg,toastLayout);
    }

    /**
     * This method is used to verify security questions of the SecureApp
     */
    private void verifySecurityQuestions(){
        txtSecurityQuestion1.setText(u.getSec_q1());
        txtSecurityQuestion2.setText(u.getSec_q2());
        btnSecQuestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtUniqueCode.getWindowToken(), 0);

                String secAnswer1 = edtSecQuestion1.getText().toString();
                String secAnswer2 = edtSecQuestion2.getText().toString();

                if(secAnswer1.equals(u.getSec_ans1()) && secAnswer2.equals(u.getSec_ans2())){
                    showResetPasswordLayout();
                }
                else if(!secAnswer1.equals(u.getSec_ans1()))
                    edtSecQuestion1.setError("Wrong Answer");
                else if(!secAnswer2.equals(u.getSec_ans2()))
                    edtSecQuestion2.setError("Wrong Answer");
            }
        });

    }

    /**
     * This method is used to verify unique code of the SecureApp
     */
    private void verifyUniqueCode(){
        int uniqueCode = Integer.parseInt(edtUniqueCode.getText().toString());
        if(u.getUniqueCode() == uniqueCode){
            showResetPasswordLayout();
        }
        else {
            edtUniqueCode.setText("");
            edtUniqueCode.setError("Incorrect unique code");
        }
    }

    /**
     * This method is used to reset the password
     */
    private void showResetPasswordLayout(){
        layoutUniqueCode.setVisibility(View.GONE);
        layoutResetPasswordHeader.setVisibility(View.GONE);
        layoutSecQuestions.setVisibility(View.GONE);
        layoutResetPassword.setVisibility(View.VISIBLE);
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmAndResetPassword();
            }
        });
    }
}
