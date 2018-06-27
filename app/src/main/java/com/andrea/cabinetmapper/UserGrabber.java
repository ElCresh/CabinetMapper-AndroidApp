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

public class UserGrabber extends Thread {
    private String response;
    private String dev_id;
    private User user;

    UserGrabber(String dev_id){
        this.dev_id = dev_id;
    }

    public void run(){
        String jsonAPI = "http://cabinetmapper.andreacrescentini.com:8090/user/"+this.dev_id;
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
            user = new User(Integer.parseInt(jsonobject.getString("id")),
                    jsonobject.getString("username"),
                    jsonobject.getString("nome"),
                    jsonobject.getString("cognome"),
                    Integer.parseInt(jsonobject.getString("banned"))
            );
        } else {
            user = null;
        }

    }

    public User getResponse(){
        return user;
    }
}

