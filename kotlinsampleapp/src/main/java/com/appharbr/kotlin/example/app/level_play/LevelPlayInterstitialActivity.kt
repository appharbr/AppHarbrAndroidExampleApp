package com.appharbr.kotlin.example.app.level_play

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
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.integration.IntegrationHelper
import com.unity3d.mediation.LevelPlay
import com.unity3d.mediation.LevelPlayAdError
import com.unity3d.mediation.LevelPlayAdInfo
import com.unity3d.mediation.LevelPlayConfiguration
import com.unity3d.mediation.LevelPlayInitError
import com.unity3d.mediation.LevelPlayInitListener
import com.unity3d.mediation.LevelPlayInitRequest
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAd
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAdListener

class LevelPlayInterstitialActivity : ComponentActivity() {
    private var interstitialAd: LevelPlayInterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initIronSourceSDK()
        setContent {
            AppHarbrExampleAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background,
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = stringResource(R.string.levelplay_interstitial_screen))
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    // **** (1) ****
    private fun initIronSourceSDK() {
        IntegrationHelper.validateIntegration(applicationContext)

        IronSource.addImpressionDataListener { impData ->
            Log.d(
                "LOG",
                "LevelPlay onImpressionSuccess $impData"
            )
        }

        val legacyAdFormats =
            listOf(LevelPlay.AdFormat.REWARDED) //We need this if we want to work also with legacy code
        val initRequest = LevelPlayInitRequest.Builder("YOUR_API_KEY")
            //.withLegacyAdFormats(legacyAdFormats)
            .build()

        Log.d("LOG", "init ironSource SDK with appKey: YOUR_API_KEY")
        LevelPlay.init(
            this, initRequest,
            object : LevelPlayInitListener {
                override fun onInitFailed(error: LevelPlayInitError) {
                    Log.d(
                        "LOG",
                        "IronSource onInitFailed: ${error.errorMessage} -> errorCode[${error.errorCode}]"
                    )
                }

                override fun onInitSuccess(configuration: LevelPlayConfiguration) {
                    Log.d("LOG", "IronSource onInitSuccess: $configuration")
                    setupAndLoadAdd()
                }
            }
        )
    }

    private fun setupAndLoadAdd() {
        // **** (2) ****
        //Initialize leveplay interstitial ad
        interstitialAd = LevelPlayInterstitialAd("YOUR_AD_UNIT_ID")

        //	**** (3) ****
        // The publisher will initiate once the listener wrapper and will use it when load the levelplay interstitial ad.
        val ahWrapperListener = interstitialAd?.let {
            AppHarbr.addInterstitial<LevelPlayInterstitialAdListener>(
                AdSdk.LEVELPLAY,
                it,
                levelPlayListener,
                lifecycle,
                ahListener
            )
        }

        // **** (4) ****
        // Set ahWrapperListener and load ad
        interstitialAd?.setListener(ahWrapperListener)
        interstitialAd?.loadAd()
    }

    fun showAd() {
        val stateResult = AppHarbr.getInterstitialResult(interstitialAd)
        Log.w(
            "LOG",
            this@LevelPlayInterstitialActivity.toString() + "-> state[${stateResult.adStateResult}] ->\n"
        )

        //	**** (5) ****
        //Check whether Ad was blocked or not
        if (interstitialAd?.isAdReady() == true && stateResult.adStateResult != AdStateResult.BLOCKED) {
            Log.d("LOG", "showAd for interstitial")
            interstitialAd?.showAd(this)
        } else {
            Log.e(
                "LOG",
                "Ad is not ready yet isReadyState[${interstitialAd?.isAdReady()}], or was blocked by AppHarbr SDK"
            )
        }
    }

    private var ahListener = object : AHAnalyze {
        override fun onAdBlocked(incidentInfo: AdIncidentInfo?) {
            Log.d(
                "LOG",
                "AppHarbr - onAdBlocked for: ${incidentInfo?.unitId}, reason: " + incidentInfo?.blockReasons.contentToString()
            )
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
            showAd()
        }
    }

    private val levelPlayListener = object : LevelPlayInterstitialAdListener {
        override fun onAdDisplayed(adInfo: LevelPlayAdInfo) {
            Log.d("LOG", "LevelPlay -> onAdDisplayed -> $adInfo")
        }

        override fun onAdLoadFailed(error: LevelPlayAdError) {
            Log.d("LOG", "LevelPlay -> onAdLoadFailed -> $error")
        }

        override fun onAdDisplayFailed(error: LevelPlayAdError, adInfo: LevelPlayAdInfo) {
            super.onAdDisplayFailed(error, adInfo)
            Log.e("LOG", "LevelPlay -> onAdDisplayFailed -> $error")
        }

        override fun onAdLoaded(adInfo: LevelPlayAdInfo) {
            Log.d("LOG", "LevelPlay -> onAdLoaded -> $adInfo")
        }

        override fun onAdClicked(adInfo: LevelPlayAdInfo) {
            super.onAdClicked(adInfo)
            Log.d("LOG", "LevelPlay -> onAdClicked -> $adInfo")
        }

        override fun onAdInfoChanged(adInfo: LevelPlayAdInfo) {
            super.onAdInfoChanged(adInfo)
            Log.d("LOG", "LevelPlay -> onAdInfoChanged -> $adInfo")
        }

        override fun onAdClosed(adInfo: LevelPlayAdInfo) {
            super.onAdClosed(adInfo)
            Log.d("LOG", "LevelPlay -> onAdClosed -> $adInfo")
        }

    }
}
