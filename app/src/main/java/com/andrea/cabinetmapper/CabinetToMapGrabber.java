package com.andrea.cabinetmapper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CabinetToMapGrabber extends Thread {
    private String response;
    private ArrayList<CabinetToMap> cab;

    public CabinetToMapGrabber(){
        cab = new ArrayList<CabinetToMap>();
    }

    public void run(){
        String dataUrl = "https://cabinetmapper.andreacrescentini.com/api-v1.php?request=cabinettomap";
        String dataUrlParameters = "";
        URL url;
        HttpURLConnection connection = null;
        try {
            // Create connection
            url = new URL(dataUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length","" + Integer.toString(dataUrlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            // Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(dataUrlParameters);
            wr.flush();
            wr.close();
            // Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer rsp = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                rsp.append(line);
                rsp.append('\r');
            }
            rd.close();
            response = rsp.toString();
        } catch (Exception e) {
            response = e.toString();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        parseResponse();
    }

    public void parseResponse(){
        String str[];
        String str2[];
        CabinetToMap cbn;
        int i;

        str = response.split("§");

        for(i=0;i<str.length-1;i++){
            if(str[i]!=""){
                str2 = str[i].split(",");
                cbn = new CabinetToMap(Integer.parseInt(str2[0]),Double.parseDouble(str2[1]),Double.parseDouble(str2[2]));
                cab.add(cbn);
            }
        }

    }

    public ArrayList<CabinetToMap> getResponse(){
        return cab;
    }
}
