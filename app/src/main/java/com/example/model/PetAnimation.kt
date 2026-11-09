package com.example.model

/**
 * Definition of animations mapped directly to official dsh-pet preview assets.
 */
data class PetAnimation(
    val id: String,
    val displayName: String,
    val filename: String,
    val durationMs: Long = 10_000L,
    val isLooping: Boolean = false
) {
    val assetPath: String
        get() = "file:///android_asset/pet/$filename"

    companion object {
        // Idle animation (looping)
        val IDLE = PetAnimation(
            id = "idle",
            displayName = "待机呼吸",
            filename = "daiji-huxi-xiuxian.gif",
            durationMs = 10_000L,
            isLooping = true
        )

        // Single tap interaction pool
        val HAPPY = PetAnimation(
            id = "happy",
            displayName = "开心跃动",
            filename = "dianji-huiying-kaixin-yuedong.gif",
            durationMs = 10_000L,
            isLooping = false
        )

        val WAVE = PetAnimation(
            id = "wave",
            displayName = "元气挥手",
            filename = "dianji-huiying-yuanqi-huishou.gif",
            durationMs = 10_000L,
            isLooping = false
        )

        val SHY = PetAnimation(
            id = "shy",
            displayName = "害羞惊讶",
            filename = "dianji-huiying-haixiu-jingya.gif",
            durationMs = 10_000L,
            isLooping = false
        )

        // Multi-tap / Combo interaction pool
        val TICKLE = PetAnimation(
            id = "tickle",
            displayName = "挠痒咯咯笑",
            filename = "dianji-huiying-naoyang-gegexiao.gif",
            durationMs = 10_000L,
            isLooping = false
        )

        val TSUNDERE = PetAnimation(
            id = "tsundere",
            displayName = "傲娇生气",
            filename = "dianji-huiying-aojiao-shengqi-ceshen-zhanshi.gif",
            durationMs = 10_000L,
            isLooping = false
        )

        val STARTLED = PetAnimation(
            id = "startled",
            displayName = "炸毛一跳",
            filename = "beixiayitiao-zhamao.gif",
            durationMs = 10_000L,
            isLooping = false
        )

        val STRETCH = PetAnimation(
            id = "stretch",
            displayName = "超大伸懒腰",
            filename = "chaoda-shenlanyao.gif",
            durationMs = 10_000L,
            isLooping = false
        )

        val RUBIK = PetAnimation(
            id = "rubik",
            displayName = "玩魔方",
            filename = "yuandi-zhuanxin-wan-mofang.gif",
            durationMs = 10_000L,
            isLooping = false
        )

        val SINGLE_TAP_POOL = listOf(HAPPY, WAVE, SHY)
        val COMBO_POOL = listOf(TICKLE, TSUNDERE, STARTLED, STRETCH, RUBIK)
    }
}
