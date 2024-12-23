package com.appharbr.kotlin.example.app.admob

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.appharbr.kotlin.example.app.R
import com.appharbr.kotlin.example.app.ui.theme.AppHarbrExampleAppTheme
import com.appharbr.sdk.engine.AdSdk
import com.appharbr.sdk.engine.AdStateResult
import com.appharbr.sdk.engine.AppHarbr
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

class AdmobNativeAdActivity : ComponentActivity() {

    private val nativeAdState = mutableStateOf<NativeAd?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNativeAd()

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
                    }
                }
            }
        }
    }

    private fun requestNativeAd() {
        val adLoader = AdLoader.Builder(
            this,
            applicationContext.resources.getString(R.string.admob_native_ad_unit_id)
        ).forNativeAd { nativeAd: NativeAd ->
            if (isDestroyed) {
                nativeAdState.value?.destroy()
                return@forNativeAd
            }

            val adResult = AppHarbr.shouldBlockNativeAd(
                AdSdk.ADMOB,
                nativeAd,
                applicationContext.resources.getString(R.string.admob_native_ad_unit_id)
            )

            when (adResult.adStateResult) {
                AdStateResult.BLOCKED -> {
                    nativeAdState.value = null
                    Log.e("LOG", "Native ad was blocked by appharbr")
                    // ***** Publisher may reload ad *****
                    return@forNativeAd
                }

                else -> {
                    // Set native add object into state
                    nativeAdState.value?.destroy()
                    nativeAdState.value = nativeAd
                }
            }
        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("LOG", "Handle the failure by logging, altering the UI, and so on.")
                // ***** Publisher may reload ad *****
            }
        }).withNativeAdOptions(
            NativeAdOptions.Builder().build()
        ).build()

        adLoader.loadAd(AdRequest.Builder().build())
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
                text = stringResource(id = R.string.admob_native_screen),
                fontSize = 20.sp
            )
            CircularProgressIndicator()
        }
    }

}