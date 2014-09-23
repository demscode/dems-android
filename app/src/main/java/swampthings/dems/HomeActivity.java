package swampthings.dems;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
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
import java.util.Calendar;
import java.util.Date;

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

        gps = new GPS(this, patientID);
        if (!gps.canGetLocation()) {
           // ask user to enable location services
            gps.showSettingsAlert();
        }


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

            Calendar calendar = Calendar.getInstance();

            JSONObject reminder = reminders.getJSONObject(i);

            // Retrieve date reminder was set for
            Long timeStamp = reminder.getLong("time");
            Date dateTime = new Date((long)timeStamp*1000);

            // Set alarm time here!
            calendar.setTime(dateTime);

/*            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.YEAR, reminder.getInt("year"));
            calendar.set(Calendar.MONTH, reminder.getInt("month"));
            calendar.set(Calendar.DAY_OF_MONTH, reminder.getInt("day"));
            calendar.set(Calendar.HOUR_OF_DAY, reminder.getInt("hour"));
            calendar.set(Calendar.MINUTE, reminder.getInt("minute"));
            calendar.set(Calendar.SECOND, 0);
 */
            // Intent to run notification
            Intent intent = new Intent(this, Notification.class);
            intent.putExtra("name", reminder.getString("name"));
            intent.putExtra("message", reminder.getString("message"));
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

            // AlarmManager will run the pending intent when date/time comes
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        }
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

    protected class Notification extends Service {

        private String name;
        private String message;

        public Notification() {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                name = extras.getString("name");
                message = extras.getString("message");
            }

            springNotification(name, message);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        public void springNotification(String name, String message) {

            // Prepare intent which is triggered if the notification is selected
            Intent intent = new Intent(this, NotificationReceiverActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

            // Build notification
            // The addAction re-uses the same intent to keep the example short
            android.app.Notification n  = new Builder(this)
                    .setContentTitle(name)
                    .setContentText(message)
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
        }

        @Override
        public boolean onUnbind(Intent intent) {
            return super.onUnbind(intent);
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
                setAlarms(reminders);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
}
