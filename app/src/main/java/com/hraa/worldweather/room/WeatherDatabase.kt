package com.hraa.worldweather.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hraa.worldweather.current_weather_model.Data
import com.hraa.worldweather.current_weather_model.WeatherConverter
import com.hraa.worldweather.forecast_weather_model.DataConverter
import com.hraa.worldweather.forecast_weather_model.ForecastWeatherModel

@Database(
    entities = [Data::class, ForecastWeatherModel::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(WeatherConverter::class, DataConverter::class)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getDatabase(context: Context): WeatherDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weather_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}