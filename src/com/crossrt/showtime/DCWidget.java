package com.crossrt.showtime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class DCWidget extends DashClockExtension
{
	protected static boolean gotClass;
	protected static ArrayList<ClassPerclass> classes = new ArrayList<ClassPerclass>();
	
	protected static int before; //Minutes before the class
	protected static int last; //Store index of last class in loop, avoid loop again. 
	
	private static int hour,minute;
	private static Date currentTime,timeCompare1,timeCompare2;
	private static SimpleDateFormat inputParser = new SimpleDateFormat("HH:mm",Locale.US);
	private Intent todayIntent;
	private static SharedPreferences config;
	
	public void onCreate()
	{
		super.onCreate();
		//todayIntent = new Intent(this,ClassToday.class);
		
		//Set fixed update time //12am
		Calendar calendar=Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY,0);
		calendar.set(Calendar.MINUTE,0);
		calendar.set(Calendar.SECOND,0);
		
		Intent intent = new Intent(this,DCProvider.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager updateAlarm=(AlarmManager) this.getSystemService(ALARM_SERVICE);
		updateAlarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,pi);
		
		config = PreferenceManager.getDefaultSharedPreferences(this);
		before = Integer.parseInt(config.getString("set_time", "60"));
	}
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	protected void onInitialize(boolean isReconnect)
	{
		setUpdateWhenScreenOn(true);
	}
	
	protected void onUpdateData(int reason)
	{
		if(gotClass)
		{
			Calendar now = Calendar.getInstance();
			hour = now.get(Calendar.HOUR_OF_DAY);
			minute = now.get(Calendar.MINUTE);
			currentTime = parseDate(hour + ":" + minute);
			
			for(int i=last;i<classes.size();i++)
			{
				String thisTime = classes.get(i).getTime();
				String thisClass = classes.get(i).getClasses();
				String thisSubject = classes.get(i).getSubject();
				
				timeCompare1 = parseDate(thisTime.substring(0, 5));
				timeCompare2 = parseDate(thisTime.substring(8,13));
				
				if(currentTime.before(timeCompare1)) /* When current time is before time1 mean need count down to start class */
				{
					long count = TimeUnit.MILLISECONDS.toMinutes(timeCompare1.getTime() - currentTime.getTime());
					if(count <= before)
					{
						publishUpdate(new ExtensionData()
		                .visible(true)
		                .icon(R.drawable.ic_actionbar)
		                .status(count+" Minute")
		                .expandedTitle("Next class:")
		                .expandedBody(count+" minutes left\nTime      : "+thisTime+"\nClass     : "+thisClass+"\nSubject : "+thisSubject)
		                .clickIntent(todayIntent));
						last=i;
						break;
					}else{publishUpdate(null);break;}
				}else if(currentTime.before(timeCompare2)) /* When current time is after time1 but before time2 mean during the class, need count down to end class */
				{
					long count = timeCompare2.getTime() - currentTime.getTime();
					count = TimeUnit.MILLISECONDS.toMinutes(count);
					
					publishUpdate(new ExtensionData()
	                .visible(true)
	                .icon(R.drawable.ic_actionbar)
	                .status(count+" Minute")
	                .expandedTitle("Current class:")
	                .expandedBody(count+" minutes to finish\nTime      : "+thisTime+"\nClass     : "+thisClass+"\nSubject : "+thisSubject)
	                .clickIntent(todayIntent));
					last=i;
					break;
				}else
				{
					publishUpdate(null);
				}
			}
		}else
		{
			publishUpdate(null);
		}
	}
	private Date parseDate(String date)
	{
		try
		{
			return inputParser.parse(date);
		}catch(java.text.ParseException e)
		{
			return new Date(0);
		}
	}
}
