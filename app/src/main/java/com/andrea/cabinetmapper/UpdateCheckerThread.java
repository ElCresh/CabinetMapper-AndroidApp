package com.andrea.cabinetmapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class UpdateCheckerThread extends Thread {
    private String response;

    public UpdateCheckerThread(){}

    public void run(){
        String jsonAPI = "http://cabinetmapper.andreacrescentini.com:8090/version/android";
        URL url;

        try {
            url = new URL(jsonAPI);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            try {
                InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                Scanner s = new Scanner(is).useDelimiter("\\A");
                response = s.hasNext() ? s.next() : "";
                parseResponse();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseResponse() throws JSONException {
        if(!response.equals("false")) {
            JSONObject jsonobject = new JSONObject(response);
            response = jsonobject.getString("version");
        }
    }

    public String getResponse(){
        return this.response;
    }
}