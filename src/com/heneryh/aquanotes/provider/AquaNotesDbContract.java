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

import android.app.SearchManager;
import android.graphics.Color;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.DateUtils;

import java.util.List;

/**
 * Contract class for interacting with {@link AquaNotesDbProvider}. Unless
 * otherwise noted, all time-based fields are milliseconds since epoch and can
 * be compared against {@link System#currentTimeMillis()}.
 * <p>
 * The backing {@link android.content.ContentProvider} assumes that {@link Uri} are generated
 * using stronger {@link String} identifiers, instead of {@code int}
 * {@link BaseColumns#_ID} values, which are prone to shuffle during sync.
 */
public class AquaNotesDbContract {

    interface ControllersColumns {
        /** Unique string identifying this block of time. */
        String _ID = "_id";
        
        /** Title describing this block of time. */
        String TITLE = "title";
        
        /** Title describing this block of time. */
        String WAN_URL = "wan_url";
        
        /** Title describing this block of time. */
        String LAN_URL = "lan_url";
        
        /** Title describing this block of time. */
        String WIFI_SSID = "wifi_ssid";
        
        /** Title describing this block of time. */
        String USER = "user";
        
        /** Title describing this block of time. */
        String PW = "pw";
        
        /** Time when this block starts. */
        String LAST_UPDATED = "last_updated";
        
        /** Title describing this block of time. */
        String UPDATE_INTERVAL = "update_i";
        
        /** Title describing this block of time. */
        String DB_SAVE_DAYS = "db_save_days";
        
        /** Type describing this block. */
        String MODEL = "model";

        /** Type describing this block. */
        String WIDGET = "widget";
    }


    interface ProbesColumns {
        /** Unique string identifying this track. */
        String _ID = "_id";
        
        /** Name describing this track. */
        String NAME = "name";
        
        /** Color used to identify this track, in {@link Color#argb} format. */
        String RESOURCE_ID = "resource_id";
        
        /** Body of text explaining this track in detail. */
        String CONTROLLER_ID = "controller_id";
    }

    interface OutletsColumns {
        /** Unique string identifying this track. */
        String _ID = "_id";
        
        /** Name describing this track. */
        String NAME = "name";
        
        /** Name describing this track. */
        String DEVICE_ID = "device_id";
        
        /** Color used to identify this track, in {@link Color#argb} format. */
        String RESOURCE_ID = "resource_id";
        
        /** Body of text explaining this track in detail. */
        String CONTROLLER_ID = "controller_id";
    }


    interface DataColumns {
        /** Unique string identifying this room. */
        String _ID = "_id";
        
        /** Color used to identify this track, in {@link Color#argb} format. */
        String TYPE = "type";
        
        /** Name describing this room. */
        String VALUE = "value";
        
        /** Name describing this room. */
        String TIMESTAMP = "timestamp";
        
        /** Building floor this room exists on. */
        String PARENT_ID = "parent_id";
    }
    
    interface ViewProbesColumns {
        /** Name describing this track. */
        String NAME = "name";
        
        /** Color used to identify this track, in {@link Color#argb} format. */
        String RESOURCE_ID = "resource_id";
        
        /** Body of text explaining this track in detail. */
        String CONTROLLER_ID = "controller_id";
        
//		//\\
//		Join
//		\\//
        
        /** Title describing this block of time. */
        String TITLE = "title";
        
        /** Title describing this block of time. */
        String WAN_URL = "wan_url";
        
        /** Title describing this block of time. */
        String LAN_URL = "lan_url";
        
        /** Title describing this block of time. */
        String WIFI_SSID = "wifi_ssid";
        
        /** Title describing this block of time. */
        String USER = "user";
        
        /** Title describing this block of time. */
        String PW = "pw";
        
        /** Time when this block starts. */
        String LAST_UPDATED = "last_updated";
        
        /** Title describing this block of time. */
        String UPDATE_INTERVAL = "update_i";
        
