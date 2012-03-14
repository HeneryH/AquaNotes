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

package com.heneryh.aquanotes.ui.controllers;

import com.heneryh.aquanotes.R;
import com.heneryh.aquanotes.provider.AquaNotesDbContract;
import com.heneryh.aquanotes.ui.BaseActivity;
import com.heneryh.aquanotes.util.ActivityHelper;
import com.heneryh.aquanotes.util.AnalyticsUtils;
import com.heneryh.aquanotes.util.NotifyingAsyncQueryHandler;
import com.heneryh.aquanotes.util.UIUtils;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import static com.heneryh.aquanotes.util.UIUtils.buildStyledSnippet;

/**
 * A {@link ListFragment} showing a list of outlet.
 */
public class OutletsXFragment extends ListFragment implements
        NotifyingAsyncQueryHandler.AsyncQueryListener {

    private static final String STATE_CHECKED_POSITION = "checkedPosition";

    private Cursor mCursor;
    private CursorAdapter mAdapter;
    private int mCheckedPosition = -1;
    private boolean mHasSetEmptyText = false;

    private NotifyingAsyncQueryHandler mHandler;
    private Handler mMessageQueueHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);
        reloadFromArguments(getArguments());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(android.R.layout.list_content, null);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));
        return root;
    }

    /**
     * 
    ListFragment has a default layout that consists of a single list view. However, if you desire, you can customize 
    the fragment layout by returning your own view hierarchy from onCreateView(LayoutInflater, ViewGroup, Bundle). To do 
    this, your view hierarchy must contain a ListView object with the id "@android:id/list" (or list if it's in code)

    		So my listview extends ListFragment and not containing list in xml , and directly we can setListAdapter in 
    		onActivityCreated rather than in onCreateView.
     */


    public void reloadFromArguments(Bundle arguments) {
        // Teardown from previous arguments
        if (mCursor != null) {
            getActivity().stopManagingCursor(mCursor);
            mCursor = null;
        }

        mCheckedPosition = -1;
        setListAdapter(null);

        mHandler.cancelOperation(OutletsViewQuery._TOKEN);

        // Load new arguments
        final Intent intent = BaseActivity.fragmentArgumentsToIntent(arguments);
        final Uri outletsUri = intent.getData();
        final int outletQueryToken;

        if (outletsUri == null) {
            return;
        }

        String[] projection;
            mAdapter = new OutletsAdapter(getActivity());
            projection = OutletsViewQuery.PROJECTION;
            outletQueryToken = OutletsViewQuery._TOKEN;

        setListAdapter(mAdapter);

        // Start background query to load sessions
        mHandler.startQuery(OutletsViewQuery._TOKEN, null, outletsUri, projection, null, null,
                AquaNotesDbContract.Outlets.DEFAULT_SORT);

        // If caller launched us with specific track hint, pass it along when
        // launching session details. Also start a query to load the track info.
//        mProbeUri = intent.getParcelableExtra(SessionDetailFragment.EXTRA_TRACK);
//        if (mProbeUri != null) {
//            mHandler.startQuery(ProbesViewQuery._TOKEN, mProbeUri, ProbesViewQuery.PROJECTION);
//        }
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
//            setEmptyText(getString(R.string.empty_outlets)); <-- re-do this since I had to turn if off for custom formats.
            mHasSetEmptyText = true;
        }
    }

    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (getActivity() == null) {
            return;
        }

        if (token == OutletsViewQuery._TOKEN) {
            onOutletsQueryComplete(cursor);
        } else {
            Log.d("OutletsXFragment/onQueryComplete", "Query complete, Not Actionable: " + token);
            cursor.close();
        }
    }

    /**
     * Handle {@link SessionsQuery} {@link Cursor}.
     */
    private void onOutletsQueryComplete(Cursor cursor) {
        if (mCursor != null) {
            // In case cancelOperation() doesn't work and we end up with consecutive calls to this
            // callback.
            getActivity().stopManagingCursor(mCursor);
            mCursor = null;
        }

        mCursor = cursor;
        getActivity().startManagingCursor(mCursor);
        mAdapter.changeCursor(mCursor);
        if (mCheckedPosition >= 0 && getView() != null) {
            getListView().setItemChecked(mCheckedPosition, true);
        }
    }

