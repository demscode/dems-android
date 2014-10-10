package swampthings.dems.test;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.SmallTest;


import swampthings.dems.*;

public class ReminderDisplayActivityTest extends ActivityInstrumentationTestCase2<ReminderDisplayActivity>{

    ReminderDisplayActivity activity;

    public ReminderDisplayActivityTest() {
        super(ReminderDisplayActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Intent intent = new Intent();
        intent.putExtra("title", "Reminder Title");
        intent.putExtra("message", "Reminder Message");
        setActivityIntent(intent);
        activity = getActivity();
    }

    @SmallTest
    public void testAlertNotNull() {
        AlertDialog alert = activity.getAlert();
        assertNotNull("Alert is null.", alert);
        alert.dismiss();
    }

    @SmallTest
    public void testAlertIsShown() {
        AlertDialog alert = activity.getAlert();
        assertTrue("Alert not displayed.", alert.isShowing());
        alert.dismiss();
    }

    @UiThreadTest
    public void testAlertCloses() {
        AlertDialog alert = activity.getAlert();
        alert.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        alert.dismiss();
        assertFalse("Alert not closed.", alert.isShowing());
    }

}
