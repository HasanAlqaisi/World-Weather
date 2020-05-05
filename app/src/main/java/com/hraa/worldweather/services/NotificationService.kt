package com.hraa.worldweather.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hraa.worldweather.R
import com.hraa.worldweather.constants.LAST_LOCATION_SHARED_PREF
import com.hraa.worldweather.constants.SHARED_PREFERENCES_NAME
import com.hraa.worldweather.constants.UNITS_SHARED_PREF
import com.hraa.worldweather.repo.Repository
import com.hraa.worldweather.room.WeatherDatabase
import com.hraa.worldweather.ui.MainActivity
import kotlinx.coroutines.*
import java.lang.Exception

class NotificationService : JobIntentService() {

    private var currentWeatherJob: Job = Job()
    private var forecastWeatherJob: Job = Job()
    private var parentJob: Job = Job()

    override fun onHandleWork(intent: Intent) {
        if (isStopped) {
            currentWeatherJob.cancel()
            forecastWeatherJob.cancel()
            parentJob.cancel()
            return
        }

        val repository: Repository
        val weatherDao = WeatherDatabase.getDatabase(this).weatherDao()
        repository = Repository(weatherDao)

        val inten = Intent(applicationContext, NotificationReceiver::class.java).apply {
            action = "com.hraa.worldweather.alarm.alerted"
        }

        val pendingIntent =
            PendingIntent.getBroadcast(
                applicationContext,
                0,
                inten,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + (3600000),
            pendingIntent
        )

        val notifIntent = Intent(this, MainActivity::class.java)
        val pendinginten =
            PendingIntent.getActivity(
                this,
                0,
                notifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        val builder = NotificationCompat.Builder(this, "20")
            .setSmallIcon(R.drawable.weather_icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendinginten)

        val city =
            getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString(
                LAST_LOCATION_SHARED_PREF, null
            )

        val units =
            getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString(
                UNITS_SHARED_PREF,
                "M"
            )

        val handler = CoroutineExceptionHandler { _, exception ->
            when (exception.cause) {
                is Exception -> {
                    stopSelf()
                }
            }
        }

        parentJob = CoroutineScope(Dispatchers.IO + handler).launch {

            //Get current weather from api by city name
            currentWeatherJob = launch {
                val currentWeather =
                    repository.getCurrentWeatherByCityNameFromApi(city!!, units!!)
                builder.setContentTitle("${currentWeather.data[0].temp?.toInt()}° in $city")
            }
            //When currentWeatherJob Completed check if there's an exception and throw it if so
            //If the operation success.. save the city name as last location for the user
            currentWeatherJob.invokeOnCompletion {
                it?.let { throwable ->
                    throw throwable
                }
            }

            forecastWeatherJob = launch {
                // Get sixteen days forecast from api by city name
                val sixteenWeather =
                    repository.getWeatherForecastByCityNameFromApi(city!!, units!!)
                builder.setContentText("high ${sixteenWeather.data[0].maxTemp?.toInt()}° | low ${sixteenWeather.data[0].minTemp?.toInt()}°")
            }

            forecastWeatherJob.invokeOnCompletion {
                it?.let { throwable ->
                    throw throwable
                }
            }
        }
        // When parentJob completed.. if no exception, consider the operation finished
        parentJob.invokeOnCompletion {
            if (it == null) {
                val notification = builder.build()
                NotificationManagerCompat.from(this).notify(1, notification)
            }
        }
    }

    fun enqueueWork(context: Context, intent: Intent) {
        enqueueWork(context, NotificationService::class.java, 4, intent)
    }
}