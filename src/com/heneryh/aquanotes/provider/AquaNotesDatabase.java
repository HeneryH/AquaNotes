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
import com.heneryh.aquanotes.provider.AquaNotesDbContract.ControllersColumns;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Data;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.DataColumns;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Probes;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.ProbesColumns;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.Outlets;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.OutletsColumns;
import com.heneryh.aquanotes.provider.AquaNotesDbContract.SyncColumns;
import android.app.SearchManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Helper for managing {@link SQLiteDatabase} that stores data for
 * {@link AquaNotesDbProvider}.
 */
public class AquaNotesDatabase extends SQLiteOpenHelper {
    private static final String TAG = "AquaNotesDatabase";

    private static final String DATABASE_NAME = "aquanotes.db";

    // NOTE: carefully update onUpgrade() when bumping database versions to make
    // sure user data is saved.

    private static final int VER_LAUNCH = 1;
    private static final int VER_ADD_WIDGET = 2;
//    private static final int VER_URL_NOW_KEY = 3;

    private static final int DATABASE_VERSION = VER_ADD_WIDGET;

    interface Tables {
        String CONTROLLERS = "controllers";
        String PROBES = "probes";
        String OUTLETS = "outlets";
        String DATA = "data";
        
        String PROBE_VIEW = "probe_view";
        String OUTLET_VIEW = "outlet_view";
        String PDATA_VIEW = "pdata_view";
        String ODATA_VIEW = "odata_view";

    }


 
    public AquaNotesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	
//      String CONTROLLER_ID = "_id";
//      String TITLE = "title";
//      String WAN_URL = "wan_url";
//      String LAN_URL = "wifi_url";
//      String WIFI_SSID = "wifi_ssid";
//      String USER = "user";
//      String PW = "pw";
//      String LAST_UPDATED = "last_updated";
//      String UPDATE_INTERVAL = "update_i";
//      String DB_SAVE_DAYS = "db_save_days";
//      String MODEL = "controller_type";
//      String WIDGET = "widget";
      db.execSQL("CREATE TABLE " + Tables.CONTROLLERS + " ("
              + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
              + ControllersColumns.TITLE + " TEXT,"
              + ControllersColumns.WAN_URL + " TEXT,"
              + ControllersColumns.LAN_URL + " TEXT,"
              + ControllersColumns.WIFI_SSID + " TEXT,"
              + ControllersColumns.USER + " TEXT,"
              + ControllersColumns.PW + " TEXT,"
              + ControllersColumns.LAST_UPDATED + " LONG,"
              + ControllersColumns.UPDATE_INTERVAL + " INTEGER,"
              + ControllersColumns.DB_SAVE_DAYS + " INTEGER,"
              + ControllersColumns.MODEL + " TEXT,"    
              + ControllersColumns.WIDGET + " INTEGER,"    
      + "UNIQUE (" + ControllersColumns.WAN_URL + ") ON CONFLICT REPLACE)");
//    + ")");  // make sure to take this comma out if removing the unique

      
//      String PROBE_ID = "_id";
//      String PROBE_NAME = "probe_name";
//      String DEVICE_ID = "device_id";
//      String TYPE = "probe_type";
//      String RESOURCE_ID = "resource_id";
//      String CONTROLLER_ID = "controller_id";
      db.execSQL("CREATE TABLE " + Tables.PROBES + " ("
              + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
              + ProbesColumns.NAME + " TEXT,"
              + ProbesColumns.RESOURCE_ID + " INTEGER,"
              + ProbesColumns.CONTROLLER_ID + " INTEGER"
              + ")");

      db.execSQL("CREATE TABLE " + Tables.OUTLETS + " ("
              + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
              + OutletsColumns.NAME + " TEXT,"
              + OutletsColumns.DEVICE_ID + " TEXT,"
              + OutletsColumns.RESOURCE_ID + " INTEGER,"
              + OutletsColumns.CONTROLLER_ID + " INTEGER"
              + ")");

//      String DATA_ID = "_id";
//      String VALUE = "value";
//      String TIMESTAMP = "timestamp";
//      String PROBE_ID = "probe_id";
      db.execSQL("CREATE TABLE " + Tables.DATA + " ("
              + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
              + DataColumns.TYPE + " INTEGER,"
              + DataColumns.VALUE + " TEXT,"
              + DataColumns.PARENT_ID + " INTEGER,"
              + DataColumns.TIMESTAMP + " LONG"
              + ")");
      
