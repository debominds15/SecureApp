package com.bdebo.secureapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bdebo.secureapp.R;
import com.bdebo.secureapp.util.AppConstant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Adapter class for side navigation drawer
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private static final int TYPE_HEADER = 0;  // Declaring Variable to Understand which View is being worked on
    // IF the view under inflation and population is header or Item
    private static final int TYPE_ITEM = 1;

    private String mNavTitles[]; // String Array to store the passed titles Value from MainActivity.java
    private int mIcons[];       // Int Array to store the passed icons resource value from MainActivity.java
    String TAG = MyAdapter.class.getSimpleName();
    private String name;        //String Resource for header View Name
    private String pic_profile;        //int Resource for header view profile picture
    private String email;       //String Resource for header view email
    Context mContext;
    CallBackInterface callBackInterface;
    private boolean isImageFromCamera;
    public interface CallBackInterface
    {
        void handleProfile();
    }

    // Creating a ViewHolder which extends the RecyclerView View Holder
    // ViewHolder are used to to store the inflated views in order to recycle them

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        int holderId;

        private TextView textView;
        private ImageView imageView;
        private ImageView profile;
        private TextView Name;
        private TextView email;


        public ViewHolder(View itemView, int ViewType) {                 // Creating ViewHolder Constructor with View and viewType As a parameter
            super(itemView);


            // Here we set the appropriate view in accordance with the the view type as passed when the holder object is created

            if(ViewType == TYPE_ITEM) {
                textView = (TextView) itemView.findViewById(R.id.rowText); // Creating TextView object with the id of textView from item_row.xml
                imageView = (ImageView) itemView.findViewById(R.id.rowIcon);// Creating ImageView object with the id of ImageView from item_row.xml
                holderId = 1;                                               // setting holder id as 1 as the object being populated are of type item row
            }
            else{

                Name = (TextView) itemView.findViewById(R.id.name);         // Creating Text View object from header.xml for name
                email = (TextView) itemView.findViewById(R.id.email);       // Creating Text View object from header.xml for email
                profile = (ImageView) itemView.findViewById(R.id.imageView_round);// Creating Image view object from header.xml for profile pic

                holderId = 0;                                                // Setting holder id = 0 as the object being populated are of type header view
            }
        }

        @Override
        public void onClick(View v) {
        }
    }



    public MyAdapter(Context context,String Titles[], int Icons[], String Name, String Email, String profilePic,boolean isImageTakenFromCamera){ // MyAdapter Constructor with titles and icons parameter
        // titles, icons, name, email, profile pic are passed from the main activity as we have seen earlier
        mContext=context;
        mNavTitles = Titles;
        mIcons = Icons;
        name = Name;
        email = Email;
        pic_profile = profilePic;                     //here we assign those passed values to the values we declared here
        //in adapter
        callBackInterface=(CallBackInterface) context;
        isImageFromCamera = isImageTakenFromCamera;
    }



    //Below first we ovverride the method onCreateViewHolder which is called when the ViewHolder is
    //Created, In this method we inflate the item_row.xml layout if the viewType is Type_ITEM or else we inflate header.xml
    // if the viewType is TYPE_HEADER
    // and pass it to the view holder

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row,parent,false); //Inflating the layout

            ViewHolder vhItem = new ViewHolder(v,viewType); //Creating ViewHolder and passing the object of type view

            return vhItem; // Returning the created object

            //inflate your layout and pass it to view holder

        } else if (viewType == TYPE_HEADER) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_header,parent,false); //Inflating the layout

            ViewHolder vhHeader = new ViewHolder(v,viewType); //Creating ViewHolder and passing the object of type view

            return vhHeader; //returning the object created


        }
        return null;

    }

    //Next we override a method which is called when the item in a row is needed to be displayed, here the int position
    // Tells us item at which position is being constructed to be displayed and the holder id of the holder object tell us
    // which view type is being created 1 for item row
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(holder.holderId ==1) {                              // as the list view is going to be called after the header view so we decrement the
            // position by 1 and pass it to the holder while setting the text and image
            holder.textView.setText(mNavTitles[position - 1]); // Setting the Text with the array of our Titles
            holder.imageView.setImageResource(mIcons[position -1]);// Settimg the image with array of our icons
        }
        else{
            if(pic_profile != null) {
                Log.d(TAG,"isImageTakenFromCamera::"+isImageFromCamera);
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M && isImageFromCamera){
                    Log.d(TAG,"isImageTakenFromCamera::2"+isImageFromCamera);
                    Uri imageUri = Uri.parse(pic_profile);
                    File file = new File(imageUri.getPath());
                    try {
                        InputStream ims = new FileInputStream(file);
                        holder.profile.setImageBitmap(BitmapFactory.decodeStream(ims));
                    } catch (FileNotFoundException e) {
                    }
                }
                else {
                    byte[] array = Base64.decode(pic_profile, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(array, 0, array.length);
                    holder.profile.setImageBitmap(bmp);
                }
            }
            else {
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.ic_user);
                holder.profile.setImageDrawable(drawable);
            }

            // Similarly we set the resources for header view
            holder.Name.setText(name);
            holder.email.setText(email);

            holder.profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    callBackInterface.handleProfile();
                }
            });
        }
    }

    // This method returns the number of items present in the list
    @Override
    public int getItemCount() {
        return mNavTitles.length+1; // the number of items in the list will be +1 the titles including the header view.
    }


    // With the following method we check what type of view is being passed
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }
}
