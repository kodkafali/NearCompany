package com.nearcompany.ui;

import android.R.integer;
import android.R.layout;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Profile;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.nearcompany.NearCompanyApplication;
import com.nearcompany.R;
import com.nearcompany.R.id;
import com.nearcompany.R.string;
import com.nearcompany.linkedin.LinkedinManager;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_READ_CONTACTS = 0;

    private static final String[] DUMMY_CREDENTIALS = {
            "foo@example.com:hello", "bar@example.com:world"
    };
    Dialog linkedInDialog;
    String linkedInCode = "";
    private LoginActivity.UserLoginTask mAuthTask;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private LinkedinManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_login);

        mManager = LinkedinManager.getInstance(this);

        this.mEmailView = this.findViewById(id.email);
        this.populateAutoComplete();

        this.mPasswordView = this.findViewById(id.password);
        this.mPasswordView.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    LoginActivity.this.attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = this.findViewById(id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(view -> {
            String name = NearCompanyApplication.getInstance().getUser().name;
            Toast.makeText(this, "Linkedin İsim: " + name, Toast.LENGTH_LONG).show();
            LoginActivity.this.attemptLogin();
        });

        Button mLinkedinBtn = this.findViewById(id.linkedinBtn);
        mLinkedinBtn.setOnClickListener(view -> {
            String uri = mManager.getFullPath();
            setupLinkedinWebviewDialog(uri);
        });

        this.mLoginFormView = this.findViewById(id.login_form);
        this.mProgressView = this.findViewById(id.login_progress);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LoginActivity.REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.populateAutoComplete();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                Uri.withAppendedPath(Profile.CONTENT_URI,
                        Contacts.Data.CONTENT_DIRECTORY), LoginActivity.ProfileQuery.PROJECTION,
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(LoginActivity.ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        this.addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    private void attemptLogin() {
        if (this.mAuthTask != null) {
            return;
        }

        this.mEmailView.setError(null);
        this.mPasswordView.setError(null);

        String email = this.mEmailView.getText().toString();
        String password = this.mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !this.isPasswordValid(password)) {
            this.mPasswordView.setError(this.getString(string.error_invalid_password));
            focusView = this.mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            this.mEmailView.setError(this.getString(string.error_field_required));
            focusView = this.mEmailView;
            cancel = true;
        } else if (!this.isEmailValid(email)) {
            this.mEmailView.setError(this.getString(string.error_invalid_email));
            focusView = this.mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            this.showProgress(true);
            this.mAuthTask = new LoginActivity.UserLoginTask(email, password);
            this.mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        layout.simple_dropdown_item_1line, emailAddressCollection);

        this.mEmailView.setAdapter(adapter);
    }

    private void populateAutoComplete() {
        if (!this.mayRequestContacts()) {
            return;
        }

        this.getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (VERSION.SDK_INT < VERSION_CODES.M) {
            return true;
        }
        if (this.checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (this.shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(this.mEmailView, string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(VERSION_CODES.M)
                        public void onClick(View v) {
                            LoginActivity.this.requestPermissions(new String[]{READ_CONTACTS}, LoginActivity.REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            this.requestPermissions(new String[]{READ_CONTACTS}, LoginActivity.REQUEST_READ_CONTACTS);
        }
        return false;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupLinkedinWebviewDialog(String fullPath) {
        linkedInDialog = new Dialog(this);

        WebView webView = new WebView(this);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setWebViewClient(new LinkedinWebviewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(fullPath);

        linkedInDialog.setContentView(webView);
        linkedInDialog.show();
    }

    @TargetApi(VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = this.getResources().getInteger(integer.config_shortAnimTime);

            this.mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            this.mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    LoginActivity.this.mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            this.mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            this.mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    LoginActivity.this.mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            this.mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            this.mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                CommonDataKinds.Email.ADDRESS,
                CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            this.mEmail = email;
            this.mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : LoginActivity.DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(this.mEmail)) {
                    return pieces[1].equals(this.mPassword);
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            LoginActivity.this.mAuthTask = null;
            LoginActivity.this.showProgress(false);

            if (success) {
                LoginActivity.this.finish();
            } else {
                LoginActivity.this.mPasswordView.setError(LoginActivity.this.getString(string.error_incorrect_password));
                LoginActivity.this.mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            LoginActivity.this.mAuthTask = null;
            LoginActivity.this.showProgress(false);
        }
    }

    private class LinkedinWebviewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request.getUrl().toString().startsWith(mManager.REDIRECT_URI)) {
                handleUrL(request.getUrl().toString());
                if (request.getUrl().toString().contains("?code=")) {
                    linkedInDialog.dismiss();
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(mManager.REDIRECT_URI)) {
                handleUrL(url);
                if (url.contains("?code=")) {
                    linkedInDialog.dismiss();
                }
                return true;
            }
            return false;
        }

        private void handleUrL(String url) {
            Uri uri = Uri.parse(url);
            if (url.contains("code")) {
                linkedInCode = uri.getQueryParameter("code");
                mManager.linkedInRequestForAccessToken(linkedInCode);
            } else if (url.contains("error")) {
                Log.e("Error: ", uri.getQueryParameter("error"));
            }
        }
    }
}

