package com.hraa.worldweather.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hraa.worldweather.R
import com.hraa.worldweather.adapter.LocationsAdapter
import com.hraa.worldweather.constants.*
import com.hraa.worldweather.enums.WeatherResultState
import com.hraa.worldweather.location_models.HeaderLocation
import com.hraa.worldweather.location_models.LocationName
import com.hraa.worldweather.view_model.WeatherViewModel
import kotlinx.android.synthetic.main.fragment_location.*

class LocationFragment : Fragment(), LocationsAdapter.OnLocationClick {

    private val locationsAdapter by lazy { LocationsAdapter(this) }
    private var dataArray = ArrayList<Any>()

    private val locationsViewModel: WeatherViewModel by activityViewModels()

    private val sharedPref by lazy {
        activity?.getSharedPreferences(
            SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        location_toolbar.setupWithNavController(navController, appBarConfiguration)
        location_toolbar.title = resources.getString(R.string.locations_title)

        Log.e("TAG", "onCreateCalled -> Getting all locations")
        locationsViewModel.getAllLocations()

        locations_recycler.apply {
            layoutManager = LinearLayoutManager(this@LocationFragment.requireContext())
            adapter = locationsAdapter
        }

        observeLocations()

        observeWeatherResult()
    }

    private fun observeLocations() {
        locationsViewModel.locations.observe(viewLifecycleOwner, Observer { location ->
            Log.e("LocationFragment", "location value is $location")
            if (location != null) {
                dataArray.add(HeaderLocation("Current location"))

                location.forEach { locationState ->
                    if (locationState.isCurrentLocation != null && locationState.isCurrentLocation!!) {
                        dataArray.add(LocationName(locationState.cityName))
                    }
                }

                for (locationState in location) {
                    if (locationState.isCurrentLocation == false) {
                        dataArray.add(HeaderLocation("Other locations"))
                        break
                    }
                }

                location.forEach { locationState ->
                    if (locationState.isCurrentLocation == false) {
                        dataArray.add(LocationName(locationState.cityName))
                    }
                }

                locationsAdapter.setData(dataArray)
                locationsViewModel.onFinishObserveLocations()
            }
        })
    }

    override fun onClick(position: Int?, cityName: String?) {
        if (position != null) {
            val units = sharedPref?.getString(UNITS_SHARED_PREF, "M")!!
            if (cityName != null) {
                Log.e("LocationFragment", "City name clicked -> Fetching data!")
                progress_location.visibility = View.VISIBLE
                locationsViewModel.getWeatherByCityName(cityName, units)
            } else {
                progress_location.visibility = View.VISIBLE
                if (ContextCompat.checkSelfPermission(this.requireContext(), LOCATION_PERMISSION)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    locationsViewModel.getWeatherByLocation(units)
                } else {
                    progress_location.visibility = View.GONE
                    enableLocationSnackBar(
                        resources.getString(R.string.enable_location_permission),
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", requireActivity().packageName, null)
                        )
                    )
                }
            }
        }
    }

    private fun observeWeatherResult() {
        locationsViewModel.weatherResult.observe(viewLifecycleOwner, Observer {
            Log.e("LocationFragment", "weather result is -> $it")
            it?.let {
                when (it.name) {
                    WeatherResultState.FINISHED.toString() -> {
                        progress_location.visibility = View.GONE
                        findNavController().navigate(R.id.action_locationFragment_to_mainFragment)
                    }
                    WeatherResultState.EXCEPTION.toString() -> {
                        progress_location.visibility = View.GONE
                        this.requireContext().toast(resources.getString(R.string.error_general))
                    }
                    WeatherResultState.NO_INTERNET.toString() -> {
                        this.requireContext().toast(resources.getString(R.string.error_no_internet))
                    }
                    WeatherResultState.LOCATION_IS_OFF.toString() -> {
                        progress_location.visibility = View.GONE
                        enableLocationSnackBar(
                            resources.getString(R.string.enable_gps),
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        )
                    }
                }
            }
        })
        locationsViewModel.onFinishWeatherResult()
    }

    private fun enableLocationSnackBar(message: String, intent: Intent) {
        Snackbar.make(location_parent, message, Snackbar.LENGTH_INDEFINITE)
            .setAction(resources.getString(R.string.settings)) {
                startActivity(intent)
            }.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("TAG", "onDestroyView called")
        locations_recycler.adapter = null
    }
}