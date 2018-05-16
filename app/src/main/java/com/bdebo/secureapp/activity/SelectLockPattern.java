package com.bdebo.secureapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bdebo.secureapp.R;
import com.bdebo.secureapp.SecureAppApplication;
import com.bdebo.secureapp.util.AppConstant;

import java.util.ArrayList;

/**
 * This class is used to select the lock type in order to set the lock screen
 */
public class SelectLockPattern extends ApplicationActivity {

    private String TAG=SelectLockPattern.class.getSimpleName();
    private ListView selectLockType;
    private LockAdapter l_adapter;
    private int LOCK_TYPE_CODE=1001;
    private int LOCK_TYPE_SET=1003;
    private String lockType="";
    private Toolbar toolbar;
    private boolean isCallFromDeviceLock;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_lock_pattern_activity);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        selectLockType=(ListView) this.findViewById(R.id.listViewLock);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        Intent intent = getIntent();
        isCallFromDeviceLock = intent.getBooleanExtra(AppConstant.IS_CALL_FROM_DEVICE_LOCK,false);

        final ArrayList<String> lockTypeList=new ArrayList<String>();
        lockTypeList.add("PIN");
        if(!isCallFromDeviceLock)
        lockTypeList.add("Password");
        lockTypeList.add("Pattern");
        this.l_adapter = new LockAdapter(this,R.layout.list_item_lock_pattern, lockTypeList);
        selectLockType.setAdapter(l_adapter);

        selectLockType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                lockType=lockTypeList.get(position).toString();
                sendLockIntent(lockType);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data!=null)
        {
            if(requestCode==LOCK_TYPE_CODE)
            {
                String pinPasswordPatternData=data.getStringExtra(AppConstant.LOCK_DATA);
                Intent intent=new Intent();
                intent.putExtra(AppConstant.LOCK_TYPE,lockType);
                intent.putExtra(AppConstant.LOCK_PIN_PASSWORD_PATTERN_DATA,pinPasswordPatternData);
                setResult(LOCK_TYPE_SET,intent);
                overridePendingTransition(R.anim.push_down_in,R.anim.push_up_out);
                finish();
            }
        }

    }

    /**
     * Adapter for selecting lock type
     */
    private class LockAdapter extends ArrayAdapter<String>
    {
        public ArrayList<String> patternList;
        private LayoutInflater vi;
        public LockAdapter(Context context, int resource,ArrayList<String> patternList) {
            super(context, resource);
            this.patternList=patternList;
            vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return patternList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = vi.inflate(R.layout.list_item_lock_pattern, null);
            TextView lock=(TextView) row.findViewById(R.id.lockName);
            lock.setText(patternList.get(position).toString());
            return row;
        }
    }

    /**
     * Send lock type details through intent
     * @param lockType
     */
    private void sendLockIntent(String lockType)
    {

        Intent intent=null;
        if(lockType.equals("PIN") || lockType.equals("Password")) {
            intent = new Intent(this,SetPINPasswordLock.class);
            //intent.putExtra(AppConstant.PIN_OR_PASSWORD,lockType);
            intent.putExtra(AppConstant.LOCK_TYPE,lockType);
            startActivityForResult(intent,LOCK_TYPE_CODE);
        }
        else {
            intent = new Intent(this, SetPatternLock.class);
            intent.putExtra(AppConstant.LOCK_TYPE,lockType);
            startActivityForResult(intent,LOCK_TYPE_CODE);
        }
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(!SecureAppApplication.isApplicationVisible()){
            overridePendingTransition(R.anim.push_down_in,R.anim.push_up_out);
            finish();
            SecureAppApplication.logout(this);
        }
    }
}
