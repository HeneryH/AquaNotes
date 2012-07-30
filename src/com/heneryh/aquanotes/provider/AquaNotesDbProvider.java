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

package com.heneryh.aquanotes.provider;

import com.heneryh.aquanotes.provider.AquaNotesDbContract.Controllers;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Livestock;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.OutletDataView;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Outlets;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Data;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.ProbeDataView;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Probes;
import com.heneryh.aquanotes.provider.AquaNotesDatabase.Tables;
import com.heneryh.aquanotes.service.SyncService;
import com.heneryh.aquanotes.util.SelectionBuilder;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;



/**
 * Provider that stores {@link AquaNotesDbContract} data. Data is usually inserted
 * by {@link SyncService}, and queried by various {@link Activity} instances.
 */
public class AquaNotesDbProvider extends ContentProvider {
    private static final String TAG = "AquaNotesDbProvider";
    private static final boolean LOGV = true; //Log.isLoggable(TAG, Log.VERBOSE);

    private AquaNotesDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

	/**
	 *  URI IDs to be used in the switch () statements
	 *  notice two possible switch statements for the same twig depending on if something follows
	 */
	private static final int CONTROLLERS = 501;
	private static final int CONTROLLERS_ID = 502;
	private static final int CONTROLLERS_URL = 503;
	private static final int CONTROLLERS_WID = 504;
	private static final int CONTROLLERS_TITLE = 505;

	private static final int PROBES = 601;
	private static final int CONTROLLERS_ID_PROBES = 602;
	private static final int CONTROLLERS_ID_PROBES_ID = 603;
	private static final int CONTROLLERS_ID_PROBES_NAME = 604;
	
	private static final int OUTLETS = 701;
	private static final int CONTROLLERS_ID_OUTLETS = 702;
	private static final int CONTROLLERS_ID_OUTLETS_ID = 703;
	private static final int CONTROLLERS_ID_OUTLETS_DEVICE_ID = 704;
	private static final int CONTROLLERS_ID_OUTLETS_RSC = 705; 

	private static final int DATA = 801; 
	private static final int PDATA = 802; 
	private static final int ODATA = 803; 
	private static final int CONTROLLERS_ID_PROBEDATA = 804; 
	private static final int CONTROLLERS_ID_PROBEDATA_AT = 805; 
	private static final int CONTROLLERS_ID_PROBEDATA_FOR_ID = 806; 
	private static final int CONTROLLERS_ID_PROBEDATA_FOR_NAME = 807; 
	private static final int CONTROLLERS_ID_OUTLETDATA = 808; 
	private static final int CONTROLLERS_ID_OUTLETDATA_AT = 809; 
	private static final int CONTROLLERS_ID_OUTLETDATA_FOR_ID = 810; 
	private static final int CONTROLLERS_ID_OUTLETDATA_FOR_DEVICE_ID = 811; 
    
	private static final int LIVESTOCK = 901;

    private static final String MIME_XML = "text/xml";

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AquaNotesDbContract.CONTENT_AUTHORITY;

    	/**
    	 *  URI IDs to be used in the switch () statements
    	 *  notice two possible switch statements for the same twig depending on if something follows
    	 */
//////////////////////////////////////////////
//    	private static final int CONTROLLERS = 501;
//    	private static final int CONTROLLERS_ID = 502;
//    	private static final int CONTROLLERS_URL = 503;
//    	private static final int CONTROLLERS_WID = 504;
//    	private static final int CONTROLLERS_TITLE = 505;
//        private static final String PATH_CONTROLLERS = "controllers";
//        private static final String PATH_CONTROLLERS_URL = "url";
//        private static final String PATH_CONTROLLERS_WIDGET = "widget";
//        private static final String PATH_CONTROLLERS_TITLE = "title";
        matcher.addURI(authority, "controllers", CONTROLLERS);
		// query = return all controllers 
		// insert = add the controller defined in the values object, auto-create a controller_id
		// update = ?
		// delete = delete all controllers, probes, data, etc ie everything!
		// getType = return type for multiple items

        matcher.addURI(authority, "controllers/#", CONTROLLERS_ID);
		// query = return just one controller, by id#
		// insert = ?
		// update = Update this one controller with the values object
		// delete = delete the referenced controller, its probes and all probe data for this controller
		// getType = return type for single item

