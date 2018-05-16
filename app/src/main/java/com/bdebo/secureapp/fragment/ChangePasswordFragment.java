package com.bdebo.secureapp.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bdebo.secureapp.activity.Login;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.model.User;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;
import com.google.gson.Gson;

/**
 * This fragment class is used to change password
 */
public class ChangePasswordFragment extends Fragment{
    private EditText oldp,newp,confirmp;
    private Button change;
    private SharedPreferences prefs;
    private SecureUtil util;

    public ChangePasswordFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_change_password, container,false);
        init(rootView);
        setHasOptionsMenu(true);//Without this onOptionsSelected method wont work in fragment
        Gson gson = new Gson();
        String json = prefs.getString(AppConstant.USER_OBJ,null);
        final User user = gson.fromJson(json, User.class);

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow((confirmp).getWindowToken(), 0);
                if(oldp.getText().toString().trim().equalsIgnoreCase(""))
                    oldp.setError("Cannot be empty");
                else if(newp.getText().toString().matches(""))
                    newp.setError("Cannot be empty");
                else if(confirmp.getText().toString().matches(""))
                    confirmp.setError("Cannot be empty");

                else if(!newp.getText().toString().equals(confirmp.getText().toString()))
                  {
                      confirmp.setError("Password and Confirm Password should be same");
                      newp.setText("");
                      confirmp.setText("");
                }
                else if(newp.getText().toString().equals(oldp.getText().toString())){

                    newp.setError("Old and new password cannot be same");
                    newp.setText("");
                    confirmp.setText("");
                }
                else
                  {
                      getActivity().getSupportFragmentManager().popBackStack();

                      p1:   if(!oldp.getText().toString().equals(user.getPass()))
                      {
                          oldp.setError("Incorrect password");

                      }
                      else
                      {
                          showAlert(user);
                      }
                  }
            }
        });
        return rootView;
    }

    /**
     * Initializes the views
     * @param rootView
     */
    private void init(View rootView){
        oldp=(EditText) rootView.findViewById(R.id.editOldPass);
        newp=(EditText) rootView.findViewById(R.id.editNewPass);
        confirmp=(EditText) rootView.findViewById(R.id.editConfirmPass);
        change=(Button) rootView.findViewById(R.id.btnChangePassword);
        prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        util = new SecureUtil(getActivity());
    }

    /**
     * Method to show alert for redirecting to login page
     * @param user
     */
    public void showAlert(final User user){

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        alertDialog.setTitle("Profile").setMessage("Do u want to update your password ? " +
                "It will then redirect you to login page");
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
            }
        });
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                user.setPass(newp.getText().toString());
                util.updateUserDetails(user,user.getId());

                callToastMessage("Password has been changed");
                Intent intent=new Intent(getActivity(),Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                getActivity().finish();
            }
        });
        alertDialog.show();
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

    /**
     * Display Toast message
     * @param msg
     */
    private void callToastMessage(String msg){
        LayoutInflater inflater = getLayoutInflater(null);
        View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) getActivity().findViewById(R.id.custom_toast_layout));
        SecureUtil.setToastMessage(getActivity(),msg,toastLayout);
    }
}
