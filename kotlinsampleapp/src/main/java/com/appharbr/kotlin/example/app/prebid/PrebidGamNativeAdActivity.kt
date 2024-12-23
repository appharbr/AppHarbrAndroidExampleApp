package com.appharbr.kotlin.example.app.prebid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.appharbr.kotlin.example.app.ui.theme.AppHarbrExampleAppTheme
import com.appharbr.sdk.engine.AdSdk
import com.appharbr.sdk.engine.AdStateResult
import com.appharbr.sdk.engine.AppHarbr
import com.appharbr.sdk.engine.adformat.AdFormat
import com.appharbr.sdk.engine.adnetworks.inappbidding.InAppBidding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.formats.OnAdManagerAdViewLoadedListener
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeCustomFormatAd
import org.prebid.mobile.NativeAdUnit
import org.prebid.mobile.NativeDataAsset
import org.prebid.mobile.NativeEventTracker
import org.prebid.mobile.NativeImageAsset
import org.prebid.mobile.NativeTitleAsset
import org.prebid.mobile.PrebidNativeAd
import org.prebid.mobile.PrebidNativeAdListener
import org.prebid.mobile.addendum.AdViewUtils

class PrebidGamNativeAdActivity : ComponentActivity() {

    // State of prebid native ad which notifies compose UI to draw native Ad after it is loaded
    private val prebidNativeAdState = mutableStateOf<PrebidNativeAd?>(null)

    /**
     * Credentials to load Native Ad
     */
    companion object {
        const val AD_UNIT_ID = "/21808260008/apollo_custom_template_native_ad_unit"
        const val CONFIG_ID = "prebid-ita-banner-native-styles"
        const val CUSTOM_FORMAT_ID = "11934135"
    }

