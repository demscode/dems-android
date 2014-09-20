package swampthings.dems;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification.*;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONObject;

import java.util.Calendar;

public class HomeActivity extends Activity {
    /**  Tracking functionality "borrowed" from Stack Overflow, be sure to take what is needed but
        change variable names etc.
    */
    /*
    public class Tracking extends MapActivity implements LocationListener {

        LocationManager locman;
        LocationListener loclis;
        Location Location;
        private MapView map;

        List<GeoPoint> geoPointsArray = new ArrayList<GeoPoint>();
        private MapController controller;
        String provider = LocationManager.GPS_PROVIDER;
        double lat;
        double lon;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.map);
            initMapView();
            initMyLocation();
            locman = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            // locman.requestLocationUpdates(provider,60000, 100,loclis);
            // Location = locman.getLastKnownLocation(provider);

        }

        // Find and initialize the map view.
        private void initMapView() {
            map = (MapView) findViewById(R.id.map);
            controller = map.getController();
            map.setSatellite(false);
            map.setBuiltInZoomControls(true);
        }

        // Find Current Position on Map.
        private void initMyLocation() {
            final MyLocationOverlay overlay = new MyLocationOverlay(this, map);
            overlay.enableMyLocation();
            overlay.enableCompass(); // does not work in emulator
            overlay.runOnFirstFix(new Runnable() {
                public void run() {
                    // Zoom in to current location
                    controller.setZoom(24);
                    controller.animateTo(overlay.getMyLocation());
                }
            });
            map.getOverlays().add(overlay);
        }

        // --This is the main functionality we need!--
        @Override
        public void onLocationChanged(Location location) {
            if (Location != null) {
                lat = Location.getLatitude();
                lon = Location.getLongitude();
                GeoPoint New_geopoint = new GeoPoint((int) (lat * 1e6),
                        (int) (lon * 1e6));
                controller.animateTo(New_geopoint);

            }

        }
       */

    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Grab the id parsed from LoginActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id = extras.getString("id");
        }
    }

    public void setNotification() {
        // Intent to run notification
        Intent intent = new Intent(this, Notification.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Set alarm time here!
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
/*
        calendar.set(Calendar.YEAR, );
        calendar.set(Calendar.MONTH, );
        calendar.set(Calendar.DAY_OF_MONTH, );
        calendar.set(Calendar.HOUR_OF_DAY, );
        calendar.set(Calendar.MINUTE, );
        calendar.set(Calendar.SECOND, 0);
 */
        // AlarmManager will run the pending intent when date/time comes
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected class Notification extends Service {
        @Override
        public void onCreate() {
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        // Not entirely sure this is correct - can't find api documentation
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            super.onStartCommand(intent, flags, startId);

            // Prepare intent which is triggered if the notification is selected
            intent = new Intent(this, NotificationReceiverActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

            // Build notification
            // The addAction re-uses the same intent to keep the example short
            android.app.Notification n  = new Builder(this)
                    .setContentTitle("Reminder!")
                    .setContentText("Subject")
                    .setSmallIcon(R.drawable.reminder)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .addAction(R.drawable.checkbox_colourful, "Ok", pIntent).build();
            // Extra buttons if needed?
            //        .addAction(R.drawable.alarm, "Sleep", pIntent)
            //        .addAction(R.drawable.ic_launcher, "More", pIntent).build();

            // Run notification
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, n);

            return flags; // ??? - unsure what it wants me to return
        }

        @Override
        public boolean onUnbind(Intent intent) {
            return super.onUnbind(intent);
        }
    }
}
