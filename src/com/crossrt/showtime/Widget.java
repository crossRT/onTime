package com.crossrt.showtime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider
{
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
		
		//Set fixed update time //12am
//		Calendar calendar=Calendar.getInstance();
//		calendar.set(Calendar.HOUR_OF_DAY,0);
//		calendar.set(Calendar.MINUTE,0);
//		calendar.set(Calendar.SECOND,0);
//		
//		Intent updateIntent = new Intent(context,Widget.class);
//		PendingIntent pi = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//		AlarmManager updateAlarm=(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//		updateAlarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,pi);
		
		readData();
		writeToWidget();
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		AppWidgetManager awm = AppWidgetManager.getInstance(context);
	    int[] ids = awm.getAppWidgetIds(new ComponentName(context, Widget.class));
	    this.onUpdate(context, awm, ids);
	}
	
	private void writeToWidget()
	{
		String packageName = context.getPackageName();
		RemoteViews widgetLayout = new RemoteViews(packageName,R.layout.widget_layout);
		widgetLayout.removeAllViews(R.id.widget_content);
		
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
	 * Load/Update today's class to widget's static variable
	 */
	private void readData()
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
		db.close();
		cursor.close();
		helper.close();
	}
}
