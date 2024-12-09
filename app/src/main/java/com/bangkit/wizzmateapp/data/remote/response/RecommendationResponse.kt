package com.bangkit.wizzmateapp.data.remote.response

import com.google.gson.annotations.SerializedName

data class RecommendationResponse(

	@field:SerializedName("recommended_flights")
	val recommendedFlights: List<Any>,

	@field:SerializedName("total_time_per_day")
	val totalTimePerDay: List<Any>,

	@field:SerializedName("recommendations")
	val recommendations: List<List<RecommendationsItemItem>>,

	@field:SerializedName("total_budget_per_day")
	val totalBudgetPerDay: List<Int>,

	@field:SerializedName("mse")
	val mse: Any
)

data class RecommendationsItemItem(

	@field:SerializedName("distance_km")
	val distanceKm: Any,

	@field:SerializedName("Rating")
	val rating: Any,

	@field:SerializedName("cf_rating")
	val cfRating: Any,

	@field:SerializedName("travel_time")
	val travelTime: Any,

	@field:SerializedName("Time_Minutes")
	val timeMinutes: Any,

	@field:SerializedName("Place_Id")
	val placeId: Int,

	@field:SerializedName("Price")
	val price: Int,

	@field:SerializedName("Long")
	val long: Any,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("category")
	val category: String,

	@field:SerializedName("similarity_score")
	val similarityScore: Any,

	@field:SerializedName("Lat")
	val lat: Any,

	@field:SerializedName("mse")
	val mse: Any
)
