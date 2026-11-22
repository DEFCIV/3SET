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
    private static final String PARTIAL_ALIAS=".LauncherPartial";
    private static final String DONE_ALIAS=".LauncherDone";
    private LauncherIconManager(){}

    public static synchronized void refresh(Context context){
        Context c=context.getApplicationContext();
        try {
            SharedPreferences prefs=c.getSharedPreferences("haennya-state",Context.MODE_PRIVATE);
            updateIcon(c,todayCount(prefs.getString("data","")));
        } catch(Exception e){ Log.w(TAG,"Could not refresh launcher icon",e); }
        scheduleDayReset(c);
    }

    public static synchronized void updateFromJson(Context context,String json){
        Context c=context.getApplicationContext();
        try{updateIcon(c,todayCount(json));}
        catch(Exception e){Log.w(TAG,"Could not update launcher icon",e);}
        scheduleDayReset(c);
    }

    private static int todayCount(String json){
        if(json==null||json.isEmpty())return 0;
        try{
            JSONObject days=new JSONObject(json).optJSONObject("days");
            if(days==null)return 0;
            return Math.max(0,Math.min(3,days.optInt(LocalDate.now().toString(),0)));
        }catch(Exception ignored){return 0;}
    }

    private static void updateIcon(Context context,int count){
        // Keep existing aliases and package name so an installed app retains its history.
        PackageManager pm=context.getPackageManager();
        ComponentName[] aliases={
            new ComponentName(context,context.getPackageName()+DEFAULT_ALIAS),
            new ComponentName(context,context.getPackageName()+PARTIAL_ALIAS),
            new ComponentName(context,context.getPackageName()+DONE_ALIAS)
        };
        int selected=count==0?0:(count>=3?2:1);
        boolean allMatch=true;
        for(int i=0;i<aliases.length;i++){
            int enabled=pm.getComponentEnabledSetting(aliases[i]);
            boolean active=enabled==PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                (i==0&&enabled==PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
            if(active!=(i==selected)){allMatch=false;break;}
        }
        if(allMatch)return;

        int flags=PackageManager.DONT_KILL_APP;
        int on=PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        int off=PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        if(Build.VERSION.SDK_INT>=33){
            pm.setComponentEnabledSettings(Arrays.asList(
                new PackageManager.ComponentEnabledSetting(aliases[0],selected==0?on:off,flags),
                new PackageManager.ComponentEnabledSetting(aliases[1],selected==1?on:off,flags),
                new PackageManager.ComponentEnabledSetting(aliases[2],selected==2?on:off,flags)
            ));
        }else{
            // Activate the destination first to avoid a moment without a launcher entry.
            pm.setComponentEnabledSetting(aliases[selected],on,flags);
            for(int i=0;i<aliases.length;i++)
                if(i!=selected)pm.setComponentEnabledSetting(aliases[i],off,flags);
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
