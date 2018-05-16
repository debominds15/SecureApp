package com.bdebo.secureapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amnix.materiallockview.MaterialLockView;
import com.bdebo.secureapp.provider.DBHelper;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.SecureAppApplication;
import com.bdebo.secureapp.model.User;
import com.bdebo.secureapp.model.App;
import com.bdebo.secureapp.model.Device;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;
import com.google.gson.Gson;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class is used to check the password set for App or Device
 */
public class CheckAppPassword extends AppCompatActivity {
    DBHelper helper;
    private static final String TAG = CheckAppPassword.class.getSimpleName();
    TextView txtShowAppNamePassword,txtShowAppNamePatternPassword;
    RelativeLayout layoutSecureAppPassword,layoutUniqueCode,layoutOuterShowTextPassword,layoutOuterShowPatternPassword,layoutPasswordAuthenticationHeader;
    RadioGroup radioGroup;
    Toolbar toolbar;
    private TextInputLayout textInputShowPassword;
    private EditText editShowAppPasswordText,editUniqueCodeForgotPassword,editSecureAppPassword;
    private Button btnUniqueCodeAppPasswordVerification,btnSecureAppPasswordVerification;
    private ImageView imageAppShowPassword,imageAppShowPatternPassword;
    private ArrayList<App> apps;
    private ArrayList<User> users;
    private User u;
    private SharedPreferences prefs;
    private User obj;
    private boolean isAppSelected;
    private MaterialLockView materialLockView;
    private int appId;
    private App app;
    private FloatingActionButton fab;
    private int LOCK_TYPE_SET=1003;
    private String lockTypeData;
    private SecureUtil util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkpassword);
        init();
        setSupportActionBar(toolbar);
        prefs= PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json=prefs.getString(AppConstant.USER_OBJ,null);
        obj = gson.fromJson(json, User.class);
        SQLiteDatabase.loadLibs(this);
        helper=new DBHelper(this);
        apps=util.getAllApp(obj.getId());
        isAppSelected = prefs.getBoolean(AppConstant.IS_APP_SELECTED,false);
        if(isAppSelected){
            getSupportActionBar().setTitle(R.string.label_check_app_password);
        }
        else{
            getSupportActionBar().setTitle(R.string.label_check_device_password);
        }

        Intent intent = getIntent();
        if(intent != null){
            appId = intent.getIntExtra(AppConstant.APP_ID,0);
        }
        SQLiteDatabase.loadLibs(this);
        users=util.getAllUsers();
        Iterator<User> itr=users.iterator();
        while(itr.hasNext()) {
            u = itr.next();
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(CheckAppPassword.this,SelectLockPattern.class);
                prefs.edit().putInt(AppConstant.APP_ID,appId).commit();
                if(isAppSelected)
                    intent.putExtra(AppConstant.IS_CALL_FROM_DEVICE_LOCK,false);
                else
                    intent.putExtra(AppConstant.IS_CALL_FROM_DEVICE_LOCK,true);

                startActivityForResult(intent,LOCK_TYPE_SET);
                overridePendingTransition(R.anim.push_up_in,R.anim.push_down_out);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    /**
     * Initializes the views.
     */
    private void init(){
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        materialLockView = (MaterialLockView) findViewById(R.id.showAppPattern);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroupCheckAppPassword);
        txtShowAppNamePassword = (TextView) findViewById(R.id.txtShowAppNamePassword);
        txtShowAppNamePatternPassword = (TextView) findViewById(R.id.txtShowAppNamePatternPassword);
        imageAppShowPassword = (ImageView) findViewById(R.id.imageAppShowPassword);
        imageAppShowPatternPassword = (ImageView) findViewById(R.id.imageAppShowPatternPassword);
        layoutPasswordAuthenticationHeader = (RelativeLayout) findViewById(R.id.layoutPasswordAuthenticationHeader);
        layoutUniqueCode = (RelativeLayout) findViewById(R.id.layoutAppPasswordUniqueCode);
        layoutSecureAppPassword = (RelativeLayout) findViewById(R.id.outerLayoutSecureAppPassword);
        layoutOuterShowTextPassword = (RelativeLayout) findViewById(R.id.layoutOuterShowTextPassword);
        layoutOuterShowPatternPassword = (RelativeLayout) findViewById(R.id.layoutOuterShowPatternPassword);
        editShowAppPasswordText = (EditText) findViewById(R.id.editShowAppPasswordText);
        editUniqueCodeForgotPassword = (EditText) findViewById(R.id.editUniqueCodeForgotPassword);
        editSecureAppPassword = (EditText) findViewById(R.id.editSecureAppPassword);
        btnUniqueCodeAppPasswordVerification = (Button) findViewById(R.id.btnUniqueCodeAppPasswordVerification);
        btnSecureAppPasswordVerification = (Button) findViewById(R.id.btnSecureAppPasswordVerification);
        textInputShowPassword = (TextInputLayout)findViewById(R.id.textInputShowPassword);
        fab = (FloatingActionButton) findViewById(R.id.fabEditAppPassword);
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
        switch (view.getId()) {
            case R.id.radioUniqueCodeApp:
                if (checked)
                    // Unique Code based recovery
                fab.setVisibility(View.GONE);
                layoutPasswordAuthenticationHeader.setVisibility(View.GONE);
                layoutSecureAppPassword.setVisibility(View.GONE);
                layoutOuterShowTextPassword.setVisibility(View.GONE);
                layoutOuterShowPatternPassword.setVisibility(View.GONE);
                layoutUniqueCode.setVisibility(View.VISIBLE);
                btnUniqueCodeAppPasswordVerification.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (editUniqueCodeForgotPassword.getText().length() == 4) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(editUniqueCodeForgotPassword.getWindowToken(), 0);
                            verifyUniqueCode();
                        } else {
                            editUniqueCodeForgotPassword.setError("Unique code is 4-digit number");
                        }
                    }
                });

                break;
            case R.id.radioSecureAppPass:
                if (checked)
                    // Security Questions based recovery
                layoutSecureAppPassword.setVisibility(View.VISIBLE);
                fab.setVisibility(View.GONE);
                layoutPasswordAuthenticationHeader.setVisibility(View.GONE);
                layoutOuterShowTextPassword.setVisibility(View.GONE);
                layoutOuterShowPatternPassword.setVisibility(View.GONE);
                layoutUniqueCode.setVisibility(View.GONE);
                btnSecureAppPasswordVerification.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        verifySecureAppPassword();
                    }
                    });

                break;
        }
    }

    /**
     * This method is used to verify the unique code and recover the app/device password
     */
    private void verifyUniqueCode(){
        int uniqueCode = Integer.parseInt(editUniqueCodeForgotPassword.getText().toString());
        if(u.getUniqueCode() == uniqueCode){

            if(isAppSelected){
                app =  getAppBasedOnId(appId);

                if(app != null && app.getAppLockType().equals("Pattern")){
                    showPatternPasswordLayout(app.getPass(),false);
                }
                else {
                    showPasswordLayout(app.getPass(),false);
                }
            }
            else
            {
                Device device = util.getDeviceLockDetails(obj.getId());
                if(device.getLockType().equals("Pattern")){
                    showPatternPasswordLayout(device.getPassword(),true);
                }
                else {
                    showPasswordLayout(device.getPassword(),true);
                }
            }
        }
        else {
            editUniqueCodeForgotPassword.setError("Incorrect unique code");
            editUniqueCodeForgotPassword.setText("");
        }
    }

    /**
     * This method is used to verify the secure app password and recover the app/device password
     */
    private void verifySecureAppPassword(){
        if(u.getPass().equals(editSecureAppPassword.getText().toString())){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editUniqueCodeForgotPassword.getWindowToken(), 0);
            if(isAppSelected){
                app = null;
                ArrayList<App> appList = util.getAllApp(obj.getId());
                for(int i=0;i<appList.size();i++){
                    if(appId == appList.get(i).getId()){
                        app = appList.get(i);
                        break;
                    }
                }
                if(app.getAppLockType().equals("Pattern")){
                    showPatternPasswordLayout(app.getPass(),false);
                }
                else {
                    showPasswordLayout(app.getPass(),false);
                }
            }
            else
            {
                Device device = util.getDeviceLockDetails(1);
                if(device.getLockType().equals("Pattern")){
                  showPatternPasswordLayout(device.getPassword(),true);
                }
                else {
                  showPasswordLayout(device.getPassword(),true);
                }
            }
        }
        else
        {
            editSecureAppPassword.setError("Wrong Password");
            editSecureAppPassword.setText("");
        }
    }

    /**
     *This method is used to show the text password for App/Device
     * @param password
     * @param isDevice
     */
    private void showPasswordLayout(String password,boolean isDevice){
        layoutUniqueCode.setVisibility(View.GONE);
        layoutSecureAppPassword.setVisibility(View.GONE);
        layoutOuterShowTextPassword.setVisibility(View.VISIBLE);
        if(isDevice) {
            String modelName = android.os.Build.MODEL;
            txtShowAppNamePassword.setText(modelName);
        }
        else{
            txtShowAppNamePassword.setText(app.getName());
            textInputShowPassword.setHint("App Password");
            imageAppShowPassword.setVisibility(View.VISIBLE);
            Drawable drawable = getAppImageIcon(app.getAppPackName());
            imageAppShowPassword.setImageDrawable(drawable);
        }
        editShowAppPasswordText.setText(password);

        editShowAppPasswordText.setEnabled(false);
    }

    /**
     * This method is used to show the pattern for App/Device
     * @param password
     * @param isDevice
     */
    private void showPatternPasswordLayout(String password, boolean isDevice){
        layoutUniqueCode.setVisibility(View.GONE);
        layoutSecureAppPassword.setVisibility(View.GONE);
        layoutOuterShowPatternPassword.setVisibility(View.VISIBLE);
        if(isDevice) {
            String modelName = android.os.Build.MODEL;
            txtShowAppNamePatternPassword.setText(modelName);
        }
        else{
            txtShowAppNamePatternPassword.setText(app.getName());
            imageAppShowPatternPassword.setVisibility(View.VISIBLE);
            Drawable drawable = getAppImageIcon(app.getAppPackName());
            imageAppShowPatternPassword.setImageDrawable(drawable);
        }
        List<MaterialLockView.Cell> cells = getCells(password);
        materialLockView.setPattern(MaterialLockView.DisplayMode.Correct,cells);
        materialLockView.setEnabled(false);
    }

    /**
     * This method return the list of cells
     * @param password
     * @return
     */
    private List<MaterialLockView.Cell> getCells(String password){
        List<MaterialLockView.Cell> listCells = new ArrayList<MaterialLockView.Cell>();
        int[] digits = digits(password);
        for(int i=0;i<digits.length;i++){
            MaterialLockView.Cell cell = getCell(digits[i]);
            listCells.add(cell);
        }
        return listCells;
    }

    /**
     * This method is used to get the cell
     * @param index
     * @return
     */
    private MaterialLockView.Cell getCell(int index){
        MaterialLockView.Cell cell = null;
        switch (index){
            case 1:
                 cell = new MaterialLockView.Cell(0,0);
                break;
            case 2:
                cell = new MaterialLockView.Cell(0,1);
                break;
            case 3:
                cell = new MaterialLockView.Cell(0,2);
                break;
            case 4:
                cell = new MaterialLockView.Cell(1,0);
                break;
            case 5:
                cell = new MaterialLockView.Cell(1,1);
                break;
            case 6:
                cell = new MaterialLockView.Cell(1,2);
                break;
            case 7:
                cell = new MaterialLockView.Cell(2,0);
                break;
            case 8:
                cell = new MaterialLockView.Cell(2,1);
                break;
            case 9:
                cell = new MaterialLockView.Cell(2,2);
                break;
        }
        return cell;
    }

    /**
     * This method returns the integer array for the text of pattern password
     * @param pass
     * @return
     */
    private int[] digits(String pass){
        int[] newGuess = new int[pass.length()];
        for (int i = 0; i < pass.length(); i++)
        {
            newGuess[i] = pass.charAt(i) - '0';
        }
        return newGuess;
    }

    /**
     * Method to get the drawable image icon based on package name
     * @param appPackName
     * @return
     */
    private Drawable getAppImageIcon(String appPackName) {
        Drawable icon = null;
        final List<ApplicationInfo> apps = getPackageManager().getInstalledApplications(0);
        for (int i = 0; i < apps.size(); i++) {
            ApplicationInfo packageInfo;

            if ((apps.get(i).flags & ApplicationInfo.FLAG_SYSTEM) != 1 && apps.get(i).packageName.equals(appPackName)) {
                //System app
                try {
                    icon = getPackageManager().getApplicationIcon(apps.get(i).packageName);
                    break;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            } else {

                if ((apps.get(i).flags & ApplicationInfo.FLAG_SYSTEM) == 1 && apps.get(i).packageName.equals(appPackName)) {
                    try {
                        icon = getPackageManager().getApplicationIcon(apps.get(i).packageName);
                    }
                    catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return icon;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null) {

            if(requestCode == LOCK_TYPE_SET) {

                if(isAppSelected) {
                    lockTypeData = data.getStringExtra(AppConstant.LOCK_TYPE);
                    appId = prefs.getInt(AppConstant.APP_ID, 0);
                    app = getAppBasedOnId(appId);
                    String pinPasswordPatternData = data.getStringExtra(AppConstant.LOCK_PIN_PASSWORD_PATTERN_DATA);

                    app.setAppLockType(lockTypeData);
                    app.setPass(pinPasswordPatternData);
                    util.updateRecord(app, appId, obj.getId());

                    LayoutInflater inflater = getLayoutInflater();
                    View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_layout));
                    SecureUtil.setToastMessage(this,"Successfully updated",toastLayout);

                    overridePendingTransition(R.anim.push_down_in,R.anim.push_up_out);
                    finish();
                }
                else{
                    lockTypeData = data.getStringExtra(AppConstant.LOCK_TYPE);
                    boolean isDeviceLockPasswordEnabled = prefs.getBoolean(AppConstant.IS_DEVICE_LOCK_ENABLED,false);
                    String pinPasswordPatternData = data.getStringExtra(AppConstant.LOCK_PIN_PASSWORD_PATTERN_DATA);
                    Device device = new Device();
                    device.setId(1);
                    device.setPassword(pinPasswordPatternData);
                    device.setLockType(lockTypeData);
                    device.setLockEnabled(isDeviceLockPasswordEnabled);
                    device.setImagePosition(1);
                    if(util.getDeviceLockDetails(1) != null) {
                        util.updateDeviceLockDetails(device,1);
                    }

                    LayoutInflater inflater = getLayoutInflater();
                    View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_layout));
                    SecureUtil.setToastMessage(this,"Successfully updated",toastLayout);

                    overridePendingTransition(R.anim.push_down_in,R.anim.push_up_out);
                    finish();
                }
            }
        }
    }

    /**
     * This method is used to return the app based on app Id
     * @param appId
     * @return
     */
    private App getAppBasedOnId(int appId){
        ArrayList<App> appList = util.getAllApp(obj.getId());
        for(int i=0;i<appList.size();i++){
            if(appId == appList.get(i).getId()){
                app = appList.get(i);
                break;
            }
        }
        return  app;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!SecureAppApplication.isApplicationVisible()){
            overridePendingTransition(R.anim.push_down_in,R.anim.push_up_out);
            finish();
            SecureAppApplication.logout(this);
        }
    }
}
