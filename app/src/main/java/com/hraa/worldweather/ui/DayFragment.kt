package com.hraa.worldweather.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.hraa.worldweather.R
import com.hraa.worldweather.view_model.WeatherViewModel
import kotlinx.android.synthetic.main.fragment_day.*
import java.text.SimpleDateFormat
import java.util.*

class DayFragment : Fragment() {

    private val weatherViewModel: WeatherViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_day, container, false)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val position: Int? = arguments?.getInt("position")
        val chosenDay = weatherViewModel.weatherForecast.value?.data!![position!!]

        // Setting up the toolbar
        val toolbar = view.findViewById(R.id.day_toolbar) as Toolbar
        toolbar.title = weatherViewModel
            .whatDay(weatherViewModel.getDayFromDate(chosenDay.datetime!!))
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        max_temp_day_txt.text = resources.getString(R.string.temp, chosenDay.maxTemp?.toInt())

        min_temp_day_txt.text = resources.getString(R.string.temp, chosenDay.minTemp?.toInt())

        val sunriseDate = Date((chosenDay.sunriseTs?.toLong() ?: 0) * 1000)
        val formattedDate = SimpleDateFormat("HH:mm")
        sunrise_day_txt.text = formattedDate.format(sunriseDate)

        val sunsetDate = Date((chosenDay.sunsetTs?.toLong() ?: 0) * 1000)
        sunset_day_txt.text = formattedDate.format(sunsetDate)

        uv_index_day_txt.text = chosenDay.uv?.toInt().toString()
        wind_spd_day_txt.text = resources.getString(R.string.wind_speed, chosenDay.windSpd?.toInt())
        wind_dir_txt.text = chosenDay.windCdir

        precip_txt.text = resources.getString(R.string.precip, chosenDay.precip)
        ozone_txt.text = chosenDay.ozone?.toInt().toString()
        description_details_txt.text = chosenDay.weather?.description
    }
}