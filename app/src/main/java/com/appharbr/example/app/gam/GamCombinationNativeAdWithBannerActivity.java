package com.appharbr.example.app.gam;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.appharbr.example.app.R;
import com.appharbr.sdk.engine.AdResult;
import com.appharbr.sdk.engine.AdSdk;
import com.appharbr.sdk.engine.AdStateResult;
import com.appharbr.sdk.engine.AppHarbr;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.formats.AdManagerAdViewOptions;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import java.util.Locale;

public class GamCombinationNativeAdWithBannerActivity extends AppCompatActivity {

    private Button refresh;
    private TextView videoStatus;
    private AdManagerAdView currentBannerAdView;
    private NativeAd unifiedNativeAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gam_nativead_activity);

        refresh = findViewById(R.id.btn_refresh);
        videoStatus = findViewById(R.id.tv_video_status);
        refresh.setOnClickListener(v -> requestAdLoader());

        requestAdLoader();
    }

    private void requestAdLoader() {
        refresh.setEnabled(false);
        AdLoader adLoader = new AdLoader.Builder(getApplicationContext(),
                getApplicationContext().getResources().getString(R.string.gam_nativebanner_ad_unit_id))
                .forNativeAd(nativeAd -> {
                    releaseBannerResources();

                    // ############ Check with AppHarbr if this ad suppose to be display ################
                    AdResult adResult = AppHarbr.shouldBlockNativeAd(AdSdk.GAM, nativeAd, getApplicationContext().getResources().getString(R.string.gam_nativebanner_ad_unit_id));
                    if (adResult.getAdStateResult() == AdStateResult.BLOCKED) {
                        // **** AppHarbr Blocked The Native Ad - Do Not Render it. ****
                        // May request another ad.
                        refresh.setEnabled(true);
                        return;
                    }

                    if (unifiedNativeAd != null) {
                        unifiedNativeAd.destroy();
                    }
                    unifiedNativeAd = nativeAd;

                    FrameLayout frameLayout = findViewById(R.id.fl_adplaceholder);
                    NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.ad_unified, null);
                    populateUnifiedNativeAdView(nativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                })
                .forAdManagerAdView(adView -> {
                    refresh.setEnabled(true);
                    releaseBannerResources();
                    currentBannerAdView = adView;

                    // ####### Publisher got a new Banner - Using AppHarbr to monitor it ########
                    AppHarbr.addBannerViewFromAdLoader(AdSdk.GAM, adView, getLifecycle(), null);

                    FrameLayout frameLayout =
                            findViewById(R.id.fl_adplaceholder);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                }, new AdSize(300, 250)).withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                    }
                })
                .withAdManagerAdViewOptions(new AdManagerAdViewOptions.Builder()
                        .build())
                .build();
        adLoader.loadAd(new AdManagerAdRequest.Builder().build());
    }

    // Publisher to display Native Ad
    private void populateUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        adView.setMediaView(adView.findViewById(R.id.ad_media));
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }
        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }
        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }
        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }
        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }
        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }
        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
        VideoController vc = nativeAd.getMediaContent().getVideoController();
        if (vc.hasVideoContent()) {
            videoStatus.setText(String.format(Locale.getDefault(),
                    "Video status: Ad contains a video asset."));
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    refresh.setEnabled(true);
                    videoStatus.setText("Video status: Video playback has ended.");
                    super.onVideoEnd();
                }
            });
        } else {
            videoStatus.setText("Video status: Ad does not contain a video asset.");
            refresh.setEnabled(true);
        }
    }


//    private final AHListener ahListener = (view, unitId, adFormat, reasons)
//            -> Log.d("LOG", "AppHarbr - onAdBlocked for: " + unitId + ", reason: " + Arrays.toString(reasons));

    private void releaseBannerResources() {
        if (currentBannerAdView != null) {
            AppHarbr.removeBannerView(currentBannerAdView);
            currentBannerAdView.destroy();
            currentBannerAdView = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseBannerResources();
    }
}
