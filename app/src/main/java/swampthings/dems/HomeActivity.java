package swampthings.dems;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
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
}
