package com.nearcompany.linkedin;

import android.content.Context;
import android.os.AsyncTask;

import com.nearcompany.NearCompanyApplication;
import com.nearcompany.model.User;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Emre on 4/19/2020.
 */

public class LinkedinManager {
    private static Context mContext;

    public String CLIENT_ID = "77m9qzss20t5f8";
    public String CLIENT_SECRET = "hS2JWeCN9grc2ThC";
    public String REDIRECT_URI = "https://www.nearcompany.com";
    public String SCOPE = "r_liteprofile%20r_emailaddress";

    public String AUTHURL = "https://www.linkedin.com/oauth/v2/authorization";
    public String TOKENURL = "https://www.linkedin.com/oauth/v2/accessToken";

    private User user = null;

    public static LinkedinManager getInstance(Context pContext) {
        mContext = pContext;
        return new LinkedinManager();
    }

    public String getFullPath() {
        String fullPath = AUTHURL + "?response_type=code&client_id=" + CLIENT_ID + "&scope=" + SCOPE + "&redirect_uri=" + REDIRECT_URI;
        return fullPath;
    }

    public void linkedInRequestForAccessToken(String linkedinCode) {

        new AsyncTaskRunnerLinkedIn(linkedinCode).execute();
    }

    private void fetchLinkedInUserProfile(String accessToken) {

        String tokenURLFull = "https://api.linkedin.com/v2/me?projection=(id,firstName,lastName,profilePicture(displayImage~:playableStreams))&oauth2_access_token=" + accessToken;

        try {
            URL url = new URL(tokenURLFull);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(false);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject object = new JSONObject(response.toString());
                String id = object.getString("id");
                String firstName = object.getJSONObject("firstName").getJSONObject("localized").getString("tr_TR");
                String lastName = object.getJSONObject("lastName").getJSONObject("localized").getString("tr_TR");
                String picture = object.getJSONObject("profilePicture").getJSONObject("displayImage~").getJSONArray("elements").getJSONObject(2).getJSONArray("identifiers").getJSONObject(0).getString("identifier");

                user = new User();
                user.id = id;
                user.name = firstName;
                user.surname = lastName;
                user.token = accessToken;
                user.usrImage = picture;
                user.email = ""; //bir sonraki adımda dolacak.

                fetchLinkedInUserEmail(accessToken, user);
            } else {
                System.out.println("GET request not worked");
            }


        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    private void fetchLinkedInUserEmail(String token, User pUser) {
        String tokenURLFull = "https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))&oauth2_access_token=" + token;

        try {
            URL url = new URL(tokenURLFull);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(false);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject object = new JSONObject(response.toString());
                String email = object.getJSONArray("elements").getJSONObject(0).getJSONObject("handle~").getString("emailAddress");
                pUser.email = email;

                NearCompanyApplication.getInstance().setUser(user);

            } else {
                System.out.println("GET request not worked");
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    private class AsyncTaskRunnerLinkedIn extends AsyncTask<String, String, String> {
        private String linkedinCode;

        public AsyncTaskRunnerLinkedIn(String linkedinCode) {
            this.linkedinCode = linkedinCode;
        }

        @Override
        protected String doInBackground(String... strings) {
            String data = "";

            try {
                String grantType = "authorization_code";
                String postParams = "grant_type=" + grantType + "&code=" + linkedinCode + "&redirect_uri=" + REDIRECT_URI + "&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET;
                URL url = new URL(TOKENURL);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestMethod("POST");
                httpsURLConnection.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded"
                );
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setDoOutput(true);

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpsURLConnection.getOutputStream());
                outputStreamWriter.write(postParams);
                outputStreamWriter.flush();
                outputStreamWriter.close();

                httpsURLConnection.connect();

                int responseCode = httpsURLConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();

                    data = sb.toString();

                    JSONObject jsonObject = new JSONObject(data);
                    String accessToken = jsonObject.getString("access_token");

                    fetchLinkedInUserProfile(accessToken);
                }

            } catch (Exception e) {
                data = e.getMessage();
            }

            return data;
        }
    }
}
