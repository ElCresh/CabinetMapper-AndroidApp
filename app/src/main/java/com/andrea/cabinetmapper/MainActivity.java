package com.andrea.cabinetmapper;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final double ANIMATION_MIN_DIFFERENCE = 0.00001;

    // Location variables
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double  longitude = 0,
                    latitude = 0,
                    oLongitude = 0,
                    oLatitude = 0,
                    accuracy = 0;
    private Circle  currentPosMarker = null,
                    currentAccuracy = null;

    // Activity variables
    private ImageButton sendreport;
    private Snackbar snack;
    private GoogleMap mMap;
    private AdView adView;

    private boolean isLocationListening = false;
    private int deviceAuthorized = -1;
    private String android_id;
    private ArrayList<Cabinet> cab;

    //CabinetGrapper Thereads
    private CabinetGrabber cg;

    //User Data
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checking if is needed to show warning on startup
        if(NetworkAnalysis.isNetworkSlow(this))
            drawSlowInternetWarning();

        if (NetworkAnalysis.isWiFiActive(this))
            drawNetworkLocationWarning();

        //drawNewUUIDWarning();

        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //android_id = UUID.randomUUID().toString();
        //Log.v("UUID",android_id);
        android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

        // Load Graphics Element
        sendreport = (ImageButton) findViewById(R.id.sendreport);
        snack = Snackbar.make(findViewById(R.id.relLay), "", Snackbar.LENGTH_LONG);
        adView = (AdView) findViewById(R.id.adView);

        // Loading ads
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_app_id));

        // Loading Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume(){
        super.onResume();

        // Locking report for user not authorized
        if(isAuthorized()){
            sendreport.setVisibility(View.VISIBLE);
        }

        // Reload Ads
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        if(UpdateChecker.isUpdated()) {
            //Check if device is authorized on the site
            if (isAuthorized()) {
                //Get User Data
                getUser();

                if(!user.isBanned()) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    } else {
                        // Loading LocalizationListener
                        createLocationListener();
                    }
                }else{
                    Snackbar.make(findViewById(R.id.relLay), getString(R.string.banned_user), Snackbar.LENGTH_INDEFINITE).show();
                }
            } else {
                Snackbar.make(findViewById(R.id.relLay), getString(R.string.device_not_auth), Snackbar.LENGTH_INDEFINITE).show();
            }
        }else{
            Snackbar.make(findViewById(R.id.relLay), getString(R.string.app_not_update), Snackbar.LENGTH_INDEFINITE).show();
        }

        // Checking Permission on device Android 6+
        checkInteractivePermission();

        // Load Cabinet Data
        loadCabinet();

        hideKeyboard();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.cabinetmap:
                showCabinetMap();
                return true;
            case R.id.settings:
                showSettings();
                return true;
            /* case R.id.savedreport:
                showSavedReport();
                return true; */
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLocationListener();
    }

    private void getUser() {
        UserGrabber ug = new UserGrabber(android_id);
        ug.start();
        try {
            ug.join();
        } catch (Exception e) {
        }

        this.user = ug.getResponse();
    }

    private Boolean isAuthorized() {
        if(deviceAuthorized == -1) {
            AuthChecker ac = new AuthChecker(android_id);
            ac.start();
            try {
                ac.join();
            } catch (Exception e) {
            }

            deviceAuthorized = ac.getResponse() ? 1 : 0;
        }

        return deviceAuthorized == 1;
    }

    private void createLocationListener() {
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                accuracy = location.getAccuracy();

                sendreport.setEnabled(true);

                updateMapMarker();
                if((oLatitude + ANIMATION_MIN_DIFFERENCE) < latitude ||( oLatitude - ANIMATION_MIN_DIFFERENCE) > latitude || (oLongitude + ANIMATION_MIN_DIFFERENCE) < longitude ||( oLongitude - ANIMATION_MIN_DIFFERENCE) > longitude){
                    oLatitude = latitude;
                    oLongitude = longitude;
                    animateCamera();
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {
                snack.setText(getString(R.string.gps_enabled)).show();
                sendreport.setEnabled(false);
            }

            public void onProviderDisabled(String provider) {
                snack.setText(getString(R.string.gps_disabled)).show();
                //appinfo.setText("LAT: NOT AVAILABLE \nLOG: NOT AVAILABLE");
                sendreport.setEnabled(false);
            }
        };

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            snack.setText(getString(R.string.gps_unavailable)).show();
        } else {
            if (NetworkAnalysis.isWiFiActive(this))
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            else
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private void stopLocationListener() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            snack.setText(getString(R.string.gps_unavailable)).show();
        } else {
            if (isLocationListening) {
                // Remove the listener you previously added
                locationManager.removeUpdates(locationListener);
                isLocationListening = true;
            }
        }
    }

    private void checkInteractivePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Request GPS Access
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
                snack.setText(getString(R.string.app_restart)).show();
            }

            // Request Internet Access
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.INTERNET}, 10);
            }

            // Request Network state access
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 10);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.330135,11.8535946), 5.5f));
        mMap.getUiSettings().setAllGesturesEnabled(false);

        waitCabinet();
        drawCabinet();
    }

    private void updateMapMarker() {
        // Accuracy circle
        Circle currentAccuracyNew = mMap.addCircle(new CircleOptions()
                .center(new LatLng(latitude, longitude))
                .radius(accuracy)
                .strokeColor(Color.argb(255, 0, 153, 255))
                .fillColor(Color.argb(30, 0, 153, 255))
                .strokeWidth(2)
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

    private void animateCamera() {
        LatLng position = new LatLng(latitude, longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 17.0f));
    }

    public void showCabinetReport(View view) {
        Intent intent = new Intent(this, CabinetReportActivity.class);
        intent.putExtra("latitude", Double.toString(latitude));
        intent.putExtra("longitude", Double.toString(longitude));

        startActivity(intent);
    }
    public void showCabinetMap() {
        Intent intent = new Intent(this, CabinetMap.class);
        startActivity(intent);
    }

    public void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void showSavedReport() {
        Intent intent = new Intent(this, SavedReport.class);
        startActivity(intent);
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

    public void hideKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void drawNewUUIDWarning(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.new_uuid_title))
                .setMessage(getString(R.string.new_uuid))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void drawSlowInternetWarning(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.slow_internet_title))
                .setMessage(getString(R.string.slow_internet))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void drawNetworkLocationWarning(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.wifi_detected_title))
                .setMessage(getString(R.string.wifi_detected))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}