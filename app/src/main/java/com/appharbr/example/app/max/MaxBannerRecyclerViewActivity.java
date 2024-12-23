package com.appharbr.example.app.max;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.appharbr.example.app.databinding.ActivityMaxRecyclerViewBinding;
import com.appharbr.example.app.databinding.RecyclerViewItemBannerBinding;
import com.appharbr.sdk.engine.AdSdk;
import com.appharbr.sdk.engine.AppHarbr;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;
import com.applovin.sdk.AppLovinSdkUtils;

import org.jetbrains.annotations.NotNull;

public class MaxBannerRecyclerViewActivity extends AppCompatActivity {

    ActivityMaxRecyclerViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	binding = ActivityMaxRecyclerViewBinding.inflate(getLayoutInflater());
	setContentView(binding.getRoot());

		//	**** (1) ****
		//Initialize AppLovinSdk
		AppLovinSdkInitializationConfiguration initConfig = AppLovinSdkInitializationConfiguration.builder(
				"YOUR_API_KEY",
				this
		).setMediationProvider(AppLovinMediationProvider.MAX).build();

		AppLovinSdk.getInstance(this).initialize(initConfig, (sdkConfig) -> {
			Log.d("KotlinSample", "MAX mediation initialized successfully -> [" + sdkConfig + ']');
			loadBannerAd(binding);
		});
    }

	private void loadBannerAd(ActivityMaxRecyclerViewBinding binding) {
		//	**** (2) ****
		//Create Adapter and Setup recyclerview
		binding.adRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		binding.adRecyclerView.setAdapter(new AdRecyclerViewAdapter());
	}

    //Create adapter
    class AdRecyclerViewAdapter extends RecyclerView.Adapter<AdAdapterViewHolder> {

	@NotNull
	public AdAdapterViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
	    RecyclerViewItemBannerBinding binding = RecyclerViewItemBannerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
	    return new AdAdapterViewHolder(binding);
	}


	public void onBindViewHolder(@NotNull AdAdapterViewHolder holder, int position) {
	    holder.bindHolder();
	}

	public void onViewRecycled(@NotNull AdAdapterViewHolder holder) {
	    super.onViewRecycled(holder);

	    //	**** (2) ****
	    //Clear banner to avoid memory leaks
	    holder.clearBanner();
	}

	public int getItemCount() {
	    return 10;
	}

    }

    //Create ViewHolder
    class AdAdapterViewHolder extends RecyclerView.ViewHolder {

	private final RecyclerViewItemBannerBinding itemBinding;

	//	**** (3) ****
	//Create banner field to clear when RecyclerView's onViewRecycled is called to avoid memory leaks
	private MaxAdView currentBannerAdView;

	public AdAdapterViewHolder(@NotNull RecyclerViewItemBannerBinding itemBinding) {
	    super(itemBinding.getRoot());
	    this.itemBinding = itemBinding;
	}

	//	**** (4) ****
	//Create banner manually
	public final void bindHolder() {
	    //MaxAdView creation manually
	    currentBannerAdView = new MaxAdView("YOUR_AD_UNIT_ID", itemView.getContext());

	    //Defining height of item according to banner height
	    int heightDp = MaxAdFormat.BANNER.getAdaptiveSize(MaxBannerRecyclerViewActivity.this).getHeight();
	    int heightPx = AppLovinSdkUtils.dpToPx(MaxBannerRecyclerViewActivity.this, heightDp);
	    itemBinding.itemLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx));

	    currentBannerAdView.setExtraParameter("adaptive_banner", "true");

	    //Setting listener mandatory for AppHarbr to scan loaded Ad
	    currentBannerAdView.setListener(new MaxAdViewAdListener() {
		public void onAdExpanded(@NonNull MaxAd ad) {}

		public void onAdCollapsed(@NonNull MaxAd ad) {}

		public void onAdLoaded(@NonNull MaxAd ad) {}

		public void onAdDisplayed(@NonNull MaxAd ad) {}

		public void onAdHidden(@NonNull MaxAd ad) {}

		public void onAdClicked(@NonNull MaxAd ad) {}

		public void onAdLoadFailed(@NonNull String adUnitId, @NonNull MaxError error) {}

		public void onAdDisplayFailed(@NonNull MaxAd ad, @NonNull MaxError error) {}
	    });

	    //Add Max's banner adView instance for Monitoring
	    //Adding Lifecycle is important to avoid memory leaks
	    AppHarbr.addBannerView(AdSdk.MAX, currentBannerAdView, getLifecycle(), null);

	    //Load banner
	    currentBannerAdView.loadAd();

	    itemBinding.itemLayout.addView(currentBannerAdView);
	}

	public final void clearBanner() {
	    if (currentBannerAdView != null) {
		//	**** (5) ****
		//This step is necessary to avoid memory leaks
		AppHarbr.removeBannerView(currentBannerAdView);
	    }
	    this.currentBannerAdView = null;
	}

	//Crating AHListener to get result if Ad was blocked and why
//	private final AHListener ahListener = (view, unitId, adFormat, reasons)
//		-> Log.d("LOG", "AppHarbr - onAdBlocked for: " + unitId + ", reason: " + Arrays.toString(reasons));
//
    }

}