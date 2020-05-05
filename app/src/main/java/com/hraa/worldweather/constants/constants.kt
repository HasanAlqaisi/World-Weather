package com.hraa.worldweather.constants

import android.Manifest
import android.content.Context
import android.widget.Toast

const val API_KEY = "7d83901c0acb497382e26306543dc8a5"

const val BASE_URL_FORECAST = "https://api.weatherbit.io/v2.0/forecast/"
const val BASE_URL_CURRENT = "https://api.weatherbit.io/v2.0/"

const val LOCATION_REQUEST_CODE = 10
const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION

const val CURRENT_WEATHER_TABLE = "current_weather_table"
const val SIXTEEN_WEATHER_TABLE = "sixteen_weather_table"

const val SHARED_PREFERENCES_NAME = "weather_preferences"
const val UNITS_SHARED_PREF = "units"
const val LAST_LOCATION_SHARED_PREF = "lastLocation"

const val IS_DIALOG_SHOWING_KEY = "isDialogShowing"
const val IS_SWITCH_CHECKED = "isSwitchChecked"

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

//fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
//    observe(lifecycleOwner, object : Observer<T> {
//        override fun onChanged(t: T?) {
//            observer.onChanged(t)
//            removeObserver(this)
//        }
//    })
//}