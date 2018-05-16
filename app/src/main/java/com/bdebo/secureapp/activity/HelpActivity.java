package com.bdebo.secureapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bdebo.secureapp.R;
import com.bdebo.secureapp.SecureAppApplication;
import com.bdebo.secureapp.util.AppConstant;

/**
 * This class is used to show the Help Screen
 */
public class HelpActivity extends ApplicationActivity {

    private ListView listView;
    private String[] faqList;
    private int helpArrayCnt;
    private LayoutInflater mLayoutInflater;
    private Toolbar toolbar;
    private String TAG = HelpActivity.class.getSimpleName();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_activity);
        faqList = getResources().getStringArray(R.array.array_help_list);
        if (faqList != null) {
            helpArrayCnt = faqList.length;
        }
        mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        listView = (ListView) findViewById(R.id.listViewFaq);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.label_help);
        listView.setAdapter(new HelpAdapter());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(HelpActivity.this,HelpDetailsActivity.class);
                intent.putExtra(AppConstant.HELP_ITEM_POSITION,position);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });
    }

    /**
     * Adapter class for Help items
     */
    private class HelpAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return helpArrayCnt;
        }

        @Override
        public String getItem(int position) {
            return faqList[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView == null) {
                view = (View) mLayoutInflater.inflate(R.layout.help_item, null);
            } else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(R.id.txtHelpItem);
            String text = getItem(position);
            textView.setText(text);
            return view;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(!SecureAppApplication.isApplicationVisible()){
            overridePendingTransition(R.anim.push_up_in,R.anim.push_down_out);
            finish();
            SecureAppApplication.logout(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.push_up_in,R.anim.push_down_out);
    }
}