        matcher.addURI(authority, "controllers/url/*", CONTROLLERS_URL);
		// query = return just one controller, by url
		// insert = ?
		// update = Update this one controller with the values object
		// delete = delete the referenced controller, its probes and all probe data for this controller
		// getType = return type for single item

        matcher.addURI(authority, "controllers/widget/#", CONTROLLERS_WID);
		// query = return just one controller, by widget#
		// insert = ?
		// update = Update this one controller with the values object
		// delete = delete the referenced controller, its probes and all probe data for this controller
		// getType = return type for single item

        matcher.addURI(authority, "controllers/title/*", CONTROLLERS_TITLE);
		// query = return just one controller, by widget#
		// insert = ?
		// update = Update this one controller with the values object
		// delete = delete the referenced controller, its probes and all probe data for this controller
		// getType = return type for single item

//////////////////////////////////////////////
//    	private static final int PROBES = 601;
//    	private static final int CONTROLLERS_ID_PROBES = 602;
//    	private static final int CONTROLLERS_ID_PROBES_ID = 603;
//    	private static final int CONTROLLERS_ID_PROBES_NAME = 604;
//        private static final String PATH_PROBES = "probes";
//        private static final String PATH_PROBES_NAME = "probes_nm";
        matcher.addURI(authority, "probes", PROBES);
		// query = return all probes for all controllers, used just in the db maint screen
		// insert = while this might work, use the one with the controller specified 
		// update = ?
		// delete = ?
		// getType = return type for multiple items

        matcher.addURI(authority, "controllers/#/probes", CONTROLLERS_ID_PROBES);
		// query = return all probes for a given controller
		// insert = add a probe defined by the values object to the referenced controller, auto-create a probe_id 
		// update = ?
		// delete = delete all probes for a given controller
		// getType = return type for multiple items

		matcher.addURI(authority, "controllers/#/probes/#", CONTROLLERS_ID_PROBES_ID);
		// query = return just one probe, by ID
		// insert = insert data into the data table for this probe
		// update = update just this probe record
		// delete = delete just this probe
		// getType = return type for single item

		matcher.addURI(authority, "controllers/#/probes_nm/*", CONTROLLERS_ID_PROBES_NAME);
		// query = return just one probe, by name
		// insert = ?
		// update = update just this probe record
		// delete = delete just this probe record
		// getType = return type for single item

//////////////////////////////////////////////
//		private static final int OUTLETS = 701;
//		private static final int CONTROLLERS_ID_OUTLETS = 702;
//		private static final int CONTROLLERS_ID_OUTLETS_ID = 703;
//		private static final int CONTROLLERS_ID_OUTLETS_DEVICE_ID = 704;
//		private static final int CONTROLLERS_ID_OUTLETS_RSC = 705; 
//	    private static final String PATH_OUTLETS = "outlets";
//	    private static final String PATH_OUTLETS_RESOURCE_ID = "outlets_rsc";
//	    private static final String PATH_OUTLETS_DEVICE_ID = "outlets_did";
        matcher.addURI(authority, "outlets", OUTLETS);
		// query = return all outlets for all controllers, used just in the db maint screen
		// insert = while this might work, use the one with the controller specified 
		// update = ?
		// delete = ?
		// getType = return type for multiple items

        matcher.addURI(authority, "controllers/#/outlets", CONTROLLERS_ID_OUTLETS);
		// query = return all outlets for a given controller
		// insert = add a probe defined by the values object to the referenced controller, auto-create a probe_id 
		// update = ?
		// delete = delete all outlets for this controller
		// getType = ?

		matcher.addURI(authority, "controllers/#/outlets/#", CONTROLLERS_ID_OUTLETS_ID);
		// query = return just one outletfor a given controller and ID
		// insert = insert data into the data table for this outlet
		// update = update just this outlet
		// delete = delete just this outlet
		// getType = ?

		matcher.addURI(authority, "controllers/#/outlets_did/*", CONTROLLERS_ID_OUTLETS_DEVICE_ID);
		// query = return just one outletfor a given controller and device id
		// insert = add ?
		// update = ?
		// delete = delete ?
		// getType = ?

