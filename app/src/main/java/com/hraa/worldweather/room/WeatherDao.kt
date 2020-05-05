package com.hraa.worldweather.room

import androidx.room.*
import com.hraa.worldweather.constants.CURRENT_WEATHER_TABLE
import com.hraa.worldweather.constants.SIXTEEN_WEATHER_TABLE
import com.hraa.worldweather.current_weather_model.Data
import com.hraa.worldweather.forecast_weather_model.ForecastWeatherModel

// This is an interface to deal with data in local room database.
@Dao
interface WeatherDao {

    // Insert current Weather info
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentWeather(currentWeather: List<Data>)

    // Insert Forecast Info
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecastWeather(forecastWeatherModel: ForecastWeatherModel)

    // Check if the given city marked as a current location
    @Query("SELECT isCurrentLocation FROM $CURRENT_WEATHER_TABLE WHERE cityName = :cityName")
    suspend fun isCurrentLocation(cityName: String): Boolean?

    // Get all locations from database
    @Query("SELECT cityName, isCurrentLocation FROM $CURRENT_WEATHER_TABLE")
    suspend fun getAllLocations(): List<Data>

    // Get current weather info depend on city name
    @Query("SELECT * FROM $CURRENT_WEATHER_TABLE WHERE cityName = :cityName")
    suspend fun getCurrentWeatherByCityName(cityName: String): List<Data>

    // Get current weather info depend on location
    @Query("SELECT * FROM $CURRENT_WEATHER_TABLE WHERE lat = :latitude AND lon = :longitude ")
    suspend fun getCurrentWeatherByLocation(latitude: String, longitude: String): List<Data>

    // Get weather forecast info depend on city name
    @Query("SELECT * FROM $SIXTEEN_WEATHER_TABLE WHERE cityName = :cityName")
    suspend fun getWeatherForecastByCityName(cityName: String): ForecastWeatherModel

    // Get weather forecast info depend on location
    @Query("SELECT * FROM $SIXTEEN_WEATHER_TABLE WHERE lat = :latitude AND lon = :longitude")
    suspend fun getWeatherForecastByLocation(
        latitude: String,
        longitude: String
    ): ForecastWeatherModel

    // Delete current weather info depend on city name
    @Query("DELETE FROM CURRENT_WEATHER_TABLE WHERE cityName = :cityName")
    suspend fun deleteCurrentWeatherLocation(cityName: String)

    // Delete forecast weather info depend on city name
    @Query("DELETE FROM SIXTEEN_WEATHER_TABLE WHERE cityName = :cityName")
    suspend fun deleteForecastLocation(cityName: String)
}