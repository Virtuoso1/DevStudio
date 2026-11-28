package com.homeservices.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeservices.app.data.AppViewModel
import com.homeservices.app.navigation.HomeServicesApp
import com.homeservices.app.ui.theme.HomeServicesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            val vm: AppViewModel = viewModel()
            val darkMode by vm.darkMode.collectAsStateWithLifecycle()
            HomeServicesTheme(darkTheme = darkMode) {
                HomeServicesApp(vm = vm)
            }
        }
    }
}
