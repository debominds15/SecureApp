package com.bdebo.secureapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bdebo.secureapp.R;
import com.bdebo.secureapp.util.SecureUtil;

/**This is the base activity for the app which is responsible for
 * checking the timeout constraint for app login.
 */
public class ApplicationActivity extends AppCompatActivity {

    public static final long DISCONNECT_TIMEOUT = 60000; // 1 min = 1 * 60 * 1000 ms
    private Handler disconnectHandler = new Handler(){
        public void handleMessage(Message msg) {
        }
    };

    private Runnable disconnectCallback = new Runnable() {
        @Override
        public void run() {

                // Perform any required operation on disconnect
                callToastMessage("Session out");
                AlertDialog.Builder builder = new AlertDialog.Builder(ApplicationActivity.this);
                builder.setMessage(R.string.dialog_session_out)
                        .setTitle(R.string.dialog_session_title)
                        .setPositiveButton(R.string.dialog_session_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        Intent intent = new Intent(getApplicationContext(), Login.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                }
                        );
                builder.setCancelable(false);
                // Create the AlertDialog object and return it
                builder.show();
        }
    };

    /**
     * This method is used to reset the timer
     */
    public void resetDisconnectTimer(){
        disconnectHandler.removeCallbacks(disconnectCallback);
        disconnectHandler.postDelayed(disconnectCallback, DISCONNECT_TIMEOUT);
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

    /**
     * This method is used to stop the timer and shows session out dialog
     */
    public void stopDisconnectTimer(){
        disconnectHandler.removeCallbacks(disconnectCallback);
    }

    @Override
    public void onUserInteraction(){
        resetDisconnectTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        resetDisconnectTimer();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopDisconnectTimer();
    }
}
