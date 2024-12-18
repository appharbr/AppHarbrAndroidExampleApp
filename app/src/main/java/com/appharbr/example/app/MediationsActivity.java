package com.appharbr.example.app;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.appharbr.example.app.admob.AdMobMediationActivity;
import com.appharbr.example.app.gam.GamMediationActivity;
import com.appharbr.example.app.level_play.LevelPlayInterstitialActivity;
import com.appharbr.example.app.max.MaxMediationActivity;

public class MediationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediations);

        Button btnGam = findViewById(R.id.gam_mediation);
        btnGam.setOnClickListener((view) -> {
            startActivity(new Intent(MediationsActivity.this, GamMediationActivity.class));
        });

        Button btnAdMob = findViewById(R.id.admob_mediation);
        btnAdMob.setOnClickListener((view) -> {
            startActivity(new Intent(MediationsActivity.this, AdMobMediationActivity.class));
        });

        Button btnMax = findViewById(R.id.max_mediation);
        btnMax.setOnClickListener((view) -> {
            startActivity(new Intent(MediationsActivity.this, MaxMediationActivity.class));
        });

        Button btnLevelPlay = findViewById(R.id.level_play);
        btnLevelPlay.setOnClickListener((view) -> {
            startActivity(new Intent(MediationsActivity.this, LevelPlayInterstitialActivity.class));
        });

    }
}
