package com.nangalsanjh.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppLanguage { ENGLISH, HINDI, PUNJABI }
enum class AppTheme { DARK, LIGHT }

data class ThemeTransition(val target: AppTheme, val originX: Float, val originY: Float)

class ThemeViewModel : ViewModel() {
    private val _theme = MutableStateFlow(AppTheme.LIGHT)
    val theme: StateFlow<AppTheme> = _theme

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language

    private val _transition = MutableStateFlow<ThemeTransition?>(null)
    val transition: StateFlow<ThemeTransition?> = _transition

    /** Starts the venom animation; the theme itself is swapped by the overlay mid-animation. */
    fun requestTheme(target: AppTheme, originX: Float, originY: Float) {
        if (_theme.value != target && _transition.value == null) {
            _transition.value = ThemeTransition(target, originX, originY)
        }
    }

    fun applyThemeNow(target: AppTheme) { _theme.value = target }

    fun finishTransition() { _transition.value = null }

    fun toggleTheme() {
        _theme.value = if (_theme.value == AppTheme.DARK) AppTheme.LIGHT else AppTheme.DARK
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }
}