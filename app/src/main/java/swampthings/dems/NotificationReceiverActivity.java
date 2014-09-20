package swampthings.dems;

import android.app.Activity;
import android.os.Bundle;

public class NotificationReceiverActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tell carer notification was acknowledged
        setContentView(R.layout.activity_home);
    }
}
