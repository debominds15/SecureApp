package com.bdebo.secureapp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bdebo.secureapp.activity.Login;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.model.User;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to update profile
 */
public class ProfileFragment extends Fragment implements OnItemSelectedListener{

    private CollapsingToolbarLayout collapsingToolbarLayout = null;
    private EditText editName,editUsername,editUniqueCode,editFirstSecAnswer,editSecondSecAnswer;
    private Spinner spinnerFirstSecQuestion,spinnerSecondSecQuestion;
    private User u;
    private String item,item2,username,instrs;
    private static final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";
    private ImageView imageView;
    private Button save;
    private ArrayList<User> users;
    private SharedPreferences prefs;
    private int btnCnt;
    private ArrayList<String> list,list2;
    private int textColor;
    private SecureUtil util;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.profile_fragment, container,false);
        init(rootView);
        setHasOptionsMenu(true);//Without this onOptionsSelected method wont work in fragment
        setAdapterForSpinner();
        spinnerFirstSecQuestion.setOnItemSelectedListener(this);
        spinnerSecondSecQuestion.setOnItemSelectedListener(this);
        textColor = getResources().getColor(R.color.colorSecondaryText);

        username=prefs.getString(AppConstant.USER_NAME, null);
        users=util.getAllUsers();
        Iterator<User> itr=users.iterator();
        while(itr.hasNext()) {
            u = itr.next();
        }
        setFields();
        String imageByteString = prefs.getString(AppConstant.PROFILE_IMAGE_BYTE_ARRAY,null);
        if(imageByteString != null) {
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                Uri imageUri = Uri.parse(imageByteString);
                File file = new File(imageUri.getPath());
                try {
                    InputStream ims = new FileInputStream(file);
                    imageView.setImageBitmap(BitmapFactory.decodeStream(ims));
                } catch (FileNotFoundException e) {
                }
            }
            else {
                byte[] array = Base64.decode(imageByteString, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(array, 0, array.length);
                imageView.setImageBitmap(bmp);
            }
        }

        save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(btnCnt %2!=0) {
                    disabledEnabled(true);
                }
                else {
                    if(!checkAllViews()){
                        if(!editUsername.getText().toString().equals(username))
                        {
                            prefs.edit().putString(AppConstant.USER_NAME, editUsername.getText().toString()).commit();
                            showAlert("Do u want to update your username ? " +
                                    "It will then redirect you to login page");
                        }
                        else{
                            showAlert("Do you want to Update it ?");
                        }
                    }
                    else{
                        Log.d("Register","Error...");
                    }
                }
            }
        });
        return rootView;
    }

    /**
     * Initializes views
     * @param rootView
     */
    private void init(View rootView){

        imageView = (ImageView) rootView.findViewById(R.id.imageProfile);
        collapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        prefs=PreferenceManager.getDefaultSharedPreferences(getActivity());
        util = new SecureUtil(getActivity());
        spinnerFirstSecQuestion=(Spinner)rootView.findViewById(R.id.spinnersSecQuest1Profile);
        spinnerSecondSecQuestion=(Spinner)rootView.findViewById(R.id.spinnersSecQuest2Profile);
        save=(Button) rootView.findViewById(R.id.butnSave);

        editName =(EditText)rootView.findViewById(R.id.editName);
        editUsername=(EditText) rootView.findViewById(R.id.editAddr);
        editUniqueCode=(EditText) rootView.findViewById(R.id.editUniqueCode);
        editFirstSecAnswer=(EditText) rootView.findViewById(R.id.editSecAnswer1Profile);
        editSecondSecAnswer=(EditText) rootView.findViewById(R.id.editSecAnswer2Profile);
    }

    /**
     * Add items in list and set adapters for spinners
     */
    private void setAdapterForSpinner(){
        list=new ArrayList<String>();
        list.add("What was your first pet name ?");
        list.add("What was your favourite teacher's first name ?");
        list.add("What is your father's nick name ?");
        list.add("Which was your first school ?");

        list2=new ArrayList<String>();
        list2.add("Who was your childhood sports hero ?");
        list2.add("What is your favourite sport ?");
        list2.add("What was your childhood nickname ?");
        list2.add("What is your mother's maiden name  ?");

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFirstSecQuestion.setAdapter(adapter);

        ArrayAdapter<String> adapter2=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,list2);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSecondSecQuestion.setAdapter(adapter2);
    }

    /**
     * Set fields
     */
    private void setFields() {
        editName.setText(u.getName());
        editUsername.setText(u.getUsername());
        String uniqueCode = String.valueOf(u.getUniqueCode());
        editUniqueCode.setText(uniqueCode);
        int pos = getItemIndexFromSecQuestionsList(list,u.getSec_q1());
        spinnerFirstSecQuestion.setSelection(pos);
        int pos2 = getItemIndexFromSecQuestionsList(list2,u.getSec_q2());
        spinnerSecondSecQuestion.setSelection(pos2);
        editFirstSecAnswer.setText(u.getSec_ans1());
        editSecondSecAnswer.setText(u.getSec_ans2());
        disabledEnabled(false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
    public void showAlert(String instr){

        instrs=instr;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        alertDialog.setTitle(R.string.str_profile).setMessage(instr);
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                arg0.cancel();
            }
        });
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if(instrs.equals("Do you want to Update it ?"))
                {
                    callToastMessage("Successfully Updated");
                    disabledEnabled(false);
                }
                else
                {
                    Intent intent=new Intent(getActivity(),Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);
                    getActivity().finish();
                }
                u = getUserDetails();
                util.updateUserDetails(u,u.getId());
            }
        });
        alertDialog.show();

    }

    /**
     * Display Toast message
     * @param msg
     */
    private void callToastMessage(String msg){
        LayoutInflater inflater = getLayoutInflater(null);
        View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) getActivity().findViewById(R.id.custom_toast_layout));
        SecureUtil.setToastMessage(getActivity(),msg,toastLayout);
    }

@Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                               long arg3) {
        switch (arg0.getId()) {
            case R.id.spinnersSecQuest1Profile:
                item = arg0.getItemAtPosition(arg2).toString();
                break;
            case  R.id.spinnersSecQuest2Profile:
                item2 = arg0.getItemAtPosition(arg2).toString();
                    break;
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    /**
     * Whether to disable or enable fields
     * @param b
     */
    public void disabledEnabled(boolean b)
    {
        if(b==true){
            save.setText("Update");
            editName.setTextColor(textColor);
            editUsername.setTextColor(textColor);
            editUniqueCode.setTextColor(textColor);
            editFirstSecAnswer.setTextColor(textColor);
            editSecondSecAnswer.setTextColor(textColor);
        }
        else {
            save.setText("Edit");
        }
        editName.setEnabled(b);
        editUsername.setEnabled(b);
        editUniqueCode.setEnabled(b);
        spinnerFirstSecQuestion.setEnabled(b);
        spinnerSecondSecQuestion.setEnabled(b);
        editFirstSecAnswer.setEnabled(b);
        editSecondSecAnswer.setEnabled(b);
        btnCnt++;
    }

    /**
     * Get item position selected from list
     * @param list
     * @param question
     * @return
     */
    private int getItemIndexFromSecQuestionsList(ArrayList<String> list, String question){

        int pos=0;
        for(String s:list)
        {
            if(s.equals(question))
                break;
            else
                pos++;
        }
        return pos;
    }

    /**
     * Validate username with regular expression
     * @param username username for validation
     * @return true valid username, false invalid username
     */
    private boolean validateUserName(final String username){

        Pattern pattern = Pattern.compile(USERNAME_PATTERN);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();

    }

    /**
     * Set user object details before updating in DB
     * @return
     */
    private User getUserDetails(){
        u.setName(editName.getText().toString());
        u.setUsername(editUsername.getText().toString());
        u.setUniqueCode(Integer.parseInt(editUniqueCode.getText().toString()));
        u.setSec_q1(item);
        u.setSec_ans1(editFirstSecAnswer.getText().toString());
        u.setSec_q2(item2);
        u.setSec_ans2(editSecondSecAnswer.getText().toString());
        return u;
    }

    /**
     * Check whether any error is observed for any field
     * @return true if any error is observed
     * @return false, if no error is observed
     */
    private boolean checkAllViews(){
        boolean isErrorCheck = false;

        if(isErrorPresent(editName))
            isErrorCheck =true;

        return isErrorCheck;
    }

    /**
     * Check if any error is present
     * @param view
     * @return
     */
    private boolean isErrorPresent(View view) {
        boolean isErrorCheck = false;
        int id = view.getId();
        int pos=0;
        switch (id){
            case R.id.editName:
                if(editName.getText().toString().length() < 3) {
                    editName.setError("Name should be minimum of 3 characters");
                    isErrorCheck = true;
                    pos=1;
                    break;
                }


            case R.id.editAddr:

                if(validateUserName(editUsername.getText().toString()) == false) {
                    editUsername.setError("Username should be alphanumeric with minimum of 4 characters");
                    isErrorCheck = true;
                    pos=2;
                    break;
                }

            case R.id.editUniqueCode:
                if(editUniqueCode.getText().toString().length()<4 || editUniqueCode.getText().toString().equals("")) {
                    editUniqueCode.setError("Unique code should be exactly 4-digit");
                    isErrorCheck = true;
                    pos=5;
                    break;
                }

            case R.id.editSecAnswer1Profile:
                if(editFirstSecAnswer.getText().toString().length() < 3) {
                    editFirstSecAnswer.setError("Security answer should be minimum of 3 characters");
                    isErrorCheck = true;
                    pos=6;
                    break;
                }

            case R.id.editSecAnswer2Profile:
                if(editSecondSecAnswer.getText().toString().length() < 3) {
                    editSecondSecAnswer.setError("Security answer should be minimum of 3 characters");
                    isErrorCheck = true;
                    pos=7;
                    break;
                }

            case R.id.spinnersSecQuest1Profile:
                if (spinnerFirstSecQuestion.getSelectedItem().toString().trim().equalsIgnoreCase("Select")){
                    ((TextView) spinnerFirstSecQuestion.getSelectedView()).setError("None selected");
                    isErrorCheck = true;
                    pos=8;
                    break;
                }

            case R.id.spinnersSecQuest2Profile:
                if(spinnerSecondSecQuestion.getSelectedItem().toString().trim().equalsIgnoreCase("Select")) {
                    ((TextView) spinnerSecondSecQuestion.getSelectedView()).setError("None selected");
                    isErrorCheck = true;
                    pos=9;
                    break;
                }
        }
        return isErrorCheck;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem register = menu.findItem(R.id.action_device_lock_theme);
        MenuItem device_invisible_pattern= menu.findItem(R.id.device_action_lock_invisible_pattern);
        MenuItem search = menu.findItem(R.id.action_search);

            register.setVisible(false);
            device_invisible_pattern.setVisible(false);
            search.setVisible(false);
    }

}