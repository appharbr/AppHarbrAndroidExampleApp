package com.appharbr.example.app.max;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appharbr.example.app.databinding.ActivityMaxRecyclerViewBinding;
import com.appharbr.example.app.databinding.RecyclerViewItemBannerBinding;
import com.appharbr.sdk.engine.mediators.max.nativead.AHMaxRecyclerAdapterWrapper;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.nativeAds.adPlacer.MaxAdPlacer;
import com.applovin.mediation.nativeAds.adPlacer.MaxAdPlacerSettings;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;
import com.applovin.sdk.AppLovinSdkUtils;

import org.jetbrains.annotations.NotNull;

public class MaxNativeRecyclerViewActivity extends AppCompatActivity {

    ActivityMaxRecyclerViewBinding binding;
    private AHMaxRecyclerAdapterWrapper ahMaxRecyclerAdapter;

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
            setupRecyclerView();
        });

    }

    private void setupRecyclerView() {
        //Regular Max mediation stuff
        MaxAdPlacerSettings settings = new MaxAdPlacerSettings("YOUR_AD_UNIT_ID");
        settings.addFixedPosition(2);
        settings.setRepeatingInterval(3);

        //	**** (2) ****
        //Appharbr only requires AHMaxRecyclerAdapterWrapper to scan loaded Ads
        ahMaxRecyclerAdapter = new AHMaxRecyclerAdapterWrapper(
                settings,
                new AdRecyclerViewAdapter(), //regular adapter. (Review step 2)
                this);

        ahMaxRecyclerAdapter.setListener(new MaxAdPlacer.Listener() {
            public void onAdLoaded(int i) {
            }

            public void onAdRemoved(int i) {
            }

            public void onAdClicked(MaxAd maxAd) {
            }

            public void onAdRevenuePaid(MaxAd maxAd) {
            }
        });


        ahMaxRecyclerAdapter.getAdPlacer().setAdSize(-1, AppLovinSdkUtils.dpToPx(this, 200));
        binding.adRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //	**** (4) ****
        // Set ahMaxRecyclerAdapter created above (Step 1) instead of regular one
        binding.adRecyclerView.setAdapter(ahMaxRecyclerAdapter);

        //	**** (5) ****
        // Load Ads
        ahMaxRecyclerAdapter.loadAds();
    }

    //	**** (3) ****
    //Create regular adapter
    private static class AdRecyclerViewAdapter extends RecyclerView.Adapter<AdRecyclerViewAdapter.AdAdapterViewHolder> {

        static class AdAdapterViewHolder extends RecyclerView.ViewHolder {

            public AdAdapterViewHolder(@NotNull RecyclerViewItemBannerBinding itemBinding) {
                super(itemBinding.getRoot());
            }
        }

        @NotNull
        public AdAdapterViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            RecyclerViewItemBannerBinding binding = RecyclerViewItemBannerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new AdAdapterViewHolder(binding);
        }

        public void onBindViewHolder(@NotNull AdAdapterViewHolder holder, int position) {
            //Regular bind stuff. bind your item here
        }

        public int getItemCount() {
            return 10;
        }

    }

    public void onDestroy() {
        //According to Max's integration doc, to call destroy is necessary to avoid memory leaks
        ahMaxRecyclerAdapter.destroy();
        super.onDestroy();
    }
}