    private var adManagerAdView: AdManagerAdView? = null
    private var nativeAd: NativeAd? = null
    private var adLoader: AdLoader? = null
    private var prebidNativeAdUnit: NativeAdUnit? = null

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
                        DisplayNativeAd()
                    }
                }
            }
        }
    }

    private fun createAd() {
        // Create Prebid NativeAdUnit
        prebidNativeAdUnit = NativeAdUnit(CONFIG_ID)
        prebidNativeAdUnit?.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC)
        prebidNativeAdUnit?.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED)
        prebidNativeAdUnit?.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL)

        // Add native assets and trackers
        addNativeAssets(prebidNativeAdUnit)

        // Make a bid request to Prebid Server
        val adRequest = AdManagerAdRequest.Builder().build()
        prebidNativeAdUnit?.fetchDemand(adRequest) {

            // Create Gam Ad loader and load Ad
            adLoader = createAdLoader()
            adLoader?.loadAd(adRequest)
        }
    }

    private fun addNativeAssets(adUnit: NativeAdUnit?) {
        val title = NativeTitleAsset()
        title.setLength(90)
        title.isRequired = true
        adUnit?.addAsset(title)

        val icon = NativeImageAsset(20, 20, 20, 20)
        icon.imageType = NativeImageAsset.IMAGE_TYPE.ICON
        icon.isRequired = true
        adUnit?.addAsset(icon)

        val image = NativeImageAsset(200, 200, 200, 200)
        image.imageType = NativeImageAsset.IMAGE_TYPE.MAIN
        image.isRequired = true
        adUnit?.addAsset(image)

        val data = NativeDataAsset()
        data.len = 90
        data.dataType = NativeDataAsset.DATA_TYPE.SPONSORED
        data.isRequired = true
        adUnit?.addAsset(data)

        val body = NativeDataAsset()
        body.isRequired = true
        body.dataType = NativeDataAsset.DATA_TYPE.DESC
        adUnit?.addAsset(body)

        val cta = NativeDataAsset()
        cta.isRequired = true
        cta.dataType = NativeDataAsset.DATA_TYPE.CTATEXT
        adUnit?.addAsset(cta)

        // Add native event trackers
        val methods = ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD>()
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE)
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS)
        try {
            val tracker = NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods)
            adUnit?.addEventTracker(tracker)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createAdLoader(): AdLoader {
        val onGamAdLoaded = OnAdManagerAdViewLoadedListener { adManagerAdView: AdManagerAdView ->
            this.adManagerAdView = adManagerAdView
        }

        val onUnifiedAdLoaded = NativeAd.OnNativeAdLoadedListener { unifiedNativeAd: NativeAd? ->
            this.nativeAd = unifiedNativeAd

            // We need to check Native Ad and for that we will use AppHarbr with appropriate parameter to scan loaded native ad
            val state = AppHarbr.shouldBlockNativeAd(
                AdSdk.GAM,
                unifiedNativeAd,
                object : InAppBidding {
                    /**
                     * In order to fulfil scanning process AppHarbr also needs bidding object,
                     * for that InAppBidding interface can be used to send various biding objects to AppHarbr SDK.
                     * In this case we have Prebid native Ad as a bidding object.
                     */
                    override fun getPrebidObject(adFormat: AdFormat, mediationAdUnitId: String) =
                        prebidNativeAdUnit

                    override fun getNimbusObject(adFormat: AdFormat, mediationAdUnitId: String) =
                        null

                    override fun getAmazonObject(adFormat: AdFormat, mediationAdUnitId: String) =
                        null
                },
                AD_UNIT_ID
            )

            if (state.adStateResult == AdStateResult.BLOCKED) {
                Log.e(
                    "LOG",
                    "Native Ad was blocked by AppHarbr! We should skipp this native Ad and load new one"
                )
                return@OnNativeAdLoadedListener
            }
        }

        val onCustomAdLoaded =
            NativeCustomFormatAd.OnCustomFormatAdLoadedListener { nativeCustomTemplateAd: NativeCustomFormatAd? ->

                // We need to check Native Ad and for that we will use AppHarbr with appropriate parameter to scan loaded native ad
                val state = AppHarbr.shouldBlockNativeAd(
                    AdSdk.GAM,
                    nativeCustomTemplateAd,
                    object : InAppBidding {

                        /**
                         * In order to fulfil scanning process AppHarbr also needs bidding object,
                         * for that InAppBidding interface can be used to send various biding objects to AppHarbr SDK.
                         * In this case we have Prebid native Ad as a bidding object.
                         */
                        override fun getPrebidObject(
                            adFormat: AdFormat,
                            mediationAdUnitId: String
                        ) = prebidNativeAdUnit

                        override fun getNimbusObject(
                            adFormat: AdFormat,
                            mediationAdUnitId: String
                        ) = null

                        override fun getAmazonObject(
                            adFormat: AdFormat,
                            mediationAdUnitId: String
                        ) = null
                    },
                    AD_UNIT_ID
                )


                if (state.adStateResult == AdStateResult.BLOCKED) {
                    Log.e(
                        "LOG",
                        "Native Custom Format Ad was blocked by AppHarbr! We should skipp this native Ad and load new one"
                    )
                    return@OnCustomFormatAdLoadedListener
                }

                // Find Prebid Native Ad
                AdViewUtils.findNative(nativeCustomTemplateAd!!, object : PrebidNativeAdListener {
                    override fun onPrebidNativeLoaded(ad: PrebidNativeAd) {
                        //Update state to notify compose UI to dra Native Ad
                        prebidNativeAdState.value = ad
                    }

                    override fun onPrebidNativeNotFound() {
                        Log.e("LOG", "onPrebidNativeNotFound")
                    }

                    override fun onPrebidNativeNotValid() {
                        Log.e("LOG", "onPrebidNativeNotValid")
                    }
                })
            }

        // Create Ad load with all required parameters
        return AdLoader.Builder(baseContext, AD_UNIT_ID)
            .forAdManagerAdView(onGamAdLoaded, AdSize.BANNER)
            .forNativeAd(onUnifiedAdLoaded)
            .forCustomFormatAd(
                CUSTOM_FORMAT_ID, onCustomAdLoaded
            ) { _: NativeCustomFormatAd?, _: String? -> }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Log.e("LOG", "onAdFailedToLoad")
                }
            })
            .build()
    }

    @Composable
    private fun DisplayNativeAd() {
        prebidNativeAdState.value?.let {
            Text(text = "Title: ${it.title}")
            Text(text = "Description: ${it.description}")
            Button(onClick = { Log.d("LOG", it.callToAction) }) {
                Text(text = it.callToAction)
            }

            val iconPainter = rememberAsyncImagePainter(it.iconUrl)
            Image(
                painter = iconPainter,
                contentDescription = "Icon",
                modifier = Modifier.fillMaxSize(), // Adjust as needed
            )

            val imagePainter = rememberAsyncImagePainter(it.imageUrl)
            Image(
                painter = imagePainter,
                contentDescription = "Image",
                modifier = Modifier.fillMaxSize(), // Adjust as needed
            )

        } ?: kotlin.run {
            Text(
                text = "Prebid Gam Native Sample",
                fontSize = 20.sp
            )
            CircularProgressIndicator()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adManagerAdView?.destroy()
        prebidNativeAdUnit?.stopAutoRefresh()
        nativeAd?.destroy()
    }

}