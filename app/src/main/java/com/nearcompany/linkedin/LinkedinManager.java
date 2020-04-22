package com.nearcompany.linkedin;

import android.content.Context;

import com.nearcompany.linkedin.platform.APIHelper;
import com.nearcompany.linkedin.platform.errors.LIApiError;
import com.nearcompany.linkedin.platform.listeners.ApiListener;
import com.nearcompany.linkedin.platform.listeners.ApiResponse;

/**
 * Created by Emre on 4/19/2020.
 */

public class LinkedinManager {
    private String url = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name)";
    private String mProfileUri = "https://www.linkedin.com/in/emre-kaplan/";


    public void startLnManager(Context pContext) {
        APIHelper apiHelper = APIHelper.getInstance(pContext);
        apiHelper.getRequest(pContext, url, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse apiResponse) {
                // Success!
            }

            @Override
            public void onApiError(LIApiError liApiError) {
                // Error making GET request!
            }
        });
    }
}
