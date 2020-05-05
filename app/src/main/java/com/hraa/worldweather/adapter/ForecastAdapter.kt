package com.hraa.worldweather.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hraa.worldweather.R
import com.hraa.worldweather.forecast_weather_model.Data
import com.hraa.worldweather.forecast_weather_model.ForecastWeatherModel

class ForecastAdapter(private val listener: OnDayItemClick) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    private var dataList: List<Data> = emptyList()

    fun setData(data: ForecastWeatherModel) {
        dataList = data.data
        notifyDataSetChanged()
    }

    override fun getItemCount() = dataList.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return ForecastDaysHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.day_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val dataPositioned = dataList[position]
        when (holder) {
            is ForecastDaysHolder -> holder.bind(dataPositioned, position, listener)
        }

    }

    interface OnDayItemClick {
        fun onClick(position: Int)
    }
}