package com.hraa.worldweather.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hraa.worldweather.R
import com.hraa.worldweather.location_models.HeaderLocation
import com.hraa.worldweather.location_models.LocationName
import java.lang.IllegalStateException

class LocationsAdapter(private val listener: OnLocationClick) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    private var dataList = ArrayList<Any>()

    companion object {
        const val HEADER_TYPE = 0
        const val LOCATION_TYPE = 1
    }

    override fun getItemCount() = dataList.size

    override fun getItemViewType(position: Int): Int {
        return when (dataList[position]) {
            is HeaderLocation -> {
                HEADER_TYPE
            }
            is LocationName -> {
                LOCATION_TYPE
            }
            else -> throw IllegalStateException("Unknown view type!")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val view = LayoutInflater.from(parent.context)
        return when (viewType) {
            HEADER_TYPE -> HeaderViewHolder(view.inflate(R.layout.header_item, parent, false))
            LOCATION_TYPE -> LocationViewHolder(view.inflate(R.layout.location_item, parent, false))
            else -> throw IllegalStateException("Unknown viewType")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val data = dataList[position]
        when (holder) {
            is HeaderViewHolder -> {
                if ((data as HeaderLocation).header == "Current location") {
                    holder.bind(data, position, listener)
                } else {
                    holder.bind(data)
                }
            }
            is LocationViewHolder -> {
                holder.bind(data as LocationName, position, listener)
            }
            else -> throw IllegalStateException("Unknown viewHolder")
        }
    }

    fun setData(data: ArrayList<Any>) {
        dataList = data
        notifyDataSetChanged()
    }

    interface OnLocationClick {
        fun onClick(position: Int?, cityName: String?, shouldDelete: Boolean)
    }

}