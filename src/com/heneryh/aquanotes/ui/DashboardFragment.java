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
import com.heneryh.aquanotes.configure.ConfigurePrefs;
import com.heneryh.aquanotes.provider.AquaNotesDbContract;
import com.heneryh.aquanotes.provider.ScheduleContract;
import com.heneryh.aquanotes.ui.phone.ScheduleActivity;
import com.heneryh.aquanotes.ui.tablet.ScheduleMultiPaneActivity;
import com.heneryh.aquanotes.ui.tablet.OutletsMultiPaneActivity;
import com.heneryh.aquanotes.ui.tablet.ProbesMultiPaneActivity;
import com.heneryh.aquanotes.util.AnalyticsUtils;
import com.heneryh.aquanotes.util.UIUtils;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DashboardFragment extends Fragment {

    public void fireTrackerEvent(String label) {
        AnalyticsUtils.getInstance(getActivity()).trackEvent(
                "Home Screen Dashboard", "Click", label, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container);

//        // Attach event handlers
//        root.findViewById(R.id.home_btn_schedule).setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                fireTrackerEvent("Schedule");
//                if (UIUtils.isHoneycombTablet(getActivity())) {
//                    startActivity(new Intent(getActivity(), ScheduleMultiPaneActivity.class));
//                } else {
//                    startActivity(new Intent(getActivity(), ScheduleActivity.class));
//                }
//                
//            }
//            
//        });

        root.findViewById(R.id.home_btn_outlets).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                fireTrackerEvent("Sessions");
                // Launch sessions list
                if (UIUtils.isHoneycombTablet(getActivity())) {
                    startActivity(new Intent(getActivity(), OutletsMultiPaneActivity.class));
                } else {
                    final Intent intent = new Intent(Intent.ACTION_VIEW,
                            AquaNotesDbContract.Outlets.CONTENT_URI);
                    intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_outlets_tracks));
                    startActivity(intent);
                }

            }
        });

        root.findViewById(R.id.home_btn_controllers).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                fireTrackerEvent("Controllers");
                // Launch list of sessions and vendors the user has starred
                startActivity(new Intent(getActivity(), ControllersActivity.class));                
            }
        });

        root.findViewById(R.id.home_btn_probes).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                fireTrackerEvent("Sandbox");
                // Launch vendors list
                if (UIUtils.isHoneycombTablet(getActivity())) {
                    startActivity(new Intent(getActivity(), ProbesMultiPaneActivity.class));
                } else {
                    final Intent intent = new Intent(Intent.ACTION_VIEW,
                            AquaNotesDbContract.Probes.CONTENT_URI);
//                    intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_probes_tracks));
//                    intent.putExtra(OutletsFragment.EXTRA_NEXT_TYPE,
//                            OutletsFragment.NEXT_TYPE_VENDORS);
                    startActivity(intent);
                }
            }
        });

        root.findViewById(R.id.home_btn_data).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Launch map of conference venue
                fireTrackerEvent("Map");
                if (UIUtils.isHoneycombTablet(getActivity())) {
                    startActivity(new Intent(getActivity(), ProbesMultiPaneActivity.class));
                } else {
                    final Intent intent = new Intent(Intent.ACTION_VIEW,
                            AquaNotesDbContract.ProbeDataView.CONTENT_P_URI);
//                    intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_probes_tracks));
//                    intent.putExtra(OutletsFragment.EXTRA_NEXT_TYPE,
//                            OutletsFragment.NEXT_TYPE_VENDORS);
                    startActivity(intent);
                }
            }
        });

        root.findViewById(R.id.home_btn_prefs).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
        				Intent i = new Intent(getActivity(), ConfigurePrefs.class);
        				int controllerId = 999; // use the special case of widget=999 , this is not a good solution <-- no longer used!
        				i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, controllerId);
        				startActivity(i);
                    }
                });

        return root;
    }
}
