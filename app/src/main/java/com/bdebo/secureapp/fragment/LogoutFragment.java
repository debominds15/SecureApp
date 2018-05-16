package com.bdebo.secureapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.bdebo.secureapp.R;
import com.bdebo.secureapp.activity.Login;

/**
 * This class is used to perform Logout
 */
public class LogoutFragment extends Fragment{

    public LogoutFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=new Intent(getActivity(),Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        getActivity().finish();
    }
}
