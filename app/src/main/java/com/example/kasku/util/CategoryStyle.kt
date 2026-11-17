package com.example.kasku.util

import android.graphics.Color
import androidx.annotation.DrawableRes
import com.example.kasku.R

data class CategoryStyle(
    @DrawableRes val icon: Int,
    val fg: Int,   // warna ikon
    val bg: Int    // warna latar kotak
)

object CategoryIcons {

    private val other = CategoryStyle(
        R.drawable.ic_cat_other, Color.parseColor("#64748B"), Color.parseColor("#E2E8F0")
    )

    fun of(category: String): CategoryStyle {
        val c = category.trim().lowercase()
        return when {
            "makan" in c -> CategoryStyle(
                R.drawable.ic_cat_food, Color.parseColor("#16A34A"), Color.parseColor("#DCFCE7"))
            "transport" in c -> CategoryStyle(
                R.drawable.ic_cat_transport, Color.parseColor("#7C3AED"), Color.parseColor("#EDE9FE"))
            "belanja" in c -> CategoryStyle(
                R.drawable.ic_cat_shopping, Color.parseColor("#DC2626"), Color.parseColor("#FEE2E2"))
            "tagihan" in c -> CategoryStyle(
                R.drawable.ic_cat_bill, Color.parseColor("#D97706"), Color.parseColor("#FEF3C7"))
            "hibur" in c -> CategoryStyle(
                R.drawable.ic_cat_entertainment, Color.parseColor("#DB2777"), Color.parseColor("#FCE7F3"))
            "kesehatan" in c -> CategoryStyle(
                R.drawable.ic_cat_health, Color.parseColor("#0891B2"), Color.parseColor("#CFFAFE"))
            "gaji" in c -> CategoryStyle(
                R.drawable.ic_cat_salary, Color.parseColor("#0F766E"), Color.parseColor("#CCFBF1"))
            else -> other
        }
    }
}