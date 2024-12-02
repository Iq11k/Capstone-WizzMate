package com.bangkit.wizzmateapp.view.authentication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.wizzmateapp.data.local.SessionPreferences
import com.bangkit.wizzmateapp.data.remote.request.LoginRequest
import com.bangkit.wizzmateapp.data.remote.response.AuthResponse
import com.bangkit.wizzmateapp.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(private val pref: SessionPreferences) : ViewModel() {
    private val _isLogin = MutableLiveData<Boolean>()
    val isLogin: LiveData<Boolean> = _isLogin
    private val _responseBody = MutableLiveData<AuthResponse>()
    val responseBody : LiveData<AuthResponse> = _responseBody

    fun login(email: String, password: String){
        val request = LoginRequest(email, password)
        val client = ApiConfig.getApiService().login(request)
        client.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                val responseBody = response.body()
                if (responseBody != null) {
                    _isLogin.value = true
                    _responseBody.value= response.body()
                    viewModelScope.launch {
                        pref.saveToken(responseBody.user.id)
                        pref.saveLoginStatus(true)
                        pref.saveusername(responseBody.user.username)
                    }
                } else {
                    _isLogin.value = false
                    Log.e("Login gagal", "Login failed, response body is null")
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                _isLogin.value = false
                Log.e("Login gagal", "onFailure: ${t.message.toString()}")
            }
        })
    }

    fun oneTimeLogin(email: String, password: String){
        val request = LoginRequest(email, password)
        val client = ApiConfig.getApiService().login(request)
        client.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                val responseBody = response.body()
                if (responseBody != null) {
                    _isLogin.value = true
                    _responseBody.value= response.body()
                    viewModelScope.launch {
                        pref.saveusername(responseBody.user.username)
                    }
                } else {
                    _isLogin.value = false
                    Log.e("Login gagal", "Login failed, response body is null")
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                _isLogin.value = false
                Log.e("Login gagal", "onFailure: ${t.message.toString()}")
            }
        })
    }

    fun isAlreadyLogin() {
        viewModelScope.launch {
            pref.isLogin().collect {
                _isLogin.value = it
            }
        }
    }
}