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
import com.heneryh.aquanotes.ui.BaseActivity;
import com.heneryh.aquanotes.ui.SessionDetailFragment;
import com.heneryh.aquanotes.ui.OutletsAdapter;
import com.heneryh.aquanotes.ui.OutletsFragment;
import com.heneryh.aquanotes.util.NotifyingAsyncQueryHandler;
import com.heneryh.aquanotes.util.UIUtils;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * A tablet-specific fragment that is a giant {@link android.widget.Spinner}-like widget. It shows
 * a {@link ListPopupWindow} containing a list of tracks, using {@link OutletsAdapter}.
 *
 * Requires API level 11 or later since {@link ListPopupWindow} is API level 11+.
 */
public class OutletsDropdownFragment extends Fragment implements
        NotifyingAsyncQueryHandler.AsyncQueryListener,
        AdapterView.OnItemClickListener,
        PopupWindow.OnDismissListener {

    public static final String EXTRA_NEXT_TYPE = "com.heneryh.aquanotes.extra.NEXT_TYPE";

    public static final String NEXT_TYPE_SESSIONS = "sessions";
    public static final String NEXT_TYPE_VENDORS = "vendors";

    private boolean mAutoloadTarget = true;
    private Cursor mCursor;
    private OutletsAdapter mAdapter;
    private String mNextType;

    private ListPopupWindow mListPopupWindow;
    private ViewGroup mRootView;
    private TextView mTitle;
    private TextView mAbstract;

    private NotifyingAsyncQueryHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);
        mAdapter = new OutletsAdapter(getActivity());

        if (savedInstanceState != null) {
            // Prevent auto-load behavior on orientation change.
            mAutoloadTarget = false;
        }

        reloadFromArguments(getArguments());
    }

    public void reloadFromArguments(Bundle arguments) {
        // Teardown from previous arguments
        if (mListPopupWindow != null) {
            mListPopupWindow.setAdapter(null);
        }
        if (mCursor != null) {
            getActivity().stopManagingCursor(mCursor);
            mCursor = null;
        }
        mHandler.cancelOperation(OutletsAdapter.OutletsViewQuery._TOKEN);

        // Load new arguments
        final Intent intent = BaseActivity.fragmentArgumentsToIntent(arguments);
        final Uri outletsUri = intent.getData();
        if (outletsUri == null) {
            return;
        }

        mNextType = intent.getStringExtra(EXTRA_NEXT_TYPE);

        // Filter our tracks query to only include those with valid results
        String[] projection = OutletsAdapter.OutletsViewQuery.PROJECTION;
        String selection = null;
//        if (OutletsFragment.NEXT_TYPE_SESSIONS.equals(mNextType)) {
//            // Only show tracks with at least one session
//            projection = OutletsAdapter.TracksQuery.PROJECTION_WITH_SESSIONS_COUNT;
//            selection = ScheduleContract.Tracks.SESSIONS_COUNT + ">0";
//
//        } else if (OutletsFragment.NEXT_TYPE_VENDORS.equals(mNextType)) {
//            // Only show tracks with at least one vendor
//            projection = OutletsAdapter.TracksQuery.PROJECTION_WITH_VENDORS_COUNT;
//            selection = ScheduleContract.Tracks.VENDORS_COUNT + ">0";
//        }

        // Start background query to load tracks
        mHandler.startQuery(OutletsAdapter.OutletsViewQuery._TOKEN, null, outletsUri, projection,
                selection, null, AquaNotesDbContract.Outlets.DEFAULT_SORT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_outlets_dropdown, null);
        mTitle = (TextView) mRootView.findViewById(R.id.track_title);
        mAbstract = (TextView) mRootView.findViewById(R.id.track_abstract);

        mRootView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mListPopupWindow = new ListPopupWindow(getActivity());
                mListPopupWindow.setAdapter(mAdapter);
                mListPopupWindow.setModal(true);
                mListPopupWindow.setContentWidth(400);
                mListPopupWindow.setAnchorView(mRootView);
                mListPopupWindow.setOnItemClickListener(OutletsDropdownFragment.this);
                mListPopupWindow.show();
                mListPopupWindow.setOnDismissListener(OutletsDropdownFragment.this);
            }
        });
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (getActivity() == null || cursor == null) {
            return;
        }

        mCursor = cursor;
        getActivity().startManagingCursor(mCursor);

        // If there was a last-opened track, load it. Otherwise load the first track.
        cursor.moveToFirst();
        String lastTrackID = UIUtils.getLastUsedTrackID(getActivity());
        if (lastTrackID != null) {
            while (!cursor.isAfterLast()) {
                if (lastTrackID.equals(cursor.getString(OutletsAdapter.OutletsViewQuery._ID))) {
                    break;
                }
                cursor.moveToNext();
            }

            if (cursor.isAfterLast()) {
                loadTrack(null, mAutoloadTarget);
            } else {
                loadTrack(cursor, mAutoloadTarget);
            }
        } else {
            loadTrack(null, mAutoloadTarget);
        }

        mAdapter.setHasAllItem(true);
