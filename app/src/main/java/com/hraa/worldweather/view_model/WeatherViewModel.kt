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
import com.hraa.worldweather.repo.Repository
import com.hraa.worldweather.room.WeatherDatabase
import com.hraa.worldweather.sixteen_weather_model.SixteenWeatherModel
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

    private val _sixteenDaysForecast = MutableLiveData<SixteenWeatherModel>()

    val sixteenDaysForecast: LiveData<SixteenWeatherModel>
        get() = _sixteenDaysForecast

    private val _currentWeather = MutableLiveData<List<Data>>()

    val currentWeather: LiveData<List<Data>>
        get() = _currentWeather

    private val _weatherResult = MutableLiveData<WeatherResultState>()
    val weatherResult: LiveData<WeatherResultState>
        get() = _weatherResult

    private val _locations = MutableLiveData<List<Data>?>()
    val locations: LiveData<List<Data>?>
        get() = _locations

    private val _latitude = MutableLiveData<Long>()
    private val _longitude = MutableLiveData<Long>()
    val lastLocation = MutableLiveData<String>()

    fun onFinishWeatherResult() {
        _weatherResult.value = null
    }

    fun getAllLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            _locations.postValue(repository.getAllLocations())
        }
    }

    fun getWeatherByLocation(units: String) {
        locationProvider.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                //Save lat and lon in liveData
                _latitude.value = location.latitude.toLong()
                _longitude.value = location.longitude.toLong()
                //Save it again in vals so we can deal with them
                val latitude = location.latitude.toLong()
                val longitude = location.longitude.toLong()
                //Handler for parentJob
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
                        getCurrentWeatherByCoordinates(latitude, longitude, units)
                    }

                    currentWeatherJob.invokeOnCompletion {
                        it?.let { throwable ->
                            throw throwable
                        }
                    }

                    val sixteenWeatherJob = launch {
                        getSixteenWeatherByCoordinates(latitude, longitude, units)
                    }

                    sixteenWeatherJob.invokeOnCompletion {
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

    fun getAllCitiesAvailableFromRoomAsync(): Deferred<String?> {
        return viewModelScope.async(Dispatchers.IO) {
            repository.getAllCitiesAvailable()
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
        val handler = CoroutineExceptionHandler { _, exception ->
            when (exception.cause) {
                is UnknownHostException -> {
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
                        _weatherResult.postValue(WeatherResultState.EXCEPTION)
                    }
                }
            }
        }

        val parentJob = viewModelScope.launch(Dispatchers.IO + handler) {

            //Get current weather from api by city name
            val currentWeatherJob = launch {
                val currentWeather = repository.getCurrentWeatherCityNameFromApi(city, units)

                //Insert units manually (Because it's not returned from the api)
                //And check the given city if it is a current location or not! (in database)
                currentWeather.data[0].units = units
                val isCurrent = getIsCurrentLocation(city)
                currentWeather.data[0].isCurrentLocation = isCurrent != null && isCurrent

                //Insert data to database
                repository.insertCurrentWeather(currentWeather.data)

                //Getting data from database
                _currentWeather.postValue(
                    repository.getCurrentWeatherByCityNameFromRoom(
                        currentWeather.data[0].cityName
                    )
                )
            }
            //When currentWeatherJob Completed check if there's an exception and throw it if so
            //If the operation success.. save the city name as last location for the user
            currentWeatherJob.invokeOnCompletion {
                it?.let { throwable ->
                    throw throwable
                }
                if (it == null) {
                    //Assume this calling as last location
                    lastLocation.postValue(city)
                }
            }

            val sixteenForecastJob = launch {
                //Get sixteen days forecast from api by city name
                val sixteenWeather = repository.getSixteenDaysForecastCityNameFromApi(city, units)

                //Insert data to database
                repository.insertSixteenWeather(sixteenWeather)

                //Getting data from database
                _sixteenDaysForecast.postValue(
                    repository.getSixteenWeatherByCityNameFromRoom(
                        sixteenWeather.cityName
                    )
                )
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
                _weatherResult.postValue(WeatherResultState.FINISHED)
            }
        }
    }

    private suspend fun getCurrentWeatherByCoordinates(
        latitude: Long,
        longitude: Long,
        units: String
    ) { //Get current weather from api by Coordinates
        val currentWeather =
            repository.getCurrentWeatherLatLonFromApi(latitude, longitude, units)

        //Assume this calling as last location
        lastLocation.postValue(currentWeather.data[0].cityName)

        //Insert units manually (Because it's not returned from the api)
        currentWeather.data[0].units = units
        currentWeather.data[0].isCurrentLocation = true

        //Insert data to database
        repository.insertCurrentWeather(currentWeather.data)

        //Getting data from database
        _currentWeather.postValue(
            repository.getCurrentWeatherByLatAndLonFromRoom(
                latitude.toString(),
                longitude.toString()
            )
        )
    }

    private suspend fun getSixteenWeatherByCoordinates(
        latitude: Long,
        longitude: Long,
        units: String
    ) {

        //Get sixteen days forecast from api by coordinates
        val sixteenWeather =
            repository.getSixteenDaysForecastLatLonFromApi(latitude, longitude, units)

        //Save the latitude and longitude in the object so you can look for it in the database
        sixteenWeather.lat = latitude.toString()
        sixteenWeather.lon = longitude.toString()

        //Insert data to database
        repository.insertSixteenWeather(sixteenWeather)

        //Getting data from database
        _sixteenDaysForecast.postValue(
            repository.getSixteenWeatherByLatAndLonFromRoom(
                latitude.toString(),
                longitude.toString()
            )
        )
    }

    private suspend fun getIsCurrentLocation(city: String): Boolean? {
        return repository.getIsCurrentLocation(city)
    }

}
