package com.crossrt.showtime;


import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

@SuppressWarnings("deprecation")
public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	//Constant
	private static final String FILTER_UPDATED = "com.crossrt.showtime.FILTER_UPDATED";
	private static final int DIALOG_MENU=10;
	private static final int DIALOG_ABOUTME=20;
	private static final int DIALOG_DONATE=30;
	
	//Settings
	private static final String PLAYSTORE_LOCATION="market://details?id=com.crossrt.showtime";
	private static final String SHOWTIME_LOCATION="http://play.google.com/store/apps/details?id=com.crossrt.showtime";
	private static final String DONATE_LOCATION="https://dl.dropboxusercontent.com/u/14993838/ShowTimeDonate.html";
	
	
	private SharedPreferences config;
	private boolean schduledRestart;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		//config = this.getSharedPreferences("com.crossrt.showtime_preferences", Context.MODE_PRIVATE);
		config = PreferenceManager.getDefaultSharedPreferences(this);
		schduledRestart = false;
		
		getPreferenceScreen().findPreference("viewIntake").setSummary(Main.intakeCode);
		getPreferenceScreen().findPreference("lecture").setSummary(Main.filter_lecture);
		getPreferenceScreen().findPreference("lab").setSummary(Main.filter_lab);
		getPreferenceScreen().findPreference("tutorial").setSummary(Main.filter_tutorial);
		getPreferenceScreen().findPreference("theme").setSummary(Main.theme);
	
		//When menu dialog is click
		Preference menuDialog=findPreference("menu_dialog");
		menuDialog.setOnPreferenceClickListener(new OnPreferenceClickListener()
			{
				public boolean onPreferenceClick(Preference preference)
				{
					showDialog(DIALOG_MENU);
					return true;
				}
			});
		
		//When menu rate is click
		Preference menuRate=findPreference("menu_rate");
		menuRate.setOnPreferenceClickListener(new OnPreferenceClickListener()
			{
				public boolean onPreferenceClick(Preference preference)
				{
					try
					{
					    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PLAYSTORE_LOCATION)));
					}catch(android.content.ActivityNotFoundException e)
					{
					    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(SHOWTIME_LOCATION)));
					}
					return true;
				}
			});
		
		//When menu donate is click
		Preference menuDonate=findPreference("menu_donate");
		menuDonate.setOnPreferenceClickListener(new OnPreferenceClickListener()
			{
				public boolean onPreferenceClick(Preference preference)
				{
					showDialog(DIALOG_DONATE);
					return true;
				}
			});
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		config.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		config.unregisterOnSharedPreferenceChangeListener(this);
		if(schduledRestart)
		{
			schduledRestart=false;
			Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
	        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        startActivity(i);
		}
	}
	
//	@Override
//	protected Dialog onCreateDialog(int id)
//	{
//		switch(id)
//		{
//			case DIALOG_MENU:
//			{
//				AlertDialog.Builder builder = new AlertDialog.Builder(this);
//				builder.setTitle(R.string.menu);
//				builder.setItems(R.array.menu, new DialogInterface.OnClickListener()
//					{
//						public void onClick(DialogInterface dialog, int select)
//						{
//							switch(select)
//							{
//								case 0:
//								{
//									showDialog(DIALOG_ABOUTME);
//									break;
//								}
//								case 1:
//								{
//									showDialog(DIALOG_DONATE);
//									break;
//								}
//								case 2:
//								{
//									ChangeLog changelog = new ChangeLog(Preferences.this);
//									changelog.getLogDialog().show();
//									break;
//								}
//								case 3:
//								{
//									String[] recipients = new String[]{"crossrt@gmail.com", "",};
//									String title=getString(R.string.bug_report);
//									Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
//									emailIntent.setType("text/plain");
//									emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
//									emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
//									startActivity(Intent.createChooser(emailIntent, "Send email"));
//									break;
//								}
//								case 4:
//								{
//									Intent share = new Intent(Intent.ACTION_SEND);
//									share.setType("text/plain");
//									share.putExtra(Intent.EXTRA_TEXT, SHOWTIME_LOCATION);
//									startActivity(Intent.createChooser(share, "Share ShowTime"));
//								}
//							}
//						}
//					}
//				);
//				builder.show();
//				break;
//			}
//			case DIALOG_ABOUTME:
//			{
//				AlertDialog.Builder builder = new AlertDialog.Builder(this);
//				builder.setTitle(R.string.about_me);
//				builder.setMessage(R.string.about_me_content);
//				builder.setPositiveButton("Okay", null);
//				builder.show();
//				break;
//			}
//			case DIALOG_DONATE:
//			{
//				AlertDialog.Builder builder = new AlertDialog.Builder(this);
//				builder.setTitle(R.string.donate_me);
//				builder.setMessage(R.string.donate_me_content);
//				builder.setPositiveButton("I have no money", null);
//				builder.setNegativeButton("Yes, bring me on!",new DialogInterface.OnClickListener()
//						{
//							public void onClick(DialogInterface dialog, int id)
//							{
//								startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(DONATE_LOCATION)));
//							}
//						});
//				builder.show();
//				break;
//			}
//			
//		}
//		return super.onCreateDialog(id);
//	}
	
	public void onSharedPreferenceChanged(SharedPreferences sp,String key)
	{
		Intent updateWidget =new Intent();
		updateWidget.setAction(FILTER_UPDATED);
		
		if(key.equals("intakeCode"))
		{
			String intakeCode = sp.getString(key, "").toUpperCase(Locale.US);
			
			//Make sure intake is Upper case
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(key, intakeCode);
			editor.commit();
			
			getPreferenceScreen().findPreference("viewIntake").setSummary(intakeCode);
			
//			sendBroadcast(updateWidget);
		}else if(key.equals("lecture")||key.equals("lab")||key.equals("tutorial"))
		{
			//Change lecture to "L" if user select empty
			if(key.equals("lecture") && sp.getString(key, null).equals(""))
			{
				//Make sure intake is Upper case
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(key, "L");
				editor.commit();
			}
			getPreferenceScreen().findPreference(key).setSummary(sp.getString(key, ""));
			
//			sendBroadcast(updateWidget);
		}else if(key.equals("theme"))
		{
			getPreferenceScreen().findPreference(key).setSummary(sp.getString(key, ""));
			schduledRestart = true;
		}else if(key.equals("autoupdate"))
		{
			boolean autoUpdate = sp.getBoolean(key, false);
			Calendar calendar=Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_WEEK,7);
			calendar.set(Calendar.HOUR_OF_DAY,13);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
//			Intent intent = new Intent(this,CallAutoUpdate.class);
//			intent.setAction("AUTOUPDATE");
//			PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//			AlarmManager updateAlarm=(AlarmManager) this.getSystemService(ALARM_SERVICE);
			
//			if(autoupdate==true)
//			{
//				updateAlarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,pi);
//			}else
//			{
//				updateAlarm.cancel(pi);
//			}
		}
	}
}