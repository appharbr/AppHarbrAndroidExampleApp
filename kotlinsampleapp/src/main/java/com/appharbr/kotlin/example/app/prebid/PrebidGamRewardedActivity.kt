package com.appharbr.kotlin.example.app.prebid

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
import com.appharbr.sdk.engine.adformat.AdFormat
import com.appharbr.sdk.engine.adnetworks.inappbidding.InAppBidding
import com.appharbr.sdk.engine.diagnostic.AHAdAnalyzedResult
import com.appharbr.sdk.engine.listeners.AHAnalyze
import com.appharbr.sdk.engine.listeners.AdAnalyzedInfo
import com.appharbr.sdk.engine.listeners.AdIncidentInfo
import com.appharbr.sdk.engine.mediators.gam.rewarded.AHGamRewardedAd
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import org.prebid.mobile.RewardedVideoAdUnit
import org.prebid.mobile.Signals
import org.prebid.mobile.VideoParameters

class PrebidGamRewardedActivity : ComponentActivity() {

    /**
     * Credentials to load Rewarded Ad
     */
    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-app-original-api-video-interstitial"
        const val CONFIG_ID = "prebid-ita-video-rewarded-320-480-original-api"
    }

    private var prebidRewardedAdUnit: RewardedVideoAdUnit? = null
    private val ahRewardedAd = AHGamRewardedAd()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()

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

    private fun createAd() {
        // Create Prebid RewardedVideoAdUnit
        prebidRewardedAdUnit = RewardedVideoAdUnit(CONFIG_ID)

        // Configure Video parameters of Prebid Video AD
        prebidRewardedAdUnit?.videoParameters = configureVideoParameters()

        // Make a bid request to Prebid Server
        val request = AdManagerAdRequest.Builder().build()
        prebidRewardedAdUnit?.fetchDemand(request) {

            // We are creating AppHarbr monitor system before loading Ad
            val adListener = useAppHarbrToMonitorAd()

            //Load a GAM Rewarded Ad
            adListener?.let { listener ->
                RewardedAd.load(
                    this,
                    AD_UNIT_ID,
                    request,
                    listener
                )
            }
        }
    }

    private fun configureVideoParameters(): VideoParameters {
        return VideoParameters(listOf("video/mp4")).apply {
            protocols = listOf(Signals.Protocols.VAST_2_0)
            playbackMethod = listOf(Signals.PlaybackMethod.AutoPlaySoundOff)
        }
    }

    private fun useAppHarbrToMonitorAd(): RewardedAdLoadCallback? {
        return AppHarbr.addRewardedAd<RewardedAdLoadCallback>(
            AdSdk.GAM,
            ahRewardedAd,
            object : InAppBidding {
                /**
                 * In order to fulfil scanning process AppHarbr also needs bidding object,
                 * for that InAppBidding interface can be used to send various biding objects to AppHarbr SDK.
                 * In this case we have Prebid Rewarded Ad as a bidding object.
                 */
                override fun getPrebidObject(adFormat: AdFormat, mediationAdUnitId: String) =
                    prebidRewardedAdUnit

                override fun getNimbusObject(adFormat: AdFormat, mediationAdUnitId: String) = null

                override fun getAmazonObject(adFormat: AdFormat, mediationAdUnitId: String) = null
            },
            /**
             * AppHarbr needs regular Rewarded Ad load callback
             */
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.i("LOG", "Prebid Gam Ad was loaded.")
                    ahRewardedAd.setRewardedAd(rewardedAd)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    ahRewardedAd.setRewardedAd(null)
                    Log.e(
                        "LOG",
                        "Failed to load Prebid: ${loadAdError.cause} | ${loadAdError.code} | ${loadAdError.message} | ${loadAdError.responseInfo}"
                    )
                }
            },
            lifecycle,
            ahListener,
        )
    }

    /**
     * All scanning and analyze results with all important parameters
     * like(Ad object, Ad format, Ad unitID, analyzed result, block reasons, report reasons and creativeID)
     * from AppHarbr will come into this listener
     */
    val ahListener: AHAnalyze = object : AHAnalyze {

        override fun onAdAnalyzed(analyzedInfo: AdAnalyzedInfo?) {
            Log.i(
                "LOG",
                "######## AppHarbr onAdAnalyzed Ad: Ad[${analyzedInfo?.view?.javaClass?.simpleName}] unitId[${analyzedInfo?.unitId}] adFormat[${analyzedInfo?.adFormat}] result[${analyzedInfo?.analyzedResult}]"
            )

            when (analyzedInfo?.analyzedResult) {
                AHAdAnalyzedResult.WILL_ANALYZE_ON_DISPLAY -> run {
                    // This means that AppHarbr will scan Rewarded Ad when it will be shown.
                    // This displayed Ad will be closed Automatically if AppHarbr blocks this Ad
                    ahRewardedAd.gamRewardedAd?.show(this@PrebidGamRewardedActivity) {
                        Log.i("LOG", "User was rewarded: Amount[${it.amount}]")
                    }
                }

                AHAdAnalyzedResult.ANALYZED_SUCCESSFULLY_ON_AD_LOAD -> run {
                    // AppHarbr analyzed Ad successfully, now we need to check result of this analyzes
                    val analyzedResult = AppHarbr.getRewardedResult(ahRewardedAd)
                    if (analyzedResult.adStateResult == AdStateResult.BLOCKED) {
                        Log.w("LOG", "AppHarbr Blocked Ad, please reload Ad")
                    } else {
                        ahRewardedAd.gamRewardedAd?.show(this@PrebidGamRewardedActivity) {
                            Log.i("LOG", "User was rewarded: Amount[${it.amount}]")
                        }
                    }
                }

                else -> {
                    Log.e("LOG", analyzedInfo?.analyzedResult?.description.orEmpty())
                }
            }
        }

        override fun onAdBlocked(incidentInfo: AdIncidentInfo?) {
            Log.w(
                "LOG",
                "######## AppHarbr Blocked Ad: Ad[${incidentInfo?.view?.javaClass?.simpleName}] unitId[${incidentInfo?.unitId}] adFormat[${incidentInfo?.adFormat}] reasons[${
                    incidentInfo?.blockReasons?.joinToString(separator = ",")
                }]\nPlease reload Ad"
            )
        }

        override fun onAdIncident(incidentInfo: AdIncidentInfo?) {
            Log.i(
                "LOG",
                "######## AppHarbr onAdIncident: Ad[${incidentInfo?.view?.javaClass?.simpleName}] unitId[${incidentInfo?.unitId}] adFormat[${incidentInfo?.adFormat}] reasons[${
                    incidentInfo?.reportReasons?.joinToString(separator = ",")
                }]"
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        prebidRewardedAdUnit?.stopAutoRefresh()
    }

}