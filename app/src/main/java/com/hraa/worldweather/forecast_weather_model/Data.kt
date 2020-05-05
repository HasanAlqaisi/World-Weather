package com.hraa.worldweather.forecast_weather_model

import com.google.gson.annotations.SerializedName

data class Data(
    val datetime: String?,
    @SerializedName("max_temp")
    val maxTemp: Double?,
    @SerializedName("min_temp")
    val minTemp: Double?,
    @SerializedName("sunrise_ts")
    val sunriseTs: Int?,
    @SerializedName("sunset_ts")
    val sunsetTs: Int?,
    val uv: Double?,
    val vis: Double?,
    val weather: Weather?,
    @SerializedName("wind_spd")
    val windSpd: Double?,
    val precip: Double?,
    val ozone: Double?,
    @SerializedName("wind_cdir")
    val windCdir: String?
    )