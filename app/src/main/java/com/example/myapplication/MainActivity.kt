package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.util.PermissionHelper

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if usage access is needed for detailed per-app stats
        if (!PermissionHelper.hasUsageStatsPermission(this)) {
            // Optional: User can grant in settings when needed
        }

        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
    }
}