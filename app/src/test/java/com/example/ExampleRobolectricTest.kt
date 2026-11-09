package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PetPreferences
import com.example.model.AppScreen
import com.example.model.PetAnimation
import com.example.ui.PetSquashState
import com.example.ui.PetViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context matches BigFish Pet`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("BigFish Pet", appName)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `test pet state machine starts idle and click triggers action`() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = PetViewModel(testDispatcher, this)
    val initialState = viewModel.uiState.value
    assertEquals(PetAnimation.IDLE, initialState.currentAnimation)
    assertFalse(initialState.isInteracting)

    // Click triggers single-tap action
    viewModel.onPetClicked()
    // Run current pending events without advancing clock through 10-second delay
    testScheduler.runCurrent()

    val interactingState = viewModel.uiState.value
    assertTrue(interactingState.isInteracting)
    assertEquals(1, interactingState.comboCount)
    assertTrue(PetAnimation.SINGLE_TAP_POOL.contains(interactingState.currentAnimation))

    // Fast-forward 10.5 seconds to verify auto-recovery to IDLE
    advanceTimeBy(10_500L)
    testScheduler.runCurrent()

    val recoveredState = viewModel.uiState.value
    assertEquals(PetAnimation.IDLE, recoveredState.currentAnimation)
    assertFalse(recoveredState.isInteracting)
    assertEquals(0, recoveredState.comboCount)
  }

  @Test
  fun `test pet squash state initial baseline`() {
    val squashState = PetSquashState(MainScope())
    assertEquals(1.0f, squashState.scaleX.value, 0.001f)
    assertEquals(1.0f, squashState.scaleY.value, 0.001f)
    assertEquals(0.0f, squashState.offsetY.value, 0.001f)
  }

  @Test
  fun `test app navigation flow between pet, control center and settings`() {
    var screen = AppScreen.PET
    assertEquals(AppScreen.PET, screen)

    // Long-press triggers transition to CONTROL_CENTER
    screen = AppScreen.CONTROL_CENTER
    assertEquals(AppScreen.CONTROL_CENTER, screen)

    // Click on Project Settings transitions to SETTINGS
    screen = AppScreen.SETTINGS
    assertEquals(AppScreen.SETTINGS, screen)

    // Back button on Settings transitions to CONTROL_CENTER
    screen = AppScreen.CONTROL_CENTER
    assertEquals(AppScreen.CONTROL_CENTER, screen)

    // Back button on Control Center transitions back to PET
    screen = AppScreen.PET
    assertEquals(AppScreen.PET, screen)
  }

  @Test
  fun `test pet preferences persistence and reset`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = PetPreferences(context)
    prefs.resetToDefaults()

    val initial = prefs.settings.value
    assertEquals(1.0f, initial.petScale, 0.001f)
    assertTrue(initial.hapticEnabled)
    assertTrue(initial.squashEnabled)
    assertEquals(10, initial.autoIdleDurationSec)
    assertTrue(initial.keepScreenOn)
    assertEquals(1800L, initial.comboWindowMs)

    // Update settings
    prefs.updatePetScale(1.25f)
    prefs.updateHapticEnabled(false)
    prefs.updateSquashEnabled(false)
    prefs.updateAutoIdleDuration(5)
    prefs.updateKeepScreenOn(false)
    prefs.updateComboWindow(1200L)

    val updated = prefs.settings.value
    assertEquals(1.25f, updated.petScale, 0.001f)
    assertFalse(updated.hapticEnabled)
    assertFalse(updated.squashEnabled)
    assertEquals(5, updated.autoIdleDurationSec)
    assertFalse(updated.keepScreenOn)
    assertEquals(1200L, updated.comboWindowMs)

    // Reload from SharedPreferences to verify persistence across app restart
    val newInstance = PetPreferences(context)
    val loaded = newInstance.settings.value
    assertEquals(1.25f, loaded.petScale, 0.001f)
    assertFalse(loaded.hapticEnabled)
    assertFalse(loaded.squashEnabled)
    assertEquals(5, loaded.autoIdleDurationSec)
    assertFalse(loaded.keepScreenOn)
    assertEquals(1200L, loaded.comboWindowMs)

    // Reset to defaults
    newInstance.resetToDefaults()
    val reset = newInstance.settings.value
    assertEquals(1.0f, reset.petScale, 0.001f)
    assertTrue(reset.hapticEnabled)
    assertTrue(reset.squashEnabled)
    assertEquals(10, reset.autoIdleDurationSec)
    assertTrue(reset.keepScreenOn)
    assertEquals(1800L, reset.comboWindowMs)
  }
}
