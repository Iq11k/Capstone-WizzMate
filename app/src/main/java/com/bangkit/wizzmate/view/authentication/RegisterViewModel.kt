package com.bangkit.wizzmate.view.authentication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bangkit.wizzmate.data.remote.request.RegisterRequest
import com.bangkit.wizzmate.data.remote.response.AuthResponse
import com.bangkit.wizzmate.data.remote.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {
    private val _isRegister = MutableLiveData<Boolean>()
    val isRegister: LiveData<Boolean> = _isRegister

    fun register(email: String, password: String){
        val request = RegisterRequest(email, password)
        val client = ApiConfig.getApiService().register(request)
        client.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                val responseBody = response.body()
                if (responseBody != null) {
                    _isRegister.value = true
                } else {
                    _isRegister.value = false
                    Log.e("Register gagal", "Regis failed, response body is null")
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                _isRegister.value = false
                Log.e("Register gagal", "onFailure: ${t.message.toString()}")
            }
        })

    }
}