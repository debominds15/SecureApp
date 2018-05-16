package com.bdebo.secureapp.dialog;

/**
 * Created by Debojyoti on 21-01-2017.
 */
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.activity.Login;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.activity.Register;
import com.bdebo.secureapp.model.User;
import com.bdebo.secureapp.activity.WelcomeTouchIdActivity;
import com.bdebo.secureapp.helper.FingerprintUIHelper;
import com.bdebo.secureapp.util.SecureUtil;
import com.google.gson.Gson;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;

/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 */
public class FingerprintAuthenticationDialogFragment extends DialogFragment
        implements TextView.OnEditorActionListener, FingerprintUIHelper.Callback {

    private Button mCancelButton;
    private Button mSecondDialogButton;
    private View mFingerprintContent;
    private View mBackupContent;
    private EditText mPassword;
    private CheckBox mUseFingerprintFutureCheckBox;
    private TextView mPasswordDescriptionTextView;
    private TextView mNewFingerprintEnrolledTextView;
    private TextView mWrongPasswordAttempt;

    private Stage mStage = Stage.FINGERPRINT;
    private SharedPreferences prefs;
    private boolean isSignUpDone;

    private FingerprintManager.CryptoObject mCryptoObject;
    private FingerprintUIHelper mFingerprintUIHelper;
    private WelcomeTouchIdActivity mActivity;

    private InputMethodManager mInputMethodManager;
    private SharedPreferences mSharedPreferences;
    private ArrayList<User> userlist;
    private SecureUtil util;
    private int cnt;
    private boolean isErrorCalled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
        cnt = 0;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.log_in));
        View v = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);
        mCancelButton = (Button) v.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mSecondDialogButton = (Button) v.findViewById(R.id.second_dialog_button);
        mSecondDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStage == Stage.FINGERPRINT && !isErrorCalled) {
                    goToBackup();
                } else {
                    verifyPassword();
                }
            }
        });
        mFingerprintContent = v.findViewById(R.id.fingerprint_container);
        mBackupContent = v.findViewById(R.id.backup_container);
        mPassword = (EditText) v.findViewById(R.id.password);
        mWrongPasswordAttempt = (TextView) v.findViewById(R.id.textIncorrectAttemptBioFp);
        mPassword.setOnEditorActionListener(this);
        mPasswordDescriptionTextView = (TextView) v.findViewById(R.id.password_description);
        mUseFingerprintFutureCheckBox = (CheckBox)
                v.findViewById(R.id.use_fingerprint_in_future_check);
        mNewFingerprintEnrolledTextView = (TextView)
                v.findViewById(R.id.new_fingerprint_enrolled_description);
        mFingerprintUIHelper = new FingerprintUIHelper(
                mActivity.getSystemService(FingerprintManager.class),
                (ImageView) v.findViewById(R.id.fingerprint_icon),
                (TextView) v.findViewById(R.id.fingerprint_status), this);

        util = new SecureUtil(getActivity());
        updateStage();

        // If fingerprint authentication is not available, switch immediately to the backup
        // (password) screen.
        if (!mFingerprintUIHelper.isFingerprintAuthAvailable()) {
            goToBackup();
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mStage == Stage.FINGERPRINT) {
            mFingerprintUIHelper.startListening(mCryptoObject);
        }
    }

    public void setStage(Stage stage) {
        mStage = stage;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mFingerprintUIHelper != null)
        mFingerprintUIHelper.stopListening();
        dismiss();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (WelcomeTouchIdActivity) activity;
        mInputMethodManager = mActivity.getSystemService(InputMethodManager.class);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
    }

    /**
     * Sets the crypto object to be passed in when authenticating with fingerprint.
     */
    public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }

    /**
     * Switches to backup (password) screen. This either can happen when fingerprint is not
     * available or the user chooses to use the password authentication method by pressing the
     * button. This can also happen when the user had too many fingerprint attempts.
     */
    private void goToBackup() {
        mFingerprintUIHelper.stopListening();

        Intent intent=new Intent(getActivity(),Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        dismiss();
        // Fingerprint is not used anymore. Stop listening for it.

    }

    private void goToBackupBioUnavailable() {
        mFingerprintUIHelper.stopListening();
        mCancelButton.setText(R.string.cancel);
        mSecondDialogButton.setText(R.string.ok);
        mFingerprintContent.setVisibility(View.GONE);
        mBackupContent.setVisibility(View.VISIBLE);
        mPasswordDescriptionTextView.setText(R.string.dialog_fingerprint_unavailable);

    }
    /**
     * Checks whether the current entered password is correct, and dismisses the the dialog and
     * let's the activity know about the result.
     */
    private void verifyPassword() {

        if (!checkPassword(mPassword.getText().toString())) {

            if(mPassword.getText().toString().equals(""))
                mPassword.setError("Password cannot be empty");
            else {
                mPassword.setText("");
                mWrongPasswordAttempt.setVisibility(View.VISIBLE);
                mWrongPasswordAttempt.setTextColor(Color.RED);
                cnt++;
                int attemptsLeft = 3 - cnt;
                if (attemptsLeft > 1 && attemptsLeft < 3)
                    mWrongPasswordAttempt.setText("Wrong password " + attemptsLeft + " attempts left");
                else if (attemptsLeft == 1) {
                    mWrongPasswordAttempt.setText("Last Attempt left");
                } else if (attemptsLeft == 0) {
                    dismiss();
                }
            }
            return;
        }
        if (mStage == Stage.NEW_FINGERPRINT_ENROLLED) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(getString(R.string.use_fingerprint_to_authenticate_key),
                    mUseFingerprintFutureCheckBox.isChecked());
            editor.apply();

            if (mUseFingerprintFutureCheckBox.isChecked()) {
                // Re-create the key so that fingerprints including new ones are validated.
                mActivity.createKey(WelcomeTouchIdActivity.DEFAULT_KEY_NAME, true);
                mStage = Stage.FINGERPRINT;
            }
        }
        mPassword.setText("");
        mActivity.onPurchased(false , null);
        dismiss();
    }

    /**
     * Check whether the password is correct or not
     * @return true if {@code password} is correct, false otherwise
     */
    private boolean checkPassword(String password) {
        // Assume the password is always correct.
        // In the real world situation, the password needs to be verified in the server side.
        SQLiteDatabase.loadLibs(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isSignUpDone = prefs.getBoolean(AppConstant.signUpDonePref, false);
        if (!isSignUpDone) {
            callToastMessage("Please sign up first");
            Intent intent = new Intent(getActivity(), Register.class);
            startActivity(intent);
        } else {
            userlist = util.getAllUsers();
            Gson gson = new Gson();
            User u = userlist.get(0);
            if (password.equals(u.getPass())) {
                //  mActivity.onPurchased(true , mCryptoObject);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean(getString(R.string.new_fingerprint_recognized),
                        false);
                editor.apply();
                return true;
            }
        }
        return false;
    }
    private final Runnable mShowKeyboardRunnable = new Runnable() {
        @Override
        public void run() {
            mInputMethodManager.showSoftInput(mPassword, 0);
        }
    };

    /**
     * Update the stage and accordingly set the UI
     * for the fragment dialog
     */
    private void updateStage() {
        switch (mStage) {
            case FINGERPRINT:
                mCancelButton.setText(R.string.cancel);
                mSecondDialogButton.setText(R.string.use_password);
                mFingerprintContent.setVisibility(View.VISIBLE);
                mBackupContent.setVisibility(View.GONE);
                break;
            case NEW_FINGERPRINT_ENROLLED:
                // Intentional fall through
                callToastMessage("New finger print has been added");
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean(getString(R.string.new_fingerprint_recognized),
                        true);
                editor.apply();
            case PASSWORD:
                mCancelButton.setText(R.string.cancel);
                mSecondDialogButton.setText(R.string.ok);
                mFingerprintContent.setVisibility(View.GONE);
                mBackupContent.setVisibility(View.VISIBLE);
                editor = mSharedPreferences.edit();
                editor.putBoolean(getString(R.string.new_fingerprint_recognized),
                        true);
                editor.apply();
                if (mStage == Stage.NEW_FINGERPRINT_ENROLLED) {
                    mPasswordDescriptionTextView.setVisibility(View.GONE);
                    mNewFingerprintEnrolledTextView.setVisibility(View.VISIBLE);
                    mUseFingerprintFutureCheckBox.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            verifyPassword();
            return true;
        }
        return false;
    }

    @Override
    public void onAuthenticated() {
        // Callback from FingerprintUiHelper. Let the activity know that authentication was
        // successful.
        mActivity.onPurchased(true /* withFingerprint */, mCryptoObject);
        dismiss();
    }

    @Override
    public void onError() {
        isErrorCalled = true;
        goToBackupBioUnavailable();
    }

    /**
     * Enumeration to indicate which authentication method the user is trying to authenticate with.
     */
    public enum Stage {
        FINGERPRINT,
        NEW_FINGERPRINT_ENROLLED,
        PASSWORD
    }

    /**
     * Display Toast message
     * @param msg
     */
    private void callToastMessage(String msg){
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) mActivity.findViewById(R.id.custom_toast_layout));
        SecureUtil.setToastMessage(mActivity,msg,toastLayout);
    }
}