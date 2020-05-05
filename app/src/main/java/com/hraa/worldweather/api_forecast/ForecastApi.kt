package com.hraa.worldweather.api_forecast

import com.hraa.worldweather.constants.API_KEY
import com.hraa.worldweather.forecast_weather_model.ForecastWeatherModel
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastApi {

    @GET("daily?key=$API_KEY")
    suspend fun getWeatherForecastByLocation(
        @Query("lat") lat: Long,
        @Query("lon") lon: Long,
        @Query("units") units: String
    ): ForecastWeatherModel

    @GET("daily?key=$API_KEY")
    suspend fun getWeatherForecastByCityName(
        @Query("city") cityName: String,
        @Query("units") units: String
    ): ForecastWeatherModel
}