        /** Title describing this block of time. */
        String DB_SAVE_DAYS = "db_save_days";
        
        /** Type describing this block. */
        String MODEL = "model";

	
    }
    
    interface ViewOutletsColumns {
        /** Name describing this track. */
        String NAME = "name";
        
        /** Name describing this track. */
        String DEVICE_ID = "device_id";
        
        /** Color used to identify this track, in {@link Color#argb} format. */
        String RESOURCE_ID = "resource_id";
        
        /** Body of text explaining this track in detail. */
        String CONTROLLER_ID = "controller_id";
        
//		//\\
//		Join
//		\\//
        
        /** Title describing this block of time. */
        String TITLE = "title";
        
        /** Title describing this block of time. */
        String WAN_URL = "wan_url";
        
        /** Title describing this block of time. */
        String LAN_URL = "lan_url";
        
        /** Title describing this block of time. */
        String WIFI_SSID = "wifi_ssid";
        
        /** Title describing this block of time. */
        String USER = "user";
        
        /** Title describing this block of time. */
        String PW = "pw";
        
        /** Time when this block starts. */
        String LAST_UPDATED = "last_updated";
        
        /** Title describing this block of time. */
        String UPDATE_INTERVAL = "update_i";
        
        /** Title describing this block of time. */
        String DB_SAVE_DAYS = "db_save_days";
        
        /** Type describing this block. */
        String MODEL = "model";

	
    }
    
    interface ViewProbeDataColumns {
        /** Unique string identifying this room. */
        String _ID = "_id";
        
        /** Color used to identify this track, in {@link Color#argb} format. */
        String TYPE = "type";
        
        /** Name describing this room. */
        String VALUE = "value";
        
        /** Name describing this room. */
        String TIMESTAMP = "timestamp";
        
        /** Building floor this room exists on. */
        String PARENT_ID = "parent_id";   	

//		//\\
//		Join
//		\\//
        
        /** Name describing this track. */
        String NAME = "name";
        
        /** Color used to identify this track, in {@link Color#argb} format. */
        String RESOURCE_ID = "resource_id";
        
        /** Body of text explaining this track in detail. */
        String CONTROLLER_ID = "controller_id";
        
    }

    interface ViewOutletDataColumns {
        /** Unique string identifying this room. */
        String _ID = "_id";
        
        /** Color used to identify this track, in {@link Color#argb} format. */
        String TYPE = "type";
        
        /** Name describing this room. */
        String VALUE = "value";
        
        /** Name describing this room. */
        String TIMESTAMP = "timestamp";
        
        /** Building floor this room exists on. */
        String PARENT_ID = "parent_id";   	

//		//\\
//		Join
//		\\//
        
        /** Name describing this track. */
        String NAME = "name";
        
        /** Name describing this track. */
        String DEVICE_ID = "device_id";
        
        /** Color used to identify this track, in {@link Color#argb} format. */
        String RESOURCE_ID = "resource_id";
        
        /** Body of text explaining this track in detail. */
        String CONTROLLER_ID = "controller_id";
        
    }

    interface LivestockColumns {
        /** Unique string identifying this livestock. */
        String _ID = "_id";
        
        /** Common name for this type of livestock*/
        String COMMON_NAME = "common_name";
        
        /** Type of livestock this is, ie fish, sps, lps etc. */
        String TYPE = "type";
        
        /** Timestamp of xxx. */
        String TIMESTAMP = "timestamp";
        
        /** Resource ID of the thumbnail image for this item. */
        String THUMBNAIL = "thumbnail";
    }
    


    public static final String CONTENT_AUTHORITY = "com.heneryh.aquanotes";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_CONTROLLERS = "controllers";
    private static final String PATH_CONTROLLERS_URL = "url";
    private static final String PATH_CONTROLLERS_TITLE = "title";
    private static final String PATH_CONTROLLERS_WIDGET = "widget";

    private static final String PATH_PROBES = "probes";
    private static final String PATH_PROBES_NAME = "probes_nm";
    
