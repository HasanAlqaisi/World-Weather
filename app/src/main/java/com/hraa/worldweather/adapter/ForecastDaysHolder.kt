package com.hraa.worldweather.adapter

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.hraa.worldweather.R
import com.hraa.worldweather.forecast_weather_model.Data
import java.text.SimpleDateFormat
import java.util.*

class ForecastDaysHolder(itemView: View) : BaseViewHolder<Data>(itemView) {
    override fun bind(item: Data, position: Int?, listener: Any?) {
        val day: TextView = itemView.findViewById(R.id.day_week)
        val maxTemp: TextView = itemView.findViewById(R.id.max_temp)
        val minTemp: TextView = itemView.findViewById(R.id.min_temp)
        val dateWeek: TextView = itemView.findViewById(R.id.date_week)
        val descriptionWeek: TextView = itemView.findViewById(R.id.description_week)
        val imageWeek: ImageView = itemView.findViewById(R.id.image_week)
        val divider: View = itemView.findViewById(R.id.divider)

        if (position == 0) {
            divider.visibility = View.GONE
        } else {
            divider.visibility = View.VISIBLE
        }

        day.text = whatDay(getDayFromDate(item.datetime))
        maxTemp.text = itemView.resources.getString(R.string.temp, item.maxTemp?.toInt())
        minTemp.text = itemView.resources.getString(R.string.temp, item.minTemp?.toInt())
        descriptionWeek.text = item.weather?.description
        dateWeek.text = getFormattedDate(item.datetime)

        setWeatherImage(item, imageWeek)

        itemView.setOnClickListener {
            if (position != null) {
                (listener as ForecastAdapter.OnDayItemClick).onClick(position)
            }
        }

    }

    private fun setWeatherImage(item: Data, imageWeek: ImageView) {
        when (item.weather?.icon) {
            "a01d" -> imageWeek.setImageResource(R.drawable.a01d)
            "a01n" -> imageWeek.setImageResource(R.drawable.a01n)
            "a02d" -> imageWeek.setImageResource(R.drawable.a02d)
            "a02n" -> imageWeek.setImageResource(R.drawable.a02n)
            "a03d" -> imageWeek.setImageResource(R.drawable.a03d)
            "a03n" -> imageWeek.setImageResource(R.drawable.a03n)
            "a04d" -> imageWeek.setImageResource(R.drawable.a04d)
            "a04n" -> imageWeek.setImageResource(R.drawable.a04n)
            "a05d" -> imageWeek.setImageResource(R.drawable.a05d)
            "a05n" -> imageWeek.setImageResource(R.drawable.a05n)
            "a06d" -> imageWeek.setImageResource(R.drawable.a06d)
            "a06n" -> imageWeek.setImageResource(R.drawable.a06n)
            "c01d" -> imageWeek.setImageResource(R.drawable.c01d)
            "c01n" -> imageWeek.setImageResource(R.drawable.c01n)
            "c02d" -> imageWeek.setImageResource(R.drawable.c02d)
            "c02n" -> imageWeek.setImageResource(R.drawable.c02n)
            "c03d" -> imageWeek.setImageResource(R.drawable.c03d)
            "c03n" -> imageWeek.setImageResource(R.drawable.c03n)
            "c04d" -> imageWeek.setImageResource(R.drawable.c04d)
            "c04n" -> imageWeek.setImageResource(R.drawable.c04n)
            "d01d" -> imageWeek.setImageResource(R.drawable.d01d)
            "d01n" -> imageWeek.setImageResource(R.drawable.d01n)
            "d02d" -> imageWeek.setImageResource(R.drawable.d02d)
            "d02n" -> imageWeek.setImageResource(R.drawable.d02n)
            "d03d" -> imageWeek.setImageResource(R.drawable.d03d)
            "d03n" -> imageWeek.setImageResource(R.drawable.d03n)
            "f01d" -> imageWeek.setImageResource(R.drawable.f01d)
            "f01n" -> imageWeek.setImageResource(R.drawable.f01n)
            "r01d" -> imageWeek.setImageResource(R.drawable.r01d)
            "r01n" -> imageWeek.setImageResource(R.drawable.r01n)
            "r02d" -> imageWeek.setImageResource(R.drawable.r02d)
            "r02n" -> imageWeek.setImageResource(R.drawable.r02n)
            "r03d" -> imageWeek.setImageResource(R.drawable.r03d)
            "r03n" -> imageWeek.setImageResource(R.drawable.r03n)
            "r04d" -> imageWeek.setImageResource(R.drawable.r04d)
            "r04n" -> imageWeek.setImageResource(R.drawable.r04n)
            "r05d" -> imageWeek.setImageResource(R.drawable.r05d)
            "r05n" -> imageWeek.setImageResource(R.drawable.r05n)
            "r06d" -> imageWeek.setImageResource(R.drawable.r06d)
            "r06n" -> imageWeek.setImageResource(R.drawable.r06n)
            "s01d" -> imageWeek.setImageResource(R.drawable.s01d)
            "s01n" -> imageWeek.setImageResource(R.drawable.s01n)
            "s02d" -> imageWeek.setImageResource(R.drawable.s02d)
            "s02n" -> imageWeek.setImageResource(R.drawable.s02n)
            "s03d" -> imageWeek.setImageResource(R.drawable.s03d)
            "s03n" -> imageWeek.setImageResource(R.drawable.s03n)
            "s04d" -> imageWeek.setImageResource(R.drawable.s04d)
            "s04n" -> imageWeek.setImageResource(R.drawable.s04n)
            "s05d" -> imageWeek.setImageResource(R.drawable.s05d)
            "s05n" -> imageWeek.setImageResource(R.drawable.s05n)
            "s06d" -> imageWeek.setImageResource(R.drawable.s06d)
            "s06n" -> imageWeek.setImageResource(R.drawable.s06n)
            "t01d" -> imageWeek.setImageResource(R.drawable.t01d)
            "t01n" -> imageWeek.setImageResource(R.drawable.t01n)
            "t02d" -> imageWeek.setImageResource(R.drawable.t02d)
            "t02n" -> imageWeek.setImageResource(R.drawable.t02n)
            "t03d" -> imageWeek.setImageResource(R.drawable.t03d)
            "t03n" -> imageWeek.setImageResource(R.drawable.t03n)
            "t04d" -> imageWeek.setImageResource(R.drawable.t04d)
            "t04n" -> imageWeek.setImageResource(R.drawable.t04n)
            "t05d" -> imageWeek.setImageResource(R.drawable.t05d)
            "t05n" -> imageWeek.setImageResource(R.drawable.t05n)
            "u00d" -> imageWeek.setImageResource(R.drawable.u00d)
            "u00n" -> imageWeek.setImageResource(R.drawable.u00n)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getFormattedDate(date: String?): String {
        val formattedDate = SimpleDateFormat("yyyy-MM-dd").parse(date!!)
        return SimpleDateFormat("d/MM").format(formattedDate ?: "")
    }

    @SuppressLint("SimpleDateFormat")
    fun getDayFromDate(date: String?): Int {
        val calender = Calendar.getInstance()
        calender.time = SimpleDateFormat("yyyy-MM-dd").parse(date!!)!!
        return calender.get(Calendar.DAY_OF_WEEK)
    }

    private fun whatDay(dayInt: Int): String {
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
}