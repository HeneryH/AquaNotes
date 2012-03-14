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

public class Directory {
    private static LivestockCategory[] mCategories;

    public static void initializeDirectory() {
    	mCategories = new LivestockCategory[] {
    			new LivestockCategory("Fish"),
    			new LivestockCategory("Softies"),
    			new LivestockCategory("LPS"),
    			new LivestockCategory("SPS"), };


//    	mCategories[0].addEntry(new LivestockEntry("Red Balloon", R.drawable.red_balloon));
//    	mCategories[0].addEntry(new LivestockEntry("Green Balloon", R.drawable.green_balloon));
//    	mCategories[0].addEntry(new LivestockEntry("Blue Balloon", R.drawable.blue_balloon));


//    	mCategories[1].addEntry(new LivestockEntry("Old school huffy", R.drawable.blue_bike));
//    	mCategories[1].addEntry(new LivestockEntry("New Bikes", R.drawable.rainbow_bike));
//    	mCategories[1].addEntry(new LivestockEntry("Chrome Fast", R.drawable.chrome_wheel));

//    	mCategories[2].addEntry(new LivestockEntry("Steampunk Android", R.drawable.punk_droid));
//    	mCategories[2].addEntry(new LivestockEntry("Stargazing Android", R.drawable.stargazer_droid));
//    	mCategories[2].addEntry(new LivestockEntry("Big Android", R.drawable.big_droid));


//    	mCategories[3].addEntry(new LivestockEntry("Cupcake", R.drawable.cupcake));
//    	mCategories[3].addEntry(new LivestockEntry("Donut", R.drawable.donut));
//    	mCategories[3].addEntry(new LivestockEntry("Eclair", R.drawable.eclair));
//    	mCategories[3].addEntry(new LivestockEntry("Froyo", R.drawable.froyo));
    }

    public static int getCategoryCount() {
        return mCategories.length;
    }

    public static LivestockCategory getCategory(int i) {
        return mCategories[i];
    }
    
    public static void addToCategory(int i, LivestockEntry entry) {
    	mCategories[i].addEntry(entry);
    }
}
