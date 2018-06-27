package com.andrea.cabinetmapper;

public class Cabinet {
    private String descriprion;
    private double latitude;
    private double longitude;
    private int telecom;
    private int vodafone;
    private int fastweb;

    Cabinet(String descriprion, double latitude, double longitude, int telecom, int vodafone, int fastweb){
        this.descriprion = descriprion;
        this.latitude = latitude;
        this.longitude = longitude;
        this.telecom = telecom;
        this.vodafone = vodafone;
        this.fastweb = fastweb;
    }

    public String getDescriprion() {
        return descriprion;
    }

    public void setDescriprion(String descriprion) {
        this.descriprion = descriprion;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getTelecom() {
        return telecom;
    }

    public void setTelecom(int telecom) {
        this.telecom = telecom;
    }

    public int getVodafone() {
        return vodafone;
    }

    public void setVodafone(int vodafone) {
        this.vodafone = vodafone;
    }

    public int getFastweb() {
        return fastweb;
    }

    public void setFastweb(int fastweb) {
        this.fastweb = fastweb;
    }

    public boolean isTelecom(){
        if(this.telecom == 1){
            return true;
        }else{
            return false;
        }
    }

    public boolean isVodafone(){
        if(this.vodafone == 1){
            return true;
        }else{
            return false;
        }
    }

    public boolean isFastweb(){
        if(this.fastweb == 1){
            return true;
        }else{
            return false;
        }
    }
}
