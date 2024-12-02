package com.bangkit.wizzmateapp.view.main.estimator

import android.os.Build
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.bangkit.wizzmateapp.databinding.FragmentEstimatorBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class EstimatorFragment : Fragment() {
    private lateinit var binding: FragmentEstimatorBinding
    private var searchJob: Job? = null

    private val viewModel: EstimatorViewModel by viewModels()

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
        viewModel.wisata.observe(viewLifecycleOwner) { dataItems ->
            val options = mutableSetOf<String>()
            dataItems.forEach {
                options.add(it.placeName)
                options.add(it.city)
            }
            setupDropdownMenu(options.toList(), binding.edTitikAwal)
            setupDropdownMenu(options.toList(), binding.edTujuan)
        }

        binding.edTitikAwal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let { query ->
                    searchJob?.cancel()
                    searchJob = lifecycleScope.launch {
                        if (query.isNotEmpty()) {
                            viewModel.searchWisata(query.toString())
                        }
                    }
                }
            }

            override fun onTextChanged(query: CharSequence?, start: Int, before: Int, count: Int) {
                query?.let {
                    searchJob?.cancel()
                    searchJob = lifecycleScope.launch {
                        if (query.isNotEmpty()) {
                            viewModel.searchWisata(query.toString())
                        }
                    }
                }
            }
        })
        binding.edTujuan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let { query ->
                    searchJob?.cancel()
                    searchJob = lifecycleScope.launch {
                        if (query.isNotEmpty()) {
                            viewModel.searchWisata(query.toString())
                        }
                    }
                }
            }

            override fun onTextChanged(query: CharSequence?, start: Int, before: Int, count: Int) {
                query?.let {
                    searchJob?.cancel() // Cancel the previous job
                    searchJob = lifecycleScope.launch {
                        if (query.isNotEmpty()) {
                            viewModel.searchWisata(query.toString())
                        }
                    }
                }
            }
        })

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
    }

    private fun setupDropdownMenu(options: List<String>, editText: AutoCompleteTextView) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)
        editText.setAdapter(adapter)
        editText.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as String
            Toast.makeText(requireContext(), "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun countDay(){
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

        val daysBetween = ChronoUnit.DAYS.between(startDate, endDate)
        if (daysBetween >= 0){
            binding.tvJumlahHari.text = "$daysBetween Hari"
        } else {
            binding.tvJumlahHari.text = "Not A Valid Day's Count"
        }
    }
}