      db.execSQL("CREATE VIEW "+ Tables.PROBE_VIEW +
      	    " AS SELECT" +
      	  " " +   Tables.PROBES + "." + BaseColumns._ID + ","+
      	    " " + Tables.PROBES + "." + Probes.NAME + "," +
      	    " " + Tables.PROBES + "." + Probes.RESOURCE_ID + "," +
      	    " " + Tables.PROBES + "." + Probes.CONTROLLER_ID + "," +
      	    " " + Tables.CONTROLLERS + "." + Controllers.TITLE + "," +
      	    " " + Tables.CONTROLLERS + "." + Controllers.WAN_URL + "," +
      	    " " + Tables.CONTROLLERS + "." + Controllers.LAN_URL + "," +
      	    " " + Tables.CONTROLLERS + "." + Controllers.WIFI_SSID + "," +
      	    " " + Tables.CONTROLLERS + "." + Controllers.USER + "," +
      	    " " + Tables.CONTROLLERS + "." + Controllers.PW + "," +
      	    " " + Tables.CONTROLLERS + "." + Controllers.LAST_UPDATED + "," +
      	    " " + Tables.CONTROLLERS + "." + Controllers.UPDATE_INTERVAL + "," +
      	    " " + Tables.CONTROLLERS + "." + Controllers.DB_SAVE_DAYS + "," +
      	    " " + Tables.CONTROLLERS + "." + Controllers.MODEL + "" +
      	    " FROM " + Tables.PROBES + " JOIN " + Tables.CONTROLLERS +
      	    " ON " + Tables.PROBES + "." + Probes.CONTROLLER_ID + " =" + Tables.CONTROLLERS + "." + BaseColumns._ID
      	    );

      db.execSQL("CREATE VIEW "+ Tables.OUTLET_VIEW +
        	    " AS SELECT" +
        	  " " +   Tables.OUTLETS + "." + BaseColumns._ID + ","+
        	    " " + Tables.OUTLETS + "." + Outlets.NAME + "," +
        	    " " + Tables.OUTLETS + "." + Outlets.DEVICE_ID + "," +
        	    " " + Tables.OUTLETS + "." + Outlets.RESOURCE_ID + "," +
        	    " " + Tables.OUTLETS + "." + Outlets.CONTROLLER_ID + "," +
        	    " " + Tables.CONTROLLERS + "." + Controllers.TITLE + "," +
        	    " " + Tables.CONTROLLERS + "." + Controllers.WAN_URL + "," +
        	    " " + Tables.CONTROLLERS + "." + Controllers.LAN_URL + "," +
        	    " " + Tables.CONTROLLERS + "." + Controllers.WIFI_SSID + "," +
        	    " " + Tables.CONTROLLERS + "." + Controllers.USER + "," +
        	    " " + Tables.CONTROLLERS + "." + Controllers.PW + "," +
        	    " " + Tables.CONTROLLERS + "." + Controllers.LAST_UPDATED + "," +
        	    " " + Tables.CONTROLLERS + "." + Controllers.UPDATE_INTERVAL + "," +
        	    " " + Tables.CONTROLLERS + "." + Controllers.DB_SAVE_DAYS + "," +
        	    " " + Tables.CONTROLLERS + "." + Controllers.MODEL + "" +
        	    " FROM " + Tables.OUTLETS + " JOIN " + Tables.CONTROLLERS +
        	    " ON " + Tables.OUTLETS + "." + Outlets.CONTROLLER_ID + " =" + Tables.CONTROLLERS + "." + BaseColumns._ID
        	    );

      db.execSQL("CREATE VIEW "+ Tables.PDATA_VIEW +
      	    " AS SELECT " + Tables.DATA + "." + BaseColumns._ID + " ," +
      	    " " + Tables.DATA + "." + Data.TYPE + "," +
      	    " " + Tables.DATA + "." + Data.VALUE + "," +
      	    " " + Tables.DATA + "." + Data.TIMESTAMP + "," +
      	    " " + Tables.DATA + "." + Data.PARENT_ID + "," +
      	    " " + Tables.PROBES + "." + Probes.NAME + "," +
      	    " " + Tables.PROBES + "." + Probes.RESOURCE_ID + "," +
      	    " " + Tables.PROBES + "." + Probes.CONTROLLER_ID + "" +
      	    " FROM " + Tables.DATA + " JOIN " + Tables.PROBES +
      	    " ON " + Tables.DATA + "." + Data.PARENT_ID + " =" + Tables.PROBES + "." + BaseColumns._ID
      	    );

