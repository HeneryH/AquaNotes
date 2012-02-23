/*
 * Copyright 2012 
 *
 * Licensed under the xxxx
 */

package com.heneryh.aquanotes.ui;

import com.heneryh.aquanotes.R;
import com.heneryh.aquanotes.service.NightlyService;
import com.heneryh.aquanotes.service.SyncService;
import com.heneryh.aquanotes.util.AnalyticsUtils;
import com.heneryh.aquanotes.util.DetachableResultReceiver;
import com.heneryh.aquanotes.util.EulaHelper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Front-door {@link Activity} that displays high-level features the schedule application offers to
 * users. Depending on whether the device is a phone or an Android 3.0+ tablet, different layouts
 * will be used. For example, on a phone, the primary content is a {@link DashboardFragment},
 * whereas on a tablet, both a {@link DashboardFragment} and a {@link WebStreamFragment} are
 * displayed.
 */
public class HomeActivity extends BaseActivity {
    private static final String TAG = "HomeActivity";

//  private SyncStatusUpdaterFragment mSyncStatusUpdaterFragment;  // Using broadcast receiver now for sync status updates
    Context homeActContext;
    MyIntentReceiver statusIntentReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        homeActContext = this;

        if (!EulaHelper.hasAcceptedEula(this)) {
            EulaHelper.showEula(false, this);
        }

        AnalyticsUtils.getInstance(this).trackPageView("/Home");

        setContentView(R.layout.activity_home);
        getActivityHelper().setupActionBar(null, 0);  /** no-op on post GB, implicit since the ActionBarCompat == null */

//        FragmentManager fm = getSupportFragmentManager();
//
//        mSyncStatusUpdaterFragment = (SyncStatusUpdaterFragment) fm
//                .findFragmentByTag(SyncStatusUpdaterFragment.TAG);
//        if (mSyncStatusUpdaterFragment == null) {
//            mSyncStatusUpdaterFragment = new SyncStatusUpdaterFragment();
//            fm.beginTransaction().add(mSyncStatusUpdaterFragment,
//                    SyncStatusUpdaterFragment.TAG).commit();
//            createdNewSyncFrag = true;  // had to move the refresh() down to postCreate due to a race condition with the fragment starting
//        }

        
        final Intent intent = new Intent(this, NightlyService.class);
        startService(intent);
        
        statusIntentReceiver = new MyIntentReceiver();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupHomeActivity();
        triggerRefresh();
    }

 
    @Override
	public void onResume() {
		super.onResume();		
	    IntentFilter intentFilter = new IntentFilter(SyncService.STATUS_UPDATE);
	    registerReceiver(statusIntentReceiver, intentFilter); 
	}
 
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(statusIntentReceiver);
	}

	/**
	 * 
	 * @param menu
	 * @return
	 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

	/**
	 * If the user hits the 'Refresh Spinner' kick off a new polling event.
	 * @param item
	 * @return
	 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            triggerRefresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void triggerRefresh() {
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, SyncService.class);
//      intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mSyncStatusUpdaterFragment.mReceiver);
        startService(intent);
    }

    void updateRefreshStatus(boolean refreshing) {
        getActivityHelper().setRefreshActionButtonCompatState(refreshing);
    }
    
    
    public class MyIntentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle resultData = intent.getExtras();
			int resultCode = intent.getIntExtra(SyncService.STATUS_RESULT, 0);
			boolean mSyncing = false;
			switch (resultCode) {
			case SyncService.STATUS_RUNNING: {
				mSyncing = true;
				break;
			}
			case SyncService.STATUS_FINISHED: {
				mSyncing = false;
				break;
			}
			case SyncService.STATUS_ERROR: {
				// Error happened down in SyncService, show as toast.
				mSyncing = false;
				final String errorText = getString(R.string.toast_sync_error, resultData
						.getString(Intent.EXTRA_TEXT));
				Toast.makeText(homeActContext, errorText, Toast.LENGTH_LONG).show();
				break;
			}
			}
			updateRefreshStatus(mSyncing);
		}
    }
    
//    /**
//     * A non-UI fragment, retained across configuration changes, that updates its activity's UI
//     * when sync status changes.
//     */
//    public static class SyncStatusUpdaterFragment extends Fragment
//            implements DetachableResultReceiver.Receiver {
//        public static final String TAG = SyncStatusUpdaterFragment.class.getName();
//
//        private boolean mSyncing = false;
//        private DetachableResultReceiver mReceiver;
//
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            setRetainInstance(true);
//            mReceiver = new DetachableResultReceiver(new Handler());
//            mReceiver.setReceiver(this);
//        }
//
//        /** {@inheritDoc} */
//        public void onReceiveResult(int resultCode, Bundle resultData) {
//            HomeActivity activity = (HomeActivity) getActivity();
//            if (activity == null) {
//                return;
//            }
//
//            switch (resultCode) {
//                case SyncService.STATUS_RUNNING: {
//                    mSyncing = true;
//                    break;
//                }
//                case SyncService.STATUS_FINISHED: {
//                    mSyncing = false;
//                    break;
//                }
//                case SyncService.STATUS_ERROR: {
//                    // Error happened down in SyncService, show as toast.
//                    mSyncing = false;
//                    final String errorText = getString(R.string.toast_sync_error, resultData
//                            .getString(Intent.EXTRA_TEXT));
//                    Toast.makeText(activity, errorText, Toast.LENGTH_LONG).show();
//                    break;
//                }
//            }
//            activity.updateRefreshStatus(mSyncing);
//        }
//
//        @Override
//        public void onActivityCreated(Bundle savedInstanceState) {
//            super.onActivityCreated(savedInstanceState);
//            ((HomeActivity) getActivity()).updateRefreshStatus(mSyncing);
//        }
//    }
}
