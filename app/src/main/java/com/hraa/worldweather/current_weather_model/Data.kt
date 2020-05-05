package com.hraa.worldweather.current_weather_model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.hraa.worldweather.constants.CURRENT_WEATHER_TABLE

@Entity(tableName = CURRENT_WEATHER_TABLE)
data class Data(
    @SerializedName("app_temp")
    val appTemp: Double?,
    @SerializedName("city_name")
    @PrimaryKey val cityName: String,
    val datetime: String?,
    val pres: Double?,
    val sunrise: String?,
    val sunset: String?,
    val temp: Double?,
    val uv: Double?,
    val vis: Double?,
    val weather: Weather?,
    @SerializedName("wind_cdir")
    val windCdir: String?,
    @SerializedName("wind_spd")
    val windSpd: Double?,
    val lat: String?,
    val lon: String?,
//    var isCurrentLocation: Boolean? = false,
    var isCurrentLocation: Boolean,
    var units: String?
)

class WeatherConverter {
    @TypeConverter
    fun weatherToString(weather: Weather): String? {
        return Gson().toJson(weather)
    }

    @TypeConverter
    fun stringToWeather(string: String): Weather? {
        return Gson().fromJson(string, Weather::class.java)
    }
}