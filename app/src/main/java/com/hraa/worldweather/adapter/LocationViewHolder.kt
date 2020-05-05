package com.hraa.worldweather.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.hraa.worldweather.R
import com.hraa.worldweather.location_models.LocationName

class LocationViewHolder(itemView: View) : BaseViewHolder<LocationName>(itemView) {

    override fun bind(item: LocationName, position: Int?, listener: Any?) {
        val cityName = itemView.findViewById<TextView>(R.id.city_name_txt)
        val deleteLocation = itemView.findViewById<ImageView>(R.id.delete_location)
        cityName.text = item.cityName
        itemView.setOnClickListener {
            (listener as LocationsAdapter.OnLocationClick).onClick(position!!, item.cityName, false)
        }
        deleteLocation.setOnClickListener {
            (listener as LocationsAdapter.OnLocationClick).onClick(position!!, item.cityName, true)
        }
    }
}