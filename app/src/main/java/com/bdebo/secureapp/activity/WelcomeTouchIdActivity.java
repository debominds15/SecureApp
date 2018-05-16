package com.bdebo.secureapp.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bdebo.secureapp.model.FingerprintModule;
import com.bdebo.secureapp.util.AppConstant;
import com.bdebo.secureapp.R;
import com.bdebo.secureapp.model.User;
import com.bdebo.secureapp.dialog.FingerprintAuthenticationDialogFragment;
import com.bdebo.secureapp.util.SecureUtil;
import com.google.gson.Gson;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * This class is used to show Welcome touch id screen
 * and to login by using touch id
 */
public class WelcomeTouchIdActivity extends AppCompatActivity {

    private static String TAG = WelcomeTouchIdActivity.class.getSimpleName();
    private Button touchLogin, noThanks;
    private Toolbar toolbar;
    private static final String DIALOG_FRAGMENT_TAG = "myFragment";
    private static final String SECRET_MESSAGE = "Very secret message";
    private static final String KEY_NAME_NOT_INVALIDATED = "key_not_invalidated";
    public static final String DEFAULT_KEY_NAME = "default_key";
    private TextView authSuccess;
    private SharedPreferences prefs;
    private boolean isSignUpDone;
    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    private SharedPreferences mSharedPreferences;
    private ArrayList<User> userlist;
    private FingerprintAuthenticationDialogFragment fragment;
    private FingerprintManager fingerprintManager;
    private SecureUtil util;
    private KeyguardManager keyguardManager;
    private Cipher defaultCipher;
    private Cipher cipherNotInvalidated;
    private FingerprintModule fingerprintModule;
    private AlertDialog alertDialog;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_touch_id_activity);
        init();

        if (!keyguardManager.isKeyguardSecure()) {
            // Show a message that the user hasn't set up a fingerprint or lock screen.
            callToastMessage("Secure lock screen hasn't set up.\n"
                    + "Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint");
            touchLogin.setEnabled(false);
            //purchaseButtonNotInvalidated.setEnabled(false);
            return;
        }

        // Now the protection level of USE_FINGERPRINT permission is normal instead of dangerous.
        // See http://developer.android.com/reference/android/Manifest.permission.html#USE_FINGERPRINT
        // The line below prevents the false positive inspection from Android Studio
        // noinspection ResourceType
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            touchLogin.setEnabled(false);
            // This happens when no fingerprints are registered.
            return;
        }
        createKey(DEFAULT_KEY_NAME, true);
        createKey(KEY_NAME_NOT_INVALIDATED, false);
        touchLogin.setEnabled(true);
        touchLogin.setOnClickListener(
                new TouchLoginButtonClickListener(defaultCipher, DEFAULT_KEY_NAME));

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
     * Initializes the views
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void init(){
        touchLogin = (Button) this.findViewById(R.id.btnTouchLogin);
        noThanks = (Button) this.findViewById(R.id.btnNoThanks);
        authSuccess = (TextView) this.findViewById(R.id.textAuthSuccess);
        util = new SecureUtil(this);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        fingerprintModule = new FingerprintModule(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        noThanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

            //mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        mKeyStore = fingerprintModule.providesKeystore();
        mKeyGenerator = fingerprintModule.providesKeyGenerator();
        defaultCipher = fingerprintModule.providesCipher();
        cipherNotInvalidated = fingerprintModule.providesCipher();

        mSharedPreferences = fingerprintModule.providesSharedPreferences(this);

        keyguardManager = fingerprintModule.providesKeyguardManager(this);
        fingerprintManager = fingerprintModule.providesFingerprintManager(this);
    }

    /**
     * Initialize the {@link Cipher} instance with the created key in the
     * {@link #createKey(String, boolean)} method.
     *
     * @param keyName the key name to init the cipher
     * @return {@code true} if initialization is successful, {@code false} if the lock screen has
     * been disabled or reset after the key was generated, or if a fingerprint got enrolled after
     * the key was generated.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private boolean initCipher(Cipher cipher, String keyName) {
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(keyName, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    /**
     * Proceed the purchase operation
     *
     * @param withFingerprint {@code true} if the purchase was made by using a fingerprint
     * @param cryptoObject the Crypto object
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void onPurchased(boolean withFingerprint,
                            @Nullable FingerprintManager.CryptoObject cryptoObject) {
        if (withFingerprint) {
            // If the user has authenticated with fingerprint, verify that using cryptography and
            // then show the confirmation message.
            assert cryptoObject != null;
            tryEncrypt(cryptoObject.getCipher());
        } else {
            // Authentication happened with backup password. Just show the confirmation message.
            showConfirmation(null);
        }
    }

    /**
     * Show confirmation, if fingerprint was used show crypto information.
     */
    private void showConfirmation(byte[] encrypted) {
            authSuccess.setVisibility(View.VISIBLE);
            checkSignUpDoneAndTouchLogin();
    }


    /**
     * Check whether sign up is done
     * then touch Login will be activated
     */
    public void checkSignUpDoneAndTouchLogin() {
        SQLiteDatabase.loadLibs(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isSignUpDone = prefs.getBoolean(AppConstant.signUpDonePref, false);
        if (!isSignUpDone) {
            callToastMessage("Please create your account first");
            Intent intent = new Intent(WelcomeTouchIdActivity.this, Register.class);
            startActivity(intent);
        } else {
            userlist = util.getAllUsers();
            Gson gson = new Gson();
            User u = userlist.get(0);
            String json = gson.toJson(u); // myObject - instance of MyObject
            prefs.edit().putString(AppConstant.USER_OBJ, json).commit();

            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            prefs.edit().putString(AppConstant.NAME, u.getName()).commit();
            prefs.edit().putInt(AppConstant.USER_ID, u.getId()).commit();
            startActivity(i);
            callToastMessage("Successfully Login");
        }
    }

    /**
     * Tries to encrypt some data with the generated key in {@link #createKey} which is
     * only works if the user has just authenticated via fingerprint.
     */
    private void tryEncrypt(Cipher cipher) {
        try {
            byte[] encrypted = cipher.doFinal(SECRET_MESSAGE.getBytes());
            showConfirmation(encrypted);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            callToastMessage("Failed to encrypt the data with the generated key. "
                    + "Retry the purchase");
            showPasswordDialog(cipher);
            Log.e(TAG, "Failed to encrypt the data with the generated key." + e.getMessage());
        }
    }

    /**
     * Dialog to show the password
     * @param cipher
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void showPasswordDialog(Cipher cipher){
        fragment = new FingerprintAuthenticationDialogFragment();
        fragment.setCryptoObject(new FingerprintManager.CryptoObject(cipher));

            fragment.setStage(
                    FingerprintAuthenticationDialogFragment.Stage.PASSWORD);

        fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
    }
    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with fingerprint.
     *
     * @param keyName the name of the key to be created
     * @param invalidatedByBiometricEnrollment if {@code false} is passed, the created key will not
     *                                         be invalidated even if a new fingerprint is enrolled.
     *                                         The default value is {@code true}, so passing
     *                                         {@code true} doesn't change the behavior
     *                                         (the key will be invalidated if a new fingerprint is
     *                                         enrolled.). Note that this parameter is only valid if
     *                                         the app works on Android N developer preview.
     *
     */
    @TargetApi(Build.VERSION_CODES.N)
    public void createKey(String keyName, boolean invalidatedByBiometricEnrollment) {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            mKeyStore.load(null);
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder

            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // This is a workaround to avoid crashes on devices whose API level is < 24
            // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
            // visible on API level +24.
            // Ideally there should be a compat library for KeyGenParameterSpec.Builder but
            // which isn't available yet.
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
            }
            mKeyGenerator.init(builder.build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *
     */
    private class TouchLoginButtonClickListener implements View.OnClickListener {

        Cipher mCipher;
        String mKeyName;

        TouchLoginButtonClickListener(Cipher cipher, String keyName) {
            mCipher = cipher;
            mKeyName = keyName;
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {

            // Set up the crypto object for later. The object will be authenticated by use
            // of the fingerprint.
            if (initCipher(mCipher, mKeyName)) {

                // Show the fingerprint dialog. The user has the option to use the fingerprint with
                // crypto, or you can fall back to using a server-side verified password.
                fragment = new FingerprintAuthenticationDialogFragment();
                fragment.setCryptoObject(new FingerprintManager.CryptoObject(mCipher));
                boolean useFingerprintPreference = mSharedPreferences
                        .getBoolean(getString(R.string.use_fingerprint_to_authenticate_key),
                                true);
                boolean newFingerprintRecognized = mSharedPreferences
                        .getBoolean(getString(R.string.new_fingerprint_recognized),
                                false);
                if (useFingerprintPreference && !newFingerprintRecognized) {
                    fragment.setStage(
                            FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT);
                } else {
                    fragment.setStage(
                            FingerprintAuthenticationDialogFragment.Stage.PASSWORD);
                }
                fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            } else {
                // This happens if the lock screen has been disabled or or a fingerprint got
                // enrolled. Thus show the dialog to authenticate with their password first
                // and ask the user if they want to authenticate with fingerprints in the
                // future

                FingerprintAuthenticationDialogFragment fragment
                        = new FingerprintAuthenticationDialogFragment();
                fragment.setCryptoObject(new FingerprintManager.CryptoObject(mCipher));
                fragment.setStage(
                        FingerprintAuthenticationDialogFragment.Stage.NEW_FINGERPRINT_ENROLLED);
                fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        authSuccess.setVisibility(View.GONE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            touchLogin.setEnabled(false);
            showAlertDialog(getResources().getString(R.string.dialog_add_fingerprint_title),getResources().getString(R.string.dialog_add_fingerprint_message),this,false);
        }
        else{
            touchLogin.setEnabled(true);
        }

    }

    /**
     * This method is used to show add fingerprint dialog
     * if there is no fingerprints enrolled in the device
     * @param title
     * @param message
     * @param context
     * @param isCallFromLogin
     */
    public void showAlertDialog(String title, String message, final Context context, final boolean isCallFromLogin)
    {
        int positiveButtonString;
        if(!isCallFromLogin)
            positiveButtonString = R.string.action_continue;
        else
            positiveButtonString = R.string.action_exit;

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(positiveButtonString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        if(!isCallFromLogin) {
                            Intent i = new Intent(context, Login.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(i);
                            finish();
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_action_add_fp, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Intent intent=new Intent(Settings.ACTION_SECURITY_SETTINGS);
                        context.startActivity(intent);
                        finish();
                    }
                })
        ;

        // Create the AlertDialog object and return it
        builder.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                }
                return true;
            }
        });

        alertDialog = builder.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(alertDialog != null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
    }
}
