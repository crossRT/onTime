package com.crossrt.showtime;

import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

@SuppressWarnings("deprecation")
public class PreferencesSetup extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	private SharedPreferences config;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_setup);
		
		//config = this.getSharedPreferences("com.crossrt.showtime_preferences", Context.MODE_PRIVATE);
		config = PreferenceManager.getDefaultSharedPreferences(this);
		
		//When select intake is click
		Preference listPreference = findPreference("selectIntake");
		listPreference.setOnPreferenceClickListener(new OnPreferenceClickListener()
			{
				public boolean onPreferenceClick(Preference preference)
				{
					if(isNetworkAvailable())
					{
						ProgressDialog mProgressDialog = new ProgressDialog(PreferencesSetup.this);
						ClassIntakeDownloader downloader = new ClassIntakeDownloader(PreferencesSetup.this, mProgressDialog);
						downloader.execute();
					}else
					{
						final String[] list = PreferencesSetup.this.getResources().getStringArray(R.array.options_prefix_intake);
						AlertDialog.Builder builder = new AlertDialog.Builder(PreferencesSetup.this);
						builder.setTitle(R.string.intake_select);
						builder.setItems(list, new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int which)
								{
									SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(PreferencesSetup.this);
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
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sp, String key)
	{
		if(key.equals("intakeCode"))
		{
			String intakeCode = sp.getString(key, "").toUpperCase(Locale.US);
			
			//Make sure intake is Upper case
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(key, intakeCode);
			editor.commit();
			
			getPreferenceScreen().findPreference("viewIntake").setSummary(intakeCode);
			
		}else if(key.equals("lecture")||key.equals("lab")||key.equals("tutorial"))
		{
			//Correct filter if user select empty
			if(key.equals("lecture") && sp.getString(key, "").equals(""))
			{
				//Make sure intake is Upper case
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(key, "L");
				editor.commit();
			}else if(key.equals("lab") && sp.getString(key, "").equals(""))
			{
				//Make sure intake is Upper case
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(key, "LAB");
				editor.commit();
			}else if(key.equals("tutorial") && sp.getString(key, "").equals(""))
			{
				//Make sure intake is Upper case
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(key, "T");
				editor.commit();
			}
			getPreferenceScreen().findPreference(key).setSummary(sp.getString(key, ""));
		}
	}
	
	private boolean isNetworkAvailable()
	{
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo != null;
	}
}
