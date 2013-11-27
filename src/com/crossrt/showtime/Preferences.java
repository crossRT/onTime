package com.crossrt.showtime;


import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

@SuppressWarnings("deprecation")
public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	//Constant
	private static final String FILTER_UPDATED = "com.crossrt.showtime.FILTER_UPDATED";
	private static final int DIALOG_MENU=10;
	private static final int DIALOG_ABOUTME=20;
	private static final int DIALOG_DONATE=30;
	
	//Settings
	private static final String MY_EMAIL = "crossRT@gmail.com";
	private static final String ADDRESS_PLAYSTORE="market://details?id=com.crossrt.showtime";
	private static final String ADDRESS_WEB="http://play.google.com/store/apps/details?id=com.crossrt.showtime";
	//private static final String DONATE_LOCATION="https://dl.dropboxusercontent.com/u/14993838/ShowTimeDonate.html";
	
	//Strings in preference
	private static final String SEND_TITLE = "Send email";
	private static final String SHARE_TITLE = "Share onTime";
	
	
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
					    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ADDRESS_PLAYSTORE)));
					}catch(android.content.ActivityNotFoundException e)
					{
					    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ADDRESS_WEB)));
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
	
		//When select intake is click
		Preference listPreference = findPreference("selectIntake");
		listPreference.setOnPreferenceClickListener(new OnPreferenceClickListener()
			{
				public boolean onPreferenceClick(Preference preference)
				{
					if(isNetworkAvailable())
					{
						Log.e("SHOWTIME","AVAILABLE");
						ProgressDialog mProgressDialog = new ProgressDialog(Preferences.this);
						ClassIntakeDownloader downloader = new ClassIntakeDownloader(Preferences.this, mProgressDialog);
						downloader.execute();
					}else
					{
						final String[] list = Preferences.this.getResources().getStringArray(R.array.options_prefix_intake);
						AlertDialog.Builder builder = new AlertDialog.Builder(Preferences.this);
						builder.setTitle(R.string.intake_select);
						builder.setItems(list, new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int which)
								{
									SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(Preferences.this);
									SharedPreferences.Editor editor = config.edit();
									editor.putString("intakeCode", list[which]);
									editor.commit();
								}
							});
						builder.setPositiveButton("Cancel", null);
						builder.show();
						
					}
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
	
	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
			case DIALOG_MENU:
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.preference_menu_title);
				builder.setItems(R.array.options_preference_menu, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int select)
						{
							switch(select)
							{
								case 0:
								{
									showDialog(DIALOG_ABOUTME);
									break;
								}
								case 1:
								{
									showDialog(DIALOG_DONATE);
									break;
								}
								case 2:
								{
									ChangeLog changelog = new ChangeLog(Preferences.this);
									changelog.getLogDialog().show();
									break;
								}
								case 3:
								{
									String[] recipients = new String[]{MY_EMAIL, "",};
									String title=getString(R.string.contact_me);
									Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
									emailIntent.setType("text/plain");
									emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
									emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
									startActivity(Intent.createChooser(emailIntent, SEND_TITLE));
									break;
								}
								case 4:
								{
									Intent share = new Intent(Intent.ACTION_SEND);
									share.setType("text/plain");
									share.putExtra(Intent.EXTRA_TEXT, ADDRESS_PLAYSTORE);
									startActivity(Intent.createChooser(share, SHARE_TITLE));
								}
							}
						}
					}
				);
				builder.show();
				break;
			}
			case DIALOG_ABOUTME:
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.about_me_title);
				builder.setMessage(R.string.about_me_content);
				builder.setPositiveButton("Okay", null);
				builder.show();
				break;
			}
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
			
		}
		return super.onCreateDialog(id);
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sp,String key)
	{
		Intent updateWidget =new Intent();
		updateWidget.setAction(FILTER_UPDATED);
		
		if(key.equals("intakeCode") || key.equals("enterIntake"))
		{
			String intakeCode = sp.getString(key, "").toUpperCase(Locale.US);
			
			//Make sure intake is Upper case
			SharedPreferences.Editor editor = sp.edit();
			editor.putString("intakeCode", intakeCode);
			editor.commit();
			
			getPreferenceScreen().findPreference("viewIntake").setSummary(intakeCode);
			
//			sendBroadcast(updateWidget);
		}else if(key.equals("lecture")||key.equals("lab")||key.equals("tutorial"))
		{
			//Correct filter if user select empty
			if(key.equals("lecture") && sp.getString(key, null).equals(""))
			{
				//Make sure intake is Upper case
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(key, "L");
				editor.commit();
			}else if(key.equals("lab") && sp.getString(key, null).equals(""))
			{
				//Make sure intake is Upper case
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(key, "LAB");
				editor.commit();
			}else if(key.equals("tutorial") && sp.getString(key, null).equals(""))
			{
				//Make sure intake is Upper case
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(key, "T");
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
//			boolean autoUpdate = sp.getBoolean(key, false);
//			Calendar calendar=Calendar.getInstance();
//			calendar.set(Calendar.DAY_OF_WEEK,7);
//			calendar.set(Calendar.HOUR_OF_DAY,13);
//			calendar.set(Calendar.MINUTE,0);
//			calendar.set(Calendar.SECOND,0);
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
	
	private boolean isNetworkAvailable()
	{
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo != null;
	}
}
