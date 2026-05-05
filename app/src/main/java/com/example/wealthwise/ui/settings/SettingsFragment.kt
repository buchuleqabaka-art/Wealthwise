package com.example.wealthwise.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wealthwise.R
import com.example.wealthwise.SessionManager
import com.example.wealthwise.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show logged-in user's name and email
        binding.tvUserName.text  = SessionManager.getUserName(requireContext())
        binding.tvUserEmail.text = SessionManager.getUserEmail(requireContext())

        // ── BACK ARROW ────────────────────────────────────────
        binding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }

        //  All rows  show "Coming in Part 3"
        val comingSoonItems = listOf(
            binding.llProfile,
            binding.llSecurity,
            binding.llExport,
            binding.llOffline,
            binding.llTips,
            binding.llNotifications
        )

        comingSoonItems.forEach { row ->
            row.setOnClickListener {
                showComingSoonDialog()
            }
        }

        // ── LOGOUT — fully functional ─────────────────────────
        binding.llLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out of WealthWise?")
                .setPositiveButton("Log Out") { _, _ ->
                    SessionManager.logout(requireContext())
                    findNavController().navigate(
                        R.id.action_settingsFragment_to_loginFragment
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // ── "Coming in Part 3" dialog ─────────────────────────────
    private fun showComingSoonDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("🚧 Coming in Part 3")
            .setMessage(
                "This feature will be fully implemented in Part 3.\n\n" +
                        "Stay tuned for the next update!"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}