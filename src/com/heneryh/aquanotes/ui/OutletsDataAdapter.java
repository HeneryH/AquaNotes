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

import java.sql.Date;
import java.text.SimpleDateFormat;

import com.heneryh.aquanotes.R;
import com.heneryh.aquanotes.provider.AquaNotesDbContract;
import com.heneryh.aquanotes.provider.ScheduleContract;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.provider.BaseColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
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

    public OutletsDataAdapter(Activity activity) {
        super(activity, null);
        mActivity = activity;
      mHasAllItem = false;
      mPositionDisplacement =  0;
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
        String titleText = cursor.getString(OutletDataViewQuery.NAME) + " (" + cursor.getString(OutletDataViewQuery.DEVICE_ID)+ ")";
        textView.setText(titleText);

        // Assign track color to visible block
        String val = cursor.getString(OutletDataViewQuery.VALUE);
        final ImageView iconView = (ImageView) view.findViewById(android.R.id.icon1);
        Resources res = mActivity.getResources();
        if(val.equalsIgnoreCase("ON")) {
        	iconView.setImageDrawable(res.getDrawable(R.drawable.on));
        	//iconView = (ImageView) view.findViewById(R.drawable.on);
        } else if (val.equalsIgnoreCase("AON")) {
        	iconView.setImageDrawable(res.getDrawable(R.drawable.on));
        } else if (val.equalsIgnoreCase("AOF")) {
        	iconView.setImageDrawable(res.getDrawable(R.drawable.off));
        } else if (val.equalsIgnoreCase("OFF")) {
        	iconView.setImageDrawable(res.getDrawable(R.drawable.off));
        } else  {
        	iconView.setImageDrawable(new ColorDrawable(Color.BLUE));
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

}
