package com.crossrt.showtime;

public class ClassPerclass
{
	private String date,time,classes,location,subject,lecturer;
	
	public ClassPerclass(){}
	public ClassPerclass(String date, String time, String classes, String location, String subject, String lecturer)
	{
		this.date = date;
		this.time = time;
		this.classes = classes;
		this.location = location;
		this.subject = subject;
		this.lecturer = lecturer;
	}

	/* GET Function */
	public String getDate()
	{
		 return date;
	}
	public String getTime()
	{
		return time;
	}
	public String getClasses()
	{
		return classes;
	}
	public String getLocation()
	{
		return location;
	}
	public String getSubject()
	{
		return subject;
	}
	public String getLecturer()
	{
		return lecturer;
	}

	/* SET Function */
	public void setDate(String date)
	{
		this.date = date;
	}
	public void setTime(String time)
	{
		this.time = time;
	}
	public void setClasses(String classes)
	{
		this.classes = classes;
	}
	public void setLocation(String location)
	{
		this.location = location;
	}
	public void setSubject(String subject)
	{
		this.subject= subject;
	}
	public void setLecturer(String lecturer)
	{
		this.lecturer = lecturer;
	}
}
