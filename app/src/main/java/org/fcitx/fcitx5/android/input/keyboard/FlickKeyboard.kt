/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.keyboard

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.view.allViews
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.core.FcitxKeyMapping
import org.fcitx.fcitx5.android.core.InputMethodEntry
import org.fcitx.fcitx5.android.core.KeyStates
import org.fcitx.fcitx5.android.core.KeySym
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.input.keyboard.CustomGestureView.GestureType
import org.fcitx.fcitx5.android.input.keyboard.CustomGestureView.OnGestureListener
import org.fcitx.fcitx5.android.input.popup.PopupAction
import splitties.dimensions.dp
import splitties.views.imageResource

@SuppressLint("ViewConstructor")
class FlickKeyboard(
    context: Context,
    theme: Theme
) : BaseKeyboard(context, theme, Layout) {

    companion object {
        const val Name = "Flick"

        private const val KANA_WIDTH = 0.2f
        private const val FUNC_WIDTH = 0.15f
        private const val SIDE_WIDTH = 0.1f

        val Layout: List<List<KeyDef>> = listOf(
            // Row 0: [←] [あ] [か] [さ] [Backspace]
            listOf(
                SymbolKey("←", SIDE_WIDTH, KeyDef.Appearance.Variant.Alternative),
                FlickKanaPresets.a(R.id.flick_key_a),
                FlickKanaPresets.ka(R.id.flick_key_ka),
                FlickKanaPresets.sa(R.id.flick_key_sa),
                BackspaceKey(FUNC_WIDTH)
            ),
            // Row 1: [→] [た] [な] [は] [小゛゜]
            listOf(
                SymbolKey("→", SIDE_WIDTH, KeyDef.Appearance.Variant.Alternative),
                FlickKanaPresets.ta(R.id.flick_key_ta),
                FlickKanaPresets.na(R.id.flick_key_na),
                FlickKanaPresets.ha(R.id.flick_key_ha),
                FlickKanaPresets.toggle(R.id.flick_key_toggle)
            ),
            // Row 2: [ABC] [ま] [や] [ら] [?123]
            listOf(
                LayoutSwitchKey("?123", "", SIDE_WIDTH),
                FlickKanaPresets.ma(R.id.flick_key_ma),
                FlickKanaPresets.ya(R.id.flick_key_ya),
                FlickKanaPresets.ra(R.id.flick_key_ra),
                SpaceKey(),
            ),
            // Row 3: [🌐] [わ] [Space] [。] [Return]
            listOf(
                LayoutSwitchKey("ABC", TextKeyboard.Name, SIDE_WIDTH),
                LanguageKey(),
                FlickKanaPresets.wa(R.id.flick_key_wa),
                SymbolKey("。", KANA_WIDTH),
                ReturnKey(FUNC_WIDTH)
            )
        )

        /**
         * All FlickKeyDef instances in Layout, indexed by their viewId.
         */
        private val flickKeyDefs: Map<Int, FlickKeyDef> by lazy {
            Layout.flatten()
                .filterIsInstance<FlickKeyDef>()
                .associateBy { (it.appearance as KeyDef.Appearance.Text).viewId }
        }
    }

    val `return`: ImageKeyView by lazy { findViewById(R.id.button_return) }
    val space: TextKeyView by lazy { findViewById(R.id.button_space) }

    private val popupOnKeyPress by AppPrefs.getInstance().keyboard.popupOnKeyPress
    private val flickThreshold = dp(24f)

    init {
        setupFlickGestures()
    }

    /**
     * Iterates over all KeyViews and configures 4-directional flick gesture listeners
     * for those corresponding to FlickKeyDef.
     */
    private fun setupFlickGestures() {
        allViews.filterIsInstance<KeyView>().forEach { keyView ->
            val flickDef = flickKeyDefs[keyView.id] ?: return@forEach
            keyView.swipeEnabled = true
            keyView.swipeThresholdX = flickThreshold
            keyView.swipeThresholdY = flickThreshold
            keyView.swipeRepeatEnabled = false

            keyView.onGestureListener = OnGestureListener { view, event ->
                view as KeyView
                when (event.type) {
                    GestureType.Down -> {
                        if (popupOnKeyPress) {
                            onPopupAction(
                                PopupAction.PreviewAction(
                                    view.id,
                                    flickDef.label,
                                    view.bounds
                                )
                            )
                        }
                        false
                    }
                    GestureType.Move -> {
                        if (popupOnKeyPress) {
                            val dir = FlickDirection.resolve(event.totalX, event.totalY)
                            val char = flickDef.charForDirection(dir) ?: flickDef.label
                            onPopupAction(
                                PopupAction.PreviewUpdateAction(view.id, char)
                            )
                        }
                        // Consume the gesture if finger has moved to prevent performClick
                        event.totalX != 0 || event.totalY != 0
                    }
                    GestureType.Up -> {
                        if (popupOnKeyPress) {
                            onPopupAction(PopupAction.DismissAction(view.id))
                        }
                        val dir = FlickDirection.resolve(event.totalX, event.totalY)
                        if (dir != FlickDirection.Center) {
                            val char = flickDef.charForDirection(dir)
                                ?: return@OnGestureListener false
                            onAction(KeyAction.FcitxKeyAction(char))
                            true // Consume: prevent default Press click
                        } else {
                            false // Let Behavior.Press handle the tap
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle ← and → symbol keys for cursor movement.
     */
    override fun onAction(action: KeyAction, source: KeyActionListener.Source) {
        when (action) {
            is KeyAction.FcitxKeyAction -> {
                when (action.act) {
                    "←" -> {
                        super.onAction(
                            KeyAction.SymAction(
                                KeySym(FcitxKeyMapping.FcitxKey_Left),
                                KeyStates.Virtual
                            ),
                            source
                        )
                        return
                    }
                    "→" -> {
                        super.onAction(
                            KeyAction.SymAction(
                                KeySym(FcitxKeyMapping.FcitxKey_Right),
                                KeyStates.Virtual
                            ),
                            source
                        )
                        return
                    }
                }
            }
            else -> {}
        }
        super.onAction(action, source)
    }

    override fun onReturnDrawableUpdate(@DrawableRes returnDrawable: Int) {
        `return`.img.imageResource = returnDrawable
    }

    override fun onInputMethodUpdate(ime: InputMethodEntry) {
        space.mainText.text = buildString {
            append(ime.displayName)
            ime.subMode.run { label.ifEmpty { name.ifEmpty { null } } }?.let { append(" ($it)") }
        }
    }
}
