/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.heneryh.aquanotes.ui.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.RemoteViews;



//import com.example.android.wiktionary.ExtendedWikiHelper;

import java.sql.Date;
import java.text.SimpleDateFormat;

import com.heneryh.aquanotes.R;
import com.heneryh.aquanotes.provider.AquaNotesDbContract;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Controllers;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Data;
import com.heneryh.aquanotes.service.SyncService;
import com.heneryh.aquanotes.ui.HomeActivity;

/**
 * Define a simple widget that shows the Apex status. To build
 * an update we spawn a background {@link Service} to perform the API queries.
 */
public class Widget2x2 extends AppWidgetProvider {
	private static final String TAG = "Widget2x2";

	public static final String ACTION_UPDATE_SINGLE = "com.heneryh.aquanotes.UPDATE_SINGLE"; // probably shouldn't repeat this but import it
	public static final String ACTION_UPDATE_ALL = "com.heneryh.aquanotes.UPDATE_ALL";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// If no specific widgets requested, collect list of all
		// BTW, how do you get an update for just one widget?
		// I think the answer is exemplified in the configure.save action.
		if (appWidgetIds == null) {
			appWidgetIds = appWidgetManager.getAppWidgetIds(
					new ComponentName(context, Widget2x2.class));
		}

		// Request update for these widgets and launch updater service
		//SyncService.requestUpdate(appWidgetIds);
		//Intent updateIntent = new Intent(ACTION_UPDATE_SINGLE);
		Intent updateIntent = new Intent(ACTION_UPDATE_ALL);
		updateIntent.setClass(context, SyncService.class);
		context.startService(updateIntent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		ContentResolver resolver = context.getContentResolver();
		for (int appWidgetId : appWidgetIds) {
			
        	/** TODO:
        	 * The update by widget isn't working yet.
        	 */

//			Log.d(TAG, "Deleting appWidgetId=" + appWidgetId);
//			Uri controllerXUri = Controllers.buildUpdateControllerXUri(appWidgetId);
//			try {
//				ContentValues values = new ContentValues();
//				values.put(AquaNotesDbContract.Controllers.WIDGET, -1);  
//				resolver.update(controllerXUri, values, null, null);
//			} catch (SQLiteConstraintException e2 ) {
//				Log.e(TAG, "Inserting/updating controller data: ", e2);
//			}
		}
	}

	/**
	 * Build an update for the given medium widget. Should only be called from a
	 * service or thread to prevent ANR during database queries.
	 */
	public static RemoteViews buildUpdate(Context context, Uri controllerUri) {
		Log.d(TAG, "Building Apex 2x2 widget update");

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_2x2);

		boolean forecastFilled = false;

		ContentResolver resolver = context.getContentResolver();

		Cursor cursor = null;
		String devType = null;
		Long timestampL= (long) 0;

