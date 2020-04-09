package com.hraa.worldweather.api_current_weather

import com.hraa.worldweather.constants.BASE_URL_CURRENT
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CurrentWeatherClient {

    var currentWeatherApi: CurrentWeatherApi

    init {

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_CURRENT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        currentWeatherApi = retrofit.create(CurrentWeatherApi::class.java)
    }
}