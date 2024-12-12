import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.wizzmateapp.data.remote.request.ModelRequest
import com.bangkit.wizzmateapp.data.remote.response.RecommendationResponse
import com.bangkit.wizzmateapp.data.remote.retrofit.MLApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback

class ResultViewModel(private val apiService: MLApiService) : ViewModel() {

    private val _recommendationResponse = MutableLiveData<RecommendationResponse>()
    val recommendationResponse: LiveData<RecommendationResponse> = _recommendationResponse

    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> = _loadingState

    fun fetchRecommendation(modelRequest: ModelRequest) {
        _loadingState.value = true // Indicate that loading has started

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val call = apiService.recomend(modelRequest)
                call.enqueue(object : Callback<RecommendationResponse> {
                    override fun onResponse(
                        call: Call<RecommendationResponse>,
                        response: retrofit2.Response<RecommendationResponse>
                    ) {
                        _loadingState.postValue(false) // Indicate that loading has finished
                        if (response.isSuccessful) {
                            _recommendationResponse.postValue(response.body())
                        } else {
                            Log.e("ResultViewModel", "Error: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<RecommendationResponse>, t: Throwable) {
                        _loadingState.postValue(false) // Indicate that loading has finished
                        // Handle failure
                    }
                })
            } catch (e: Exception) {
                _loadingState.postValue(false) // Indicate that loading has finished
                // Handle exception
            }
        }
    }
}
