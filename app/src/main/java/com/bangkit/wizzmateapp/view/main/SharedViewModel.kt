package com.bangkit.wizzmateapp.view.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class SharedViewModel : ViewModel() {
    val userBudget = MutableLiveData<Double>()
    val userDUration = MutableLiveData<Int>()

    fun getLocationLatLong(locationName: String) : LatLng {
        val latitude = 37.7749
        val longitude = -122.4194
        return LatLng(latitude, longitude)
    }

    fun setBudget(budget: Double) {
        userBudget.value = budget
    }

    fun setDuration(duration: Int) {
        userDUration.value = duration
    }
}