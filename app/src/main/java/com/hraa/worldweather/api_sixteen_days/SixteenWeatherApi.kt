package com.hraa.worldweather.api_sixteen_days

import com.hraa.worldweather.constants.API_KEY
import com.hraa.worldweather.sixteen_weather_model.SixteenWeatherModel
import retrofit2.http.GET
import retrofit2.http.Query

interface SixteenWeatherApi {

    @GET("daily?key=$API_KEY")
    suspend fun getSixteenDaysForecastLatLon(
        @Query("lat") lat: Long,
        @Query("lon") lon: Long,
        @Query("units") units: String
    ): SixteenWeatherModel

    @GET("daily?key=$API_KEY")
    suspend fun getSixteenDaysForecastCityName(
        @Query("city") cityName: String,
        @Query("units") units: String
    ): SixteenWeatherModel
}