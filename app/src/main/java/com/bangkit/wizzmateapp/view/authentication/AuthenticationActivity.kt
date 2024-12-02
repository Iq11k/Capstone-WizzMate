package com.bangkit.wizzmateapp.view.authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bangkit.wizzmateapp.R
import com.bangkit.wizzmateapp.databinding.ActivityAuthenticationBinding
import com.bangkit.wizzmateapp.view.welcome.WelcomeActivity

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var loginViewModel: LoginViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isRegister = intent.getBooleanExtra("isRegister", false)

        if (savedInstanceState == null) {
            if (isRegister) {
                switchFragment(RegisterFragment())
            } else {
                switchFragment(LoginFragment())
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(
            this,
            WelcomeActivity::class.java
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .commit()
    }
}