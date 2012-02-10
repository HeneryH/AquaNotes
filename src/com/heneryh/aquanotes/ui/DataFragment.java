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
import com.heneryh.aquanotes.util.ActivityHelper;
import com.heneryh.aquanotes.util.AnalyticsUtils;
import com.heneryh.aquanotes.util.NotifyingAsyncQueryHandler;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
 * A {@link ListFragment} showing a list of controller probes.
 */
public class DataFragment extends ListFragment implements
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
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

//        ViewGroup root = (ViewGroup) inflater.inflate(android.R.layout.list_content, null);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_list_with_spinner, null);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));
        return root;
    }
    
    public void reloadFromArguments(Bundle arguments) {
        // Teardown from previous arguments
        if (mCursor != null) {
            getActivity().stopManagingCursor(mCursor);
            mCursor = null;
        }

        mCheckedPosition = -1;
        setListAdapter(null);

//        mHandler.cancelOperation(ProbeDataViewQuery._TOKEN);
        mHandler.cancelOperation(OutletDataViewQuery._TOKEN);

        // Load new arguments
        final Intent intent = BaseActivity.fragmentArgumentsToIntent(arguments);
        final Uri probesUri = intent.getData();
        final int probeQueryToken;

        if (probesUri == null) {
            return;
        }

        String[] projection;
        mAdapter = new ProbeDataAdapter(getActivity());
        projection = OutletDataViewQuery.PROJECTION;
        probeQueryToken = OutletDataViewQuery._TOKEN;

        setListAdapter(mAdapter);

        // Start background query to load vendors
        mHandler.startQuery(probeQueryToken, null, probesUri, projection, null, null, AquaNotesDbContract.Data.DEFAULT_SORT);
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
//            setEmptyText(getString(R.string.empty_probes));
            mHasSetEmptyText = true;
        }
    }


    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (getActivity() == null) {
            return;
        }

        if (token == ProbeDataViewQuery._TOKEN) {
            onProbeDataQueryComplete(cursor);
        } else {
            cursor.close();
        }
    }

    /**
     * Handle {@link VendorsQuery} {@link Cursor}.
     */
    private void onProbeDataQueryComplete(Cursor cursor) {
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
                ScheduleContract.Vendors.CONTENT_URI, true, mProbeDataChangesObserver);
        if (mCursor != null) {
            mCursor.requery();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getContentResolver().unregisterContentObserver(mProbeDataChangesObserver);
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
    private class ProbeDataAdapter extends CursorAdapter {
        public ProbeDataAdapter(Context context) {
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
        	
        	Integer cId = cursor.getInt(ProbeDataViewQuery.CONTROLLER_ID);
        	String titleText = cId.toString() + ":  " + cursor.getString(ProbeDataViewQuery.NAME);
            ((TextView) view.findViewById(R.id.probe_name)).setText(titleText);

            ((TextView) view.findViewById(R.id.probe_value)).setText(
            		cursor.getString(ProbeDataViewQuery.VALUE));

            Long timeL = cursor.getLong(ProbeDataViewQuery.TIMESTAMP);
			Date timestampD = new Date(timeL);
			SimpleDateFormat formatter = new SimpleDateFormat("M/d/yy h:mm a");
			String timestampS = "       " + formatter.format(timestampD);
            ((TextView) view.findViewById(R.id.probe_timestamp)).setText(timestampS);

            final boolean starred = false /*cursor.getInt(VendorsQuery.STARRED) != 0*/;
            view.findViewById(R.id.star_button).setVisibility(
                    starred ? View.VISIBLE : View.INVISIBLE);
        }
    }


    private ContentObserver mProbeDataChangesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (mCursor != null) {
                mCursor.requery();
            }
        }
    };

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
    
    public interface OutletDataViewQuery {

        int _TOKEN = 0x2;
        
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
//                
//                String NAME = "name";
//                String DEVICE_ID = "device_id";
//                String RESOURCE_ID = "resource_id";
//                String CONTROLLER_ID = "controller_id";
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
