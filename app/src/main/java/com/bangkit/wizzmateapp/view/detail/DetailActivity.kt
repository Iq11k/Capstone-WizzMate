package com.bangkit.wizzmateapp.view.detail

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class DetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap

    private var userLocation: LatLng? = null
    private var destinationLocation: LatLng? = null

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

        Glide.with(this)
            .load(imageUrl)
            .into(binding.ivWisata)
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
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mapFragment)
                .commit()
            mapFragment.getMapAsync(this)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        enableMyLocation()

        googleMap.apply {
            mapType = GoogleMap.MAP_TYPE_HYBRID

            isMyLocationEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchCurrentLocation()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                userLocation = LatLng(location.latitude, location.longitude)
                addMarkersToMap()
            } else {
                Log.e("DetailActivity", "Failed to retrieve location")
            }
        }
    }

    private fun addMarkersToMap() {
        if (userLocation == null || destinationLocation == null) return
        googleMap.addMarker(
            MarkerOptions().position(destinationLocation!!).snippet(intent.getStringExtra("PLACE_NAME"))
        )

        val bounds = LatLngBounds.builder()
            .include(userLocation!!)
            .include(destinationLocation!!)
            .build()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

        // Set the click listener for the markers
        googleMap.setOnMarkerClickListener { marker ->
            // Zoom to the clicked marker
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.position, 15f) // Zoom level of 15
            googleMap.animateCamera(cameraUpdate)
            true // Return true to indicate that the event was handled
        }
    }

    fun getBitmapFromVectorDrawable(resourceId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(this, resourceId) ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}

