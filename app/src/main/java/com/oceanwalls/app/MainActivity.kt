package com.oceanwalls.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.oceanwalls.app.ui.nav.OceanWallsApp
import com.oceanwalls.app.ui.theme.OceanWallsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as OceanWallsApplication).container
        setContent {
            OceanWallsTheme {
                OceanWallsApp(container = container)
            }
        }
    }
}
