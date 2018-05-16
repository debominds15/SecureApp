package com.bdebo.secureapp.adapter;

/**
 * Created by Debojyoti on 25-07-2016.
 */
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bdebo.secureapp.R;
import com.bdebo.secureapp.activity.AllThirdPartyApps;

import java.util.List;

/**
 * Adapter class for third party apps installed
 */
public class CustomThirdPartyAppAdapter extends BaseAdapter{

    private List<String> result;
    private List<Drawable> imageId;
    private Context context;
    private static LayoutInflater inflater=null;

    public CustomThirdPartyAppAdapter(AllThirdPartyApps mainActivity, List<String> prgmNameList, List<Drawable> prgmImages) {
        result=prgmNameList;
        context=mainActivity;
        imageId=prgmImages;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return result.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder
    {
        TextView tv;
        ImageView img;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder=new Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.third_party_app, null);
        holder.tv=(TextView) rowView.findViewById(R.id.textThirdApps);
        holder.img=(ImageView) rowView.findViewById(R.id.imageThirdApps);

        holder.tv.setText(result.get(position));
        holder.img.setImageDrawable(imageId.get(position));

        return rowView;
    }

}