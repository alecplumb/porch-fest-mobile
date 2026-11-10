package org.towerporchfest.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.towerporchfest.app.theme.PorchFestTheme
import org.towerporchfest.app.ui.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PorchFestTheme { AppNavigation() }
        }
    }
}
