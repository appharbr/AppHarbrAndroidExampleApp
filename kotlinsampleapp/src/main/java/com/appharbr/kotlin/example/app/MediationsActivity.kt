package com.appharbr.kotlin.example.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.appharbr.kotlin.example.app.admob.AdmobMediationActivity
import com.appharbr.kotlin.example.app.gam.GamMediationActivity
import com.appharbr.kotlin.example.app.level_play.LevelPlayInterstitialActivity
import com.appharbr.kotlin.example.app.max.MaxMediationActivity
import com.appharbr.kotlin.example.app.prebid.PrebidGamMenuActivity
import com.appharbr.kotlin.example.app.ui.theme.AppHarbrExampleAppTheme

class MediationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppHarbrExampleAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(
                            15.dp,
                            Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        MediationButton("Google Ad Manager (GAM)", GamMediationActivity::class.java)
                        MediationButton("Admob", AdmobMediationActivity::class.java)
                        MediationButton("Max", MaxMediationActivity::class.java)
                        MediationButton("Prebid Gam", PrebidGamMenuActivity::class.java)
                        MediationButton("Level Play", LevelPlayInterstitialActivity::class.java)

                    }
                }
            }
        }
    }

    @Composable
    private fun MediationButton(
        mediationName: String,
        activityClass: Class<*>
    ) {
        val context = LocalContext.current
        Button(
            onClick = {
                context.startActivity(
                    Intent(
                        context,
                        activityClass
                    )
                )
            }) {
            Text(text = mediationName)
        }
    }

}