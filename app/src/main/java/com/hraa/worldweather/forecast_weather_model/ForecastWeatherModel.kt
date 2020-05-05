package com.hraa.worldweather.forecast_weather_model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.hraa.worldweather.constants.SIXTEEN_WEATHER_TABLE

@Entity(tableName = SIXTEEN_WEATHER_TABLE)
data class ForecastWeatherModel(
    @SerializedName("city_name")
    @PrimaryKey val cityName: String,
    var lat: String,
    var lon: String,
    val `data`: List<Data>
)

class DataConverter {
    @TypeConverter
    fun dataToString(data: List<Data>): String? {
        return Gson().toJson(data)
    }
    @TypeConverter
    fun stringToData(string: String): List<Data?> {
        val type = object : TypeToken<List<Data?>>() {}.type
        return Gson().fromJson(string, type)
    }
}