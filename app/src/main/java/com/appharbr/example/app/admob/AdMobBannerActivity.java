package com.appharbr.example.app.admob;


import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.appharbr.sdk.engine.AdSdk;
import com.appharbr.sdk.engine.AppHarbr;
import com.appharbr.sdk.engine.listeners.AHAnalyze;
import com.appharbr.example.app.R;
import com.appharbr.sdk.engine.listeners.AdAnalyzedInfo;
import com.appharbr.sdk.engine.listeners.AdIncidentInfo;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

import java.util.Arrays;

public class AdMobBannerActivity extends AppCompatActivity {

    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admob_banner_layout);

//      **** (1) ****
//      Get the AdView
        adView = findViewById(R.id.admob_banner_ad_view);

//      **** (2) ****
//      Set your ad listener for AdMob events
        adView.setAdListener(mAdListener);

//      **** (3) ****
//      Add the adView instance to AdMob for Monitoring
        AppHarbr.addBannerView(AdSdk.ADMOB, adView, ahListener);

//      **** (4) ****
//      Request for the Ads
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//      **** (5) ****
//      Remove the view on Destroy
        AppHarbr.removeBannerView(adView);
    }

    private final AHAnalyze ahListener = new AHAnalyze() {
        @Override
        public void onAdBlocked(@Nullable AdIncidentInfo adIncidentInfo) {
            Log.d("LOG", "AppHarbr - onAdBlocked for: "
                    + (adIncidentInfo != null && adIncidentInfo.getUnitId() != null ? adIncidentInfo.getUnitId() : "null")
                    + ", reason: "
                    + (adIncidentInfo != null && adIncidentInfo.getBlockReasons() != null ? Arrays.toString(adIncidentInfo.getBlockReasons()) : "null"));
        }

        @Override
        public void onAdIncident(@Nullable AdIncidentInfo adIncidentInfo) {
            Log.d("LOG", "AppHarbr - onAdIncident for: "
                    + (adIncidentInfo != null && adIncidentInfo.getUnitId() != null ? adIncidentInfo.getUnitId() : "null")
                    + ", reason: "
                    + (adIncidentInfo != null && adIncidentInfo.getReportReasons() != null ? Arrays.toString(adIncidentInfo.getReportReasons()) : "null"));
        }

        @Override
        public void onAdAnalyzed(@Nullable AdAnalyzedInfo adAnalyzedInfo) {
            Log.d("LOG", "AppHarbr - onAdAnalyzed for: "
                    + (adAnalyzedInfo != null && adAnalyzedInfo.getUnitId() != null ? adAnalyzedInfo.getUnitId() : "null")
                    + ", result: "
                    + (adAnalyzedInfo != null && adAnalyzedInfo.getAnalyzedResult() != null ? adAnalyzedInfo.getAnalyzedResult() : "null"));

        }
    };

    private final AdListener mAdListener = new AdListener() {

        @Override
        public void onAdImpression() {
            super.onAdImpression();
            Log.d("LOG", "AdMob - onAdImpression");
        }

        @Override
        public void onAdLoaded() {
            Log.d("LOG", "AdMob - onAdLoaded");
        }

        @Override
        public void onAdFailedToLoad(LoadAdError adError) {
            Log.d("LOG", "AdMob - onAdFailedToLoad: " + adError.getMessage());
        }

        @Override
        public void onAdOpened() {
            Log.d("LOG", "AdMob - onAdOpened");
        }

        @Override
        public void onAdClicked() {
            Log.d("LOG", "AdMob - onAdClicked");
        }

        @Override
        public void onAdClosed() {
            Log.d("LOG", "AdMob - onAdClosed");
        }

    };
}
