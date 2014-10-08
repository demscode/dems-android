package swampthings.dems;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import android.text.format.DateFormat;
import java.util.Calendar;

public class ReminderCreationActivity extends Activity implements View.OnClickListener{

    protected static int reminderHour;
    protected static int reminderMinute;
    protected static int reminderYear;
    protected static int reminderMonth;
    protected static int reminderDay;
    protected static TextView time;
    protected static TextView date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_creation);

        findViewById(R.id.reminder_cancel).setOnClickListener(this);
        findViewById(R.id.reminder_submit).setOnClickListener(this);
        findViewById(R.id.reminder_date).setOnClickListener(this);
        findViewById(R.id.reminder_time).setOnClickListener(this);

        time = (TextView) findViewById(R.id.reminder_time);
        date = (TextView) findViewById(R.id.reminder_date);

        reminderHour = 0;
        reminderMinute = 0;
        time.setText("12:00am");

        final Calendar c = Calendar.getInstance();
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
            System.out.println("HOUR OF DAY = " + reminderHour);
            System.out.println("MINUTE = " + reminderMinute);
        }
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

            String hour = Integer.toString(hourOfDay % 12);
            if (hourOfDay % 12 == 0) {
                hour = "12";
            }

            String meridiem = "am";
            if (hourOfDay > 11 && hourOfDay < 24) {
                meridiem = "pm";
            }

            time.setText(hour + ":" + String.format("%02d",minute) + meridiem);
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
}
