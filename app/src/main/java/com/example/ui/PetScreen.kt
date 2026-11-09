package com.example.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.data.PetPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PetScreen(
    viewModel: PetViewModel = viewModel(),
    onNavigateToControlCenter: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val squashState = rememberPetSquashState()
    val gestureScope = rememberCoroutineScope()

    val petPreferences = remember(context) { PetPreferences.getInstance(context) }
    val settings by petPreferences.settings.collectAsState()

    // Configure Coil ImageLoader with native ImageDecoder on Android 10+ (API 28+)
    // to preserve transparent background and support hardware-accelerated playback
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    // Build the request with memory cache key tied to animationKey to ensure clean resets on state change
    val imageRequest = remember(uiState.currentAnimation, uiState.animationKey) {
        ImageRequest.Builder(context)
            .data(uiState.currentAnimation.assetPath)
            .memoryCacheKey("${uiState.currentAnimation.id}_${uiState.animationKey}")
            .crossfade(false)
            .build()
    }

    // Fullscreen pure AMOLED black #000000 container with zero extraneous UI elements
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("pet_fullscreen_container"),
        contentAlignment = Alignment.Center
    ) {
        // Precise dimensional math preserving 220:124 aspect ratio without clipping or letterboxing
        val ratio = 220f / 124f
        val maxAvailableWidth = maxWidth * 0.94f
        val maxAvailableHeight = maxHeight * 0.44f

        val (baseWidth, baseHeight) = if (maxAvailableWidth / ratio <= maxAvailableHeight) {
            maxAvailableWidth to (maxAvailableWidth / ratio)
        } else {
            (maxAvailableHeight * ratio) to maxAvailableHeight
        }

        // Apply real-time user configured scale factor (0.7x ~ 1.5x)
        val currentWidth = baseWidth * settings.petScale
        val currentHeight = baseHeight * settings.petScale

        Box(
            modifier = Modifier
                .size(width = currentWidth, height = currentHeight)
                .pointerInput(onNavigateToControlCenter, settings) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startPosition = down.position
                        val touchSlop = viewConfiguration.touchSlop
                        var isLongPressTriggered = false
                        var pointerMovedBeyondSlop = false

                        // Start 1000ms timer for long press to enter Control Center
                        val timerJob = gestureScope.launch {
                            delay(1000L)
                            if (!pointerMovedBeyondSlop) {
                                isLongPressTriggered = true
                                if (settings.hapticEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onNavigateToControlCenter()
                            }
                        }

                        while (true) {
                            val event = awaitPointerEvent()
                            val currentPointer = event.changes.firstOrNull { it.id == down.id }
                            if (currentPointer == null || !currentPointer.pressed) {
                                // Finger released or cancelled
                                timerJob.cancel()
                                if (!isLongPressTriggered && !pointerMovedBeyondSlop) {
                                    // Regular tap: trigger squash & interaction animation per user settings
                                    if (settings.hapticEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    if (settings.squashEnabled) {
                                        squashState.triggerSquash()
                                    }
                                    viewModel.onPetClicked()
                                }
                                break
                            } else {
                                val distance = (currentPointer.position - startPosition).getDistance()
                                if (distance > touchSlop) {
                                    pointerMovedBeyondSlop = true
                                    timerJob.cancel()
                                }
                            }
                        }
                    }
                }
                .graphicsLayer {
                    // Physical Squash & Stretch hardware layer transform:
                    // Grounded near belly/bottom (0.5f, 0.70f) for realistic contact physics
                    scaleX = squashState.scaleX.value
                    scaleY = squashState.scaleY.value
                    translationY = squashState.offsetY.value
                    transformOrigin = TransformOrigin(0.5f, 0.70f)
                }
                .testTag("pet_touch_target"),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageRequest,
                imageLoader = imageLoader,
                contentDescription = "BigFish Pet",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("pet_animation_view")
            )
        }
    }
}
