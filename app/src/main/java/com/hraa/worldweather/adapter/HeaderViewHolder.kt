package com.hraa.worldweather.adapter

import android.view.View
import android.widget.TextView
import com.hraa.worldweather.R
import com.hraa.worldweather.location_models.HeaderLocation

class HeaderViewHolder(itemView: View) : BaseViewHolder<HeaderLocation>(itemView) {

    override fun bind(item: HeaderLocation, position: Int?, listener: Any?) {
        val locationTxt = itemView.findViewById<TextView>(R.id.location_txt)
        val detectLoc = itemView.findViewById<TextView>(R.id.detect_my_location_txt)
        locationTxt.text = item.header
        if (item.header == "Current location") {
            detectLoc.visibility = View.VISIBLE
        } else {
            detectLoc.visibility = View.GONE
        }
        detectLoc.setOnClickListener {
            (listener as LocationsAdapter.OnLocationClick).onClick(position!!, null, false)
        }
    }
}