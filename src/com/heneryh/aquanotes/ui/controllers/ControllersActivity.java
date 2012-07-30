/*
 * Copyright 2012 
 *
 * Licensed under the xxxx
 */

package com.heneryh.aquanotes.ui.controllers;

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
import com.heneryh.aquanotes.ui.BaseMultiPaneActivity;
import com.heneryh.aquanotes.ui.BaseSinglePaneActivity;
import com.heneryh.aquanotes.ui.widget.ObservableScrollView;
import com.heneryh.aquanotes.ui.widget.Workspace;
import com.heneryh.aquanotes.util.AnalyticsUtils;
import com.heneryh.aquanotes.util.MotionEventUtils;
import com.heneryh.aquanotes.util.NotifyingAsyncQueryHandler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.text.method.LinkMovementMethod;
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
 * screen has tabs for probes, outlets, notes and graphs. This activity can be
 * either single or multi-pane, depending on the device configuration. We want the multi-pane
 * support that {@link BaseMultiPaneActivity} offers, so we inherit from it instead of
 * {@link BaseSinglePaneActivity}.  Hmm, which features are those???
 */
public class ControllersActivity extends BaseMultiPaneActivity implements
								NotifyingAsyncQueryHandler.AsyncQueryListener,
								View.OnClickListener  {

	Context controllerActContext;
	
	/**
	 * Receiver for status update broadcasts from the Sync Service
	 */
    MyIntentReceiver statusIntentReceiver;

    /**
	 * Base tags for the tabs in each controller.  The actual tag will be the base+id.
	 */
	public static final String TAG_PROBES = "probes";
	public static final String TAG_OUTLETS = "outlets";
	public static final String TAG_NOTES = "notes";
	public static final String TAG_GRAPHS = "graphs";

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
	 * Keep the currently displayed controller because when the fragments want us to do something
	 * we'll want to know which controller is being displayed.
	 */
	private int currentCtlrIndex;
	
	/**
	 * A helper class containing references and data related to a particular controller tab-view.
	 */
	private List<Ctlr> mCtlrs = new ArrayList<Ctlr>();

	private class Ctlr {
		private int index;

		/** 
		 * Views for each swipe pane for each controller
		 */
		private ViewGroup mRootView; // Host for the tab view within the fragment.  Below the L/R and Workspace Title
		private FrameLayout mTabHolderFrame;

		private TabHost mTabHost;
		private TabWidget mTabWidget;
//        private TabManager mTabManager; // alternate way of managing tabs

		private String mTitleString;
		private TextView mTitleView;
		private TextView mSubtitleView;
		private CompoundButton mStarredView;
		private String mSubtitle;

		/**
		 * The current tab host method reqiures us to track the fragment container at this level
		 */
		FrameLayout probesFragmentContainer;
		FrameLayout outletsFragmentContainer;
		FrameLayout notesFragmentContainer;
		FrameLayout graphsFragmentContainer;
	
		private ProbesFragment mProbesFragment;
		private OutletsDataFragment mOutletsFragment;
		private NotesFragment mNotesFragment;
		private GraphsFragment mGraphsFragment;


		/**
		 * Don't think these are needed anymore...
		 */
		String graphsTagId;
		int graphsRid;

		private Integer mControllerId;
		private Uri mControllerUri;
		private Long mTimestamp;
		
		/**
		 * dialog for the graphs needs the full probe list.
		 */
		CharSequence[] probeList; 
	}

	/*****************************  Activity Methods  ********************************
	 * 
	 *
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		controllerActContext = this;

        AnalyticsUtils.getInstance(this).trackPageView("/Controllers");

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

        getActivityHelper().setupActionBar("empty", 0);

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

		 /** 
		  * For the status update messages from the syncService thread
		  */
	     statusIntentReceiver = new MyIntentReceiver();
	}

	/**
	 * 
	 * @param savedInstanceState
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		/**
		 * If we are on HC or ICS the actionBar is setup differently for sub-activities
		 */
		getActivityHelper().setupSubActivity();
	}

	@Override
	public void onResume() {
		super.onResume();

	    IntentFilter intentFilter = new IntentFilter(SyncService.STATUS_UPDATE);
	    registerReceiver(statusIntentReceiver, intentFilter); 
		updateRefreshStatus(false);

	    /**
	     * Since we build our views manually instead of using an adapter, we
	     * need to manually requery every time launched.
	     */
		requery();
	}

	@Override
	public void onPause() {
		super.onPause();
		
		// De-register any listeners that we want
		unregisterReceiver(statusIntentReceiver);

	}


	/**
	 * 
	 * 
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

	/**
	 * Be clear here whether we are talking about a database refresh or actually going out to the
	 * controller for fresh data.  In this case we want to really go get new data from the controller.
	 */
	private void triggerRefresh() {
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, SyncService.class);
		startService(intent);
	}

	/**
	 * Set the spinner status according to the results received in the status intent.
	 * @param refreshing
	 */
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
        getActivityHelper().setActionBarTitle(ctlr.mTitleString);
        currentCtlrIndex = ctlrIndex;

		mLeftIndicator
		.setVisibility((ctlrIndex != 0) ? View.VISIBLE : View.INVISIBLE);
		mRightIndicator
		.setVisibility((ctlrIndex < mCtlrs.size() - 1) ? View.VISIBLE : View.INVISIBLE);
	}

	/**
	 * Prepare the TabHost for this controller and inflate it within a workspace pane.
	 * This will inflate the below as defined in R.layout.controllers_list_content_tabbed:
	 *  Header:
	 *      Title & Subtitle
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

		ctlr.mTitleView = (TextView) ctlr.mRootView.findViewById(R.id.controller_title);
		ctlr.mSubtitleView = (TextView) ctlr.mRootView.findViewById(R.id.controller_subtitle);
		ctlr.mStarredView = (CompoundButton) ctlr.mRootView.findViewById(R.id.star_button);
		ctlr.mStarredView.setFocusable(true);
		ctlr.mStarredView.setClickable(true);

		ctlr.mTabHolderFrame = (FrameLayout) ctlr.mRootView.findViewById(R.id.controllers_tab_holder_frame);

		ctlr.mTabHost = (TabHost) ctlr.mRootView.findViewById(android.R.id.tabhost);
		ctlr.mTabWidget = (TabWidget) ctlr.mRootView.findViewById(android.R.id.tabs);
		ctlr.mTabHost.setup();
//        ctlr.mTabManager = new TabManager(this, ctlr.mTabHost/*, R.id.realtabcontent this should be the container id*/);
		
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

		/**
		 * Skip over the dummy tabhost setup upon create
		 */
		if(ctlr.mControllerId>=0) {
			setupProbesTab(ctlr);
			setupOutletsTab(ctlr);
        	setupNotesTab(ctlr);
			setupGraphsTab(ctlr, null);
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
		
		/**
		 * The tag we'll use to refer to this particular tab is the preface and the controllerId
		 */
		String tagSpec = TAG_PROBES + "_" + ctlr.mControllerId.toString().replace('-', 'n');

		
		/**
		 * We need to keep IDs for each tab withing each controller.  I can't think of any way around this.
		 * I should add error checking in case someone tries to add 11.
		 */
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

		/**
		 * This is a unique layout for each probe tab within all of the probes tabs.  Make a new blank
		 * frame, set its ID and attach it to the tabcontent root.
		 */
		ctlr.probesFragmentContainer = new FrameLayout(this);
		ctlr.probesFragmentContainer.setId(Rid);
		ctlr.probesFragmentContainer.setLayoutParams(
										new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
																	ViewGroup.LayoutParams.FILL_PARENT));
		ViewGroup tabContentView = (ViewGroup) ctlr.mRootView.findViewById(android.R.id.tabcontent);
		tabContentView.addView(ctlr.probesFragmentContainer);

		/**
		 * For the probe fragment, build a Uri pointing to this controller's probe list.
		 */
		final Intent intent = new Intent(Intent.ACTION_VIEW, Data.buildQueryPDataAtUri(ctlr.mControllerId, ctlr.mTimestamp));

		/**
		 * Kickoff the fragment
		 */
		final FragmentManager fm = getSupportFragmentManager();

		ctlr.mProbesFragment = (ProbesFragment) fm.findFragmentByTag(tagSpec);
		if (ctlr.mProbesFragment != null && !ctlr.mProbesFragment.isDetached()) {
			fm.beginTransaction().detach(ctlr.mProbesFragment)
									.commit();
		} 
		if(ctlr.mProbesFragment == null){
			ctlr.mProbesFragment = new ProbesFragment();
			ctlr.mProbesFragment.setArguments(intentToFragmentArguments(intent));
			fm.beginTransaction().add(Rid, ctlr.mProbesFragment, tagSpec)
									.commit();
		}

		/** 
		 * Add the tab to the tabhost
		 */
		ctlr.mTabHost.addTab(ctlr.mTabHost.newTabSpec(tagSpec)
											.setIndicator(buildIndicator(ctlr, R.string.probes_tab_title))
											.setContent(Rid));

