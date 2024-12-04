package com.bangkit.wizzmateapp.data.paging_source

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bangkit.wizzmateapp.data.remote.response.DataItem
import com.bangkit.wizzmateapp.data.remote.retrofit.ApiService

class WisataPagingSource(private val apiService: ApiService, val category: String) : PagingSource<Int, DataItem>() {
    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
    override fun getRefreshKey(state: PagingState<Int, DataItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DataItem> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val response = apiService.getDataByCategory(position, params.loadSize, category, "rating")
            val responseData = response.data
            Log.d("WisataPagingSource", "Loaded data: $responseData")

            // Make sure that if there's no data, there's no next page.
            val nextKey = if (responseData.isEmpty()) null else position + 1
            val prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1

            LoadResult.Page(
                data = responseData,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (exception: Exception) {
            Log.e("WisataPagingSource", "Error loading data", exception)
            LoadResult.Error(exception)
        }
    }

}