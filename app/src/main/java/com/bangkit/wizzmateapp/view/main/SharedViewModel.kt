package com.bangkit.wizzmateapp.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class SharedViewModel : ViewModel() {

    private val _userBudget = MutableLiveData<Double>(0.0)
    val userBudget: LiveData<Double> get() = _userBudget

    private val _userDuration = MutableLiveData<Long>(0)
    val userDuration: LiveData<Long> get() = _userDuration

    private val _userCity = MutableLiveData<String>("")
    val userCity: LiveData<String> get() = _userCity

    private val _userLatitude = MutableLiveData<Double>(0.0)
    val userLatitude: LiveData<Double> get() = _userLatitude

    private val _userLongitude = MutableLiveData<Double>(0.0)
    val userLongitude: LiveData<Double> get() = _userLongitude

    private val _userDeparture = MutableLiveData<String>("")
    val userDeparture: LiveData<String> get() = _userDeparture

    val airports = listOf(
        Triple("Jakarta", -6.125567, 106.655897),
        Triple("Yogyakarta", -7.900211, 110.053325),
        Triple("Semarang", -6.970570, 110.375807),
        Triple("Surabaya", -7.379831, 112.787750),
        Triple("Bandung", -6.900343, 107.575845)
    )

    // Returns coordinates as LatLng for a given city, or a default LatLng (0.0, 0.0) if not found
    fun getCoordinatesForCity(city: String): LatLng {
        val airport = airports.find { it.first == city }
        return LatLng(airport?.second ?: 0.0, airport?.third ?: 0.0)
    }

    // Sets the user budget
    fun setBudget(budget: Double) {
        _userBudget.value = budget
    }

    // Sets the trip duration
    fun setDuration(duration: Long) {
        _userDuration.value = duration
    }

    // Sets the selected city
    fun setCity(city: String) {
        _userCity.value = city
        setLatitude(city)
        setLongitude(city)
    }

    // Sets the latitude for the given city
    fun setLatitude(city: String) {
        _userLatitude.value = getCoordinatesForCity(city).latitude
    }

    // Sets the longitude for the given city
    fun setLongitude(city: String) {
        _userLongitude.value = getCoordinatesForCity(city).longitude
    }

    // Sets the departure city
    fun setDeparture(departure: String) {
        _userDeparture.value = departure
    }
}