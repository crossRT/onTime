package com.crossrt.showtime;

import java.util.Locale;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
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
			//Change lecture to "L" if user select empty
			if(key.equals("lecture") && sp.getString(key, null).equals(""))
			{
				//Make sure intake is Upper case
				SharedPreferences.Editor editor = sp.edit();
				editor.putString(key, "L");
				editor.commit();
			}
			getPreferenceScreen().findPreference(key).setSummary(sp.getString(key, ""));
		}
	}
}
