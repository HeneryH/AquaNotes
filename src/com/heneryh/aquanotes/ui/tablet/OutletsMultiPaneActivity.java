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

package com.heneryh.aquanotes.ui.tablet;

import com.heneryh.aquanotes.R;
import com.heneryh.aquanotes.provider.AquaNotesDbContract;
import com.heneryh.aquanotes.provider.ScheduleContract;
import com.heneryh.aquanotes.ui.BaseMultiPaneActivity;
import com.heneryh.aquanotes.ui.SessionDetailFragment;
import com.heneryh.aquanotes.ui.OutletsXFragment;
import com.heneryh.aquanotes.ui.OutletsDataFragment;
import com.heneryh.aquanotes.ui.phone.SessionDetailActivity;
import com.heneryh.aquanotes.ui.phone.SessionsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;

/**
 * A multi-pane activity, consisting of a {@link OutletsDropdownFragment}, a
 * {@link OutletsXFragment}, and {@link SessionDetailFragment}.
 *
 * This activity requires API level 11 or greater because {@link OutletsDropdownFragment} requires
 * API level 11.
 */
public class OutletsMultiPaneActivity extends BaseMultiPaneActivity {

    private OutletsDropdownFragment mTracksDropdownFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

//        Intent intent = new Intent();
//        intent.setData(ScheduleContract.Tracks.CONTENT_URI);
////        intent.putExtra(OutletsDataFragment.EXTRA_NEXT_TYPE, OutletsDataFragment.NEXT_TYPE_SESSIONS);
        Intent intent = new Intent();
        intent.setData(AquaNotesDbContract.Outlets.CONTENT_URI);
//        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_outlets_tracks));

        final FragmentManager fm = getSupportFragmentManager();
        mTracksDropdownFragment = (OutletsDropdownFragment) fm.findFragmentById(
                R.id.fragment_tracks_dropdown);
        mTracksDropdownFragment.reloadFromArguments(intentToFragmentArguments(intent));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();

        ViewGroup detailContainer = (ViewGroup)
                findViewById(R.id.fragment_container_session_detail);
        if (detailContainer != null && detailContainer.getChildCount() > 0) {
            findViewById(R.id.fragment_container_session_detail).setBackgroundColor(0xffffffff);
        }
    }

    @Override
    public FragmentReplaceInfo onSubstituteFragmentForActivityLaunch(String activityClassName) {
        if (SessionsActivity.class.getName().equals(activityClassName)) {
            return new FragmentReplaceInfo(
                    OutletsXFragment.class, // was OutletXFragment
                    "sessions",
                    R.id.fragment_container_sessions);
        } else if (SessionDetailActivity.class.getName().equals(activityClassName)) {
            findViewById(R.id.fragment_container_session_detail).setBackgroundColor(
                    0xffffffff);
            return new FragmentReplaceInfo(
                    SessionDetailFragment.class,
                    "session_detail",
                    R.id.fragment_container_session_detail);
        }
        return null;
    }
}
