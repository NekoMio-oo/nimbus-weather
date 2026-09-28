package com.example.nimbus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.nimbus.ui.NimbusApp
import com.example.nimbus.ui.theme.NimbusTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Draw behind the system bars; the sky runs edge to edge and the screens inset their own content.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as NimbusApplication).container
        setContent {
            NimbusTheme {
                NimbusApp(container)
            }
        }
    }
}
