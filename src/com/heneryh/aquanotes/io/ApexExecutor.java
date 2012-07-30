/*
 * Copyright 2011 Google Inc.
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

package com.heneryh.aquanotes.io;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import com.heneryh.aquanotes.io.NewXmlHandler.HandlerException;
import com.heneryh.aquanotes.provider.AquaNotesDbContract;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.BaseColumns;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes an {@link HttpUriRequest} and passes the result as an
 * {@link XmlPullParser} to the given {@link XmlHandler}.
 */
public class ApexExecutor {
	/**
	 * DefaultHttpClient that is setup in the calling activity (or service).  Need to figure out why
	 * this is setup up there rather than down here??
	 */
    private final HttpClient mHttpClient;
    
    /**
     * Database reference instantiated once in the calling activity (or service)
     */
    private final ContentResolver mDbResolver;
    
    /**
     * Context of the calling activity (or service)
     */
    private final Context mActContext;
    
    Uri controllerUri;

    public ApexExecutor(Context cx, HttpClient httpClient, ContentResolver resolver) {
    	mActContext = cx;
   		mHttpClient = httpClient;
   		mDbResolver = resolver; 
    }

    /**
     * Execute a {@link HttpGet} request, passing a valid response through
     * to the specified XML parser.  This common method can then be used to parse
     * various kinds of XML feeds.
     */
    public void executeGet(Uri ctrlUri, DefaultHandler xmlParser) throws HandlerException {

    	controllerUri = ctrlUri;
		Cursor cursor = null;

		String username = null;
		String password = null;
		String apexBaseURL = null;
		String apexWANURL = null;
		String apexWiFiURL = null;
		String apexWiFiSID = null;
		String controllerType = null;

		/**
		 * Poll the database for facts about this controller
		 */
		try {
			cursor = mDbResolver.query(controllerUri, ControllersQuery.PROJECTION, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				username = cursor.getString(ControllersQuery.USER);
				password = cursor.getString(ControllersQuery.PW);
				apexWANURL = cursor.getString(ControllersQuery.WAN_URL);
				apexWiFiURL = cursor.getString(ControllersQuery.LAN_URL);
				apexWiFiSID = cursor.getString(ControllersQuery.WIFI_SSID);
				controllerType = cursor.getString(ControllersQuery.MODEL);
			}
		} catch (SQLException e) {
			throw new HandlerException("Database error getting controller data.");
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		/**
		 * Depending on whether or not we are on the 'Home' wifi network we want to use either the
		 * WAN or LAN URL.
		 * 
		 * Uhg, WifiManager stuff below crashes if wifi not enabled so first we have to check if
		 * on wifi.
		 */
		ConnectivityManager cm  = (ConnectivityManager) mActContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		if(netInfo.getType()==ConnectivityManager.TYPE_WIFI) {
			/**
			 * Get the currently connected SSID, if it matches the 'Home' one then use the local WiFi URL rather than the public one
			 */
			WifiManager wm = (WifiManager) mActContext.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = wm.getConnectionInfo();
			
			// somewhere read this was a quoted string but appears not to be
			if(wInfo.getSSID().equalsIgnoreCase(apexWiFiSID)) {  
				apexBaseURL=apexWiFiURL;
			} else {
				apexBaseURL=apexWANURL;
			}
		} else {
			apexBaseURL=apexWANURL;
		}
		
		/**
		 * for this function we need to append to the URL.  To be safe we try to catch various
		 * forms of URL that might be entered by the user:
		 * 
		 * check if the "/" was put on the end
		 */
		if(!apexBaseURL.endsWith("/")) {
			String tmp = apexBaseURL + "/";
			apexBaseURL = tmp;
		}

		/**
		 * check if it starts with an "http://"
		 */
		if(!apexBaseURL.toLowerCase().startsWith("http://")) {
			String tmp = "http://" + apexBaseURL;
			apexBaseURL = tmp;
		}

		// oh, we should also check if it ends with an "status.sht" on the end and remove it.

		/**
		 * When all cleaned up, add the xml portion of the url to grab the status.
		 * 
		 * TODO: we tried to make this call handle various xml feeds but this call is hardcoded
		 * for the status feed.
		 */
		String apexURL = apexBaseURL + "cgi-bin/status.xml";

        final HttpUriRequest request = new HttpGet(apexURL);
        executeWhySeparate(request, xmlParser, username, password);
    }

    /**
     * Execute this {@link HttpUriRequest}, passing a valid response through
     * {@link XmlHandler#parseAndApply(XmlPullParser, ContentResolver)}.
     */
    public void executeWhySeparate(HttpUriRequest request, DefaultHandler xmlParser, String user, String pw) throws HandlerException {
    	
        try {  	
    		// Create credentials for basic auth
    		UsernamePasswordCredentials c = new UsernamePasswordCredentials(user,pw);
    		BasicCredentialsProvider cP = new BasicCredentialsProvider();
    		cP.setCredentials(AuthScope.ANY, c );
    		((DefaultHttpClient) mHttpClient).setCredentialsProvider(cP);

    		/**
    		 * Execute the command and check the status
    		 */
            final HttpResponse resp = mHttpClient.execute(request);
            final int status = resp.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK) {
                throw new HandlerException("Unexpected server response " + resp.getStatusLine()
                        + " for " + request.getRequestLine());
            }

            final InputStream input = resp.getEntity().getContent();
            
            try {
                NewXmlHandler.parseAndStore(input, controllerUri, xmlParser);
            } catch (HandlerException e) {
                throw new HandlerException("Malformed response for " + request.getRequestLine(), e);
            } finally {
                if (input != null) input.close();
            }
        } catch (HandlerException e) {
            throw e;
        } catch (IOException e) {
            throw new HandlerException("Problem reading remote response for "
                    + request.getRequestLine(), e);
        }
    }
    
