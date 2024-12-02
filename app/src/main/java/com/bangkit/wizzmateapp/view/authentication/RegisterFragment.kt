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
import com.bangkit.wizzmateapp.databinding.FragmentRegisterBinding
import com.bangkit.wizzmateapp.helper.StringHelper.makeTextLink

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
            val username = binding.edRegisterName.text.toString()
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
                if (binding.edRegisterPassword.text.toString() != binding.edRegisterKonfirmasiPassword.text.toString()){
                    binding.edRegisterKonfirmasiPasswordLayout.error = "Password tidak sama"
                    binding.loadingBar.visibility = View.GONE
                } else {
                    viewModel.register(username, email, password)
                    binding.apply {
                        buttonRegister.isEnabled = false
                        edRegisterName.isEnabled = false
                        edRegisterEmail.isEnabled = false
                        edRegisterPassword.isEnabled = false
                        edRegisterKonfirmasiPassword.isEnabled = false
                    }

                }
            }
        }

        viewModel.isRegister.observe(viewLifecycleOwner) { loginStatus ->
            if (loginStatus) {
                Toast.makeText(requireContext(), "Register berhasil", Toast.LENGTH_SHORT).show()
                startActivity(Intent(context, AuthenticationActivity::class.java))
            } else {
                Toast.makeText(requireContext(), "Register gagal", Toast.LENGTH_SHORT).show()
                binding.apply {
                    loadingBar.visibility = View.GONE
                    buttonRegister.isEnabled = true
                    edRegisterName.isEnabled = true
                    edRegisterEmail.isEnabled = true
                    edRegisterPassword.isEnabled = true
                    edRegisterKonfirmasiPassword.isEnabled = false
                }
            }
        }
    }
}