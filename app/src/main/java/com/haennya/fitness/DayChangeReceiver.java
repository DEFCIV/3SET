package com.haennya.fitness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Restore the neutral icon at the start of a new calendar day. */
public final class DayChangeReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){
        LauncherIconManager.refresh(context);
    }
}
