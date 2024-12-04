package com.bangkit.wizzmateapp.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bangkit.wizzmateapp.data.WisataRepository
import com.bangkit.wizzmateapp.data.local.SessionPreferences
import com.bangkit.wizzmateapp.data.remote.response.DataItem
import com.bangkit.wizzmateapp.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch

class MainViewModel(
    private val wisataRepository: WisataRepository,
    private val pref: SessionPreferences
) : ViewModel() {

    private val _category = MutableLiveData<String>()
    val category: LiveData<String> get() = _category

    lateinit var searchResult: LiveData<PagingData<DataItem>>

    val wisata: LiveData<PagingData<DataItem>> = _category.switchMap { category ->
        wisataRepository.getWisata(category).cachedIn(viewModelScope)
    }

    val username: LiveData<String?> = pref.getusername().asLiveData()

    fun logout() {
        viewModelScope.launch {
            pref.logout()
        }
    }

    fun setCategory(category: String) {
        if (_category.value != category) {
            _category.value = category
        }
    }

    fun searchWisata(q: String){
        viewModelScope.launch {
            searchResult = wisataRepository.searchWisata(q).cachedIn(viewModelScope)
        }
    }
}