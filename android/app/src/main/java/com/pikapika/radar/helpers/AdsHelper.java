package com.pikapika.radar.helpers;

import android.view.View;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;
import com.pikapika.radar.utils.Debug;

/**
 * Created by flavioreyes on 8/7/16.
 */
public class AdsHelper implements MoPubInterstitial.InterstitialAdListener, MoPubView.BannerAdListener {

    MoPubView mMoPubView;

    public void setBannerView(MoPubView banner){
        mMoPubView = banner;
        mMoPubView.setBannerAdListener(this);
    }

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

    //***************************** Banner *************************

    @Override
    public void onBannerLoaded(MoPubView banner) {
        if(mMoPubView != null)
            mMoPubView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        if(mMoPubView != null)
            mMoPubView.setVisibility(View.GONE);
    }

    @Override
    public void onBannerClicked(MoPubView banner) {

    }

    @Override
    public void onBannerExpanded(MoPubView banner) {

    }

    @Override
    public void onBannerCollapsed(MoPubView banner) {

    }
}
