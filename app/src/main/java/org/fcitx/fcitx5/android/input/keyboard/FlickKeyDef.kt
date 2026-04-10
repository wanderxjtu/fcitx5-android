/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.keyboard

import kotlin.math.abs

enum class FlickDirection {
    Center, Left, Up, Right, Down;

    companion object {
        fun resolve(totalX: Int, totalY: Int): FlickDirection {
            if (totalX == 0 && totalY == 0) return Center
            return if (abs(totalX) >= abs(totalY)) {
                if (totalX < 0) Left else Right
            } else {
                if (totalY < 0) Up else Down
            }
        }
    }
}

class FlickKeyDef(
    val label: String,
    val flickMap: Map<FlickDirection, String>,
    percentWidth: Float = 0.2f,
    viewId: Int = -1
) : KeyDef(
    Appearance.Text(
        displayText = label,
        textSize = 23f,
        percentWidth = percentWidth,
        viewId = viewId
    ),
    setOf(
        Behavior.Press(KeyAction.FcitxKeyAction(flickMap[FlickDirection.Center]!!))
    ),
    popup = null
) {
    fun charForDirection(direction: FlickDirection): String? = flickMap[direction]
}

/**
 * Predefined flick key definitions for Japanese kana input.
 */
object FlickKanaPresets {

    private fun kana(
        label: String,
        center: String,
        left: String,
        up: String,
        right: String,
        down: String,
        percentWidth: Float = 0.2f,
        viewId: Int = -1
    ) = FlickKeyDef(
        label = label,
        flickMap = mapOf(
            FlickDirection.Center to center,
            FlickDirection.Left to left,
            FlickDirection.Up to up,
            FlickDirection.Right to right,
            FlickDirection.Down to down
        ),
        percentWidth = percentWidth,
        viewId = viewId
    )

    fun a(viewId: Int) = kana("あ", "あ", "い", "う", "え", "お", viewId = viewId)
    fun ka(viewId: Int) = kana("か", "か", "き", "く", "け", "こ", viewId = viewId)
    fun sa(viewId: Int) = kana("さ", "さ", "し", "す", "せ", "そ", viewId = viewId)
    fun ta(viewId: Int) = kana("た", "た", "ち", "つ", "て", "と", viewId = viewId)
    fun na(viewId: Int) = kana("な", "な", "に", "ぬ", "ね", "の", viewId = viewId)
    fun ha(viewId: Int) = kana("は", "は", "ひ", "ふ", "へ", "ほ", viewId = viewId)
    fun ma(viewId: Int) = kana("ま", "ま", "み", "む", "め", "も", viewId = viewId)
    fun ya(viewId: Int) = kana("や", "や", "（", "ゆ", "）", "よ", viewId = viewId)
    fun ra(viewId: Int) = kana("ら", "ら", "り", "る", "れ", "ろ", viewId = viewId)
    fun wa(viewId: Int) = kana("わ", "わ", "を", "ん", "ー", "〜", viewId = viewId)
    fun symbol(viewId: Int) = kana("、", "、", "。", "？", "！", "……", viewId = viewId)

    /**
     * Toggle key for dakuten (゛), handakuten (゜), and small kana (小).
     * Center = ゛, Left = ゜, Up = 小 (sends xtu for small-tsu behavior), Right = ゛, Down = ゜
     */
    fun toggle(viewId: Int) = kana("小゛゜", "゛", "゜", "小", "゛", "゜",
        percentWidth = 0.2f, viewId = viewId)
}
