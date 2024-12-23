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
import com.appharbr.sdk.engine.mediators.gam.interstitial.AHGamInterstitialAd
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import org.prebid.mobile.AdUnit
import org.prebid.mobile.InterstitialAdUnit

class PrebidGamInterstitialActivity : ComponentActivity() {

    /**
     * Credentials to load Interstitial Ad
     */
    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-app-original-api-display-interstitial"
        const val CONFIG_ID = "prebid-ita-display-interstitial-320-480"
    }

    private var prebidAdUnit: AdUnit? = null
    private val ahInterstitialAd = AHGamInterstitialAd()

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
                        Text(text = "Prebid Gam Interstitial Sample")

                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    private fun createAd() {
        // Create Prebid Interstitial Ad
        prebidAdUnit = InterstitialAdUnit(CONFIG_ID, 80, 60)
        prebidAdUnit?.setAutoRefreshInterval(30)

        // Make a bid request to Prebid Server
        val request = AdManagerAdRequest.Builder().build()
        prebidAdUnit?.fetchDemand(request) {

            // We are creating AppHarbr monitor system before loading Ad
            val adListener = useAppHarbrToMonitorAd()

            // Load a GAM interstitial ad
            adListener?.let { listener ->
                AdManagerInterstitialAd.load(
                    this,
                    AD_UNIT_ID,
                    request,
                    listener
                )
            }
        }
    }

    private fun useAppHarbrToMonitorAd() =
        AppHarbr.addInterstitial<AdManagerInterstitialAdLoadCallback>(
            AdSdk.GAM,
            ahInterstitialAd,
            object : InAppBidding {

                /**
                 * In order to fulfil scanning process AppHarbr also needs bidding object,
                 * for that InAppBidding interface can be used to send various biding objects to AppHarbr SDK.
                 * In this case we have Prebid Interstitial Ad as a bidding object.
                 */
                override fun getPrebidObject(adFormat: AdFormat, mediationAdUnitId: String) =
                    prebidAdUnit

                override fun getNimbusObject(adFormat: AdFormat, mediationAdUnitId: String) = null

                override fun getAmazonObject(adFormat: AdFormat, mediationAdUnitId: String) = null
            },
            /**
             * AppHarbr needs regular Interstitial Ad load callback
             */
            object : AdManagerInterstitialAdLoadCallback() {

                override fun onAdLoaded(interstitialAd: AdManagerInterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    Log.i("LOG", "Prebid Gam Ad was loaded.")
                    ahInterstitialAd.setInterstitialAd(interstitialAd)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    ahInterstitialAd.setInterstitialAd(null)
                    super.onAdFailedToLoad(loadAdError)
                    Log.e(
                        "LOG",
                        "Failed to load Prebid: ${loadAdError.cause} | ${loadAdError.code} | ${loadAdError.message} | ${loadAdError.responseInfo}"
                    )
                }

            },
            lifecycle,
            ahListener
        )

    /**
     * All scanning and analyze results with all important parameters
     * like(Ad object, Ad format, Ad unitID, analyzed result, block reasons, report reasons and creativeID)
     * from AppHarbr will come into this listener
     */
    val ahListener: AHAnalyze = object : AHAnalyze {

        override fun onAdAnalyzed(
            analyzedInfo: AdAnalyzedInfo?
        ) {
            Log.i(
                "LOG",
                "######## AppHarbr onAdAnalyzed Ad: Ad[${analyzedInfo?.view?.javaClass?.simpleName}] unitId[${analyzedInfo?.unitId}] adFormat[${analyzedInfo?.adFormat}] result[${analyzedInfo?.analyzedResult}]"
            )

            when (analyzedInfo?.analyzedResult) {
                AHAdAnalyzedResult.WILL_ANALYZE_ON_DISPLAY -> run {
                    // This means that AppHarbr will scan Interstitial Ad when it will be shown.
                    // This displayed Ad will be closed Automatically if AppHarbr blocks this Ad
                    ahInterstitialAd.gamInterstitialAd?.show(this@PrebidGamInterstitialActivity)
                }

                AHAdAnalyzedResult.ANALYZED_SUCCESSFULLY_ON_AD_LOAD -> run {
                    // AppHarbr analyzed Ad successfully, now we need to check result of this analyzes
                    val analyzedResult = AppHarbr.getInterstitialResult(ahInterstitialAd)
                    if (analyzedResult.adStateResult == AdStateResult.BLOCKED) {
                        Log.w("LOG", "AppHarbr Blocked Ad, please reload Ad")
                    } else {
                        ahInterstitialAd.gamInterstitialAd?.show(this@PrebidGamInterstitialActivity)
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
                    incidentInfo?.blockReasons?.joinToString(
                        separator = ","
                    )
                }]\nPlease reload Ad"
            )
        }

        override fun onAdIncident(incidentInfo: AdIncidentInfo?) {
            Log.i(
                "LOG",
                "######## AppHarbr onAdIncident: Ad[${incidentInfo?.view?.javaClass?.simpleName}] unitId[${incidentInfo?.unitId}] adFormat[${incidentInfo?.adFormat}] reasons[${
                    incidentInfo?.reportReasons?.joinToString(
                        separator = ","
                    )
                }]"
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        prebidAdUnit?.stopAutoRefresh()
    }

}