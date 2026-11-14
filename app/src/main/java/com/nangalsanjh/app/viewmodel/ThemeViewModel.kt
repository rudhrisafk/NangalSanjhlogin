package com.nangalsanjh.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppLanguage { ENGLISH, HINDI, PUNJABI }
enum class AppTheme { DARK, LIGHT }

class ThemeViewModel : ViewModel() {
    private val _theme = MutableStateFlow(AppTheme.LIGHT)
    val theme: StateFlow<AppTheme> = _theme

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language

    // Text size multiplier: 1.0 normal, 1.15 large, 1.3 extra large
    private val _textScale = MutableStateFlow(1.0f)
    val textScale: StateFlow<Float> = _textScale
    fun setTextScale(scale: Float) { _textScale.value = scale }

    fun setTheme(target: AppTheme) { _theme.value = target }

    fun toggleTheme() {
        _theme.value = if (_theme.value == AppTheme.DARK) AppTheme.LIGHT else AppTheme.DARK
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }
}