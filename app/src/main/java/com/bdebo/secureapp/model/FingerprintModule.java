package com.bdebo.secureapp.model;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.security.keystore.KeyProperties;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;


/**
 * Dagger module for Fingerprint APIs.
 */
public class FingerprintModule {

    private final Context mContext;

    public FingerprintModule(Context context) {
        mContext = context;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public FingerprintManager providesFingerprintManager(Context context) {
        return context.getSystemService(FingerprintManager.class);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public KeyguardManager providesKeyguardManager(Context context) {
        return context.getSystemService(KeyguardManager.class);
    }

    /**
     * Provides the keystore instance
     * @return keystore instance
     */
    public KeyStore providesKeystore() {
        try {
            return KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get an instance of KeyStore", e);
        }
    }

    /**
     * Provides the KeyGenerator instance
     * @return KeyGenerator instance
     */
    public KeyGenerator providesKeyGenerator() {
        try {
            return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
        }
    }
    /**
     * Provides the Cipher instance
     * @return Cipher instance
     */
    public Cipher providesCipher() {
        try {
            return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get an instance of Cipher", e);
        }
    }

    /**
     * Provides the getDefaultSharedPreferences
     * @return SharedPreferences instance
     */
    public SharedPreferences providesSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
