package com.haennya.fitness;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.util.Calendar;

/** Three daily local reminders; stop notifying after all three sets. */
public final class ReminderScheduler {
    static final String PREFS="haennya-reminders";
    static final String CHANNEL="haennya-minimal-v2";
    static final String EXTRA_INDEX="reminder-index";
    private static final int MAX_SLOTS=3;
    private ReminderScheduler(){}

    public static void ensureChannel(Context c){
        NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel ch=new NotificationChannel(CHANNEL,"3SET 운동 알림",NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("하루 3세트를 마치기 전까지 오는 운동 알림");
        ch.enableVibration(true);
        nm.createNotificationChannel(ch);
    }
    static void storeAndSchedule(Context c,String json){
        try{
            JSONObject obj=new JSONObject(json);
            JSONArray slots=obj.optJSONArray("slots");
            if(slots==null)slots=new JSONArray();
            SharedPreferences.Editor e=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit();
            e.putBoolean("enabled",obj.optBoolean("enabled",true));
            e.putString("slots",slots.toString());
            e.putString("completedDate",obj.optString("completedDate",""));
            e.putString("countDate",obj.optString("countDate",""));
            e.putInt("doneCount",obj.optInt("doneCount",0));
            e.apply();
            rebuild(c);
        }catch(Exception err){android.util.Log.e("3SET","Cannot sync reminders",err);}
    }
    private static PendingIntent alarmIntent(Context c,int i){
        Intent intent=new Intent(c,ReminderReceiver.class).putExtra(EXTRA_INDEX,i);
        return PendingIntent.getBroadcast(c,3000+i,intent,
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    private static String slotAt(SharedPreferences p,int i){
        try{return new JSONArray(p.getString("slots","[]")).getString(i);}
        catch(Exception ignored){return null;}
    }
    public static synchronized void rebuild(Context c){
        AlarmManager mgr=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        SharedPreferences p=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        for(int i=0;i<MAX_SLOTS;i++)mgr.cancel(alarmIntent(c,i));
        if(!p.getBoolean("enabled",false))return;
        for(int i=0;i<MAX_SLOTS;i++)if(slotAt(p,i)!=null)schedule(c,i,false);
    }
    private static void schedule(Context c,int index,boolean tomorrow){
        SharedPreferences p=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        if(!p.getBoolean("enabled",false))return;
        String slot=slotAt(p,index);if(slot==null)return;
        try{
            String[] parts=slot.split(":");
            int hour=Integer.parseInt(parts[0]),minute=Integer.parseInt(parts[1]);
            if(hour<7||hour>23||minute<0||minute>59)return;
            Calendar when=Calendar.getInstance();
            when.set(Calendar.HOUR_OF_DAY,hour);
            when.set(Calendar.MINUTE,minute);
            when.set(Calendar.SECOND,0);
            when.set(Calendar.MILLISECOND,0);
            if(tomorrow||when.getTimeInMillis()<=System.currentTimeMillis())when.add(Calendar.DAY_OF_YEAR,1);
            AlarmManager mgr=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
            mgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when.getTimeInMillis(),alarmIntent(c,index));
        }catch(Exception err){android.util.Log.w("3SET","Invalid time",err);}
    }
    static synchronized void onAlarm(Context context,int index){
        Context c=context.getApplicationContext();
        SharedPreferences p=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        if(p.getBoolean("enabled",false)&&slotAt(p,index)!=null){
            if(!LocalDate.now().toString().equals(p.getString("completedDate","")))send(c,index,false);
            schedule(c,index,true);
        }
    }
    static void showTest(Context c){send(c,0,true);}
    private static void send(Context c,int index,boolean test){
        if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
        ensureChannel(c);
        SharedPreferences p=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        int done=LocalDate.now().toString().equals(p.getString("countDate",""))?p.getInt("doneCount",0):0;
        if(!test&&done>=3)return;
        String[] messages={"오늘 3세트. 아직 안 했냐?","운동해야지. 아직 안 끝났잖아.","오늘 마지막 독촉. 3세트 끝내자."};
        String message=test?"운동 끝내기 전까지 찾아간다.":
            done==1?"흠… 2세트 남았다. 아직 안 끝났어.":
            done==2?"1세트만 더. 오늘 끝내고 쉬자.":messages[Math.min(Math.max(index,0),messages.length-1)];
        Intent open=new Intent(c,MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pending=PendingIntent.getActivity(c,8000,open,
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification n=new Notification.Builder(c,CHANNEL)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("3SET").setContentText(message)
            .setStyle(new Notification.BigTextStyle().bigText(message))
            .setAutoCancel(true).setContentIntent(pending)
            .setCategory(Notification.CATEGORY_REMINDER).build();
        NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(test?9999:1000+index,n);
    }
}