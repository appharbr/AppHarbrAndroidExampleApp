package com.appharbr.example.app.max;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.appharbr.example.app.databinding.ActivityMaxInterstitialBinding;
import com.appharbr.sdk.engine.AdResult;
import com.appharbr.sdk.engine.AdSdk;
import com.appharbr.sdk.engine.AdStateResult;
import com.appharbr.sdk.engine.AppHarbr;
import com.appharbr.sdk.engine.listeners.AHAnalyze;
import com.appharbr.sdk.engine.listeners.AdAnalyzedInfo;
import com.appharbr.sdk.engine.listeners.AdIncidentInfo;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;

import java.util.Arrays;

public class MaxInterstitialActivity extends AppCompatActivity {

    private ActivityMaxInterstitialBinding binding;
    private MaxInterstitialAd maxInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMaxInterstitialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //	**** (1) ****
        //Initialize AppLovinSdk
        AppLovinSdkInitializationConfiguration initConfig = AppLovinSdkInitializationConfiguration.builder(
                "YOUR_API_KEY",
                this
        ).setMediationProvider(AppLovinMediationProvider.MAX).build();

        AppLovinSdk.getInstance(this).initialize(initConfig, (sdkConfig) -> {
            Log.d("KotlinSample", "MAX mediation initialized successfully -> [" + sdkConfig + ']');
            loadBannerAd();
        });
    }

    private void loadBannerAd() {
        //	**** (2) ****
        //Initialize max interstitial Ad
        maxInterstitialAd = new MaxInterstitialAd("YOUR_AD_UNIT_ID", this);

        //	**** (3) ****
        // The publisher will initiate once the listener wrapper and will use it when load the Max interstitial ad.
        MaxAdListener ahWrapperListener = AppHarbr.addInterstitial(AdSdk.MAX,
                maxInterstitialAd,
                maxAdListener,
                getLifecycle(),
                ahListener);

        //	**** (4) ****
        //Set ahWrapperListener and load Ad
        maxInterstitialAd.setListener(ahWrapperListener);
        maxInterstitialAd.loadAd();
    }

    private void checkAd() {
        //	**** (4) ****
        //Check whether Ad was blocked or not
        final AdResult interstitialResult = AppHarbr.getInterstitialResult(maxInterstitialAd);
        if (interstitialResult.getAdStateResult() != AdStateResult.BLOCKED) {
            Log.d(
                    "LOG",
                    "**************************** AppHarbr Permit to Display Max Interstitial ****************************"
            );

            if (maxInterstitialAd.isReady()) {
                maxInterstitialAd.showAd(this);
            }
        } else {
            Log.d("LOG", "**************************** AppHarbr Blocked Max Interstitial ****************************");
            // You may call to reload Max interstitial
        }
    }

    private final MaxAdListener maxAdListener = new MaxAdListener() {
        @Override
        public void onAdLoaded(@NonNull MaxAd ad) {
            Log.d("LOG", "Max - onAdLoaded");
            binding.progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onAdDisplayed(@NonNull MaxAd ad) {
            Log.d("LOG", "Max - onAdDisplayed");
        }

        @Override
        public void onAdHidden(@NonNull MaxAd ad) {
            Log.d("LOG", "Max - onAdHidden");
            // You may load new add
        }

        @Override
        public void onAdClicked(@NonNull MaxAd ad) {
            Log.d("LOG", "Max - onAdClicked");
        }

        @Override
        public void onAdLoadFailed(@NonNull String adUnitId, @NonNull MaxError error) {
            Log.d("LOG", "Max - onAdLoadFailed");
        }

        @Override
        public void onAdDisplayFailed(@NonNull MaxAd ad, @NonNull MaxError error) {
            Log.d("LOG", "Max - onAdDisplayFailed");
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

            checkAd();
        }
    };
}