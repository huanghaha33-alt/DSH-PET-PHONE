package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PetPreferences
import com.example.model.PetAnimation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PetViewModel(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val externalScope: CoroutineScope? = null,
    private val petPreferences: PetPreferences? = null
) : ViewModel() {

    private val scope: CoroutineScope
        get() = externalScope ?: viewModelScope

    private val _uiState = MutableStateFlow(PetUiState())
    val uiState: StateFlow<PetUiState> = _uiState.asStateFlow()

    private var returnToIdleJob: Job? = null
    private var lastClickTimestamp = 0L
    private var singleTapIndex = 0
    private var comboIndex = 0

    companion object {
        // Fallback default combo window (1.8 seconds)
        private const val DEFAULT_COMBO_WINDOW_MS = 1800L
        // Minimum interval before switching to a new combo animation to prevent frame flashing
        private const val SWITCH_COOLDOWN_MS = 350L
    }

    private val comboWindowMs: Long
        get() = petPreferences?.settings?.value?.comboWindowMs ?: DEFAULT_COMBO_WINDOW_MS

    private val autoIdleDurationMs: Long
        get() = ((petPreferences?.settings?.value?.autoIdleDurationSec ?: 10) * 1000L)

    private var lastAnimSwitchTimestamp = 0L

    fun onPetClicked() {
        val now = System.currentTimeMillis()
        val timeSinceLastClick = now - lastClickTimestamp
        val isCombo = timeSinceLastClick < comboWindowMs
        val currentCombo = if (isCombo) _uiState.value.comboCount + 1 else 1

        lastClickTimestamp = now

        val currentState = _uiState.value

        // Determine if we should change animation or keep playing current while updating combo
        val shouldSwitchAnimation: Boolean
        val nextAnimation: PetAnimation

        if (!currentState.isInteracting) {
            // From IDLE -> Trigger interaction
            shouldSwitchAnimation = true
            nextAnimation = PetAnimation.SINGLE_TAP_POOL[singleTapIndex % PetAnimation.SINGLE_TAP_POOL.size]
            singleTapIndex++
        } else {
            // Already interacting
            val timeSinceSwitch = now - lastAnimSwitchTimestamp
            if (currentCombo >= 2 && timeSinceSwitch >= SWITCH_COOLDOWN_MS) {
                // Escalate to combo interaction
                shouldSwitchAnimation = true
                nextAnimation = PetAnimation.COMBO_POOL[comboIndex % PetAnimation.COMBO_POOL.size]
                comboIndex++
            } else {
                // Keep playing currently running interaction without interrupting/restarting frame 0
                shouldSwitchAnimation = false
                nextAnimation = currentState.currentAnimation
            }
        }

        if (shouldSwitchAnimation) {
            lastAnimSwitchTimestamp = now
            returnToIdleJob?.cancel()

            _uiState.update {
                it.copy(
                    currentAnimation = nextAnimation,
                    animationKey = System.nanoTime(),
                    comboCount = currentCombo,
                    isInteracting = true
                )
            }

            // Schedule auto-return to IDLE using the configured duration
            returnToIdleJob = scope.launch(dispatcher) {
                delay(autoIdleDurationMs)
                _uiState.update {
                    it.copy(
                        currentAnimation = PetAnimation.IDLE,
                        animationKey = System.nanoTime(),
                        comboCount = 0,
                        isInteracting = false
                    )
                }
            }
        } else {
            // Just update combo counter, let current animation continue smoothly
            _uiState.update {
                it.copy(comboCount = currentCombo)
            }
        }
    }

    fun onPetLongPressed() {
        // Reserved hook for long press
    }
}
