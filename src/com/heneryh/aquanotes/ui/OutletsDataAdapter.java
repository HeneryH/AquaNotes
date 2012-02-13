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

package com.heneryh.aquanotes.ui;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
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

import com.heneryh.aquanotes.R;
import com.heneryh.aquanotes.io.ApexExecutor;
import com.heneryh.aquanotes.io.NewXmlHandler.HandlerException;
import com.heneryh.aquanotes.provider.AquaNotesDbContract;
import com.heneryh.aquanotes.provider.ScheduleContract;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Controllers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * A {@link android.widget.CursorAdapter} that renders a {@link OutletQuery}.
 */
public class OutletsDataAdapter extends CursorAdapter {
    private static final int ALL_ITEM_ID = Integer.MAX_VALUE;

    private Activity mActivity;
    private boolean mHasAllItem;
    private int mPositionDisplacement;
    private boolean mIsSessions = true;
    String[] outletStates;
    ArrayAdapter<String> adapter;
    
    public OutletsDataAdapter(Activity activity) {
    	super(activity, null);
        mActivity = activity;
      mHasAllItem = false;
      mPositionDisplacement =  0;
      Resources res = mActivity.getResources();
      outletStates = res.getStringArray(R.array.outlet_spinner);
      adapter=new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, outletStates);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    public void setHasAllItem(boolean hasAllItem) {
//        mHasAllItem = hasAllItem;
//        mPositionDisplacement = mHasAllItem ? 1 : 0;
    }

    public void setIsSessions(boolean isSessions) {
        mIsSessions = isSessions;
    }

    @Override
    public int getCount() {
        return super.getCount() + mPositionDisplacement;
    }

    public class ViewHolder
    {
    	ImageView icon;
    	Spinner spinner;
    	TextView title;
    	TextView subtitle;
    }

//	ViewHolder viewHolder;
//
//	if (convertView == null) {
//		convertView = mActivity.getLayoutInflater().inflate(
//				R.layout.list_item_outlet, parent, false);
//		viewHolder=new ViewHolder();
//		viewHolder.icon    = (ImageView)convertView.findViewById(android.R.id.icon1);
//		viewHolder.spinner = (Spinner)convertView.findViewById(R.id.spin);
//		viewHolder.spinner.setAdapter(adapter);
//		viewHolder.title = (TextView)convertView.findViewById(android.R.id.text1);
//		viewHolder.subtitle = (TextView)convertView.findViewById(R.id.outlet_subtitle);
//		convertView.setTag(viewHolder);
//	} else
//	{
//		viewHolder=(ViewHolder)convertView.getTag();
//	}
//	return convertView;
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        if (mHasAllItem && position == 0) {
//            if (convertView == null) {
//                convertView = mActivity.getLayoutInflater().inflate(
//                        R.layout.list_item_outlet, parent, false);
//            }
//
//            // Custom binding for the first item
//            ((TextView) convertView.findViewById(android.R.id.text1)).setText(
//                    "(" + mActivity.getResources().getString(mIsSessions
//                            ? R.string.all_sessions_title
//                            : R.string.all_sandbox_title)
//                            + ")");
//            convertView.findViewById(android.R.id.icon1).setVisibility(View.INVISIBLE);
//
//            return convertView;
//        }
//        return super.getView(position - mPositionDisplacement, convertView, parent);
//
//    }
    
    @Override
    public Object getItem(int position) {
        if (mHasAllItem && position == 0) {
            return null;
        }
        return super.getItem(position - mPositionDisplacement);
    }

    @Override
    public long getItemId(int position) {
        if (mHasAllItem && position == 0) {
            return ALL_ITEM_ID;
        }
        return super.getItemId(position - mPositionDisplacement);
    }

    @Override
    public boolean isEnabled(int position) {
        if (mHasAllItem && position == 0) {
            return true;
        }
        return super.isEnabled(position - mPositionDisplacement);
    }

    @Override
    public int getViewTypeCount() {
        // Add an item type for the "All" view.
        return super.getViewTypeCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mHasAllItem && position == 0) {
            return getViewTypeCount() - 1;
        }
        return super.getItemViewType(position - mPositionDisplacement);
    }

