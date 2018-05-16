package com.bdebo.secureapp.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bdebo.secureapp.R;

public class HelpFragment extends Fragment{
    private TextView welcome;
    private String username;
    private SharedPreferences prefs;
    public HelpFragment() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_help, container,false);
        setRetainInstance(true);
        setHasOptionsMenu(true);//Without this onOptionsSelected method wont work in fragment

        return rootView;
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
