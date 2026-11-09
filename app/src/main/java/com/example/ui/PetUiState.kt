package com.example.ui

import com.example.model.PetAnimation

data class PetUiState(
    val currentAnimation: PetAnimation = PetAnimation.IDLE,
    val animationKey: Long = 0L,
    val comboCount: Int = 0,
    val isInteracting: Boolean = false
)
