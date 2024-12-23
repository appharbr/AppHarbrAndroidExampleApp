package com.appharbr.kotlin.example.app.gam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import com.appharbr.kotlin.example.app.R
import com.appharbr.kotlin.example.app.ui.theme.AppHarbrExampleAppTheme
import com.appharbr.sdk.engine.AdSdk
import com.appharbr.sdk.engine.AdStateResult
import com.appharbr.sdk.engine.AppHarbr
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.formats.AdManagerAdViewOptions
import com.google.android.gms.ads.nativead.NativeAd

class GamCombinationNativeAdWithBannerActivity : ComponentActivity() {

    private val nativeAdState = mutableStateOf<NativeAd?>(null)
    private val adManagerAdViewState = mutableStateOf<AdManagerAdView?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestCombinedAd()

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
                        DisplayNativeAd()
                        DisplayBanner()
                    }
                }
            }
        }
    }

    private fun requestCombinedAd() {
        AdLoader.Builder(
            applicationContext,
            applicationContext.resources.getString(R.string.gam_nativebanner_ad_unit_id)
        )
            .forNativeAd { nativeAd: NativeAd ->

                // ############ Check with AppHarbr if this ad suppose to be display ################
                val adResult = AppHarbr.shouldBlockNativeAd(AdSdk.GAM, nativeAd, applicationContext.resources.getString(R.string.gam_nativebanner_ad_unit_id))
                if (adResult.adStateResult == AdStateResult.BLOCKED) {
                    // **** AppHarbr Blocked The Native Ad - Do Not Render it. ****
                    // May request another ad.
                    nativeAdState.value = null
                }

                if (isDestroyed) {
                    nativeAdState.value?.destroy()
                    return@forNativeAd
                }
                nativeAdState.value?.destroy()
                nativeAdState.value = nativeAd
            }
            .forAdManagerAdView({ adView: AdManagerAdView ->

                // ####### Publisher got a new Banner - Using AppHarbr to monitor it ########
                adManagerAdViewState.value?.let {
                    AppHarbr.removeBannerView(it)
                }
                AppHarbr.addBannerViewFromAdLoader(AdSdk.GAM, adView, null)

                adManagerAdViewState.value = adView

            }, AdSize(300, 250)).withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {}
            })
            .withAdManagerAdViewOptions(AdManagerAdViewOptions.Builder().build()).build()
            .loadAd(AdManagerAdRequest.Builder().build())
    }

    @Composable
    private fun DisplayNativeAd() {
        nativeAdState.value?.let {
            Text(text = "Headline: ${it.headline ?: "Empty Headline"}")
            Text(text = "Body: ${it.body ?: "Empty body"}")
            it.icon?.drawable?.toBitmap()?.let { bitmap -> Image(bitmap.asImageBitmap(), "") }
            for (image in it.images) {
                image.drawable?.toBitmap()?.let { bitmap -> Image(bitmap.asImageBitmap(), "") }
            }
        } ?: kotlin.run {
            Text(
                text = stringResource(id = R.string.gam_native_and_banner),
                fontSize = 20.sp
            )
            CircularProgressIndicator()
        }
    }

    @Composable
    private fun DisplayBanner() {
        adManagerAdViewState.value?.let {
            AndroidView(
                modifier = Modifier.wrapContentSize(),
                factory = { _ ->
                    it
                })
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