import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.bangkit.wizzmateapp.data.remote.response.DataItem

class ResultViewModel : ViewModel() {

    private val _destinations = MutableLiveData<PagingData<DataItem>>()
    val destinations: LiveData<PagingData<DataItem>> = _destinations

    private val _totalBudget = MutableLiveData<Double>()
    val totalBudget: LiveData<Double> = _totalBudget

    fun setDestinations(destinationList: PagingData<DataItem>) {
        _destinations.value = destinationList
    }

    fun setTotalBudget(budget: Double) {
        _totalBudget.value = budget
    }
}