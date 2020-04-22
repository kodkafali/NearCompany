package com.nearcompany.linkedin.platform;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.nearcompany.linkedin.platform.errors.LIAppErrorCode;
import com.nearcompany.linkedin.platform.errors.LIAuthError;
import com.nearcompany.linkedin.platform.internals.AppStore;
import com.nearcompany.linkedin.platform.internals.LIAppVersion;
import com.nearcompany.linkedin.platform.listeners.AuthListener;
import com.nearcompany.linkedin.platform.utils.Scope;

import java.util.List;

public class LISessionManager {

    private static final String TAG = LISessionManager.class.getSimpleName();
    private static final int LI_SDK_AUTH_REQUEST_CODE = 3672;
    private static final String AUTH_TOKEN = "token";
    private static final String AUTH_STATE = "state";
    private static final String LI_APP_PACKAGE_NAME = "com.linkedin.android";
    private static final String LI_APP_AUTH_CLASS_NAME = "com.linkedin.android.liauthlib.thirdparty.LiThirdPartyAuthorizeActivity";
    private static final String SCOPE_DATA = "com.linkedin.thirdpartysdk.SCOPE_DATA";
    private static final String LI_APP_ACTION_AUTHORIZE_APP = "com.linkedin.android.auth.AUTHORIZE_APP";
    private static final String LI_APP_CATEGORY = "com.linkedin.android.auth.thirdparty.authorize";
    private static final String LI_ERROR_INFO = "com.linkedin.thirdparty.authorize.RESULT_ACTION_ERROR_INFO";
    private static final String LI_ERROR_DESCRIPTION = "com.linkedin.thirdparty.authorize.RESULT_ACTION_ERROR_DESCRIPTION";

    private static LISessionManager sessionManager;

    private Context ctx;
    private LISessionImpl session;
    private AuthListener authListener;

    private LISessionManager() {
        this.session = new LISessionImpl();
    }

    public static LISessionManager getInstance(@NonNull Context context) {
        if (sessionManager == null) {
            sessionManager = new LISessionManager();
        }
        if (context != null && sessionManager.ctx == null) {
            sessionManager.ctx = context.getApplicationContext();
        }
        return sessionManager;
    }

    /**
     * Builds scope based on List of permissions.
     *
     * @param perms
     * @return
     */
    private static String createScope(List<String> perms) {
        if (perms == null || perms.isEmpty()) {
            return "";
        }
        return TextUtils.join(" ", perms);
    }

    /**
     * Initializes LISession using previously obtained AccessToken
     * The passed in access token should be one that was obtained from the LinkedIn Mobile SDK.
     *
     * @param accessToken access token
     */
    public void init(AccessToken accessToken) {
        session.setAccessToken(accessToken);
    }

    public void init(Activity activity,
                     Scope scope, AuthListener callback, boolean showGoToAppStoreDialog) {
        //check if LI
        if (!LIAppVersion.isLIAppCurrent(ctx)) {
            AppStore.goAppStore(activity, showGoToAppStoreDialog);
            return;
        }
        authListener = callback;
        Intent i = new Intent();
        i.setClassName(LI_APP_PACKAGE_NAME, LI_APP_AUTH_CLASS_NAME);
        i.putExtra(SCOPE_DATA, scope.createScope());
        i.setAction(LI_APP_ACTION_AUTHORIZE_APP);
        i.addCategory(LI_APP_CATEGORY);
        try {
            activity.startActivityForResult(i, LI_SDK_AUTH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    /**
     * This method must be called in the calling Activity's onActivityResult in order to
     * process the response to
     * {@link com.linkedin.platform.LISessionManager#init(android.app.Activity, com.linkedin.platform.utils.Scope, com.linkedin.platform.listeners.AuthListener, boolean)}
     *
     * @param activity
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        // set access token
        if (authListener != null && requestCode == LI_SDK_AUTH_REQUEST_CODE) {
            // got result
            if (resultCode == Activity.RESULT_OK) {
                String token = data.getStringExtra(AUTH_TOKEN);
                long expiresOn = data.getLongExtra("expiresOn", 0L);
                AccessToken accessToken = new AccessToken(token, expiresOn);
                init(accessToken);
                // call the callback with the
                authListener.onAuthSuccess();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                authListener.onAuthError(new LIAuthError(LIAppErrorCode.USER_CANCELLED, "user canceled"));
            } else {
                String errorInfo = data.getStringExtra(LI_ERROR_INFO);
                String errorDesc = data.getStringExtra(LI_ERROR_DESCRIPTION);
                authListener.onAuthError(new LIAuthError(errorInfo, errorDesc));
            }
            authListener = null;
        }
    }

    /**
     * @return the LISession
     */
    public LISession getSession() {
        return session;
    }

    /**
     * Clears the session.  Calls to retrieve LinkedIn data or to view profiles will no longer
     * work.
     */
    public void clearSession() {
        session.setAccessToken(null);
    }

    /**
     * private implementation of LISession
     * takes are of saving and restoring session to/from shared preferences
     */
    private static class LISessionImpl implements LISession {

        private static final String LI_SDK_SHARED_PREF_STORE = "li_shared_pref_store";
        private static final String SHARED_PREFERENCES_ACCESS_TOKEN = "li_sdk_access_token";
        private AccessToken accessToken = null;

        public LISessionImpl() {
        }

        @Override
        public AccessToken getAccessToken() {
            if (accessToken == null) {
                recover();
            }
            return accessToken;
        }

        void setAccessToken(@Nullable AccessToken accessToken) {
            this.accessToken = accessToken;
            save();
        }

        /**
         * @return true if a valid accessToken is present.  Note that if the member revokes
         * access to this application, this will still return true
         */
        @Override
        public boolean isValid() {
            AccessToken at = getAccessToken();
            return at != null && !at.isExpired();
        }

        /**
         * clears session. (Kills it)
         */
        public void clear() {
            setAccessToken(null);
        }

        /**
         * Storage
         */
        private SharedPreferences getSharedPref() {
            return LISessionManager.sessionManager.ctx.getSharedPreferences(LI_SDK_SHARED_PREF_STORE, Context.MODE_PRIVATE);
        }

        private void save() {
            SharedPreferences.Editor edit = getSharedPref().edit();
            edit.putString(SHARED_PREFERENCES_ACCESS_TOKEN, accessToken == null ? null : accessToken.toString());
            edit.commit();
        }

        private void recover() {
            SharedPreferences sharedPref = getSharedPref();
            String accessTokenStr = sharedPref.getString(SHARED_PREFERENCES_ACCESS_TOKEN, null);
            accessToken = accessTokenStr == null ? null : AccessToken.buildAccessToken(accessTokenStr);
        }
    }


}
