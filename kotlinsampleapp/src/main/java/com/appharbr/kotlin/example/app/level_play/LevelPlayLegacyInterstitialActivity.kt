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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.appharbr.kotlin.example.app.R
import com.appharbr.kotlin.example.app.ui.theme.AppHarbrExampleAppTheme
import com.appharbr.sdk.engine.AdSdk
import com.appharbr.sdk.engine.AppHarbr
import com.appharbr.sdk.engine.listeners.AHAnalyze
import com.appharbr.sdk.engine.listeners.AdAnalyzedInfo
import com.appharbr.sdk.engine.listeners.AdIncidentInfo
import com.appharbr.sdk.engine.mediators.levelplay.interstitial.IronsourceInterstitialAd
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.LevelPlayInterstitialListener

class LevelPlayLegacyInterstitialActivity : ComponentActivity() {

    private lateinit var ahInterstitial: IronsourceInterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        androidx.compose.material.Text(text = stringResource(id = R.string.levelplay_interstitial_screen))

                        CircularProgressIndicator()
                    }
                }
            }
        }


        //The beginning
        initAppHarbr()
        initIronSource()
    }

    private fun initAppHarbr() {
        //	**** (1) ****
        //Initialize Our IronSource Wrapper
        ahInterstitial = IronsourceInterstitialAd("")
    }

    private fun initIronSource() {
        //	**** (2) ****
        //Initialize IronSource & Set Listener
        IronSource.setLevelPlayInterstitialListener( //(3)
            integrateAppHarbr()
        )
        IronSource.shouldTrackNetworkState(this, true)
        IronSource.setAdaptersDebug(true)
        IronSource.setMetaData("is_test_suite", "enable")
        IronSource.init(this, "YOUR_API_KEY", IronSource.AD_UNIT.INTERSTITIAL)

        //After this, you can load ad

        //	**** (4) ****
        //Load IronSource Ad
        loadAd()
    }

    private fun integrateAppHarbr(): LevelPlayInterstitialListener? {
        //	**** (3) ****
        //Start communication between AppHarbr and IronSource
        return AppHarbr.addInterstitial<LevelPlayInterstitialListener>(
            AdSdk.LEVELPLAY,
            ahInterstitial,
            levelPlayInterstitialListener,
            ahListener
        )
    }

    private fun loadAd() {
        IronSource.loadInterstitial()
        Log.d("LOG", "Loading Level Play Ad")
    }

    private val levelPlayInterstitialListener: LevelPlayInterstitialListener =
        object : LevelPlayInterstitialListener {
            override fun onAdReady(adInfo: AdInfo) {
                Log.d("LOG", "Level Play -> onReady -> $adInfo")
                //Once your ad is ready, you can show it to the user anytime you wish
                IronSource.showInterstitial()
            }

            override fun onAdLoadFailed(ironSourceError: IronSourceError) {
                Log.d(
                    "LOG",
                    "Level Play -> onAdLoadFailed -> ${ironSourceError.errorCode} - ${ironSourceError.errorMessage}"
                )
            }

            override fun onAdOpened(adInfo: AdInfo) {
                Log.d("LOG", "Level Play -> onAdOpened -> $adInfo")
            }

            override fun onAdShowSucceeded(adInfo: AdInfo) {
                Log.d("LOG", "Level Play -> onAdShowSucceeded -> $adInfo")
            }

            override fun onAdShowFailed(ironSourceError: IronSourceError, adInfo: AdInfo) {
                Log.d(
                    "LOG",
                    "Level Play -> onAdShowFailed -> ${ironSourceError.errorCode} - ${ironSourceError.errorMessage}"
                )
            }

            override fun onAdClicked(adInfo: AdInfo) {
                Log.d("LOG", "Level Play -> onAdClicked -> $adInfo")
            }

            override fun onAdClosed(adInfo: AdInfo) {
                Log.d("LOG", "Level Play -> onAdClosed -> $adInfo")
            }
        }

    private val ahListener = object : AHAnalyze {
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
