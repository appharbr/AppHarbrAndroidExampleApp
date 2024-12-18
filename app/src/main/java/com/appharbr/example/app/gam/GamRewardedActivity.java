package com.appharbr.example.app.gam;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.appharbr.sdk.engine.AdResult;
import com.appharbr.sdk.engine.AdSdk;
import com.appharbr.sdk.engine.AdStateResult;
import com.appharbr.sdk.engine.AppHarbr;
import com.appharbr.sdk.engine.listeners.AHAnalyze;
import com.appharbr.sdk.engine.listeners.AdAnalyzedInfo;
import com.appharbr.sdk.engine.listeners.AdIncidentInfo;
import com.appharbr.sdk.engine.mediators.gam.rewarded.AHGamRewardedAd;
import com.appharbr.example.app.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import java.util.Arrays;

public class GamRewardedActivity extends AppCompatActivity {

    private Button btnDisplay;
    private AHGamRewardedAd ahGamRewardedAd = new AHGamRewardedAd();
    private RewardedAdLoadCallback ahWrapperListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gam_rewarded_layout);

        btnDisplay = findViewById(R.id.gam_rewarded_display);
        setDisplayClick();

        // The publisher will initiate once the listener wrapper and will use it when load the GAM Rewarded ad.
        ahWrapperListener = AppHarbr.addRewardedAd(AdSdk.GAM,
                ahGamRewardedAd,
                adManagerRewardedAdLoadCallback,
                ahListener);

        // The publisher load GAM (Google Ad Manager) Rewarded ad
        requestAd();
    }

    private void requestAd() {
        btnDisplay.setEnabled(false);
        RewardedAd.load(getApplicationContext(),
                getApplicationContext().getResources().getString(R.string.gam_rewarded_ad_unit_id),
                new AdRequest.Builder().build(),
                ahWrapperListener); //AppHarbr wrapper listener
    }

    private void setDisplayClick() {
        btnDisplay.setOnClickListener((view) -> {
            // The publisher display the GAM (Google Ad Manager) Rewarded
            if (isDestroyed()) {
                return;
            }

            setFullScreenCallBack();
            showAd();
        });
    }

    private void showAd() {
        if (ahGamRewardedAd.getGamRewardedAd() != null) {
            final AdResult rewardedResult = AppHarbr.getRewardedResult(ahGamRewardedAd);
            if (rewardedResult.adStateResult != AdStateResult.BLOCKED) {
                Log.d("LOG", "**************************** AppHarbr Permit to Display GAM Rewarded ****************************");
                ahGamRewardedAd.getGamRewardedAd().show(GamRewardedActivity.this,
                        rewardItem -> Log.d("TAG", "onUserEarnedReward: " + rewardItem));
            } else {
                Log.d("LOG", "**************************** AppHarbr Blocked GAM Rewarded ****************************");
                // You may call to reload Rewarded
            }
        } else {
            Log.d("TAG", "The GAM Rewarded wasn't loaded yet.");
        }
    }

    private void setFullScreenCallBack() {
        if (ahGamRewardedAd.getGamRewardedAd() != null) {
            ahGamRewardedAd.getGamRewardedAd().setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    ahGamRewardedAd.setRewardedAd(null);
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

//      **** (7) ****
//      Remove the view on Destroy
        AppHarbr.removeRewardedAd(ahGamRewardedAd);
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

    private final RewardedAdLoadCallback adManagerRewardedAdLoadCallback =
            new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                    Log.d("LOG", "onAdLoaded");
                    btnDisplay.setEnabled(true);
                    super.onAdLoaded(rewardedAd);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.d("LOG", "onAdFailedToLoad: " + loadAdError.getMessage());
                    super.onAdFailedToLoad(loadAdError);
                }
            };
}