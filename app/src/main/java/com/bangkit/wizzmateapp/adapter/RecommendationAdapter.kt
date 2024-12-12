package com.bangkit.wizzmateapp.adapter

import android.annotation.SuppressLint
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.wizzmateapp.R
import com.bangkit.wizzmateapp.adapter.WisataAdapter.MyViewHolder
import com.bangkit.wizzmateapp.data.remote.response.DataItem
import com.bangkit.wizzmateapp.data.remote.response.RecommendationsItemItem
import com.bangkit.wizzmateapp.databinding.DestinationItemBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.util.Locale

class RecommendationAdapter :
    ListAdapter<RecommendationsItemItem, RecommendationAdapter.RecommendationViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RecommendationsItemItem>() {
            override fun areItemsTheSame(oldItem: RecommendationsItemItem, newItem: RecommendationsItemItem): Boolean {
                return oldItem.placeId == newItem.placeId
            }

            override fun areContentsTheSame(oldItem: RecommendationsItemItem, newItem: RecommendationsItemItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val binding =
            DestinationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecommendationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val recommendation = getItem(position)
        recommendation?.let {
            holder.bind(it) // Pass the RecommendationsItemItem object
        }
    }

    inner class RecommendationViewHolder(private val binding: DestinationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val geocoder = Geocoder(itemView.context, Locale.getDefault())

        @SuppressLint("SetTextI18n")
        fun bind(recommendation: RecommendationsItemItem) {
            binding.tvDestinationName.text = recommendation.name
            Glide.with(itemView.context)
                .load(recommendation.imageUrl)
                .error(R.drawable.error_image_loading)
                .transform(RoundedCorners(16))
                .centerCrop()
                .into(binding.ivDestination)
            binding.apply {
                tvDestinationRating.text = recommendation.rating.toString()
                tvCategory.text = "Rp. ${recommendation.price}"
            }
            // Get the address from latitude and longitude (if available)
            val lat = recommendation.lat
            val lng = recommendation.long

            if (lat != null && lng != null) {
                val address = getAddressFromCoordinates(lat.toString().toDouble(), lng.toString().toDouble())
                binding.tvDestinationLocation.text = address
            }
        }
        private fun getAddressFromCoordinates(latitude: Double, longitude: Double): String {
            return try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (addresses!!.isNotEmpty()) {
                    val address = addresses.get(0)
                    address!!.getAddressLine(0)
                } else {
                    "Address not found"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "Geocoder service unavailable"
            }
        }
    }

}

