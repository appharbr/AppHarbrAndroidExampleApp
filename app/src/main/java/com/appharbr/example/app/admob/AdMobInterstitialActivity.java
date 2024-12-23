package com.appharbr.example.app.admob;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.appharbr.sdk.engine.AdResult;
import com.appharbr.sdk.engine.AdSdk;
import com.appharbr.sdk.engine.AdStateResult;
import com.appharbr.sdk.engine.AppHarbr;
import com.appharbr.sdk.engine.listeners.AHAnalyze;
import com.appharbr.sdk.engine.listeners.AdAnalyzedInfo;
import com.appharbr.sdk.engine.listeners.AdIncidentInfo;
import com.appharbr.sdk.engine.mediators.admob.interstitial.AHAdMobInterstitialAd;
import com.appharbr.example.app.R;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Arrays;

public class AdMobInterstitialActivity extends AppCompatActivity {

    private Button btnDisplay;
    private AHAdMobInterstitialAd ahAdMobInterstitialAd = new AHAdMobInterstitialAd();
    private InterstitialAdLoadCallback ahWrapperListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admob_interstitial_layout);

        btnDisplay = findViewById(R.id.admob_interstitial_display);
        setDisplayClick();

        // The publisher will initiate once the listener wrapper and will use it when load the AdMob interstitial ad.
        ahWrapperListener = AppHarbr.addInterstitial(AdSdk.ADMOB,
                ahAdMobInterstitialAd,
                interstitialAdLoadCallback,
                ahListener);

        // The publisher load the AdMob Interstitial
        requestAd();
    }

    private void requestAd() {
        btnDisplay.setEnabled(false);
        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        AdManagerInterstitialAd.load(this,
                getApplicationContext().getResources().getString(R.string.admob_interstitial_ad_unit_id),
                adRequest,
                ahWrapperListener);//AppHarbr wrapper listener
    }

    private void setDisplayClick() {
        btnDisplay.setOnClickListener((view) -> {
            // The publisher display the AdMob Interstitial
            if (isDestroyed()) {
                return;
            }

            setFullScreenCallBack();
            showAd();
        });
    }

    private void showAd() {
        if (ahAdMobInterstitialAd.getAdMobInterstitialAd() != null) {
            final AdResult interstitialResult = AppHarbr.getInterstitialResult(ahAdMobInterstitialAd);
            if (interstitialResult.adStateResult != AdStateResult.BLOCKED) {
                Log.d("LOG", "**************************** AppHarbr Permit to Display AdMob Interstitial ****************************");
                ahAdMobInterstitialAd.getAdMobInterstitialAd().show(this);
            } else {
                Log.d("LOG", "**************************** AppHarbr Blocked AdMob Interstitial ****************************");
                // You may call to reload AdMob interstitial
            }
        } else {
            Log.d("TAG", "The AdMob interstitial wasn't loaded yet.");
        }
    }

    private void setFullScreenCallBack() {
        if (ahAdMobInterstitialAd.getAdMobInterstitialAd() != null) {
            ahAdMobInterstitialAd.getAdMobInterstitialAd().setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    ahAdMobInterstitialAd.setInterstitialAd(null);
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.d(
                            "LOG",
                            "**************************** Interstitial Add Dismissed ****************************"
                    );

                    // You may load new add
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//      Remove the view on Destroy
        AppHarbr.removeInterstitial(ahAdMobInterstitialAd);
    }

    private final AHAnalyze ahListener = new AHAnalyze() {
        @Override
        public void onAdBlocked(@Nullable AdIncidentInfo adIncidentInfo) {
            Log.d("LOG", "AppHarbr - onAdBlocked for: "
                    + (adIncidentInfo != null && adIncidentInfo.getUnitId() != null ? adIncidentInfo.getUnitId() : "null")
                    + ", reason: "
                    + (adIncidentInfo != null && adIncidentInfo.getBlockReasons() != null ? Arrays.toString(adIncidentInfo.getBlockReasons()) : "null"));

            if (adIncidentInfo != null && adIncidentInfo.getShouldLoadNewAd()) {
                // If add was blocked before being displayed, load new add
            }
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

    private final InterstitialAdLoadCallback interstitialAdLoadCallback = new InterstitialAdLoadCallback() {
        @Override
        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
            super.onAdLoaded(interstitialAd);
            Log.d("LOG", "onAdLoaded");

            btnDisplay.setEnabled(true);
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            super.onAdFailedToLoad(loadAdError);
            Log.d("LOG", "onAdFailedToLoad: " + loadAdError.getMessage());
        }
    };
}