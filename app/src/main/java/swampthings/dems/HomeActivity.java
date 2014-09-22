package swampthings.dems;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;


public class HomeActivity extends Activity implements View.OnClickListener {

    protected String patientID;
    protected GPS gps;
    protected String patientAPIURL = "http://demsweb.herokuapp.com/api/patient/";

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

        gps = new GPS(this);
        if (gps.canGetLocation()) {
           // POST initial location to web service

        } else {
            gps.showSettingsAlert();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        findViewById(R.id.panic_button).setOnClickListener(this);
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
        System.out.println("In onClick");
        if (v.getId() == R.id.panic_button) {
            System.out.println("Panic Button");
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
            System.out.println(location);
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
            StringBuilder builder = new StringBuilder();

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
