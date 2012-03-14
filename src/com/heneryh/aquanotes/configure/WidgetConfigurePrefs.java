/**
 * Copyright (C) 2012 Joe Flynn - Licensing to be determined
 */

package com.heneryh.aquanotes.configure;

/**
 * For each widget instance we present the list of current controllers and an option
 * to select a new one (pre-populated default values).
 * 
 * This same activity copied and slightly modified exists for the app preferences.
 * The only difference is that this one sets the widgetID.  They can probably be combined
 * with minor effort
 */

import java.util.ArrayList;
import java.util.List;

import com.heneryh.aquanotes.R;
import com.heneryh.aquanotes.provider.AquaNotesDbContract;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Controllers;
import com.heneryh.aquanotes.service.SyncService;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WidgetConfigurePrefs extends Activity implements View.OnClickListener {

	private static final boolean LOGD = true;
	private static final String LOG_TAG = "WidgetConfigurePrefs";

	/**
	 * Graphical elements
	 */
	private Button mSave;
	private EditText mTitle;
	private EditText mWanUrl;
	private EditText mLanUrl;
	private EditText mWiFiSid;
	private EditText mUser;
	private EditText mPassword;
	private EditText mUpdateIntervalMins;
	private EditText mPruneAge;

	String type = null;

	/**
	 * Widget ID is used across many methods so make it a class variable.
	 */
	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	
	/** 
	 * These two are not carried over via text field so we need to save them off 
	 */
	int mControllerId = -1;
	Long lastUpdated;

	ContentResolver dbResolverWConfigAct;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Read the Controller/AppWidget Id to configure from the incoming intent
		mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);

		setConfigureResult(Activity.RESULT_CANCELED);

		dbResolverWConfigAct = getContentResolver();

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		/**
		 * Connect this activity to the configuration layout
		 */
		setContentView(R.layout.activity_prefs);

		/**
		 * then grab references to the graphical elements
		 */
		mTitle = (EditText)findViewById(R.id.conf_title);
		mWanUrl = (EditText)findViewById(R.id.conf_url);
		mLanUrl = (EditText)findViewById(R.id.conf_wifi_url);
		mWiFiSid = (EditText)findViewById(R.id.conf_wifi_sid);
		mUser = (EditText)findViewById(R.id.conf_user);
		mPassword = (EditText)findViewById(R.id.conf_password);
		mUpdateIntervalMins = (EditText)findViewById(R.id.conf_update_interval_mins);
		mPruneAge = (EditText)findViewById(R.id.conf_prune);

		mSave = (Button)findViewById(R.id.conf_save);
		mSave.setOnClickListener(this);
		
		/**
		 * Try to pick a word that fits for assiciating this widget to controller x
		 */
		mSave.setText("Associate");


		/**
		 * First lets get a list of all active controllers then present the list to the user in a dialog.
		 */
 		List<String> controllerURLs = new ArrayList<String>();
 		controllerURLs.add("New");
 		Cursor cursor = null;
		try {
			Uri controllersQueryUri = Controllers.buildQueryControllersUri();
			cursor = dbResolverWConfigAct.query(controllersQueryUri, ControllersQuery.PROJECTION, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
   				while (!cursor.isAfterLast()) {
    				String mURL = cursor.getString(ControllersQuery.WAN_URL); 
    				controllerURLs.add(mURL);
    				cursor.moveToNext();
				}
			}
		} catch (SQLException e) {
			Log.e(LOG_TAG, "getting controller list", e);	
			// need a little more here!
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		final CharSequence[] items = controllerURLs.toArray(new String[controllerURLs.size()]);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select a new or existing controller for this widget:");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
				if(item>0) {
					String Url = items[item].toString();
					Uri controllerXUri = AquaNotesDbContract.Controllers.buildQueryControllerByUrlUri(Url);
					Cursor cursor2 = null;

					String username = null;
					String password = null;
					String apexBaseURL = null;
					String apexWiFiURL = null;
					String apexWiFiSid = null;
					String title = null;
					Integer interval=0;
					Integer prune_age = 0;

					// Poll the database for facts about this controller
					// If already set in the db, then pre-populate the fields.
					try {
						ContentResolver dbResolverDialogAct = getContentResolver();
						cursor2 = dbResolverDialogAct.query(controllerXUri, ControllersQuery.PROJECTION, null, null, null);
						if (cursor2 != null && cursor2.moveToFirst()) {
							title = cursor2.getString(ControllersQuery.TITLE);
							username = cursor2.getString(ControllersQuery.USER);
							password = cursor2.getString(ControllersQuery.PW);
							apexBaseURL = cursor2.getString(ControllersQuery.WAN_URL);
							apexWiFiURL = cursor2.getString(ControllersQuery.WIFI_URL);
							apexWiFiSid = cursor2.getString(ControllersQuery.WIFI_SSID);
							interval = cursor2.getInt(ControllersQuery.UPDATE_INTERVAL);
							prune_age = cursor2.getInt(ControllersQuery.DB_SAVE_DAYS);
							lastUpdated = cursor2.getLong(ControllersQuery.LAST_UPDATED);
							type = cursor2.getString(ControllersQuery.MODEL);

							mTitle.setText(title);
							mWanUrl.setText(apexBaseURL);
							mLanUrl.setText(apexWiFiURL);
							mWiFiSid.setText(apexWiFiSid);
							mUser.setText(username);
							mPassword.setText(password);
							mUpdateIntervalMins.setText(interval.toString());
							mPruneAge.setText(prune_age.toString());
						}
					} catch (SQLException e) {
						Log.e(LOG_TAG, "onCreate: getting controller facts.", e);	
					} finally {
						if (cursor2 != null) {
							cursor2.close();
						}
					}
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();


		// If restoring, read location and units from bundle
		// I really need to learn more about this concept.
		if (savedInstanceState != null) {
			//            mUnits = savedInstanceState.getInt(ControllersColumns.xxx);
			//            if (mUnits == ControllersColumns.) {
			//                ((RadioButton)findViewById(R.id.xxxx)).setSelected(true);
			//            } else if (mUnits == AppWidgetsColumns.UNITS_CELSIUS) {
			//                ((RadioButton)findViewById(R.id.conf_units_c)).setSelected(true);
			//            }
		}

	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.conf_save: {
			// Picked save, so write values to database
			ContentValues values = new ContentValues();

			// Grab the values, note that they are all strings regardless of 
			// the format they will be stored in the database.
			String title = mTitle.getText().toString();
			String url = mWanUrl.getText().toString();
			String wiFiUrl = mLanUrl.getText().toString();
			String wiFiSid = mWiFiSid.getText().toString();
			String user = mUser.getText().toString();
			String pword = mPassword.getText().toString();
			String updIntervalMins = mUpdateIntervalMins.getText().toString();
			String pruneAge = mPruneAge.getText().toString();

			// Strings are easily put into the database directly but numbers must
			// be parsed.  The parsing may throw an exception.
			//values.put(BaseColumns._ID, mAppWidgetId);
			values.put(AquaNotesDbContract.Controllers.TITLE, title);
			values.put(AquaNotesDbContract.Controllers.WAN_URL, url);
			values.put(AquaNotesDbContract.Controllers.LAN_URL, wiFiUrl);
			values.put(AquaNotesDbContract.Controllers.WIFI_SSID, wiFiSid);
			values.put(AquaNotesDbContract.Controllers.USER, user);
			values.put(AquaNotesDbContract.Controllers.PW, pword);
			values.put(AquaNotesDbContract.Controllers.WIDGET, mAppWidgetId);  
			if(type==null) values.put(AquaNotesDbContract.Controllers.MODEL, "none set");
			try {
				Integer updIntervalMinsInt = Integer.parseInt(updIntervalMins.trim());
				values.put(AquaNotesDbContract.Controllers.UPDATE_INTERVAL, updIntervalMinsInt);  

				Integer pruneAgeInt = Integer.parseInt(pruneAge.trim());
				values.put(AquaNotesDbContract.Controllers.DB_SAVE_DAYS, pruneAgeInt);  
			} catch(NumberFormatException nfe) {
				if (LOGD) Log.e(LOG_TAG, "ERROR: Non-number entered for update frequency or prune age.");
				// How shall we handle this?????
				// Maybe a toast message?
			}
			values.put(AquaNotesDbContract.Controllers.LAST_UPDATED, lastUpdated);  

			// I can't figure out why the insert constraint violation is not properly caught
			// when there is an update needed rather than an insert.
			// Well, the quick fix is to just try the update first then the insert if needed.
			// This is only done rarely anyway so it doesn't matter much.
			ContentResolver resolver = getContentResolver();
			Uri controllerXUri = Controllers.buildUpdateControllerXUri(url);
			int updateStatus = 0;
			try {
				updateStatus = resolver.update(controllerXUri, values, null, null);
			} catch (SQLiteConstraintException e2 ) {
				Log.e(LOG_TAG, "Inserting/updating controller data: ", e2);
			}
			if(updateStatus==0) {
				try {
					controllerXUri = resolver.insert(Controllers.buildInsertControllerUri(), values);
				} catch (SQLiteConstraintException e) {
					Log.w(LOG_TAG, "Inserting controller data, maybe updating: ", e);
				} catch (SQLException e) {
					Log.e(LOG_TAG, "Inserting controller data, maybe updating: ", e);
				} 
			}

				// Trigger an update
				if(controllerXUri!=null) {
					String controllerId = Controllers.getControllerId(controllerXUri);
					//SyncService.requestUpdate(new int[] {Integer.valueOf(controllerId)});

					Intent updateIntent = new Intent(SyncService.ACTION_UPDATE_ALL);
					updateIntent.setClass(this, SyncService.class);
					
					// note that startService() will only really start it if not already running
					startService(updateIntent);
				}

			setConfigureResult(Activity.RESULT_OK);
			finish();

			break;
		} // end of case save:
		} // end of switch
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		//        //outState.putString(AppWidgetsColumns.TITLE, mTitle); // why not the title?
		//        outState.putString(ControllersColumns.URL, mWanUrl);
		//        outState.putString(ControllersColumns.USERNAME, mUser);
		//        outState.putString(ControllersColumns.PASSWORD, mPassword);

	}


	/**
	 * Convenience method to always include {@link #mAppWidgetId} when setting
	 * the result {@link Intent}.
	 */
	public void setConfigureResult(int resultCode) {
		final Intent data = new Intent();
		data.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(resultCode, data);
	}

	private interface ControllersQuery {
		String[] PROJECTION = {
				//              String CONTROLLER_ID = "_id";
				//              String TITLE = "title";
				//              String WAN_URL = "wan_url";
				//              String LAN_URL = "wifi_url";
				//              String WIFI_SSID = "wifi_ssid";
				//              String USER = "user";
				//              String PW = "pw";
				//              String LAST_UPDATED = "last_updated";
				//              String UPDATE_INTERVAL = "update_i";
				//              String DB_SAVE_DAYS = "db_save_days";
				//              String CONTROLLER_TYPE = "controller_type";
				BaseColumns._ID,
				AquaNotesDbContract.Controllers.TITLE,
				AquaNotesDbContract.Controllers.WAN_URL,
				AquaNotesDbContract.Controllers.LAN_URL,
				AquaNotesDbContract.Controllers.WIFI_SSID,
				AquaNotesDbContract.Controllers.USER,
				AquaNotesDbContract.Controllers.PW,
				AquaNotesDbContract.Controllers.LAST_UPDATED,
				AquaNotesDbContract.Controllers.UPDATE_INTERVAL,
				AquaNotesDbContract.Controllers.DB_SAVE_DAYS,
				AquaNotesDbContract.Controllers.MODEL,
		};

		int _ID = 0;
		int TITLE = 1;
		int WAN_URL = 2;
		int WIFI_URL = 3;
		int WIFI_SSID = 4;
		int USER = 5;
		int PW = 6;
		int LAST_UPDATED = 7;
		int UPDATE_INTERVAL = 8;
		int DB_SAVE_DAYS = 9;
		int MODEL = 10;
	}
}  // end of ConfigurePrefs
