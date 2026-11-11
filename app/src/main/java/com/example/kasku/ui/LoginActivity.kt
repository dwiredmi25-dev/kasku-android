package com.example.kasku.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.kasku.R
import com.example.kasku.databinding.ActivityLoginBinding
import com.example.kasku.util.AuthErrors
import com.example.kasku.util.isOnline
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sesi tersimpan otomatis oleh Firebase: kalau sudah login, langsung ke beranda.
        if (auth.currentUser != null) {
            goMain()
            return
        }
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnLogin.setOnClickListener { login() }
        b.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val email = b.etEmail.text.toString().trim()
        val pass = b.etPassword.text.toString()

        b.tilEmail.error = null
        b.tilPassword.error = null

        when {
            email.isEmpty() -> { b.tilEmail.error = getString(R.string.err_email_empty); return }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                b.tilEmail.error = getString(R.string.err_email_invalid); return
            }
            pass.length < 6 -> { b.tilPassword.error = getString(R.string.err_password_short); return }
        }
        if (!isOnline()) {
            Snackbar.make(b.root, R.string.err_no_network, Snackbar.LENGTH_LONG).show()
            return
        }

        setLoading(true)
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) { task ->
            setLoading(false)
            if (task.isSuccessful) goMain()
            else Snackbar.make(b.root, AuthErrors.message(this, task.exception), Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setLoading(loading: Boolean) {
        b.progress.isVisible = loading
        b.btnLogin.isEnabled = !loading
    }

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}