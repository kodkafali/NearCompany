package com.nearcompany.linkedin.platform;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nearcompany.linkedin.platform.errors.LIApiError;
import com.nearcompany.linkedin.platform.errors.LIApiError.ErrorType;
import com.nearcompany.linkedin.platform.internals.BuildConfig;
import com.nearcompany.linkedin.platform.internals.QueueManager;
import com.nearcompany.linkedin.platform.listeners.ApiListener;
import com.nearcompany.linkedin.platform.listeners.ApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to make authenticated REST api calls to retrieve LinkedIn data.
 * The LISession must be properly initialized before using this class.
 *
 * @see <a href="https://developer.linkedin.com/rest">https://developer.linkedin.com/rest</a>
 * for information on type of calls available and the information returned.
 * Data is returned in json format.
 */
public class APIHelper {

    private static final String TAG = APIHelper.class.getName();
    private static final String LOCATION_HEADER = "Location";
    private static final String HTTP_STATUS_CODE = "StatusCode";
    private static final String DATA = "responseData";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_SRC = "x-li-src";
    private static final String HEADER_LI_FORMAT = "x-li-format";
    private static final String HEADER_LI_VER = "x-li-msdk-ver";
    private static final String CONTENT_VALUE = "application/json";
    private static final String HEADER_SRC_VALUE = "msdk";
    private static final String HEADER_LI_FORMAT_VALUE = "json";
    private static final String HEADER_LI_PLFM = "x-li-plfm";
    private static final String HEADER_LI_PLFM_ANDROID = "ANDROID_SDK";

    private static APIHelper apiHelper;

    public static APIHelper getInstance(@NonNull Context ctx) {
        if (APIHelper.apiHelper == null) {
            APIHelper.apiHelper = new APIHelper();
            QueueManager.initQueueManager(ctx);
        }
        return APIHelper.apiHelper;
    }

    private Map<String, String> getLiHeaders(String accessToken) {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(APIHelper.HEADER_CONTENT_TYPE, APIHelper.CONTENT_VALUE);
        headers.put(APIHelper.HEADER_AUTHORIZATION, "Bearer " + accessToken);
        headers.put(APIHelper.HEADER_SRC, APIHelper.HEADER_SRC_VALUE);
        headers.put(APIHelper.HEADER_LI_FORMAT, APIHelper.HEADER_LI_FORMAT_VALUE);
        headers.put(APIHelper.HEADER_LI_VER, BuildConfig.MSDK_VERSION);
        headers.put(APIHelper.HEADER_LI_PLFM, APIHelper.HEADER_LI_PLFM_ANDROID);

        return headers;
    }

    private JsonObjectRequest buildRequest(final String accessToken, int method, String url,
                                           JSONObject body, @Nullable final ApiListener apiListener) {
        return new JsonObjectRequest(method,
                url, body,
                new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (apiListener != null) {
                            apiListener.onApiSuccess(ApiResponse.buildApiResponse(response));
                        }
                    }
                },
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (apiListener != null) {
                            LIApiError liLIApiError = LIApiError.buildLiApiError(error);
                            apiListener.onApiError(liLIApiError);
                        }
                    }
                }
        ) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    JSONObject responseData = new JSONObject();
                    responseData.put(APIHelper.HTTP_STATUS_CODE, response.statusCode);
                    String location = response.headers.get(APIHelper.LOCATION_HEADER);
                    if (!TextUtils.isEmpty(location)) {
                        responseData.put(APIHelper.LOCATION_HEADER, location);
                    }
                    if (response.data != null && response.data.length != 0) {
                        String responseDataAsString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                        responseData.put(APIHelper.DATA, responseDataAsString);

                    }
                    return Response.success(responseData, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }

            @Override
            public Map<String, String> getHeaders() {
                return APIHelper.this.getLiHeaders(accessToken);
            }
        };
    }

    private void request(@NonNull Context context, int method, @NonNull String url, @Nullable JSONObject body, @Nullable ApiListener apiListener) {
        LISession session = LISessionManager.getInstance(context.getApplicationContext()).getSession();
        if (!session.isValid()) {
            if (apiListener != null) {
                apiListener.onApiError(new LIApiError(ErrorType.accessTokenIsNotSet, "access toke is not set", null));
            }
            return;
        }
        JsonObjectRequest jsonObjectRequest = this.buildRequest(session.getAccessToken().getValue(), method, url, body, apiListener);
        jsonObjectRequest.setTag(context == null ? APIHelper.TAG : context);
        QueueManager.getInstance(context).getRequestQueue().add(jsonObjectRequest);
    }

    /**
     * Helper method to make authenticated HTTP requests to LinkedIn REST api using GET Method
     *
     * @param context
     * @param url         rest api endpoint to call. example: "https://api.linkedin.com/v1/people/~:(first-name,last-name,public-profile-url)"
     * @param apiListener
     */
    public void getRequest(@NonNull Context context, String url, ApiListener apiListener) {
        this.request(context, Method.GET, url, null, apiListener);
    }

    /**
     * Helper method to make authenticated HTTP requests to LinkedIn REST api using POST Method
     *
     * @param context
     * @param url
     * @param body
     * @param apiListener
     */
    public void postRequest(@NonNull Context context, String url, JSONObject body, ApiListener apiListener) {
        this.request(context, Method.POST, url, body, apiListener);
    }

    /**
     * Helper method to make authenticated HTTP requests to LinkedIn REST api using POST Method
     *
     * @param context
     * @param url
     * @param body
     * @param apiListener
     */
    public void postRequest(Context context, String url, String body, ApiListener apiListener) {
        try {
            JSONObject bodyObject = body != null ? new JSONObject(body) : null;
            this.postRequest(context, url, bodyObject, apiListener);
        } catch (JSONException e) {
            apiListener.onApiError(new LIApiError("Unable to convert body to json object " + e, e));
        }
    }

    /**
     * Helper method to make authenticated HTTP requests to LinkedIn REST api using PUT Method
     *
     * @param context
     * @param url
     * @param body
     * @param apiListener
     */
    public void putRequest(Context context, String url, JSONObject body, ApiListener apiListener) {
        this.request(context, Method.PUT, url, body, apiListener);
    }

    /**
     * Helper method to make authenticated HTTP requests to LinkedIn REST api using PUT method with
     * string body
     *
     * @param context
     * @param url
     * @param body
     * @param apiListener
     */
    public void putRequest(@NonNull Context context, String url, String body, ApiListener apiListener) {
        try {
            JSONObject bodyObject = body != null ? new JSONObject(body) : null;
            this.putRequest(context, url, bodyObject, apiListener);
        } catch (JSONException e) {
            apiListener.onApiError(new LIApiError("Unable to convert body to json object " + e, e));
        }
    }

    /**
     * Helper method to make authenticated HTTP requests to LinkedIn REST api using DELETE Method
     *
     * @param context
     * @param url
     * @param apiListener
     */
    public void deleteRequest(@NonNull Context context, String url, ApiListener apiListener) {
        this.request(context, Method.DELETE, url, null, apiListener);
    }

    /**
     * cancel any unsent api calls
     *
     * @param context
     */
    public void cancelCalls(@NonNull Context context) {
        QueueManager.getInstance(context).getRequestQueue().cancelAll(context);
    }

}
