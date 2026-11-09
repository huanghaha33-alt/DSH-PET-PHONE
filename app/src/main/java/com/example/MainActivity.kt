package com.example

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PetPreferences
import com.example.model.AppScreen
import com.example.ui.ControlCenterScreen
import com.example.ui.PetScreen
import com.example.ui.PetViewModel
import com.example.ui.ProjectSettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private var activeScreen: AppScreen = AppScreen.PET

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge drawing
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            var currentScreen by rememberSaveable { mutableStateOf(AppScreen.PET) }
            activeScreen = currentScreen

            val petPreferences = remember { PetPreferences.getInstance(applicationContext) }
            val settings by petPreferences.settings.collectAsState()

            // Dynamic screen wake lock based on user settings
            LaunchedEffect(settings.keepScreenOn) {
                if (settings.keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            // Create shared PetViewModel bound to PetPreferences
            val petViewModel: PetViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return PetViewModel(petPreferences = petPreferences) as T
                    }
                }
            )

            // Dynamically synchronize system bars styling with active screen
            LaunchedEffect(currentScreen) {
                updateSystemBarsForScreen(currentScreen)
            }

            MyApplicationTheme {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        // 250ms smooth crossfade between pet screen and control center
                        fadeIn(animationSpec = tween(250)) togetherWith
                                fadeOut(animationSpec = tween(250))
                    },
                    label = "screen_navigation_transition"
                ) { screen ->
                    when (screen) {
                        AppScreen.PET -> {
                            PetScreen(
                                viewModel = petViewModel,
                                onNavigateToControlCenter = {
                                    currentScreen = AppScreen.CONTROL_CENTER
                                }
                            )
                        }
                        AppScreen.CONTROL_CENTER -> {
                            ControlCenterScreen(
                                onNavigateToSettings = {
                                    currentScreen = AppScreen.SETTINGS
                                },
                                onNavigateBackToPet = {
                                    currentScreen = AppScreen.PET
                                }
                            )
                        }
                        AppScreen.SETTINGS -> {
                            ProjectSettingsScreen(
                                onNavigateBackToControlCenter = {
                                    currentScreen = AppScreen.CONTROL_CENTER
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && activeScreen == AppScreen.PET) {
            hideSystemBarsForPet()
        }
    }

    override fun onResume() {
        super.onResume()
        if (activeScreen == AppScreen.PET) {
            hideSystemBarsForPet()
        }
    }

    private fun updateSystemBarsForScreen(screen: AppScreen) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (screen == AppScreen.PET) {
            window.decorView.setBackgroundColor(Color.BLACK)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            // Light background for Control Center & Settings
            window.decorView.setBackgroundColor(Color.parseColor("#F8F9FA"))
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun hideSystemBarsForPet() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}
