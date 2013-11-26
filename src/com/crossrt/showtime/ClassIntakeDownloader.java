package com.crossrt.showtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class ClassIntakeDownloader extends AsyncTask<String, Integer, String>
{
	private static final int RETRY_LIMIT = 3;
	
	ProgressDialog mProgressDialog;
	Document doc;
	Elements intakesElements;
	private ArrayList<String> intakes = new ArrayList<String>();
	
	public ClassIntakeDownloader(ProgressDialog mProgressDialog)
	{
		this.mProgressDialog = mProgressDialog;
	}
	
	@Override
	protected void onPreExecute()
	{
		Log.e("SHOWTIME","START");
		mProgressDialog.setMessage("Connecting to APU");
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setCancelable(false);
		//mProgressDialog.show();
	}

	@Override
	protected String doInBackground(String... arg0)
	{
		Log.e("SHOWTIME","DOING");
		int count=0;
		while(count<RETRY_LIMIT) //Reconnect to prevent weak connection
    	{
			Log.e("SHOWTIME","DOING"+count);
    		try
    		{
    			Log.e("SHOWTIME","in try");
        		doc = (Document)Jsoup.connect(ClassUpdater.URL).get();
        		//Log.e("SHOWTIME",doc.toString());
        		Element script = doc.select("script").get(3);
        		Log.e("SHOWTIME",script.toString());
//        		
        		//Pattern p = Pattern.compile("(?is)$(\'#selectIntakeAll\').append('<option value=\"(.+?)\">");
        		Pattern p = Pattern.compile("(?is)<option value=\"(.+?)\">");
        		Matcher m = p.matcher(script.toString());
//        		
        		while(m.find())
        		{
        			//Log.e("SHOWTIME","FOUND: " + m.group(1));
        			intakes.add(m.group(1));
        			m.find();
        		}
        		
        		for(int i=0;i<intakes.size();i++)
        		{
        			Log.e("SHOWTIME","intake: "+intakes.get(i));
        		}
        		
    			//success = true;
                break;
    		}
    		catch(IOException e)
    		{
    			count++;
    		}
    	}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(String result)
	{
		super.onPostExecute(result);
	}

}
