package com.appharbr.kotlin.example.app.gam

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
import com.appharbr.sdk.engine.mediators.gam.rewarded.AHGamRewardedAd
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class GamRewardedActivity : ComponentActivity() {

    private val ahGamRewardedAd = AHGamRewardedAd()
    private var ahWrapperListener: RewardedAdLoadCallback? = null

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
                        Text(text = stringResource(id = R.string.gam_rewarded_screen))

                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    private fun prepareAppHarbrWrapperListener() {
        //      **** (1) ****
        // The publisher will initiate the listener wrapper and will use it when load the GAM Rewarded Ad.
        ahWrapperListener = AppHarbr.addRewardedAd<RewardedAdLoadCallback>(
            AdSdk.GAM,
            ahGamRewardedAd,
            adManagerRewardedAdLoadCallback,
            ahListener
        )
    }

    private fun requestAd() {
        //      **** (2) ****
        //Request to load rewarded Ad and instead of RewardedAdLoadCallback we should use ahWrapperListener to monitor rewarded Ad
        ahWrapperListener?.let {
            RewardedAd.load(
                applicationContext,
                applicationContext.resources.getString(R.string.gam_rewarded_ad_unit_id),
                AdRequest.Builder().build(),
                it
            )
        }
    }

    private val adManagerRewardedAdLoadCallback: RewardedAdLoadCallback =
        object : RewardedAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedAd) {
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
        ahGamRewardedAd.gamRewardedAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    ahGamRewardedAd.setRewardedAd(null)
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(
                        "LOG",
                        "**************************** Rewarded Add Dismissed ****************************"
                    )

                    // You may load new add
                }
            }
    }

    //      **** (4) ****
    //Check was rewarded Ad blocked or not
    private fun showAd() {
        ahGamRewardedAd.gamRewardedAd?.let {
            val rewardedResult = AppHarbr.getRewardedResult(ahGamRewardedAd)
            if (rewardedResult.adStateResult != AdStateResult.BLOCKED) {
                Log.d(
                    "LOG",
                    "**************************** AppHarbr Permit to Display GAM Rewarded ****************************"
                )
                it.show(this) { rewardItem: RewardItem ->
                    Log.d(
                        "LOG",
                        "onUserEarnedReward: $rewardItem"
                    )
                }
            } else {
                Log.d(
                    "LOG",
                    "**************************** AppHarbr Blocked GAM Rewarded ****************************"
                )
                // You may call to reload Rewarded
            }
        } ?: Log.d("TAG", "The GAM Rewarded wasn't loaded yet.")
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