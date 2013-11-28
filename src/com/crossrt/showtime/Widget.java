package com.crossrt.showtime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider
{
	private static final String WIDGET_BROADCAST_TYPE = "com.crossrt.showtime.WIDGET_BROADCAST_TYPE";
	private static final String WIDGET_ALARM_NEXTCLASS = "com.crossrt.showtime.WIDGET_ALARM_NEXTCLASS";
	private static final String NEXT_CLASS_ID = "com.crossrt.showtime.NEXT_CLASS_ID";
	
	private Context context;
	private AppWidgetManager appWidgetManager;
	private int[] appWidgetIds;
	
	private static ArrayList<ClassPerclass> classes;
	
	@Override
	public void onUpdate(Context context,AppWidgetManager appWidgetManager,int[] appWidgetIds)
	{
		this.context = context;
		this.appWidgetManager = appWidgetManager;
		this.appWidgetIds = appWidgetIds;
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		this.context = context;
		appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, Widget.class));

		/*
		 * Identify what to do when received broadcast from system
		 */
		if(intent.getStringExtra(WIDGET_BROADCAST_TYPE)!=null)
		{
			//When received an alarm
			if(intent.getStringExtra(WIDGET_BROADCAST_TYPE).equals(WIDGET_ALARM_NEXTCLASS))
			{
				//Get the index(class position) from extra
				int index = intent.getIntExtra(NEXT_CLASS_ID, 0);
				writeToWidget(index);
				
				//Set next class alarm if next class available
				if(index>=classes.size())
				{
					int nextClassId = index+1;	//index is current class, now need to set for next class
					int hour = Integer.parseInt(classes.get(nextClassId).getTime().substring(0, 2));
					int minute = Integer.parseInt(classes.get(nextClassId).getTime().substring(3, 5));
					setNextAlarm(hour,minute,nextClassId);
				}
			}
		}else if(intent.getAction()!=null)
		{
			if(intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE))
			{
				//Normal read and write
				readTimetable();
				//writeToWidget();
				
				//Get current time
				Calendar current = Calendar.getInstance();
				
				//Check current status
				for(int i=0,count=0;i<classes.size();i++)
				{
					int hour = Integer.parseInt(classes.get(i).getTime().substring(0, 2));
					int minute = Integer.parseInt(classes.get(i).getTime().substring(3, 5));
					Calendar checkTime = Calendar.getInstance();
					checkTime.set(Calendar.SECOND,0);
					checkTime.set(Calendar.HOUR_OF_DAY, hour);
					checkTime.set(Calendar.MINUTE,minute);
					
					/*
					 * If current is before next class,
					 * write previous class to header
					 * and set next class alarm.
					 */
					if(current.before(checkTime))
					{
						writeToWidget(i-1);
						setNextAlarm(hour,minute,i);
						break;
					}else count++;
					
					//When all class is finish
					if(count==classes.size())
					{
						writeToWidget();
					}
				}
			}
		}
	}
	
	/**
	 * Load/Update today's class to widget's static variable
	 */
	private void readTimetable()
	{
		//Clean all previous class
		if(classes!=null) classes.clear();
		
		SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
		String lecture = config.getString("lecture", "L");
		String lab = config.getString("lab", "LAB");
		String tutorial = config.getString("tutorial", "T");
		String today = new SimpleDateFormat("dd-MMM-yyyy",Locale.US).format(new Date());
		
		ClassDBHelper helper = new ClassDBHelper(context, "timetableData",null,1);
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = db.rawQuery("SELECT * " +
				"FROM timetableData " +
				"WHERE date LIKE '%"+today+"%' " +
						"AND (subject LIKE '%"+lecture+"' " +
								"OR subject LIKE '%"+lab+"' " +
								"OR subject LIKE '%"+tutorial+"' " +
								"OR subject LIKE '%-L' " +
								"OR subject LIKE '%-LAB' " +
								"OR subject LIKE '%-T');", null);
		
		if(cursor != null && cursor.moveToFirst())
		{
			classes = new ArrayList<ClassPerclass>();
			do
			{
				ClassPerclass perclass = new ClassPerclass();
				perclass.setTime(cursor.getString(2));
				perclass.setClasses(cursor.getString(3));
				perclass.setSubject(cursor.getString(5));
				
				classes.add(perclass);
			}while(cursor.moveToNext());
			helper.close();
		}else
		{
			Log.e("SHOWTIME","cursor null");
		}
	}
	
	private void writeToWidget()
	{
		String packageName = context.getPackageName();
		RemoteViews widgetLayout = new RemoteViews(packageName,R.layout.widget_layout);
		widgetLayout.removeAllViews(R.id.widget_content);
		Log.e("SHOWTIME","classes size: "+classes.size());
		for(int i=0;i<classes.size();i++)
		{
			RemoteViews widgetPerclass = new RemoteViews(packageName,R.layout.widget_perclass);
			widgetPerclass.setTextViewText(R.id.widget_time, classes.get(i).getTime());
			widgetPerclass.setTextViewText(R.id.widget_class, classes.get(i).getClasses());
			widgetPerclass.setTextViewText(R.id.widget_subject, classes.get(i).getSubject());
			widgetLayout.addView(R.id.widget_content, widgetPerclass);
		}
		appWidgetManager.updateAppWidget(appWidgetIds, widgetLayout);
	}
	
	/**
	 * Write all today's class to widget. Use it when need to print certain class on header 
	 * @param index The index of the class need to print on header
	 */
	private void writeToWidget(int index)
	{
		String packageName = context.getPackageName();
		RemoteViews widgetLayout = new RemoteViews(packageName,R.layout.widget_layout);
		widgetLayout.removeAllViews(R.id.widget_content);
		Log.e("SHOWTIME","index: "+index);
		
		//First class will show here
		widgetLayout.setTextViewText(R.id.widget_header_time, classes.get(index).getTime());
		widgetLayout.setTextViewText(R.id.widget_header_class, classes.get(index).getClasses());
		widgetLayout.setTextViewText(R.id.widget_header_module, classes.get(index).getSubject());
		
		for(int i=index+1;i<classes.size();i++)
		{
			Log.e("SHOWTIME","inside: "+ i);
			RemoteViews widgetPerclass = new RemoteViews(packageName,R.layout.widget_perclass);
			widgetPerclass.setTextViewText(R.id.widget_time, classes.get(i).getTime());
			widgetPerclass.setTextViewText(R.id.widget_class, classes.get(i).getClasses());
			widgetPerclass.setTextViewText(R.id.widget_subject, classes.get(i).getSubject());
			widgetLayout.addView(R.id.widget_content, widgetPerclass);
		}
		appWidgetManager.updateAppWidget(appWidgetIds, widgetLayout);
	}
	
	/**
	 * Set next class alarm to update widget's view by sending broadcast.
	 * @param hour next class's hour
	 * @param minute next class's minute
	 * @param timetableId next class's id
	 */
	private void setNextAlarm(int hour,int minute,int timetableId)
	{
		//Set target time to send broadcast
		final Calendar targetTime = Calendar.getInstance();
		targetTime.set(Calendar.HOUR_OF_DAY,hour);
		targetTime.set(Calendar.MINUTE,minute);
		targetTime.set(Calendar.SECOND, 0);
		
		final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		final Intent intent = new Intent(context,Widget.class);
		intent.putExtra(WIDGET_BROADCAST_TYPE, WIDGET_ALARM_NEXTCLASS);	//define current broadcast type
		intent.putExtra(NEXT_CLASS_ID, timetableId);	//set next class id in extra
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		am.set(AlarmManager.RTC, targetTime.getTimeInMillis() , pendingIntent);
	}
}
