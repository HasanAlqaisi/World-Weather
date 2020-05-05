package com.hraa.worldweather.repo

import com.hraa.worldweather.api_current_weather.CurrentWeatherClient
import com.hraa.worldweather.api_forecast.SixteenWeatherClient
import com.hraa.worldweather.current_weather_model.Data
import com.hraa.worldweather.room.WeatherDao
import com.hraa.worldweather.forecast_weather_model.ForecastWeatherModel

class Repository(private val weatherDao: WeatherDao) {

    private val apiForecast = SixteenWeatherClient.forecastApi

    private val apiCurrent = CurrentWeatherClient.currentWeatherApi

    suspend fun isCurrentLocation(cityName: String) = weatherDao.isCurrentLocation(cityName)

    suspend fun getWeatherForecastByLocationFromApi(
        latitude: Long,
        longitude: Long,
        units: String
    ) =
        apiForecast.getWeatherForecastByLocation(latitude, longitude, units)

    suspend fun getWeatherForecastByCityNameFromApi(cityName: String, units: String) =
        apiForecast.getWeatherForecastByCityName(cityName, units)

    suspend fun getCurrentWeatherByLocationFromApi(latitude: Long, longitude: Long, units: String) =
        apiCurrent.getCurrentWeatherByLocation(latitude, longitude, units)

    suspend fun getCurrentWeatherByCityNameFromApi(cityName: String, units: String) =
        apiCurrent.getCurrentWeatherByCityName(cityName, units)

    suspend fun getCurrentWeatherByCityNameFromRoom(cityName: String) =
        weatherDao.getCurrentWeatherByCityName(cityName)

    suspend fun getCurrentWeatherByLocationFromRoom(latitude: String, longitude: String) =
        weatherDao.getCurrentWeatherByLocation(latitude, longitude)

    suspend fun getWeatherForecastByCityNameFromRoom(cityName: String) =
        weatherDao.getWeatherForecastByCityName(cityName)

    suspend fun getWeatherForecastByLocationFromRoom(latitude: String, longitude: String) =
        weatherDao.getWeatherForecastByLocation(latitude, longitude)

    suspend fun insertCurrentWeatherToRoom(currentWeather: List<Data>) {
        weatherDao.insertCurrentWeather(currentWeather)
    }

    suspend fun insertForecastWeatherToRoom(forecastWeatherModel: ForecastWeatherModel) {
        weatherDao.insertForecastWeather(forecastWeatherModel)
    }

    suspend fun getAllLocationsFromRoom() = weatherDao.getAllLocations()

    suspend fun deleteCurrentWeatherLocation(cityName: String) {
        weatherDao.deleteCurrentWeatherLocation(cityName)
    }

    suspend fun deleteForecastLocation(cityName: String) {
        weatherDao.deleteForecastLocation(cityName)
    }
}