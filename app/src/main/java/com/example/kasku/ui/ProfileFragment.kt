package com.example.kasku.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.kasku.R
import com.example.kasku.databinding.FragmentProfileBinding
import com.example.kasku.reminder.ReminderScheduler
import com.example.kasku.util.ThemeManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _b: FragmentProfileBinding? = null
    private val b get() = _b!!

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                ReminderScheduler.enable(requireContext())
            } else {
                setSwitch(false)
                _b?.let { Snackbar.make(it.root, R.string.notif_denied, Snackbar.LENGTH_LONG).show() }
            }
        }

    private val switchListener = CompoundButton.OnCheckedChangeListener { _, checked ->
        if (checked) {
            if (needsPermission()) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            else ReminderScheduler.enable(requireContext())
        } else {
            ReminderScheduler.disable(requireContext())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _b = FragmentProfileBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.tvEmail.text = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

        // Jika izin dicabut lewat pengaturan sistem, anggap pengingat mati.
        val on = ReminderScheduler.isEnabled(requireContext()) && !needsPermission()
        setSwitch(on)

        b.switchDark.isChecked = ThemeManager.isDark(requireContext())
        b.switchDark.setOnCheckedChangeListener { _, checked ->
            ThemeManager.set(requireContext(), checked)
        }

        b.btnLogout.setOnClickListener {
            ReminderScheduler.disable(requireContext())
            FirebaseAuth.getInstance().signOut()
            startActivity(
                Intent(requireContext(), LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }

    private fun setSwitch(on: Boolean) {
        val sw = _b?.switchReminder ?: return
        sw.setOnCheckedChangeListener(null)
        sw.isChecked = on
        sw.setOnCheckedChangeListener(switchListener)
    }

    private fun needsPermission(): Boolean =
        Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}