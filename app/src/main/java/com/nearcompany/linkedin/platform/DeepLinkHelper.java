package com.nearcompany.linkedin.platform;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri.Builder;
import android.support.annotation.NonNull;

import com.nearcompany.linkedin.platform.errors.LIAppErrorCode;
import com.nearcompany.linkedin.platform.errors.LIDeepLinkError;
import com.nearcompany.linkedin.platform.internals.AppStore;
import com.nearcompany.linkedin.platform.internals.LIAppVersion;
import com.nearcompany.linkedin.platform.listeners.DeepLinkListener;


/**
 * DeepLinkHelper enables linking to pages within the LinkedIn application
 */
public class DeepLinkHelper {

    public static final int LI_SDK_CROSSLINK_REQUEST_CODE = 13287;
    private static final String TAG = DeepLinkHelper.class.getName();
    private static final String CURRENTLY_LOGGED_IN_MEMBER = "you";
    private static final String DEEPLINK_ERROR_CODE_EXTRA_NAME = "com.linkedin.thirdparty.deeplink.EXTRA_ERROR_CODE";
    private static final String DEEPLINK_ERROR_MESSAGE_EXTRA_NAME = "com.linkedin.thirdparty.deeplink.EXTRA_ERROR_MESSAGE";
    private static DeepLinkHelper deepLinkHelper;
    private DeepLinkListener deepLinkListener;

    public static DeepLinkHelper getInstance() {
        if (DeepLinkHelper.deepLinkHelper == null) {
            DeepLinkHelper.deepLinkHelper = new DeepLinkHelper();
        }
        return DeepLinkHelper.deepLinkHelper;
    }

    /**
     * opens up a view which shows the profile of the user that is currently logged in to the
     * LinkedIn app.
     *
     * @param activity
     * @param callback
     */
    public void openCurrentProfile(@NonNull Activity activity, DeepLinkListener callback) {
        this.openOtherProfile(activity, DeepLinkHelper.CURRENTLY_LOGGED_IN_MEMBER, callback);
    }

    /**
     * opens a view which shows the profile of the given member
     *
     * @param activity
     * @param memberId obtained through an api call
     * @param callback
     */
    public void openOtherProfile(@NonNull Activity activity, String memberId, DeepLinkListener callback) {
        deepLinkListener = callback;

        LISession session = LISessionManager.getInstance(activity.getApplicationContext()).getSession();
        if (!session.isValid()) {
            callback.onDeepLinkError(new LIDeepLinkError(LIAppErrorCode.NOT_AUTHENTICATED, "there is no access token"));
            return;
        }
        try {
            if (!LIAppVersion.isLIAppCurrent(activity)) {
                AppStore.goAppStore(activity, true);
                return;
            }
            this.deepLinkToProfile(activity, memberId, session.getAccessToken());
        } catch (ActivityNotFoundException e) {
            callback.onDeepLinkError(new LIDeepLinkError(LIAppErrorCode.LINKEDIN_APP_NOT_FOUND,
                    "LinkedIn app needs to be either installed or` updated"));
            this.deepLinkListener = null;
        }
    }

    private void deepLinkToProfile(@NonNull Activity activity, String memberId, @NonNull AccessToken accessToken) {
        Intent i = new Intent("android.intent.action.VIEW");
        Builder uriBuilder = new Builder();
        uriBuilder.scheme("linkedin");
        if (DeepLinkHelper.CURRENTLY_LOGGED_IN_MEMBER.equals(memberId)) {
            uriBuilder.authority(DeepLinkHelper.CURRENTLY_LOGGED_IN_MEMBER);
        } else {
            uriBuilder.authority("profile").appendPath(memberId);
        }
        uriBuilder.appendQueryParameter("accessToken", accessToken.getValue());
        uriBuilder.appendQueryParameter("src", "sdk");
        i.setData(uriBuilder.build());
        activity.startActivityForResult(i, DeepLinkHelper.LI_SDK_CROSSLINK_REQUEST_CODE);
    }

    /**
     * call this method in your activity's onActivityResult method.
     * Handles any response code from LinkedIn and calls the DeepLinkListener callback
     *
     * @param activity
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == DeepLinkHelper.LI_SDK_CROSSLINK_REQUEST_CODE && this.deepLinkListener != null) {
            if (resultCode == Activity.RESULT_OK) {
                this.deepLinkListener.onDeepLinkSuccess();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (data == null || data.getExtras() == null) {
                    this.deepLinkListener.onDeepLinkError(new LIDeepLinkError(LIAppErrorCode.USER_CANCELLED, ""));
                } else {
                    String errorMessage = data.getExtras().getString(DeepLinkHelper.DEEPLINK_ERROR_MESSAGE_EXTRA_NAME);
                    String errorCode = data.getExtras().getString(DeepLinkHelper.DEEPLINK_ERROR_CODE_EXTRA_NAME);
                    this.deepLinkListener.onDeepLinkError(new LIDeepLinkError(errorCode, errorMessage));
                }
            }
        }
    }

}
