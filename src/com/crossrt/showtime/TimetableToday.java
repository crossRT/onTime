package com.crossrt.showtime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimetableToday extends Timetable
{	
	private String today;
	
	public TimetableToday()
	{
		getTodayDate();
	}
	
	@Override
	public boolean validator(ClassPerclass perclass)
	{
		String subject = perclass.getSubject();
		String date = perclass.getDate().substring(5,11);
		if(
				(subject.matches(".*"+filter_lecture)||
				subject.matches(".*"+filter_lab)||
				subject.matches(".*"+filter_tutorial)||
				subject.matches(".*-L")||
				subject.matches(".*-LAB")||
				subject.matches(".*-T")) &&
				date.equals(today)
		)return true;
		else return false;
	}
	
	@Override
	public String setTitle()
	{
		return "Today";
	}
	
	public void getTodayDate()
	{
		today = new SimpleDateFormat("dd-MMM",Locale.US).format(new Date());
	}
}
