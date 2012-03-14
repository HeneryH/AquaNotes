/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.heneryh.aquanotes.ui.feed;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Spinner;

import com.heneryh.aquanotes.R;
import com.heneryh.aquanotes.io.ApexExecutor;
import com.heneryh.aquanotes.io.NewXmlHandler.HandlerException;
import com.heneryh.aquanotes.provider.AquaNotesDbContract;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Controllers;
import com.heneryh.aquanotes.ui.BaseMultiPaneActivity;

/** This is the main "launcher" activity.
 * When running on a "large" or larger screen, this activity displays both the
 * TitlesFragments and the Content Fragment. When on a smaller screen size, this
 * activity displays only the TitlesFragment. In which case, selecting a list
 * item opens the ContentActivity, holds only the ContentFragment. */
public class FeedActivity extends BaseMultiPaneActivity {

	ContentResolver dbResolverFeedAct;
	
	/**
	 * Abstract out the IO to the controller
	 */
	private ApexExecutor mRemoteExecutor;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        dbResolverFeedAct = getContentResolver();
        fillSpinner();
        
		 /**
		  * helper class for defaultHttpClient seen below
		  */
		 final HttpClient httpClient = getHttpClient(this);

		 /**
		  * Create the executor for the controller of choice.  Now it is just the apex but I can see using
		  * other ones like the DA.  Pass in the http client and database resolver it will need to do its job.
		  */
		 mRemoteExecutor = new ApexExecutor(this, httpClient, dbResolverFeedAct);


    }

    private void fillSpinner(){

    	Uri controllersQueryUri = Controllers.buildQueryControllersUri();
		Cursor cursor = dbResolverFeedAct.query(controllersQueryUri, ControllersQuery.PROJECTION, null, null, null);

    	startManagingCursor(cursor);
    	 
    	// create an array to specify which fields we want to display
    	String[] from = new String[]{ControllersQuery.PROJECTION[ControllersQuery.TITLE]};
    	// create an array of the display item we want to bind our data to
    	int[] to = new int[]{android.R.id.text1};
    	// create simple cursor adapter
    	@SuppressWarnings("deprecation")
		SimpleCursorAdapter adapter =
    	  new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, from, to );
    	adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
    	// get reference to our spinner
    	Spinner s = (Spinner) findViewById( R.id.ctrlr_id);
    	s.setAdapter(adapter);
    	}
    
    public void feedA(View view) {
		Cursor cursor2 = null;
    	Spinner s2 = (Spinner) findViewById( R.id.ctrlr_id);
    	Cursor cursor1 = (Cursor)s2.getSelectedItem();
    	String title = cursor1.getString(ControllersQuery.TITLE);     //s2.getItemAtPosition(i).toString();
		try {
			Uri controllersQueryUri = Controllers.buildQueryControllerByTitleUri(title);
			cursor2 = dbResolverFeedAct.query(controllersQueryUri, ControllersQuery.PROJECTION, null, null, null);
			if (cursor2 != null && cursor2.moveToFirst()) {
				new FeedCycleThread(cursor2, 1).execute();
			}
		} catch (SQLException e) {
			//Log.e(TAG, "getting controller list", e);	
			// need a little more here!
		} finally {
			if (cursor2 != null) {
//				cursor.close();  close it in the thread
			}
		}
    }
    
    public void feedB(View view) {
		Cursor cursor2 = null;
    	Spinner s2 = (Spinner) findViewById( R.id.ctrlr_id);
    	Cursor cursor1 = (Cursor)s2.getSelectedItem();
    	String title = cursor1.getString(ControllersQuery.TITLE);     //s2.getItemAtPosition(i).toString();
		try {
			Uri controllersQueryUri = Controllers.buildQueryControllerByTitleUri(title);
			cursor2 = dbResolverFeedAct.query(controllersQueryUri, ControllersQuery.PROJECTION, null, null, null);
			if (cursor2 != null && cursor2.moveToFirst()) {
				new FeedCycleThread(cursor2, 1).execute();
			}
		} catch (SQLException e) {
			//Log.e(TAG, "getting controller list", e);	
			// need a little more here!
		} finally {
			if (cursor2 != null) {
//				cursor.close();  close it in the thread
			}
		}
    }
    
    public void feedC(View view) {
		Cursor cursor2 = null;
    	Spinner s2 = (Spinner) findViewById( R.id.ctrlr_id);
    	Cursor cursor1 = (Cursor)s2.getSelectedItem();
    	String title = cursor1.getString(ControllersQuery.TITLE);     //s2.getItemAtPosition(i).toString();
		try {
			Uri controllersQueryUri = Controllers.buildQueryControllerByTitleUri(title);
			cursor2 = dbResolverFeedAct.query(controllersQueryUri, ControllersQuery.PROJECTION, null, null, null);
			if (cursor2 != null && cursor2.moveToFirst()) {
				new FeedCycleThread(cursor2, 2).execute();
			}
		} catch (SQLException e) {
			//Log.e(TAG, "getting controller list", e);	
			// need a little more here!
		} finally {
			if (cursor2 != null) {
//				cursor.close();  close it in the thread
			}
		}
    }
    
    public void feedD(View view) {
		Cursor cursor2 = null;
    	Spinner s2 = (Spinner) findViewById( R.id.ctrlr_id);
    	Cursor cursor1 = (Cursor)s2.getSelectedItem();
    	String title = cursor1.getString(ControllersQuery.TITLE);     //s2.getItemAtPosition(i).toString();
		try {
			Uri controllersQueryUri = Controllers.buildQueryControllerByTitleUri(title);
			cursor2 = dbResolverFeedAct.query(controllersQueryUri, ControllersQuery.PROJECTION, null, null, null);
			if (cursor2 != null && cursor2.moveToFirst()) {
				new FeedCycleThread(cursor2, 3).execute();
			}
		} catch (SQLException e) {
			//Log.e(TAG, "getting controller list", e);	
			// need a little more here!
		} finally {
			if (cursor2 != null) {
//				cursor.close();  close it in the thread
			}
		}
    }
    
    public void feedCancel(View view) {
		Cursor cursor2 = null;
    	Spinner s2 = (Spinner) findViewById( R.id.ctrlr_id);
    	Cursor cursor1 = (Cursor)s2.getSelectedItem();
    	String title = cursor1.getString(ControllersQuery.TITLE);     //s2.getItemAtPosition(i).toString();
		try {
			Uri controllersQueryUri = Controllers.buildQueryControllerByTitleUri(title);
			cursor2 = dbResolverFeedAct.query(controllersQueryUri, ControllersQuery.PROJECTION, null, null, null);
			if (cursor2 != null && cursor2.moveToFirst()) {
				new FeedCycleThread(cursor2, 4).execute();
			}
		} catch (SQLException e) {
			//Log.e(TAG, "getting controller list", e);	
			// need a little more here!
		} finally {
			if (cursor2 != null) {
//				cursor.close();  close it in the thread
			}
		}
    }
    
	private class FeedCycleThread extends AsyncTask<String, Integer, Boolean> {
		int position;
		Cursor cursor;

		FeedCycleThread(Cursor c, int ps) {
			position = ps;
			cursor = c;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				mRemoteExecutor.feedCycle(cursor, position);
			} catch (HandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Boolean resultFailedFlag) {
//			triggerRefresh();
			cursor.close();

		}	
	}
	
	/**
	 * There is an embedded http client helper below.  Why do I need to keep copies of this in each activity
	 * that uses the remote executor?  I should be able to have a single copy.  No time to figure it out now...
	 */
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String ENCODING_GZIP = "gzip";
	private static final int SECOND_IN_MILLIS = (int) DateUtils.SECOND_IN_MILLIS;

	/**
	 * Generate and return a {@link HttpClient} configured for general use,
	 * including setting an application-specific user-agent string.
	 */
	public static HttpClient getHttpClient(Context context) {
		final HttpParams params = new BasicHttpParams();

		// Use generous timeouts for slow mobile networks
		HttpConnectionParams.setConnectionTimeout(params, 20 * SECOND_IN_MILLIS);
		HttpConnectionParams.setSoTimeout(params, 20 * SECOND_IN_MILLIS);

		HttpConnectionParams.setSocketBufferSize(params, 8192);
		HttpProtocolParams.setUserAgent(params, buildUserAgent(context));

		final DefaultHttpClient client = new DefaultHttpClient(params);

		client.addRequestInterceptor(new HttpRequestInterceptor() {
			public void process(HttpRequest request, HttpContext context) {
				// Add header to accept gzip content
				if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
					request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
				}
			}
		});

		client.addResponseInterceptor(new HttpResponseInterceptor() {
			public void process(HttpResponse response, HttpContext context) {
				// Inflate any responses compressed with gzip
				final HttpEntity entity = response.getEntity();
				final Header encoding = entity.getContentEncoding();
				if (encoding != null) {
					for (HeaderElement element : encoding.getElements()) {
						if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
							response.setEntity(new InflatingEntity(response.getEntity()));
							break;
						}
					}
				}
			}
		});
		return client;
	}

	/**
	 * Build and return a user-agent string that can identify this application
	 * to remote servers. Contains the package name and version code.
	 */
	private static String buildUserAgent(Context context) {
		try {
			final PackageManager manager = context.getPackageManager();
			final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);

			// Some APIs require "(gzip)" in the user-agent string.
			return info.packageName + "/" + info.versionName
					+ " (" + info.versionCode + ") (gzip)";
		} catch (NameNotFoundException e) {
			return null;
		}
	}

	/**
	 * Simple {@link HttpEntityWrapper} that inflates the wrapped
	 * {@link HttpEntity} by passing it through {@link GZIPInputStream}.
	 */
	private static class InflatingEntity extends HttpEntityWrapper {
		public InflatingEntity(HttpEntity wrapped) {
			super(wrapped);
		}

		@Override
		public InputStream getContent() throws IOException {
			return new GZIPInputStream(wrappedEntity.getContent());
		}

		@Override
		public long getContentLength() {
			return -1;
		}
	}

	private interface ControllersQuery {

		int _TOKEN = 0x1;

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
		int LAN_URL = 3;
		int WIFI_SSID = 4;
		int USER = 5;
		int PW = 6;
		int LAST_UPDATED = 7;
		int UPDATE_INTERVAL = 8;
		int DB_SAVE_DAYS = 9;
		int MODEL = 10;
	}

}
