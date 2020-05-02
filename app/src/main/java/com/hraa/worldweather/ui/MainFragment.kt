package com.hraa.worldweather.ui

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.hraa.worldweather.R
import com.hraa.worldweather.adapter.ForecastAdapter
import com.hraa.worldweather.constants.*
import com.hraa.worldweather.enums.WeatherResultState
import com.hraa.worldweather.services.NotificationReceiver
import com.hraa.worldweather.services.NotificationService
import com.hraa.worldweather.sixteen_weather_model.Data
import com.hraa.worldweather.view_model.WeatherViewModel
import kotlinx.android.synthetic.main.fragment_main.*
import java.util.*

class MainFragment : Fragment(), ForecastAdapter.OnDayItemClick,
    NavigationView.OnNavigationItemSelectedListener, TextWatcher {

    private val weatherViewModel: WeatherViewModel by activityViewModels()

    private val recyclerAdapter by lazy { ForecastAdapter(this) }

    private val dialog by lazy { Dialog(this.requireContext()) }
    private var isDialogShowing = false


    private val sharedPref by lazy {
        activity?.getSharedPreferences(
            SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
    }

    private var sixteenForecastList: List<Data> = emptyList()

    private lateinit var okBtn: Button
    private lateinit var closeDialog: ImageView
    private lateinit var cityNameEditText: EditText

    private lateinit var units: String
    private lateinit var tempUnitText: TextView

    private lateinit var switchNotif: Switch

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e("MainFragment", "current weather livedata is ${weatherViewModel.currentWeather.value}")

        if (savedInstanceState != null) {
            // Check if dialog was showing, If so.. show it.. else dismiss it
            isDialogShowing = savedInstanceState.getBoolean(IS_DIALOG_SHOWING_KEY)
            if (isDialogShowing) {
                dialog.show()
            } else {
                dialog.dismiss()
            }
        }

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.mainFragment), drawer_layout)
        nav_view.setupWithNavController(navController)
        main_toolbar.setupWithNavController(navController, appBarConfiguration)

        nav_view.setNavigationItemSelectedListener(this)
        //Get units from sharedpref
        units = sharedPref?.getString(UNITS_SHARED_PREF, "M")!!
        //Get item one and put the view for it
        switchNotif =
            nav_view.menu.getItem(1)
                .setActionView(R.layout.notification_nav_item).actionView.findViewById(R.id.notif_switch)
        switchNotif.isChecked = sharedPref!!.getBoolean("isSwitchChecked", false)

        switchNotif.setOnCheckedChangeListener { buttonView, isChecked ->
            sharedPref?.edit()?.putBoolean("isSwitchChecked", isChecked)?.apply()
            if (buttonView.isPressed)
                switchStateHandling(isChecked)
        }
        //Get item two and put the view for it
        val tempView = nav_view.menu.getItem(2).setActionView(R.layout.temp_nav_item).actionView
        //Get the child view from parent
        tempUnitText = tempView.findViewById(R.id.temp_unit_txt)
        //Put the units to its text
        tempUnitText.text = when (units) {
            "M" -> "C"
            "I" -> "F"
            else -> ""
        }

        //Get item four and put the view for it
        nav_view.menu.getItem(4).setActionView(R.layout.version_nav_item)

        //Saving the showed location as the last one!
        sharedPref?.edit()
            ?.putString(LAST_LOCATION_SHARED_PREF, weatherViewModel.lastLocation.value)?.apply()

        setDialog()
        //Listen to text changes
        cityNameEditText.addTextChangedListener(this)

        observeSixteenDaysForecast()

        observeCurrentWeather()

        observeWeatherResult()

        setRecycler()

        refresh_weather.setOnRefreshListener {
            weatherViewModel.getWeatherByCityName(
                weatherViewModel.lastLocation.value!!,
                units
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_DIALOG_SHOWING_KEY, isDialogShowing)
    }

