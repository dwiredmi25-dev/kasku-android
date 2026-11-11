package com.example.kasku.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.kasku.R
import com.example.kasku.databinding.ActivityRegisterBinding
import com.example.kasku.util.AuthErrors
import com.example.kasku.util.isOnline
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var b: ActivityRegisterBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnRegister.setOnClickListener { register() }
        b.tvLogin.setOnClickListener { finish() }
    }

    private fun register() {
        val email = b.etEmail.text.toString().trim()
        val pass = b.etPassword.text.toString()
        val confirm = b.etConfirm.text.toString()

        b.tilEmail.error = null
        b.tilPassword.error = null
        b.tilConfirm.error = null

        when {
            email.isEmpty() -> { b.tilEmail.error = getString(R.string.err_email_empty); return }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                b.tilEmail.error = getString(R.string.err_email_invalid); return
            }
            pass.length < 6 -> { b.tilPassword.error = getString(R.string.err_password_short); return }
            confirm != pass -> { b.tilConfirm.error = getString(R.string.err_password_mismatch); return }
        }
        if (!isOnline()) {
            Snackbar.make(b.root, R.string.err_no_network, Snackbar.LENGTH_LONG).show()
            return
        }

        setLoading(true)
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) { task ->
            setLoading(false)
            if (task.isSuccessful) {
                // Setelah daftar, pengguna otomatis login.
                startActivity(
                    Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            } else {
                Snackbar.make(b.root, AuthErrors.message(this, task.exception), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        b.progress.isVisible = loading
        b.btnRegister.isEnabled = !loading
    }
}