    private static final String PATH_OUTLETS = "outlets";
    private static final String PATH_OUTLETS_RESOURCE_ID = "outlets_rsc";
    private static final String PATH_OUTLETS_DEVICE_ID = "outlets_did";

    private static final String PATH_DATA = "data";
    private static final String PATH_PDATA = "pdata";
    private static final String PATH_ODATA = "odata";
    private static final String PATH_PROBE_DATA_AT = "pdata_at";
    private static final String PATH_PROBE_DATA_FOR_ID = "pdata_id";
    private static final String PATH_PROBE_DATA_FOR_NAME = "pdata_nm";
    private static final String PATH_OUTLET_DATA_AT = "odata_at";
    private static final String PATH_OUTLET_DATA_FOR_ID = "odata_id";
    private static final String PATH_OUTLET_DATA_FOR_DID = "odata_did";

    private static final String PATH_LIVESTOCK = "livestock";

    
    /**
     * Controllers are 
     */
    public static class Controllers implements ControllersColumns, BaseColumns {

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.aquanotes.controllers";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.aquanotes.controllers";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONTROLLERS).build();

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = ControllersColumns.LAST_UPDATED + " DESC ";

        //          content://org.dvrc.aquanotes/controllers
        public static Uri buildQueryControllersUri() {
            return BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONTROLLERS)
            		.build();
        }
        
        public static Uri buildInsertControllerUri() { 
        	return buildQueryControllersUri();  // same as query
        }

        //          content://org.dvrc.aquanotes/controllers/x
        public static Uri buildQueryControllerXUri(Integer controllerId) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.build();
        }
        
        //          content://org.dvrc.aquanotes/controllers/url/nnn
        public static Uri buildQueryControllerByUrlUri(String controllerUrl) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS)
            		.appendPath(PATH_CONTROLLERS_URL).appendPath(/*Uri.encode(*/controllerUrl) // appendPath() already encodes
            		.build();
        }
        
        //          content://org.dvrc.aquanotes/controllers/title/nnn
        public static Uri buildQueryControllerByTitleUri(String controllerTitle) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS)
            		.appendPath(PATH_CONTROLLERS_TITLE).appendPath(/*Uri.encode(*/controllerTitle) // appendPath() already encodes
            		.build();
        }
        
        //          content://org.dvrc.aquanotes/controllers/widget/nnn
        public static Uri buildQueryControllerByWidgetUri(String controllerWid) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS)
            		.appendPath(PATH_CONTROLLERS_WIDGET).appendPath(controllerWid)
            		.build();
        }
        
        public static Uri buildDeleteControllerXUri(int controllerId) {
        	return buildQueryControllerXUri(controllerId); // same as query
        }

        public static Uri buildDeleteControllerUrlUri(String controllerUrl) {
        	return buildQueryControllerByUrlUri(controllerUrl); // same as query
        }

        public static Uri buildUpdateControllerXUri(int controllerId) {
        	return buildQueryControllerXUri(controllerId); // same as query
        }

        public static Uri buildUpdateControllerXUri(String controllerUrl) {
        	return buildQueryControllerByUrlUri(controllerUrl); // same as query
        }

        //          content://org.dvrc.aquanotes/controllers/x
        public static String getControllerId(Uri controllerXUri) {
            return controllerXUri.getPathSegments().get(1);
        }
        public static String getControllerWidget(Uri controllerXUri) {
            return controllerXUri.getPathSegments().get(2);
        }
        public static String getControllerUrl(Uri controllerXUri) {
            return controllerXUri.getPathSegments().get(2);
        }
    }

    /**
     * Probes are overall categories for {@link Sessions} and {@link Vendors},
     * such as "Android" or "Enterprise."
     */
    public static class Probes implements ProbesColumns, BaseColumns {

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.aquanotes.probes";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.aquanotes.probes";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PROBES).build();

        /** Count of {@link Sessions} inside given track. */
        public static final String DATA_COUNT = "data_count";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = ProbesColumns._ID + " ASC";


