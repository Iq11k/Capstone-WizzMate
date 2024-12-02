package com.bangkit.wizzmateapp.data.remote.response

import com.google.gson.annotations.SerializedName

data class WisataResponse(

	@field:SerializedName("size")
	val size: Int,

	@field:SerializedName("data")
	val data: List<DataItem>,

	@field:SerializedName("page")
	val page: Int,

	@field:SerializedName("total_pages")
	val totalPages: Int,

	@field:SerializedName("total_items")
	val totalItems: Int,

	@field:SerializedName("status")
	val status: String
)

data class DataItem(

	@field:SerializedName("Place_City")
	val placeCity: String,

	@field:SerializedName("Description")
	val description: String,

	@field:SerializedName("Category")
	val category: String,

	@field:SerializedName("Place_Name")
	val placeName: String,

	@field:SerializedName("image_url")
	val imageUrl: String,

	@field:SerializedName("Rating")
	val rating: Any,

	@field:SerializedName("City")
	val city: String,

	@field:SerializedName("Time_Minutes")
	val timeMinutes: Int,

	@field:SerializedName("Unnamed: 11")
	val unnamed11: Any,

	@field:SerializedName("Unnamed: 12")
	val unnamed12: Int,

	@field:SerializedName("Place_Id")
	val placeId: Int,

	@field:SerializedName("Price")
	val price: Int,

	@field:SerializedName("Coordinate")
	val coordinate: String,

	@field:SerializedName("Long")
	val long: Any,

	@field:SerializedName("Lat")
	val lat: Any
)
