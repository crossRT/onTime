package com.crossrt.showtime;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

@SuppressWarnings("deprecation")
public class DCWidgetSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	private SharedPreferences config;
	private ListPreference pd;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.dc_widget_settings);
        config = PreferenceManager.getDefaultSharedPreferences(this);
        
        pd=(ListPreference)findPreference("set_time");
        pd.setSummary("Current: "+config.getString("set_time", "60")+" minutes");
        
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key)
	{
		if(key.equals("set_time"))
		{
			pd=(ListPreference)findPreference(key);
			pd.setSummary("Current: "+sharedPreferences.getString(key, "60")+" minutes");
			DCWidget.before = Integer.parseInt(sharedPreferences.getString(key, "60"));
		}
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
}