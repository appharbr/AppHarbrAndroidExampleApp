package com.appharbr.example.app.level_play;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.appharbr.example.app.databinding.ActivityLevelPlayInterstitialBinding;
import com.appharbr.sdk.engine.AdSdk;
import com.appharbr.sdk.engine.AppHarbr;
import com.appharbr.sdk.engine.listeners.AHAnalyze;
import com.appharbr.sdk.engine.listeners.AdAnalyzedInfo;
import com.appharbr.sdk.engine.listeners.AdIncidentInfo;
import com.appharbr.sdk.engine.mediators.ironsource.interstitial.IronsourceInterstitialAd;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.LevelPlayInterstitialListener;
import java.util.Arrays;

public class LevelPlayInterstitialActivity extends AppCompatActivity {

    private ActivityLevelPlayInterstitialBinding binding;
    private IronsourceInterstitialAd ahInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLevelPlayInterstitialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //The beginning
        initAppHarbr();
        initIronSource();

        initClickListeners();
    }

    private void initClickListeners() {
        binding.admobInterstitialDisplay.setOnClickListener((l) -> {
            IronSource.showInterstitial();
        });
    }

    private void initAppHarbr() {
        //	**** (1) ****
        //Initialize Our IronSource Wrapper
        ahInterstitial = new IronsourceInterstitialAd("");
    }

    private void initIronSource() {
        //	**** (2) ****
        //Initialize IronSource & Set Listener
        IronSource.setLevelPlayInterstitialListener(integrateAppHarbr());
        IronSource.shouldTrackNetworkState(this, true);
        IronSource.setAdaptersDebug(true);
        IronSource.setMetaData("is_test_suite", "enable");
        IronSource.init(this, "YOUR_UNIT_ID", IronSource.AD_UNIT.INTERSTITIAL);
        //After this, you can load ad

        //	**** (4) ****
        //Load IronSource Ad
        loadAd();
    }

    private LevelPlayInterstitialListener integrateAppHarbr() {
        //	**** (3) ****
        //Start communication between AppHarbr and IronSource
        return AppHarbr.addInterstitial(
                AdSdk.LEVELPLAY,
                ahInterstitial,
                levelPlayInterstitialListener,
                ahListener
        );
    }

    private void loadAd() {
        binding.admobInterstitialDisplay.setEnabled(false);
        IronSource.loadInterstitial();
        Log.d("LOG", "Loading Level Play Ad");
    }

    private final LevelPlayInterstitialListener levelPlayInterstitialListener = new LevelPlayInterstitialListener() {
        @Override
        public void onAdReady(AdInfo adInfo) {
            Log.d("LOG", "Level Play -> onReady -> " + adInfo);
        }

        @Override
        public void onAdLoadFailed(IronSourceError ironSourceError) {
            Log.d("LOG", "Level Play -> onAdLoadFailed -> " + ironSourceError.getErrorCode() + '-' + ironSourceError.getErrorMessage());
        }

        @Override
        public void onAdOpened(AdInfo adInfo) {
            Log.d("LOG", "Level Play -> onAdOpened -> " + adInfo);
        }

        @Override
        public void onAdShowSucceeded(AdInfo adInfo) {
            Log.d("LOG", "Level Play -> onAdShowSucceeded -> " + adInfo);
        }

        @Override
        public void onAdShowFailed(IronSourceError ironSourceError, AdInfo adInfo) {
            Log.d("LOG", "Level Play -> onAdShowFailed -> " + ironSourceError.getErrorCode() + '-' + ironSourceError.getErrorMessage());
        }

        @Override
        public void onAdClicked(AdInfo adInfo) {
            Log.d("LOG", "Level Play -> onAdClicked -> " + adInfo);
        }

        @Override
        public void onAdClosed(AdInfo adInfo) {
            Log.d("LOG", "Level Play -> onAdClosed -> " + adInfo);
        }
    };


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

            //Once your ad is ready, you can show it to the user anytime you wish
            binding.admobInterstitialDisplay.setEnabled(true);
        }
    };
}