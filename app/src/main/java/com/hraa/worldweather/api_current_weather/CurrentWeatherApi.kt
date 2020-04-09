package com.hraa.worldweather.api_current_weather

import com.hraa.worldweather.constants.API_KEY
import com.hraa.worldweather.current_weather_model.CurrentWeatherModel
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrentWeatherApi {

    @GET("current?key=$API_KEY")
    suspend fun getCurrentWeatherLatLon(
        @Query("lat") latitude: Long,
        @Query("lon") longitude: Long,
        @Query("units") units: String
    ): CurrentWeatherModel

    @GET("current?key=$API_KEY")
    suspend fun getCurrentWeatherCityName(
        @Query("city") cityName: String,
        @Query("units") units: String
    ): CurrentWeatherModel
}