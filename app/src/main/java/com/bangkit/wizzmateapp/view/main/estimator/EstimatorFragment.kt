package com.bangkit.wizzmateapp.view.main.estimator

import ResultFragment
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bangkit.wizzmateapp.R
import com.bangkit.wizzmateapp.databinding.FragmentEstimatorBinding
import com.bangkit.wizzmateapp.ml.CfModel
import com.bangkit.wizzmateapp.view.main.SharedViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar

class EstimatorFragment : Fragment() {
    private lateinit var binding: FragmentEstimatorBinding
    private var searchJob: Job? = null

    private val viewModel: EstimatorViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEstimatorBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonHariAwal.setOnClickListener {
            binding.dpHariPertama.visibility = View.VISIBLE
        }
        binding.buttonHariAkhir.setOnClickListener {
            binding.dpHariKedua.visibility = View.VISIBLE
        }
        binding.dpHariPertama.setOnDateChangedListener { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            binding.buttonHariAwal.text = selectedDate // Display the selected date on the button
            binding.dpHariPertama.visibility = View.GONE // Hide the DatePicker after selection
            countDay()
        }

        binding.dpHariKedua.setOnDateChangedListener { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            binding.buttonHariAkhir.text = selectedDate // Display the selected date on the button
            binding.dpHariKedua.visibility = View.GONE // Hide the DatePicker after selection
            countDay()
        }

        val departure = resources.getStringArray(R.array.departure)
        val adapter = ArrayAdapter(requireContext(), R.layout.drop_down_item, departure)
        binding.edTitikAwal.setAdapter(adapter)

        val kotaTujuan = resources.getStringArray(R.array.kota_tujuan)
        val adapter2 = ArrayAdapter(requireContext(), R.layout.drop_down_item, kotaTujuan)
        binding.edTujuan.setAdapter(adapter2)

        binding.buttonEstimate.setOnClickListener{
            if (binding.buttonHariAwal.text != getString(R.string.hari_pertama) && binding.buttonHariAkhir.text != getString(R.string.hari_terakhir)
                && binding.edTitikAwal.text.isNotEmpty() && binding.edTujuan.text.isNotEmpty() && binding.edBudget.text!!.isNotEmpty()){
                val bundle = Bundle()
                bundle.putString("estimate_result", "Your estimate is ready!") // Replace with real result
                bundle.putLong("days_between", countDay())

                val resultFragment = ResultFragment()
                resultFragment.arguments = bundle
                activity?.findViewById<View>(R.id.bottom_navigation_view)?.visibility = View.GONE
                // Navigate to the ResultFragment
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, resultFragment) // Replace with your fragment container ID
                    .addToBackStack(null) // Add to backstack for proper navigation
                    .commit()
            } else {
                Toast.makeText(context, "Fill All the Input Needed", Toast.LENGTH_SHORT).show()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun countDay() : Long{
        val startDate = LocalDate.of(
            binding.dpHariPertama.year,
            binding.dpHariPertama.month + 1,
            binding.dpHariPertama.dayOfMonth
        )

        val endDate = LocalDate.of(
            binding.dpHariKedua.year,
            binding.dpHariKedua.month + 1,
            binding.dpHariKedua.dayOfMonth
        )
        var daysBetween: Long = 0

        if (isDateInFuture(binding.dpHariKedua.year, binding.dpHariKedua.month, binding.dpHariKedua.dayOfMonth) && isDateInFuture(binding.dpHariPertama.year, binding.dpHariPertama.month, binding.dpHariPertama.dayOfMonth)){
            daysBetween = ChronoUnit.DAYS.between(startDate, endDate)
            if (daysBetween >= 0){
                binding.tvJumlahHari.text = "$daysBetween Hari"
            } else {
                binding.tvJumlahHari.text = "Not A Valid Day's Count"
                daysBetween = 0
            }
            binding.buttonHariAkhir.apply {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.primaryColor))
                strokeColor = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.primaryColor)
                )
            }
            binding.buttonHariAwal.apply {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.primaryColor))
                strokeColor = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.primaryColor)
                )
            }
        } else {
            if (!isDateInFuture(binding.dpHariKedua.year, binding.dpHariKedua.month, binding.dpHariKedua.dayOfMonth)){
                binding.buttonHariAkhir.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    strokeColor = ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.red)
                    )
                }
            } else if (!isDateInFuture(binding.dpHariPertama.year, binding.dpHariPertama.month, binding.dpHariPertama.dayOfMonth)){
                binding.buttonHariAwal.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    strokeColor = ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.red)
                    )
                }
            }
            Toast.makeText(context, "Tanggal Tidak Valid", Toast.LENGTH_SHORT).show()
        }
        return daysBetween
    }

    fun isDateInFuture(year: Int, month: Int, day: Int): Boolean {
        // Mendapatkan instance Calendar untuk hari ini
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        // Membuat instance Calendar untuk tanggal yang dipilih
        val selectedDate = Calendar.getInstance()
        selectedDate.set(year, month, day, 0, 0, 0)
        selectedDate.set(Calendar.MILLISECOND, 0)

        // Membandingkan tanggal
        return selectedDate.timeInMillis >= today.timeInMillis
    }
}