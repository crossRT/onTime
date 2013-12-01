package com.crossrt.showtime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class DCProvider extends BroadcastReceiver
{
	private ClassDBHelper helper;
	private SQLiteDatabase db;
	private Cursor cursor;
	private SharedPreferences config;
	private static String today,lecture,lab,tutorial;
  	
	@Override
	public void onReceive(Context context,Intent intent)
	{
		config = PreferenceManager.getDefaultSharedPreferences(context);
		getInfo();
		try
		{
			helper = new ClassDBHelper(context, "timetableData",null,1);
			db = helper.getWritableDatabase();
			cursor = db.rawQuery("SELECT * FROM timetableData WHERE date LIKE '%"+today+"%' AND (subject LIKE '%"+lecture+"' OR subject LIKE '%"+lab+"' OR subject LIKE '%"+tutorial+"');", null);
			
		}catch(NullPointerException e)
		{
			Toast.makeText(context, "No data found", Toast.LENGTH_SHORT).show();
		}
			
		if(cursor != null && cursor.moveToFirst())
		{
			DCWidget.gotClass=true; //Tell DashClock today have class
			DCWidget.last=0; //Set loop counter to 0
			
			DCWidget.classes.clear(); //Clear all previous day class
			do
			{
				ClassPerclass perclass = new ClassPerclass();
				perclass.setTime(cursor.getString(2));
				perclass.setClasses(cursor.getString(3));
				perclass.setSubject(cursor.getString(5));
				DCWidget.classes.add(perclass);
				
			}while(cursor.moveToNext());
		}else
		{
			DCWidget.gotClass=false; //Tell DashClock today have no class
		}
		helper.close();
	}
	
	private void getInfo()
	{
		today = new SimpleDateFormat("dd-MMM-yyyy",Locale.US).format(new Date());
		lecture = config.getString("lecture", "");
		lab = config.getString("lab", "");
		tutorial = config.getString("tutorial", "");
	}
}
