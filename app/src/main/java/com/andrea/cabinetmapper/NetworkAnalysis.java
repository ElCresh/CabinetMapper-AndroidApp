package com.andrea.cabinetmapper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public final class NetworkAnalysis {

    public static boolean isNetworkFast(Context context){
        boolean fast = false;
        // Get details from context
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        try {
            // Check if VERIFYING_POOR_LINK is detected
            if (!cm.getActiveNetworkInfo().getDetailedState().toString().equals(NetworkInfo.DetailedState.VERIFYING_POOR_LINK))
                fast = true;
        }catch(NullPointerException e){}

        if(NetworkAnalysis.getClassOfMobileConnection(context).equals("2G"))
            fast = false;

        return fast;
    }
    public static boolean isNetworkSlow(Context context){
        return !NetworkAnalysis.isNetworkFast(context);
    }

    public static String getClassOfMobileConnection(Context context){
        // Get details from context
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        String class_conn = "UNK";

        try {
            if (ni.getType() == ConnectivityManager.TYPE_WIFI)
                class_conn = "WIFI";
            else if (ni.getType() == ConnectivityManager.TYPE_MOBILE) {
                int networkType = ni.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        class_conn = "2G";
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        class_conn = "3G";
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        class_conn = "4G";
                }
            }
        }catch(NullPointerException e){}

        return class_conn;
    }

    public static boolean isWiFiActive(Context context){
        // Get details from context
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        boolean active = false;

        try {
            if (ni.getType() == ConnectivityManager.TYPE_WIFI)
                active = true;
        }catch(NullPointerException e){}

        return active;
    }
}
