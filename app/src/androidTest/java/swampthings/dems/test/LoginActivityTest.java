package swampthings.dems.test;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import com.google.android.gms.common.SignInButton;

import swampthings.dems.*;

public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    LoginActivity activity;

    public LoginActivityTest() {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
    }

    @SmallTest
    public void testSignInButtonNotNull() {
        SignInButton button = (SignInButton) activity.findViewById(R.id.sign_in_button);
        assertNotNull("Sign in button is null.", button);
    }
}
