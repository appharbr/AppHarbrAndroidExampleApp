package com.appharbr.kotlin.example.app.max

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.appharbr.kotlin.example.app.R
import com.appharbr.kotlin.example.app.ui.theme.AppHarbrExampleAppTheme
import com.appharbr.sdk.engine.AdSdk
import com.appharbr.sdk.engine.AdStateResult
import com.appharbr.sdk.engine.AppHarbr
import com.appharbr.sdk.engine.listeners.AHAnalyze
import com.appharbr.sdk.engine.listeners.AdAnalyzedInfo
import com.appharbr.sdk.engine.listeners.AdIncidentInfo
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration

class MaxRewardedActivity : ComponentActivity() {

    private lateinit var maxRewardedAd: MaxRewardedAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeApplovinSDK()

        setContent {
            AppHarbrExampleAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = stringResource(id = R.string.max_rewarded_screen))

                        CircularProgressIndicator()

                    }
                }
            }
        }
    }

    //	**** (1) ****
    // Initialize Applovin SDK
    private fun initializeApplovinSDK(){
        val initConfig = AppLovinSdkInitializationConfiguration.builder(
            "YOUR_API_KEY",
            this
        ).setMediationProvider(AppLovinMediationProvider.MAX).build()

        AppLovinSdk.getInstance(this).initialize(initConfig) { sdkConfig ->
            Log.d("KotlinSample", "MAX mediation initialized successfully -> [${sdkConfig}]")
            createAndLoadRewardedAd()
        }
    }

    private fun createAndLoadRewardedAd() {
        //	**** (2) ****
        //Initialize max Rewarded Ad
        maxRewardedAd = MaxRewardedAd.getInstance("YOUR_UNIT_ID", this)

        //	**** (3) ****
        // The publisher will initiate once the listener wrapper and will use it when load the Max rewarded ad.
        val ahWrapperListener = AppHarbr.addRewardedAd(
            AdSdk.MAX,
            maxRewardedAd,
            maxRewardedAdListener,
            lifecycle,
            ahListener
        )

        //	**** (4) ****
        //Set ahWrapperListener and load Ad
        maxRewardedAd.setListener(ahWrapperListener)
        maxRewardedAd.loadAd()
    }

    private val maxRewardedAdListener: MaxRewardedAdListener = object : MaxRewardedAdListener {
        override fun onAdLoaded(ad: MaxAd) {
            Log.d("LOG", "Max - onAdLoaded")
            if (maxRewardedAd.isReady) {
                checkAd()
            }
        }

        private fun checkAd() {
            //	**** (5) ****
            //Check whether Ad was blocked or not
            val rewardedResult = AppHarbr.getRewardedResult(maxRewardedAd)
            if (rewardedResult.adStateResult != AdStateResult.BLOCKED) {
                Log.d(
                    "LOG",
                    "**************************** AppHarbr Permit to Display Max Rewarded ****************************"
                )
                maxRewardedAd.showAd(this@MaxRewardedActivity)
            } else {
                Log.d(
                    "LOG",
                    "**************************** AppHarbr Blocked Max Rewarded ****************************"
                )
                // You may call to reload Max interstitial
            }
        }

        override fun onAdDisplayed(ad: MaxAd) {
            Log.d("LOG", "Max - onAdDisplayed")
        }

        override fun onAdHidden(ad: MaxAd) {
            Log.d("LOG", "Max - onAdHidden")
        }

        override fun onAdClicked(ad: MaxAd) {
            Log.d("LOG", "Max - onAdClicked")
        }

        override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
            Log.d("LOG", "Max - onAdLoadFailed")
        }

        override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
            Log.d("LOG", "Max - onAdDisplayFailed")
        }

        override fun onUserRewarded(ad: MaxAd, reward: MaxReward) {
            Log.d("LOG", "Max - onUserRewarded")
        }
    }

    var ahListener = object : AHAnalyze {
        override fun onAdBlocked(incidentInfo: AdIncidentInfo?) {
            Log.d(
                "LOG",
                "AppHarbr - onAdBlocked for: ${incidentInfo?.unitId}, reason: " + incidentInfo?.blockReasons.contentToString()
            )

            if (incidentInfo?.shouldLoadNewAd == true) {
                // If add was blocked before being displayed, load new add
            }
        }

        override fun onAdIncident(incidentInfo: AdIncidentInfo?) {
            Log.d(
                "LOG",
                "AppHarbr - onAdIncident for: ${incidentInfo?.unitId}, reason: " + incidentInfo?.reportReasons.contentToString()
            )
        }

        override fun onAdAnalyzed(analyzedInfo: AdAnalyzedInfo?) {
            Log.d(
                "LOG",
                "AppHarbr - onAdAnalyzed for: ${analyzedInfo?.unitId}, result: ${analyzedInfo?.analyzedResult}"
            )
        }
    }
}