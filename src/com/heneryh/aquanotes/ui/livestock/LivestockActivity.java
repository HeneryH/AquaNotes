/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.heneryh.aquanotes.ui.livestock;

import com.heneryh.aquanotes.R;
import com.heneryh.aquanotes.provider.AquaNotesDbContract;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Livestock;
import com.heneryh.aquanotes.ui.BaseMultiPaneActivity;
import com.heneryh.aquanotes.ui.livestock.ContentActivity;
import com.heneryh.aquanotes.util.AnalyticsUtils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

/** This is the main "launcher" activity.
 * When running on a "large" or larger screen, this activity displays both the
 * TitlesFragments and the Content Fragment. When on a smaller screen size, this
 * activity displays only the TitlesFragment. In which case, selecting a list
 * item opens the ContentActivity, holds only the ContentFragment. */
public class LivestockActivity extends BaseMultiPaneActivity /*implements TitlesFragment.OnItemSelectedListener*/ {

    private Animator mCurrentTitlesAnimator;
    private String[] mToggleLabels = {"Show Titles", "Hide Titles"};
    private static final int NOTIFICATION_DEFAULT = 1;
    private static final String ACTION_DIALOG = "com.example.android.hcgallery.action.DIALOG";
    private int mThemeId = -1;
    private boolean mDualFragments = false;
    private boolean mTitlesHidden = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AnalyticsUtils.getInstance(this).trackPageView("/Livestock");

        setContentView(R.layout.activity_livestock);

//        ActionBar bar = getActionBar();
//        bar.setDisplayShowTitleEnabled(false);

        getActivityHelper().setupActionBar("Livestock", 0);

        final FragmentManager fm = getSupportFragmentManager();

        ContentFragment frag = (ContentFragment) fm.findFragmentById(R.id.content_frag);
        if (frag != null) mDualFragments = true;
        
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

	/**
	 * 
	 * 
	 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.livestock_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
         case R.id.menu_add_ls:
        	ContentResolver mResolver = getContentResolver();
    		ContentValues values = new ContentValues();
    		Uri livestockUri = Livestock.buildInsertLivestockUri();
            values.clear();
            values.put(AquaNotesDbContract.Livestock.COMMON_NAME, "Red Balloon");
            values.put(AquaNotesDbContract.Livestock.GENUS_ID, "SPS");
            values.put(AquaNotesDbContract.Livestock.THUMBNAIL, R.drawable.red_balloon);
            values.put(AquaNotesDbContract.Livestock.TIMESTAMP, 0);
            try {
            	mResolver.insert(livestockUri, values);
            } catch (SQLException e) {
            	Log.e("LOG_TAG", "Inserting livestock", e);
            }

        	//Directory.addToCategory(0,new LivestockEntry("Blue Balloon", R.drawable.blue_balloon));
        	return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        if (ACTION_DIALOG.equals(intent.getAction())) {
            showDialog(intent.getStringExtra(Intent.EXTRA_TEXT));
        }
    }

    void showDialog(String text) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        DialogFragment newFragment = MyDialogFragment.newInstance(text);

        // Show the dialog.
        newFragment.show(ft, "dialog");
    }


    PendingIntent getDialogPendingIntent(String dialogText) {
        return PendingIntent.getActivity(
                this,
                dialogText.hashCode(), // Otherwise previous PendingIntents with the same
                                       // requestCode may be overwritten.
                new Intent(ACTION_DIALOG)
                        .putExtra(Intent.EXTRA_TEXT, dialogText)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                0);
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("theme", mThemeId);
        outState.putBoolean("titlesHidden", mTitlesHidden);
    }

    /** Implementation for TitlesFragment.OnItemSelectedListener.
     * When the TitlesFragment receives an onclick event for a list item,
     * it's passed back to this activity through this method so that we can
     * deliver it to the ContentFragment in the manner appropriate */
    public void onItemSelected(int category, int position) {
      if (!mDualFragments) {
          // If showing only the TitlesFragment, start the ContentActivity and
          // pass it the info about the selected item
          Intent intent = new Intent(this, ContentActivity.class);
          intent.putExtra("category", category);
          intent.putExtra("position", position);
          intent.putExtra("theme", mThemeId);
          startActivity(intent);
      } else {
          // If showing both fragments, directly update the ContentFragment
          ContentFragment frag = (ContentFragment) getSupportFragmentManager()
                  .findFragmentById(R.id.content_frag);
          frag.updateContentAndRecycleBitmap(category, position);
      }
    }


    /** Dialog implementation that shows a simple dialog as a fragment */
    public static class MyDialogFragment extends DialogFragment {

        public static MyDialogFragment newInstance(String title) {
            MyDialogFragment frag = new MyDialogFragment();
            Bundle args = new Bundle();
            args.putString("text", title);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String text = getArguments().getString("text");

            return new AlertDialog.Builder(getActivity())
                    .setTitle("A Dialog of Awesome")
                    .setMessage(text)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            }
                    )
                    .create();
        }
    }
}
