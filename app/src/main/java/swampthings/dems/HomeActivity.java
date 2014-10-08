package swampthings.dems;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class HomeActivity extends Activity implements View.OnClickListener {

    protected String patientID;
    protected GPS gps;
    protected String patientAPIURL = "http://demsweb.herokuapp.com/api/patient/";
    protected String carerPhone = null;
    protected Date currentUpdate;

    private AlarmManager alarmManager;
    private ReminderReceiver broadcastReceiver;
    private Set<String> reminderIDs;
    private Date lastUpdate;

    private static final long UPDATE_INTERVAL = 1000 * 60 * 30; // 30 minutes


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

        // set reminder ids & last update time defaults
        reminderIDs = new HashSet<String>();
        lastUpdate = new Date();
        lastUpdate.setTime(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        findViewById(R.id.panic_button).setOnClickListener(this);
        findViewById(R.id.call_carer).setOnClickListener(this);

        // post the initial location
        if (gps.canGetLocation()) {
            gps.POSTLocation();
        }

        // set up timer task to handle restful updates
        SetUpTimerTask();

    }

    public void SetUpTimerTask() {
        Timer timer;
        TimerTask timerTask;

        timer = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                new ContactRESTful().execute();
                new ReminderRESTful().execute();
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, UPDATE_INTERVAL);
    }

    public void UpdateReminders(JSONArray reminders) throws JSONException {
        for(int i = 0; i < reminders.length(); i++) {

            JSONObject reminder = reminders.getJSONObject(i);

            // get acknowledgement status of reminder
            String acknowledgement;
            try {
                acknowledgement = reminder.getString("acknowledgement");
            } catch (JSONException e) {
                acknowledgement = null;
            }

            // set reminder if it hasn't already been acknowledged
            if (acknowledgement == null || acknowledgement.equals("none")) {

                String id;
                try {
                    id = reminder.getString("id");
                } catch (JSONException e) {
                    id = null;
                }

                if (id != null) {
                    if (!reminderIDs.contains(id)) {
                        //add new reminder
                        reminderIDs.add(id);
                        SetAlarm(reminder);
                    } else {
                        //update existing reminder
                        long lastModified;
                        try {
                            lastModified = reminder.getLong("createdAt");
                        } catch (JSONException e) {
                            lastModified = 0;
                        }

                        // alarm has been modified since last update
                        if (lastModified >= lastUpdate.getTime() || lastModified == 0) {
                            CancelAlarm(id);
                            SetAlarm(reminder);
                        }
                    }
                }
            }
        }

        //remove alarms for deleted reminders
        RemoveDeletedAlarms(reminders);
        lastUpdate = currentUpdate;
    }

    public void SetAlarm(JSONObject reminder) throws JSONException {

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

    private void CancelAlarm(String id) {
        int flag = id.hashCode();

        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, flag, new Intent(intent), PendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);
    }

    private void RemoveDeletedAlarms(JSONArray reminders) {
        HashSet<String> retrieved = new HashSet<String>();
        ArrayList<String> toRemove = new ArrayList<String>();

        for (int i = 0; i < reminders.length(); i++) {
            try {
                JSONObject reminder = reminders.getJSONObject(i);
                retrieved.add(reminder.getString("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (String id : reminderIDs) {
            if (!retrieved.contains(id)) {
                toRemove.add(id);
            }
        }

        for (String id: toRemove) {
            CancelAlarm(id);
            reminderIDs.remove(id);
        }
    }

    public void RemoveAllAlarms() {
        for (String id: reminderIDs) {
            CancelAlarm(id);
        }

        reminderIDs.clear();
    }

    @Override
    protected void onDestroy() {
        RemoveAllAlarms();
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
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
        } else if (v.getId() == R.id.call_carer) {
            //call carer button pressed

            if (carerPhone != null) {
                String url = "tel:" + carerPhone;
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(url));
                this.startActivity(intent);
            } else {
                Toast.makeText(this, "Could not retrieve contact number at this time.", Toast.LENGTH_LONG).show();
            }
        }

    }

    public Set<String> getReminderIDs() {
        return reminderIDs;
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
            currentUpdate = new Date();

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
                    //SetAlarm(reminders);
                    UpdateReminders(reminders);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    /* RESTful API calls background task for getting carer contact details
    * Runs in a separate thread than main activity
    */
    protected class ContactRESTful extends AsyncTask<String, Integer, JSONObject> {

        // Executes doInBackground task first
        @Override
        protected JSONObject doInBackground(String... params) {
            AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
            HttpGet request = new HttpGet(patientAPIURL + patientID + "/contact");
            HttpResponse response;
            boolean success = false;
            JSONObject contactDetails = null;
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

                    contactDetails = new JSONObject(jsonString);
                }

            } catch (Exception e) {
                success = false;
            } finally {
                httpClient.close();
            }

            if (success) {
                return contactDetails;
            } else {
                return null;
            }
        }

        // Tasks result of doInBackground and executes after completion of task
        @Override
        protected void onPostExecute(JSONObject contactDetails) {
            super.onPostExecute(contactDetails);

            // Store carer contact number
            if (contactDetails != null) {
                try {
                    carerPhone = contactDetails.getString("contact_number");
                } catch (Exception e) {
                    carerPhone = null;
                }
            } else {
                carerPhone = null;
            }
        }

    }
}
