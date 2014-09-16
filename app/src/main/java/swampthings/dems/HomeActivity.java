package swampthings.dems;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

public class HomeActivity extends Activity {
    /**  Tracking functionality "borrowed" from Stack Overflow, be sure to take what is needed but
        change variable names etc.
    */
    /*
    public class Tracking extends MapActivity implements LocationListener {
>>>>>>> Basic notification implementation.


public class HomeActivity extends Activity implements View.OnClickListener {

    protected String patientID;
    protected GPS gps;
    protected String patientAPIURL = "http://demsweb.herokuapp.com/api/patient/";//"http://demsweb.herokuapp.com/api/patient/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Grab the patient id passed from the LoginActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            patientID = extras.getString("id");
            TextView pid = (TextView) findViewById(R.id.PatientID);
            pid.setText("PatientID: " + patientID);
        }

        gps = new GPS(this, patientID);
        if (!gps.canGetLocation()) {
           // ask user to enable location services
            gps.showSettingsAlert();
        }


    }

<<<<<<< HEAD
    @Override
    protected void onStart() {
        super.onStart();
        findViewById(R.id.panic_button).setOnClickListener(this);

        // post the intial location
        if (gps.canGetLocation()) {
            gps.POSTLocation();
        }
=======
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

    public void notification() {
        // Prepare intent which is triggered if the notification is selected
        Intent intent = new Intent(this, NotificationReceiverActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Build notification
        // The addAction re-uses the same intent to keep the example short
        Notification n  = new Notification.Builder(this)
                .setContentTitle("Reminder!")
                .setContentText("Subject")
                .setSmallIcon(R.drawable.reminder)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.checkbox_colourful, "Ok", pIntent)
                .addAction(R.drawable.alarm, "Sleep", pIntent)
                .addAction(R.drawable.ic_launcher, "More", pIntent).build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);
>>>>>>> Basic notification implementation.
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.panic_button) {
            //panic button pressed

            //get location
            gps.getLocation();
            JSONObject location = new JSONObject();
            try {
                location.put("latitude", gps.getLatitude());
                location.put("longitude", gps.getLongitude());
            } catch (Exception e) {
                // JSON error
            }

            // send panic through restful
            new PanicRESTful().execute(location);
        }

    }


    /* RESTful API calls background task for panic button
     * Runs in a separate thread than main activity
     */
    protected class PanicRESTful extends AsyncTask<JSONObject, Integer, Boolean> {

        // Executes doInBackground task first
        @Override
        protected Boolean doInBackground(JSONObject... locations) {
            AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
            HttpPost request = new HttpPost(patientAPIURL + patientID + "/panic");
            HttpResponse response;
            boolean success = false;

            try {

                StringEntity stringEntity = new StringEntity(locations[0].toString());

                request.setEntity(stringEntity);
                request.setHeader("Accept", "application/json");
                request.setHeader("Content-type", "application/json");

                response = httpClient.execute(request);

                if (response.getStatusLine().getStatusCode() == 200) {
                    success = true;
                } else {
                    success = false;
                }


            } catch (Exception e) {
                success =  false;
            } finally {
                httpClient.close();
            }

            return success;
        }

        // Tasks result of doInBackground and executes after completion of task
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

        }

    }

}
