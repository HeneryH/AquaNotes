/*
 * Copyright 2012 
 *
 * Licensed under the xxxx
 */

package com.heneryh.aquanotes.ui;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Controllers;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Data;
import com.heneryh.aquanotes.service.SyncService;
import com.heneryh.aquanotes.ui.HomeActivity.SyncStatusUpdaterFragment;
import com.heneryh.aquanotes.ui.widget.BlockView;
import com.heneryh.aquanotes.ui.widget.ObservableScrollView;
import com.heneryh.aquanotes.ui.widget.Workspace;
import com.heneryh.aquanotes.util.AnalyticsUtils;
import com.heneryh.aquanotes.util.DetachableResultReceiver;
import com.heneryh.aquanotes.util.MotionEventUtils;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An activity that shows the user's controllers in left/right swipeable screens, each
 * screen has tabs for probes, outlets and controllers. This activity can be
 * either single or multi-pane, depending on the device configuration. We want the multi-pane
 * support that {@link BaseMultiPaneActivity} offers, so we inherit from it instead of
 * {@link BaseSinglePaneActivity}.
 */
public class ControllersActivity extends BaseMultiPaneActivity implements
								NotifyingAsyncQueryHandler.AsyncQueryListener,
								View.OnClickListener  {

    private SyncStatusUpdaterFragment mSyncStatusUpdaterFragment;

    /**
	 * Base tags for the tabs in each controller.  The actual tag will be the base+id.
	 */
	public static final String TAG_OUTLETS = "outlets";
	public static final String TAG_PROBES = "probes";
	public static final String TAG_NOTES = "notes";

	/**
	 * Handler to process database query complete events
	 */
	private NotifyingAsyncQueryHandler mHandler;

	/**
	 * Reference to the database resolver for this activity
	 */
	private ContentResolver dbResolverControllerAct;

	/**
	 * Abstract out the IO to the controller
	 */
	private ApexExecutor mRemoteExecutor;

	/**
	 * Keep track of if an 'update' is coming from the initial fragment setup or from a 
	 * database change sensor
	 */
	boolean controllerUpdateFlag = false;

	/**
	 * Views for the main shell view
	 */
	private TextView mWorkspaceTitleView;
	private View mLeftIndicator;
	private View mRightIndicator;
	private Workspace mWorkspace;

	/**
	 * A helper class containing object references related to a particular controller tab-view.
	 */
	private List<Ctlr> mCtlrs = new ArrayList<Ctlr>();

	private class Ctlr {
		private int index;

		/** 
		 * Views for each swipe pane for each controller
		 */
		private ViewGroup mRootView; // Host for the tab view within the fragment.  Below the L/R and Workspace Title
		private FrameLayout scrollView;

		private TabHost mTabHost;
		private TabWidget mTabWidget;
//      private TabManager mTabManager; // alternate way of managing tabs

		private String mTitleString;
		private TextView mTitleView;
		private TextView mSubtitleView;
		private CompoundButton mStarredView;
		private String mSubtitle;

		FrameLayout probeFragmentContainer;
		FrameLayout outletFragmentContainer;

		private OutletsDataFragment mOutletsFragment;
		private ProbesFragment mProbesFragment;
//        private NotesFragment mNotesFragment;

		private Integer mControllerId;
		private Uri mControllerUri;
		private Long mTimestamp;
	}

	/**
	 * 
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        AnalyticsUtils.getInstance(this).trackPageView("/Controllers");

        getActivityHelper().setupActionBar(null, 0);

		/**
		 * The handler in this case only gets the list of active controllers
		 * then creates a swipeable tab-view for each.
		 */
		mHandler = new NotifyingAsyncQueryHandler(getContentResolver(), this);

		/**
		 * The main view is a left/right scroll button a title and a workspace below it for content
		 */
		setContentView(R.layout.activity_controllers);
		mLeftIndicator = findViewById(R.id.indicator_left);
		mWorkspaceTitleView = (TextView) findViewById(R.id.controller_ws_title);
		mRightIndicator = findViewById(R.id.indicator_right);
		mWorkspace = (Workspace) findViewById(R.id.workspace);

		/**
		 * Add click listeners for the scroll buttons.
		 */
		mLeftIndicator.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if ((motionEvent.getAction() & MotionEventUtils.ACTION_MASK)
						== MotionEvent.ACTION_DOWN) {
					mWorkspace.scrollLeft();
					return true;
				}
				return false;
			}
		});
		mLeftIndicator.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mWorkspace.scrollLeft();
			}
		});

		mRightIndicator.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if ((motionEvent.getAction() & MotionEventUtils.ACTION_MASK)
						== MotionEvent.ACTION_DOWN) {
					mWorkspace.scrollRight();
					return true;
				}
				return false;
			}
		});
		mRightIndicator.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mWorkspace.scrollRight();
			}
		});

		/**
		 * Need at least one tab-view for the next step to continue
		 * so put a dummy one in.  We'll swap this out when we get real data.
		 */
		 setupCtlr(new Ctlr(), null);

		 /**
		  * Set the scroll listener for the workspace
		  */
		 mWorkspace.setOnScrollListener(new Workspace.OnScrollListener() {
			 public void onScroll(float screenFraction) {
				 updateWorkspaceHeader(Math.round(screenFraction));
			 }
		 }, true);

		 /**
		  * Interface to the database which is passed into the remoteExecutor.  Is there an advantage to
		  * having a centralized one rather than each getting there own???  Might want to look at this more.
		  * Seems like the answer is that you need the context to get the resolver
		  */
		 dbResolverControllerAct = getContentResolver();

		 /**
		  * Setup stuff below for the ApexExecutor that will handle the spinner-select and outlet change function
		  */

		 /**
		  * helper class for defaultHttpClient seen below
		  */
		 final HttpClient httpClient = getHttpClient(this);


		 /**
		  * Create the executor for the controller of choice.  Now it is just the apex but I can see using
		  * other ones like the DA.  Pass in the http client and database resolver it will need to do its job.
		  */
		 mRemoteExecutor = new ApexExecutor(this, httpClient, dbResolverControllerAct);

		 
		 FragmentManager fm = getSupportFragmentManager();

		 mSyncStatusUpdaterFragment = (SyncStatusUpdaterFragment) fm
				 .findFragmentByTag(SyncStatusUpdaterFragment.TAG);
		 if (mSyncStatusUpdaterFragment == null) {
			 mSyncStatusUpdaterFragment = new SyncStatusUpdaterFragment();
			 fm.beginTransaction().add(mSyncStatusUpdaterFragment,
					 SyncStatusUpdaterFragment.TAG).commit();
		 }

	}

	/**
	 * 
	 * @param savedInstanceState
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getActivityHelper().setupSubActivity();
	}

	/**
	 * 
	 * @param menu
	 * @return
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.refresh_menu_items, menu);
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

	/**
	 * Be clear here whether we are talking about a database refresh or actually going out to the
	 * controller for fresh data.  In this case we want to really go get new data.
	 */
	private void triggerRefresh() {
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, SyncService.class);
        intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mSyncStatusUpdaterFragment.mReceiver);
		startService(intent);
	}

	private void updateRefreshStatus(boolean refreshing) {
		getActivityHelper().setRefreshActionButtonCompatState(refreshing);
	}


	/**
	 * The workspace header shows the a title and figures out whether or not to
	 * activate the << or >> scroll buttons.
	 * 
	 * @param ctlrIndex
	 */
	public void updateWorkspaceHeader(int ctlrIndex) {

		Ctlr ctlr = mCtlrs.get(ctlrIndex);
		mWorkspaceTitleView.setText(ctlr.mTitleString);
		//getActivityHelper().setupActionBar(ctlr.mTitleString/*getTitle()*/, 0);

		mLeftIndicator
		.setVisibility((ctlrIndex != 0) ? View.VISIBLE : View.INVISIBLE);
		mRightIndicator
		.setVisibility((ctlrIndex < mCtlrs.size() - 1) ? View.VISIBLE : View.INVISIBLE);
	}

	/**
	 * Prepare the TabHost for this controller and inflate it within a workspace pane.
	 * This will inflate the below as defined in R.layout.controllers_list_content_tabbed:
	 *  Header:
	 *      Star, Title & Subtitle
	 *  TabView:
	 *      Probes (ListActivity), Outlets (List Activity), Notes (Custom Fragment)
	 * 
	 * @param controllerId
	 * @param title
	 */
	private void setupCtlr(Ctlr ctlr, Cursor cursor) {

		LayoutInflater inflater = getLayoutInflater();

		/**
		 * Setup views
		 */
		ctlr.mRootView = (ViewGroup) inflater.inflate(R.layout.controllers_tabbed_content, null);
		ctlr.scrollView = (FrameLayout) ctlr.mRootView.findViewById(R.id.controllers_scroll);
		//ctlr.scrollView.setOnScrollListener(this);

		ctlr.mTitleView = (TextView) ctlr.mRootView.findViewById(R.id.controller_title);
		ctlr.mSubtitleView = (TextView) ctlr.mRootView.findViewById(R.id.controller_subtitle);
		ctlr.mStarredView = (CompoundButton) ctlr.mRootView.findViewById(R.id.star_button);
		ctlr.mStarredView.setFocusable(true);
		ctlr.mStarredView.setClickable(true);

//        	ctlr.mTabHost = (TabHost) findViewById(android.R.id.tabhost);
//        	ctlr.mTabWidget = (TabWidget) findViewById(android.R.id.tabs);
//        	ctlr.mTabHost.setup();

		ctlr.mTabHost = (TabHost) ctlr.mRootView.findViewById(android.R.id.tabhost);
		ctlr.mTabWidget = (TabWidget) ctlr.mRootView.findViewById(android.R.id.tabs);
		ctlr.mTabHost.setup();
//        	ctlr.mTabManager = new TabManager(this, ctlr.mTabHost, R.id.realtabcontent);
		
		ctlr.index = mCtlrs.size();
		if(cursor!=null) {
			ctlr.mControllerId=cursor.getInt(ControllersQuery._ID);
			ctlr.mTitleString = cursor.getString(ControllersQuery.TITLE);
			ctlr.mTimestamp = cursor.getLong(ControllersQuery.LAST_UPDATED);
			ctlr.mControllerUri=Controllers.buildUpdateControllerXUri(ctlr.mControllerId);
			getContentResolver().registerContentObserver(ctlr.mControllerUri, false, mControllerChangesObserver);
		} else {
			ctlr.mControllerId=-1;
			ctlr.mTitleString = "Empty";
			ctlr.mTimestamp = (long) 0;
			ctlr.mControllerUri=null;
		}


		if(ctlr.mControllerId>=0) {
			setupProbesTab(ctlr);
			setupOutletsTab(ctlr);
//        		setupNotesTab(ctlr);
		}

		mWorkspace.addView(ctlr.mRootView);
		mCtlrs.add(ctlr);
	}

	private void DeleteCtlr(Ctlr ctlr) {
		mWorkspace.removeView(ctlr.mRootView);
		mCtlrs.remove(ctlr.index);
	}


	/**
	 * Build and add "probes" tab.
	 */
	private void setupProbesTab(Ctlr ctlr) {
		String tagSpec = TAG_PROBES + "_" + ctlr.mControllerId.toString().replace('-', 'n');

		// TODO: this is very inefficient and messy, clean it up

		int Rid;
		switch(ctlr.index){
		case 0:
			Rid = R.id.fragment_probes_0;
			break;
		case 1:
			Rid = R.id.fragment_probes_1;
			break;
		case 2:
			Rid = R.id.fragment_probes_2;
			break;
		case 3:
			Rid = R.id.fragment_probes_3;
			break;
		case 4:
			Rid = R.id.fragment_probes_4;
			break;
		case 5:
			Rid = R.id.fragment_probes_5;
			break;
		case 6:
			Rid = R.id.fragment_probes_6;
			break;
		case 7:
			Rid = R.id.fragment_probes_7;
			break;
		case 8:
			Rid = R.id.fragment_probes_8;
			break;
		case 9:
			Rid = R.id.fragment_probes_9;
			break;
		default:
			Rid=0;
		}
		ctlr.probeFragmentContainer = new FrameLayout(this);
		ctlr.probeFragmentContainer.setId(Rid);
//            ctlr.probeFragmentContainer.setLayoutParams(
//                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
//                            ViewGroup.LayoutParams.FILL_PARENT));
		((ViewGroup) ctlr.mRootView.findViewById(android.R.id.tabcontent)).addView(ctlr.probeFragmentContainer);

//            final Intent intent = new Intent(Intent.ACTION_VIEW, Probes.buildQueryProbesUri(ctlr.mControllerUri));
		final Intent intent = new Intent(Intent.ACTION_VIEW, Data.buildQueryPDataAtUri(ctlr.mControllerId, ctlr.mTimestamp));

		final FragmentManager fm = getSupportFragmentManager();

		ctlr.mProbesFragment = (ProbesFragment) fm.findFragmentByTag(tagSpec);
		if (ctlr.mProbesFragment == null) {
			ctlr.mProbesFragment = new ProbesFragment();
			ctlr.mProbesFragment.setArguments(intentToFragmentArguments(intent));
			fm.beginTransaction()
			.add(Rid, ctlr.mProbesFragment, tagSpec)
			.commit();
		}

		ctlr.mTabHost.addTab(ctlr.mTabHost.newTabSpec(tagSpec)
				.setIndicator(buildIndicator(ctlr, R.string.starred_vendors))
				.setContent(Rid));

//          // Supply controller uri as an argument.
//          Bundle args = new Bundle();
//          args.putInt("controllerId", ctlr.mControllerId);

//            ctlr.mTabManager.addTab(ctlr.mTabHost.newTabSpec(tagSpec)
//            										.setIndicator(buildIndicator(ctlr, R.string.starred_vendors))
//            										.setContent(R.id.fragment_probes),
//            						ProbesFragment.class, 
//									null /*args*/);  	
	}

	/**
	 * Build and add "sessions" tab.
	 */
	private void setupOutletsTab(Ctlr ctlr) {
		String tagSpec = TAG_OUTLETS + "_" + ctlr.mControllerId.toString().replace('-', 'n');

		int Rid;
		switch(ctlr.index){
		case 0:
			Rid = R.id.fragment_outlets_0;
			break;
		case 1:
			Rid = R.id.fragment_outlets_1;
			break;
		case 2:
			Rid = R.id.fragment_outlets_2;
			break;
		case 3:
			Rid = R.id.fragment_outlets_3;
			break;
		case 4:
			Rid = R.id.fragment_outlets_4;
			break;
		case 5:
			Rid = R.id.fragment_outlets_5;
			break;
		case 6:
			Rid = R.id.fragment_outlets_6;
			break;
		case 7:
			Rid = R.id.fragment_outlets_7;
			break;
		case 8:
			Rid = R.id.fragment_outlets_8;
			break;
		case 9:
			Rid = R.id.fragment_outlets_9;
			break;
		default:
			Rid=0;
		}

		// TODO: this is very inefficient and messy, clean it up
		ctlr.outletFragmentContainer = new FrameLayout(this);
		ctlr.outletFragmentContainer.setId(Rid);
//        ctlr.outletFragmentContainer.setLayoutParams(
//                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
//                        ViewGroup.LayoutParams.FILL_PARENT));
		((ViewGroup) ctlr.mRootView.findViewById(android.R.id.tabcontent)).addView(ctlr.outletFragmentContainer);

		final Intent intent = new Intent(Intent.ACTION_VIEW, Data.buildQueryODataAtUri(ctlr.mControllerId, ctlr.mTimestamp));
//        final Intent intent = new Intent(Intent.ACTION_VIEW, Data.buildQueryAllOutletDataUri(ctlr.mControllerId));


		final FragmentManager fm = getSupportFragmentManager();
		ctlr.mOutletsFragment = (OutletsDataFragment) fm.findFragmentByTag(tagSpec); // was OutletXFragment
		if (ctlr.mOutletsFragment == null) {
			ctlr.mOutletsFragment = new OutletsDataFragment(); // was OutletXFragment
			ctlr.mOutletsFragment.setArguments(intentToFragmentArguments(intent));
			fm.beginTransaction()
			.add(Rid, ctlr.mOutletsFragment, tagSpec)
			.commit();
		}

		ctlr.mTabHost.addTab(ctlr.mTabHost.newTabSpec(tagSpec)
				.setIndicator(buildIndicator(ctlr, R.string.starred_sessions))
				.setContent(Rid));

//      // Supply controller uri as an argument.
//      Bundle args = new Bundle();
//      args.putInt("controllerId", ctlr.mControllerId);

//        ctlr.mTabManager.addTab(ctlr.mTabHost.newTabSpec(tagSpec)
//												.setIndicator(buildIndicator(ctlr, R.string.starred_vendors))
//												.setContent(R.id.fragment_outlets),
//								OutletsXFragment.class, 
//								null /*args*/);  	
	}

	/**
	 * Build a {@link View} to be used as a tab indicator, setting the requested string resource as
	 * its label.
	 */
	private View buildIndicator(Ctlr ctlr, int textRes) {
		final TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator,
				ctlr.mTabWidget, false);
		indicator.setText(textRes);
		return indicator;
	}

	/**
	 * Handle {@link SessionsQuery} {@link Cursor}.
	 */
	private void updateControllerTabs(Ctlr cntl, Cursor cursor) {
		try {
			// Header Area
			cntl.mTitleString = cursor.getString(ControllersQuery.TITLE);
			cntl.mSubtitle = cursor.getString(ControllersQuery.WAN_URL);
			cntl.mTimestamp = cursor.getLong(ControllersQuery.LAST_UPDATED);

			Date timestampD = new Date(cntl.mTimestamp);
			SimpleDateFormat formatter = new SimpleDateFormat("M/d/yy h:mm a");
			String timestampS = formatter.format(timestampD);

			cntl.mTitleView.setText(timestampS);
			cntl.mSubtitleView.setText(cntl.mSubtitle);
			try {
				cntl.mControllerId = Integer.valueOf(cursor.getString(ControllersQuery._ID));
			} catch (NumberFormatException e) {
				cntl.mControllerId = -1;
			}

			if(controllerUpdateFlag){
				Uri newProbeUri = Data.buildQueryPDataAtUri(cntl.mControllerId, cntl.mTimestamp);
				Uri newOutletUri = Data.buildQueryODataAtUri(cntl.mControllerId, cntl.mTimestamp);
				cntl.mProbesFragment.reloadSelf(newProbeUri);
				cntl.mOutletsFragment.reloadSelf(newOutletUri);
			}

			//            AnalyticsUtils.getInstance(this).trackPageView("/Sessions/" + cntl.mTitleString);

			//            updateOutletsTab(cursor);
			//            updateNotesTab();

			updateWorkspaceHeader(cntl.index);

		} finally {
//            cursor.close();
		}
	}



	@Override
	public void onResume() {
		super.onResume();

		// Since we build our views manually instead of using an adapter, we
		// need to manually requery every time launched.
		requery();

		// Re-register any listeners that we want
		Iterator<Ctlr> iterator = mCtlrs.iterator();
		while (iterator.hasNext()) {
			Ctlr ctlr = iterator.next();
			if(ctlr.mControllerUri!=null)
				getContentResolver().registerContentObserver(ctlr.mControllerUri, false, mControllerChangesObserver);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		// De-register any listeners that we want
		getContentResolver().unregisterContentObserver(mControllerChangesObserver);
	}

	public void onScrollChanged(ObservableScrollView view) {
		// Keep each day view at the same vertical scroll offset.
		final int scrollY = view.getScrollY();
		for (Ctlr ctlr : mCtlrs) {
			if (ctlr.scrollView != view) {
				ctlr.scrollView.scrollTo(0, scrollY);
			}
		}
	}

	private ContentObserver mControllerChangesObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			controllerUpdateFlag = true;
			requery();
		} 
	};


	private void requery() {
		mHandler.startQuery(ControllersQuery._TOKEN, 0, Controllers.buildQueryControllersUri(), ControllersQuery.PROJECTION,
				null, null, AquaNotesDbContract.Controllers.DEFAULT_SORT);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {

		if (token == ControllersQuery._TOKEN) {
			onControllersQueryComplete(cursor);
		} else {
			cursor.close();
		}
	}

	public void onControllersQueryComplete(Cursor cursor) {

		// BlocksView is like our TabHost, there is one per day or one per controller

		// The sample picked up the current day from a cookie but we'll need
		// to find the controller from the database record

		try {
			/** For each controller in the database, */
			while (cursor.moveToNext()) {
				final Integer controllerId = cursor.getInt(ControllersQuery._ID);
				Ctlr thisCtlr;

				// Look for this controller already in the list
				int index = -1;
				Iterator<Ctlr> iterator = mCtlrs.iterator();
				while (iterator.hasNext()) {
					Ctlr ctlr = iterator.next();
					if(ctlr.mControllerId==controllerId) 
						index=ctlr.index;
				}
				// Depending on whether or not this controller has already been
				// registered with the tab-host we'll act differently

				if(mCtlrs.get(0).mControllerId==-1) {
					// If only the stub one is there, reuse it.
					DeleteCtlr(mCtlrs.get(0));
					thisCtlr = new Ctlr();
					setupCtlr(thisCtlr, cursor);
				}
				else 

					if (index == -1) {
						// else if it is not found, create a new one
						thisCtlr = new Ctlr();
						setupCtlr(thisCtlr, cursor);
					}
					else {
						// otherwise it must be in there somewhere
						thisCtlr = mCtlrs.get(index);
					}
				updateControllerTabs(thisCtlr, cursor);

			} // end of while()
		} finally {
			cursor.close();
		}


		//Can't seem to force the tab to the proper heading so just go to last or first
		updateWorkspaceHeader(0 /*mCtlrs.size()-1*/);
		//    	mWorkspace.setCurrentScreenNow(mCtlrs.size()-1);
	}

	//    @Override
	//    public FragmentReplaceInfo onSubstituteFragmentForActivityLaunch(String activityClassName) {
	//        if (findViewById(R.id.fragment_container_starred_detail) != null) {
	//            // The layout we currently have has a detail container, we can add fragments there.
	//            findViewById(android.R.id.empty).setVisibility(View.GONE);
	//            if (SessionDetailActivity.class.getName().equals(activityClassName)) {
	//                clearSelectedItems();
	//                return new FragmentReplaceInfo(
	//                        SessionDetailFragment.class,
	//                        "session_detail",
	//                        R.id.fragment_container_starred_detail);
	//            } else if (ProbesDetailActivity.class.getName().equals(activityClassName)) {
	//                clearSelectedItems();
	//                return new FragmentReplaceInfo(
	//                        ProbesDetailFragment.class,
	//                        "vendor_detail",
	//                        R.id.fragment_container_starred_detail);
	//            }
	//        }
	//        return null;
	//    }

	private void clearSelectedItems() {
		//        if (mOutletsFragment != null) {
		//        	mOutletsFragment.clearCheckedPosition();
		//        }
		//        if (mProbesFragment != null) {
		//        	mProbesFragment.clearCheckedPosition();
		//        }
	}

	/**
	 * This is a helper class that implements a generic mechanism for
	 * associating fragments with the tabs in a tab host.  It relies on a
	 * trick.  Normally a tab host has a simple API for supplying a View or
	 * Intent that each tab will show.  This is not sufficient for switching
	 * between fragments.  So instead we make the content part of the tab host
	 * 0dp high (it is not shown) and the TabManager supplies its own dummy
	 * view to show as the tab content.  It listens to changes in tabs, and takes
	 * care of switch to the correct fragment shown in a separate content area
	 * whenever the selected tab changes.
	 */
	public static class TabManager implements TabHost.OnTabChangeListener {
		private final FragmentActivity mActivity;
		private final TabHost mTabHost;
		private final int mContainerId;
		private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
		TabInfo mLastTab;

		static final class TabInfo {
			private final String tag;
			private final Class<?> clss;
			private final Bundle args;
			private Fragment fragment;

			TabInfo(String _tag, Class<?> _class, Bundle _args) {
				tag = _tag;
				clss = _class;
				args = _args;
			}
		}

		static class DummyTabFactory implements TabHost.TabContentFactory {
			private final Context mContext;

			public DummyTabFactory(Context context) {
				mContext = context;
			}

			@Override
			public View createTabContent(String tag) {
				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}

		public TabManager(FragmentActivity activity, TabHost tabHost, int containerId) {
			mActivity = activity;
			mTabHost = tabHost;
			mContainerId = containerId;
			mTabHost.setOnTabChangedListener(this);
		}

		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
			tabSpec.setContent(new DummyTabFactory(mActivity));
			String tag = tabSpec.getTag();

			TabInfo info = new TabInfo(tag, clss, args);

			// Check to see if we already have a fragment for this tab, probably
			// from a previously saved state.  If so, deactivate it, because our
			// initial state is that a tab isn't shown.
			info.fragment = mActivity.getSupportFragmentManager().findFragmentByTag(tag);
					if (info.fragment != null && !info.fragment.isDetached()) {
						FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
						ft.detach(info.fragment);
						ft.commit();
					}

					mTabs.put(tag, info);
					mTabHost.addTab(tabSpec);
		}

		@Override
		public void onTabChanged(String tabId) {
			TabInfo newTab = mTabs.get(tabId);
			if (mLastTab != newTab) {
				FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
				if (mLastTab != null) {
					if (mLastTab.fragment != null) {
						ft.detach(mLastTab.fragment);
					}
				}
				if (newTab != null) {
					if (newTab.fragment == null) {
						newTab.fragment = Fragment.instantiate(mActivity,
								newTab.clss.getName(), newTab.args);
						ft.add(mContainerId, newTab.fragment, newTab.tag);
					} else {
						ft.attach(newTab.fragment);
					}
				}

				mLastTab = newTab;
				ft.commit();
				mActivity.getSupportFragmentManager().executePendingTransactions();
			}
		}
	}

	private interface ControllersQuery {

		int _TOKEN = 0x1;

		String[] PROJECTION = {
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
				BaseColumns._ID,
				AquaNotesDbContract.Controllers.TITLE,
				AquaNotesDbContract.Controllers.WAN_URL,
				AquaNotesDbContract.Controllers.LAN_URL,
				AquaNotesDbContract.Controllers.WIFI_SSID,
				AquaNotesDbContract.Controllers.USER,
				AquaNotesDbContract.Controllers.PW,
				AquaNotesDbContract.Controllers.LAST_UPDATED,
				AquaNotesDbContract.Controllers.UPDATE_INTERVAL,
				AquaNotesDbContract.Controllers.DB_SAVE_DAYS,
				AquaNotesDbContract.Controllers.MODEL,
		};
		int _ID = 0;
		int TITLE = 1;
		int WAN_URL = 2;
		int LAN_URL = 3;
		int WIFI_SSID = 4;
		int USER = 5;
		int PW = 6;
		int LAST_UPDATED = 7;
		int UPDATE_INTERVAL = 8;
		int DB_SAVE_DAYS = 9;
		int MODEL = 10;
	}

	@Override
	public void onClick(View v) {
		if (v instanceof BlockView) {
			//          String title = ((BlockView)view).getText().toString();
			//          AnalyticsUtils.getInstance(getActivity()).trackEvent(
			//                  "Schedule", "Session Click", title, 0);
			//          final String blockId = ((BlockView) view).getBlockId();
			//          final Uri sessionsUri = AquaNotesDbContract.Blocks.buildSessionsUri(blockId);
			//
			//          final Intent intent = new Intent(Intent.ACTION_VIEW, sessionsUri);
			//          intent.putExtra(DbMaintProbesFragment.EXTRA_SCHEDULE_TIME_STRING,
			//                  ((BlockView) view).getBlockTimeString());
			//          ((BaseActivity) getActivity()).openActivityOrFragment(intent);
		}
	}

	void outletUpdate(int cid, String name, int position) {
		Cursor cursor = null;
		try {
			Uri controllersQueryUri = Controllers.buildQueryControllerXUri(cid);
			cursor = dbResolverControllerAct.query(controllersQueryUri, ControllersQuery.PROJECTION, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String wanUri = cursor.getString(ControllersQuery.WAN_URL); 
				String lanUri = cursor.getString(ControllersQuery.LAN_URL); 
				String user = cursor.getString(ControllersQuery.USER); 
				String pw = cursor.getString(ControllersQuery.PW); 
				String ssid = cursor.getString(ControllersQuery.WIFI_SSID); 
				new OutletUpdateThread(this, wanUri, lanUri, user, pw, ssid, name, position).execute();
			}
		} catch (SQLException e) {
			//Log.e(TAG, "getting controller list", e);	
			// need a little more here!
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

	}

	private class OutletUpdateThread extends AsyncTask<String, Integer, Boolean> {
		String wanUri; 
		String lanUri; 
		String user; 
		String pw; 
		String ssid; 
		String name;
		int position;
		Context context;

		OutletUpdateThread(Context cx, String wa, String la, String us, String pwd, String ss, String nm, int ps) {
			context = cx;
			wanUri = wa; 
			lanUri = la; 
			user = us; 
			pw = pwd; 
			ssid = ss; 
			name = nm;
			position = ps;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				mRemoteExecutor.updateOutlet(wanUri,lanUri,ssid,user ,pw, name,position);
			} catch (HandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Boolean resultFailedFlag) {
			triggerRefresh();
		}	
	}
	/**
	 * There is an embedded http client helper below
	 */
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String ENCODING_GZIP = "gzip";
	private static final int SECOND_IN_MILLIS = (int) DateUtils.SECOND_IN_MILLIS;

	/**
	 * Generate and return a {@link HttpClient} configured for general use,
	 * including setting an application-specific user-agent string.
	 */
	public static HttpClient getHttpClient(Context context) {
		final HttpParams params = new BasicHttpParams();

		// Use generous timeouts for slow mobile networks
		HttpConnectionParams.setConnectionTimeout(params, 20 * SECOND_IN_MILLIS);
		HttpConnectionParams.setSoTimeout(params, 20 * SECOND_IN_MILLIS);

		HttpConnectionParams.setSocketBufferSize(params, 8192);
		HttpProtocolParams.setUserAgent(params, buildUserAgent(context));

		final DefaultHttpClient client = new DefaultHttpClient(params);

		client.addRequestInterceptor(new HttpRequestInterceptor() {
			public void process(HttpRequest request, HttpContext context) {
				// Add header to accept gzip content
				if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
					request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
				}
			}
		});

		client.addResponseInterceptor(new HttpResponseInterceptor() {
			public void process(HttpResponse response, HttpContext context) {
				// Inflate any responses compressed with gzip
				final HttpEntity entity = response.getEntity();
				final Header encoding = entity.getContentEncoding();
				if (encoding != null) {
					for (HeaderElement element : encoding.getElements()) {
						if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
							response.setEntity(new InflatingEntity(response.getEntity()));
							break;
						}
					}
				}
			}
		});
		return client;
	}

	/**
	 * Build and return a user-agent string that can identify this application
	 * to remote servers. Contains the package name and version code.
	 */
	private static String buildUserAgent(Context context) {
		try {
			final PackageManager manager = context.getPackageManager();
			final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);

			// Some APIs require "(gzip)" in the user-agent string.
			return info.packageName + "/" + info.versionName
					+ " (" + info.versionCode + ") (gzip)";
		} catch (NameNotFoundException e) {
			return null;
		}
	}

	/**
	 * Simple {@link HttpEntityWrapper} that inflates the wrapped
	 * {@link HttpEntity} by passing it through {@link GZIPInputStream}.
	 */
	private static class InflatingEntity extends HttpEntityWrapper {
		public InflatingEntity(HttpEntity wrapped) {
			super(wrapped);
		}

		@Override
		public InputStream getContent() throws IOException {
			return new GZIPInputStream(wrappedEntity.getContent());
		}

		@Override
		public long getContentLength() {
			return -1;
		}
	}

	
    /**
     * A non-UI fragment, retained across configuration changes, that updates its activity's UI
     * when sync status changes.
     */
    public static class SyncStatusUpdaterFragment extends Fragment
            implements DetachableResultReceiver.Receiver {
        public static final String TAG = SyncStatusUpdaterFragment.class.getName()+"_2";

        private boolean mSyncing = false;
        private DetachableResultReceiver mReceiver;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            mReceiver = new DetachableResultReceiver(new Handler());
            mReceiver.setReceiver(this);
        }

        /** {@inheritDoc} */
        public void onReceiveResult(int resultCode, Bundle resultData) {
            ControllersActivity activity = (ControllersActivity) getActivity();
            if (activity == null) {
                return;
            }

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
                    Toast.makeText(activity, errorText, Toast.LENGTH_LONG).show();
                    break;
                }
            }
            activity.updateRefreshStatus(mSyncing);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            ((ControllersActivity) getActivity()).updateRefreshStatus(mSyncing);
        }
    }

}
