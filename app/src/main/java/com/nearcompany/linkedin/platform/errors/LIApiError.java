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

import com.android.volley.VolleyError;

import org.json.JSONException;

public class LIApiError extends Exception {

    private VolleyError volleyError;
    private int httpStatusCode = -1;
    private ApiErrorResponse apiErrorResponse;
    private LIApiError.ErrorType errorType;

    public LIApiError(String detailMessage, Throwable throwable) {
        this(LIApiError.ErrorType.other, detailMessage, throwable);
    }

    public LIApiError(LIApiError.ErrorType errorType, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.errorType = errorType;
    }

    public LIApiError(VolleyError volleyError) {
        super(volleyError.getMessage(), volleyError.fillInStackTrace());
        this.volleyError = volleyError;
        if (volleyError.networkResponse != null) {
            httpStatusCode = volleyError.networkResponse.statusCode;
            try {
                apiErrorResponse = ApiErrorResponse.build(volleyError.networkResponse.data);
                errorType = LIApiError.ErrorType.apiErrorResponse;
            } catch (JSONException e) {
                errorType = LIApiError.ErrorType.other;
            }
        }
    }

    public static LIApiError buildLiApiError(VolleyError volleyError) {
        return new LIApiError(volleyError);
    }

    public ApiErrorResponse getApiErrorResponse() {
        return apiErrorResponse;
    }

    public LIApiError.ErrorType getErrorType() {
        return this.errorType;
    }

    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }

    @Override
    public String toString() {
        return this.apiErrorResponse == null ? "exceptionMsg: " + super.getMessage() : this.apiErrorResponse.toString();
    }

    public enum ErrorType {
        accessTokenIsNotSet,
        apiErrorResponse,
        other
    }

}
