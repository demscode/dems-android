package com.example.cbrad24.mapexample2;

import android.os.AsyncTask;
import android.os.Handler;
import android.view.MenuItem;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Cbrad24 on 7/09/2014.
 */
public class Restful extends AsyncTask<String, Void, String> {
    double latitude = 0;
    double longitude = 0;
    MenuItem sendMenuItem;

    public void setLatLong(double lat, double lng) {
        latitude = lat;
        longitude = lng;
    }
    public void setMenuItem(MenuItem menuitem) {
        sendMenuItem = menuitem;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    @Override
    protected String doInBackground(String... urls) {
        InputStream inputStream = null;
        String result = "";
        try {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(urls[0]);

            JSONObject json = new JSONObject();
            String jsonstr = "";
            try {
                json.put("latitude",  latitude);
                json.put("longitude", longitude);
                json.put("patient_id", 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            jsonstr = json.toString();
            StringEntity poststr = new StringEntity(jsonstr);

            httppost.setEntity(poststr);
            httppost.setHeader("Accept", "application/json");
            httppost.setHeader("Content-type", "application/json");

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            inputStream = response.getEntity().getContent();
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

            return result;
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            System.out.println(result);
        }
        //Toast.makeText( getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        sendMenuItem.setTitle("Sent!");

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 2000ms
                sendMenuItem.setTitle("Send Location");
            }
        }, 2000);
    }
}
