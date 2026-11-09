package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Controller for physical Squash & Stretch elastic deformation feedback.
 * Simulates a squishy silicone/gel toy when touched, with immediate responsive deformation,
 * recoil jump, and spring settling.
 */
class PetSquashState(
    private val scope: CoroutineScope
) {
    val scaleX = Animatable(1.0f)
    val scaleY = Animatable(1.0f)
    val offsetY = Animatable(0.0f)

    private var squashJob: Job? = null

    companion object {
        private const val SQUASH_DURATION_MS = 80
        private const val STRETCH_DURATION_MS = 120

        // Squash phase: compressed Y, expanded X, pushed slightly down
        private const val TARGET_SQUASH_SCALE_Y = 0.84f
        private const val TARGET_SQUASH_SCALE_X = 1.14f
        private const val TARGET_SQUASH_OFFSET_Y = 22f

        // Stretch phase: elongated Y, slimmed X, pops slightly upward
        private const val TARGET_STRETCH_SCALE_Y = 1.12f
        private const val TARGET_STRETCH_SCALE_X = 0.92f
        private const val TARGET_STRETCH_OFFSET_Y = -30f
    }

    fun triggerSquash() {
        // Cancel previous animation to immediately respond to rapid consecutive taps
        // without cumulative distortion or delayed response
        squashJob?.cancel()

        squashJob = scope.launch {
            // Phase 1: Impact Squash (0 -> 80ms)
            // Character flattens into the touch with volume expansion
            val squashTween = tween<Float>(durationMillis = SQUASH_DURATION_MS, easing = FastOutSlowInEasing)
            launch { scaleY.animateTo(TARGET_SQUASH_SCALE_Y, squashTween) }
            launch { scaleX.animateTo(TARGET_SQUASH_SCALE_X, squashTween) }
            launch { offsetY.animateTo(TARGET_SQUASH_OFFSET_Y, squashTween) }
            delay(SQUASH_DURATION_MS.toLong())

            // Phase 2: Elastic Stretch & Jump (80 -> 200ms)
            // Character springs upward past baseline
            val stretchTween = tween<Float>(durationMillis = STRETCH_DURATION_MS, easing = FastOutLinearInEasing)
            launch { scaleY.animateTo(TARGET_STRETCH_SCALE_Y, stretchTween) }
            launch { scaleX.animateTo(TARGET_STRETCH_SCALE_X, stretchTween) }
            launch { offsetY.animateTo(TARGET_STRETCH_OFFSET_Y, stretchTween) }
            delay(STRETCH_DURATION_MS.toLong())

            // Phase 3: Organic Spring Rebound (200 -> ~400ms)
            // Returns to exact resting baseline (1.0f, 1.0f, 0f) with a playful slight wobble
            val springSpec = spring<Float>(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
            launch { scaleY.animateTo(1.0f, springSpec) }
            launch { scaleX.animateTo(1.0f, springSpec) }
            launch { offsetY.animateTo(0.0f, springSpec) }
        }
    }
}

@Composable
fun rememberPetSquashState(
    scope: CoroutineScope = rememberCoroutineScope()
): PetSquashState {
    return remember(scope) {
        PetSquashState(scope)
    }
}
