package com.bangkit.wizzmateapp.view.main.home

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit.wizzmateapp.R
import com.bangkit.wizzmateapp.adapter.WisataAdapter
import com.bangkit.wizzmateapp.data.WisataRepository
import com.bangkit.wizzmateapp.data.local.SessionPreferences
import com.bangkit.wizzmateapp.data.local.dataStore
import com.bangkit.wizzmateapp.data.remote.retrofit.ApiConfig
import com.bangkit.wizzmateapp.databinding.FragmentHomeBinding
import com.bangkit.wizzmateapp.view.authentication.LoginViewModel
import com.bangkit.wizzmateapp.view.authentication.LoginViewModelFactory
import com.bangkit.wizzmateapp.view.detail.DetailActivity
import com.bangkit.wizzmateapp.view.main.MainActivity
import com.bangkit.wizzmateapp.view.main.MainViewModel
import com.bangkit.wizzmateapp.view.main.MainViewModelFactory
import com.bangkit.wizzmateapp.view.welcome.WelcomeActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

@Suppress("DEPRECATION")
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var pref: SessionPreferences
    private lateinit var repository: WisataRepository
    private lateinit var mainViewModel: MainViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userLocation: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        pref = SessionPreferences.getInstance(requireContext().dataStore)
        repository = WisataRepository(ApiConfig.getApiService())
        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(repository, pref)
        )[MainViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        mainViewModel.setCategory("")
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.username.observe(viewLifecycleOwner) { username ->
            binding.tvProfileName.text = username
        }
        getUserLocation()

        val storyAdapter = WisataAdapter()

        mainViewModel.wisata.observe(viewLifecycleOwner) {
            binding.loadingBar.visibility = View.GONE
            storyAdapter.submitData(lifecycle, it)
            binding.rvWisata.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = storyAdapter
            }
        }

        storyAdapter.addLoadStateListener { loadState ->
            binding.loadingBar.visibility =
                if (loadState.source.refresh is androidx.paging.LoadState.Loading) {
                    // Show loading spinner while data is being loaded
                    binding.rvWisata.isLayoutFrozen = true // Disable scrolling
                    View.VISIBLE
                } else {
                    // Hide loading spinner and enable scrolling once data is loaded
                    binding.rvWisata.isLayoutFrozen = false // Enable scrolling
                    View.GONE
                }
        }

        binding.ivActionLogout.setOnClickListener {
            val auth = Firebase.auth
            mainViewModel.logout()
            lifecycleScope.launch {
                val credentialManager = CredentialManager.create(requireContext())
                auth.signOut()
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            }
            val intent = Intent(
                context,
                WelcomeActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        binding.apply {
            val buttons = listOf(
                buttonDefault,
                buttonCagar,
                buttonBudaya,
                buttonHiburan,
                buttonBahari,
                buttonPerbelanjaan,
                buttonTempatIbadah
            )

            fun resetButtonStyles() {
                buttons.forEach { button ->
                    button.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    button.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                    button.strokeColor =
                        ContextCompat.getColorStateList(requireContext(), R.color.grey)
                    button.strokeWidth = 4
                }
            }

            fun highlightButton(selectedButton: MaterialButton) {
                selectedButton.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.primaryColor
                    )
                )
                selectedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                selectedButton.strokeColor =
                    ContextCompat.getColorStateList(requireContext(), R.color.primaryColor)
            }

            resetButtonStyles()
            highlightButton(buttonDefault)

            buttonDefault.setOnClickListener {
                mainViewModel.setCategory("")
                rvWisata.layoutManager?.scrollToPosition(0)
                resetButtonStyles()
                highlightButton(buttonDefault)
            }
            buttonCagar.setOnClickListener {
                mainViewModel.setCategory("Cagar Alam")
                resetButtonStyles()
                highlightButton(buttonCagar)
            }
            buttonBudaya.setOnClickListener {
                mainViewModel.setCategory("Budaya")
                resetButtonStyles()
                highlightButton(buttonBudaya)
            }
            buttonHiburan.setOnClickListener {
                mainViewModel.setCategory("Taman Hiburan")
                resetButtonStyles()
                highlightButton(buttonHiburan)
            }
            buttonBahari.setOnClickListener {
                mainViewModel.setCategory("Bahari")
                resetButtonStyles()
                highlightButton(buttonBahari)
            }
            buttonPerbelanjaan.setOnClickListener {
                mainViewModel.setCategory("Pusat Perbelanjaan")
                resetButtonStyles()
                highlightButton(buttonPerbelanjaan)
            }
            buttonTempatIbadah.setOnClickListener {
                mainViewModel.setCategory("Tempat Ibadah")
                resetButtonStyles()
                highlightButton(buttonTempatIbadah)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                getCityName(latitude, longitude)
            } else {
                Toast.makeText(context, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCityName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses!!.isNotEmpty()) {
                userLocation = addresses.get(0)?.locality ?: "Unknown city"
                binding.tvProfileLocation.text = userLocation
            } else {
                Toast.makeText(context, "No address found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Geocoder error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}