package com.pikapika.radar.helpers;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.pikapika.radar.utils.Debug;

/**
 * Created by flavioreyes on 8/7/16.
 */
public class AdsHelper implements MoPubInterstitial.InterstitialAdListener {
    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        Debug.Log("onInterstitialLoaded");
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        Debug.Log("onInterstitialFailed, error: "+errorCode.toString());
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {

    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {

    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {

    }
}