    public void updateOutlet(Cursor cursor, String outletName,int position) throws HandlerException {
    	
    	/**
    	 * The cursor contains all of the controller details.
    	 */
    	String lanUri = cursor.getString(ControllersQuery.LAN_URL);
    	String wanUri = cursor.getString(ControllersQuery.WAN_URL);
    	String user = cursor.getString(ControllersQuery.USER);
    	String pw = cursor.getString(ControllersQuery.PW);
    	String ssid = cursor.getString(ControllersQuery.WIFI_SSID);
    	String model = cursor.getString(ControllersQuery.MODEL);
    	
    	// Uhg, WifiManager stuff below crashes in AVD if wifi not enabled so first we have to check if on wifi
    	ConnectivityManager cm  = (ConnectivityManager) mActContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo nInfo = cm.getActiveNetworkInfo();
    	String apexBaseURL;

    	if(nInfo.getType()==ConnectivityManager.TYPE_WIFI) {
    		// Get the currently connected SSID, if it matches the 'Home' one then use the local WiFi URL rather than the public one
    		WifiManager wm = (WifiManager) mActContext.getSystemService(Context.WIFI_SERVICE);
    		WifiInfo wInfo = wm.getConnectionInfo();

    		// somewhere read this was a quoted string but appears not to be
    		if(wInfo.getSSID().equalsIgnoreCase(ssid)) {  // the ssid will be quoted in the info class
    			apexBaseURL=lanUri;
    		} else {
    			apexBaseURL=wanUri;
    		}
    	} else {
    		apexBaseURL=wanUri;

    	}

    	// for this function we need to append to the URL.  I should really
    	// check if the "/" was put on the end by the user here to avoid 
    	// possible errors.
    	if(!apexBaseURL.endsWith("/")) {
    		String tmp = apexBaseURL + "/";
    		apexBaseURL = tmp;
    	}

    	// oh, we should also check if it starts with an "http://"
    	if(!apexBaseURL.toLowerCase().startsWith("http://")) {
    		String tmp = "http://" + apexBaseURL;
    		apexBaseURL = tmp;
    	}

    	// oh, we should also check if it ends with an "status.sht" on the end and remove it.

     	// This used to be common for both the Apex and ACiii but during
    	// the 4.04 beta Apex release it seemed to have broke and forced
    	// me to use status.sht for the Apex.  Maybe it was fixed but I 
    	// haven't checked it.
    	// edit - this was not needed for the longest while but now that we are pushing just one
    	// outlet, the different methods seem to be needed again.  Really not sure why.
		String apexURL;
		if(model.equalsIgnoreCase("AC4")) {
			apexURL = apexBaseURL + "status.sht";
		} else {
			apexURL = apexBaseURL + "cgi-bin/status.cgi";
		}

    	//Create credentials for basic auth
    	// create a basic credentials provider and pass the credentials
    	// Set credentials provider for our default http client so it will use those credentials
    	UsernamePasswordCredentials c = new UsernamePasswordCredentials(user,pw);
    	BasicCredentialsProvider cP = new BasicCredentialsProvider();
    	cP.setCredentials(AuthScope.ANY, c );
    	((DefaultHttpClient) mHttpClient).setCredentialsProvider(cP);

    	// Build the POST update which looks like this:
    	// form="status"
    	// method="post"
    	// action="status.sht"
    	//
    	// name="T5s_state", value="0"   (0=Auto, 1=Man Off, 2=Man On)
    	// submit -> name="Update", value="Update"
    	// -- or
    	// name="FeedSel", value="0"   (0=A, 1=B)
    	// submit -> name="FeedCycle", value="Feed"
    	// -- or
    	// submit -> name="FeedCycle", value="Feed Cancel"

    	HttpPost httppost = new HttpPost(apexURL);

    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2); 

