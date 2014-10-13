package swampthings.dems.test;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

import swampthings.dems.*;

public class HomeActivityTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    HomeActivity activity;
    JSONArray reminders;
    JSONObject reminder1, reminder2;

    public HomeActivityTest() {
        super(HomeActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();

        // Create a JSONArray of reminders
        reminder1 = new JSONObject();
        reminder1.put("id", "123");
        reminder1.put("name", "reminder1");
        reminder1.put("message", "test message");
        reminder1.put("time", (long)1577880000);
        reminder1.put("acknowledgement", "none");
        reminder1.put("createdAt", (long)0);
        reminder1.put("level", 0);

        reminder2 = new JSONObject();
        reminder2.put("id", "456");
        reminder2.put("name", "reminder1");
        reminder2.put("message", "test message");
        reminder2.put("time", (long)1577840400);
        reminder2.put("acknowledgement", "none");
        reminder2.put("createdAt", (long)0);
        reminder2.put("level", 0);

        reminders = new JSONArray();
        reminders.put(reminder1);
        reminders.put(reminder2);
    }

    @SmallTest
    public void testPanicButtonNotNull() {
        Button button = (Button) activity.findViewById(R.id.panic_button);
        assertNotNull("Panic button is null.", button);
    }

    @SmallTest
    public void testCallButtonNotNull() {
        Button button = (Button) activity.findViewById(R.id.call_carer);
        assertNotNull("Call Carer button is null", button);
    }

    @SmallTest
    public void testPatientIDTextViewNotNull() {
        TextView textview = (TextView) activity.findViewById(R.id.PatientID);
        assertNotNull("PatientID textview is null", textview);
    }

    @SmallTest
    public void testSetReminders() throws JSONException {
        activity.UpdateReminders(reminders);
        Set<String> reminderIDs = activity.getReminderIDs();
        assertTrue("Reminder1 not added.", reminderIDs.contains(reminder1.getString("id")));
        assertTrue("Reminder2 not added.", reminderIDs.contains(reminder1.getString("id")));
        activity.RemoveAllAlarms();
    }

    @SmallTest
    public void testCancelReminders() throws JSONException {
        activity.UpdateReminders(reminders);
        activity.RemoveAllAlarms();

        Set<String> reminderIDs = activity.getReminderIDs();
        assertTrue("Reminders not cancelled.", reminderIDs.isEmpty());
    }

}
