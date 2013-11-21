package com.crossrt.showtime;


public class TimetableAll extends Timetable
{	
	@Override
	public boolean validator(ClassPerclass perclass)
	{
		return true;
	}
	
	@Override
	public String setTitle()
	{
		return "All";
	}

}
