package com.example.kasku

import android.app.Application
import com.example.kasku.util.ThemeManager

class KaskuApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Terapkan pilihan mode gelap/terang sebelum layar pertama dibuat.
        ThemeManager.apply(this)
    }
}
