package com.bangkit.wizzmateapp.view.main.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit.wizzmateapp.R
import com.bangkit.wizzmateapp.adapter.WisataAdapter
import com.bangkit.wizzmateapp.data.WisataRepository
import com.bangkit.wizzmateapp.data.local.SessionPreferences
import com.bangkit.wizzmateapp.data.local.dataStore
import com.bangkit.wizzmateapp.data.remote.retrofit.ApiConfig
import com.bangkit.wizzmateapp.databinding.FragmentHomeBinding
import com.bangkit.wizzmateapp.view.main.MainViewModel
import com.bangkit.wizzmateapp.view.main.MainViewModelFactory
import com.bangkit.wizzmateapp.view.welcome.WelcomeActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
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

    private var activeButton: MaterialButton? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getUserLocation() // Call your function to fetch location
            } else {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.username.observe(viewLifecycleOwner) { username ->
            binding.tvProfileName.text = username
        }
        getUserLocation()

        val wisataAdapter = WisataAdapter()

        mainViewModel.wisata.observe(viewLifecycleOwner) {
            binding.loadingBar.visibility = View.GONE
            wisataAdapter.submitData(lifecycle, it)
            binding.rvWisata.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = wisataAdapter
            }
        }

        wisataAdapter.addLoadStateListener { loadState ->
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

        val buttonData = listOf(
            "" to R.string.recommendation,
            "Budaya" to R.string.budaya,
            "Taman Hiburan" to R.string.taman_hiburan,
            "Cagar Alam" to R.string.cagar_alam,
            "Bahari" to R.string.bahari,
            "Pusat Perbelanjaan" to R.string.pusat_perbelanjaan,
            "Tempat Ibadah" to R.string.tempat_ibadah
        )

        buttonData.forEachIndexed { index, (category, stringRes) ->
            val button = MaterialButton(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 8
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                    strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.grey)
                    strokeWidth = 5
                }
                text = getString(stringRes)

                if (index == 0) setActiveButton(this)

                setOnClickListener {
                    mainViewModel.setCategory(category)
                    setActiveButton(this)
                }
            }
            binding.buttonContainer.addView(button)
        }

        binding.edSearchBar.apply{
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    mainViewModel.searchWisata(query!!)
                    mainViewModel.searchResult.observe(viewLifecycleOwner) { result ->
                        if (result != null) {
                            wisataAdapter.submitData(lifecycle, result)
                        }
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrEmpty()) {
                        binding.buttonContainer.visibility = View.VISIBLE
                        mainViewModel.wisata.observe(viewLifecycleOwner) { wisataList ->
                            wisataAdapter.submitData(lifecycle, wisataList)
                            binding.rvWisata.apply {
                                layoutManager = LinearLayoutManager(requireContext())
                                adapter = wisataAdapter
                            }
                        }
                    } else {
                        binding.buttonContainer.visibility = View.GONE
                        mainViewModel.searchWisata(newText)
                        mainViewModel.searchResult.observe(viewLifecycleOwner) { result ->
                            if (result != null) {
                                wisataAdapter.submitData(lifecycle, result)
                            }
                        }
                    }
                    return false
                }
            })
            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.buttonContainer.visibility = View.GONE
                } else {
                    binding.buttonContainer.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setActiveButton(button: MaterialButton) {
        activeButton?.apply {
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.grey)
            strokeWidth = 5
        }

        button.apply {
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primaryColor))
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            strokeWidth = 0
        }

        activeButton = button
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
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
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getCityName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses!!.isNotEmpty()) {
                userLocation = addresses[0]?.locality ?: "Unknown city"
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