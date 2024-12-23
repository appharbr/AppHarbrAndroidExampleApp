package com.appharbr.example.app.max;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.appharbr.example.app.databinding.ActivityMaxBannerBinding;
import com.appharbr.sdk.engine.AdSdk;
import com.appharbr.sdk.engine.AppHarbr;
import com.appharbr.sdk.engine.listeners.AHAnalyze;
import com.appharbr.sdk.engine.listeners.AdAnalyzedInfo;
import com.appharbr.sdk.engine.listeners.AdIncidentInfo;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;

import java.util.Arrays;

public class MaxBannerActivity extends AppCompatActivity {

    private ActivityMaxBannerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMaxBannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //	**** (1) ****
        //Initialize AppLovinSdk
        AppLovinSdkInitializationConfiguration initConfig = AppLovinSdkInitializationConfiguration.builder(
                "YOUR_API_KEY",
                this
        ).setMediationProvider(AppLovinMediationProvider.MAX).build();

        AppLovinSdk.getInstance(this).initialize(initConfig, (sdkConfig) -> {
            Log.d("KotlinSample", "MAX mediation initialized successfully -> [" + sdkConfig + ']');
            loadBannerAd(binding);
        });


    }

    private void loadBannerAd(ActivityMaxBannerBinding binding) {
        //	**** (2) ****
        //Set your ad listener for Max events
        binding.maxAdViewBanner.setListener(mAdListener);

        //      **** (3) ****
        //Add Max's banner adView instance for Monitoring
        AppHarbr.addBannerView(AdSdk.MAX, binding.maxAdViewBanner, getLifecycle(), ahListener);

        //      **** (4) ****
        //Star loading Ads
        binding.maxAdViewBanner.loadAd();

        //Optionally set auto refresh of ads
        binding.maxAdViewBanner.startAutoRefresh();
    }

    private final MaxAdViewAdListener mAdListener = new MaxAdViewAdListener() {

        @Override
        public void onAdExpanded(@NonNull MaxAd ad) {
            Log.d("LOG", "Max - onAdExpanded");
        }

        @Override
        public void onAdCollapsed(@NonNull MaxAd ad) {
            Log.d("LOG", "Max - onAdCollapsed");
        }

        @Override
        public void onAdLoaded(@NonNull MaxAd ad) {
            Log.d("LOG", "Max - onAdLoaded");
        }

        @Override
        public void onAdDisplayed(@NonNull MaxAd ad) {
            Log.d("LOG", "Max - onAdDisplayed");
        }

        @Override
        public void onAdHidden(@NonNull MaxAd ad) {
            Log.d("LOG", "Max - onAdHidden");
        }

        @Override
        public void onAdClicked(@NonNull MaxAd ad) {
            Log.d("LOG", "Max - onAdClicked");
        }

        @Override
        public void onAdLoadFailed(@NonNull String adUnitId, MaxError error) {
            Log.d("LOG", "Max - onAdLoadFailed: " + error.getMessage() + " => " + error.getCode());
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

}