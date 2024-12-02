package com.bangkit.wizzmateapp.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bangkit.wizzmateapp.data.WisataRepository
import com.bangkit.wizzmateapp.data.local.SessionPreferences
import com.bangkit.wizzmateapp.data.remote.response.DataItem
import kotlinx.coroutines.launch

class MainViewModel(wisataRepository: WisataRepository, val pref: SessionPreferences) : ViewModel() {
    val wisata: LiveData<PagingData<DataItem>> = wisataRepository.getWisata().cachedIn(viewModelScope)
    val username: LiveData<String?> = pref.getusername().asLiveData()

    fun logout(){
        viewModelScope.launch {
            pref.logout()
        }
    }
}