//////////////////////////////////////////////
//private static final int CONTROLLERS_ID_PROBES = 201;
//private static final int CONTROLLERS_ID_PROBES_ID = 202;
//private static final int CONTROLLERS_ID_PROBES_NAME = 203;
//private static final int CONTROLLERS_ID_OUTLETS = 204;
//private static final int CONTROLLERS_ID_OUTLETS_ID = 205;
//private static final int CONTROLLERS_ID_OUTLETS_DEVICE_ID = 206;
//private static final int CONTROLLERS_ID_OUTLETS_RSC = 207; 
//private static final String PATH_PROBES = "probes";
//private static final String PATH_PROBES_NAME = "probes_nm";
//private static final String PATH_OUTLETS = "outlets";
//private static final String PATH_OUTLETS_RESOURCE_ID = "outlets_rsc";
//private static final String PATH_OUTLETS_DEVICE_ID = "outlets_did";

        /** Build {@link Uri} for requested {@link #PROBE_ID}. */
        //          content://org.dvrc.aquanotes/controllers/x/probes
        public static Uri buildQueryProbesUri(Integer controllerId) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.appendPath(PATH_PROBES)
            		.build();
        }

        /** Build {@link Uri} for requested {@link #PROBE_ID}. */
        //          content://org.dvrc.aquanotes/controllers/x/probes
        public static Uri buildQueryProbesUri(Uri controllerUri) {
            return controllerUri.buildUpon()
            		.appendPath(PATH_PROBES)
            		.build();
        }
        
        public static Uri buildInsertProbeUri(Uri controllerUri) {
        	return buildQueryProbesUri(controllerUri); // same as query
        }

        /** Build {@link Uri} for requested {@link #PROBE_ID}. */
        //          content://org.dvrc.aquanotes/controllers/x/probes/y
        public static Uri buildQueryProbeXByIdUri(Integer controllerId, Integer probeId) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.appendPath(PATH_PROBES).appendPath(probeId.toString())
            		.build();
        }

        /** Build {@link Uri} for requested {@link #PROBE_ID}. */
        //          content://org.dvrc.aquanotes/controllers/x/probes_nm/abc
        public static Uri buildQueryProbeXByNameUri(Integer controllerId, String probeName) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.appendPath(PATH_PROBES_NAME).appendPath(/*Uri.encode(*/probeName)
            		.build();
        }

        /** Build {@link Uri} for requested {@link #PROBE_ID}. */
        //          content://org.dvrc.aquanotes/controllers/x/probes_nm/abc
        public static Uri buildQueryProbeByNameUri(Uri controllerUri, String probeName) {
            return controllerUri.buildUpon()
             		.appendPath(PATH_PROBES_NAME).appendPath(/*Uri.encode(*/probeName)
            		.build();
        }


        public static String getControllerId(Uri controllerXprobeYUri) {
            return controllerXprobeYUri.getPathSegments().get(1);
        }
        public static Integer getProbeId(Uri controllerXprobeYUri) {
            return Integer.valueOf(controllerXprobeYUri.getPathSegments().get(3));
        }
        public static String getProbeName(Uri controllerXprobeYUri) {
            return controllerXprobeYUri.getPathSegments().get(3);
        }

    }

    /**
     * Probes are overall categories for {@link Sessions} and {@link Vendors},
     * such as "Android" or "Enterprise."
     */
    public static class Outlets implements OutletsColumns, BaseColumns {

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.aquanotes.outlets";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.aquanotes.outlets";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_OUTLETS).build();

        /** Count of {@link Sessions} inside given track. */
        public static final String DATA_COUNT = "data_count";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = ProbesColumns._ID + " ASC";


