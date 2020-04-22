package com.nearcompany.linkedin.platform.errors;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class LIAuthError {

    private static final String TAG = LIAuthError.class.getName();

    private final LIAppErrorCode errorCode;
    private final String errorMsg;

    public LIAuthError(String errorInfo, String errorMsg) {
        LIAppErrorCode liAuthErrorCode = LIAppErrorCode.findErrorCode(errorInfo);
        this.errorCode = liAuthErrorCode;
        this.errorMsg = errorMsg;
    }

    public LIAuthError(LIAppErrorCode errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("errorCode", this.errorCode.name());
            jsonObject.put("errorMessage", this.errorMsg);
            return jsonObject.toString(2);
        } catch (JSONException e) {
            Log.d(LIAuthError.TAG, e.getMessage());
        }
        return null;
    }


}
