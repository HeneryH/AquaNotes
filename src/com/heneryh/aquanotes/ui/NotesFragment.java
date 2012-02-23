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

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.FloatMath;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.SimpleXYSeries.ArrayFormat;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.androidplot.Plot;
import com.heneryh.aquanotes.R;
import com.heneryh.aquanotes.provider.AquaNotesDbContract;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Controllers;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Data;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Probes;
import com.heneryh.aquanotes.ui.ControllersActivity.TabManager.DummyTabFactory;
import com.heneryh.aquanotes.ui.ControllersActivity.TabManager.TabInfo;
import com.heneryh.aquanotes.util.CatchNotesHelper;
import com.heneryh.aquanotes.util.NotifyingAsyncQueryHandler;
import com.heneryh.aquanotes.util.UIUtils;

/**
 * A {@link ListFragment} showing a list of controller probes.
 */
public class NotesFragment extends Fragment {

    private static final String TAG = "NotesFragment";
    
    private ViewGroup mRootView;
    private String mTitleString;
    private String mHashtag;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setHasOptionsMenu(true);
    }
 
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mRootView = (ViewGroup) inflater.inflate(R.layout.tab_controller_notes, null);

        // Make powered-by clickable
        ((TextView) mRootView.findViewById(R.id.notes_powered_by)).setMovementMethod(
                LinkMovementMethod.getInstance());

        return mRootView;
    }
   
    @Override
    public void onResume() {
        super.onResume();
        updateNotesTab();
    }
    
    private void updateNotesTab() {
        final CatchNotesHelper helper = new CatchNotesHelper(getActivity());
        final boolean notesInstalled = helper.isNotesInstalledAndMinimumVersion();

        final Intent marketIntent = helper.notesMarketIntent();
        final Intent newIntent = helper.createNoteIntent(
                getString(R.string.note_template, mTitleString, "test"));
        
        final Intent viewIntent = helper.viewNotesIntent("test");

        // Set icons and click listeners
        ((ImageView) mRootView.findViewById(R.id.notes_catch_market_icon)).setImageDrawable(
                UIUtils.getIconForIntent(getActivity(), marketIntent));
        ((ImageView) mRootView.findViewById(R.id.notes_catch_new_icon)).setImageDrawable(
                UIUtils.getIconForIntent(getActivity(), newIntent));
        ((ImageView) mRootView.findViewById(R.id.notes_catch_view_icon)).setImageDrawable(
                UIUtils.getIconForIntent(getActivity(), viewIntent));

        // Set click listeners
        mRootView.findViewById(R.id.notes_catch_market_link).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        startActivity(marketIntent);
//                        fireNotesEvent(R.string.notes_catch_market_title);
                    }
                });

        mRootView.findViewById(R.id.notes_catch_new_link).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        startActivity(newIntent);
//                        fireNotesEvent(R.string.notes_catch_new_title);
                    }
                });

        mRootView.findViewById(R.id.notes_catch_view_link).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        startActivity(viewIntent);
//                        fireNotesEvent(R.string.notes_catch_view_title);
                    }
                });

        // Show/hide elements
        mRootView.findViewById(R.id.notes_catch_market_link).setVisibility(
                notesInstalled ? View.GONE : View.VISIBLE);
        mRootView.findViewById(R.id.notes_catch_market_separator).setVisibility(
                notesInstalled ? View.GONE : View.VISIBLE);

        mRootView.findViewById(R.id.notes_catch_new_link).setVisibility(
                !notesInstalled ? View.GONE : View.VISIBLE);
        mRootView.findViewById(R.id.notes_catch_new_separator).setVisibility(
                !notesInstalled ? View.GONE : View.VISIBLE);

        mRootView.findViewById(R.id.notes_catch_view_link).setVisibility(
                !notesInstalled ? View.GONE : View.VISIBLE);
        mRootView.findViewById(R.id.notes_catch_view_separator).setVisibility(
                !notesInstalled ? View.GONE : View.VISIBLE);
    }
    
    
}
