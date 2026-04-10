/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.keyboard

import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.prefs.ManagedPreferenceEnum

enum class JapaneseKeyboardLayout(override val stringRes: Int) : ManagedPreferenceEnum {
    QWERTY(R.string.japanese_keyboard_qwerty),
    Flick(R.string.japanese_keyboard_flick);
}
