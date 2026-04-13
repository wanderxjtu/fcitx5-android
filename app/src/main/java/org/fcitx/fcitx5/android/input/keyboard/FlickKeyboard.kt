/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.inputmethodservice.InputMethodService
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
class FlickKeyboard(context: Context, theme: Theme) : BaseKeyboard(context, theme, Layout) {

    companion object {
        const val Name = "Flick"

        private const val KANA_WIDTH = 0.2f
        private const val FUNC_WIDTH = 0.2f
        private const val SIDE_WIDTH = 0.2f

        val Layout: List<List<KeyDef>> =
            listOf(
                // Row 0: [←] [あ] [か] [さ] [Backspace]
                listOf(
                    SymbolKey("←", SIDE_WIDTH, KeyDef.Appearance.Variant.Alternative),
                    FlickKanaPresets.a(R.id.flick_key_a),
                    FlickKanaPresets.ka(R.id.flick_key_ka),
                    FlickKanaPresets.sa(R.id.flick_key_sa),
                    BackspaceKey(FUNC_WIDTH),
                ),
                // Row 1: [→] [た] [な] [は] [小゛゜]
                listOf(
                    SymbolKey("→", SIDE_WIDTH, KeyDef.Appearance.Variant.Alternative),
                    FlickKanaPresets.ta(R.id.flick_key_ta),
                    FlickKanaPresets.na(R.id.flick_key_na),
                    FlickKanaPresets.ha(R.id.flick_key_ha),
                    SymbolKey("ﾞﾟ", SIDE_WIDTH, KeyDef.Appearance.Variant.Alternative),
                ),
                // Row 2: [ABC] [ま] [や] [ら] [?123]
                listOf(
                    LayoutSwitchKey("?123", NumberKeyboard.Name, SIDE_WIDTH),
                    FlickKanaPresets.ma(R.id.flick_key_ma),
                    FlickKanaPresets.ya(R.id.flick_key_ya),
                    FlickKanaPresets.ra(R.id.flick_key_ra),
                    SpaceKey(),
                ),
                // Row 3: [🌐] [わ] [Space] [。] [Return]
                listOf(
                    LayoutSwitchKey("ABC", TextKeyboard.Name, SIDE_WIDTH),
                    LanguageKey(SIDE_WIDTH),
                    FlickKanaPresets.wa(R.id.flick_key_wa),
                    FlickKanaPresets.symbol(R.id.flick_key_symbol),
                    ReturnKey(FUNC_WIDTH),
                ),
            )

        /** All FlickKeyDef instances in Layout, indexed by their viewId. */
        private val flickKeyDefs: Map<Int, FlickKeyDef> by lazy {
            Layout.flatten().filterIsInstance<FlickKeyDef>().associateBy {
                (it.appearance as KeyDef.Appearance.Text).viewId
            }
        }
    }

    val `return`: ImageKeyView by lazy { findViewById(R.id.button_return) }
    val space: TextKeyView by lazy { findViewById(R.id.button_space) }

    private val popupOnKeyPress by AppPrefs.getInstance().keyboard.popupOnKeyPress
    private val flickThreshold = dp(24f)
    private var lastKana: String? = null

    init {
        setupFlickGestures()
    }

