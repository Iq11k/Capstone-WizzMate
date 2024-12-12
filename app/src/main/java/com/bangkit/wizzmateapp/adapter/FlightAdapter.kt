package com.bangkit.wizzmateapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.wizzmateapp.data.remote.response.RecommendationResponse
import com.bangkit.wizzmateapp.data.remote.response.RecommendationsItemItem
import com.bangkit.wizzmateapp.data.remote.response.RecommendedFlightsItem
import com.bangkit.wizzmateapp.databinding.ItemFlightBinding

class FlightAdapter : ListAdapter<RecommendedFlightsItem, FlightAdapter.FlightViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RecommendedFlightsItem>() {
            override fun areItemsTheSame(oldItem: RecommendedFlightsItem, newItem: RecommendedFlightsItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: RecommendedFlightsItem, newItem: RecommendedFlightsItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val binding = ItemFlightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FlightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        val flight = getItem(position)
        holder.bind(flight)
    }

    inner class FlightViewHolder(private val binding: ItemFlightBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(flight: RecommendedFlightsItem) {
            binding.tvAirline.text = flight.airline
            binding.tvDepartureAirport.text = flight.departureAirportName
            binding.tvDepartureTime.text = flight.departureTime
            binding.tvArrivalAirport.text = flight.arrivalAirportName
            binding.tvArrivalTime.text = flight.arrivalTime
            binding.tvDuration.text = "${flight.duration} mins"
            binding.tvPrice.text = "Rp. ${flight.price}"
            binding.tvTravelClass.text = flight.travelClass
        }
    }
}
