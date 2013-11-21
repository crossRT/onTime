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
	private ViewGroup root;
	
	protected String filter_lecture,filter_lab,filter_tutorial; 	//Filter Settings
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
		root = (ViewGroup)inflater.inflate(R.layout.timetable_normal, null);
		if(((Main)getSherlockActivity()).readData())
		{
			classes = ((Main)getSherlockActivity()).getClasses();
			filter_lecture = ((Main)getActivity()).getLecture();
			filter_lab = ((Main)getActivity()).getLab();
			filter_tutorial = ((Main)getActivity()).getTutorial();
			
			writeToTable();
		}else
		{
			//Print a message if no timetable found
			root = (ViewGroup)inflater.inflate(R.layout.timetable_empty, null);
			TextView emptyMessage = (TextView)root.findViewById(R.id.empty_message);
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
		//Initialize
		boolean haveClass = false;	//Use to identify is that schedule available
		LinearLayout ll = (LinearLayout)root.findViewById(R.id.timetable_main);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.topMargin = 10; lp.bottomMargin = 5;
		ll.removeAllViews();
		
		if(!filter_lecture.equals("") || !filter_lab.equals("") || !filter_tutorial.equals(""))
		{
			if(filter_lecture.equals(""))
			{
				filter_lecture = "-L";
			}
			if(filter_lab.equals(""))
			{
				filter_lab = "-LAB";
			}
			if(filter_tutorial.equals(""))
			{
				filter_tutorial = "-T";
			}
		}
		
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
				
				table_day.setText(classes.get(i).getDate().substring(0, 3));
				table_date.setText(classes.get(i).getDate().substring(5,11));
				table_time.setText(classes.get(i).getTime());
				table_location.setText(classes.get(i).getLocation());
				table_class.setText(classes.get(i).getClasses());
				table_subject.setText(classes.get(i).getSubject());
				table_lecturer.setText(classes.get(i).getLecturer());
		
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
			ll.addView(emptyTable,lp);
		}
	}
	
	public abstract boolean validator(ClassPerclass perclass);
	public abstract String setTitle();
}