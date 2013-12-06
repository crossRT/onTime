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
	private static final String WIDGET_SET_ALARM = "com.crossrt.showtime.WIDGET_SET_ALARM";
	private static final String NEXT_CLASS_ID = "com.crossrt.showtime.NEXT_CLASS_ID";
	private static final String WIDGET_DAILY_UPDATE = "com.crossrt.showtime.DAILY_UPDATE";
	
	private Context context;
	private AppWidgetManager appWidgetManager;
	private int[] appWidgetIds;
	
	private static ArrayList<ClassPerclass> classes = new ArrayList<ClassPerclass>();
	
	@Override
	public void onUpdate(Context context,AppWidgetManager appWidgetManager,int[] appWidgetIds)
	{
		this.context = context;
		this.appWidgetManager = appWidgetManager;
		this.appWidgetIds = appWidgetIds;
		Log.e("SHOWTIME","onUPDTE");
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);
		this.context = context;
		appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, Widget.class));
		
		/*
		 * What to do when received broadcast from system
		 */
		if(intent.getAction()!=null)
		{
			String intentAction = intent.getAction();
			
			if(intentAction.equals(WIDGET_SET_ALARM)) /* Intent to set alarm */
			{
				Log.e("SHOWTIME","ALARM RING RING RING");
				//Get the index(class position) from extra
				int index = intent.getIntExtra(NEXT_CLASS_ID, 0);
				writeToWidget(index);
				
				//Set next class alarm if next class available
				if(index<classes.size()-1)
				{
					Log.e("SHOWTIME","After writeToWidget, set Alarm for next class");
					int nextClassId = index+1;	//index is current class, now need to set for next class
					setNextAlarm(nextClassId);
				}
				
			}else if(intentAction.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
					|| intentAction.equals("android.intent.action.DATE_CHANGED")
					|| intentAction.equals(ClassUpdater.UPDATE_SUCCESS)
					|| intentAction.equals("com.crossrt.showtime.FILTER_UPDATED")
					|| intentAction.equals(WIDGET_DAILY_UPDATE))
			{
				Log.e("SHOWTIME","UPDATE!");
				Log.e("SHOWTIME",intentAction);
				//Normal read and write
				readTimetable();
				
				//Get current time
				Calendar current = Calendar.getInstance();
				
				//Check current status
				for(int i=0,count=0;i<classes.size();i++)
				{
					//Check Time 1
					int hour = Integer.parseInt(classes.get(i).getTime().substring(0, 2));
					int minute = Integer.parseInt(classes.get(i).getTime().substring(3, 5));
					Calendar checkTime1 = Calendar.getInstance();
					checkTime1.set(Calendar.SECOND,0);
					checkTime1.set(Calendar.HOUR_OF_DAY, hour);
					checkTime1.set(Calendar.MINUTE,minute);
					
					//Check Time 2
					int hour2 = Integer.parseInt(classes.get(i).getTime().substring(8,10));
					int minute2 = Integer.parseInt(classes.get(i).getTime().substring(11,13));
					Calendar checkTime2 = Calendar.getInstance();
					checkTime2.set(Calendar.SECOND,0);
					checkTime2.set(Calendar.HOUR_OF_DAY, hour2);
					checkTime2.set(Calendar.MINUTE,minute2);
										
					/*
					 * If current is before next class,
					 * write previous class to header
					 * and set next class alarm.
					 */
					if(current.after(checkTime1) && current.before(checkTime2))
					{
						Log.e("SHOWTIME","index: "+i);
						writeToWidget(i);
						
						/* Avoid FC after all class finish */
						if(i<classes.size()-1)
							setNextAlarm(i+1);
						
						break;
					}else count++;
					
					//When no class is match
					if(count==classes.size())
					{
						//Get first class time
						int firstHour = Integer.parseInt(classes.get(0).getTime().substring(0, 2));
						int firstMinute = Integer.parseInt(classes.get(0).getTime().substring(3, 5));
						Calendar firstTime = Calendar.getInstance();
						firstTime.set(Calendar.SECOND,0);
						firstTime.set(Calendar.HOUR_OF_DAY, firstHour);
						firstTime.set(Calendar.MINUTE,firstMinute);
						
						if(current.before(firstTime))
						{
							writeToWidget(0);
							setNextAlarm(1);
						}
					}
				}
			}else if(intentAction.equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED)) /* Set repeated update alarm */
			{
				Log.e("SHOWTIME","ENABLED!");
				//Set fixed update time //12am
				Calendar calendar=Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY,0);
				calendar.set(Calendar.MINUTE,0);
				calendar.set(Calendar.SECOND,0);
				
				Intent updateIntent = new Intent(context,Widget.class);
				updateIntent.setAction(WIDGET_DAILY_UPDATE);
				PendingIntent pi = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				AlarmManager updateAlarm=(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				updateAlarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,pi);
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
			writeToWidget();
		}
	}
	
	private void writeToWidget()
	{
		String packageName = context.getPackageName();
		RemoteViews widgetLayout = new RemoteViews(packageName,R.layout.widget_layout);
		widgetLayout.removeAllViews(R.id.widget_content);
		
		//First class will show here
//		widgetLayout.setTextViewText(R.id.widget_header_time, "");
//		widgetLayout.setTextViewText(R.id.widget_header_class, "");
//		widgetLayout.setTextViewText(R.id.widget_header_module, "");
				
		//Set onClick listener
		Intent intent = new Intent(context,Main.class);
		intent.putExtra(Main.INTENT_EXTRA, Main.LAUNCH_TODAY);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		widgetLayout.setOnClickPendingIntent(R.id.widget_content, pendingIntent);
		
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
		
		//First class will show here
//		widgetLayout.setTextViewText(R.id.widget_header_time, classes.get(index).getTime());
//		widgetLayout.setTextViewText(R.id.widget_header_class, classes.get(index).getClasses());
//		widgetLayout.setTextViewText(R.id.widget_header_module, classes.get(index).getSubject());
		
		//Set onClick listener
		Intent intent = new Intent(context,Main.class);
		intent.putExtra(Main.INTENT_EXTRA, Main.LAUNCH_TODAY);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		widgetLayout.setOnClickPendingIntent(R.id.widget_content, pendingIntent);
		
		for(int i=index+1;i<classes.size();i++)
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
	 * Set next class alarm to update widget's view by sending broadcast.
	 * @param hour next class's hour
	 * @param minute next class's minute
	 * @param timetableId next class's id
	 */
	private void setNextAlarm(int timetableId)
	{
		int hour = Integer.parseInt(classes.get(timetableId).getTime().substring(0, 2));
		int minute= Integer.parseInt(classes.get(timetableId).getTime().substring(3, 5));		
		
		//Set target time to send broadcast
		final Calendar targetTime = Calendar.getInstance();
		targetTime.set(Calendar.HOUR_OF_DAY,hour);
		targetTime.set(Calendar.MINUTE,minute);
		targetTime.set(Calendar.SECOND, 0);
		
		final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		final Intent intent = new Intent(context,Widget.class);
		intent.setAction(WIDGET_SET_ALARM); //Set current intent action
		intent.putExtra(NEXT_CLASS_ID, timetableId);	//set next class id in extra
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Log.e("SHOWTIME","Next alarm is: " + targetTime.getTime());
		am.set(AlarmManager.RTC_WAKEUP, targetTime.getTimeInMillis() , pendingIntent);
	}
}
