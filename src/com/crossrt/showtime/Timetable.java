package com.crossrt.showtime;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public abstract class Timetable extends SherlockFragment
{
	private static final int MARGIN_FIRST = 10;
	private static final int MARGIN_NORMAL = 10;
	private static final int MARGIN_GROUP_HEAD = 50;
	private static final int MARGIN_GROUP_OTHER = 8;
	
	private ViewGroup root;
	
	protected String filter_lecture,filter_lab,filter_tutorial; 	//Filter Settings
	protected boolean dayGroup;
	protected boolean first;
	protected ArrayList<ClassPerclass> classes = new ArrayList<ClassPerclass>();
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getSherlockActivity().getSupportActionBar().setTitle(this.setTitle());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
	{
		first = Main.first;
		root = (ViewGroup)inflater.inflate(R.layout.timetable_normal, null);
		if(((Main)getSherlockActivity()).readData())
		{
			classes = ((Main)getSherlockActivity()).getClasses();
			filter_lecture = ((Main)getActivity()).getLecture();
			filter_lab = ((Main)getActivity()).getLab();
			filter_tutorial = ((Main)getActivity()).getTutorial();
			dayGroup = ((Main)getActivity()).getDayGroup();
			
			writeToTable();
		}else
		{
			//Print a message if no timetable found
			root = (ViewGroup)inflater.inflate(R.layout.timetable_empty, null);
			TextView emptyMessage = (TextView)root.findViewById(R.id.empty_message);
			
			if(first)
				emptyMessage.setText(R.string.first_time);
			else
				emptyMessage.setText(R.string.no_class_found);
		}
		
		return root;
	}
	
	/**
	 * Used to write the timetable from variable to application layout.<br/>
	 * Any filter in derived class should override this function.
	 */
	public void writeToTable()
	{
		String savedDay="";			//Temporarily save previous day 
		boolean haveClass = false;	//Identify is there timetable available
		
		LinearLayout ll = (LinearLayout)root.findViewById(R.id.timetable_main);
		LinearLayout.LayoutParams lp;
		ll.removeAllViews();
		
		//set table layout
		for(int i=0;i<classes.size();i++)
		{
			//Filter to match lecture/lab/tutorial
			if(validator(classes.get(i)))//end if
			{
				LayoutInflater inflater = getActivity().getLayoutInflater();
				View perClassTable = inflater.inflate(R.layout.perclass_container, null);
				
				TextView table_day = (TextView)perClassTable.findViewById(R.id.table_day);
				TextView table_date = (TextView)perClassTable.findViewById(R.id.table_date);
				TextView table_time = (TextView)perClassTable.findViewById(R.id.table_time);
				TextView table_location = (TextView)perClassTable.findViewById(R.id.table_location);
				TextView table_class = (TextView)perClassTable.findViewById(R.id.table_class);
				TextView table_subject = (TextView)perClassTable.findViewById(R.id.table_subject);
				TextView table_lecturer = (TextView)perClassTable.findViewById(R.id.table_lecturer);
				
				String tableDay = classes.get(i).getDate().substring(0, 3);
				table_day.setText(tableDay);
				table_date.setText(classes.get(i).getDate().substring(5,11));
				table_time.setText(classes.get(i).getTime());
				table_location.setText(classes.get(i).getLocation());
				table_class.setText(classes.get(i).getClasses());
				table_subject.setText(classes.get(i).getSubject());
				table_lecturer.setText(classes.get(i).getLecturer());
				
				lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				
				//When day group is enable
				if(dayGroup)
				{
					//When current subject's day is not same as previous subject's day, use to identify the first subject also
					if(!tableDay.equals(savedDay))
					{
						lp.topMargin = (savedDay.equals("")) ? MARGIN_FIRST : MARGIN_GROUP_HEAD ;	//assign MARGIN_GROUP_HEAD when it is not first subject
						savedDay = tableDay; //save current subject's day in order recognize it when next day's subject start.
					}else if(tableDay.equals(savedDay) && !tableDay.equals(""))
					{
						lp.topMargin = MARGIN_GROUP_OTHER;
					}
				}else
				{
					lp.topMargin = MARGIN_NORMAL;
				}
				
				//add to LinearLayout
				ll.addView(perClassTable,lp);
				haveClass=true;
			}
		}
		
		//If there is no class after filter, show message
		if(!haveClass)
		{
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View emptyTable = inflater.inflate(R.layout.timetable_empty,null);
			TextView emptyMessage = (TextView)emptyTable.findViewById(R.id.empty_message);
			emptyMessage.setText(R.string.no_class_filter);
			lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.addView(emptyTable,lp);
		}
	}
	
	/**
	 * Validator to check every class, consider whether write into timetable.
	 * @param perclass single class to check
	 * @return permission to write current class into timetable
	 */
	public abstract boolean validator(ClassPerclass perclass);
	
	/**
	 * Set ActionBar Title. This method will run in onCreate() when any instance of Timetable is created.
	 * @return Manually return the title you want to set on ActionBar.
	 */
	public abstract String setTitle();
}
