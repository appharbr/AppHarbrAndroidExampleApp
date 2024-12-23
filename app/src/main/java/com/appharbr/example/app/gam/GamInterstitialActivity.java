package com.appharbr.example.app.gam;

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
import com.appharbr.sdk.engine.mediators.gam.interstitial.AHGamInterstitialAd;
import com.appharbr.example.app.R;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import java.util.Arrays;

public class GamInterstitialActivity extends AppCompatActivity {

    private Button btnDisplay;
    private AHGamInterstitialAd ahGamInterstitialAd = new AHGamInterstitialAd();
    private AdManagerInterstitialAdLoadCallback geWrapperListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gam_interstitial_layout);

        btnDisplay = findViewById(R.id.gam_interstitial_display);
        setDisplayClick();

        // The publisher will initiate once the listener wrapper and will use it when load the GAM interstitial ad.
        geWrapperListener = AppHarbr.addInterstitial(AdSdk.GAM,
                                                    ahGamInterstitialAd,
                                                    adManagerInterstitialAdLoadCallback,
                                                    ahListener);

        // The publisher load the GAM (Google Ad Manager) Interstitial
        requestAd();
    }

    private void requestAd() {
        btnDisplay.setEnabled(false);
        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        AdManagerInterstitialAd.load(this,
                getApplicationContext().getResources().getString(R.string.gam_interstitial_ad_unit_id),
                adRequest,
                geWrapperListener);//AppHarbr wrapper listener
    }

    private void setDisplayClick() {
        btnDisplay.setOnClickListener((view) -> {
            // The publisher display the GAM (Google Ad Manager) Interstitial
            if (isDestroyed()) {
                return;
            }

            setFullScreenCallBack();
            showAd();
        });
    }

    private void showAd() {
        if (ahGamInterstitialAd.getGamInterstitialAd() != null) {
            final AdResult interstitialResult = AppHarbr.getInterstitialResult(ahGamInterstitialAd);
            if (interstitialResult.getAdStateResult() != AdStateResult.BLOCKED) {
                Log.d("LOG", "**************************** AppHarbr Permit to Display GAM Interstitial ****************************");
                ahGamInterstitialAd.getGamInterstitialAd().show(this);
            }
            else {
                Log.d("LOG", "**************************** AppHarbr Blocked GAM Interstitial ****************************");
                // You may call to reload interstitial
            }
        }
        else {
            Log.d("TAG", "The GAM interstitial wasn't loaded yet.");
        }
    }

    private void setFullScreenCallBack() {
        if (ahGamInterstitialAd.getGamInterstitialAd() != null) {
            ahGamInterstitialAd.getGamInterstitialAd().setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    ahGamInterstitialAd.setInterstitialAd(null);
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

        AppHarbr.removeInterstitial(ahGamInterstitialAd);
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

    private final AdManagerInterstitialAdLoadCallback adManagerInterstitialAdLoadCallback =
            new AdManagerInterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull AdManagerInterstitialAd adManagerInterstitialAd) {
                    Log.d("LOG", "onAdLoaded");
                    super.onAdLoaded(adManagerInterstitialAd);
                    btnDisplay.setEnabled(true);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.d("LOG", "onAdFailedToLoad: " + loadAdError.getMessage());
                    super.onAdFailedToLoad(loadAdError);
                }
            };

}