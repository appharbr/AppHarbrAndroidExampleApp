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
import com.appharbr.sdk.engine.mediators.admob.rewarded.AHAdMobRewardedAd
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdmobRewardedActivity : ComponentActivity() {

    private val ahAdMobRewardedAd = AHAdMobRewardedAd()
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
                        Text(text = stringResource(id = R.string.admob_rewarded_screen))

                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    private fun prepareAppHarbrWrapperListener() {
        //      **** (1) ****
        // The publisher will initiate the listener wrapper and will use it when load the Admob Rewarded Ad.
        ahWrapperListener = AppHarbr.addRewardedAd<RewardedAdLoadCallback>(
            AdSdk.ADMOB,
            ahAdMobRewardedAd,
            rewardedLoadCallback,
            ahListener
        )
    }

    private fun requestAd() {
        //      **** (2) ****
        //Request to load rewarded Ad and instead of RewardedAdLoadCallback we should use ahWrapperListener to monitor rewarded Ad
        ahWrapperListener?.let {
            RewardedAd.load(
                applicationContext,
                applicationContext.resources.getString(R.string.admob_rewarded_ad_unit_id),
                AdRequest.Builder().build(),
                it
            )
        }

    }

    private val rewardedLoadCallback: RewardedAdLoadCallback = object : RewardedAdLoadCallback() {
        override fun onAdLoaded(rewardedAd: RewardedAd) {
            super.onAdLoaded(rewardedAd)
            Log.e("TAG", "AdMob - RewardedAdLoadCallback - onAdLoaded")
            if (isDestroyed) {
                return
            }

            setFullScreenCallBack()
            showAd()
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            Log.e(
                "TAG",
                "AdMob - RewardedAdLoadCallback - onAdFailedToLoad: " + loadAdError.message
            )
        }
    }

    //      **** (3) ****
    // Add full screen callbacks for the add
    private fun setFullScreenCallBack() {
        ahAdMobRewardedAd.adMobRewardedAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    ahAdMobRewardedAd.setRewardedAd(null)
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
        ahAdMobRewardedAd.adMobRewardedAd?.let {
            val rewardedResult = AppHarbr.getRewardedResult(ahAdMobRewardedAd)
            if (rewardedResult.adStateResult != AdStateResult.BLOCKED) {
                Log.d(
                    "LOG",
                    "**************************** AppHarbr Permit to Display Admob Rewarded ****************************"
                )
                it.show(this) { rewardItem: RewardItem ->
                    Log.d(
                        "TAG",
                        "onUserEarnedReward: $rewardItem"
                    )
                }
            } else {
                Log.d(
                    "LOG",
                    "**************************** AppHarbr Blocked Admob Rewarded ****************************"
                )
                // You may call to reload Rewarded
            }
        } ?: Log.d("TAG", "The Admob Rewarded wasn't loaded yet.")
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