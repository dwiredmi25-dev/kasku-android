package com.example.kasku.util

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    private const val PREFS = "kasku_prefs"
    private const val KEY = "night_mode"

    private fun saved(ctx: Context): Int =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    fun apply(ctx: Context) {
        AppCompatDelegate.setDefaultNightMode(saved(ctx))
    }

    fun set(ctx: Context, dark: Boolean) {
        val mode = if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY, mode).apply()
        AppCompatDelegate.setDefaultNightMode(mode) // Activity dibuat ulang otomatis
    }

    fun isDark(ctx: Context): Boolean = when (saved(ctx)) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> (ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
    }
}
