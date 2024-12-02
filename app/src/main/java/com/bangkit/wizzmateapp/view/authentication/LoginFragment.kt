package com.bangkit.wizzmateapp.view.authentication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bangkit.wizzmateapp.R
import com.bangkit.wizzmateapp.data.local.SessionPreferences
import com.bangkit.wizzmateapp.data.local.dataStore
import com.bangkit.wizzmateapp.databinding.FragmentLoginBinding
import com.bangkit.wizzmateapp.helper.StringHelper.makeTextLink
import com.bangkit.wizzmateapp.view.main.MainActivity

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pref = SessionPreferences.getInstance(requireContext().dataStore)
        val viewModel =
            ViewModelProvider(this, LoginViewModelFactory(pref))[LoginViewModel::class.java]
        binding.edLoginPassword.enforceMinLength = false
        makeTextLink(binding.tvDontHaveAccount, "Sign Up", false, R.color.primaryColor) {
            val intent = Intent(context, AuthenticationActivity::class.java).apply {
                putExtra("isRegister", true)
            }
            startActivity(intent)
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            binding.loadingBar.visibility = View.VISIBLE

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Email dan password harus diisi",
                    Toast.LENGTH_SHORT
                ).show()
                binding.loadingBar.visibility = View.GONE
            } else {
                if (binding.cbRemember.isChecked){
                    viewModel.login(email, password)
                } else {
                    viewModel.oneTimeLogin(email, password)
                }
                binding.apply {
                    buttonLogin.isEnabled = false
                    edLoginEmail.isEnabled = false
                    edLoginPassword.isEnabled = false
                    cbRemember.isEnabled = false
                }
            }
        }

        viewModel.isLogin.observe(viewLifecycleOwner) { loginStatus ->
            if (loginStatus) {
                Toast.makeText(requireContext(), "Login berhasil", Toast.LENGTH_SHORT).show()
                viewModel.responseBody.observe(viewLifecycleOwner) {
                    val intent = Intent(context, MainActivity::class.java).
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(requireContext(), "Login gagal", Toast.LENGTH_SHORT).show()
                binding.apply {
                    loadingBar.visibility = View.GONE
                    buttonLogin.isEnabled = true
                    edLoginEmail.isEnabled = true
                    edLoginPassword.isEnabled = true
                    cbRemember.isEnabled = false
                }
            }
        }
    }
}