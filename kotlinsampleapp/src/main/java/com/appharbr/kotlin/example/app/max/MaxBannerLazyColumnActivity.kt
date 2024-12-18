package com.appharbr.kotlin.example.app.max

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.appharbr.kotlin.example.app.ui.theme.AppHarbrExampleAppTheme
import com.appharbr.sdk.engine.AdSdk
import com.appharbr.sdk.engine.AppHarbr
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView

class MaxBannerLazyColumnActivity : ComponentActivity() {

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

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(all = 10.dp),
                        ) {
                            items(30) { index ->

                                if (index % 4 == 0) {
                                    AddBanner()
                                } else {
                                    Text(text = "Some item example: $index")
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    @Composable
    private fun AddBanner() {
        //We need AndroidView to add banner in Compose UI
        AndroidView(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
            factory = { context ->
                //      **** (1) ****
                //      Add Banner View in compose with all necessary params, like unit id and ad listener
                MaxAdView("YOUR_AD_UNIT_ID", context).apply {
                    setListener(createMaxAdViewAdListener())

                    //      **** (2) ****
                    //Add Max's banner View instance for Monitoring
                    AppHarbr.addBannerView(
                        AdSdk.MAX,
                        this,
                        lifecycle,
                        null
                    )

                    //      **** (3) ****
                    //      Request for the Ads
                    loadAd()
                }
            }
        )
    }

    private fun createMaxAdViewAdListener() = object : MaxAdViewAdListener {
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

//    private val ahListener =
//        AHListener { view: Any?, unitId: String?, adFormat: AdFormat?, reasons: Array<AdBlockReason?>? ->
//            Log.d(
//                "LOG",
//                "AppHarbr - onAdBlocked for: $unitId, reason: " + Arrays.toString(
//                    reasons
//                )
//            )
//        }

}