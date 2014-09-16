package com.example.cbrad24.mapexample2;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MyActivity extends Activity {
    GPS gps;
    GoogleMap map;
    TextView loctxt;
    Marker patient;
    Menu menuitem;
    MenuItem sendMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        gps = new GPS(MyActivity.this);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        loctxt = (TextView) findViewById(R.id.header);

        if (gps.canGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            LatLng position = new LatLng(latitude, longitude);
            patient = map.addMarker(new MarkerOptions().position(position).title("Patient"));
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(position, 16);
            map.animateCamera( update );
            loctxt.setText( "Latitude:\t\t" +  latitude  + "\nLongitude:\t"+ longitude );
        } else {
            gps.showSettingsAlert();
        }
    }

    public void onLocationUpdate() {
        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();
        LatLng position = new LatLng(latitude, longitude);
        patient.setPosition(position);
        loctxt.setText( "Latitude:\t\t" +  latitude  + "\nLongitude:\t"+ longitude );
    }

    public void locate_onClick(View v) {
        gps.getLocation();
        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();
        LatLng position = new LatLng(latitude, longitude);
        patient.setPosition(position);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(position, 16);
        map.animateCamera( update );
        loctxt.setText( "Latitude:\t\t" +  latitude  + "\nLongitude:\t"+ longitude );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        menuitem = menu;
        sendMenuItem = menu.findItem(R.id.action_send);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_send) {
            gps.getLocation();
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            Restful restful = new Restful();
            //restful.setLatLong(-27.479178, 153.022059);
            restful.setLatLong(latitude, longitude);
            restful.setMenuItem(sendMenuItem);
            restful.execute("http://demsweb.herokuapp.com/api/patient/1/locations");

            sendMenuItem.setTitle("Sending...");

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