//    /**
//     * Handle {@link TracksQuery} {@link Cursor}.
//     */
//    private void onProbesQueryComplete(Cursor cursor) {
//        try {
//            if (!cursor.moveToFirst()) {
//                return;
//            }
//
//            // Use found track to build title-bar
//            ActivityHelper activityHelper = ((BaseActivity) getActivity()).getActivityHelper();
//            String trackName = cursor.getString(ProbesViewQuery.TRACK_NAME);
//            activityHelper.setActionBarTitle(trackName);
//            activityHelper.setActionBarColor(cursor.getInt(ProbesViewQuery.TRACK_COLOR));
//
//            AnalyticsUtils.getInstance(getActivity()).trackPageView("/Tracks/" + trackName);
//        } finally {
//            cursor.close();
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        mMessageQueueHandler.post(mRefreshSessionsRunnable);
        getActivity().getContentResolver().registerContentObserver(
                AquaNotesDbContract.Outlets.CONTENT_URI, true, mOutletsChangesObserver);
        if (mCursor != null) {
            mCursor.requery();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMessageQueueHandler.removeCallbacks(mRefreshSessionsRunnable);
        getActivity().getContentResolver().unregisterContentObserver(mOutletsChangesObserver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CHECKED_POSITION, mCheckedPosition);
    }

    /** {@inheritDoc} */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
//        // Launch viewer for specific session, passing along any track knowledge
//        // that should influence the title-bar.
//        final Cursor cursor = (Cursor)mAdapter.getItem(position);
//        final String sessionId = cursor.getString(cursor.getColumnIndex(
//                ScheduleContract.Sessions.SESSION_ID));
//        final Uri sessionUri = ScheduleContract.Sessions.buildSessionUri(sessionId);
//        final Intent intent = new Intent(Intent.ACTION_VIEW, sessionUri);
//        intent.putExtra(SessionDetailFragment.EXTRA_TRACK, mProbeUri);
//        ((BaseActivity) getActivity()).openActivityOrFragment(intent);
//
//        getListView().setItemChecked(position, true);
//        mCheckedPosition = position;
    }

    public void clearCheckedPosition() {
        if (mCheckedPosition >= 0) {
            getListView().setItemChecked(mCheckedPosition, false);
            mCheckedPosition = -1;
        }
    }

    /**
     * {@link CursorAdapter} that renders a {@link SessionsQuery}.
     */
    private class OutletsAdapter extends CursorAdapter {
        public OutletsAdapter(Context context) {
            super(context, null);
        }

        /** {@inheritDoc} */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getActivity().getLayoutInflater().inflate(R.layout.list_item_outletx, parent,
                    false);
        }

        /** {@inheritDoc} */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final TextView titleView = (TextView) view.findViewById(R.id.outlet_title);
            final TextView subtitleView = (TextView) view.findViewById(R.id.outlet_subtitle);

            titleView.setText(cursor.getString(OutletsViewQuery.TITLE));

            // Format time block this session occupies
            subtitleView.setText(cursor.getString(OutletsViewQuery.NAME));

            final boolean starred = false /*cursor.getInt(SessionsQuery.STARRED) != 0*/;
            view.findViewById(R.id.star_button).setVisibility(
                    starred ? View.VISIBLE : View.INVISIBLE);

            // Possibly indicate that the session has occurred in the past.
//            UIUtils.setSessionTitleColor(blockStart, blockEnd, titleView, subtitleView);
        }
    }


    private ContentObserver mOutletsChangesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (mCursor != null) {
                mCursor.requery();
            }
        }
    };

    private Runnable mRefreshSessionsRunnable = new Runnable() {
        public void run() {
            if (mAdapter != null) {
                // This is used to refresh session title colors.
                mAdapter.notifyDataSetChanged();
            }

            // Check again on the next quarter hour, with some padding to account for network
            // time differences.
            long nextQuarterHour = (SystemClock.uptimeMillis() / 900000 + 1) * 900000 + 5000;
            mMessageQueueHandler.postAtTime(mRefreshSessionsRunnable, nextQuarterHour);
        }
    };

    private interface OutletsViewQuery {

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
