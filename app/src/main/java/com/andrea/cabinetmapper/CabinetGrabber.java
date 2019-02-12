package com.andrea.cabinetmapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class CabinetGrabber extends Thread {
    private String response;
    private ArrayList<Cabinet> cabinets;

    CabinetGrabber(){
        cabinets = new ArrayList<Cabinet>();
    }

    public void run(){
        String jsonAPI = "http://cabinetmapper.andreacrescentini.com:8090/cabinets/";
        URL url;

        try {
            url = new URL(jsonAPI);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            try {
                InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                Scanner s = new Scanner(is).useDelimiter("\\A");
                response = s.hasNext() ? s.next() : "";
                parseResponse();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseResponse() throws JSONException {
        JSONArray jsonarray = new JSONArray(response);
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            Cabinet cabinet = new Cabinet(jsonobject.getString("description"),
                    Double.parseDouble(jsonobject.getString("latitude")),
                    Double.parseDouble(jsonobject.getString("longitude")),
                    Integer.parseInt(jsonobject.getString("telecom")),
                    Integer.parseInt(jsonobject.getString("vodafone")),
                    Integer.parseInt(jsonobject.getString("fastweb"))
            );

            cabinets.add(cabinet);
        }

    }

    public ArrayList<Cabinet> getResponse(){
        return cabinets;
    }
}
