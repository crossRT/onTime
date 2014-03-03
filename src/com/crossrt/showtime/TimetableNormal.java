package com.crossrt.showtime;



public class TimetableNormal extends Timetable
{	
	@Override
	public boolean validator(ClassPerclass perclass)
	{
		String subject = perclass.getSubject();
		if(
			subject.matches(".*"+filter_lecture)||
			subject.matches(".*"+filter_lab)||
			subject.matches(".*"+filter_tutorial)||
			subject.matches(".*-L")||
			subject.matches(".*-LAB")||
			subject.matches(".*-T")
		) return true;
		else return false;
	}
	
	@Override
	public String setTitle()
	{
		return "onTime";
	}
	
	@Override
	public Timetable getInstance()
	{
		return new TimetableNormal();
	}
}
