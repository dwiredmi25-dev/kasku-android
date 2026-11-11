package com.example.kasku.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.example.kasku.R
import com.google.android.material.color.MaterialColors

/** Grafik batang berpasangan (pemasukan vs pengeluaran) per bulan, digambar manual dengan Canvas. */
class MonthlyBarChart @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null
) : View(ctx, attrs) {

    data class Entry(val label: String, val income: Long, val expense: Long)

    private var entries: List<Entry> = emptyList()

    private val incomePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(ctx, R.color.income)
    }
    private val expensePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(ctx, R.color.expense)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11f, resources.displayMetrics)
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 2f }

    fun setData(list: List<Entry>) {
        entries = list
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (entries.isEmpty()) return

        val onSurface = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface)
        textPaint.color = onSurface
        axisPaint.color = onSurface
        axisPaint.alpha = 90

        val labelHeight = textPaint.textSize * 1.8f
        val chartHeight = height - paddingTop - paddingBottom - labelHeight
        val baseY = paddingTop + chartHeight
        val maxValue = entries.maxOf { maxOf(it.income, it.expense) }.coerceAtLeast(1L)
        val groupWidth = (width - paddingLeft - paddingRight).toFloat() / entries.size
        val barWidth = groupWidth * 0.28f
        val gap = 3f

        entries.forEachIndexed { i, e ->
            val cx = paddingLeft + groupWidth * i + groupWidth / 2
            val incomeH = chartHeight * e.income / maxValue
            val expenseH = chartHeight * e.expense / maxValue
            canvas.drawRect(cx - barWidth - gap, baseY - incomeH, cx - gap, baseY, incomePaint)
            canvas.drawRect(cx + gap, baseY - expenseH, cx + barWidth + gap, baseY, expensePaint)
            canvas.drawText(e.label, cx, baseY + labelHeight * 0.8f, textPaint)
        }
        canvas.drawLine(paddingLeft.toFloat(), baseY, (width - paddingRight).toFloat(), baseY, axisPaint)
    }
}
