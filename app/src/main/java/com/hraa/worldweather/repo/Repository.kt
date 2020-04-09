package com.hraa.worldweather.repo

import com.hraa.worldweather.api_current_weather.CurrentWeatherClient
import com.hraa.worldweather.api_sixteen_days.SixteenWeatherClient
import com.hraa.worldweather.current_weather_model.Data
import com.hraa.worldweather.room.WeatherDao
import com.hraa.worldweather.sixteen_weather_model.SixteenWeatherModel

class Repository(private val weatherDao: WeatherDao) {

    private val apiForecast = SixteenWeatherClient.sixteenWeatherApi

    private val apiCurrent = CurrentWeatherClient.currentWeatherApi

    suspend fun getIsCurrentLocation(cityName: String) = weatherDao.getIsCurrentLocation(cityName)

    suspend fun getSixteenDaysForecastLatLonFromApi(latitude: Long, longitude: Long, units: String) =
        apiForecast.getSixteenDaysForecastLatLon(latitude, longitude, units)

    suspend fun getSixteenDaysForecastCityNameFromApi(cityName: String, units: String) =
        apiForecast.getSixteenDaysForecastCityName(cityName, units)

    suspend fun getCurrentWeatherLatLonFromApi(latitude: Long, longitude: Long, units: String) =
        apiCurrent.getCurrentWeatherLatLon(latitude, longitude, units)

    suspend fun getCurrentWeatherCityNameFromApi(cityName: String, units: String) =
        apiCurrent.getCurrentWeatherCityName(cityName, units)

    suspend fun getCurrentWeatherByCityNameFromRoom(cityName: String) =
        weatherDao.getCurrentWeatherByCityName(cityName)

    suspend fun getCurrentWeatherByLatAndLonFromRoom(latitude: String, longitude: String) =
        weatherDao.getCurrentWeatherByLatAndLon(latitude, longitude)

    suspend fun getSixteenWeatherByCityNameFromRoom(cityName: String) =
        weatherDao.getSixteenWeatherByCityName(cityName)

    suspend fun getSixteenWeatherByLatAndLonFromRoom(latitude: String, longitude: String) =
        weatherDao.getSixteenWeatherByLatAndLon(latitude, longitude)

    suspend fun insertCurrentWeather(currentWeather: List<Data>) {
        weatherDao.insertCurrentWeather(currentWeather)
    }

    suspend fun insertSixteenWeather(sixteenWeatherModel: SixteenWeatherModel) {
        weatherDao.insertSixteenWeather(sixteenWeatherModel)
    }

    suspend fun getAllCitiesAvailable() = weatherDao.getAllCitiesAvailable()

    suspend fun getAllLocations() = weatherDao.getAllLocations()

}