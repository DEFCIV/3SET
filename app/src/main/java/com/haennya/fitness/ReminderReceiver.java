package com.haennya.fitness;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class ReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){
        ReminderScheduler.onAlarm(context,intent.getIntExtra(ReminderScheduler.EXTRA_INDEX,-1));
    }
}