    /** {@inheritDoc} */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mActivity.getLayoutInflater().inflate(R.layout.list_item_outlet, parent,
                false);
    }

    /** {@inheritDoc} */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    	String outletName = cursor.getString(OutletDataViewQuery.NAME);
    	((TextView) view.findViewById(R.id.outlet_title)).setText(outletName);

    	String deviceId = cursor.getString(OutletDataViewQuery.DEVICE_ID);
        ((TextView) view.findViewById(R.id.outlet_subtitle)).setText(deviceId);

        Spinner spinner = (Spinner)view.findViewById(R.id.spin);
		spinner.setAdapter(adapter);
		
	   	Integer controllerId = cursor.getInt(OutletDataViewQuery.CONTROLLER_ID);
		
		spinner.setOnItemSelectedListener(new myOnItemSelectedListener(outletName, controllerId));
		

        // Assign track color to visible block
        String val = cursor.getString(OutletDataViewQuery.VALUE);
        final ImageView iconView = (ImageView) view.findViewById(android.R.id.icon1);
        Resources res = mActivity.getResources();
        
		////  Axx = Auto-On or Auto-Off
		////  OFF = Manual Off
		////  ON = Manual On

        if (val.equalsIgnoreCase("AON")) {
        	iconView.setImageDrawable(res.getDrawable(R.drawable.on));
        	spinner.setSelection(0);
        } else if (val.equalsIgnoreCase("AOF")) {
        	iconView.setImageDrawable(res.getDrawable(R.drawable.off));
        	spinner.setSelection(0);
        } else if (val.equalsIgnoreCase("OFF")) {
        	iconView.setImageDrawable(res.getDrawable(R.drawable.off));
        	spinner.setSelection(1);
        } else if(val.equalsIgnoreCase("ON")) {
        	iconView.setImageDrawable(res.getDrawable(R.drawable.on));
        	spinner.setSelection(2);
        } else  {
        	iconView.setImageDrawable(new ColorDrawable(Color.BLUE));
       }
    }
        
        
    public class myOnItemSelectedListener implements OnItemSelectedListener {
    	Boolean initialDisplay = true;
    	String name;
    	int cid;
    	
    	myOnItemSelectedListener(String x, int controllerId) {
    		name=x;
    		cid=controllerId;
    	}
    	@Override
    	public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
    		// your code here
    		if(!initialDisplay){
    			((ControllersActivity) mActivity).outletUpdate(cid, name, position);
    		}
    		initialDisplay=false;
    	}
    	@Override
    	public void onNothingSelected(AdapterView<?> parentView) {
    		// your code here
    	}
    }
        public interface OutletDataViewQuery {

            int _TOKEN = 0x1;
            
            String[] PROJECTION = {
//                    String _ID = "_id";
//                    String TYPE = "type";
//                    String VALUE = "value";
//                    String TIMESTAMP = "timestamp";
//                    String PARENT_ID = "parent_id";   	
//
//            		//\\
//            		Join
//            		\\//
//                    
//                    String NAME = "name";
//                    String DEVICE_ID = "device_id";
//                    String RESOURCE_ID = "resource_id";
//                    String CONTROLLER_ID = "controller_id";
            		BaseColumns._ID,
            		AquaNotesDbContract.OutletDataView.TYPE,
            		AquaNotesDbContract.OutletDataView.VALUE,
            		AquaNotesDbContract.OutletDataView.TIMESTAMP,
            		AquaNotesDbContract.OutletDataView.PARENT_ID,

            		AquaNotesDbContract.OutletDataView.NAME,
            		AquaNotesDbContract.OutletDataView.DEVICE_ID,
            		AquaNotesDbContract.OutletDataView.RESOURCE_ID,
            		AquaNotesDbContract.OutletDataView.CONTROLLER_ID,
             };
            int _ID = 0;
            int TYPE = 1;
            int VALUE = 2;
            int TIMESTAMP = 3;
            int PARENT_ID = 4;
            int NAME = 5;
            int DEVICE_ID = 6;
            int RESOURCE_ID = 7;
            int CONTROLLER_ID = 8;
         }
        
        private interface ControllersQuery {
            String[] PROJECTION = {
//                  String CONTROLLER_ID = "_id";
//                  String TITLE = "title";
//                  String WAN_URL = "wan_url";
//                  String LAN_URL = "wifi_url";
//                  String WIFI_SSID = "wifi_ssid";
//                  String USER = "user";
//                  String PW = "pw";
//                  String LAST_UPDATED = "last_updated";
//                  String UPDATE_INTERVAL = "update_i";
//                  String DB_SAVE_DAYS = "db_save_days";
//                  String CONTROLLER_TYPE = "controller_type";
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
                    AquaNotesDbContract.Controllers.WIDGET,
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
            int WIDGET = 11;
        }
        
}
