import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.wizzmateapp.R
import com.bangkit.wizzmateapp.adapter.FlightAdapter
import com.bangkit.wizzmateapp.adapter.RecommendationAdapter
import com.bangkit.wizzmateapp.adapter.WisataAdapter
import com.bangkit.wizzmateapp.data.remote.retrofit.MLApiConfig
import com.bangkit.wizzmateapp.data.remote.retrofit.MLApiService
import com.bangkit.wizzmateapp.databinding.FragmentResultBinding
import com.bangkit.wizzmateapp.view.main.SharedViewModel
import com.bangkit.wizzmateapp.view.main.result.ResultViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class ResultFragment : Fragment() {

    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalBudgetTextView: TextView
    private lateinit var wisataAdapter: WisataAdapter
    private lateinit var binding: FragmentResultBinding
    private lateinit var resultViewModel: ResultViewModel
    private lateinit var apiService: MLApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentResultBinding.inflate(inflater, container, false)
        val root: View = binding.root
        apiService = MLApiConfig.getApiService()
        resultViewModel = ViewModelProvider(
            requireActivity(),
            ResultViewModelFactory(apiService)
        )[ResultViewModel::class.java]
        return root
    }

    @RequiresApi(35)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_destinations)
        totalBudgetTextView = view.findViewById(R.id.tv_total_budget)

        recyclerView.layoutManager = LinearLayoutManager(context)
        wisataAdapter = WisataAdapter()
        recyclerView.adapter = wisataAdapter

        viewModel.userCity.observe(viewLifecycleOwner){ city ->
            Log.d("ResultFragment", "Observed City: $city")
            binding.edTujuan.setText(city)
        }
        viewModel.userDeparture.observe(viewLifecycleOwner) { departure ->
            binding.edDaerahAsal.setText(departure)
        }
        viewModel.userDuration.observe(viewLifecycleOwner) { duration ->
            binding.edWaktu.setText(duration.toString())
        }
        val wisataAdapter = RecommendationAdapter()
        val flightAdapter = FlightAdapter()
        resultViewModel.recommendationResponse.observe(viewLifecycleOwner) { response ->
            wisataAdapter.submitList(response.recommendations[0])
            binding.rvDestinations.adapter = wisataAdapter
            var totalBudget = 0.0
            for (item in response.totalBudgetPerDay) {
                totalBudget += item.toString().toDouble()
            }
            val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))
            var totalBudgetPerday = "Your Daily Expanse\n"
            for (item in response.totalBudgetPerDay) {
                val formattedItem = formatter.format(item)
                totalBudgetPerday += "Day ${response.totalBudgetPerDay.indexOf(item) + 1}: Rp. $formattedItem \n"
            }
            totalBudgetPerday += "\nTotal: Rp. $totalBudget"
            binding.tvTotalBudget.text = totalBudgetPerday

            if (response.recommendedFlights.isNotEmpty()) {
                binding.rvFlight.adapter = flightAdapter
                flightAdapter.submitList(response.recommendedFlights)
                binding.rvFlight.layoutManager = LinearLayoutManager(context)
            } else {
                binding.rvFlight.visibility = View.GONE
                binding.tvRekomendasiPenerbangan.text = getString(R.string.no_flight_recommendation)
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.findViewById<View>(R.id.bottom_navigation_view)?.visibility = View.VISIBLE
    }
}