package com.hraa.worldweather.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hraa.worldweather.R
import com.hraa.worldweather.constants.*
import com.hraa.worldweather.enums.WeatherResultState
import com.hraa.worldweather.view_model.WeatherViewModel
import kotlinx.android.synthetic.main.fragment_welcome.*
import kotlinx.coroutines.*

class WelcomeFragment : Fragment(), TextWatcher {

    private val weatherViewModel: WeatherViewModel by activityViewModels()

    private val dialog by lazy { Dialog(this.requireContext()) }

    private val sharedPref by lazy {
        activity?.getSharedPreferences(
            SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
    }

    private lateinit var okBtn: Button
    private lateinit var cityNameEditText: EditText
    private lateinit var closeDialog: ImageView

    private lateinit var units: String
    private lateinit var lastLocation: String

    private var isDialogShowing = false
    private var ifFirstUsedJob: Job = Job()
    private var navigateJob1: Job = Job()
    private var navigateJob2: Job = Job()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Getting last location and units from shared preferences
        // And if they haven't created yet! get the default values
        lastLocation = sharedPref?.getString(LAST_LOCATION_SHARED_PREF, "")!!
        units = sharedPref?.getString(UNITS_SHARED_PREF, "M")!!

        if (savedInstanceState == null) {
            // Check location permission Only if this is  first use of app
            ifFirstUsedJob = CoroutineScope(Dispatchers.IO).launch {
                if (weatherViewModel.isCitiesInRoomNull()) {
                    withContext(Dispatchers.Main) {
                        checkLocationPermission()
                    }
                } else {
                    weatherViewModel.getWeatherByCityName(lastLocation, units)
                }
            }
        } else {
            // Check if dialog was showing, If so.. show it.. else dismiss it
            isDialogShowing = savedInstanceState.getBoolean(IS_DIALOG_SHOWING_KEY)

            if (isDialogShowing) {
                dialog.show()
            } else {
                dialog.dismiss()
            }
        }

        setDialog()
        observeWeatherState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_DIALOG_SHOWING_KEY, isDialogShowing)
    }

    private fun checkLocationPermission() {
        weatherViewModel.permissionRequest.value = LOCATION_PERMISSION

        weatherViewModel.permissionRequest.observe(viewLifecycleOwner, Observer { permission ->

            if (ContextCompat.checkSelfPermission(this.requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                //Permission is not granted.. requesting!
                requestPermissions(arrayOf(permission), LOCATION_REQUEST_CODE)
            } else {
                //Permission has been already granted
                //If the last location from sharedpref is not null or empty
                //Depend on it when getting weather
                //Else.. Depend on user location
                if (lastLocation.isNotEmpty()) {
                    //What will changes with liveData by calling this?
                    //_lastLocation, _weatherResult
                    weatherViewModel.getWeatherByCityName(lastLocation, units)
                } else {
                    //What will changes with liveData by calling this?
                    //_latitude, _longitude, _weatherResult
                    weatherViewModel.getWeatherByLocation(lastLocation)
                }
            }
        })
    }

    private fun setDialog() {
        // Set the dialog view
        dialog.setContentView(R.layout.dialog_refused)
        // set that dialog as not cancelable
        dialog.setCancelable(false)
        // Get views from inflated layout
        okBtn = dialog.findViewById(R.id.okBtn_dialog)
        cityNameEditText = dialog.findViewById(R.id.city_edtxt_dialog)
        closeDialog = dialog.findViewById(R.id.close_dialog)
        // Listen to text changes
        cityNameEditText.addTextChangedListener(this)

        okBtn.setOnClickListener {
            okBtn.isEnabled = false
            val loc = cityNameEditText.text.toString()
            weatherViewModel.getWeatherByCityName(loc, units)
            // In case of internet went off (Save the location)
            sharedPref?.edit()?.putString(LAST_LOCATION_SHARED_PREF, loc)?.apply()
        }

        closeDialog.setOnClickListener {
            dialog.dismiss()
            isDialogShowing = false
        }
    }

    private fun observeWeatherState() {
        weatherViewModel.weatherResult.observe(viewLifecycleOwner, Observer { weatherState ->
            weatherState?.let {
                when (weatherState.name) {
                    WeatherResultState.FINISHED.toString() -> {
                        if (dialog.isShowing) {
                            dialog.dismiss()
                            isDialogShowing = false
                        }
                        findNavController().navigate(R.id.action_welcomeFragment_to_mainFragment)
                    }

                    WeatherResultState.NO_INTERNET.toString() -> {
                        okBtn.isEnabled = true
                        navigateJob1 = CoroutineScope(Dispatchers.IO).launch {
                            if (!weatherViewModel.isCitiesInRoomNull()) {
                                withContext(Dispatchers.Main) {
                                    findNavController().navigate(R.id.action_welcomeFragment_to_mainFragment)
                                }
                            }
                        }
                    }

                    WeatherResultState.WRONG_CITY_NAME.toString() -> {
                        cityNameEditText.error = resources.getString(R.string.error_city_name)
                    }

                    WeatherResultState.EXCEPTION.toString() -> {
                        navigateJob2 = CoroutineScope(Dispatchers.IO).launch {
                            if (!weatherViewModel.isCitiesInRoomNull()) {
                                withContext(Dispatchers.Main) {
                                    findNavController().navigate(R.id.action_welcomeFragment_to_mainFragment)
                                }
                            }
                        }
                    }

                    WeatherResultState.LOCATION_IS_OFF.toString() -> {
                        enableLocationSnackBar(
                            resources.getString(R.string.enable_gps),
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        )
                    }
                }
            }
        })
        weatherViewModel.onFinishWeatherResult()
    }

    private fun enableLocationSnackBar(message: String, intent: Intent) {
        Snackbar.make(welcome_parent, message, Snackbar.LENGTH_INDEFINITE)
            .setAction(resources.getString(R.string.settings)) {
                startActivity(intent)
                requireActivity().finish()
            }.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Permission is accepted
                    weatherViewModel.getWeatherByLocation(units)
                } else {
                    // Permission denied
                    dialog.show()
                }
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        s?.let {
            okBtn.isEnabled = it.isNotEmpty()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ifFirstUsedJob.cancel()
        navigateJob1.cancel()
        navigateJob2.cancel()
        weatherViewModel.weatherResult.removeObservers(viewLifecycleOwner)
    }
}