    private fun getNextToggledChar(c: String): String? {
        return when (c) {
            // 平假名变换
            "あ" -> "ぁ" ; "ぁ" -> "あ"
            "い" -> "ぃ" ; "ぃ" -> "い"
            "う" -> "ぅ" ; "ぅ" -> "ゔ" ; "ゔ" -> "う"
            "え" -> "ぇ" ; "ぇ" -> "え"
            "お" -> "ぉ" ; "ぉ" -> "お"
            "か" -> "が" ; "加" -> "か"
            "き" -> "ぎ" ; "ぎ" -> "き"
            "く" -> "ぐ" ; "ぐ" -> "く"
            "け" -> "げ" ; "げ" -> "け"
            "こ" -> "ご" ; "ご" -> "こ"
            "さ" -> "ざ" ; "ざ" -> "さ"
            "し" -> "じ" ; "じ" -> "し"
            "す" -> "ず" ; "ず" -> "す"
            "せ" -> "ぜ" ; "ぜ" -> "せ"
            "そ" -> "ぞ" ; "ぞ" -> "そ"
            "た" -> "だ" ; "だ" -> "た"
            "ち" -> "ぢ" ; "ぢ" -> "ち"
            "つ" -> "っ" ; "っ" -> "づ" ; "づ" -> "つ"
            "て" -> "で" ; "で" -> "て"
            "と" -> "ど" ; "ど" -> "と"
            "は" -> "ば" ; "ば" -> "ぱ" ; "ぱ" -> "は"
            "ひ" -> "び" ; "び" -> "ぴ" ; "ぴ" -> "ひ"
            "ふ" -> "ぶ" ; "ぶ" -> "ぷ" ; "ぷ" -> "ふ"
            "へ" -> "べ" ; "べ" -> "ぺ" ; "ぺ" -> "へ"
            "ほ" -> "ぼ" ; "ぼ" -> "ぽ" ; "ぽ" -> "ほ"
            "や" -> "ゃ" ; "ゃ" -> "や"
            "ゆ" -> "ゅ" ; "ゅ" -> "ゆ"
            "よ" -> "ょ" ; "ょ" -> "よ"
            "わ" -> "ゎ" ; "ゎ" -> "わ"
            else -> null
        }
    }

    /**
     * Iterates over all KeyViews and configures 4-directional flick gesture listeners for those
     * corresponding to FlickKeyDef.
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
                                PopupAction.PreviewAction(view.id, flickDef.label, view.bounds)
                            )
                        }
                        false
                    }
                    GestureType.Move -> {
                        if (popupOnKeyPress) {
                            val dir = FlickDirection.resolve(event.totalX, event.totalY)
                            val char = flickDef.charForDirection(dir) ?: flickDef.label
                            onPopupAction(PopupAction.PreviewUpdateAction(view.id, char))
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
                            val char =
                                flickDef.charForDirection(dir) ?: return@OnGestureListener false
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

    tailrec fun Context.unwrapToIMS(): InputMethodService? {
        return when (this) {
            is InputMethodService -> this
            is ContextWrapper -> baseContext.unwrapToIMS()
            else -> null
        }
    }

    private fun getCharBefore(): String? {
        // 无论套了多少层 ThemeWrapper，都能安全拿到 Service
        val ims = context.unwrapToIMS() ?: return null
        val text = ims.currentInputConnection?.getTextBeforeCursor(1, 0)?.toString()
        return text
    }

    /** Handle ← and → symbol keys for cursor movement. */
    override fun onAction(action: KeyAction, source: KeyActionListener.Source) {
        when (action) {
            is KeyAction.FcitxKeyAction -> {
                var act = action.act
                when (act) {
                    "ﾞﾟ" -> {
                        var char = lastKana
                        if (char == null) char = getCharBefore()

                        if (!char.isNullOrEmpty()) {
                            val nextChar = getNextToggledChar(char)
                            if (nextChar != null) {
                                // 1. 发送退格：利用 super 调用，确保逻辑链路完整
                                super.onAction(
                                    KeyAction.SymAction(
                                        KeySym(FcitxKeyMapping.FcitxKey_BackSpace),
                                        KeyStates.Virtual,
                                    ),
                                    source,
                                )
                                // 2. 发送新字符
                                super.onAction(KeyAction.FcitxKeyAction(nextChar), source)
                                lastKana = nextChar
                                return // 拦截成功
                            }
                        }
                        return
                    }
                    "←" -> {
                        super.onAction(
                            KeyAction.SymAction(
                                KeySym(FcitxKeyMapping.FcitxKey_Left),
                                KeyStates.Virtual,
                            ),
                            source,
                        )
                        lastKana = null
                        return
                    }
                    "→" -> {
                        super.onAction(
                            KeyAction.SymAction(
                                KeySym(FcitxKeyMapping.FcitxKey_Right),
                                KeyStates.Virtual,
                            ),
                            source,
                        )
                        lastKana = null
                        return
                    }
                }

                if (act.length == 1 && act[0].code >= 0x3000) {
                    lastKana = act
                } else {
                    // 如果输入了非假名字符（空格、回车、符号），清空缓存
                    lastKana = null
                }
            }
            is KeyAction.SymAction -> {
                lastKana = null
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
