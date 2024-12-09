import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.wizzmateapp.R
import com.bangkit.wizzmateapp.adapter.WisataAdapter

class ResultFragment : Fragment() {

    private lateinit var viewModel: ResultViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalBudgetTextView: TextView
    private lateinit var saveButton: Button
    private lateinit var wisataAdapter: WisataAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(ResultViewModel::class.java)

        recyclerView = view.findViewById(R.id.rv_destinations)
        totalBudgetTextView = view.findViewById(R.id.tv_total_budget)
        saveButton = view.findViewById(R.id.btn_save)

        recyclerView.layoutManager = LinearLayoutManager(context)
        wisataAdapter = WisataAdapter()
        recyclerView.adapter = wisataAdapter

        viewModel.destinations.observe(viewLifecycleOwner, Observer { pagingData ->
            wisataAdapter.submitData(lifecycle, pagingData)
        })

        viewModel.totalBudget.observe(viewLifecycleOwner, Observer { budget ->
            totalBudgetTextView.text = "Kemungkinan Total Budget: $budget"
        })

        saveButton.setOnClickListener {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.findViewById<View>(R.id.bottom_navigation_view)?.visibility = View.VISIBLE
    }
}