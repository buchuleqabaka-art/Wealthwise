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
import com.example.wealthwise.database.WealthWiseDatabase
import com.example.wealthwise.database.entities.Category
import com.example.wealthwise.database.entities.User
import com.example.wealthwise.databinding.FragmentSignUpBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── CREATE ACCOUNT ────────────────────────────────────
        binding.btnSignUp.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val email    = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (!validateAll(fullName, username, email, password)) return@setOnClickListener

            hideKeyboard()
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSignUp.isEnabled    = false

            lifecycleScope.launch(Dispatchers.IO) {
                val db = WealthWiseDatabase.getDatabase(requireContext())
                val usernameTaken = db.userDao().usernameExists(username) > 0

                withContext(Dispatchers.Main) {
                    if (_binding == null) return@withContext

                    if (usernameTaken) {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSignUp.isEnabled    = true
                        binding.tilUsername.error      =
                            "Username already taken. Choose another."
                        return@withContext
                    }

                    val newUser = User(
                        fullName     = fullName,
                        username     = username,
                        email        = email,
                        passwordHash = password.hashCode().toString()
                    )

                    lifecycleScope.launch(Dispatchers.IO) {
                        val insertedId = db.userDao().insert(newUser)

                        if (insertedId > 0) {
                            // Seed default categories for the new user
                            seedDefaultCategories(insertedId.toString())

                            withContext(Dispatchers.Main) {
                                if (_binding == null) return@withContext
                                binding.progressBar.visibility = View.GONE

                                // ── Tell user to log in with their new details ──
                                Toast.makeText(
                                    requireContext(),
                                    "Account created! Please log in with your new credentials.",
                                    Toast.LENGTH_LONG
                                ).show()

                                // ── Go to LOGIN page ──────────────────────────
                                // popUpTo loginFragment clears the sign-up screen
                                // from the back stack so Back goes to Login cleanly
                                findNavController().navigate(
                                    R.id.action_signUpFragment_to_loginFragment
                                )
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                binding.progressBar.visibility = View.GONE
                                binding.btnSignUp.isEnabled    = true
                                Toast.makeText(requireContext(),
                                    "Registration failed. Please try again.",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        // ── ALREADY HAVE ACCOUNT → LOG IN ─────────────────────
        binding.tvLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    // ── Validate all fields ───────────────────────────────────
    private fun validateAll(
        fullName: String, username: String, email: String, password: String
    ): Boolean {
        var valid = true

        if (fullName.isEmpty()) {
            binding.tilFullName.error = "Please enter your full name"
            valid = false
        } else binding.tilFullName.error = null

        if (username.isEmpty()) {
            binding.tilUsername.error = "Please choose a username"
            valid = false
        } else if (username.length < 3) {
            binding.tilUsername.error = "Username must be at least 3 characters"
            valid = false
        } else if (username.contains(" ")) {
            binding.tilUsername.error = "Username cannot contain spaces"
            valid = false
        } else binding.tilUsername.error = null

        if (email.isEmpty()) {
            binding.tilEmail.error = "Please enter your email address"
            valid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email address"
            valid = false
        } else binding.tilEmail.error = null

        if (password.isEmpty()) {
            binding.tilPassword.error = "Please create a password"
            valid = false
        } else {
            val hasLength  = password.length >= 8
            val hasUpper   = password.any { it.isUpperCase() }
            val hasLower   = password.any { it.isLowerCase() }
            val hasNumber  = password.any { it.isDigit() }
            val hasSpecial = password.any { "!@#\$%^&*()_+-=[]{}|;':\",./<>?".contains(it) }

            when {
                !hasLength  -> binding.tilPassword.error =
                    "Password must be at least 8 characters"
                !hasUpper   -> binding.tilPassword.error =
                    "Password must contain at least one uppercase letter (A-Z)"
                !hasLower   -> binding.tilPassword.error =
                    "Password must contain at least one lowercase letter (a-z)"
                !hasNumber  -> binding.tilPassword.error =
                    "Password must contain at least one number (0-9)"
                !hasSpecial -> binding.tilPassword.error =
                    "Password must contain at least one special character (!@#\$%^&*)"
                else        -> binding.tilPassword.error = null
            }

            if (!hasLength || !hasUpper || !hasLower || !hasNumber || !hasSpecial)
                valid = false
        }

        return valid
    }

    // ── Seed default categories for the new user ──────────────
    private suspend fun seedDefaultCategories(userId: String) {
        val db = WealthWiseDatabase.getDatabase(requireContext())
        listOf(
            Category(userId=userId, name="Groceries",     type="EXPENSE", budgetLimit=3000.0, color="#1D9E75"),
            Category(userId=userId, name="Transport",     type="EXPENSE", budgetLimit=1500.0, color="#2B5CE6"),
            Category(userId=userId, name="Entertainment", type="EXPENSE", budgetLimit=1000.0, color="#7C5CBF"),
            Category(userId=userId, name="Dining",        type="EXPENSE", budgetLimit=1200.0, color="#EF9F27"),
            Category(userId=userId, name="Health",        type="EXPENSE", budgetLimit=800.0,  color="#E24B4A"),
            Category(userId=userId, name="Salary",        type="INCOME",  budgetLimit=0.0,    color="#1D9E75"),
            Category(userId=userId, name="Freelance",     type="INCOME",  budgetLimit=0.0,    color="#2B5CE6"),
            Category(userId=userId, name="Allowance",     type="INCOME",  budgetLimit=0.0,    color="#FFD600"),
        ).forEach { db.categoryDao().insert(it) }
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