      db.execSQL("CREATE VIEW "+ Tables.ODATA_VIEW +
        	    " AS SELECT " + Tables.DATA + "." + BaseColumns._ID + " ," +
          	    " " + Tables.DATA + "." + Data.TYPE + "," +
        	    " " + Tables.DATA + "." + Data.VALUE + "," +
        	    " " + Tables.DATA + "." + Data.TIMESTAMP + "," +
        	    " " + Tables.DATA + "." + Data.PARENT_ID + "," +
        	    " " + Tables.OUTLETS + "." + Outlets.NAME + "," +
        	    " " + Tables.OUTLETS + "." + Outlets.DEVICE_ID + "," +
        	    " " + Tables.OUTLETS + "." + Outlets.RESOURCE_ID + "," +
        	    " " + Tables.OUTLETS + "." + Outlets.CONTROLLER_ID + "" +
        	    " FROM " + Tables.DATA + " JOIN " + Tables.OUTLETS +
        	    " ON " + Tables.DATA + "." + Data.PARENT_ID + " =" + Tables.OUTLETS + "." + BaseColumns._ID
        	    );

    }


     @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

        // NOTE: This switch statement is designed to handle cascading database
        // updates, starting at the current version and falling through to all
        // future upgrade cases. Only use "break;" when you want to drop and
        // recreate the entire database.
        int version = oldVersion;

        switch (version) {
        case VER_LAUNCH:
        	// Version 2 reworked a lot but not the controllers.
            Log.w(TAG, "Adding widget ID column to controllers table");
            db.execSQL("ALTER TABLE " + Tables.CONTROLLERS + " ADD COLUMN "
            		+ Controllers.WIDGET + " INTEGER");
        	version = VER_ADD_WIDGET;
        	
//        case VER_REWORK_ALL_TABLES:
//        	// Version 3 changed key for controllers, sorry.
//            Log.w(TAG, "Destroying old data during upgrade");
//
//            db.execSQL("DROP TABLE IF EXISTS " + Tables.CONTROLLERS);
//            db.execSQL("DROP TABLE IF EXISTS " + Tables.PROBES);
//            db.execSQL("DROP TABLE IF EXISTS " + Tables.OUTLETS);
//            db.execSQL("DROP TABLE IF EXISTS " + Tables.DATA);
//            
//            db.execSQL("DROP VIEW IF EXISTS " + Tables.PROBE_VIEW);
//            db.execSQL("DROP VIEW IF EXISTS " + Tables.OUTLET_VIEW);
//            db.execSQL("DROP VIEW IF EXISTS " + Tables.PDATA_VIEW);
//            db.execSQL("DROP VIEW IF EXISTS " + Tables.ODATA_VIEW);
//
//            onCreate(db);
//        	version = VER_URL_NOW_KEY;
//
//  case VER_SESSION_FEEDBACK_URL:
//      // Version 3 added columns for session official notes URL and slug.
//      db.execSQL("ALTER TABLE " + Tables.SESSIONS + " ADD COLUMN "
//              + SessionsColumns.SESSION_NOTES_URL + " TEXT");
//      db.execSQL("ALTER TABLE " + Tables.SESSIONS + " ADD COLUMN "
//              + SessionsColumns.SESSION_SLUG + " TEXT");
//      version = VER_SESSION_NOTES_URL_SLUG;
}

        Log.d(TAG, "after upgrade logic, at version " + version);
        if (version != DATABASE_VERSION) {
            Log.w(TAG, "Destroying old data during upgrade");

            db.execSQL("DROP TABLE IF EXISTS " + Tables.CONTROLLERS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.PROBES);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.OUTLETS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.DATA);
            
            db.execSQL("DROP VIEW IF EXISTS " + Tables.PROBE_VIEW);
            db.execSQL("DROP VIEW IF EXISTS " + Tables.OUTLET_VIEW);
            db.execSQL("DROP VIEW IF EXISTS " + Tables.PDATA_VIEW);
            db.execSQL("DROP VIEW IF EXISTS " + Tables.ODATA_VIEW);

            onCreate(db);
        }
    }
}