//////////////////////////////////////////////
//private static final int CONTROLLERS_ID_PROBES = 201;
//private static final int CONTROLLERS_ID_PROBES_ID = 202;
//private static final int CONTROLLERS_ID_PROBES_NAME = 203;
//private static final int CONTROLLERS_ID_OUTLETS = 204;
//private static final int CONTROLLERS_ID_OUTLETS_ID = 205;
//private static final int CONTROLLERS_ID_OUTLETS_DEVICE_ID = 206;
//private static final int CONTROLLERS_ID_OUTLETS_RSC = 207; 
//private static final String PATH_PROBES = "probes";
//private static final String PATH_PROBES_NAME = "probes_nm";
//private static final String PATH_OUTLETS = "outlets";
//private static final String PATH_OUTLETS_RESOURCE_ID = "outlets_rsc";
//private static final String PATH_OUTLETS_DEVICE_ID = "outlets_did";

        /** Build {@link Uri} for requested {@link #PROBE_ID}. */
        //          content://org.dvrc.aquanotes/controllers/x/outlets
        public static Uri buildQueryOutletsUri(Integer controllerId) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.appendPath(PATH_OUTLETS)
            		.build();
        }

        /** Build {@link Uri} for requested {@link #PROBE_ID}. */
        //          content://org.dvrc.aquanotes/controllers/x/outlets
        public static Uri buildQueryOutletsUri(Uri controllerUri) {
            return controllerUri.buildUpon()
            		.appendPath(PATH_OUTLETS)
            		.build();
        }

        /** Build {@link Uri} for requested {@link #PROBE_ID}. */
        //          content://org.dvrc.aquanotes/controllers/x/outlets/x
       public static Uri buildQueryOutletXByIdUri(Integer controllerId, Integer probeId) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.appendPath(PATH_OUTLETS).appendPath(probeId.toString())
            		.build();
        }

        /** Build {@link Uri} for requested {@link #PROBE_ID}. */
        //          content://org.dvrc.aquanotes/controllers/x/outlets_did/abc
        public static Uri buildQueryOutletXByDeviceIdUri(Integer controllerId, String outletDId) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.appendPath(PATH_OUTLETS_DEVICE_ID).appendPath(outletDId)
            		.build();
        }

        /** Build {@link Uri} for requested {@link #PROBE_ID}. */
        //          content://org.dvrc.aquanotes/controllers/x/outlets_did/abc
        public static Uri buildQueryOutletXByDeviceIdUri(Uri controllerUri, String outletDId) {
            return controllerUri.buildUpon()
            		.appendPath(PATH_OUTLETS_DEVICE_ID).appendPath(outletDId)
            		.build();
        }

        /** Build {@link Uri} for requested {@link #PROBE_ID}. */
        //          content://org.dvrc.aquanotes/controllers/x/outlets_rsc/abc
        public static Uri buildQueryOutletXByResourceIdUri(Integer controllerId, String outletRId) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.appendPath(PATH_OUTLETS_RESOURCE_ID).appendPath(outletRId)
            		.build();
        }

        public static Uri buildInsertOutletUri(Uri controllerUri) {
        	return buildQueryOutletsUri(controllerUri); // same as query
        }

        public static String getControllerId(Uri controllerXprobeYUri) {
            return controllerXprobeYUri.getPathSegments().get(1);
        }
        public static Integer getOutletId(Uri controllerXprobeYUri) {
            return Integer.valueOf(controllerXprobeYUri.getPathSegments().get(3));
        }
        public static String getOutletName(Uri controllerXprobeYUri) {
            return controllerXprobeYUri.getPathSegments().get(3);
        }

    }
    
    /**
     * Probes are overall categories for {@link Sessions} and {@link Vendors},
     * such as "Android" or "Enterprise."
     */
    public static class ProbesView implements ViewProbesColumns, BaseColumns {

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.aquanotes.probes";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.aquanotes.probes";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = ProbesColumns._ID + " ASC";
    }
    public static class OutletsView implements ViewOutletsColumns, BaseColumns {

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.aquanotes.outlets";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.aquanotes.outlets";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = OutletsColumns._ID + " ASC";
    }

    /**
     * ProbeData are physical locations at the conference venue.
     */
    public static class Data implements DataColumns, BaseColumns {

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.aquanotes.data";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.aquanotes.data";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DATA).build();
 
 

        /** Default "ORDER BY" clause. */
