package com.example.p2pappmovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.p2pappmovil.presentation.navigation.AppNavigation
import com.example.p2pappmovil.ui.theme.P2PAppMovilTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            P2PAppMovilTheme {
                AppNavigation()
            }
        }
    }
}