		matcher.addURI(authority, "controllers/#/outlets_rsc/#", CONTROLLERS_ID_OUTLETS_RSC);
		// query = return just one outlet record for a given controller and outlet name.
		// insert = N/A
		// update = N/A
		// delete = N/A
		// getType = ?

//////////////////////////////////////////////
//		private static final int DATA = 801; 
//		private static final int PDATA = 802; 
//		private static final int ODATA = 803; 
//		private static final int CONTROLLERS_ID_PROBEDATA = 804; 
//		private static final int CONTROLLERS_ID_PROBEDATA_AT = 805; 
//		private static final int CONTROLLERS_ID_PROBEDATA_FOR_ID = 806; 
//		private static final int CONTROLLERS_ID_PROBEDATA_FOR_NAME = 807; 
//		private static final int CONTROLLERS_ID_OUTLETDATA = 808; 
//		private static final int CONTROLLERS_ID_OUTLETDATA_AT = 809; 
//		private static final int CONTROLLERS_ID_OUTLETDATA_FOR_ID = 810; 
//		private static final int CONTROLLERS_ID_OUTLETDATA_FOR_DEVICE_ID = 811; 
//	    private static final String PATH_DATA = "data";
//	    private static final String PATH_PDATA = "pdata";
//	    private static final String PATH_ODATA = "odata";
//	    private static final String PATH_PROBE_DATA_AT = "pdata_at";
//	    private static final String PATH_PROBE_DATA_FOR_ID = "pdata_id";
//	    private static final String PATH_PROBE_DATA_FOR_NAME = "pdata_nm";
//	    private static final String PATH_OUTLET_DATA_AT = "odata_at";
//	    private static final String PATH_OUTLET_DATA_FOR_ID = "odata_id";
//	    private static final String PATH_OUTLET_DATA_FOR_DID = "odata_did";
		
		matcher.addURI(authority, "data", DATA);
		matcher.addURI(authority, "pdata", PDATA);
		matcher.addURI(authority, "odata", ODATA);
		// query = return all the data records (or probe or outlet subsets) for all controllers, only used in db maint screen.
		// insert = N/A
		// update = N/A
		// delete = 
		// getType = ?

		matcher.addURI(authority, "controllers/#/pdata/", CONTROLLERS_ID_PROBEDATA);
		// query = return all the probe data records for a given controller.
		// insert = while this could be used, we use the pattern up inb the outlet section
		// update = N/A
		// delete = N/A
		// getType = ? 

		matcher.addURI(authority, "controllers/#/odata/", CONTROLLERS_ID_OUTLETDATA);
		// query = return all the outlet data records for a given controller.
		// insert = while this could be used, we use the pattern up inb the outlet section
		// update = N/A
		// delete = N/A
		// getType = ?  

		matcher.addURI(authority, "controllers/#/pdata_at/*", CONTROLLERS_ID_PROBEDATA_AT);
		// query = return all the probe data records for a given controller at the timestamp provided.
		// insert = N/A
		// update = N/A
		// delete = delete probe data older than '*'
		// getType = ?

		matcher.addURI(authority, "controllers/#/odata_at/*", CONTROLLERS_ID_OUTLETDATA_AT);
		// query = return all the outlet data records for a given controller at the timestamp provided.
		// insert = N/A
		// update = N/A
		// delete = delete outlet data older than '*'
		// getType = ?

		matcher.addURI(authority, "controllers/#/pdata_id/#", CONTROLLERS_ID_PROBEDATA_FOR_ID);
		// query = return all the probe data records for a given controller and probe name.
		// insert = N/A
		// update = N/A
		// delete = N/A
		// getType = ?

		matcher.addURI(authority, "controllers/#/odata_id/#", CONTROLLERS_ID_OUTLETDATA_FOR_ID);
		// query = return all the outlet data records for a given controller and outlet name.
		// insert = N/A
		// update = N/A
		// delete = N/A
		// getType = ?

		matcher.addURI(authority, "controllers/#/pdata_nm/*", CONTROLLERS_ID_PROBEDATA_FOR_NAME);
		// query = return all the probe data records for a given controller and probe name.
		// insert = N/A
		// update = N/A
		// delete = N/A
		// getType = ?


		matcher.addURI(authority, "controllers/#/odata_deviceID/*", CONTROLLERS_ID_OUTLETDATA_FOR_DEVICE_ID);
		// query = return all the outlet data records for a given controller and outlet device id.
		// insert = N/A
		// update = N/A
		// delete = N/A
		// getType = ?  

		
//////////////////////////////////////////////
//		private static final int LIVESTOCK = 901;
//	    private static final String PATH_LIVESTOCK = "livestock";
		matcher.addURI(authority, "livestock", LIVESTOCK);
		// query = return all livestock 
		// insert = add the livestock defined in the values object, auto-create a livestock_id
		// update = ?
		// delete = delete all livestock
		// getType = return type for multiple items



