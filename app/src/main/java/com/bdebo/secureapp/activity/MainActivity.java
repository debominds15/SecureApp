package com.bdebo.secureapp.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bdebo.secureapp.BuildConfig;
import com.bdebo.secureapp.fragment.FragmentDrawer;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.SecureAppApplication;
import com.bdebo.secureapp.adapter.MyAdapter;
import com.bdebo.secureapp.fragment.ChangePasswordFragment;
import com.bdebo.secureapp.fragment.HelpFragment;
import com.bdebo.secureapp.fragment.HomeFragment;
import com.bdebo.secureapp.fragment.LogoutFragment;
import com.bdebo.secureapp.fragment.ProfileFragment;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.util.SecureUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


import static android.os.Build.VERSION_CODES.M;

/**
 * This class is the main class which holds all other fragments
 * for securing other apps or device
 */
public class MainActivity extends ApplicationActivity implements FragmentDrawer.FragmentDrawerListener,MyAdapter.CallBackInterface{
    private  static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 5;
    private String TAG = MainActivity.class.getSimpleName();
    private Uri picUri;
    private File pic;
    private Toolbar toolbar;
    private FragmentDrawer drawerFragment;
    private boolean checkPermissionREAD_EXTERNAL_STORAGE = false;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1,CROP_IMAGE = 2,REQUEST_TAKE_PHOTO = 3;
    SharedPreferences prefs;
    byte[] byteArray;
    private Uri imageUri;
    private static final String CAPTURE_IMAGE_FILE_PROVIDER = "com.dwx331409.secureapp.provider";
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs= PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.DrawerLayout), toolbar);
        drawerFragment.setDrawerListener(this);
        // display the first navigation drawer view on app launch
        displayView(0);
    }

    /**
     * This method is used to select image file from Gallery or by Camera
     */
    public void selectImage() {

        checkPermissionREAD_EXTERNAL_STORAGE = checkPermissionREAD_EXTERNAL_STORAGE(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View deleteDialogView = factory.inflate(
                R.layout.set_profile_image_dialog, null);
        final AlertDialog builder = new AlertDialog.Builder(this).create();
        builder.setTitle("Select Image");
        builder.setView(deleteDialogView);
        ImageView camera=(ImageView) deleteDialogView.findViewById(R.id.cameraIcon);
        ImageView gallery=(ImageView) deleteDialogView.findViewById(R.id.galleryIcon);
        ImageView remove=(ImageView) deleteDialogView.findViewById(R.id.removeIcon);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                prefs.edit().putBoolean(AppConstant.IS_IMAGE_SELECTED,true).commit();
                if(Build.VERSION.SDK_INT >= M){
                    //openCamera();
                    try {
                        dispatchTakePictureIntent();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    cameraIntent();
                }
                builder.dismiss();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                prefs.edit().putBoolean(AppConstant.IS_IMAGE_SELECTED,true).commit();
                galleryIntent();
                builder.dismiss();
            }
        });

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imageByteString = prefs.getString(AppConstant.PROFILE_IMAGE_BYTE_ARRAY,null);
                if(imageByteString != null){
                    prefs.edit().putString(AppConstant.PROFILE_IMAGE_BYTE_ARRAY,null).commit();
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_user);
                    ImageView imageView=(ImageView) findViewById(R.id.imageView_round);
                    imageView.setImageDrawable(drawable);
                }
                callToastMessage("Profile image is removed");
                builder.dismiss();
            }
        });
        builder.show();
    }

    /**
     * This method is used to show the alert if the read
     * external storage permission is not granted
     * @param instr
     */
    public void showAlert(String instr){

        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(R.string.label_external_storage_permission).setMessage(instr);
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.cancel();
                finish();
            }
        });
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent();
                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.setData(Uri.parse("package:" + getPackageName()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(i);
            }
        });
        // Create the AlertDialog object and return it
        alertDialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                }
                return true;
            }
        });
        alertDialog.show();
    }

    /**
     * This method is used to select image from gallery or show alert if
     * permission is not given
     */
    private void galleryIntent() {
       // checkPermissionREAD_EXTERNAL_STORAGE = checkPermissionREAD_EXTERNAL_STORAGE(this);
        if (checkPermissionREAD_EXTERNAL_STORAGE) {
            try {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_FILE);
            }
            catch (ActivityNotFoundException e){
                Log.e(TAG,e.getLocalizedMessage());
                callToastMessage("Gallery could not be open!!!");
            }
        }
        else{
            showAlert("Permission required for selecting image from external storage");
        }
    }

    /**
     * This method is used to capture image from Camera
     * and set uri if the version is less than android 6
     */
    private void cameraIntent() {
        try {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            if(Build.VERSION.SDK_INT > M){
                cameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                File imagePath = new File(getFilesDir(), "images");
                File newFile = new File(imagePath, "default_image.jpg");
                picUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", newFile  );
            }
            else {
                pic = new File(Environment.getExternalStorageDirectory(),
                        "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                picUri = Uri.fromFile(pic);
            }
            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, picUri);
            cameraIntent.putExtra(AppConstant.RETURN_DATA, true);
            startActivityForResult(cameraIntent, REQUEST_CAMERA);

        }catch (ActivityNotFoundException e){
            Log.e(TAG,e.getLocalizedMessage());
            callToastMessage("Camera not found in device!!!");
        }
    }

