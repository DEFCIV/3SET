package com.haennya.fitness;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import org.json.JSONObject;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;

/** Update the real Android launcher icon based on today's 3SET completion. */
public final class LauncherIconManager {
    private static final String TAG="3SET";
    private static final int DAY_RESET_REQUEST=41231;
    private static final String DEFAULT_ALIAS=".LauncherDefault";
    private static final String DONE_ALIAS=".LauncherDone";
    private LauncherIconManager(){}

    public static synchronized void refresh(Context context){
        Context c=context.getApplicationContext();
        try {
            SharedPreferences prefs=c.getSharedPreferences("haennya-state",Context.MODE_PRIVATE);
            updateIcon(c,isCompletedToday(prefs.getString("data","")));
        } catch(Exception e){ Log.w(TAG,"Could not refresh launcher icon",e); }
        scheduleDayReset(c);
    }

    public static synchronized void updateFromJson(Context context,String json){
        Context c=context.getApplicationContext();
        try{updateIcon(c,isCompletedToday(json));}
        catch(Exception e){Log.w(TAG,"Could not update launcher icon",e);}
        scheduleDayReset(c);
    }

    private static boolean isCompletedToday(String json){
        if(json==null||json.isEmpty())return false;
        try{
            JSONObject days=new JSONObject(json).optJSONObject("days");
            return days!=null&&days.optInt(LocalDate.now().toString(),0)>=3;
        }catch(Exception ignored){return false;}
    }

    private static void updateIcon(Context context,boolean completed){
        PackageManager pm=context.getPackageManager();
        ComponentName normal=new ComponentName(context,context.getPackageName()+DEFAULT_ALIAS);
        ComponentName done=new ComponentName(context,context.getPackageName()+DONE_ALIAS);
        int currentlyNormal=pm.getComponentEnabledSetting(normal);
        int currentlyDone=pm.getComponentEnabledSetting(done);
        boolean normalOn=currentlyNormal==PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            || currentlyNormal==PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        boolean doneOn=currentlyDone==PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        if(normalOn==!completed && doneOn==completed)return;

        int flags=PackageManager.DONT_KILL_APP;
        int on=PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        int off=PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        if(Build.VERSION.SDK_INT>=33){
            pm.setComponentEnabledSettings(Arrays.asList(
                new PackageManager.ComponentEnabledSetting(normal,completed?off:on,flags),
                new PackageManager.ComponentEnabledSetting(done,completed?on:off,flags)
            ));
        }else{
            // Keep at least one launcher alias active during the transition.
            pm.setComponentEnabledSetting(completed?done:normal,on,flags);
            pm.setComponentEnabledSetting(completed?normal:done,off,flags);
        }
    }

    private static void scheduleDayReset(Context context){
        Intent intent=new Intent(context,DayChangeReceiver.class);
        PendingIntent pending=PendingIntent.getBroadcast(context,DAY_RESET_REQUEST,intent,
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Calendar next=Calendar.getInstance();
        next.add(Calendar.DAY_OF_YEAR,1);
        next.set(Calendar.HOUR_OF_DAY,0);
        next.set(Calendar.MINUTE,0);
        next.set(Calendar.SECOND,5);
        next.set(Calendar.MILLISECOND,0);
        try{
            AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next.getTimeInMillis(),pending);
        }catch(Exception e){Log.w(TAG,"Could not schedule day reset",e);}
    }
}
