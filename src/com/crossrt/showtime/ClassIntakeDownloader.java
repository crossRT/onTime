package com.crossrt.showtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class ClassIntakeDownloader extends AsyncTask<Void, String, Void>
{
	private static final int RETRY_LIMIT = 3;
	
	private static final String PROGRESS_CONNECTING = "Connecting to APU";
	private static final String PROGRESS_PARSING = "Parsing intake";
	
	private Context context;
	private ProgressDialog mProgressDialog;
	private ArrayList<String> intakes = new ArrayList<String>();
	int retryCount;
	
	public ClassIntakeDownloader(Context context, ProgressDialog mProgressDialog)
	{
		this.context = context;
		this.mProgressDialog = mProgressDialog;
	}
	
	@Override
	protected void onPreExecute()
	{
		retryCount = 0;
		
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}
	
	@Override
	protected Void doInBackground(Void...params)
	{
		while(retryCount<RETRY_LIMIT ) //Reconnect to prevent weak connection
    	{
    		try
    		{
    			Document doc;
    			Element script;
    			
    			publishProgress(PROGRESS_CONNECTING);
        		doc = (Document)Jsoup.connect(ClassUpdater.URL).get();
        		script = doc.select("script").get(3);
        		
        		publishProgress(PROGRESS_PARSING);
        		//Pattern p = Pattern.compile("(?is)$(\'#selectIntakeAll\').append('<option value=\"(.+?)\">");
        		Pattern p = Pattern.compile("(?is)<option value=\"(.+?)\">");
        		Matcher m = p.matcher(script.toString());
        		
        		m.find();
        		while(m.find())
        		{
        			intakes.add(m.group(1));
        			m.find();
        		}
        		
                break;
    		}
    		catch(IOException e)
    		{
    			retryCount++;
    		}
    	}
		
		return null;
	}
	
	@Override
	protected void onProgressUpdate(String... progress)
	{
		mProgressDialog.setMessage(progress[0]);
	}
	
	@Override
	protected void onPostExecute(Void result)
	{
		mProgressDialog.dismiss();
		
		if(intakes.size()!=0)
		{
			final String[] list = intakes.toArray(new String[intakes.size()]);
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.intake_select);
			builder.setItems(list, new DialogInterface.OnClickListener()
				{
				public void onClick(DialogInterface dialog, int which)
				{
					SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
					SharedPreferences.Editor editor = config.edit();
		   			editor.putString("intakeCode", list[which]);
		   			editor.commit();
	               }
				});
			builder.setPositiveButton("Cancel", null);
			builder.show();
		}else if(retryCount==RETRY_LIMIT)
		{
			//Failed to connect APU
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.intake_select_failed);
			builder.setPositiveButton("Cancel", null);
			builder.show();
		}
	}

}
