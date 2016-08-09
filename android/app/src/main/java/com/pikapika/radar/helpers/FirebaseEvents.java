package com.pikapika.radar.helpers;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by flavioreyes on 8/8/16.
 */
public class FirebaseEvents {
    public static final String SCAN_CLICK_EVENT_ID = "1001";
    public static final String SCAN_CLICK_EVENT = "Scan Clicked";

    public void logScanEvent(FirebaseAnalytics firebaseAnalytics){
        /*Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);*/
    }
}
