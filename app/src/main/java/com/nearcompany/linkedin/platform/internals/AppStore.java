package com.nearcompany.linkedin.platform.internals;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

public class AppStore {

    public static void goAppStore(final Activity activity, boolean showDialog) {
        if (!showDialog) {
            AppStore.goToAppStore(activity);
            return;
        }
        Builder builder = new Builder(activity);
        builder.setMessage("update message")
                .setTitle("app title");
        builder.setPositiveButton("update_linkedin_app_download", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AppStore.goToAppStore(activity);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("update_cancel_download", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private static void goToAppStore(Activity activity) {
        AppStore.SupportedAppStore appStore = AppStore.SupportedAppStore.fromDeviceManufacturer();
        String appStoreUri = appStore.getAppStoreUri();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(appStoreUri));
        try {
            activity.startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            //should not happen
        }
    }

    private static enum SupportedAppStore {
        amazonAppstore("amazon", "amzn://apps/android?p=com.linkedin.android"),
        googlePlay("google", "market://details?id=com.linkedin.android"),
        samsungApps("samsung", "samsungapps://ProductDetail/com.linkedin.android");

        private final String deviceManufacturer;
        private final String appStoreUri;

        private SupportedAppStore(String deviceManufacturer, String appStoreUri) {
            this.deviceManufacturer = deviceManufacturer;
            this.appStoreUri = appStoreUri;
        }

        public static AppStore.SupportedAppStore fromDeviceManufacturer() {
            for (AppStore.SupportedAppStore appStore : AppStore.SupportedAppStore.values()) {
                if (appStore.getDeviceManufacturer().equalsIgnoreCase(Build.MANUFACTURER)) {
                    return appStore;
                }
            }
            //return google play by default
            return AppStore.SupportedAppStore.googlePlay;
        }

        public String getDeviceManufacturer() {
            return deviceManufacturer;
        }

        public String getAppStoreUri() {
            return appStoreUri;
        }
    }

}
