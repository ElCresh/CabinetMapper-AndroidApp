package com.andrea.cabinetmapper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

public class CabinetReportActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Location variables
    private double latitude;
    private double longitude;
    // Intent Data
    private Bundle acvtivity_data;

    // Activity variables
    private Button sendreport;
    private GoogleMap mMap;
    private Snackbar snack;
    private CheckBox telecom,vodafone,fastweb;
    private EditText cabcode,centcode;
    private String android_id;

    // User Data
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commit_cabinet);

        //Set ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Load Graphics Element
        telecom = (CheckBox) findViewById(R.id.telecom);
        vodafone = (CheckBox) findViewById(R.id.vodafone);
        fastweb = (CheckBox) findViewById(R.id.fastweb);
        centcode = (EditText) findViewById(R.id.centcode);
        cabcode = (EditText) findViewById(R.id.cabcode);
        sendreport = (Button) findViewById(R.id.sendreport);
        snack = Snackbar.make(findViewById(R.id.relLay), "", Snackbar.LENGTH_LONG);

        // Get position from intent
        acvtivity_data = getIntent().getExtras();
        latitude = Double.parseDouble(acvtivity_data.getString("latitude"));
        longitude = Double.parseDouble(acvtivity_data.getString("longitude"));

        android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        //Get User Data
        getUser();

        // Loading Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Start Ads
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_app_id));
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void sendReport(View view) {
        int iTelecom, iVodafone, iFastweb;

        if (latitude == 0 && longitude == 0) {
            snack.setText(getString(R.string.wait_gps_fix_report));
            snack.show();
        } else {
            if (!telecom.isChecked() && !vodafone.isChecked() && !fastweb.isChecked()) {
                snack.setText(getString(R.string.select_operator));
                snack.show();
            } else {
                if (cabcode.getText().toString().equals("")) {
                    snack.setText(getString(R.string.insert_code));
                    snack.show();
                } else {
                    if (telecom.isChecked()) {
                        iTelecom = 1;
                    } else {
                        iTelecom = 0;
                    }

                    if (vodafone.isChecked()) {
                        iVodafone = 1;
                    } else {
                        iVodafone = 0;
                    }

                    if (fastweb.isChecked()) {
                        iFastweb = 1;
                    } else {
                        iFastweb = 0;
                    }

                    ReportSender rp = new ReportSender(latitude, longitude, centcode.getText().toString(), cabcode.getText().toString(), iTelecom, iVodafone, iFastweb, this.user.getId());
                    rp.start();
                    try {
                        rp.join();
                    } catch (Exception e) {
                    }

                    // Empty all fields
                    telecom.setChecked(false);
                    vodafone.setChecked(false);
                    fastweb.setChecked(false);
                    cabcode.setText("");
                    centcode.setText("");

                    hideKeyboard();

                    if(rp.getResponse().equals("true")){
                        drawSubmittedMessage();
                    }else{
                        drawSubmitErrorMessage();
                    }
                }
            }
        }
    }

    public void hideKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), 17.0f));
        mMap.getUiSettings().setAllGesturesEnabled(false);

        // Position circle
        Circle currentPosMarker = mMap.addCircle(new CircleOptions()
                .center(new LatLng(latitude, longitude))
                .radius(6f)
                .fillColor(Color.rgb(30,136,229))
                .strokeColor(Color.WHITE)
                .strokeWidth(2f));

    }

    private void drawSubmittedMessage(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.report_send_title))
                .setMessage(getString(R.string.report_send))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private void drawSubmitErrorMessage(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.report_error_title))
                .setMessage(getString(R.string.report_error))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