//		/** we need to override the container ID now */
//            ctlr.mTabManager.addTab(ctlr.mTabHost.newTabSpec(tagSpec)
//            										.setIndicator("Probes"/*buildIndicator(ctlr, R.string.probes_tab_title)*/)
//            										.setContent(Rid),  /* is this duplicative with rid below?*/
//            						ProbesFragment.class, 
//            						Rid,
//            						intentToFragmentArguments(intent));  	
	}

	/**
	 * Build and add "sessions" tab.
	 */
	private void setupOutletsTab(Ctlr ctlr) {

		/**
		 * nearly all of the comments here are the same as for the probes.
		 */
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

		ctlr.outletsFragmentContainer = new FrameLayout(this);
		ctlr.outletsFragmentContainer.setId(Rid);
		ctlr.outletsFragmentContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
														ViewGroup.LayoutParams.FILL_PARENT));

		ViewGroup tabContentView = (ViewGroup) ctlr.mRootView.findViewById(android.R.id.tabcontent);
		tabContentView.addView(ctlr.outletsFragmentContainer);

		final Intent intent = new Intent(Intent.ACTION_VIEW, Data.buildQueryODataAtUri(ctlr.mControllerId, ctlr.mTimestamp));

		final FragmentManager fm = getSupportFragmentManager();
		ctlr.mOutletsFragment = (OutletsDataFragment) fm.findFragmentByTag(tagSpec); // was OutletXFragment
		if (ctlr.mOutletsFragment == null) {
			ctlr.mOutletsFragment = new OutletsDataFragment(); // was OutletXFragment
			ctlr.mOutletsFragment.setArguments(intentToFragmentArguments(intent));
			fm.beginTransaction().add(Rid, ctlr.mOutletsFragment, tagSpec)
									.commit();
		}

		ctlr.mTabHost.addTab(ctlr.mTabHost.newTabSpec(tagSpec)
													.setIndicator(buildIndicator(ctlr, R.string.outlets_tab_title))
													.setContent(Rid));

