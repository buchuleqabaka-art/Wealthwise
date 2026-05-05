package com.example.wealthwise.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wealthwise.R
import com.example.wealthwise.databinding.FragmentWelcomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // CHECK IF USER JUST SIGNED UP
        val successMessage = arguments?.getString("signup_success")

        if (successMessage != null) {
            binding.tvSuccessMessage.text = successMessage
            binding.tvSuccessMessage.visibility = View.VISIBLE

            //  Smooth fade-in animation
            binding.tvSuccessMessage.alpha = 0f
            binding.tvSuccessMessage.animate()
                .alpha(1f)
                .setDuration(800)
                .start()
        }

        // 🔹 Load cached user
        val prefs = requireContext()
            .getSharedPreferences("wealthwise_prefs", Context.MODE_PRIVATE)

        val cachedName = prefs.getString("logged_in_name", null)

        if (cachedName != null) {
            // ✅ Returning user
            val greeting = if (successMessage != null) {
                "Welcome, $cachedName 🎉"
            } else {
                "Welcome back, $cachedName 👋"
            }
            binding.tvWelcomeName.text = greeting
        } else {
            // 🔄 Fallback to Firebase
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.uid)
                    .get()
                    .addOnSuccessListener { doc ->

                        val fullName = doc.getString("fullName")
                        val email = doc.getString("email") ?: user.email ?: ""

                        if (fullName != null) {
                            val greeting = if (successMessage != null) {
                                "Welcome, $fullName 🎉"
                            } else {
                                "Welcome back, $fullName 👋"
                            }

                            binding.tvWelcomeName.text = greeting

                            // Save session
                            com.example.wealthwise.SessionManager.login(
                                requireContext(),
                                user.uid,
                                fullName,
                                email
                            )
                        } else {
                            binding.tvWelcomeName.text = "Welcome 👋"
                        }
                    }
                    .addOnFailureListener {
                        binding.tvWelcomeName.text = "Welcome 👋"
                    }
            } else {
                binding.tvWelcomeName.text = "Welcome 👋"
            }
        }

        //  NAVIGATION
        binding.btnGetStarted.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment_to_homeFragment)
        }

        binding.llQuickAdd.setOnClickListener {
            findNavController().navigate(R.id.action_global_addTransactionFragment)
        }

        binding.llQuickBudget.setOnClickListener {
            findNavController().navigate(R.id.action_global_budgetFragment)
        }

        binding.tvViewAll.setOnClickListener {
            findNavController().navigate(R.id.transactionListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}