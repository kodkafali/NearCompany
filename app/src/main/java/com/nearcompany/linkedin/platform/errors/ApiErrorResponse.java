/*
    Copyright 2014 LinkedIn Corp.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.nearcompany.linkedin.platform.errors;

import org.json.JSONException;
import org.json.JSONObject;

public class ApiErrorResponse {

    public static final String ERROR_CODE = "errorCode";
    public static final String MESSAGE = "message";
    public static final String REQUEST_ID = "requestId";
    public static final String STATUS = "status";
    public static final String TIMESTAMP = "timestamp";
    private static final String TAG = ApiErrorResponse.class.getName();
    public final int errorCode;
    public final String message;
    public final String requestId;
    public final int status;
    public final long timestamp;
    private final JSONObject jsonApiErrorResponse;

    private ApiErrorResponse(JSONObject jsonApiErrorResponse, int errorCode, String message, String requestId, int status, long timestamp) {
        this.jsonApiErrorResponse = jsonApiErrorResponse;
        this.errorCode = errorCode;
        this.message = message;
        this.requestId = requestId;
        this.status = status;
        this.timestamp = timestamp;
    }

    public static ApiErrorResponse build(byte[] apiErrorResponseData) throws JSONException {
        return ApiErrorResponse.build(new JSONObject(new String(apiErrorResponseData)));

    }

    public static ApiErrorResponse build(JSONObject jsonErr) {
        return new ApiErrorResponse(jsonErr, jsonErr.optInt(ApiErrorResponse.ERROR_CODE, -1), jsonErr.optString(ApiErrorResponse.MESSAGE),
                jsonErr.optString(ApiErrorResponse.REQUEST_ID), jsonErr.optInt(ApiErrorResponse.STATUS, -1), jsonErr.optLong(ApiErrorResponse.TIMESTAMP, 0));
    }

    public int getErrorCode() {
        return this.jsonApiErrorResponse.optInt(ApiErrorResponse.ERROR_CODE, -1);
    }

    public String getMessage() {
        return this.jsonApiErrorResponse.optString(ApiErrorResponse.MESSAGE);
    }

    public String getRequestId() {
        return this.jsonApiErrorResponse.optString(ApiErrorResponse.REQUEST_ID);
    }

    public int getStatus() {
        return this.jsonApiErrorResponse.optInt(ApiErrorResponse.STATUS, -1);
    }

    public long getTimestamp() {
        return this.jsonApiErrorResponse.optLong(ApiErrorResponse.TIMESTAMP, 0);
    }

    @Override
    public String toString() {
        try {
            return this.jsonApiErrorResponse.toString(2);
        } catch (JSONException e) {
        }
        return null;
    }
}

