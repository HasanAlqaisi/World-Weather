package com.hraa.worldweather.api_forecast

import com.hraa.worldweather.constants.BASE_URL_FORECAST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SixteenWeatherClient {

    var forecastApi: ForecastApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_FORECAST)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        forecastApi = retrofit.create(ForecastApi::class.java)
    }
}