package com.bdebo.secureapp.activity;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.model.User;
import com.bdebo.secureapp.model.AppLockScreen;
import com.bdebo.secureapp.util.SecureUtil;

import net.sqlcipher.database.SQLiteDatabase;

/**
 * This class is used to register the user
 */
public class Register extends Activity implements OnItemSelectedListener{

    private EditText editName,editUsername,editSecurityAnswer,editSecuritySecondAnswer,editPassword,editConfirmPassword,editUniqueCode;
    private Spinner secQuestion1,secQuestion2;
    private User user;
    private String item,item2;
    private Button save;
    private SharedPreferences signUpPrefs;
    private TextInputLayout layoutSecQuestion2;
    private static final String USERNAME_PATTERN = "^[a-z0-9_-]{4,15}$";
    //String strongPasswordRegex = "^(?=.*[A-Z].*[A-Z])(?=.*[!@#$&*])(?=.*[0-9].*[0-9])(?=.*[a-z].*[a-z].*[a-z]).{6,15}$";
    private String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[!@#$&*])(?=.*[0-9])(?=.*[a-z]).{6,15}$";
    private ArrayList<String> list1,list2;
    private SecureUtil util;
    private ProgressBar progressBarPassword;
    private RelativeLayout layoutPasswordStrength;
    private LinearLayout layout_parent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
        user=new User();
        SQLiteDatabase.loadLibs(this);
        signUpPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        layoutSecQuestion2.setVisibility(View.GONE);
        editSecurityAnswer.setVisibility(View.GONE);
        secQuestion1.setOnItemSelectedListener(this);
        secQuestion2.setOnItemSelectedListener(this);
        setListInAdapterSpinner();
        detectPasswordStrength();

        layout_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SecureUtil.hideSoftKeyboard(Register.this);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!checkAllViews()){
                    user.setName(editName.getText().toString());
                    user.setUsername(editUsername.getText().toString());
                    user.setPass(editPassword.getText().toString());
                    user.setSec_q1(item);
                    user.setSec_ans1(editSecurityAnswer.getText().toString());
                    user.setSec_q2(item2);
                    user.setSec_ans2(editSecuritySecondAnswer.getText().toString());
                    user.setUniqueCode(Integer.parseInt(editUniqueCode.getText().toString()));
                    util.insertRecords(user);
                    insertAppLockThemePosition();
                    SharedPreferences.Editor editor = signUpPrefs.edit();
                    editor.putBoolean(AppConstant.signUpDonePref, true);
                    editor.commit();

                    LayoutInflater inflater = getLayoutInflater();
                    View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_layout));
                    SecureUtil.setToastMessage(Register.this,"Register successfully",toastLayout);

                    finish();
                }
                else{
                    Log.d("Register","Error...");
                }
            }
        });

    }

    /**
     * This method is used to check all the views before registering
     * @return true: if no errors are observed
     * @return false: if some error is observed
     */
    private boolean checkAllViews(){
        boolean isErrorCheck = false;

        if(isErrorPresent(editName))
        isErrorCheck =true;
        return isErrorCheck;
    }

    /**
     * This method is used to check if any error is observed
     * @return true: if no errors are observed
     * @return false: if some error is observed
     */
    private boolean isErrorPresent(View view) {
        boolean isErrorCheck = false;
        int id = view.getId();
        int pos=0;
        switch (id){
            case R.id.edtName:
                if(editName.getText().toString().length() < 3) {
                    editName.setError("Name should be minimum of 3 characters");
                    isErrorCheck = true;
                    pos=1;
                    break;
                }


            case R.id.edtAddr:

                if(validateUserName(editUsername.getText().toString(),USERNAME_PATTERN) == false) {
                    editUsername.setError("Username should be alphanumeric with minimum of 4 characters");
                    isErrorCheck = true;
                    pos=2;
                    break;
                }

            case R.id.edtPassword:
                if(editPassword.getText().toString().length() < 3) {
                    editPassword.setError("Password should be minimum of 3 characters");
                    isErrorCheck = true;
                    pos=3;
                    break;
                }

            case R.id.edtConfirm:
                if(!editConfirmPassword.getText().toString().trim().equalsIgnoreCase(editPassword.getText().toString())) {
                    editConfirmPassword.setError("Password and Confirm Password should be same");
                    isErrorCheck = true;
                    pos=4;
                    break;
                }

            case R.id.edtUniqueCode:
                if(editUniqueCode.getText().toString().length()<4 || editUniqueCode.getText().toString().equals("")) {
                    editUniqueCode.setError("Unique code should be exactly 4-digit");
                    isErrorCheck = true;
                    pos=5;
                    break;
                }

            case R.id.spinnerSecQuestion1:
                Log.d("Register","item: "+secQuestion1.getSelectedItem().toString());
                if (secQuestion1.getSelectedItem().toString().equals("Select your first Security Question")){
                    ((TextView) secQuestion1.getSelectedView()).setError("None selected");
                    isErrorCheck = true;
                    pos=8;
                    break;
                }

            case R.id.edtSecAnswer1:
                if(editSecurityAnswer.getText().toString().length() < 3) {
                    editSecurityAnswer.setError("Security answer should be minimum of 3 characters");
                    isErrorCheck = true;
                    pos=6;
                    break;
                }

            case R.id.spinnerSecQuestion2:
                if(secQuestion2.getSelectedItem().toString().equals("Select your second Security Question")) {
                    ((TextView) secQuestion2.getSelectedView()).setError("None selected");
                    isErrorCheck = true;
                    pos=9;
                    break;
                }

            case R.id.edtSecAnswer2:
                if(editSecuritySecondAnswer.getText().toString().length() < 3) {
                    editSecuritySecondAnswer.setError("Security answer should be minimum of 3 characters");
                    isErrorCheck = true;
                    pos=7;
                    break;
                }
        }
        return isErrorCheck;
    }

    /**
     * Initializes the views
     */
    public void init()
    {
        layout_parent = (LinearLayout) findViewById(R.id.layout_parent);
        layoutSecQuestion2 = (TextInputLayout) findViewById(R.id.textInputLayoutSecurityAnswer2);
        editPassword=(EditText) findViewById(R.id.edtPassword);
        editConfirmPassword=(EditText) findViewById(R.id.edtConfirm);
        secQuestion1=(Spinner)findViewById(R.id.spinnerSecQuestion1);
        secQuestion2=(Spinner)findViewById(R.id.spinnerSecQuestion2);
        editUniqueCode=(EditText) findViewById(R.id.edtUniqueCode);
        save=(Button) findViewById(R.id.btnSave);
        editName=(EditText)findViewById(R.id.edtName);
        editUsername=(EditText) findViewById(R.id.edtAddr);
        editSecurityAnswer=(EditText) findViewById(R.id.edtSecAnswer1);
        editSecuritySecondAnswer=(EditText) findViewById(R.id.edtSecAnswer2);
        util = new SecureUtil(this);
        progressBarPassword = (ProgressBar) findViewById(R.id.progressPassword);
        layoutPasswordStrength = (RelativeLayout) findViewById(R.id.layoutPasswordStrength);
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                               long arg3) {
        // TODO Auto-generated method stub
        switch(arg0.getId()) {
            case R.id.spinnerSecQuestion1:
                if (arg2 != 0) {
                    item=arg0.getItemAtPosition(arg2).toString();
                    if(!item.trim().equalsIgnoreCase("Select")) {
                        editSecurityAnswer.setVisibility(View.VISIBLE);
                        secQuestion2.setVisibility(View.VISIBLE);
                    }
                    else {
                        editSecurityAnswer.setVisibility(View.GONE);
                        secQuestion2.setVisibility(View.GONE);
                    }
                }
                break;
            case R.id.spinnerSecQuestion2:
                if (arg2 != 0) {
                    item2=arg0.getItemAtPosition(arg2).toString();
                    if(!item.trim().equalsIgnoreCase("Select")) {
                        layoutSecQuestion2.setVisibility(View.VISIBLE);
                        editSecuritySecondAnswer.setVisibility(View.VISIBLE);
                    }
                    else {
                        layoutSecQuestion2.setVisibility(View.GONE);
                        editSecurityAnswer.setVisibility(View.GONE);
                    }
                }
                break;
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        hideKeyBoard();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    /**
     * Hide the keyboard
     */
    public void hideKeyBoard()
    {
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    /**
     * Set the app lock theme
     */
    private void insertAppLockThemePosition(){
        AppLockScreen appLockScreen = new AppLockScreen(1,0);
        appLockScreen.setId(1);
        appLockScreen.setPos(1);
        util.insertAppThemeImagePos(appLockScreen);
    }

    /**
     * Validate username with regular expression
     * @param username username for validation
     * @return true valid username, false invalid username
     */
    private boolean validateUserName(final String username,String regexPattern){

        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();

    }

    /**
     * Set the list in adapter for Spinner
     */
    private void setListInAdapterSpinner(){
        list1=new ArrayList<String>();
        list1.add("Select your first Security Question");
        list1.add("What was your first pet name ?");
        list1.add("What was your favourite teacher's first name ?");
        list1.add("What is your father's nick name ?");
        list1.add("Which was your first school ?");

        list2=new ArrayList<String>();
        list2.add("Select your second Security Question");
        list2.add("Who was your childhood sports hero ?");
        list2.add("What is your favourite sport ?");
        list2.add("What was your childhood nickname ?");
        list2.add("What is your mother's maiden name  ?");

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        secQuestion1.setAdapter(adapter);

        ArrayAdapter<String> adapter2=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list2);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        secQuestion2.setAdapter(adapter2);
    }

    private void detectPasswordStrength(){


        editPassword.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(editPassword.length() == 0){
                    progressBarPassword.setVisibility(View.GONE);
                    layoutPasswordStrength.setVisibility(View.GONE);
                }
                else if(validateUserName(editPassword.getText().toString(),USERNAME_PATTERN)) {
                    Log.d("Register","moderate password");
                    progressBarPassword.setVisibility(View.VISIBLE);
                    layoutPasswordStrength.setVisibility(View.VISIBLE);
                    progressBarPassword.setProgress(2);
                    progressBarPassword.getProgressDrawable().setColorFilter(getResources().getColor(android.R.color.holo_blue_dark), PorterDuff.Mode.SRC_IN);
                }
                else if(validateUserName(editPassword.getText().toString(),PASSWORD_REGEX)) {
                    Log.d("Register","strong password");
                    progressBarPassword.setVisibility(View.VISIBLE);
                    layoutPasswordStrength.setVisibility(View.VISIBLE);
                    progressBarPassword.setProgress(3);
                    progressBarPassword.getProgressDrawable().setColorFilter(getResources().getColor(android.R.color.holo_green_dark), PorterDuff.Mode.SRC_IN);
                }
                else{
                    Log.d("Register","weak password");
                    progressBarPassword.setVisibility(View.VISIBLE);
                    layoutPasswordStrength.setVisibility(View.VISIBLE);
                    progressBarPassword.setProgress(1);
                    progressBarPassword.getProgressDrawable().setColorFilter(getResources().getColor(android.R.color.holo_red_dark), PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}
