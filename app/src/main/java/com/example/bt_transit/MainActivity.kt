package com.example.bt_transit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.bt_transit.ui.navigation.AppNavigation
import com.example.bt_transit.ui.theme.BT_TransitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BT_TransitTheme {
                AppNavigation()
            }
        }
    }
}
