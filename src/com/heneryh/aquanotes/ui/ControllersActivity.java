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
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Outlets;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Probes;
import com.heneryh.aquanotes.provider.ScheduleContract.Sessions;
import com.heneryh.aquanotes.provider.ScheduleContract.Vendors;
import com.heneryh.aquanotes.ui.phone.SessionDetailActivity;
import com.heneryh.aquanotes.ui.phone.ProbesDetailActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

/**
 * An activity that shows the user's starred sessions and sandbox companies. This activity can be
 * either single or multi-pane, depending on the device configuration. We want the multi-pane
 * support that {@link BaseMultiPaneActivity} offers, so we inherit from it instead of
 * {@link BaseSinglePaneActivity}.
 */
public class ControllersActivity extends BaseMultiPaneActivity {

    public static final String TAG_OUTLETS = "outlets";
    public static final String TAG_PROBES = "probes";

    private TabHost mTabHost;
    private TabWidget mTabWidget;

    private OutletsXFragment mOutletsFragment;
    private ProbesFragment mProbesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controllers);
        getActivityHelper().setupActionBar(getTitle(), 0);

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabWidget = (TabWidget) findViewById(android.R.id.tabs);
        mTabHost.setup();

        setupOutletsTab();
        setupProbesTab();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();

        ViewGroup detailContainer = (ViewGroup) findViewById(R.id.fragment_container_starred_detail);
        if (detailContainer != null && detailContainer.getChildCount() > 1) {
            findViewById(android.R.id.empty).setVisibility(View.GONE);
        }
    }

    /**
     * Build and add "sessions" tab.
     */
    private void setupOutletsTab() {
        // TODO: this is very inefficient and messy, clean it up
        FrameLayout fragmentContainer = new FrameLayout(this);
        fragmentContainer.setId(R.id.fragment_outlets);
        fragmentContainer.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.FILL_PARENT));
        ((ViewGroup) findViewById(android.R.id.tabcontent)).addView(fragmentContainer);

        final Intent intent = new Intent(Intent.ACTION_VIEW, Outlets.CONTENT_URI);

        final FragmentManager fm = getSupportFragmentManager();
        mOutletsFragment = (OutletsXFragment) fm.findFragmentByTag("outlets");
        if (mOutletsFragment == null) {
        	mOutletsFragment = new OutletsXFragment();
        	mOutletsFragment.setArguments(intentToFragmentArguments(intent));
            fm.beginTransaction()
                    .add(R.id.fragment_outlets, mOutletsFragment, "outlets")
                    .commit();
        }

        // Sessions content comes from reused activity
        mTabHost.addTab(mTabHost.newTabSpec(TAG_OUTLETS)
                .setIndicator(buildIndicator(R.string.starred_sessions))
                .setContent(R.id.fragment_outlets));
    }

    /**
     * Build and add "vendors" tab.
     */
    private void setupProbesTab() {
        // TODO: this is very inefficient and messy, clean it up
        FrameLayout fragmentContainer = new FrameLayout(this);
        fragmentContainer.setId(R.id.fragment_probes);
        fragmentContainer.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.FILL_PARENT));
        ((ViewGroup) findViewById(android.R.id.tabcontent)).addView(fragmentContainer);

        final Intent intent = new Intent(Intent.ACTION_VIEW, Probes.CONTENT_URI);

        final FragmentManager fm = getSupportFragmentManager();

        mProbesFragment = (ProbesFragment) fm.findFragmentByTag("probes");
        if (mProbesFragment == null) {
        	mProbesFragment = new ProbesFragment();
            mProbesFragment.setArguments(intentToFragmentArguments(intent));
            fm.beginTransaction()
                    .add(R.id.fragment_probes, mProbesFragment, "probes")
                    .commit();
        }

        // Vendors content comes from reused activity
        mTabHost.addTab(mTabHost.newTabSpec(TAG_PROBES)
                .setIndicator(buildIndicator(R.string.starred_vendors))
                .setContent(R.id.fragment_probes));
    }

    /**
     * Build a {@link View} to be used as a tab indicator, setting the requested string resource as
     * its label.
     */
    private View buildIndicator(int textRes) {
        final TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator,
                mTabWidget, false);
        indicator.setText(textRes);
        return indicator;
    }

    @Override
    public FragmentReplaceInfo onSubstituteFragmentForActivityLaunch(String activityClassName) {
        if (findViewById(R.id.fragment_container_starred_detail) != null) {
            // The layout we currently have has a detail container, we can add fragments there.
            findViewById(android.R.id.empty).setVisibility(View.GONE);
            if (SessionDetailActivity.class.getName().equals(activityClassName)) {
                clearSelectedItems();
                return new FragmentReplaceInfo(
                        SessionDetailFragment.class,
                        "session_detail",
                        R.id.fragment_container_starred_detail);
            } else if (ProbesDetailActivity.class.getName().equals(activityClassName)) {
                clearSelectedItems();
                return new FragmentReplaceInfo(
                        ProbesDetailFragment.class,
                        "vendor_detail",
                        R.id.fragment_container_starred_detail);
            }
        }
        return null;
    }

    private void clearSelectedItems() {
        if (mOutletsFragment != null) {
        	mOutletsFragment.clearCheckedPosition();
        }
        if (mProbesFragment != null) {
        	mProbesFragment.clearCheckedPosition();
        }
    }
}
