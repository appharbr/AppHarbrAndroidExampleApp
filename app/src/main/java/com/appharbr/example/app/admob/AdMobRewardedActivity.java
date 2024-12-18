package com.appharbr.example.app.admob;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.appharbr.example.app.R;
import com.appharbr.sdk.engine.AdResult;
import com.appharbr.sdk.engine.AdSdk;
import com.appharbr.sdk.engine.AdStateResult;
import com.appharbr.sdk.engine.AppHarbr;
import com.appharbr.sdk.engine.listeners.AHAnalyze;
import com.appharbr.sdk.engine.listeners.AdAnalyzedInfo;
import com.appharbr.sdk.engine.listeners.AdIncidentInfo;
import com.appharbr.sdk.engine.mediators.admob.rewarded.AHAdMobRewardedAd;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import java.util.Arrays;

public class AdMobRewardedActivity extends AppCompatActivity {

    private Button btnDisplay;
    private AHAdMobRewardedAd ahAdMobRewardedAd = new AHAdMobRewardedAd();
    private RewardedAdLoadCallback ahWrapperListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admob_rewarded_layout);

        btnDisplay = findViewById(R.id.admob_rewarded_display);
        setDisplayClick();

        // The publisher will initiate once the listener wrapper and will use it when load the AdMob Rewarded ad.
        ahWrapperListener = AppHarbr
                .addRewardedAd(AdSdk.ADMOB,
                        ahAdMobRewardedAd,
                        mRewardedLoadCallback,
                        ahListener);

        // The publisher load AdMob Rewarded ad
        requestAd();
    }

    private void requestAd() {
        btnDisplay.setEnabled(false);
        RewardedAd.load(getApplicationContext(),
                getApplicationContext().getResources().getString(R.string.admob_rewarded_ad_unit_id),
                new AdRequest.Builder().build(),
                ahWrapperListener); //AppHarbr wrapper listener
    }

    private void setDisplayClick() {
        btnDisplay.setOnClickListener((view) -> {
            // The publisher display the AdMob Rewarded
            if (isDestroyed()) {
                return;
            }

            setFullScreenCallBack();
            showAd();
        });
    }

    private void showAd() {
        if (ahAdMobRewardedAd.getAdMobRewardedAd() != null) {
            final AdResult rewardedResult = AppHarbr.getRewardedResult(ahAdMobRewardedAd);
            if (rewardedResult.adStateResult != AdStateResult.BLOCKED) {
                Log.d("LOG", "**************************** AppHarbr Permit to Display AdMob Rewarded ****************************");
                ahAdMobRewardedAd.getAdMobRewardedAd().show(AdMobRewardedActivity.this,
                        rewardItem -> Log.d("TAG",  "onUserEarnedReward: " + rewardItem));
            }
            else {
                Log.d("LOG", "**************************** AppHarbr Blocked AdMob Rewarded ****************************");
                // You may call to reload Rewarded
            }
        } else {
            Log.d("TAG", "The AdMob Rewarded wasn't loaded yet.");
        }
    }

    private void setFullScreenCallBack() {
        if (ahAdMobRewardedAd.getAdMobRewardedAd() != null) {
            ahAdMobRewardedAd.getAdMobRewardedAd().setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    ahAdMobRewardedAd.setRewardedAd(null);
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.d(
                            "LOG",
                            "**************************** Rewarded Add Dismissed ****************************"
                    );

                    // You may load new add
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AppHarbr.removeRewardedAd(ahAdMobRewardedAd);
    }

    private final AHAnalyze ahListener = new AHAnalyze() {
        @Override
        public void onAdBlocked(@Nullable AdIncidentInfo adIncidentInfo) {
            Log.d("LOG", "AppHarbr - onAdBlocked for: "
                    + (adIncidentInfo != null && adIncidentInfo.getUnitId() != null ? adIncidentInfo.getUnitId() : "null")
                    + ", reason: "
                    + (adIncidentInfo != null && adIncidentInfo.getBlockReasons() != null ? Arrays.toString(adIncidentInfo.getBlockReasons()) : "null"));

            if (adIncidentInfo != null && adIncidentInfo.getShouldLoadNewAd()) {
                // If add was blocked before being displayed, load new add
            }
        }

        @Override
        public void onAdIncident(@Nullable AdIncidentInfo adIncidentInfo) {
            Log.d("LOG", "AppHarbr - onAdIncident for: "
                    + (adIncidentInfo != null && adIncidentInfo.getUnitId() != null ? adIncidentInfo.getUnitId() : "null")
                    + ", reason: "
                    + (adIncidentInfo != null && adIncidentInfo.getReportReasons() != null ? Arrays.toString(adIncidentInfo.getReportReasons()) : "null"));
        }

        @Override
        public void onAdAnalyzed(@Nullable AdAnalyzedInfo adAnalyzedInfo) {
            Log.d("LOG", "AppHarbr - onAdAnalyzed for: "
                    + (adAnalyzedInfo != null && adAnalyzedInfo.getUnitId() != null ? adAnalyzedInfo.getUnitId() : "null")
                    + ", result: "
                    + (adAnalyzedInfo != null && adAnalyzedInfo.getAnalyzedResult() != null ? adAnalyzedInfo.getAnalyzedResult() : "null"));

        }
    };

    private final RewardedAdLoadCallback mRewardedLoadCallback = new RewardedAdLoadCallback() {

        @Override
        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
            super.onAdLoaded(rewardedAd);
            Log.e("TAG", "AdMob - RewardedAdLoadCallback - onAdLoaded");
            btnDisplay.setEnabled(true);
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            super.onAdFailedToLoad(loadAdError);
            Log.e("TAG", "AdMob - RewardedAdLoadCallback - onAdFailedToLoad: " + loadAdError.getMessage());
        }
    };
}