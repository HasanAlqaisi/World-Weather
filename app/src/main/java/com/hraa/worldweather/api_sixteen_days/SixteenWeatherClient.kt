package com.hraa.worldweather.api_sixteen_days

import com.hraa.worldweather.constants.BASE_URL_FORECAST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SixteenWeatherClient {

    var sixteenWeatherApi: SixteenWeatherApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_FORECAST)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        sixteenWeatherApi = retrofit.create(SixteenWeatherApi::class.java)
    }
}