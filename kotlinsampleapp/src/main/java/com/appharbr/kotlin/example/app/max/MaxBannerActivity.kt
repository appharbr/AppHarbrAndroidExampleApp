package com.appharbr.kotlin.example.app.max

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.viewinterop.AndroidView
import com.appharbr.kotlin.example.app.R
import com.appharbr.kotlin.example.app.ui.theme.AppHarbrExampleAppTheme
import com.appharbr.sdk.engine.AdSdk
import com.appharbr.sdk.engine.AppHarbr
import com.appharbr.sdk.engine.listeners.AHAnalyze
import com.appharbr.sdk.engine.listeners.AdAnalyzedInfo
import com.appharbr.sdk.engine.listeners.AdIncidentInfo
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration

class MaxBannerActivity : ComponentActivity() {

    private lateinit var maxAdView: MaxAdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        maxAdView = MaxAdView("YOUR_UNIT_ID", this)

        setContent {
            AppHarbrExampleAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 50.dp, bottom = 50.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(text = stringResource(id = R.string.max_banner_screen))

                        AddBanner()
                    }
                }
            }
        }
        initializeApplovinSDK()
    }

    override fun onDestroy() {
        super.onDestroy()

        maxAdView.destroy()
    }

    //	**** (2) ****
    // Initialize Applovin SDK
    private fun initializeApplovinSDK(){
        val initConfig = AppLovinSdkInitializationConfiguration.builder(
            "YOUR_API_KEY",
            this
        ).setMediationProvider(AppLovinMediationProvider.MAX).build()

        AppLovinSdk.getInstance(this).initialize(initConfig) { sdkConfig ->
            Log.d("KotlinSample", "MAX mediation initialized successfully -> [${sdkConfig}]")
            loadBannerAd()
        }
    }

    private fun loadBannerAd() {
        with(maxAdView) {
            setListener(mAdListener)

            //      **** (3) ****
            //Add Max's banner View instance for Monitoring
            AppHarbr.addBannerView(
                AdSdk.MAX,
                this,
                lifecycle,
                ahListener
            )

            //      **** (4) ****
            //      Request for the Ads
            loadAd()
        }
    }

    //	**** (1) ****
    // Add banner view to the UI
    @Composable
    private fun AddBanner() {
        //We need AndroidView to add banner in Compose UI
        AndroidView(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
            factory = { maxAdView }
        )
    }

    private val mAdListener: MaxAdViewAdListener = object : MaxAdViewAdListener {
        override fun onAdExpanded(ad: MaxAd) {
            Log.d("LOG", "Max - onAdExpanded")
        }

        override fun onAdCollapsed(ad: MaxAd) {
            Log.d("LOG", "Max - onAdCollapsed")
        }

        override fun onAdLoaded(ad: MaxAd) {
            Log.d("LOG", "Max - onAdLoaded")
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
            Log.d("LOG", "Max - onAdLoadFailed: " + error.message + " => " + error.code)
        }

        override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
            Log.d("LOG", "Max - onAdDisplayFailed")
        }
    }

    var ahListener = object : AHAnalyze {
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
        }
    }
}