    	// Add your data  
    	nameValuePairs.add(new BasicNameValuePair("name","status"));  
    	nameValuePairs.add(new BasicNameValuePair("method","post"));  
		if(model.equalsIgnoreCase("AC4")) {
			nameValuePairs.add(new BasicNameValuePair("action","status.sht"));  
		} else {
			nameValuePairs.add(new BasicNameValuePair("action","/cgi-bin/status.cgi"));  
		}

    	String pendingStateS = String.valueOf(position);
    	nameValuePairs.add(new BasicNameValuePair(outletName+"_state", pendingStateS));

    	nameValuePairs.add(new BasicNameValuePair("Update","Update"));  
    	try {  	

    		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  
 
    		HttpResponse resp = mHttpClient.execute(httppost);
    		final int status = resp.getStatusLine().getStatusCode();
    		if (status != HttpStatus.SC_OK) {
    			throw new HandlerException("Unexpected server response " + resp.getStatusLine()
    					+ " for " + httppost.getRequestLine());
    		}
    	} catch (HandlerException e) {
    		throw e;
    	} catch (ClientProtocolException e) {
    		throw new HandlerException("Problem reading remote response for "
    				+ httppost.getRequestLine(), e);
    	} catch (IOException e) {
    		throw new HandlerException("Problem reading remote response for "
    				+ httppost.getRequestLine(), e);
    	}

    }
    
    public void feedCycle(Cursor cursor, int cycleNumber) throws HandlerException {
    	
    	/**
    	 * The cursor contains all of the controller details.
    	 */
    	String lanUri = cursor.getString(ControllersQuery.LAN_URL);
    	String wanUri = cursor.getString(ControllersQuery.WAN_URL);
    	String user = cursor.getString(ControllersQuery.USER);
    	String pw = cursor.getString(ControllersQuery.PW);
    	String ssid = cursor.getString(ControllersQuery.WIFI_SSID);
    	String model = cursor.getString(ControllersQuery.MODEL);

    	String apexBaseURL;

    	// Determine if we are on the LAN or WAN and then use appropriate URL
    	
    	// Uhg, WifiManager stuff below crashes if wifi not enabled so first we have to check if on wifi
    	ConnectivityManager cm  = (ConnectivityManager) mActContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo nInfo = cm.getActiveNetworkInfo();

    	if(nInfo.getType()==ConnectivityManager.TYPE_WIFI) {
    		// Get the currently connected SSID, if it matches the 'Home' one then use the local WiFi URL rather than the public one
    		WifiManager wm = (WifiManager) mActContext.getSystemService(Context.WIFI_SERVICE);
    		WifiInfo wInfo = wm.getConnectionInfo();

    		// somewhere read this was a quoted string but appears not to be
    		if(wInfo.getSSID().equalsIgnoreCase(ssid)) {  // the ssid will be quoted in the info class
    			apexBaseURL=lanUri;
    		} else {
    			apexBaseURL=wanUri;
    		}
    	} else {
    		apexBaseURL=wanUri;

    	}

    	// for this function we need to append to the URL.  I should really
    	// check if the "/" was put on the end by the user here to avoid 
    	// possible errors.
    	if(!apexBaseURL.endsWith("/")) {
    		String tmp = apexBaseURL + "/";
    		apexBaseURL = tmp;
    	}

    	// oh, we should also check if it starts with an "http://"
    	if(!apexBaseURL.toLowerCase().startsWith("http://")) {
    		String tmp = "http://" + apexBaseURL;
    		apexBaseURL = tmp;
    	}

    	// we should also check if it ends with an "status.sht" on the end and remove it.

     	// This used to be common for both the Apex and ACiii but during
    	// the 4.04 beta Apex release it seemed to have broke and forced
    	// me to use status.sht for the Apex.  Maybe it was fixed but I 
    	// haven't checked it.
    	// edit - this was not needed for the longest while but now that we are pushing just one
    	// outlet, the different methods seem to be needed again.  Really not sure why.
		String apexURL;
		if(model.equalsIgnoreCase("AC4")) {
			apexURL = apexBaseURL + "status.sht";
		} else {
			apexURL = apexBaseURL + "cgi-bin/status.cgi";
		}

    	//Create credentials for basic auth
    	// create a basic credentials provider and pass the credentials
    	// Set credentials provider for our default http client so it will use those credentials
    	UsernamePasswordCredentials c = new UsernamePasswordCredentials(user,pw);
    	BasicCredentialsProvider cP = new BasicCredentialsProvider();
    	cP.setCredentials(AuthScope.ANY, c );
    	((DefaultHttpClient) mHttpClient).setCredentialsProvider(cP);

    	// Build the POST update which looks like this:
    	// form="status"
    	// method="post"
    	// action="status.sht"
    	//
    	// name="T5s_state", value="0"   (0=Auto, 1=Man Off, 2=Man On)
    	// submit -> name="Update", value="Update"
    	// -- or
    	// name="FeedSel", value="0"   (0=A, 1=B)
    	// submit -> name="FeedCycle", value="Feed"
    	// -- or
    	// submit -> name="FeedCycle", value="Feed Cancel"

    	HttpPost httppost = new HttpPost(apexURL);

    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2); 

    	// Add your data  
    	nameValuePairs.add(new BasicNameValuePair("name","status"));  
    	nameValuePairs.add(new BasicNameValuePair("method","post"));  
		if(model.equalsIgnoreCase("AC4")) {
			nameValuePairs.add(new BasicNameValuePair("action","status.sht"));  
		} else {
			nameValuePairs.add(new BasicNameValuePair("action","/cgi-bin/status.cgi"));  
		}

		String cycleNumberString = Integer.toString(cycleNumber).trim();
		if(cycleNumber<4){
			nameValuePairs.add(new BasicNameValuePair("FeedSel", cycleNumberString));  
			nameValuePairs.add(new BasicNameValuePair("FeedCycle","Feed"));  
		} else {
			nameValuePairs.add(new BasicNameValuePair("FeedCycle","Feed Cancel"));  					
		}

    	nameValuePairs.add(new BasicNameValuePair("Update","Update"));  
    	try {  	

    		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  
 
    		HttpResponse resp = mHttpClient.execute(httppost);
    		final int status = resp.getStatusLine().getStatusCode();
    		if (status != HttpStatus.SC_OK) {
    			throw new HandlerException("Unexpected server response " + resp.getStatusLine()
    					+ " for " + httppost.getRequestLine());
    		}
    	} catch (HandlerException e) {
    		throw e;
    	} catch (ClientProtocolException e) {
    		throw new HandlerException("Problem reading remote response for "
    				+ httppost.getRequestLine(), e);
    	} catch (IOException e) {
    		throw new HandlerException("Problem reading remote response for "
    				+ httppost.getRequestLine(), e);
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
