package com.bdebo.secureapp.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bdebo.secureapp.R;
import com.bdebo.secureapp.activity.AllThemesLockScreenActivity;

import java.util.List;

/**
 * Adapter class for setting Themes
 */
public class ThemesAdapter extends BaseAdapter {

    private List<Drawable> imageId;
    private Context context;
    private static LayoutInflater inflater=null;

    public ThemesAdapter(AllThemesLockScreenActivity mainActivity, List<Drawable> prgmImages) {
        context=mainActivity;
        imageId=prgmImages;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return imageId.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class Holder{
        ImageView img;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder=new Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.all_themes_pics, null);
        holder.img=(ImageView) rowView.findViewById(R.id.imageThemeImages);

        holder.img.setImageDrawable(imageId.get(position));

        return rowView;
    }

}