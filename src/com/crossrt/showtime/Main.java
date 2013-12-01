package com.crossrt.showtime;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class Main extends SherlockFragmentActivity
{	
	public static final String INTENT_EXTRA = "com.crossrt.showtime.INTENT_EXTRA";
	public static final String LAUNCH_TODAY = "com.crossrt.showtime.LAUNCH_TODAY";
	
	private SharedPreferences config;
	private ClassUpdater updater;
	private BroadcastReceiver receiveUpdate;
	
	private ClassDBHelper helper;
	private SQLiteDatabase db;
	private Cursor cursor;
	
	protected static String intakeCode,theme; 							//Settings
	protected static String filter_lecture,filter_lab,filter_tutorial; 	//Filter Settings
	protected static boolean dayGroup;
	protected static boolean first;
	protected static ArrayList<ClassPerclass> classes = new ArrayList<ClassPerclass>();
	
	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private ListView drawerMenu;
	private Timetable timetable;
	private Bundle savedInstanceState;
	private Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		this.savedInstanceState = savedInstanceState;
		this.intent = getIntent();
		super.onCreate(savedInstanceState);
		config = PreferenceManager.getDefaultSharedPreferences(this);
		theme = config.getString("theme", "");
		
		setActionBarTheme(theme);
		setContentView(R.layout.main);
		
		drawerLayout = (DrawerLayout) findViewById(R.id.main_layout);
		drawerMenu = (ListView)findViewById(R.id.main_menu);

		//Setup Drawer Menu (Left side menu)
		drawerMenu.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menu));
		drawerMenu.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				menuSelected(position);
			}
		});
		
		//ActionBar setting
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		
		drawerToggle = new ActionBarDrawerToggle(
				this,
				drawerLayout,
				R.drawable.ic_drawer_am,
				R.string.no_connection,
				R.string.no_class_filter);
		drawerLayout.setDrawerListener(drawerToggle);
		
		//Show changelog when new version installed
		ChangeLog changelog = new ChangeLog(this);
		if (changelog.firstRun())
		{
			changelog.getLogDialog().show();
			changelog.getLastVersion();
		}
		
	}
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
    }
	
	@Override
	public void onResume()
	{
		super.onResume();
		//Make sure timetable is correct even if preferences changed
		intakeCode = config.getString("intakeCode","");
		filter_lecture = config.getString("lecture", "");
		filter_lab = config.getString("lab", "");
		filter_tutorial = config.getString("tutorial", "");
		dayGroup = config.getBoolean("day_group", true);
		
		//Check whether first time or the intakeCode is empty
		if(config.getBoolean("first", true) || config.getString("intakeCode", "").equals(""))
		{
			Intent intent = new Intent(this,PreferencesSetup.class);
			startActivity(intent);
			SharedPreferences.Editor editor = config.edit();
			editor.putBoolean("first", false);
			editor.putBoolean("day_group", true);
			editor.commit();
			
			first = true; //Use to show different message when user first time using something;
		}else
		{
			//Start default fragment
			if(savedInstanceState==null)
			{
				String intentExtra = intent.getStringExtra(INTENT_EXTRA);
				FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
				//Log.e("SHOWTIME",intentExtra.toString());
				if(intentExtra!=null)
				{
					if(intentExtra.equals(LAUNCH_TODAY))
					{
						timetable = new TimetableToday();
					}
				}else
				{
					timetable = new TimetableNormal();
				}
				tx.replace(R.id.main_fragment, timetable).commit();
			}
		}
	}
	
	//Main menu
	private String[] menu = {"Normal","Today","All","Preferences"};
	protected void menuSelected(int position)
	{
		FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
		switch(position)
		{
		case 0:
			timetable = new TimetableNormal();
			tx.replace(R.id.main_fragment, timetable);
			break;
		case 1:
			timetable = new TimetableToday();
			tx.replace(R.id.main_fragment, timetable);
			break;
		case 2:
			timetable = new TimetableAll();
			tx.replace(R.id.main_fragment,timetable);
			break;
		case 3:
			Intent intent = new Intent(this,Preferences.class);
			startActivity(intent);
			break;
		}
		
		tx.commit();
		drawerLayout.closeDrawer(drawerMenu);
	}
	
	//ActionBar menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.sub_menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
			{
				if(drawerLayout.isDrawerOpen(drawerMenu))
				{
					drawerLayout.closeDrawer(drawerMenu);
				}else drawerLayout.openDrawer(drawerMenu);
				return true;
			}
			case R.id.menu_update:
			{
				pendingUpdate();
				return true;
			}
			default:
			{
				return super.onOptionsItemSelected(item);
			}
		}
	}
	
	/**
	 * Used to load or update local timetable variable from database.
	 * <br/>
	 * @return Status of reading data from database. 
	 * Return <strong>false</strong> if no timetable in database.
	 */
	public boolean readData()
	{
		helper = new ClassDBHelper(this, "timetableData",null,1);
		db = helper.getWritableDatabase();
		cursor = db.rawQuery("SELECT * FROM timetableData;", null);
		classes.clear();
		
		//If cursor!=null means there is timetable in database
		if(cursor != null && cursor.moveToFirst())
		{
			do
			{
				String date = cursor.getString(1);
				String time = cursor.getString(2);
				String classs = cursor.getString(3);
				String location = cursor.getString(4);
				String subject = cursor.getString(5);
				String lecturer = cursor.getString(6);
				
				ClassPerclass perclass = new ClassPerclass(date,time,classs,location,subject,lecturer);
				classes.add(perclass);
				
			}while(cursor.moveToNext());
			
			helper.close();
			return true;
		}else return false;
	}
	
	/**
	 * Update process. Check everything necessary
	 */
	public void pendingUpdate()
	{
		if(isNetworkAvailable())
		{
			if(!intakeCode.equals(""))
			{
				//Cancel current task if updater is existed and not finish.
				if(updater!=null && updater.getStatus()!=AsyncTask.Status.FINISHED)
				{
					updater.cancel(true);
				}
				
				/*
				 * Received success broadcast to update current timetable
				 * Received fail broadcast is in order to unregisterReceiver
				 */
				receiveUpdate = new BroadcastReceiver()
					{
						public void onReceive(Context context, Intent intent)
						{
							if(intent.getAction().equals(ClassUpdater.UPDATE_SUCCESS))
							{
								readData();
								FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
								timetable = new TimetableNormal();
								tx.replace(R.id.main_fragment, timetable);
								tx.commit();
								
								unregisterReceiver(this);
							}else
								unregisterReceiver(this);
						}
					};
				IntentFilter intentFilter = new IntentFilter();
				intentFilter.addAction(ClassUpdater.UPDATE_SUCCESS);
				intentFilter.addAction(ClassUpdater.UPDATE_FAIL);
				this.registerReceiver(receiveUpdate, intentFilter);
				
				//Update timetable
				updater = new ClassUpdater(this,intakeCode);
				updater.execute();
			}
		}else
		{
			Toast.makeText(Main.this, R.string.no_connection, Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Used to check device's network status
	 * @return Availability of network
	 */
	private boolean isNetworkAvailable()
	{
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo != null;
	}
	private void setActionBarTheme(String theme)
	{
		if(theme.equals("Holo Red"))
		{
			this.setTheme(R.style.onTime_Theme_Holo_Red);
		}else if(theme.equals("Holo Yellow"))
		{
			this.setTheme(R.style.onTime_Theme_Holo_Yellow);
		}else if(theme.equals("Holo Blue"))
		{
			this.setTheme(R.style.onTime_Theme_Holo_Blue);
		}else if(theme.equals("Holo Green"))
		{
			this.setTheme(R.style.onTime_Theme_Holo_Green);
		}else if(theme.equals("Holo Purple"))
		{
			this.setTheme(R.style.onTime_Theme_Holo_Purple);
		}else
		{
			this.setTheme(R.style.onTime_Theme_Default);
		}
	}
	
	//Getter function
	public String getLecture()
	{
		return filter_lecture;
	}
	public String getLab()
	{
		return filter_lab;
	}
	public String getTutorial()
	{
		return filter_tutorial;
	}
	public boolean getDayGroup()
	{
		return dayGroup;
	}
	public ArrayList<ClassPerclass> getClasses()
	{
		return classes;
	}

}