		// Pull out controller title and other data for this instance
		try {
			cursor = resolver.query(controllerUri, ControllersQuery.PROJECTION, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {

				/////////////////////////
				// We have three parts to the widget
				// 1) Title    2)Time   
				// 3) StatusText

				// Controller friendly title
				String title = cursor.getString(ControllersQuery.TITLE);
				views.setTextViewText(R.id.widget_apex_title, title);

				devType = cursor.getString(ControllersQuery.MODEL);

				// Timestamp
				timestampL = cursor.getLong(ControllersQuery.LAST_UPDATED);
				Date timestampD = new Date(timestampL);
				SimpleDateFormat formatter = new SimpleDateFormat("M/d/yy h:mm a");
				String timestampS = formatter.format(timestampD);
				views.setTextViewText(R.id.widget_apex_time,timestampS);
			} else {
				Log.e(TAG, "Didn't find a database entry for this controller.");
			}
		} catch (SQLException e) {
			Log.e(TAG, "building widget screen (title & time) from database.", e);	
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		// Fetch the XML status from the device, but if the device type is tagged as unsupported, error.
		if(devType.equalsIgnoreCase("Apex-old")) {
			views.setTextViewText(R.id.widget_probe_values, "Controller firmware too old, please update fw version.");
		} else if(devType.equalsIgnoreCase("AC3")) {
			views.setTextViewText(R.id.widget_probe_values, "ACiii not supported, search the market for the ACiii version of the app.");
		} else {
			String statusText;/*Apex.getShortXMLStatus(context, controllerUri);*/
			// For now, lets assume that there are no more than 20 characters per probe name/value pair
			// ToDo change to dynamic count THIS REALLY NEEDS TO GET FIXED !!
			StringBuffer probesBuffer = new StringBuffer(10*20);

			Cursor cursor2 = null;

			// Get all the data points for the timestamp noted above.
			Uri probeDataAtUri = Data.buildQueryPDataAtUri(controllerUri, timestampL);

			try {
				cursor = resolver.query(probeDataAtUri, ProbeDataViewQuery.PROJECTION, null, null, null);
				String name;
				String value;

				// Loop through the data points.
				if (cursor != null && cursor.moveToFirst()) {
					while (!cursor.isAfterLast()) {
						// Get the name/value & add to the string.
						name = cursor.getString(ProbeDataViewQuery.NAME);
						value = cursor.getString(ProbeDataViewQuery.VALUE);
						probesBuffer.append(name).append(" = ").append(value).append("\n");
						cursor.moveToNext();
					}
				}
			} catch (SQLException e) {
				Log.e(TAG, "getShortXMLStatus: building status string from database records.", e);	
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			} // end of controller query

			views.setTextViewText(R.id.widget_probe_values, probesBuffer.toString().trim());
			//views.setTextViewText(R.id.widget_probe_values, "test");
		}

		forecastFilled = true; // should make sure we have good data first. ERROR HANDLING!  

		// If not filled correctly, show error message and hide other fields
		if (!forecastFilled) {
			views = new RemoteViews(context.getPackageName(), R.layout.widget_2x2_initial);
			//views.setTextViewText(R.id.loading, res.getString(R.string.widget_error));
			Log.e(TAG, "Error filling the widget data, using the error template.");
		}

		// Connect click intent to launch details dialog
		Intent detailIntent = new Intent(context, HomeActivity.class);
		detailIntent.setData(controllerUri);
		PendingIntent pending = PendingIntent.getActivity(context, 0, detailIntent, 0);
		views.setOnClickPendingIntent(R.id.widget_2x2, pending);

		return views;
	}
	
	/**
	 * Projection and column definitions for the controller table.  Note that the widget_id is being
	 * used as the controller_id in the database.  This aids in looking up data for "this" controller
	 * and assumes a 1:1 relationship between widgets and controllers.
	 */
    private interface ControllersQuery {
        String[] PROJECTION = {
//              String CONTROLLER_ID = "_id";
//              String TITLE = "title";
//              String WAN_URL = "wan_url";
//              String WIFI_URL = "wifi_url";
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
        int LAN_URL = 3;
        int WIFI_SSID = 4;
        int USER = 5;
        int PW = 6;
        int LAST_UPDATED = 7;
        int UPDATE_INTERVAL = 8;
        int DB_SAVE_DAYS = 9;
        int MODEL = 10;
    }
    
	private interface ProbesQuery {
        String[] PROJECTION = {
        	//  String PROBE_ID = "_id";
        	//  String PROBE_NAME = "probe_name";
        	//  String DEVICE_ID = "device_id";
        	//  String TYPE = "probe_type";
        	//  String RESOURCE_ID = "resource_id";
        	//  String CONTROLLER_ID = "controller_id";
                BaseColumns._ID,
                AquaNotesDbContract.Probes.NAME,
                AquaNotesDbContract.Probes.RESOURCE_ID,
                AquaNotesDbContract.Probes.CONTROLLER_ID,
        };
        
        int _ID = 0;
        int PROBE_NAME = 1;
        int RESOURCE_ID = 2;
        int CONTROLLER_ID = 3;
    }

    private interface ProbeDataViewQuery {

        int _TOKEN = 0x1;
        
        String[] PROJECTION = {
//                String _ID = "_id";
//                String TYPE = "type";
//                String VALUE = "value";
//                String TIMESTAMP = "timestamp";
//                String PARENT_ID = "parent_id";   	
//
//        		//\\
//        		Join
//        		\\//
//                String NAME = "name";
//                String RESOURCE_ID = "resource_id";
//                String CONTROLLER_ID = "controller_id";
        		BaseColumns._ID,
        		AquaNotesDbContract.ProbeDataView.TYPE,
        		AquaNotesDbContract.ProbeDataView.VALUE,
        		AquaNotesDbContract.ProbeDataView.TIMESTAMP,
        		AquaNotesDbContract.ProbeDataView.PARENT_ID,

        		AquaNotesDbContract.ProbeDataView.NAME,
        		AquaNotesDbContract.ProbeDataView.RESOURCE_ID,
        		AquaNotesDbContract.ProbeDataView.CONTROLLER_ID,
         };
        int _ID = 0;
        int TYPE = 1;
        int VALUE = 2;
        int TIMESTAMP = 3;
        int PARENT_ID = 4;
        int NAME = 5;
        int RESOURCE_ID = 6;
        int CONTROLLER_ID = 7;
     }

}