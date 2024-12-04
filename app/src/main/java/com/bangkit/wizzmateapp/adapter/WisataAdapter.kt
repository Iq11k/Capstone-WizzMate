package com.bangkit.wizzmateapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.wizzmateapp.R
import com.bangkit.wizzmateapp.data.remote.response.DataItem
import com.bangkit.wizzmateapp.view.detail.DetailActivity
import com.bangkit.wizzmateapp.databinding.DestinationItemBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class WisataAdapter : PagingDataAdapter<DataItem, WisataAdapter.MyViewHolder>(DIFF_CALLBACK) {

    class MyViewHolder(val binding: DestinationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(wisata: DataItem) {
            binding.tvDestinationName.text = wisata.placeName
            Glide.with(itemView.context)
                .load(wisata.imageUrl)
                .error(R.drawable.error_image_loading)
                .transform(RoundedCorners(16))
                .centerCrop()
                .into(binding.ivDestination)
            binding.apply {
                tvDestinationLocation.text = wisata.city
                tvDestinationRating.text = wisata.rating.toString()
                tvCategory.text = wisata.category
            }
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val wisata = getItem(position)
        holder.bind(wisata!!)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra("LATITUDE", wisata.lat.toString().toDouble())
                putExtra("LONGITUDE", wisata.long.toString().toDouble())
                putExtra("IMAGE_URL", wisata.imageUrl)
                putExtra("PLACE_NAME", wisata.placeName)
                putExtra("CITY", wisata.city)
                putExtra("RATING", wisata.rating.toString().toDouble())
                putExtra("CATEGORY", wisata.category)
                putExtra("DESCRIPTION", wisata.description)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            DestinationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DataItem>() {
            override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: DataItem,
                newItem: DataItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}