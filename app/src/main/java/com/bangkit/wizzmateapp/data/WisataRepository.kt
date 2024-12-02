package com.bangkit.wizzmateapp.data

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.bangkit.wizzmateapp.data.remote.response.DataItem
import com.bangkit.wizzmateapp.data.remote.retrofit.ApiService

class WisataRepository(private val apiService: ApiService) {
    fun getWisata(category: String): LiveData<PagingData<DataItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false,
                initialLoadSize = 5
            ),
            pagingSourceFactory = {
                WisataPagingSource(apiService, category)
            }
        ).liveData
    }
}