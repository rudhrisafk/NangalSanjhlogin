package com.nangalsanjh.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppLanguage { ENGLISH, HINDI, PUNJABI }
enum class AppTheme { DARK, LIGHT }

class ThemeViewModel : ViewModel() {
    private val _theme = MutableStateFlow(AppTheme.DARK)
    val theme: StateFlow<AppTheme> = _theme

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language

    fun toggleTheme() {
        _theme.value = if (_theme.value == AppTheme.DARK) AppTheme.LIGHT else AppTheme.DARK
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }
}