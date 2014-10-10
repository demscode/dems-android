package swampthings.dems.test;

import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;

import junit.framework.TestSuite;


public class Runner extends InstrumentationTestRunner {

    @Override
    public TestSuite getAllTests() {
        InstrumentationTestSuite suite = new InstrumentationTestSuite(this);
        suite.addTestSuite(HomeActivityTest.class);
        suite.addTestSuite(LoginActivityTest.class);
        suite.addTestSuite(ReminderDisplayActivityTest.class);
        suite.addTestSuite(ReminderCreationActivityTest.class);
        return suite;
    }

    @Override
    public ClassLoader getLoader() {
        return Runner.class.getClassLoader();
    }
}

