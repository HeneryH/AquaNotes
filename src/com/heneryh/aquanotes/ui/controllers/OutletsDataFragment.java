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

import com.heneryh.aquanotes.R;
import com.heneryh.aquanotes.io.ApexExecutor;
import com.heneryh.aquanotes.io.NewXmlHandler.HandlerException;
import com.heneryh.aquanotes.provider.AquaNotesDbContract;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Outlets;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Controllers;
import com.heneryh.aquanotes.ui.BaseActivity;
import com.heneryh.aquanotes.util.AnalyticsUtils;
import com.heneryh.aquanotes.util.NotifyingAsyncQueryHandler;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * A simple {@link ListFragment} that renders a list of tracks with available outlets or probes
 * (depending on {@link OutletsDataFragment#EXTRA_NEXT_TYPE}) using a {@link OutletsAdapter}.
 */
public class OutletsDataFragment extends ListFragment implements
        NotifyingAsyncQueryHandler.AsyncQueryListener {

    private OutletsDataAdapter mAdapter;
    private NotifyingAsyncQueryHandler mHandler;
    private Cursor mCursor;
    private Uri outletsUri;
    private Uri controllerUri;
    
    boolean hackBailOut = true; // this is a bug in my code for when the dashboard updates and call this fragments refreshSelf but this is not in foreground

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new OutletsDataAdapter(getActivity());
        mAdapter.setHasAllItem(false);
        reloadFromArguments(getArguments());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

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

        mHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);
        mHandler.cancelOperation(OutletsDataAdapter.OutletDataViewQuery._TOKEN);

        // Load new arguments
        final Intent intent = BaseActivity.fragmentArgumentsToIntent(arguments);
        outletsUri = intent.getData();
        controllerUri = Controllers.buildQueryControllerXUri(Integer.valueOf(Outlets.getControllerId(outletsUri)));
        final int outletQueryToken;

        if (outletsUri == null) {
            return;
        }
 

        // Start background query to load outlets
        String[] projection = OutletsDataAdapter.OutletDataViewQuery.PROJECTION;
        String selection = null;
        mHandler.startQuery(outletsUri, projection, selection, null,
                AquaNotesDbContract.Outlets.DEFAULT_SORT);

        setListAdapter(mAdapter);

    }
    
    public void reloadSelf(Uri newOutletsUri) {
    	outletsUri = newOutletsUri;

    	if (outletsUri == null || hackBailOut) {
    		return;
    	}

    	// Start background query to load outlets
    	String[] projection = OutletsDataAdapter.OutletDataViewQuery.PROJECTION;
    	String selection = null;
    	mHandler.startQuery(outletsUri, projection, selection, null,
    			AquaNotesDbContract.Outlets.DEFAULT_SORT);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }
    
    @Override
    public void onResume() {
        super.onResume();
//        getActivity().getContentResolver().registerContentObserver(
//                controllerUri, false, mOutletsChangesObserver);
        if (mCursor != null) {
            mCursor.requery();
        }
        hackBailOut=false;
    }

    @Override
    public void onPause() {
        super.onPause();
        hackBailOut=true;
//        getActivity().getContentResolver().unregisterContentObserver(mOutletsChangesObserver);
    }

    private ContentObserver mOutletsChangesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            // Start background query to load outlets
            String[] projection = OutletsDataAdapter.OutletDataViewQuery.PROJECTION;
            String selection = null;
            mHandler.startQuery(outletsUri, projection, selection, null,
                    AquaNotesDbContract.Outlets.DEFAULT_SORT);

        }
    };

    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
    		if (mCursor != null) {
    			// In case cancelOperation() doesn't work and we end up with consecutive calls to this
    			// callback.
    			getActivity().stopManagingCursor(mCursor);
    			mCursor = null;
    		}

    		mCursor = cursor;
    		getActivity().startManagingCursor(mCursor);
    		mAdapter.changeCursor(mCursor);
    		//        if (mCheckedPosition >= 0 && getView() != null) {
    		//            getListView().setItemChecked(mCheckedPosition, true);
    		//        }
    }

    /** {@inheritDoc} */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Cursor cursor = (Cursor) mAdapter.getItem(position);
        final String outletId;
            outletId = cursor.getString(OutletsDataAdapter.OutletDataViewQuery._ID);
    }
    
    

}
