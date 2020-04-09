package com.hraa.worldweather.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hraa.worldweather.constants.CURRENT_WEATHER_TABLE
import com.hraa.worldweather.constants.SIXTEEN_WEATHER_TABLE
import com.hraa.worldweather.current_weather_model.Data
import com.hraa.worldweather.sixteen_weather_model.SixteenWeatherModel

@Dao
interface WeatherDao {

    @Query("SELECT isCurrentLocation FROM $CURRENT_WEATHER_TABLE WHERE cityName = :cityName")
    suspend fun getIsCurrentLocation(cityName: String): Boolean?

    @Query("SELECT cityName FROM $CURRENT_WEATHER_TABLE")
    suspend fun getAllCitiesAvailable(): String

    @Query("SELECT * FROM $CURRENT_WEATHER_TABLE")
    suspend fun getAllLocations(): List<Data>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentWeather(currentWeather: List<Data>)

    @Query("SELECT * FROM $CURRENT_WEATHER_TABLE WHERE cityName = :cityName")
    suspend fun getCurrentWeatherByCityName(cityName: String): List<Data>

    @Query("SELECT * FROM $CURRENT_WEATHER_TABLE WHERE lat = :latitude AND lon = :longitude ")
    suspend fun getCurrentWeatherByLatAndLon(latitude: String, longitude: String): List<Data>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSixteenWeather(sixteenWeatherModel: SixteenWeatherModel)

    @Query("SELECT * FROM $SIXTEEN_WEATHER_TABLE WHERE cityName = :cityName")
    suspend fun getSixteenWeatherByCityName(cityName: String): SixteenWeatherModel

    @Query("SELECT * FROM $SIXTEEN_WEATHER_TABLE WHERE lat = :latitude AND lon = :longitude")
    suspend fun getSixteenWeatherByLatAndLon(
        latitude: String,
        longitude: String
    ): SixteenWeatherModel
}