package com.crossrt.showtime;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

/**
 * True background auto updater,
 * since only service can stay on background more than 5 second,
 * it's useful to wait for user change network status when alarm is triggered 
 * but device is not connected to network
 * @author crossRT
 *
 */
public class ClassAutoUpdateService extends Service
{
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	public int onStartCommand(Intent intent,int flags,int startId)
	{
		/* When device is online, update immediately */
		if(isConnected(this))
		{
			ClassUpdater updater = new ClassUpdater(this);
			updater.execute();
			stopSelf();
		}else /* Otherwise create a broadcast to wait device online */
		{
			BroadcastReceiver pendingUpdate = new BroadcastReceiver()
				{
					public void onReceive(Context context, Intent intent)
					{
						if(isConnected(context))
						{
							ClassUpdater updater = new ClassUpdater(ClassAutoUpdateService.this);
							updater.execute();
							unregisterReceiver(this);
							stopSelf();
						}
					}
				};
			registerReceiver(pendingUpdate,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		}
		return START_STICKY;
	}
	
	public static boolean isConnected(Context context)
	{
	    ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = null;
	    if (connectivityManager != null)
	    {
	        networkInfo = connectivityManager.getActiveNetworkInfo();
	    }
	    return networkInfo == null ? false : networkInfo.getState() == NetworkInfo.State.CONNECTED;
	}

}
