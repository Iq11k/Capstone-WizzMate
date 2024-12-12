package com.bangkit.wizzmateapp.view.main.estimator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.wizzmateapp.data.remote.response.DataItem
import com.bangkit.wizzmateapp.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch

class EstimatorViewModel : ViewModel() {
    private val _wisata = MutableLiveData<List<DataItem>>()
    val wisata: LiveData<List<DataItem>> = _wisata



}