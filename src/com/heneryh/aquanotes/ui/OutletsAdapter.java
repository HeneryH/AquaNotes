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

import com.heneryh.aquanotes.R;
import com.heneryh.aquanotes.provider.AquaNotesDbContract;
import com.heneryh.aquanotes.provider.ScheduleContract;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.provider.BaseColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A {@link android.widget.CursorAdapter} that renders a {@link TracksQuery}.
 */
public class OutletsAdapter extends CursorAdapter {
    private static final int ALL_ITEM_ID = Integer.MAX_VALUE;

    private Activity mActivity;
    private boolean mHasAllItem;
    private int mPositionDisplacement;
    private boolean mIsSessions = true;

    public OutletsAdapter(Activity activity) {
        super(activity, null);
        mActivity = activity;
    }

    public void setHasAllItem(boolean hasAllItem) {
        mHasAllItem = hasAllItem;
        mPositionDisplacement = mHasAllItem ? 1 : 0;
    }

    public void setIsSessions(boolean isSessions) {
        mIsSessions = isSessions;
    }

    @Override
    public int getCount() {
        return super.getCount() + mPositionDisplacement;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mHasAllItem && position == 0) {
            if (convertView == null) {
                convertView = mActivity.getLayoutInflater().inflate(
                        R.layout.list_item_outlet, parent, false);
            }

            // Custom binding for the first item
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(
                    "(" + mActivity.getResources().getString(mIsSessions
                            ? R.string.all_sessions_title
                            : R.string.all_sandbox_title)
                            + ")");
            convertView.findViewById(android.R.id.icon1).setVisibility(View.INVISIBLE);

            return convertView;
        }
        return super.getView(position - mPositionDisplacement, convertView, parent);
    }

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
        final TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(cursor.getString(OutletsViewQuery.NAME));

        // Assign track color to visible block
        final ImageView iconView = (ImageView) view.findViewById(android.R.id.icon1);
        iconView.setImageDrawable(new ColorDrawable(1/*cursor.getInt(TracksQuery.TRACK_COLOR)*/));
    }

    public interface OutletsViewQuery {

        int _TOKEN = 0x1;
        
        String[] PROJECTION = {
            	//  String PROBE_ID = "_id";
            	//  String PROBE_NAME = "probe_name";
            	//  String DEVICE_ID = "device_id";
            	//  String TYPE = "probe_type";
            	//  String RESOURCE_ID = "resource_id";
            	//  String CONTROLLER_ID = "controller_id";
                    BaseColumns._ID,
                    AquaNotesDbContract.OutletsView.NAME,
                    AquaNotesDbContract.OutletsView.DEVICE_ID,
                    AquaNotesDbContract.OutletsView.RESOURCE_ID,
                    AquaNotesDbContract.OutletsView.CONTROLLER_ID,
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
                AquaNotesDbContract.OutletsView.TITLE,
                AquaNotesDbContract.OutletsView.WAN_URL,
                AquaNotesDbContract.OutletsView.LAN_URL,
                AquaNotesDbContract.OutletsView.WIFI_SSID,
                AquaNotesDbContract.OutletsView.USER,
                AquaNotesDbContract.OutletsView.PW,
                AquaNotesDbContract.OutletsView.LAST_UPDATED,
                AquaNotesDbContract.OutletsView.UPDATE_INTERVAL,
                AquaNotesDbContract.OutletsView.DB_SAVE_DAYS,
                AquaNotesDbContract.OutletsView.MODEL,
        };
        int _ID = 0;
        int NAME = 1;
        int DEVICE_ID = 2;
        int RESOURCE_ID = 3;
        int CONTROLLER_ID = 4;
        int TITLE = 5;
        int WAN_URL = 6;
        int LAN_URL = 7;
        int WIFI_SSID = 8;
        int USER = 9;
        int PW = 10;
        int LAST_UPDATED = 11;
        int UPDATE_INTERVAL = 12;
        int DB_SAVE_DAYS = 13;
        int MODEL = 14;
    }
    

}
