package com.andrea.cabinetmapper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class AuthChecker extends Thread {
    private Boolean response;
    private String android_id;

    AuthChecker(String android_id){
        this.android_id = android_id;
        this.response = false;
    }

    public void run(){
        String jsonAPI = "http://cabinetmapper.andreacrescentini.com:8090/auth/"+android_id;
        URL url;

        try {
            url = new URL(jsonAPI);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            try {
                InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                Scanner s = new Scanner(is).useDelimiter("\\A");
                String rsp = s.hasNext() ? s.next() : "";

                if(rsp.equals("true")){
                    response = true;
                }else{
                    response = false;
                }
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

    public Boolean getResponse(){
        return this.response;
    }
}