//    override fun onStop() {
//        super.onStop()
//        sharedPref?.edit()
//            ?.putString(LAST_LOCATION_SHARED_PREF, weatherViewModel.lastLocation.value)?.apply()
//    }

    private fun setDialog() {
        dialog.setContentView(R.layout.dialog_entering_city)
        dialog.setCancelable(false)
        okBtn = dialog.findViewById(R.id.okBtn_dialog)
        closeDialog = dialog.findViewById(R.id.close_dialog)
        cityNameEditText = dialog.findViewById(R.id.city_edtxt_dialog)

        add_icon.setOnClickListener {
            dialog.show()
            isDialogShowing = true
        }
        okBtn.setOnClickListener {
            okBtn.isEnabled = false
            weatherViewModel.getWeatherByCityName(cityNameEditText.text.toString(), units)
        }
        closeDialog.setOnClickListener {
            dialog.dismiss()
            isDialogShowing = false
        }
    }

    private fun setRecycler() {
        daily_recycler.apply {
            adapter = recyclerAdapter
        }
    }

    private fun observeWeatherResult() {
        weatherViewModel.weatherResult.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it.name) {
                    WeatherResultState.NO_INTERNET.toString() -> {
                        okBtn.isEnabled = true
                        this.requireContext().toast(resources.getString(R.string.error_no_internet))
                        weatherViewModel.onFinishWeatherResult()
                    }

                    WeatherResultState.WRONG_CITY_NAME.toString() -> {
                        cityNameEditText.error = resources.getString(R.string.error_city_name)
                        weatherViewModel.onFinishWeatherResult()
                    }
                    WeatherResultState.FINISHED.toString() -> {
                        if (dialog.isShowing) {
                            dialog.dismiss()
                            isDialogShowing = false
                        }
                        refresh_weather.isRefreshing = false
                        weatherViewModel.onFinishWeatherResult()
                    }
                    WeatherResultState.EXCEPTION.toString() -> {
                        this.requireContext().toast(resources.getString(R.string.error_general))
                        refresh_weather.isRefreshing = false
                        weatherViewModel.onFinishWeatherResult()
                    }
                }
            }
        })
    }

    private fun observeCurrentWeather() {
        weatherViewModel.currentWeather.observe(viewLifecycleOwner, Observer { currentWeather ->
            if (!currentWeather.isNullOrEmpty()) {
                var date = currentWeather[0].datetime
                val formattedDate = date.removeRange(10, date.length)

                val day =
                    weatherViewModel.whatDay(weatherViewModel.getDayFromDate(formattedDate))

                date = "$day, $formattedDate"

                temp_txt.text = currentWeather[0].temp.toInt().toString()
                place_txt.text = currentWeather[0].cityName
                temp_app_text.text =
                    resources.getString(R.string.feels_like, currentWeather[0].appTemp.toInt())

                description.text = currentWeather[0].weather.description
                date_txt.text = date

            } else {
                Toast.makeText(
                    this.requireContext(),
                    "Please refresh the page...",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    override fun onClick(position: Int) {
        if (sixteenForecastList.isNotEmpty()) {
            val bundle = bundleOf("position" to position)
            findNavController().navigate(R.id.action_mainFragment_to_dayFragment, bundle)
        }
    }

    private fun observeSixteenDaysForecast() {
        weatherViewModel.sixteenDaysForecast.observe(
            viewLifecycleOwner,
            Observer { sixteenForecast ->
                if (!sixteenForecast.data.isNullOrEmpty()) {
                    sixteenForecastList = sixteenForecast.data
                    recyclerAdapter.setData(sixteenForecast)
                } else {
                    Toast.makeText(this.requireContext(), "Sixteen is null", Toast.LENGTH_LONG)
                        .show()
                }
            })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.temp_unit_item -> {
                val items = arrayOf("C", "F")
                val alertDialog = AlertDialog.Builder(this.requireContext())
                alertDialog.setTitle(resources.getString(R.string.temp_unit))
                alertDialog.setSingleChoiceItems(items, -1) { dialog, indexItem ->
                    when (items[indexItem]) {
                        "C" -> {
                            units_txt.text = items[indexItem]
                            tempUnitText.text = items[indexItem]
                            sharedPref?.edit()?.putString(UNITS_SHARED_PREF, "M")?.apply()

                            weatherViewModel.getWeatherByCityName(
                                weatherViewModel.lastLocation.value!!,
                                sharedPref?.getString(UNITS_SHARED_PREF, "M")!!
                            )
                        }
                        "F" -> {
                            units_txt.text = items[indexItem]
                            tempUnitText.text = items[indexItem]
                            sharedPref?.edit()?.putString(UNITS_SHARED_PREF, "I")?.apply()

                            weatherViewModel.getWeatherByCityName(
                                weatherViewModel.lastLocation.value!!,
                                sharedPref?.getString(UNITS_SHARED_PREF, "M")!!
                            )

                        }
                    }
                    dialog.dismiss()
                    isDialogShowing = false
                }
                alertDialog.create().show()
            }

            R.id.locationFragment -> findNavController().navigate(R.id.action_mainFragment_to_locationFragment)

            R.id.notification_item -> {
                Log.e("MainFragment", "notification item clicked")

                switchNotif.isChecked = !switchNotif.isChecked

                switchStateHandling(switchNotif.isChecked)
            }
        }
        return true
    }

    private fun switchStateHandling(isChecked: Boolean) {
        if (isChecked) {
            Log.e("MainFragment", "Switch is turned on")
            val lastLocation = requireContext().getSharedPreferences(
                SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
                .getString(LAST_LOCATION_SHARED_PREF, null)
            if (!lastLocation.isNullOrEmpty()) {
                Log.e("MainFragment", "Switch checked and location available")
                val cal = Calendar.getInstance()

                val alarmManager =
                    requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val pendingIntent =
                    PendingIntent.getBroadcast(
                        activity?.applicationContext,
                        0,
                        Intent(requireActivity(), NotificationReceiver::class.java).apply {
                            putExtra("units", units)
                            putExtra("cityName", lastLocation)
                            action = "com.hraa.worldweather.alarm.alerted"
                        },
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            }
        } else {
            Log.e("MainFragment", "Switch is turned off")
            val alarmManager =
                requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val inten = Intent(activity?.applicationContext, NotificationReceiver::class.java).apply {
                action = "com.hraa.worldweather.alarm.alerted"
            }

            alarmManager.cancel(
                PendingIntent.getBroadcast(
                    activity?.applicationContext,
                    0,
                    inten,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
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
}