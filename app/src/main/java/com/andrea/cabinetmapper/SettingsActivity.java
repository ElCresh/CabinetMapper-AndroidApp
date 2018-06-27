package com.andrea.cabinetmapper;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class SettingsActivity extends AppCompatActivity {
    private TextView    deviceid,
                        name,
                        username,
                        network_class_tv,
                        verInfo;
    private String      android_id,
                        network_class;
    private Toast       toast;
    private User        user;
    private int         deviceAuthorized = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        deviceid = (TextView) findViewById(R.id.deviceid);
        name = (TextView) findViewById(R.id.name);
        username = (TextView) findViewById(R.id.username);
        network_class_tv = (TextView) findViewById(R.id.network_class);
        verInfo = (TextView) findViewById(R.id.verInfo);

        // Get deatils to fill TextView
        //android_id = UUID.randomUUID().toString();
        android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        network_class = NetworkAnalysis.getClassOfMobileConnection(this);

        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG);

        // Fill TextView
        deviceid.setText("Device ID: "+android_id);
        network_class_tv.setText("Network: "+network_class);
        try {
            verInfo.setText("ver "+this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName+
                                "-b"+this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode+
                                " ["+getString(R.string.app_version)+"]");
        } catch (PackageManager.NameNotFoundException e) {
            verInfo.setText(e.toString());
        }

        if (isAuthorized()) {
            //Get User Data
            getUser();

            username.setText(getString(R.string.username)+": "+user.getUsername());
            name.setText(getString(R.string.name)+": "+user.getNome()+" "+user.getCognome());
        }
    }

    public void copyAdroidIdOnClipboard(View v){
        ClipboardManager clipboard = (ClipboardManager)   getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied", android_id);
        if (clipboard != null)
            clipboard.setPrimaryClip(clip);

        toast.setText("Device ID copiato negli appunti");
        toast.show();
    }

    private void getUser() {
        UserGrabber ug = new UserGrabber(android_id);
        ug.start();
        try {
            ug.join();
        } catch (Exception e) {
            e.printStackTrace();
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
}
