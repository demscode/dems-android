package swampthings.dems;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ReminderReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        //display reminder
        Toast.makeText(context, bundle.getString("title"), Toast.LENGTH_LONG).show();
    }
}
