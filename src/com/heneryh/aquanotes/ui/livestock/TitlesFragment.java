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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Fragment that shows the list of images
 * As an extension of ListFragment, this fragment uses a default layout
 * that includes a single ListView, which you can acquire with getListView()
 * When running on a screen size smaller than "large", this fragment appears alone
 * in LivestockActivity. In this case, selecting a list item opens the ContentActivity,
 * which likewise holds only the ContentFragment.
 */
public class TitlesFragment extends Fragment 
implements /*ActionBar.TabListener,*/ LoaderManager.LoaderCallbacks<Cursor> {

	private static final int PHOTO_LIST_LOADER = 0x01;
	private ImageCursorAdapter adapter;
	private Cursor c;
    private ViewGroup mRootView;
    private GridView mGridview;
//	OnItemSelectedListener mListener;

//	/** Container Activity must implement this interface and we ensure
//	 * that it does during the onAttach() callback
//	 */
//	public interface OnItemSelectedListener {
//		public void onItemSelected(int category, int position);
//	}
//
//	@Override
//	public void onAttach(Activity activity) {
//		super.onAttach(activity);
//		// Check that the container activity has implemented the callback interface
//		try {
//			mListener = (OnItemSelectedListener) activity;
//		} catch (ClassCastException e) {
//			throw new ClassCastException(activity.toString()
//					+ " must implement OnItemSelectedListener");
//		}
//	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_livestock, null);

        mGridview = (GridView) mRootView.findViewById(R.id.gridview);
//
//        mGridview.setOnItemClickListener(new OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                Toast.makeText(getActivity(), "" + position, Toast.LENGTH_SHORT).show();
//            }
//        });

        return mRootView;
    }

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

//		// Give some text to display if there is no data.  In a real
//		// application this would come from a resource.
////		setEmptyText("No phone numbers");
//
//		// We have a menu item to show in action bar.
//		setHasOptionsMenu(true);
//
//		// Create an empty adapter we will use to display the loaded data.
        adapter = new ImageCursorAdapter(getActivity().getApplicationContext(), c);
        mGridview.setAdapter(adapter);
//
//		// Start out with a progress indicator.
//		setListShown(false);

		// Prepare the loader.  Either re-connect with an existing one,
		// or start a new one.
        getLoaderManager().initLoader(PHOTO_LIST_LOADER, null, (LoaderCallbacks<Cursor>) this);
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Place an action bar item for searching.
	}


	// Loader manager methods
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	// This is called when a new Loader needs to be created.  This
	// sample only has one Loader, so we don't care about the ID.
	// First, pick the base URI to use depending on whether we are
	// currently filtering.
	Uri baseUri;
//	if (mCurFilter != null) {
		baseUri = Livestock.CONTENT_URI;
		//            baseUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI,
		//                    Uri.encode(mCurFilter));
//	} else {
//		baseUri = Livestock.CONTENT_URI;
//	}

	// Now create and return a CursorLoader that will take care of
	// creating a Cursor for the data being displayed.
	CursorLoader cl = new CursorLoader(getActivity(), baseUri,
			LivestockQuery.PROJECTION, null, null,
			Livestock.DEFAULT_SORT);
	int delayMS = 2000;
	cl.setUpdateThrottle(delayMS);
	return cl;
}               

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// Swap the new cursor in.  (The framework will take care of closing the
		// old cursor once we return.)
	    adapter.swapCursor(cursor);
	    
////	// The list should now be shown.
////	if (isResumed()) {
////		setListShown(true);
////	} else {
////		setListShownNoAnimation(true);
////	}

	}

	public void onLoaderReset(Loader<Cursor> cursor) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed.  We need to make sure we are no
		// longer using it.
	    adapter.swapCursor(null);
	}

	private class ImageCursorAdapter extends CursorAdapter {
	    private LayoutInflater mLayoutInflater;
//	    private Context mContext;
//
	    public ImageCursorAdapter(Context context, Cursor c) {
	        super(context, c);
	        mContext = context;
	        mLayoutInflater = LayoutInflater.from(context);
	    }
	    

	    @Override
	    public void bindView(View view, Context context, Cursor cursor) {
	    	ImageView imageView = (ImageView)view.findViewById(R.id.grid_pic);
	    	TextView textView = (TextView)view.findViewById(R.id.grid_text);

//	    	imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
	    	imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	    	imageView.setPadding(8, 8, 8, 8);
	    	int imageRsc = cursor.getInt(LivestockQuery.THUMBNAIL);
	    	if(imageRsc>0) {
	    		imageView.setImageResource(imageRsc);	        	
	    	} else {
	    		imageView.setImageResource(R.drawable.blue_balloon);
	    	}

	    	String commonName = cursor.getString(LivestockQuery.COMMON_NAME);
	    	if(commonName==null || commonName.isEmpty())
	    		textView.setText("blank");
	    	else
	    		textView.setText(commonName);

	    }
	    
	    @Override
	    public View newView(Context context, Cursor cursor, ViewGroup parent) {
	        View v = mLayoutInflater.inflate(R.layout.grid_item, parent, false);
	        return v;
	    }
	}
	
	private interface LivestockQuery {
		String[] PROJECTION = {
				//		        String _ID = "_id";
				//		        String COMMON_NAME = "common_name";
				//		        String TYPE = "type";
				//		        String TIMESTAMP = "timestamp";
				BaseColumns._ID,
				AquaNotesDbContract.Livestock.COMMON_NAME,
				AquaNotesDbContract.Livestock.GENUS_ID,
				AquaNotesDbContract.Livestock.TIMESTAMP,
				AquaNotesDbContract.Livestock.THUMBNAIL,
		};

		int _ID = 0;
		int COMMON_NAME = 1;
		int TYPE = 2;
		int TIMESTAMP = 3;
		int THUMBNAIL = 4;
	}
}

