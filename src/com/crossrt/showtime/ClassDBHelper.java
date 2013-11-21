package com.crossrt.showtime;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ClassDBHelper extends SQLiteOpenHelper
{
	public ClassDBHelper(Context context, String dbName, SQLiteDatabase.CursorFactory factory, int version)
	{
		super(context, dbName, factory, version);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		String createString = "CREATE TABLE IF NOT EXISTS timetableData" + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
																			" date TEXT NOT NULL," +
																			" time TEXT NOT NULL," +
																			" class TEXT NOT NULL,"+
																			" location TEXT NOT NULL,"+
																			" subject TEXT NOT NULL,"+
																			" lecturer TEXT NOT NULL);";
		db.execSQL(createString);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		String dropString = "DROP TABLE IF EXISTS timetableData;";
		db.execSQL(dropString);
		onCreate(db);
	}
}
