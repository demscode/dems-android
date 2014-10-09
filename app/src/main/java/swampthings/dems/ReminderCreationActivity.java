package swampthings.dems;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.text.format.DateFormat;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.Date;

public class ReminderCreationActivity extends Activity implements View.OnClickListener{

    protected static int reminderHour;
    protected static int reminderMinute;
    protected static int reminderYear;
    protected static int reminderMonth;
    protected static int reminderDay;
    protected static TextView time;
    protected static TextView date;


    private final String reminderType = "Patient Created";
    protected String patientAPIURL = "http://demsweb.herokuapp.com/api/patient/";
    protected String patientID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_creation);

        try {
            patientID = savedInstanceState.getString("patientID");
        } catch (NullPointerException e) {
            patientID = null;
        }

        findViewById(R.id.reminder_cancel).setOnClickListener(this);
        findViewById(R.id.reminder_submit).setOnClickListener(this);
        findViewById(R.id.reminder_date).setOnClickListener(this);
        findViewById(R.id.reminder_time).setOnClickListener(this);

        time = (TextView) findViewById(R.id.reminder_time);
        date = (TextView) findViewById(R.id.reminder_date);

        // set default time
        final Calendar c = Calendar.getInstance();
        reminderHour = c.get(Calendar.HOUR_OF_DAY);
        reminderMinute = c.get(Calendar.MINUTE);
        time.setText(TimeToString(reminderHour, reminderMinute));

        // set default date
        reminderYear = c.get(Calendar.YEAR);
        reminderMonth = c.get(Calendar.MONTH);
        reminderDay = c.get(Calendar.DAY_OF_MONTH);
        date.setText(reminderDay + "/" + reminderMonth + "/" + reminderYear);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.reminder_cancel) {
            this.finish();
        } else if (v.getId() == R.id.reminder_date) {
            DialogFragment dateFragment = new DatePickerFragment();
            dateFragment.show(getFragmentManager(), "datePicker");
        } else if (v.getId() == R.id.reminder_time) {
            DialogFragment timeFragment = new TimePickerFragment();
            timeFragment.show(getFragmentManager(), "timePicker");
        } else if (v.getId() == R.id.reminder_submit) {
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(reminderYear, reminderMonth, reminderDay, reminderHour, reminderMinute);
            Date date = c.getTime();
            long epochTime = date.getTime();

            EditText nameField = (EditText) findViewById(R.id.reminder_name);
            String title = nameField.getText().toString();

            EditText messageField = (EditText) findViewById(R.id.reminder_name);
            String message = messageField.getText().toString();

            JSONObject reminder = new JSONObject();
            try {
                reminder.put("name", title);
                reminder.put("message", message);
                reminder.put("type", reminderType);
                reminder.put("time", epochTime);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // API call to create reminder
            new CreateNewReminder().execute(reminder);
        }
    }

    public static String TimeToString(int hourOfDay, int minute) {
        String hour = Integer.toString(hourOfDay % 12);
        if (hourOfDay % 12 == 0) {
            hour = "12";
        }

        String meridiem = "am";
        if (hourOfDay > 11 && hourOfDay < 24) {
            meridiem = "pm";
        }

        return (hour + ":" + String.format("%02d",minute) + meridiem);
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Set Reminder Hour and Minute
            reminderHour = hourOfDay;
            reminderMinute = minute;

            time.setText(TimeToString(hourOfDay, minute));
        }
    }


    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            // Set Reminder Year, Month, Day
            reminderYear = year;
            reminderMonth = monthOfYear;
            reminderDay = dayOfMonth;

            date.setText(dayOfMonth + "/" + monthOfYear + "/" + year);
        }
    }


    /* RESTful API calls background task
     * Runs in a separate thread than main activity
     */
    protected class CreateNewReminder extends AsyncTask<JSONObject, Integer, Boolean> {

        // Executes doInBackground task first
        @Override
        protected Boolean doInBackground(JSONObject... params) {
            AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android");
            HttpPost request = new HttpPost(patientAPIURL + patientID + "/reminder");
            HttpResponse response;
            boolean success = false;
            JSONObject reminder = params[0];

            try {

                StringEntity stringEntity = new StringEntity(reminder.toString());

                request.setEntity(stringEntity);
                request.setHeader("Accept", "application/json");
                request.setHeader("Content-type", "application/json");

                response = httpClient.execute(request);

                if (response.getStatusLine().getStatusCode() == 200) {
                    success = true;
                }

            } catch (Exception e) {
                success = false;
            } finally {
                httpClient.close();
            }

            return success;
        }

        // Tasks result of doInBackground and executes after completion of task
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                ReminderCreationActivity.this.finish();
            } else {
                Toast.makeText(ReminderCreationActivity.this, "Could not create the new reminder.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
