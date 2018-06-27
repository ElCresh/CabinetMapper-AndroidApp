package com.andrea.cabinetmapper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class CabinetMap extends AppCompatActivity implements OnMapReadyCallback {
    private ArrayList<Cabinet> cab;
    private GoogleMap mMap;
    private Circle  currentPosMarker = null,
                    currentAccuracy = null;

    // Location variables
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double  longitude = 0,
                    latitude = 0,
                    accuracy = 0;

    private boolean isZommed = false;
    private boolean isLocationActive = false;
    private boolean isLocationUpdated = false;

    //CabinetGrapper Theread
    private CabinetGrabber cg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cabinet_map);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //Set ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Loading Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapCab);
        mapFragment.getMapAsync(this);

        loadCabinet();

        createLocationListener();
        isLocationActive = true;

        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.warning))
            .setMessage(getString(R.string.point_warning))
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();

        // Start Ads
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_app_id));
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.330135,11.8535946), 5.5f));

        waitCabinet();
        drawCabinet();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_fixgps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.fixgps:
                if(!isLocationActive){
                    //never
                }else if(isLocationUpdated){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.0f));
                }else{
                    Snackbar.make(findViewById(R.id.relLay), getString(R.string.wait_gps_fix), Snackbar.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createLocationListener() {
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                accuracy = location.getAccuracy();

                updateMapMarker();

                isLocationUpdated = true;

                if(!isZommed) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.0f));
                    isZommed = true;
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        } else {
            if (NetworkAnalysis.isWiFiActive(this))
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            else
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private void stopLocationListener() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        } else {
            // Remove the listener you previously added
            locationManager.removeUpdates(locationListener);
        }
    }

    private void drawCabinet(){
        LatLng position;
        String snp = "";

        for(Cabinet c : cab) {
            if (c.isTelecom())
                snp = concatIsp(snp, getString(R.string.telecom));

            if (c.isVodafone())
                snp = concatIsp(snp, getString(R.string.vodafone));

            if (c.isFastweb())
                snp = concatIsp(snp, getString(R.string.fastweb));

            position = new LatLng(c.getLatitude(), c.getLongitude());
            mMap.addMarker(new MarkerOptions().position(position).title(c.getDescriprion()).snippet(snp).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            snp = "";
        }
    }

    private String concatIsp(String snp, String isp) {
        if (snp.equals(""))
            return isp;
        return snp + " | " + isp;
    }

    private void loadCabinet(){
        cg = new CabinetGrabber();
        cg.start();
    }

    private void waitCabinet(){
        try {
            cg.join();
        } catch (Exception e) {
            Log.w("cabinetmapper", "unable to get cabinet");
        }

        cab = cg.getResponse();
    }

    private void updateMapMarker() {
        // Accuracy circle
        Circle currentAccuracyNew = mMap.addCircle(new CircleOptions()
                .center(new LatLng(latitude, longitude))
                .radius(accuracy)
                .strokeColor(Color.argb(255, 0, 153, 255))
                .fillColor(Color.argb(30, 0, 153, 255)).strokeWidth(2)
        );

        // Position circle
        Circle currentPosMarkerNew = mMap.addCircle(new CircleOptions()
                .center(new LatLng(latitude, longitude))
                .radius(6f)
                .fillColor(Color.rgb(30,136,229))
                .strokeColor(Color.WHITE)
                .strokeWidth(0f)
        );

        if (currentPosMarker != null) {
            currentPosMarker.remove();
            currentAccuracy.remove();
            currentPosMarker = null;
            currentAccuracy = null;
        }
        currentPosMarker = currentPosMarkerNew;
        currentAccuracy = currentAccuracyNew;
    }
}

