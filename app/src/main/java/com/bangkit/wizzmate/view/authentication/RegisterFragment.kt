package com.bangkit.wizzmate.view.authentication

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bangkit.wizzmate.R
import com.bangkit.wizzmate.databinding.FragmentLoginBinding
import com.bangkit.wizzmate.databinding.FragmentRegisterBinding
import com.bangkit.wizzmate.helper.StringHelper.makeTextLink
import com.bangkit.wizzmate.view.main.MainActivity

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewModel =
            ViewModelProvider(this, RegisterViewModelFactory())[RegisterViewModel::class.java]

        makeTextLink(binding.tvAlreadyHaveAccount, "Sign In", false, R.color.primaryColor) {
            val intent = Intent(context, AuthenticationActivity::class.java).apply {
                putExtra("isRegister", false)
            }
            startActivity(intent)
        }

        binding.buttonRegister.setOnClickListener {
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            binding.loadingBar.visibility = View.VISIBLE

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Email dan password harus diisi",
                    Toast.LENGTH_SHORT
                ).show()
                binding.loadingBar.visibility = View.GONE
            } else {
                viewModel.register(email, password)
                binding.buttonRegister.isEnabled = false
            }
        }

        viewModel.isRegister.observe(viewLifecycleOwner) { loginStatus ->
            if (loginStatus) {
                Toast.makeText(requireContext(), "Register berhasil", Toast.LENGTH_SHORT).show()
                startActivity(Intent(context, AuthenticationActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "Register gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}