package com.crossrt.showtime;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class WidgetService extends Service
{
	
	@Override
	public void onStart(Intent intent,int startId)
	{
		
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

}
