package com.appharbr.kotlin.example.app.admob

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
import com.appharbr.sdk.engine.mediators.admob.interstitial.AHAdMobInterstitialAd
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdmobInterstitialActivity : ComponentActivity() {

    private val ahAdMobInterstitialAd = AHAdMobInterstitialAd()
    private var ahWrapperListener: InterstitialAdLoadCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prepareAppHarbrWrapperListener()
        requestAd()

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
                        Text(text = stringResource(id = R.string.admob_interstitial_screen))

                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    private fun prepareAppHarbrWrapperListener() {
        //      **** (1) ****
        // The publisher will initiate the listener wrapper and will use it when load the Admob Interstitial Ad.
        ahWrapperListener = AppHarbr.addInterstitial<InterstitialAdLoadCallback>(
            AdSdk.ADMOB,
            ahAdMobInterstitialAd,
            interstitialAdLoadCallback,
            ahListener
        )
    }

    private fun requestAd() {
        //      **** (2) ****
        //Request to load interstitial Ad and instead of AdManagerInterstitialAdLoadCallback we should use ahWrapperListener to monitor interstitial Ad
        ahWrapperListener?.let {
            AdManagerInterstitialAd.load(
                this,
                applicationContext.resources.getString(R.string.admob_interstitial_ad_unit_id),
                AdManagerAdRequest.Builder().build(),
                it
            )
        }
    }

    private val interstitialAdLoadCallback: InterstitialAdLoadCallback =
        object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                Log.d("LOG", "onAdLoaded")
                if (isDestroyed) {
                    return
                }

                setFullScreenCallBack()
                showAd()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d("LOG", "onAdFailedToLoad: " + loadAdError.message)
            }
        }

    //      **** (3) ****
    // Add full screen callbacks for the add
    private fun setFullScreenCallBack() {
        ahAdMobInterstitialAd.adMobInterstitialAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    ahAdMobInterstitialAd.setInterstitialAd(null)
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(
                        "LOG",
                        "**************************** AppHarbr Interstitial Add Dismissed ****************************"
                    )

                    // You may load new add
                }
            }
    }

    //      **** (4) ****
    // Check was interstitial Ad blocked or not
    private fun showAd() {
        ahAdMobInterstitialAd.adMobInterstitialAd?.let {
            val interstitialResult = AppHarbr.getInterstitialResult(ahAdMobInterstitialAd)
            if (interstitialResult.adStateResult != AdStateResult.BLOCKED) {
                Log.d(
                    "LOG",
                    "**************************** AppHarbr Permit to Display Admob Interstitial ****************************"
                )
                it.show(this)
            } else {
                Log.d(
                    "LOG",
                    "**************************** AppHarbr Blocked Admob Interstitial ****************************"
                )
                // You may call to reload interstitial
            }
        } ?: Log.d("TAG", "The Admob interstitial wasn't loaded yet.")
    }

    var ahListener = object : AHAnalyze {
        override fun onAdBlocked(incidentInfo: AdIncidentInfo?) {
            Log.d(
                "LOG",
                "AppHarbr - onAdBlocked for: ${incidentInfo?.unitId}, reason: " + incidentInfo?.blockReasons.contentToString()
            )

            if (incidentInfo?.shouldLoadNewAd == true) {
                // If add was blocked before being displayed, load new add
                requestAd()
            }
        }

        override fun onAdIncident(incidentInfo: AdIncidentInfo?) {
            Log.d(
                "LOG",
                "AppHarbr - onAdIncident for: ${incidentInfo?.unitId}, reason: " + incidentInfo?.blockReasons.contentToString()
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