        return matcher;
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        mOpenHelper = new AquaNotesDatabase(context);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
//////////////////////////////////////////////
//    	private static final int CONTROLLERS = 501;
//    	private static final int CONTROLLERS_ID = 502;
//    	private static final int CONTROLLERS_URL = 503;
//    	private static final int CONTROLLERS_WID = 504;
//        private static final String PATH_CONTROLLERS = "controllers";
//        private static final String PATH_CONTROLLERS_URL = "url";
//      private static final String PATH_CONTROLLERS_WIDGET = "widget";
//      private static final String PATH_CONTROLLERS_TITLE = "title";
        case CONTROLLERS:
        	return Controllers.CONTENT_TYPE;
        	
        case CONTROLLERS_ID:
        	return Controllers.CONTENT_ITEM_TYPE;

        case CONTROLLERS_URL:
        	return Controllers.CONTENT_ITEM_TYPE;

        case CONTROLLERS_WID:
        	return Controllers.CONTENT_ITEM_TYPE;

        case CONTROLLERS_TITLE:
        	return Controllers.CONTENT_ITEM_TYPE;

//////////////////////////////////////////////
//private static final int PROBES = 601;
//private static final int CONTROLLERS_ID_PROBES = 602;
//private static final int CONTROLLERS_ID_PROBES_ID = 603;
//private static final int CONTROLLERS_ID_PROBES_NAME = 604;
//private static final String PATH_PROBES = "probes";
//private static final String PATH_PROBES_NAME = "probes_nm";
        case PROBES:
        	return Probes.CONTENT_TYPE;
        	
        case CONTROLLERS_ID_PROBES:
        	return Probes.CONTENT_TYPE;
        	
        case CONTROLLERS_ID_PROBES_ID:
        	return Probes.CONTENT_ITEM_TYPE;
        	
        case CONTROLLERS_ID_PROBES_NAME:
        	return Probes.CONTENT_ITEM_TYPE;
        	
//////////////////////////////////////////////
//private static final int OUTLETS = 701;
//private static final int CONTROLLERS_ID_OUTLETS = 702;
//private static final int CONTROLLERS_ID_OUTLETS_ID = 703;
//private static final int CONTROLLERS_ID_OUTLETS_DEVICE_ID = 704;
//private static final int CONTROLLERS_ID_OUTLETS_RSC = 705; 
//private static final String PATH_OUTLETS = "outlets";
//private static final String PATH_OUTLETS_RESOURCE_ID = "outlets_rsc";
//private static final String PATH_OUTLETS_DEVICE_ID = "outlets_did";
        case OUTLETS:
        	return Outlets.CONTENT_TYPE;
        	
        case CONTROLLERS_ID_OUTLETS:
        	return Outlets.CONTENT_TYPE;
        	
        case CONTROLLERS_ID_OUTLETS_ID:
        	return Outlets.CONTENT_ITEM_TYPE;
        	
        case CONTROLLERS_ID_OUTLETS_DEVICE_ID:
        	return Outlets.CONTENT_ITEM_TYPE;
        	
        case CONTROLLERS_ID_OUTLETS_RSC:
        	return Outlets.CONTENT_ITEM_TYPE;

//////////////////////////////////////////////
//private static final int DATA = 801; 
//private static final int PDATA = 802; 
//private static final int ODATA = 803; 
//private static final int CONTROLLERS_ID_PROBEDATA = 804; 
//private static final int CONTROLLERS_ID_PROBEDATA_AT = 805; 
//private static final int CONTROLLERS_ID_PROBEDATA_FOR_ID = 806; 
//private static final int CONTROLLERS_ID_PROBEDATA_FOR_NAME = 807; 
//private static final int CONTROLLERS_ID_OUTLETDATA = 808; 
//private static final int CONTROLLERS_ID_OUTLETDATA_AT = 809; 
//private static final int CONTROLLERS_ID_OUTLETDATA_FOR_ID = 810; 
//private static final int CONTROLLERS_ID_OUTLETDATA_FOR_DEVICE_ID = 811; 
//private static final String PATH_DATA = "data";
//private static final String PATH_PDATA = "pdata";
//private static final String PATH_ODATA = "odata";
//private static final String PATH_PROBE_DATA_AT = "pdata_at";
//private static final String PATH_PROBE_DATA_FOR_ID = "pdata_id";
//private static final String PATH_PROBE_DATA_FOR_NAME = "pdata_nm";
//private static final String PATH_OUTLET_DATA_AT = "odata_at";
//private static final String PATH_OUTLET_DATA_FOR_ID = "odata_id";
//private static final String PATH_OUTLET_DATA_FOR_DID = "odata_did";
        case DATA:
        	return Data.CONTENT_TYPE;
        case PDATA:
        	return Data.CONTENT_TYPE;
        case ODATA:
        	return Data.CONTENT_TYPE;
        	
