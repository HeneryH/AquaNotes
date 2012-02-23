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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.heneryh.aquanotes.R;
import com.heneryh.aquanotes.util.AnalyticsUtils;
import com.heneryh.aquanotes.util.CatchNotesHelper;
import com.heneryh.aquanotes.util.UIUtils;

public class NotesFragment extends Fragment {

    private static final String TAG = "NotesFragment";
    
    private ViewGroup mRootView;
    private Uri mControllerUri;
    private String mControllerName;
    private String mControllerHashTag;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /**
         * The controller URI will be in the intent plus, depending on the situation
         * the probe name to graph can be in the extra.  If default, the probe name
         * will come in as "none" which will signal us to just graph the first one
         * found.
         */
        final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
        mControllerUri = intent.getData();
        mControllerName = intent.getExtras().getString("ControllerName");

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
        mControllerHashTag = "#" + mControllerName;
        final Intent newIntent = helper.createNoteIntent(
                getString(R.string.note_template_controller, mControllerName, "time here", mControllerHashTag));
        
        final Intent viewIntent = helper.viewNotesIntent(mControllerHashTag);

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
                        fireNotesEvent(R.string.notes_catch_market_title);
                    }
                });

        mRootView.findViewById(R.id.notes_catch_new_link).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        startActivity(newIntent);
                        fireNotesEvent(R.string.notes_catch_new_title);
                    }
                });

        mRootView.findViewById(R.id.notes_catch_view_link).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        startActivity(viewIntent);
                        fireNotesEvent(R.string.notes_catch_view_title);
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
  
    /*
     * Event structure:
     * Category -> "Session Details"
     * Action -> "Create Note", "View Note", etc
     * Label -> Session's Title
     * Value -> 0.
     */
    public void fireNotesEvent(int actionId) {
        AnalyticsUtils.getInstance(getActivity()).trackEvent(
                "Notes:", "test"/*getActivity().getString(actionId)*/, mControllerName, 0);
    }

    
}
