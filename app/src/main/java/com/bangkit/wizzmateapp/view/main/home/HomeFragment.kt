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
import android.widget.Toast
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

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
        mainViewModel = ViewModelProvider(this, MainViewModelFactory(repository,pref))[MainViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
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
        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)

        mainViewModel.wisata.observe(viewLifecycleOwner) {
            storyAdapter.submitData(lifecycle, it)
            binding.rvWisata.apply {
                addItemDecoration(dividerItemDecoration)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = storyAdapter
            }
        }


        binding.button.setOnClickListener {
            startActivity(Intent(requireContext(), DetailActivity::class.java))
        }

        binding.ivActionLogout.setOnClickListener {
            val auth = Firebase.auth
            mainViewModel.logout()
            lifecycleScope.launch {
                val credentialManager = CredentialManager.create(requireContext())
                auth.signOut()
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            }
            val intent = Intent(context, WelcomeActivity::class.java).
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
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