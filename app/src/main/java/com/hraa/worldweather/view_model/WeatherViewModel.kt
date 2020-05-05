package com.hraa.worldweather.view_model

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.hraa.worldweather.current_weather_model.Data
import com.hraa.worldweather.enums.WeatherResultState
import com.hraa.worldweather.forecast_weather_model.ForecastWeatherModel
import com.hraa.worldweather.repo.Repository
import com.hraa.worldweather.room.WeatherDatabase
import kotlinx.coroutines.*
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: Repository

    init {
        val weatherDao = WeatherDatabase.getDatabase(application).weatherDao()
        repository = Repository(weatherDao)
    }

    val permissionRequest = MutableLiveData<String>()

    private val locationProvider by lazy {
        LocationServices.getFusedLocationProviderClient(application)
    }

    private val _weatherForecast = MutableLiveData<ForecastWeatherModel>()

    val weatherForecast: LiveData<ForecastWeatherModel>
        get() = _weatherForecast

    private val _currentWeather = MutableLiveData<List<Data>>()

    val currentWeather: LiveData<List<Data>>
        get() = _currentWeather

    private val _weatherResult = MutableLiveData<WeatherResultState>()
    val weatherResult: LiveData<WeatherResultState>
        get() = _weatherResult

    private val _locations = MutableLiveData<List<Data>>()
    val locations: LiveData<List<Data>>
        get() = _locations

    val lastLocation = MutableLiveData<String>()

    fun onFinishObserveLocations() {
        _locations.value = null
    }

    fun onFinishWeatherResult() {
        _weatherResult.value = null
    }

    fun getAllLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            _locations.postValue(repository.getAllLocationsFromRoom())
        }
    }

    fun getWeatherByLocation(units: String) {
        locationProvider.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Save latitude and longitude again in val so we can deal with them
                val latitude = location.latitude.toLong()
                val longitude = location.longitude.toLong()
                // Handler for parentJob
                val handler = CoroutineExceptionHandler { _, exception ->
                    when (exception.cause) {
                        is UnknownHostException -> {
                            _weatherResult.postValue(WeatherResultState.NO_INTERNET)
                        }
                        is CancellationException -> {
                        }
                        else -> {
                            if (exception.cause != null) {
                                _weatherResult.postValue(WeatherResultState.EXCEPTION)
                            }
                        }
                    }
                }

                val parentJob = viewModelScope.launch(Dispatchers.IO + handler) {

                    val currentWeatherJob = launch {
                        getCurrentWeatherByLocation(latitude, longitude, units)
                    }

                    currentWeatherJob.invokeOnCompletion {
                        it?.let { throwable ->
                            throw throwable
                        }
                    }

                    val forecastWeatherJob = launch {
                        getWeatherForecastByLocation(latitude, longitude, units)
                    }

                    forecastWeatherJob.invokeOnCompletion {
                        it?.let { throwable ->
                            throw throwable
                        }
                    }
                }

                parentJob.invokeOnCompletion {
                    if (it == null) {
                        Log.e("TAG", "ParentJob finished successfully")
                        _weatherResult.postValue(WeatherResultState.FINISHED)
                    }
                }

            } else {
                _weatherResult.postValue(WeatherResultState.LOCATION_IS_OFF)
            }
        }
    }

    suspend fun isCitiesInRoomNull(): Boolean {
        return withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            repository.getAllLocationsFromRoom().isNullOrEmpty()
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getDayFromDate(date: String): Int {
        val calender = Calendar.getInstance()
        calender.time = SimpleDateFormat("yyyy-MM-dd").parse(date)!!
        return calender.get(Calendar.DAY_OF_WEEK)
    }

    fun whatDay(dayInt: Int): String {
        return when (dayInt) {
            1 -> "Sun"
            2 -> "Mon"
            3 -> "Tue"
            4 -> "Wed"
            5 -> "Thu"
            6 -> "Fri"
            7 -> "Sat"
            else -> "Unknown day"
        }
    }

    fun getWeatherByCityName(city: String, units: String) {

        Log.e("TAG", "getWeatherByCityName called")

        // Assume this calling as last location
        lastLocation.postValue(city)

        val handler = CoroutineExceptionHandler { _, exception ->
            when (exception.cause) {
                is UnknownHostException -> {
                    getInfoFromRoom(city)
                    _weatherResult.postValue(WeatherResultState.NO_INTERNET)
                }
                is NullPointerException -> {
                    Log.e("TAG", "ERROR NullPointerException -> ${exception.cause}")
                    _weatherResult.postValue(WeatherResultState.WRONG_CITY_NAME)
                }
                is CancellationException -> {
                    Log.e("TAG", "ERROR CancellationException -> ${exception.cause}")
                }
                else -> {
                    Log.e("TAG", "ERROR exception -> ${exception.cause}")
                    if (exception.cause != null) {
                        Log.e("TAG", "General exception is NOT null")
                        getInfoFromRoom(city)
                        _weatherResult.postValue(WeatherResultState.EXCEPTION)
                    }
                }
            }
        }

        val parentJob = viewModelScope.launch(Dispatchers.IO + handler) {

            Log.e("TAG", "parentJob Accessed")

            // Get current weather from api by city name
            val currentWeatherJob = launch {
                Log.e("TAG", "currentWeatherJob Accessed")
                val currentWeather = repository.getCurrentWeatherByCityNameFromApi(city, units)

                // Insert units manually (Because it's not returned from the api)
                // And check the given city if it is a current location or not! (in database)
                currentWeather.data[0].units = units
                currentWeather.data[0].isCurrentLocation =
                    repository.isCurrentLocation(currentWeather.data[0].cityName) ?: false

                // Insert data to database
                repository.insertCurrentWeatherToRoom(currentWeather.data)

                // Getting data from database
                _currentWeather.postValue(
                    repository.getCurrentWeatherByCityNameFromRoom(
                        currentWeather.data[0].cityName
                    )
                )
            }
            // When currentWeatherJob Completed check if there's an exception and throw it if so
            // If the operation success.. save the city name as last location for the user
            currentWeatherJob.invokeOnCompletion {
                it?.let { throwable ->
                    throw throwable
                }
            }

            val forecastWeatherJob = launch {

                Log.e("TAG", "forecastWeatherJob Accessed")

                // Get sixteen days forecast from api by city name
                val forecastWeather = repository.getWeatherForecastByCityNameFromApi(city, units)

                // Insert data to database
                repository.insertForecastWeatherToRoom(forecastWeather)

                // Getting data from database
                _weatherForecast.postValue(
                    repository.getWeatherForecastByCityNameFromRoom(
                        forecastWeather.cityName
                    )
                )
            }

            forecastWeatherJob.invokeOnCompletion {
                it?.let { throwable ->
                    throw throwable
                }
            }
        }
        // When parentJob completed.. if no exception consider the operation finished
        parentJob.invokeOnCompletion {
            if (it == null) {
                Log.e("TAG", "Parent job Finished!")
                _weatherResult.postValue(WeatherResultState.FINISHED)
            } else {
                Log.e("TAG", "Parent job FAILED!")
            }
        }
    }

    private suspend fun getCurrentWeatherByLocation(
        latitude: Long,
        longitude: Long,
        units: String
    ) {
        // Get current weather from api by location
        val currentWeather =
            repository.getCurrentWeatherByLocationFromApi(latitude, longitude, units)

        // Assume this calling as last location
        lastLocation.postValue(currentWeather.data[0].cityName)

        // Insert units manually (Because it's not returned from the api)
        currentWeather.data[0].units = units
        currentWeather.data[0].isCurrentLocation = true

        // Insert data to database
        repository.insertCurrentWeatherToRoom(currentWeather.data)

        // Getting data from database
        _currentWeather.postValue(
            repository.getCurrentWeatherByLocationFromRoom(
                latitude.toString(),
                longitude.toString()
            )
        )
    }

    private suspend fun getWeatherForecastByLocation(
        latitude: Long,
        longitude: Long,
        units: String
    ) {
        // Get sixteen days forecast from api by coordinates
        val sixteenWeather =
            repository.getWeatherForecastByLocationFromApi(latitude, longitude, units)

        // Save the latitude and longitude in the object so you can look for it in the database
        sixteenWeather.lat = latitude.toString()
        sixteenWeather.lon = longitude.toString()

        // Insert data to database
        repository.insertForecastWeatherToRoom(sixteenWeather)

        // Getting data from database
        _weatherForecast.postValue(
            repository.getWeatherForecastByLocationFromRoom(
                latitude.toString(),
                longitude.toString()
            )
        )
    }

    fun deleteWeatherLocation(cityName: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteCurrentWeatherLocation(cityName)
        repository.deleteForecastLocation(cityName)
    }

    // Getting weather from local database in case we couldn't fetch data
    // from the network and we have a previous data there...
    private fun getInfoFromRoom(city: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!isCitiesInRoomNull()) {
                _currentWeather.postValue(
                    repository.getCurrentWeatherByCityNameFromRoom(city)
                )
                _weatherForecast.postValue(
                    repository.getWeatherForecastByCityNameFromRoom(city)
                )
            }
        }
    }
}