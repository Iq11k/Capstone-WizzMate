package com.bangkit.wizzmate.view.main.estimator

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bangkit.wizzmate.R

class EstimatorFragment : Fragment() {

    companion object {
        fun newInstance() = EstimatorFragment()
    }

    private val viewModel: EstimatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_estimator, container, false)
    }
}