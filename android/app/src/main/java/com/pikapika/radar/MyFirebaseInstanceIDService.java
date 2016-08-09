package com.pikapika.radar;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.pikapika.radar.utils.Debug;

/**
 * Created by flavioreyes on 8/8/16.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Debug.Log( "Refreshed token: " + refreshedToken);
        //sendRegistrationToServer(refreshedToken);
    }
}

