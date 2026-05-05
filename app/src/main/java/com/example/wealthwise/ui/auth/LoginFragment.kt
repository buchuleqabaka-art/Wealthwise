package com.example.wealthwise.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.wealthwise.R
import com.example.wealthwise.SessionManager
import com.example.wealthwise.database.WealthWiseDatabase
import com.example.wealthwise.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // AUTO-LOGIN: if session exists skip login entirely ─
        if (SessionManager.isLoggedIn(requireContext())) {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            return
        }

        //LOG IN BUTTON
        binding.btnLogin.setOnClickListener {
            val identifier = binding.etIdentifier.text.toString().trim()
            val password   = binding.etPassword.text.toString()

            // Clear previous errors
            binding.tilIdentifier.error = null
            binding.tilPassword.error   = null

            // Validate inputs
            if (identifier.isEmpty()) {
                binding.tilIdentifier.error = "Please enter your username or email"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.tilPassword.error = "Please enter your password"
                return@setOnClickListener
            }

            // Dismiss keyboard for clean UX
            hideKeyboard()

            // Show spinner, disable button
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled     = false
            binding.btnSignUp.isEnabled    = false

            // FAST DB CHECK on IO thread
            // Using Dispatchers.IO so the DB query runs immediately
            // on a background thread — no delays added
            lifecycleScope.launch(Dispatchers.IO) {
                val db   = WealthWiseDatabase.getDatabase(requireContext())
                val user = db.userDao().findByIdentifier(identifier)
                val passwordMatch = user?.passwordHash == password.hashCode().toString()

                // Back to Main thread to update UI
                withContext(Dispatchers.Main) {
                    if (_binding == null) return@withContext

                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled     = true
                    binding.btnSignUp.isEnabled    = true

                    if (user != null && passwordMatch) {
                        // SUCCESS
                        // Save session
                        SessionManager.login(
                            context  = requireContext(),
                            userId   = user.id.toString(),
                            fullName = user.fullName,
                            email    = user.email
                        )

                        // Show success message immediately
                        showSuccessAndNavigate(user.fullName)

                    } else if (user == null) {
                        // Username/email not found
                        binding.tilIdentifier.error =
                            "No account found with this username or email"
                    } else {
                        // Wrong password
                        binding.tilPassword.error = "Incorrect password. Please try again."
                        binding.etPassword.setText("")
                        binding.etPassword.requestFocus()
                    }
                }
            }
        }

        //  CREATE ACCOUNT BUTTON
        // FIX: was binding.tvSignUp — now correctly binding.btnSignUp
        binding.btnSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        //FORGOT PASSWORD
        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
    }

    //  Show success Snackbar then navigate immediately
    // The Snackbar appears and the user is taken straight to
    // the Home screen without waiting — the message shows ON
    // the home screen briefly
    private fun showSuccessAndNavigate(userName: String) {
        // Navigate immediately — no delay
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)

        // The Toast will show on the Home screen as it loads
        Toast.makeText(
            requireContext(),
            "Welcome back, $userName! ",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun hideKeyboard() {
        val imm = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}