package com.hraa.worldweather.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.hraa.worldweather.R
import com.hraa.worldweather.constants.LAST_LOCATION_SHARED_PREF
import com.hraa.worldweather.constants.SHARED_PREFERENCES_NAME
import com.hraa.worldweather.repo.Repository
import com.hraa.worldweather.room.WeatherDatabase
import com.hraa.worldweather.ui.MainActivity
import com.hraa.worldweather.ui.MainFragment
import kotlinx.coroutines.*
import java.lang.Exception
import java.net.UnknownHostException

class NotificationService : Service() {

    private val repository: Repository

    init {
        val weatherDao = WeatherDatabase.getDatabase(this).weatherDao()
        repository = Repository(weatherDao)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("TAG", "onStartCommand called")

        val inten = Intent(this, NotificationService::class.java).apply {
            putExtra(
                "units", intent?.extras?.getString("units")
            )
            putExtra(
                "cityName", intent?.extras?.getString("cityName")
            )
        }
        val pendingInten =
            PendingIntent.getService(this, 0, inten, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 3600 * 1000,
            pendingInten
        )

        intent?.let { i ->

            val notifIntent = Intent(this, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(
                    this,
                    0,
                    notifIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

            val builder = NotificationCompat.Builder(this, "20")
                .setSmallIcon(R.drawable.ic_sun)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)

            val city = i.extras?.getString("cityName")
            val units = i.extras?.getString("units")

            val handler = CoroutineExceptionHandler { _, exception ->
                when (exception.cause) {
                    is Exception -> {
                        stopSelf()
                    }
                }
            }

            val parentJob = CoroutineScope(Dispatchers.IO + handler).launch {

                //Get current weather from api by city name
                val currentWeatherJob = launch {
                    val currentWeather =
                        repository.getCurrentWeatherCityNameFromApi(city!!, units!!)
                    builder.setContentTitle("${currentWeather.data[0].temp.toInt()} in $city")
                }
                //When currentWeatherJob Completed check if there's an exception and throw it if so
                //If the operation success.. save the city name as last location for the user
                currentWeatherJob.invokeOnCompletion {
                    it?.let { throwable ->
                        throw throwable
                    }
                }

                val sixteenForecastJob = launch {
                    //Get sixteen days forecast from api by city name
                    val sixteenWeather =
                        repository.getSixteenDaysForecastCityNameFromApi(city!!, units!!)
                    builder.setContentText("high ${sixteenWeather.data[0].maxTemp.toInt()} | low ${sixteenWeather.data[0].minTemp.toInt()}")
                }

                sixteenForecastJob.invokeOnCompletion {
                    it?.let { throwable ->
                        throw throwable
                    }
                }
            }
            //When parentJob completed.. if no exception consider the operation finished
            parentJob.invokeOnCompletion {
                if (it == null) {
                    val notification = builder.build()
                    startForeground(1, notification)
                }
            }
        }

        return START_NOT_STICKY
    }
}