//        mAdapter.setIsSessions(OutletsFragment.NEXT_TYPE_SESSIONS.equals(mNextType));
        mAdapter.changeCursor(mCursor);
    }

    /** {@inheritDoc} */
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Cursor cursor = (Cursor) mAdapter.getItem(position);
        loadTrack(cursor, true);

        if (cursor != null) {
            UIUtils.setLastUsedTrackID(getActivity(), cursor.getString(
                    OutletsAdapter.OutletsViewQuery._ID));
        } else {
            UIUtils.setLastUsedTrackID(getActivity(), ScheduleContract.Tracks.ALL_TRACK_ID);
        }

        if (mListPopupWindow != null) {
            mListPopupWindow.dismiss();
        }
    }

    public void loadTrack(Cursor cursor, boolean loadTargetFragment) {
        final String trackId;
        final int trackColor;
        final Resources res = getResources();

//        if (cursor != null) {
//            trackColor = cursor.getInt(OutletsAdapter.TracksQuery.TRACK_COLOR);
//            trackId = cursor.getString(OutletsAdapter.TracksQuery.TRACK_ID);
//
//            mTitle.setText(cursor.getString(OutletsAdapter.TracksQuery.TRACK_NAME));
//            mAbstract.setText(cursor.getString(OutletsAdapter.TracksQuery.TRACK_ABSTRACT));
//
//        } else {
//            trackColor = res.getColor(R.color.all_track_color);
//            trackId = ScheduleContract.Tracks.ALL_TRACK_ID;
//
//            mTitle.setText(OutletsFragment.NEXT_TYPE_SESSIONS.equals(mNextType)
//                    ? R.string.all_sessions_title
//                    : R.string.all_sandbox_title);
//            mAbstract.setText(OutletsFragment.NEXT_TYPE_SESSIONS.equals(mNextType)
//                    ? R.string.all_sessions_subtitle
//                    : R.string.all_sandbox_subtitle);
//        }
//
//        boolean isDark = UIUtils.isColorDark(trackColor);
//        mRootView.setBackgroundColor(trackColor);
//
//        if (isDark) {
//            mTitle.setTextColor(res.getColor(R.color.body_text_1_inverse));
//            mAbstract.setTextColor(res.getColor(R.color.body_text_2_inverse));
//            mRootView.findViewById(R.id.track_dropdown_arrow).setBackgroundResource(
//                    R.drawable.track_dropdown_arrow_light);
//        } else {
//            mTitle.setTextColor(res.getColor(R.color.body_text_1));
//            mAbstract.setTextColor(res.getColor(R.color.body_text_2));
//            mRootView.findViewById(R.id.track_dropdown_arrow).setBackgroundResource(
//                    R.drawable.track_dropdown_arrow_dark);
//        }
//
//        if (loadTargetFragment) {
//            final Intent intent = new Intent(Intent.ACTION_VIEW);
//            final Uri trackUri = ScheduleContract.Tracks.buildTrackUri(trackId);
//            intent.putExtra(SessionDetailFragment.EXTRA_TRACK, trackUri);
//
//            if (NEXT_TYPE_SESSIONS.equals(mNextType)) {
//                if (cursor == null) {
//                    intent.setData(ScheduleContract.Sessions.CONTENT_URI);
//                } else {
//                    intent.setData(ScheduleContract.Tracks.buildSessionsUri(trackId));
//                }
//            } else if (NEXT_TYPE_VENDORS.equals(mNextType)) {
//                if (cursor == null) {
//                    intent.setData(ScheduleContract.Vendors.CONTENT_URI);
//                } else {
//                    intent.setData(ScheduleContract.Tracks.buildVendorsUri(trackId));
//                }
//            }
//
//            ((BaseActivity) getActivity()).openActivityOrFragment(intent);
//        }
    }

    public void onDismiss() {
        mListPopupWindow = null;
    }
}
