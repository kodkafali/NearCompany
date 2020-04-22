package com.nearcompany.utility;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.content.ContentValues.TAG;

/**
 * Created by Emre on 4/23/2020.
 */

public class Utility {

    private static Context mContext;

    public static Utility getInstance(Context pContext) {
        mContext = pContext;
        return new Utility();
    }

    public void generateHashkey() {
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo("com.nearcompany", PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());

                Log.d("Hask key", Base64.encodeToString(md.digest(), Base64.NO_WRAP));

            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, e.getMessage(), e);
        }
    }
}
