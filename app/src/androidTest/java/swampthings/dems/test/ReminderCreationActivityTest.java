package swampthings.dems.test;

import android.app.DialogFragment;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import swampthings.dems.R;
import swampthings.dems.ReminderCreationActivity;

public class ReminderCreationActivityTest extends ActivityInstrumentationTestCase2<ReminderCreationActivity>{

    ReminderCreationActivity activity;

    public ReminderCreationActivityTest() {
        super(ReminderCreationActivity.class);
    }

    @Override
    protected  void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
    }

    @SmallTest
    public void testNameEditTextNotNull() {
        EditText editText = (EditText) activity.findViewById(R.id.reminder_name);
        assertNotNull("Name EditText is null.", editText);
    }

    @SmallTest
    public void testMessageEditTextNotNull() {
        EditText editText = (EditText) activity.findViewById(R.id.reminder_message);
        assertNotNull("Message EditText is null.", editText);
    }

    @SmallTest
    public void testCancelButtonNotNull() {
        Button button = (Button) activity.findViewById(R.id.reminder_cancel);
        assertNotNull("Cancel Button is null.", button);
    }

    @SmallTest
    public void testSubmitButtonNotNull() {
        Button button = (Button) activity.findViewById(R.id.reminder_submit);
        assertNotNull("Submit Button is null.", button);
    }

    @SmallTest
    public void testDateTextViewNotNull() {
        TextView textView = (TextView) activity.findViewById(R.id.reminder_date);
        assertNotNull("Date TextView is null.", textView);
    }

    @SmallTest
    public void testTimeTextViewNotNull() {
        TextView textView = (TextView) activity.findViewById(R.id.reminder_time);
        assertNotNull("Time TextView is null.", textView);
    }

    @UiThreadTest
    public void testCancelButtonFinishesActivity() {
        activity.onClick(activity.findViewById(R.id.reminder_cancel));
        assertTrue("Cancel button didn't finish activity.", activity.isFinishing());
    }

    @SmallTest
    public void testDateFragmentCanBeCreated() {
        DialogFragment dateFragment = new ReminderCreationActivity.DatePickerFragment();
        assertNotNull("Date Fragment failed to be created.", dateFragment);
    }

    @SmallTest
    public void testDateFragmentIsVisible() {
        DialogFragment dateFragment = new ReminderCreationActivity.DatePickerFragment();
        dateFragment.show(activity.getFragmentManager(), "datePicker");

        assertTrue("Date Fragment is not visible.", dateFragment.getShowsDialog());
    }

    @SmallTest
    public void testTimeFragmentCanBeCreated() {
        DialogFragment timeFragment = new ReminderCreationActivity.TimePickerFragment();
        assertNotNull("Time Fragment failed to be created.", timeFragment);
    }

    @SmallTest
    public void testTimeFragmentIsVisible() {
        DialogFragment timeFragment = new ReminderCreationActivity.TimePickerFragment();
        timeFragment.show(activity.getFragmentManager(), "timePicker");

        assertTrue("Time Fragment is not visible.", timeFragment.getShowsDialog());
    }
}
