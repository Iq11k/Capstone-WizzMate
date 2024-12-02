package com.bangkit.wizzmateapp.view.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.wizzmateapp.R
import com.bangkit.wizzmateapp.databinding.ActivityMainBinding
import com.bangkit.wizzmateapp.view.main.estimator.EstimatorFragment
import com.bangkit.wizzmateapp.view.main.history.HistoryFragment
import com.bangkit.wizzmateapp.view.main.home.HomeFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = intent.getStringExtra("USERNAME")
        if (savedInstanceState == null) {
            val bundle = Bundle()
            bundle.putString("USERNAME", username)

            val homeFragment = HomeFragment()
            homeFragment.arguments = bundle

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit()
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                    true
                }
                R.id.nav_cost_estimator -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, EstimatorFragment())
                        .commit()
                    true
                }
                R.id.nav_History -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HistoryFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

    }
}