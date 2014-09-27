package swampthings.dems;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class HomeActivity extends Activity implements View.OnClickListener {

    protected String patientID;
    protected GPS gps;
    protected String patientAPIURL = "http://demsweb.herokuapp.com/api/patient/";

    private AlarmManager alarmManager;
    private ReminderReceiver broadcastReceiver;


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

        // set up GPS service
        gps = new GPS(this, patientID);
        if (!gps.canGetLocation()) {
           // ask user to enable location services
            gps.showSettingsAlert();
        }

        // setup reminder/alarm services
        broadcastReceiver = new ReminderReceiver();
        registerReceiver(broadcastReceiver, new IntentFilter("swampthings.dems"));
        alarmManager = (AlarmManager)(this.getSystemService(Context.ALARM_SERVICE));

    }

    @Override
    protected void onStart() {
        super.onStart();
        findViewById(R.id.panic_button).setOnClickListener(this);

        // post the initial location
        if (gps.canGetLocation()) {
            gps.POSTLocation();
        }

        new ReminderRESTful().execute();
    }


    public void setAlarms(JSONArray reminders) throws JSONException {
        for(int i = 0; i < reminders.length(); i++) {

            JSONObject reminder = reminders.getJSONObject(i);

            // get status of reminder
            String status;
            try {
                status = reminder.getString("status");
            } catch (JSONException e) {
                status = null;
            }

            // set reminder if it hasn't already been acknowledged
            if (status == null || status.equals("unknown")) {

                long timeStamp = reminder.getLong("time");
                String message = reminder.getString("message");
                String title = reminder.getString("name");
                String id = reminder.getString("id");
                int idHash = id.hashCode();

                Intent intent = new Intent(this, ReminderReceiver.class);
                intent.putExtra("message", message);
                intent.putExtra("title", title);
                intent.putExtra("id", id);
                intent.putExtra("time", timeStamp);
                intent.putExtra("patientID", patientID);

                alarmManager.set(AlarmManager.RTC_WAKEUP, timeStamp, PendingIntent.getBroadcast(this, idHash, new Intent(intent), 0));
            }
        }
    }

    @Override
    protected void onDestroy() {
        //alarmManager.cancel(pendingIntent);
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
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

    /* RESTful API calls background task for getting reminders
    * Runs in a separate thread than main activity
    */
    protected class ReminderRESTful extends AsyncTask<String, Integer, JSONArray> {

        // Executes doInBackground task first
        @Override
        protected JSONArray doInBackground(String... params) {
            AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
            HttpGet request = new HttpGet(patientAPIURL + patientID + "/reminder");
            HttpResponse response;
            boolean success = false;
            JSONArray reminders = null;
            StringBuilder builder = new StringBuilder();

            try {
                response = httpClient.execute(request);


                if (response.getStatusLine().getStatusCode() == 200) {
                    success = true;

                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    String jsonString = builder.toString();

                    reminders = new JSONArray(jsonString);
                }

            } catch (Exception e) {
                success = false;
            } finally {
                httpClient.close();
            }

            if (success) {
                return reminders;
            } else {
                return null;
            }
        }

        // Tasks result of doInBackground and executes after completion of task
        @Override
        protected void onPostExecute(JSONArray reminders) {
            super.onPostExecute(reminders);

            // Do something with the JSONArray of reminders here..
            try {
                if (reminders != null) {
                    setAlarms(reminders);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
}
