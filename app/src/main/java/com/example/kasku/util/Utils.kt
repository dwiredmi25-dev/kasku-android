package com.example.kasku.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.kasku.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object Format {
    private val locale = Locale("id", "ID")

    fun rupiah(value: Long): String =
        NumberFormat.getCurrencyInstance(locale).apply { maximumFractionDigits = 0 }.format(value)

    fun date(ts: Timestamp): String =
        SimpleDateFormat("dd MMM yyyy", locale).format(ts.toDate())

    fun date(millis: Long): String =
        SimpleDateFormat("dd MMM yyyy", locale).format(millis)
}

/** MaterialDatePicker bekerja dengan milidetik UTC; dua fungsi ini menjaga tanggal tidak bergeser. */
object DateUtil {
    fun localToUtc(millis: Long): Long {
        val local = Calendar.getInstance().apply { timeInMillis = millis }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    fun utcToLocal(utcMillis: Long): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
        return Calendar.getInstance().apply {
            clear()
            set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 12, 0, 0)
        }.timeInMillis
    }
}

fun Context.isOnline(): Boolean {
    val cm = getSystemService(ConnectivityManager::class.java) ?: return false
    val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

object AuthErrors {
    fun message(ctx: Context, e: Exception?): String = when (e) {
        is FirebaseNetworkException -> ctx.getString(R.string.err_no_network)
        is FirebaseAuthWeakPasswordException -> ctx.getString(R.string.err_weak_password)
        is FirebaseAuthUserCollisionException -> ctx.getString(R.string.err_email_taken)
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> ctx.getString(R.string.err_credentials)
        else -> ctx.getString(R.string.err_generic)
    }
}
