package com.zkc.commandmcu.WebServiceConnection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static com.zkc.commandmcu.SoundPoolUtil.context;


/**
 * this class is used to retrieve json object from the server
 * Created by Sir Sheyi on 26/09/13.
 */

public class HTTPConnection {
    private static InputStream is = null;
    private static JSONObject jObj = null;
    private static String json = "";

    private static String TAG = "JSONRetriever";

    public final static String TAGGING_URL = "https://evapurse-api.azurewebsites.net";
    public final static String LOGIN_URL = "http://www.i-monitors.com/engine/mobile/actions/user-login.php";
    public final static String REGISTER_URL = "http://www.i-monitors.com/engine/mobile/actions/register-user.php";
    public final static String RETRIEVE_PROGRAMS_URL = "http://www.i-monitors.com/engine/mobile/actions/get-programs.php";
    public final static String RETRIEVE_NEWS_URL = "http://www.i-monitors.com/engine/mobile/actions/get-news.php";
    public final static String RETRIEVE_COMMUNITY_URL = "http://www.i-monitors.com/engine/mobile/actions/get-community.php";
    public final static String POST_URL = "http://www.i-monitors.com/engine/mobile/actions/get-post-by-id.php";
    public final static String COMMENTS_URL = "http://www.i-monitors.com/engine/mobile/actions/get-comments-by-post-id.php";
    public final static String CHANGE_PASSWORD_URL = "http://www.i-monitors.com/engine/mobile/actions/get-change-password.php";
    public final static String CREATE_COMMENT = "http://www.i-monitors.com/engine/mobile/actions/create-post-comment.php";
    public final static String NEWPOST_URL = "http://www.i-monitors.com/engine/mobile/actions/create-post.php";
    public final static String LIKE_POST_URL = "http://www.i-monitors.com/engine/mobile/actions/like-comment-post.php";
    public final static String EDIT_POST_URL = "http://www.i-monitors.com/engine/mobile/actions/edit-post.php";


    public String getStringFromURL(String webServiceURL, String urlParameters) {
        Log.d("WebService", "starting the webservice connection");
        String result="";

        try {
            URL url = new URL(webServiceURL);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();

            //add the request headers
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Android");

            //send the POST request
            conn.setDoOutput(true);
            DataOutputStream dataOutputStream = new DataOutputStream(conn.getOutputStream());
            dataOutputStream.writeBytes(urlParameters);
            dataOutputStream.flush();
            dataOutputStream.close();


            if (conn.getResponseCode() != 200) {
                return conn.getResponseMessage() +  ". The response code is " + conn.getResponseCode()  ;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String output;

            System.out.println("Method invoked");

            while ((output = br.readLine()) != null) {
                //System.out.println(output);
                result=output;
            }

            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("WebService", "this was returned from the web service :" + result);
        return result;
    }

    public JSONObject getJSONFromURL(String webServiceURL, String urlParameters) {
        String result = "";
        JSONObject jsonObject = null;

        try {
            URL url = new URL(webServiceURL);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();

            //add the request headers
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Android");

            //send the POST request
            conn.setDoOutput(true);
            DataOutputStream dataOutputStream = new DataOutputStream(conn.getOutputStream());
            dataOutputStream.writeBytes(urlParameters);
            dataOutputStream.flush();
            dataOutputStream.close();

            //check if the response code is okay
            if (conn.getResponseCode() != 200) {
                Log.d("Webservice", ". The response code is " + conn.getResponseCode());
                //return conn.getResponseMessage() + ". The response code is " + conn.getResponseCode();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String output;

            while ((output = br.readLine()) != null) {
                stringBuilder.append(output);
            }

            String stringOutput = stringBuilder.toString();

            jsonObject = new JSONObject(stringOutput);

            conn.disconnect();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        Log.d("WebService", "this was returned from the web service :" + result);

        return jsonObject;
    }


    private static boolean checkConnection(Context context) {
        final ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            Log.e(TAG, "checkConnection - no connection found");
            return false;
        }

        return true;
    }
}