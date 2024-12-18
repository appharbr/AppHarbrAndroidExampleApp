package com.appharbr.example.app.max;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.appharbr.example.app.databinding.ActivityMaxNativeAdBinding;
import com.appharbr.sdk.engine.AdResult;
import com.appharbr.sdk.engine.AdSdk;
import com.appharbr.sdk.engine.AdStateResult;
import com.appharbr.sdk.engine.AppHarbr;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;

public class MaxNativeAdActivity extends AppCompatActivity {

    private ActivityMaxNativeAdBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMaxNativeAdBinding.inflate(getLayoutInflater());
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
        //Create Max native ad loader
        final MaxNativeAdLoader maxNativeAdLoader = new MaxNativeAdLoader("YOUR_AD_UNIT_ID", this);

        //	**** (3) ****
        //Set listener to get results
        maxNativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {

            @Override
            public void onNativeAdLoaded(@Nullable MaxNativeAdView maxNativeAdView, @NonNull MaxAd maxAd) {
                Log.d("LOG", "Max - onNativeAdLoaded");

                //hiding progressBar cause ad is already loaded
                binding.progressBar.setVisibility(View.GONE);

                //	**** (4) ****
                //Check loaded Max native ad from AppHarbr if it needs to be blocked
                AdResult adResult = AppHarbr.shouldBlockNativeAd(AdSdk.MAX, maxAd, "YOUR_AD_UNIT_ID");
                if (adResult.getAdStateResult() != AdStateResult.BLOCKED) {
                    Log.d("LOG", "**************************** AppHarbr Permit to Display Max Native Ad ****************************");
                    binding.mainLayout.addView(maxNativeAdView);
                } else {
                    Log.d("LOG", "**************************** AppHarbr Blocked Max Native Ad ****************************");
                    // ***** Publisher may reload ad *****
                }
            }

            @Override
            public void onNativeAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
                Log.d("LOG", "Max - onNativeAdLoadFailed");
                // ***** Publisher may reload ad *****
            }

        });

        //	**** (5) ****
        //And finally load Max native Ad
        maxNativeAdLoader.loadAd();
    }

}