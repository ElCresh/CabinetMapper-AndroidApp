package com.andrea.cabinetmapper;

import android.util.Log;
import android.util.Pair;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ReportSender extends Thread {
    private double latitude,longitude;
    private int telecom,vodafone,fastweb;
    private String response, centrale, description;
    private int userid;
    private String master_key;

    public ReportSender(double latitude,double longitude,String centrale,String description,int telecom,int vodafone, int fastweb, int userid){
        this.latitude = latitude;
        this.longitude = longitude;
        this.centrale = centrale;
        this.description = description;
        this.telecom = telecom;
        this.vodafone = vodafone;
        this.fastweb = fastweb;
        this.userid = userid;
        this.master_key = "";
    }

    public void run() {
        String jsonAPI = "http://cabinetmapper.andreacrescentini.com:8090/add/" + this.master_key + "?descrizione="+this.description+"&centrale="+this.centrale+"&latitude="+this.latitude+"&longitude="+this.longitude+"&telecom="+this.telecom+"&fastweb="+this.fastweb+"&vodafone="+this.vodafone+"&uuid="+this.userid;
        URL url;
        StringBuilder req = new StringBuilder();

        try {
            url = new URL(jsonAPI);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            try {
                InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                Scanner s = new Scanner(is).useDelimiter("\\A");
                response = s.hasNext() ? s.next() : "";
            } catch (IOException e) {
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
    public String getResponse(){
        return this.response;
    }
}
