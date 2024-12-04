package com.bangkit.wizzmateapp.data.paging_source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bangkit.wizzmateapp.data.remote.response.DataItem
import com.bangkit.wizzmateapp.data.remote.retrofit.ApiService

class SearchPagingSource(private val apiService: ApiService, val query: String) : PagingSource<Int, DataItem>() {
    override fun getRefreshKey(state: PagingState<Int, DataItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DataItem> {
        val page = params.key ?: 1
        return try {
            val response = apiService.searchData(query)
            val data = response.data

            LoadResult.Page(
                data = data,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (data.isEmpty() || data.size < params.loadSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}