//        ctlr.mTabManager.addTab(ctlr.mTabHost.newTabSpec(tagSpec)
//											.setIndicator("Outlets")//buildIndicator(ctlr, R.string.outlets_tab_title))
//											.setContent(Rid),
//								OutletsDataFragment.class, 
//								Rid,
//								intentToFragmentArguments(intent));  	

	}

	/**
	 * Build and add "graphs" tab.
	 */
	private void setupGraphsTab(Ctlr ctlr, String probeName) {
		
		/**
		 * nearly all of the comments here are the same as for the probes.
		 */
		String tagSpec = TAG_GRAPHS + "_" + ctlr.mControllerId.toString().replace('-', 'n');
		int Rid;
		switch(ctlr.index){
		case 0:
			Rid = R.id.fragment_graphs_0;
			break;
		case 1:
			Rid = R.id.fragment_graphs_1;
			break;
		case 2:
			Rid = R.id.fragment_graphs_2;
			break;
		case 3:
			Rid = R.id.fragment_graphs_3;
			break;
		case 4:
			Rid = R.id.fragment_graphs_4;
			break;
		case 5:
			Rid = R.id.fragment_graphs_5;
			break;
		case 6:
			Rid = R.id.fragment_graphs_6;
			break;
		case 7:
			Rid = R.id.fragment_graphs_7;
			break;
		case 8:
			Rid = R.id.fragment_graphs_8;
			break;
		case 9:
			Rid = R.id.fragment_graphs_9;
			break;
		default:
			Rid=0;
		}

		/** 
		 * Don't think these are used anymore
		 */
		ctlr.graphsTagId = tagSpec;
		ctlr.graphsRid = Rid;


		/**
		 * Build the intent that will be sent to the fragment
		 */
		final Intent intent = new Intent(Intent.ACTION_VIEW, Controllers.buildQueryControllerXUri(ctlr.mControllerId));
		if(probeName==null)
			intent.putExtra("ProbeName", "none"); 
		else
			intent.putExtra("ProbeName", probeName);   // this was an attempt at updating the fragment...

		ctlr.graphsFragmentContainer = new FrameLayout(this);
		ctlr.graphsFragmentContainer.setId(Rid);
		ViewGroup tabContentView = (ViewGroup) ctlr.mRootView.findViewById(android.R.id.tabcontent);
		tabContentView.addView(ctlr.graphsFragmentContainer);

		final FragmentManager fm = getSupportFragmentManager();

		ctlr.mGraphsFragment = (GraphsFragment) fm.findFragmentByTag(tagSpec);
		if (ctlr.mGraphsFragment == null) {
			ctlr.mGraphsFragment = new GraphsFragment();
			ctlr.mGraphsFragment.setArguments(intentToFragmentArguments(intent));
			fm.beginTransaction().add(Rid, ctlr.mGraphsFragment, tagSpec)
									.commit();
		}

		ctlr.mTabHost.addTab(ctlr.mTabHost.newTabSpec(tagSpec)
				.setIndicator(buildIndicator(ctlr, R.string.graphs_tab_title))
				.setContent(Rid));

//        ctlr.mTabManager.addTab(ctlr.mTabHost.newTabSpec(tagSpec)
//												.setIndicator("Graphs")//buildIndicator(ctlr, R.string.graphs_tab_title))
//												.setContent(Rid),
//								GraphsFragment.class, 
//								Rid,
//								intentToFragmentArguments(intent));  	
}

    /**
     * Build and add "notes" tab.
     */
    private void setupNotesTab(Ctlr ctlr) {
    	
		/**
		 * nearly all of the comments here are the same as for the probes.
		 */
		String tagSpec = TAG_NOTES + "_" + ctlr.mControllerId.toString().replace('-', 'n');
		int Rid;
		switch(ctlr.index){
		case 0:
			Rid = R.id.fragment_notes_0;
			break;
		case 1:
			Rid = R.id.fragment_notes_1;
			break;
		case 2:
			Rid = R.id.fragment_notes_2;
			break;
		case 3:
			Rid = R.id.fragment_notes_3;
			break;
		case 4:
			Rid = R.id.fragment_notes_4;
			break;
		case 5:
			Rid = R.id.fragment_notes_5;
			break;
		case 6:
			Rid = R.id.fragment_notes_6;
			break;
		case 7:
			Rid = R.id.fragment_notes_7;
			break;
		case 8:
			Rid = R.id.fragment_notes_8;
			break;
		case 9:
			Rid = R.id.fragment_notes_9;
			break;
		default:
			Rid=0;
		}

		/**
		 * Build the intent that will be sent to the fragment
		 */
		final Intent intent = new Intent(Intent.ACTION_VIEW, Controllers.buildQueryControllerXUri(ctlr.mControllerId));
		intent.putExtra("ControllerName", ctlr.mTitleString); 

		ctlr.notesFragmentContainer = new FrameLayout(this);
		ctlr.notesFragmentContainer.setId(Rid);
		ViewGroup tabContentView = (ViewGroup) ctlr.mRootView.findViewById(android.R.id.tabcontent);
		tabContentView.addView(ctlr.notesFragmentContainer);

		final FragmentManager fm = getSupportFragmentManager();

		ctlr.mNotesFragment = (NotesFragment) fm.findFragmentByTag(tagSpec);
		if (ctlr.mNotesFragment == null) {
			ctlr.mNotesFragment = new NotesFragment();
			ctlr.mNotesFragment.setArguments(intentToFragmentArguments(intent));
			fm.beginTransaction().add(Rid, ctlr.mNotesFragment, tagSpec)
									.commit();
		}

		ctlr.mTabHost.addTab(ctlr.mTabHost.newTabSpec(tagSpec)
				.setIndicator(buildIndicator(ctlr, R.string.notes_tab_title))
				.setContent(Rid));

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
	 * Handle updates to a controller tabhost based on a query result stored in a cursor.
	 */
	private void updateAllControllerTabs(Ctlr cntl, Cursor cursor) {
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
				controllerUpdateFlag=false;
			}

			//            AnalyticsUtils.getInstance(this).trackPageView("/Sessions/" + cntl.mTitleString);

			updateWorkspaceHeader(cntl.index);

		} finally {
//			cursor.close();  closed a level above
		}
	}
	
	/**
	 * Take notice if a controller has changed which is usually a timestamp change.  We use this timestamp
	 * as a proxy for all controller data must have changed.  I don't want a bunch of hits based on every
	 * probe or outlet changing.
	 */
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
				else if (index == -1) {
					// else if it is not found, create a new one
					thisCtlr = new Ctlr();
					setupCtlr(thisCtlr, cursor);
				}
				else {
					// otherwise it must be in there somewhere
					thisCtlr = mCtlrs.get(index);
				}
				updateAllControllerTabs(thisCtlr, cursor);
			} // end of while()
		} finally {
			cursor.close();
		}
		
		//Can't seem to force the tab to the proper heading so just go to last or first
		updateWorkspaceHeader(0 /*mCtlrs.size()-1*/);
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
		private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
		TabInfo mLastTab;

		static final class TabInfo {
			private final String tag;
			private final Class<?> clss;
			private final Bundle args;
			private final int mContainerId;
			private Fragment fragment;

			TabInfo(String _tag, Class<?> _class,int containerId, Bundle _args) {
				tag = _tag;
				clss = _class;
				args = _args;
				mContainerId=containerId;
			}
		}

		static class DummyTabFactory implements TabHost.TabContentFactory {
			private final Context mContext;

			public DummyTabFactory(Context context) {
				mContext = context;
			}

			//@Override
			public View createTabContent(String tag) {
				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}

		public TabManager(FragmentActivity activity, TabHost tabHost) {
			mActivity = activity;
			mTabHost = tabHost;
			mTabHost.setOnTabChangedListener(this);
		}

		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, int containerId, Bundle args) {
			tabSpec.setContent(new DummyTabFactory(mActivity));
			String tag = tabSpec.getTag();

			TabInfo info = new TabInfo(tag, clss, containerId, args);

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

		//@Override
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
						ft.add(newTab.mContainerId, newTab.fragment, newTab.tag);
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

	//@Override
	public void onClick(View v) {
		//		if (v instanceof BlockView) {
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
		//		}
	}

	/**
	 * This is an activity method that will get called from the outlet tab fragment.  I had to push the functionality
	 * up here to the activity level for it to work properly.
	 * @param cid
	 * @param name
	 * @param position
	 */
	void outletUpdate(int cid, String name, int position) {
		Cursor cursor = null;
		try {
			Uri controllersQueryUri = Controllers.buildQueryControllerXUri(cid);
			cursor = dbResolverControllerAct.query(controllersQueryUri, ControllersQuery.PROJECTION, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				new OutletUpdateThread(cursor, name, position).execute();
			}
		} catch (SQLException e) {
			//Log.e(TAG, "getting controller list", e);	
			// need a little more here!
		} finally {
			if (cursor != null) {
//				cursor.close();  close it in the thread
			}
		}

	}

	private class OutletUpdateThread extends AsyncTask<String, Integer, Boolean> {
		String outletName;
		int position;
		Cursor cursor;

		OutletUpdateThread(Cursor cur, String name, int ps) {
			cursor = cur;
			outletName = name;
			position = ps;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				mRemoteExecutor.updateOutlet(cursor, outletName, position);
			} catch (HandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Boolean resultFailedFlag) {
			triggerRefresh();
			cursor.close();

		}	
	}
	
	/**
	 * This is an activity method that will get called from the graph tab fragment.  I had to push the functionality
	 * up here to the activity level for it to work properly.
	 */
	void prepProbeListDialog(int ctrlId, List<CharSequence> argProbeList) {
		
		/**
		 * Find this controller and update its probe list accordingly
		 */
		int index = -1;
		Iterator<Ctlr> iterator = mCtlrs.iterator();
		while (index==-1 && iterator.hasNext()) {
			Ctlr ctlr = iterator.next();
			if(ctlr.mControllerId==ctrlId) {
				index=ctlr.index;
				ctlr.probeList = argProbeList.toArray(new CharSequence[argProbeList.size()]);
			}
		}
	}
	
	/**
	 * When the graph fragment requests us, prompt for the probe change
	 * @param view
	 */
    public void selfDestruct(View view) {
    	showDialog(currentCtlrIndex);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
    	Ctlr ctlr = mCtlrs.get(currentCtlrIndex);
    	return new AlertDialog.Builder(this)
    	.setTitle("Which probe to graph:")
    	.setItems(ctlr.probeList, new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int which) {
    			Ctlr ctlr = mCtlrs.get(currentCtlrIndex);
    			ctlr.mGraphsFragment.kickoffDataQuery(ctlr.probeList[which].toString());
    		}
    	})
    	.create();
    }


	/**
	 * There is an embedded http client helper below.  Why do I need to keep copies of this in each activity
	 * that uses the remote executor?  I should be able to have a single copy.  No time to figure it out now...
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
				Toast.makeText(controllerActContext, errorText, Toast.LENGTH_LONG).show();
				break;
			}
			}
			updateRefreshStatus(mSyncing);
		}
    }
}
