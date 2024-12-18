package com.appharbr.kotlin.example.app

import android.app.Application
import android.util.Log
import com.appharbr.sdk.configuration.AHSdkConfiguration
import com.appharbr.sdk.configuration.AHSdkDebug
import com.appharbr.sdk.engine.AdSdk
import com.appharbr.sdk.engine.AppHarbr
import com.appharbr.sdk.engine.InitializationFailureReason
import com.appharbr.sdk.engine.listeners.OnAppHarbrInitializationCompleteListener
import org.prebid.mobile.Host
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.api.data.InitializationStatus

class MainApplication : Application() {

    private val API_KEY = "YOUR_API_KEY"

    override fun onCreate() {
        super.onCreate()
        initPrebid()
        initAppHarbr()
    }

    /**
     * Publishers create AHSdkConfiguration for initializing AppHarbr.
     * AHSdkConfiguration offers various internal features for customization.
     * We've provided a few examples in this method.
     * You can combine these examples into a single AHSdkConfiguration tailored to your specific requirements.
     */
    private fun getAppHarbrRelevantConfiguration(): AHSdkConfiguration? {
        // Regular AppHarbr Configuration
        val ahSdkNormalConfiguration = AHSdkConfiguration.Builder(API_KEY).build()

        // Alternatively AppHarbr Configuration with BlockAll testing
        val ahSdkBlockAllTestingConfiguration = AHSdkConfiguration.Builder(API_KEY)
            .withDebugConfig(AHSdkDebug(true).withBlockAll(true))
            .build()

        // Alternatively AppHarbr Configuration with Targeted Ad Networks
        val ahSdkTargetedAdNetworksConfiguration = AHSdkConfiguration.Builder(API_KEY)
            .withTargetedNetworks(arrayOf(AdSdk.ADMOB, AdSdk.CHARTBOOST))
            .build()

        // Alternatively AppHarbr Configuration with Interstitial time limit
        val ahSdkInterstitialTimeLimitConfiguration = AHSdkConfiguration.Builder(API_KEY)
            .withInterstitialAdTimeLimit(30) // 30 seconds
            .build()

        // Alternatively AppHarbr Configuration with Mute Autoplay Sound
        val ahSdkMuteAutoPlaySoundConfiguration = AHSdkConfiguration.Builder(API_KEY)
            .withMuteAd(true)
            .build()

        // Alternatively AppHarbr Configuration with ignoring GAM Campaigns
        val ahSdkIgnoreGAMCampaignsConfiguration = AHSdkConfiguration.Builder(API_KEY)
            .withIgnoreHouseCampaignCreativeIds(hashSetOf("123456", "987654"))
            .build()

        return ahSdkNormalConfiguration

    }

    private fun initAppHarbr() {
        // Regular AppHarbr Configuration
        val ahSdkNormalConfiguration: AHSdkConfiguration = AHSdkConfiguration
            .Builder(API_KEY)
            .withInterstitialAdTimeLimit(10)
            .withRewardAdTimeLimit(20)
            .withDebugConfig(AHSdkDebug(true, false))
            .withMuteAd(true)
            .build()

        // Alternatively AppHarbr Configuration with BlockAll testing
        val ahSdkBlockAllTestingConfiguration: AHSdkConfiguration =
            AHSdkConfiguration.Builder(API_KEY)
                .withDebugConfig(AHSdkDebug(true).withBlockAll(true))
                .build()

        // Alternatively AppHarbr Configuration with Targeted Ad Networks

        // Alternatively AppHarbr Configuration with Targeted Ad Networks
        val ahSdkTargetedAdNetworksConfiguration = AHSdkConfiguration.Builder(API_KEY)
            .withTargetedNetworks(arrayOf(AdSdk.ADMOB, AdSdk.CHARTBOOST))
            .build()


        AppHarbr.initialize(
            applicationContext,
            ahSdkNormalConfiguration,
            object : OnAppHarbrInitializationCompleteListener {

                override fun onSuccess() {
                    Log.d("LOG", "AppHarbr SDK Initialized Successfully")
                }

                override fun onFailure(reason: InitializationFailureReason) {
                    Log.e(
                        "LOG",
                        "AppHarbr SDK Initialization Failed: " + reason.readableHumanReason
                    )
                }
            })
    }

    private fun initPrebid() {
        PrebidMobile.setPrebidServerAccountId("0689a263-318d-448b-a3d4-b02e8a709d9d")
        PrebidMobile.setPrebidServerHost(Host.createCustomHost("https://prebid-server-test-j.prebid.org/openrtb2/auction"))
        PrebidMobile.setCustomStatusEndpoint("https://prebid-server-test-j.prebid.org/status")

        PrebidMobile.initializeSdk(applicationContext) { status ->
            when (status) {
                InitializationStatus.SUCCEEDED -> Log.d("LOG","Prebid SDK initialized successfully")
                InitializationStatus.SERVER_STATUS_WARNING -> Log.w("LOG","Prebid Server status checking failed: $status\n${status.description}")
                else -> Log.e("LOG","SDK initialization error: $status\n${status.description}")
            }
        }
        PrebidMobile.setShareGeoLocation(true)
    }

}