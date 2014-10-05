package swampthings.dems.test;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Button;
import android.widget.TextView;

import swampthings.dems.*;

public class HomeActivityTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    HomeActivity activity;

    public HomeActivityTest() {
        super(HomeActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
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

}
