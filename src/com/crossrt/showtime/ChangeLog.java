package com.crossrt.showtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView;

/**
 * 
 * Source from: Android Change Log
 * https://code.google.com/p/android-change-log/
 * 
 */
public class ChangeLog {

	private final Context context;
	private String lastVersion, thisVersion;

	// this is the key for storing the version name in SharedPreferences
	private static final String VERSION_KEY = "PREFS_VERSION_KEY";

	private static final String NO_VERSION = "";

	public ChangeLog(Context context) {
		this(context, PreferenceManager.getDefaultSharedPreferences(context));
	}

	public ChangeLog(Context context, SharedPreferences sp) {
		this.context = context;

		// get version numbers
		this.lastVersion = sp.getString(VERSION_KEY, NO_VERSION);
		Log.d(TAG, "lastVersion: " + lastVersion);
		try {
			this.thisVersion = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			this.thisVersion = NO_VERSION;
			Log.e(TAG, "could not get version name from manifest!");
			e.printStackTrace();
		}
		Log.d(TAG, "appVersion: " + this.thisVersion);
	}

	public String getLastVersion() {
		return this.lastVersion;
	}
	public String getThisVersion() {
		return this.thisVersion;
	}
	public boolean firstRun() {
		return !this.lastVersion.equals(this.thisVersion);
	}
	public boolean firstRunEver() {
		return NO_VERSION.equals(this.lastVersion);
	}
	public AlertDialog getLogDialog() {
		return this.getDialog(this.firstRunEver());
	}
	public AlertDialog getFullLogDialog() {
		return this.getDialog(true);
	}

	private AlertDialog getDialog(boolean full) {
		WebView wv = new WebView(this.context);
		wv.setBackgroundColor(0); // transparent
		// wv.getSettings().setDefaultTextEncodingName("utf-8");
		wv.loadDataWithBaseURL(null, this.getLog(full), "text/html", "UTF-8",
				null);

		AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
		builder.setTitle(
				context.getResources().getString(
						full ? R.string.changelog_full_title
								: R.string.changelog_title))
				.setView(wv)
				.setCancelable(false)
				// OK button
				.setPositiveButton(
						context.getResources().getString(
								R.string.changelog_ok_button),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								updateVersionInPreferences();
							}
						});

		if (!full) {
			// "more ..." button
			builder.setNegativeButton(R.string.changelog_show_full,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							getFullLogDialog().show();
						}
					});
		}

		return builder.create();
	}

	private void updateVersionInPreferences() {
		// save new version number to preferences
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(VERSION_KEY, thisVersion);
		// // on SDK-Versions > 9 you should use this:
		// if(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
		// editor.commit();
		// } else {
		// editor.apply();
		// }
		editor.commit();
	}
	public String getLog() {
		return this.getLog(false);
	}
	public String getFullLog() {
		return this.getLog(true);
	}
	private enum Listmode {
		NONE, ORDERED, UNORDERED,
	};

	private Listmode listMode = Listmode.NONE;
	private StringBuffer sb = null;
	private static final String EOCL = "END_OF_CHANGE_LOG";

	private String getLog(boolean full) {
		// read changelog.txt file
		sb = new StringBuffer();
		try {
			InputStream ins = context.getResources().openRawResource(
					R.raw.changelog);
			BufferedReader br = new BufferedReader(new InputStreamReader(ins));

			String line = null;
			boolean advanceToEOVS = false; // if true: ignore further version
											// sections
			while ((line = br.readLine()) != null) {
				line = line.trim();
				char marker = line.length() > 0 ? line.charAt(0) : 0;
				if (marker == '$') {
					// begin of a version section
					this.closeList();
					String version = line.substring(1).trim();
					// stop output?
					if (!full) {
						if (this.lastVersion.equals(version)) {
							advanceToEOVS = true;
						} else if (version.equals(EOCL)) {
							advanceToEOVS = false;
						}
					}
				} else if (!advanceToEOVS) {
					switch (marker) {
					case '%':
						// line contains version title
						this.closeList();
						sb.append("<div class='title'>"
								+ line.substring(1).trim() + "</div>\n");
						break;
					case '_':
						// line contains version title
						this.closeList();
						sb.append("<div class='subtitle'>"
								+ line.substring(1).trim() + "</div>\n");
						break;
					case '!':
						// line contains free text
						this.closeList();
						sb.append("<div class='freetext'>"
								+ line.substring(1).trim() + "</div>\n");
						break;
					case '#':
						// line contains numbered list item
						this.openList(Listmode.ORDERED);
						sb.append("<li>" + line.substring(1).trim() + "</li>\n");
						break;
					case '*':
						// line contains bullet list item
						this.openList(Listmode.UNORDERED);
						sb.append("<li>" + line.substring(1).trim() + "</li>\n");
						break;
					default:
						// no special character: just use line as is
						this.closeList();
						sb.append(line + "\n");
					}
				}
			}
			this.closeList();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	private void openList(Listmode listMode) {
		if (this.listMode != listMode) {
			closeList();
			if (listMode == Listmode.ORDERED) {
				sb.append("<div class='list'><ol>\n");
			} else if (listMode == Listmode.UNORDERED) {
				sb.append("<div class='list'><ul>\n");
			}
			this.listMode = listMode;
		}
	}

	private void closeList() {
		if (this.listMode == Listmode.ORDERED) {
			sb.append("</ol></div>\n");
		} else if (this.listMode == Listmode.UNORDERED) {
			sb.append("</ul></div>\n");
		}
		this.listMode = Listmode.NONE;
	}

	private static final String TAG = "ChangeLog";

	void setLastVersion(String lastVersion) {
		this.lastVersion = lastVersion;
	}
}