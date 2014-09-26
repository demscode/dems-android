package swampthings.dems;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ReminderReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        //display reminder
        Intent reminder = new Intent(context, ReminderDisplayActivity.class);
        reminder.putExtras(bundle);
        reminder.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(reminder);
    }
}
