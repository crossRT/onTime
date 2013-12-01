package com.crossrt.showtime;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * This is a entry class of auto update. 
 * All auto update alarm should refer to here instead of ClassAutoUpdateService.
 * @author crossRT
 *
 */
public class ClassAutoUpdate extends BroadcastReceiver
{
	public static final String AUTO_UPDATE = "com.crossrt.showtime.AUTO_UPDATE";
	
	@Override
	public void onReceive(Context context,Intent intent)
	{
		String intentAction = intent.getAction();
		/*
		 * AUTO_UPDATE - start background service
		 * ACTION_BOOT_COMPLETED - set alarm when boot up
		 */
		if(intentAction.equals(AUTO_UPDATE))
		{
			Intent i = new Intent(context,ClassAutoUpdateService.class);
			context.startService(i);
		}else if(intentAction.equals(Intent.ACTION_BOOT_COMPLETED))
		{			
			SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
			boolean autoupdate = config.getBoolean("auto_update", false);
			
			/* If auto update function is enable */
			if(autoupdate)
			{
				/* Set every Saturday 1pm alarm */
				Calendar calendar=Calendar.getInstance();
				calendar.set(Calendar.DAY_OF_WEEK,7);
				calendar.set(Calendar.HOUR_OF_DAY,13);
				calendar.set(Calendar.MINUTE,0);
				calendar.set(Calendar.SECOND,0);
				
				/* Intent attach to the alarm.
				 * When alarm trigger, the intent will fire and call background service
				 */
				Intent i = new Intent(context,ClassAutoUpdate.class);
				i.setAction(AUTO_UPDATE);
				
				PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
				AlarmManager updateAlarm=(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				updateAlarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,pi);
			}
		}
	}
}
