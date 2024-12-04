package com.bangkit.wizzmateapp.view.detail

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bangkit.wizzmateapp.R
import com.bangkit.wizzmateapp.databinding.ActivityDetailBinding
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class DetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap

    private var userLocation: LatLng? = null
    private var destinationLocation: LatLng? = null
    private var destinationLocationMarker: Marker? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchCurrentLocation()
            Log.d("DetailActivity", "Location permission granted")
        } else {
            Log.e("DetailActivity", "Location permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get destination coordinates from intent
        val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)
        val destinationName = intent.getStringExtra("PLACE_NAME")
        val city = intent.getStringExtra("CITY")
        val rating = intent.getDoubleExtra("RATING", 0.0)
        val imageUrl = intent.getStringExtra("IMAGE_URL")
        val category = intent.getStringExtra("CATEGORY")
        val description = intent.getStringExtra("DESCRIPTION")

        Glide.with(this).load(imageUrl).error(R.drawable.error_image_loading).into(binding.ivWisata)
        binding.apply {
            tvNamaWisata.text = destinationName
            tvDestinationLocation.text = city
            tvRating.text = rating.toString()
            tvCategory.text = category
            tvDescription.text = description
        }

        destinationLocation = LatLng(latitude, longitude)

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Set up the map fragment
        if (savedInstanceState == null) {
            val mapFragment = SupportMapFragment()
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, mapFragment)
                .commit()
            mapFragment.getMapAsync(this)
        }
        binding.buttonZoomOut.setOnClickListener {
            val bounds =
                LatLngBounds.builder().include(userLocation!!).include(destinationLocation!!)
                    .build()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            destinationLocationMarker?.showInfoWindow()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        enableMyLocation()

        googleMap.apply {
            mapType = GoogleMap.MAP_TYPE_HYBRID

            if (ActivityCompat.checkSelfPermission(
                    this@DetailActivity, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                isMyLocationEnabled = true
            } else {
                // Request the missing location permission
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            uiSettings.isMyLocationButtonEnabled = true
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchCurrentLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this@DetailActivity, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = LatLng(location.latitude, location.longitude)
                    addMarkersToMap()
                } else {
                    Log.e("DetailActivity", "Failed to retrieve location")
                }
            }
        } else {
            // Request the missing location permission
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun addMarkersToMap() {
        if (userLocation == null || destinationLocation == null) return
        destinationLocationMarker = googleMap.addMarker(
            MarkerOptions().position(destinationLocation!!).snippet(intent.getStringExtra("CITY"))
                .title(intent.getStringExtra("PLACE_NAME"))
        )
        destinationLocationMarker?.showInfoWindow()
        val bounds =
            LatLngBounds.builder().include(userLocation!!).include(destinationLocation!!).build()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))


        // Set the click listener for the markers
        googleMap.setOnMarkerClickListener { marker ->
            val offsetPosition = offsetLatLng(marker.position) // Offset 200 meter ke bawah
            val cameraUpdate =
                CameraUpdateFactory.newLatLngZoom(offsetPosition, 15f) // Zoom level 15
            googleMap.animateCamera(cameraUpdate)

            marker.showInfoWindow()
            true
        }
    }

    private fun offsetLatLng(original: LatLng): LatLng {
        val earthRadius = 6378137.0 // Radius bumi dalam meter
        val latOffset = -200.0 / earthRadius
        val newLatitude =
            original.latitude - Math.toDegrees(latOffset) // Mengurangi agar marker terlihat lebih ke atas layar
        return LatLng(newLatitude, original.longitude)
    }
}

