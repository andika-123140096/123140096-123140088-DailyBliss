package com.dailybliss.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

/**
 * Android MainActivity
 *
 * Entry point UI untuk platform Android. Mengatur edge-to-edge
 * dan me-render App component dari shared code.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Aktifkan edge-to-edge dengan sistem bar transparan
        enableEdgeToEdge(
            statusBarStyle =
            SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ),
            navigationBarStyle =
            SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ),
        )

        setContent {
            App()
        }
    }
}
