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
import com.heneryh.aquanotes.util.ActivityHelper;
import com.heneryh.aquanotes.util.AnalyticsUtils;
import com.heneryh.aquanotes.util.NotifyingAsyncQueryHandler;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.text.Spannable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import static com.heneryh.aquanotes.util.UIUtils.buildStyledSnippet;

/**
 * A {@link ListFragment} showing a list of controller probes.
 */
public class ProbesFragment extends ListFragment implements
        NotifyingAsyncQueryHandler.AsyncQueryListener {

    private static final String STATE_CHECKED_POSITION = "checkedPosition";

    private Cursor mCursor;
    private CursorAdapter mAdapter;
    private int mCheckedPosition = -1;
    private boolean mHasSetEmptyText = false;

    private NotifyingAsyncQueryHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);
        reloadFromArguments(getArguments());
    }

    public void reloadFromArguments(Bundle arguments) {
        // Teardown from previous arguments
        if (mCursor != null) {
            getActivity().stopManagingCursor(mCursor);
            mCursor = null;
        }

        mCheckedPosition = -1;
        setListAdapter(null);

        mHandler.cancelOperation(ProbesViewQuery._TOKEN);

        // Load new arguments
        final Intent intent = BaseActivity.fragmentArgumentsToIntent(arguments);
        final Uri probesUri = intent.getData();
        final int probeQueryToken;

        if (probesUri == null) {
            return;
        }

        String[] projection;
        mAdapter = new ProbesAdapter(getActivity());
        projection = ProbesViewQuery.PROJECTION;
        probeQueryToken = ProbesViewQuery._TOKEN;

        setListAdapter(mAdapter);

        // Start background query to load vendors
        mHandler.startQuery(probeQueryToken, null, probesUri, projection, null, null,
                AquaNotesDbContract.Probes.DEFAULT_SORT);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        if (savedInstanceState != null) {
            mCheckedPosition = savedInstanceState.getInt(STATE_CHECKED_POSITION, -1);
        }

        if (!mHasSetEmptyText) {
            // Could be a bug, but calling this twice makes it become visible when it shouldn't
            // be visible.
            setEmptyText(getString(R.string.empty_probes));
            mHasSetEmptyText = true;
        }
    }


    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (getActivity() == null) {
            return;
        }

        if (token == ProbesViewQuery._TOKEN) {
            onProbesQueryComplete(cursor);
        } else {
            cursor.close();
        }
    }

    /**
     * Handle {@link VendorsQuery} {@link Cursor}.
     */
    private void onProbesQueryComplete(Cursor cursor) {
        if (mCursor != null) {
            // In case cancelOperation() doesn't work and we end up with consecutive calls to this
            // callback.
            getActivity().stopManagingCursor(mCursor);
            mCursor = null;
        }

        // TODO(romannurik): stopManagingCursor on detach (throughout app)
        mCursor = cursor;
        getActivity().startManagingCursor(mCursor);
        mAdapter.changeCursor(mCursor);
        if (mCheckedPosition >= 0 && getView() != null) {
            getListView().setItemChecked(mCheckedPosition, true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getContentResolver().registerContentObserver(
                ScheduleContract.Vendors.CONTENT_URI, true, mProbeChangesObserver);
        if (mCursor != null) {
            mCursor.requery();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getContentResolver().unregisterContentObserver(mProbeChangesObserver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CHECKED_POSITION, mCheckedPosition);
    }

    /** {@inheritDoc} */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
//        // Launch viewer for specific vendor.
//        final Cursor cursor = (Cursor)mAdapter.getItem(position);
//        final String vendorId = cursor.getString(VendorsQuery.VENDOR_ID);
//        final Uri vendorUri = ScheduleContract.Vendors.buildVendorUri(vendorId);
//        ((BaseActivity) getActivity()).openActivityOrFragment(new Intent(Intent.ACTION_VIEW,
//                vendorUri));

        getListView().setItemChecked(position, true);
        mCheckedPosition = position;
    }

    public void clearCheckedPosition() {
        if (mCheckedPosition >= 0) {
            getListView().setItemChecked(mCheckedPosition, false);
            mCheckedPosition = -1;
        }
    }

    /**
     * {@link CursorAdapter} that renders a {@link VendorsQuery}.
     */
    private class ProbesAdapter extends CursorAdapter {
        public ProbesAdapter(Context context) {
            super(context, null);
        }

        /** {@inheritDoc} */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getActivity().getLayoutInflater().inflate(R.layout.list_item_probe,
                    parent, false);
        }

        /** {@inheritDoc} */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view.findViewById(R.id.probe_name)).setText(
                    cursor.getString(ProbesViewQuery.WAN_URL));

            ((TextView) view.findViewById(R.id.probe_value)).setText(
            		cursor.getString(ProbesViewQuery.NAME));

            final boolean starred = false /*cursor.getInt(VendorsQuery.STARRED) != 0*/;
            view.findViewById(R.id.star_button).setVisibility(
                    starred ? View.VISIBLE : View.INVISIBLE);
        }
    }


    private ContentObserver mProbeChangesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (mCursor != null) {
                mCursor.requery();
            }
        }
    };

    
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
    
	private interface xxxProbesQuery {
        int _TOKEN = 0x2;
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
        int NAME = 1;
        int RESOURCE_ID = 2;
        int CONTROLLER_ID = 3;
    }

    private interface ProbesViewQuery {

        int _TOKEN = 0x1;
        
        String[] PROJECTION = {
            	//  String PROBE_ID = "_id";
            	//  String PROBE_NAME = "probe_name";
            	//  String DEVICE_ID = "device_id";
            	//  String TYPE = "probe_type";
            	//  String RESOURCE_ID = "resource_id";
            	//  String CONTROLLER_ID = "controller_id";
                    BaseColumns._ID,
                    AquaNotesDbContract.ProbesView.NAME,
                    AquaNotesDbContract.ProbesView.RESOURCE_ID,
                    AquaNotesDbContract.ProbesView.CONTROLLER_ID,
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
                AquaNotesDbContract.ProbesView.TITLE,
                AquaNotesDbContract.ProbesView.WAN_URL,
                AquaNotesDbContract.ProbesView.LAN_URL,
                AquaNotesDbContract.ProbesView.WIFI_SSID,
                AquaNotesDbContract.ProbesView.USER,
                AquaNotesDbContract.ProbesView.PW,
                AquaNotesDbContract.ProbesView.LAST_UPDATED,
                AquaNotesDbContract.ProbesView.UPDATE_INTERVAL,
                AquaNotesDbContract.ProbesView.DB_SAVE_DAYS,
                AquaNotesDbContract.ProbesView.MODEL,
        };
        int _ID = 0;
        int NAME = 1;
        int RESOURCE_ID = 2;
        int CONTROLLER_ID = 3;
        int TITLE = 4;
        int WAN_URL = 5;
        int LAN_URL = 6;
        int WIFI_SSID = 7;
        int USER = 8;
        int PW = 9;
        int LAST_UPDATED = 10;
        int UPDATE_INTERVAL = 11;
        int DB_SAVE_DAYS = 12;
        int MODEL = 13;
    }
    


}