/*    *//**
     * This method is used to capture image from Camera
     * and set uri if the version is more than android 6
     *//*
    private void openCamera(){

        PackageManager packageManager = getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            //yes
            try {
                Log.i("camera", "This device has camera!");
                File path = new File(getFilesDir(), ".");
                if (!path.exists())
                    path.mkdirs();
                File image = new File(path, "image.jpg");
                imageUri = FileProvider.getUriForFile(this, CAPTURE_IMAGE_FILE_PROVIDER, image);
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, REQUEST_CAMERA);
            }
            catch (ActivityNotFoundException e){
                Log.e(TAG,e.getLocalizedMessage());
                setToastMessage("Camera not found in device!!!");
            }
            }else{
            //no
            Log.i("camera", "This device has no camera!");
            setToastMessage("Camera not found in device!!!");
        }

    }*/

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
         mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() throws IOException {

        PackageManager packageManager = getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            //yes
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    return;
                }
                // Continue only if the File was successfully created
                Log.d(TAG, BuildConfig.APPLICATION_ID + ".fileprovider");

                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                            BuildConfig.APPLICATION_ID + ".fileprovider",
                            createImageFile());
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_CAMERA);
                }
            }
        }
    else{
        //no
        Log.i("camera", "This device has no camera!");
            callToastMessage("Camera not found in device!!!");
    }

    }

    /**
     * This method is used to check the permission for external storage
     * @param context
     * @return
     */
    public boolean checkPermissionREAD_EXTERNAL_STORAGE(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage",context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                }
                else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    /**
     * This method is used to show the dialog
     * @param msg
     * @param context
     * @param permission
     */
    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    callToastMessage("GET_ACCOUNTS Denied");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                if (data != null) {

                    if (checkPermissionREAD_EXTERNAL_STORAGE) {
                        picUri = data.getData();
                        if (picUri != null) {
                            Bitmap photo = decodeUriAsBitmap(picUri);
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            photo.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                            byteArray = bytes.toByteArray();
                            // Set the image in ImageView
                            updateProfileInDB(byteArray,picUri);
                        }
                        else{
                            Log.d(TAG,"Permission not granted!!!");
                        }
                    }
                }
            }
            else if (requestCode == REQUEST_CAMERA) {
                prefs.edit().putBoolean(AppConstant.IS_IMAGE_SELECTED,true).commit();
                if(Build.VERSION.SDK_INT > M){
                    Uri imageUri = Uri.parse(mCurrentPhotoPath);
                    updateProfileInDBNougat(imageUri);
                }
                else {
                    CropImage(true);
                }
            }
            else if (requestCode == CROP_IMAGE) {
                if (data != null) {
                    Bundle extras = data.getExtras();
                    // get the cropped bitmap
                    if(extras != null) {


                        Bitmap photo = extras.getParcelable("data");
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        photo.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

                        byteArray = bytes.toByteArray();
                        updateProfileInDB(byteArray,imageUri);
                        if (pic != null) {
                            // To delete original image taken by camera
                            if (pic.delete())
                                callToastMessage("Image deleted");
                        }
                    }
                    else{
                        Uri uri = data.getData();
                        Bitmap photo = decodeUriAsBitmap(uri);
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        photo.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                        byteArray = bytes.toByteArray();
                        updateProfileInDB(byteArray,uri);
                    }
                }
            }
            else if(requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE){
                Log.d(TAG,"MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE");
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            Log.d(TAG,"resultCode: Canceled");
        }

