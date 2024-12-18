package com.appharbr.example.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.appharbr.sdk.configuration.AHSdkConfiguration;
import com.appharbr.sdk.configuration.AHSdkDebug;
import com.appharbr.sdk.engine.AdSdk;
import com.appharbr.sdk.engine.AppHarbr;
import com.appharbr.sdk.engine.InitializationFailureReason;
import com.appharbr.sdk.engine.listeners.OnAppHarbrInitializationCompleteListener;

import java.util.HashSet;
import java.util.Set;

public class MainApplication extends Application {

    private final String API_KEY = "YOUR_API_KEY";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initAppHarbr();
    }

    /**
     * Publishers create AHSdkConfiguration for initializing AppHarbr.
     * AHSdkConfiguration offers various internal features for customization.
     * We've provided a few examples in this method.
     * You can combine these examples into a single AHSdkConfiguration tailored to your specific requirements.
     */
    private AHSdkConfiguration getAppHarbrRelevantConfiguration() {
        // Regular AppHarbr Configuration
        AHSdkConfiguration ahSdkNormalConfiguration = new AHSdkConfiguration.Builder(API_KEY).build();

        // Alternatively AppHarbr Configuration with BlockAll testing
        AHSdkConfiguration ahSdkBlockAllTestingConfiguration = new AHSdkConfiguration.Builder(API_KEY)
                .withDebugConfig(new AHSdkDebug(true).withBlockAll(true))
                .build();

        // Alternatively AppHarbr Configuration with Targeted Ad Networks
        AHSdkConfiguration ahSdkTargetedAdNetworksConfiguration = new AHSdkConfiguration.Builder(API_KEY)
                .withTargetedNetworks(new AdSdk[] {AdSdk.ADMOB, AdSdk.CHARTBOOST})
                .build();

        // Alternatively AppHarbr Configuration with Interstitial time limit
        AHSdkConfiguration ahSdkInterstitialTimeLimitConfiguration = new AHSdkConfiguration.Builder(API_KEY)
                .withInterstitialAdTimeLimit(30) // 30 seconds
                .build();

        // Alternatively AppHarbr Configuration with Mute Autoplay Sound
        AHSdkConfiguration ahSdkMuteAutoPlaySoundConfiguration = new AHSdkConfiguration.Builder(API_KEY)
                .withMuteAd(true)
                .build();

        // Alternatively AppHarbr Configuration with ignoring GAM Campaigns
        Set houseCampaignCreatives = new HashSet<>();
        houseCampaignCreatives.add("123456");
        houseCampaignCreatives.add("987654");
        AHSdkConfiguration ahSdkIgnoreGAMCampaignsConfiguration = new AHSdkConfiguration.Builder(API_KEY)
                .withIgnoreHouseCampaignCreativeIds(houseCampaignCreatives)
                .build();

        return ahSdkNormalConfiguration;
    }

    private void initAppHarbr() {
        AppHarbr.initialize(getApplicationContext(),
                getAppHarbrRelevantConfiguration(),
                new OnAppHarbrInitializationCompleteListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("LOG", "AppHarbr SDK Initialized Successfully");
                    }

                    @Override
                    public void onFailure(@NonNull InitializationFailureReason reason) {
                        Log.e("LOG", "AppHarbr SDK Initialization Failed: " + reason.getReadableHumanReason());
                    }
                });
    }
}