//        public static final String DEFAULT_SORT = ProbeDataColumns.PROBE_ID + " ASC, "
//                + ProbeDataColumns.VALUE + " COLLATE NOCASE ASC";
        public static final String DEFAULT_SORT = DataColumns.TIMESTAMP + " DESC ";
//                + ProbeDataColumns.VALUE + " COLLATE NOCASE ASC";

//////////////////////////////////////////////
//        private static final String PATH_DATA = "data";
//        private static final String PATH_PDATA = "pdata";
//        private static final String PATH_ODATA = "odata";
//        private static final String PATH_PROBE_DATA_AT = "pdata_at";
//        private static final String PATH_PROBE_DATA_FOR_ID = "pdata_id";
//        private static final String PATH_PROBE_DATA_FOR_NAME = "pdata_nm";
//        private static final String PATH_OUTLET_DATA_AT = "odata_at";
//        private static final String PATH_OUTLET_DATA_FOR_ID = "odata_id";
//        private static final String PATH_OUTLET_DATA_FOR_DID = "odata_did";
//   
        
//      content://org.dvrc.aquanotes/controllers/x/pdata_at/y
//      content://org.dvrc.aquanotes/controllers/x/odata_at/y
        public static Uri buildDeletePDataOlderThanUri(Integer controllerId, Integer days) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.appendPath(PATH_PROBE_DATA_AT).appendPath(days.toString())
            		.build();
        }
        public static Uri buildDeleteODataOlderThanUri(Integer controllerId, Integer days) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.appendPath(PATH_OUTLET_DATA_AT).appendPath(days.toString())
            		.build();
        }
        public static Uri buildQueryPDataAtUri(Integer controllerId, Long timestamp) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.appendPath(PATH_PROBE_DATA_AT).appendPath(timestamp.toString())
            		.build();
        }
        public static Uri buildQueryPDataAtUri(Uri controllerUri, Long timestamp) {
            return controllerUri.buildUpon()
            		.appendPath(PATH_PROBE_DATA_AT).appendPath(timestamp.toString())
            		.build();
        }
        public static Uri buildQueryODataAtUri(Integer controllerId, Long timestamp) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.appendPath(PATH_OUTLET_DATA_AT).appendPath(timestamp.toString())
            		.build();
        }
      
        
