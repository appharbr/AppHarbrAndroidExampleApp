package com.appharbr.example.app.gam;


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
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import java.util.Arrays;

public class GamBannerActivity extends AppCompatActivity {

    private AdManagerAdView adManagerAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gam_banner_layout);

//      **** (1) ****
//      Get the AdView
        adManagerAdView = findViewById(R.id.gam_banner_ad_view);

//      **** (2) ****
//      Set your ad listener for Google Ad Manager events
        adManagerAdView.setAdListener(mAdListener);

//      **** (3) ****
//      Add the adView instance to GeoEdge for Monitoring
        AppHarbr.addBannerView(AdSdk.GAM, adManagerAdView, ahListener);

//      **** (4) ****
//      Request for the Ads
        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        adManagerAdView.loadAd(adRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//      **** (5) ****
//      Remove the view on Destroy
        AppHarbr.removeBannerView(adManagerAdView);
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
            Log.d("LOG", "GAM - onAdImpression");
        }

        @Override
        public void onAdLoaded() {
            Log.d("LOG", "GAM - onAdLoaded");
        }

        @Override
        public void onAdFailedToLoad(LoadAdError adError) {
            Log.d("LOG", "GAM - onAdFailedToLoad: " + adError.getMessage());
        }

        @Override
        public void onAdOpened() {
            Log.d("LOG", "GAM - onAdOpened");
        }

        @Override
        public void onAdClicked() {
            Log.d("LOG", "GAM - onAdClicked");
        }

        @Override
        public void onAdClosed() {
            Log.d("LOG", "GAM - onAdClosed");
        }

    };
}