        case CONTROLLERS_ID_PROBEDATA_AT:
        	return Data.CONTENT_TYPE;
        	
        case CONTROLLERS_ID_OUTLETDATA_AT:
        	return Data.CONTENT_TYPE;
        	
        case CONTROLLERS_ID_PROBEDATA_FOR_ID:
        	return Data.CONTENT_TYPE;
        	
        case CONTROLLERS_ID_OUTLETDATA_FOR_ID:
        	return Data.CONTENT_TYPE;
        	
        case CONTROLLERS_ID_PROBEDATA_FOR_NAME:
        	return Data.CONTENT_TYPE;
        	
        case CONTROLLERS_ID_OUTLETDATA_FOR_DEVICE_ID:
        	return Data.CONTENT_TYPE;

        case CONTROLLERS_ID_OUTLETDATA:
        	return Data.CONTENT_TYPE;

        case CONTROLLERS_ID_PROBEDATA:
        	return Data.CONTENT_TYPE;
        	
//////////////////////////////////////////////
//private static final int LIVESTOCK = 901;
//private static final String PATH_LIVESTOCK = "livestock";
        case LIVESTOCK:
        	return Livestock.CONTENT_TYPE;

        default:
        	throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        if (LOGV) Log.v(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
        default: {
        	// Most cases are handled with simple SelectionBuilder
        	final SelectionBuilder builder = buildExpandedSelection(uri, match);
        	Cursor cursor = builder.where(selection, selectionArgs).query(db, projection, sortOrder);
        	
        	/* seems registering for changes is a two-step process. */
        	if(uri.compareTo(Livestock.buildQueryLivestockUri())==0 ||
        	   uri.compareTo(Controllers.buildQueryControllersUri())==0) {
        		cursor.setNotificationUri(getContext().getContentResolver(), uri);
        	}
        	return cursor;
        }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (LOGV) Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
		long rowId;

        switch (match) {
        
        // Insert a single controller
		case CONTROLLERS: {
			rowId = db.insertOrThrow(Tables.CONTROLLERS, Controllers.TITLE, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, rowId);
		}
				
		// Insert a probe for controller x.
		case CONTROLLERS_ID_PROBES: {
			rowId = db.insertOrThrow(Tables.PROBES, Probes.NAME, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, rowId);
		}
		
		// Insert an outlet for controller x.
		case CONTROLLERS_ID_OUTLETS: {
			rowId = db.insertOrThrow(Tables.OUTLETS, Outlets.NAME, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, rowId);
		}
		
		// Insert data for controller x and probe/outlet y
		case CONTROLLERS_ID_PROBES_ID: {
			values.put(Data.TYPE, 1);
			rowId = db.insertOrThrow(Tables.DATA, Data.VALUE, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, rowId);
		}
		case CONTROLLERS_ID_OUTLETS_ID: {
			values.put(Data.TYPE, 0);
			rowId = db.insertOrThrow(Tables.DATA, Data.VALUE, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, rowId);
		}
		
        // Insert a single livestock
		case LIVESTOCK: {
			rowId = db.insertOrThrow(Tables.LIVESTOCK, Livestock.COMMON_NAME, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, rowId);
		}

		default: {
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
        }
    }

    /** {@inheritDoc} */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (LOGV) Log.v(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
    	/** TODO:
    	 * The update by widget isn't working yet.  Only update by controller id.
    	 */

        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).update(db, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return retVal;
    }

    /** {@inheritDoc} */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (LOGV) Log.v(TAG, "delete(uri=" + uri + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
		int count = 0;

		switch (sUriMatcher.match(uri)) {
		case CONTROLLERS_ID: {
			// Delete all the data associated with this controller
			String controllerId = uri.getPathSegments().get(1);
			count += db.delete(Tables.PROBES, 
					Probes.CONTROLLER_ID + "= ?",
							new String[]{controllerId});
			count += db.delete(Tables.OUTLETS, 
					Outlets.CONTROLLER_ID + "= ?",
							new String[]{controllerId});
			count += db.delete(Tables.CONTROLLERS, 
					BaseColumns._ID + "= ?",
							new String[]{controllerId});
			/* TODO: should also delete the data too, but I need to figure in the join. */
			break;
		}
		case CONTROLLERS_ID_PROBEDATA_AT: {
			// Delete all the probe data older than x (in days)
			/* TODO: manage different save days for each controller. */
			String controllerId = uri.getPathSegments().get(1);
			long ageDays = Long.parseLong(uri.getPathSegments().get(3));
			long now = System.currentTimeMillis();
			String cutoff = Long.toString(now- (ageDays*DateUtils.DAY_IN_MILLIS));
			count += db.delete(Tables.DATA, 
//					Data.CONTROLLER_ID + "= ? and " +
							Data.TYPE + "= ? and " +
							Data.TIMESTAMP + "< ?", 
							new String[]{/*controllerId,*/ "1", cutoff});
			break;
		}
		case CONTROLLERS_ID_OUTLETDATA_AT: {
			// Delete all the probe data older than x
			String controllerId = uri.getPathSegments().get(1);
			long age = Long.parseLong(uri.getPathSegments().get(3));
			long now = System.currentTimeMillis();
			String cutoff = Long.toString(now-age);
			count += db.delete(Tables.DATA, 
//					ProbeDataColumns.CONTROLLER_ID + "= ?  and " +
							Data.TYPE + "= ? and " +
							Data.TIMESTAMP + "< ?",
							new String[]{/*controllerId,*/ "0", cutoff});
			break;
		}
		default:
	        final SelectionBuilder builder = buildSimpleSelection(uri);
	        count = builder.where(selection, selectionArgs).delete(db);
		}

		getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * Apply the given set of {@link ContentProviderOperation}, executing inside
     * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     */
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Build a simple {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually enough to support {@link #insert},
     * {@link #update}, and {@link #delete} operations.
     */
    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
        case CONTROLLERS: {
            return builder.table(Tables.CONTROLLERS);
        }
        case CONTROLLERS_ID: {
            final String controllerId = Controllers.getControllerId(uri);
            return builder.table(Tables.CONTROLLERS)
                    .where(BaseColumns._ID + "=?", controllerId);
        }
        case CONTROLLERS_URL: {
            final String controllerURL = Controllers.getControllerUrl(uri);
            return builder.table(Tables.CONTROLLERS)
                    .where(Controllers.WAN_URL + "=?", Uri.decode(controllerURL));
        }
        case CONTROLLERS_WID: {
            final String controllerWID = Controllers.getControllerWidget(uri);
            return builder.table(Tables.CONTROLLERS)
                    .where(Controllers.WIDGET + "=?", controllerWID);
        } 
        case LIVESTOCK: {
            return builder.table(Tables.LIVESTOCK);
        }
        default: {
        	throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        }
    }

    /**
     * Build an advanced {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually only used by {@link #query}, since it
     * performs table joins useful for {@link Cursor} data.
     */
    private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        switch (match) {
   
        case CONTROLLERS: {
            return builder.table(Tables.CONTROLLERS);
        }
        case CONTROLLERS_ID: {
            final String controllerId = Controllers.getControllerId(uri);
            return builder.table(Tables.CONTROLLERS)
                    .where(BaseColumns._ID + "=?", controllerId);
        }
        case CONTROLLERS_URL: {
            final String controllerURL = Controllers.getControllerUrl(uri);
            return builder.table(Tables.CONTROLLERS)
                    .where(Controllers.WAN_URL + "=?", Uri.decode(controllerURL));
        }
        case CONTROLLERS_WID: {
            final String controllerWID = Controllers.getControllerWidget(uri);
            return builder.table(Tables.CONTROLLERS)
                    .where(Controllers.WIDGET + "=?", controllerWID);
        }
        case CONTROLLERS_TITLE: {
            final String controllerTitle = Controllers.getControllerWidget(uri);
            return builder.table(Tables.CONTROLLERS)
                    .where(Controllers.TITLE + "=?", controllerTitle);
        }

        case CONTROLLERS_ID_PROBES: {
        	final String controllerId = Probes.getControllerId(uri);
        	return builder.table(Tables.PROBE_VIEW)
        			.where(Probes.CONTROLLER_ID + "=?", controllerId);
        }

        case CONTROLLERS_ID_PROBES_NAME: {
        	final String controllerId = Probes.getControllerId(uri);
        	final String probeName = Probes.getProbeName(uri);
        	return builder.table(Tables.PROBES)
        			.where(Probes.CONTROLLER_ID + "=?", controllerId)
        			.where(Probes.NAME + "=?", Uri.decode(probeName));
        }

        case CONTROLLERS_ID_OUTLETS: {
        	final String controllerId = Probes.getControllerId(uri);
        	return builder.table(Tables.OUTLET_VIEW)
        			.where(Probes.CONTROLLER_ID + "=?", controllerId);
        }

        case CONTROLLERS_ID_OUTLETS_DEVICE_ID: {
        	final String controllerId = Probes.getControllerId(uri);
        	final String outletDID = Probes.getProbeName(uri);
        	return builder.table(Tables.OUTLETS)
        			.where(Outlets.CONTROLLER_ID + "=?", controllerId)
        			.where(Outlets.DEVICE_ID + "=?", outletDID);
        }
        case PROBES: {
        	return builder.table(Tables.PROBE_VIEW);
        }
        case OUTLETS: {
        	return builder.table(Tables.OUTLET_VIEW);
        }
      
      // Get all data records regardless of controller, db maint use only
        /** TODO: probe and outlet data both! */
      case DATA: {
      	return builder.table(Tables.DATA);
      }
      case PDATA: {
        	return builder.table(Tables.PDATA_VIEW)
			.where(Data.TYPE + "=?", "1");
        }
      case ODATA: {
      	return builder.table(Tables.ODATA_VIEW)
			.where(Data.TYPE + "=?", "0");
        }
      
		case CONTROLLERS_ID_PROBEDATA: {
			String controllerId = uri.getPathSegments().get(1);
        	return builder.table(Tables.PDATA_VIEW)
        			.where(ProbeDataView.TYPE + "=?", "1")
        			.where(ProbeDataView.CONTROLLER_ID + "=?", controllerId);
		}
		case CONTROLLERS_ID_OUTLETDATA: {
			String controllerId = uri.getPathSegments().get(1);
        	return builder.table(Tables.ODATA_VIEW)
        			.where(OutletDataView.TYPE + "=?", "0")
        			.where(OutletDataView.CONTROLLER_ID + "=?", controllerId);
		}
		case CONTROLLERS_ID_PROBEDATA_AT: {
			String controllerId = uri.getPathSegments().get(1);
			String timestamp = uri.getPathSegments().get(3);
        	return builder.table(Tables.PDATA_VIEW)
        			.where(ProbeDataView.TYPE + "=?", "1")
        			.where(ProbeDataView.CONTROLLER_ID + "=?", controllerId)
        			.where(ProbeDataView.TIMESTAMP + "=?", timestamp);
		}
		case CONTROLLERS_ID_OUTLETDATA_AT: {
			String controllerId = uri.getPathSegments().get(1);
			String timestamp = uri.getPathSegments().get(3);
        	return builder.table(Tables.ODATA_VIEW)
        			.where(OutletDataView.TYPE + "=?", "0")
        			.where(OutletDataView.CONTROLLER_ID + "=?", controllerId)
        			.where(OutletDataView.TIMESTAMP + "=?", timestamp);
		}
		case CONTROLLERS_ID_PROBEDATA_FOR_NAME: {
			String controllerId = uri.getPathSegments().get(1);
			String probeName = uri.getPathSegments().get(3);
        	return builder.table(Tables.PDATA_VIEW)
        			.where(ProbeDataView.TYPE + "=?", "1")
        			.where(ProbeDataView.NAME + "=?", probeName)
        			.where(ProbeDataView.CONTROLLER_ID + "=?", controllerId);
		}
        case LIVESTOCK: {
            return builder.table(Tables.LIVESTOCK);
        }
		default: {
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
        }
    }



}