//      content://org.dvrc.aquanotes/controllers/x/pdata_for_nm/y
//      content://org.dvrc.aquanotes/controllers/x/pdata_for_id/y
//      content://org.dvrc.aquanotes/controllers/x/odata_for_id/y
//      content://org.dvrc.aquanotes/controllers/x/odata_for_deviceID/y
        public static Uri buildQueryProbeDataByNameUri(Integer controllerId, String probeNm) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.appendPath(PATH_PROBE_DATA_FOR_NAME).appendPath(probeNm)
            		.build();
        }
        public static Uri buildQueryProbeDataByNameUri(Uri controllerUri, String probeNm) {
            return controllerUri.buildUpon()
            		.appendPath(PATH_PROBE_DATA_FOR_NAME).appendPath(probeNm)
            		.build();
        }
        public static Uri buildQueryProbeDataByIdUri(Integer controllerId, Integer probeId) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.appendPath(PATH_PROBE_DATA_FOR_ID).appendPath(probeId.toString())
            		.build();
        }
        public static Uri buildQueryOutletDataByIdUri(Integer controllerId, Integer outletId) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.appendPath(PATH_OUTLET_DATA_FOR_ID).appendPath(outletId.toString())
            		.build();
        }
        public static Uri buildQueryOutlerDataByDidUri(Integer controllerId, String outletDid) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.appendPath(PATH_OUTLET_DATA_FOR_DID).appendPath(outletDid)
            		.build();
        }

        /** Build {@link Uri} for requested {@link #DATA_ID}. */
        //          content://org.dvrc.aquanotes/controllers/x/data
        public static Uri buildQueryAllOutletDataUri(Uri controllerUri) {
            return controllerUri.buildUpon()
            		.appendPath(PATH_PDATA)
            		.build();
        }
        public static Uri buildQueryAllOutletDataUri(Integer controllerId) {
            return BASE_CONTENT_URI.buildUpon()
            		.appendPath(PATH_CONTROLLERS).appendPath(controllerId.toString())
            		.appendPath(PATH_ODATA)
            		.build();
        }

        
        /** Build {@link Uri} for requested {@link #DATA_ID}. */
        //          content://org.dvrc.aquanotes/controllers/x/probes/y/data
        public static Uri buildInsertProbeDataUri(Uri controllerUri, Integer probeId) {
            return controllerUri.buildUpon()
            		.appendPath(PATH_PROBES).appendPath(probeId.toString())
            		.build();
        }
        
        /** Build {@link Uri} for requested {@link #DATA_ID}. */
        //          content://org.dvrc.aquanotes/controllers/x/probes/y/data
        public static Uri buildInsertOutletDataUri(Uri controllerUri, Integer outletId) {
            return controllerUri.buildUpon()
            		.appendPath(PATH_OUTLETS).appendPath(outletId.toString())
            		.build();
        }

    }

    public static class ProbeDataView implements ViewProbeDataColumns, BaseColumns {

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.aquanotes.pdata";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.aquanotes.pdata";
        
        public static final Uri CONTENT_P_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PDATA).build();
 
        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = ViewProbeDataColumns.TIMESTAMP + " DESC";
    }


     public static class OutletDataView implements ViewOutletDataColumns, BaseColumns {

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.aquanotes.odata";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.aquanotes.odata";

        public static final Uri CONTENT_O_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ODATA).build();
 


        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = ViewOutletDataColumns.TIMESTAMP + " DESC";
    }

     /**
      * Controllers are 
      */
     public static class Livestock implements LivestockColumns, BaseColumns {

         public static final String CONTENT_TYPE =
                 "vnd.android.cursor.dir/vnd.aquanotes.livestock";
         public static final String CONTENT_ITEM_TYPE =
                 "vnd.android.cursor.item/vnd.aquanotes.livestock";

         public static final Uri CONTENT_URI =
                 BASE_CONTENT_URI.buildUpon().appendPath(PATH_LIVESTOCK).build();

         /** Default "ORDER BY" clause. */
         public static final String DEFAULT_SORT = LivestockColumns.TIMESTAMP + " DESC ";

         //          content://org.dvrc.aquanotes/livestock
         public static Uri buildQueryLivestockUri() {
             return BASE_CONTENT_URI.buildUpon().appendPath(PATH_LIVESTOCK)
             		.build();
         }
         
         public static Uri buildInsertLivestockUri() { 
         	return buildQueryLivestockUri();  // same as query
         }

         //          content://org.dvrc.aquanotes/livestock/x
         public static Uri buildQueryLivestockXUri(Integer livestockId) {
             return BASE_CONTENT_URI.buildUpon()
             		.appendPath(PATH_LIVESTOCK).appendPath(livestockId.toString())
             		.build();
         }
         
          
         public static Uri buildDeleteLivestockXUri(int livestockId) {
         	return buildQueryLivestockXUri(livestockId); // same as query
         }

         public static Uri buildUpdateControllerXUri(int livestockId) {
         	return buildQueryLivestockXUri(livestockId); // same as query
         }

         //          content://org.dvrc.aquanotes/controllers/x
         public static String getLivestockId(Uri livestockXUri) {
             return livestockXUri.getPathSegments().get(1);
         }
         public static String getControllerWidget(Uri controllerXUri) {
             return controllerXUri.getPathSegments().get(2);
         }
         public static String getControllerUrl(Uri controllerXUri) {
             return controllerXUri.getPathSegments().get(2);
         }
     }


    private AquaNotesDbContract() {
    }
}