/*        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            // Show the thumbnail on ImageView
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File file = new File(imageUri.getPath());
            try {
                InputStream ims = new FileInputStream(file);
                Log.d(TAG,"camera is done in nougat!!! "+ims);
             //   ivPreview.setImageBitmap(BitmapFactory.decodeStream(ims));
            } catch (FileNotFoundException e) {
                return;
            }

            // ScanFile so it will be appeared on Gallery
            MediaScannerConnection.scanFile(MainActivity.this,
                    new String[]{imageUri.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
        }*/

    }

    /**
     * This class is used to decode the Uri as bitmap
     * @param uri
     * @return
     */
    private Bitmap decodeUriAsBitmap(Uri uri){
        Bitmap bitmap = null;
        try {
            //checkRuntimePermission();
            Log.d(TAG,"decodeUriAsBitmap():: "+getContentResolver().openInputStream(uri));
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    /*
     * This method is used to crop the image accordingly
    */
    protected void CropImage(boolean isCallFromCamera,Uri picUri) {
        Log.d(TAG, "CropImage()" +picUri);
        try {
            grantUriPermission("com.android.camera",picUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(picUri, "image/*");
            Log.d(TAG, "");

            intent.putExtra("crop", "true");
            intent.putExtra("outputX", 200);
            intent.putExtra("outputY", 200);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);

            if(!isCallFromCamera)
            intent.setAction(Intent.ACTION_GET_CONTENT);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
            intent.putExtra(AppConstant.RETURN_DATA, true);
            startActivityForResult(intent, CROP_IMAGE);

        }
        catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, e.getLocalizedMessage());
            callToastMessage("Your device doesn't support the crop action");
        }
    }
    /*
     * This method is used to crop the image accordingly
     */
    protected void CropImage(boolean isCallFromCamera) {
        Log.d(TAG, "CropImage()");
            try {
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(picUri, "image/*");
                Log.d(TAG, "");

                intent.putExtra("crop", "true");
                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 200);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("scale", true);
                intent.putExtra(AppConstant.RETURN_DATA, true);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, CROP_IMAGE);
                } else {
                    callToastMessage("No Crop App Available");
                }

            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, e.getLocalizedMessage());
                callToastMessage("Your device doesn't support the crop action");
            }
    }

    /*
     * This method update the profile image in database and set the image
     */
    public void updateProfileInDB(byte[] byteArray,Uri imageUri)
    {

        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        ImageView imageView=(ImageView) findViewById(R.id.imageView_round);
        imageView.setImageBitmap(bmp);

        String saveThis = Base64.encodeToString(byteArray, Base64.DEFAULT);
        prefs.edit().putBoolean(AppConstant.IS_IMAGE_CAMERA,false).commit();
        prefs.edit().putString(AppConstant.PROFILE_IMAGE_BYTE_ARRAY,saveThis).commit();
    }


    /*
     * This method update the profile image in database and set the image if android version is more than M
     */
    public void updateProfileInDBNougat(Uri imageUri)
    {

        File file = new File(imageUri.getPath());
        try {
            InputStream ims = new FileInputStream(file);
            ImageView imageView=(ImageView) findViewById(R.id.imageView_round);
            imageView.setImageBitmap(BitmapFactory.decodeStream(ims));
            String saveThis = mCurrentPhotoPath;
            prefs.edit().putBoolean(AppConstant.IS_IMAGE_CAMERA,true).commit();
            prefs.edit().putString(AppConstant.PROFILE_IMAGE_BYTE_ARRAY,saveThis).commit();

        } catch (FileNotFoundException e) {
            return;
        }
    }
    @Override
    public void handleProfile() {
         selectImage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case R.id.action_help:
                Intent helpIntent = new Intent(this, HelpActivity.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.push_down_in,R.anim.push_up_out);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position-1);
    }

    /**
     * This method is used to display the items in Navigation drawer
     * and on click fragment will be loaded for each item
     * @param position
     */
    public void displayView(int position) {
        String TAG = HomeFragment.class.getSimpleName();
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                fragment= new HomeFragment();
                title=getString(R.string.title_home);
                TAG = HomeFragment.class.getSimpleName();
                break;
            case 1:
                fragment= new ProfileFragment();
                title=getString(R.string.title_profile);
                TAG = ProfileFragment.class.getSimpleName();
                break;
            case 2:
                fragment=new ChangePasswordFragment();
                title=getString(R.string.title_change_password);
                TAG = ChangePasswordFragment.class.getSimpleName();
                break;
            case 3:
                fragment=new HelpFragment();
                title=getString(R.string.title_about);
                TAG = HelpFragment.class.getSimpleName();
                break;
            case 4: fragment= new LogoutFragment();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            //fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            if(!TAG.equals(HomeFragment.class.getSimpleName())) {
                fragmentTransaction.addToBackStack(null);
            }
            fragmentTransaction.commit();
            //fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
          /*  Intent i = new Intent(Login.this, ForgotPassword.class);
            startActivity(i, options.toBundle());*/
            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        // Update your UI here.
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container_body);
                        if(fragment instanceof  HomeFragment){
                            getSupportActionBar().setTitle("Home");
                        }
                        else if(fragment instanceof ProfileFragment){
                            getSupportActionBar().setTitle("Profile");
                        }
                        else if(fragment instanceof HelpFragment){
                            getSupportActionBar().setTitle("Help");
                        }
                        else if(fragment instanceof ChangePasswordFragment){
                            getSupportActionBar().setTitle("Change Password");
                        }
                    }
                });

    }

    @Override
    public void onStop() {
        super.onStop();
        if(!SecureAppApplication.isApplicationVisible()){
            boolean isImageSelected = prefs.getBoolean(AppConstant.IS_IMAGE_SELECTED,false);
            boolean isAppUsageSet = prefs.getBoolean(AppConstant.IS_APP_USAGE_ACCESS_OPENED,false);
            boolean isPlayStoreOpened =  prefs.getBoolean(AppConstant.IS_OTHER_APP_OPENED,false);
            if(!isAppUsageSet && !isPlayStoreOpened && !isImageSelected) {
                finish();
                SecureAppApplication.logout(this);
            }
            else {
                prefs.edit().putBoolean(AppConstant.IS_APP_USAGE_ACCESS_OPENED,false).commit();
                prefs.edit().putBoolean(AppConstant.IS_IMAGE_SELECTED,false).commit();
                prefs.edit().putBoolean(AppConstant.IS_OTHER_APP_OPENED,false).commit();
            }
        }
    }
    /**
     * Display Toast message
     * @param msg
     */
    private void callToastMessage(String msg){
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_layout));
        SecureUtil.setToastMessage(this,msg,toastLayout);
    }

}