
import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("app_max_temp")
    val appMaxTemp: Double,
    @SerializedName("app_min_temp")
    val appMinTemp: Double,
    val clouds: Int,
    @SerializedName("clouds_hi")
    val cloudsHi: Int,
    @SerializedName("clouds_low")
    val cloudsLow: Int,
    @SerializedName("clouds_mid")
    val cloudsMid: Int,
    val datetime: String,
    val dewpt: Double,
    @SerializedName("high_temp")
    val highTemp: Double,
    @SerializedName("low_temp")
    val lowTemp: Double,
    @SerializedName("max_dhi")
    val maxDhi: Any,
    @SerializedName("max_temp")
    val maxTemp: Double,
    @SerializedName("min_temp")
    val minTemp: Double,
    @SerializedName("moon_phase")
    val moonPhase: Double,
    @SerializedName("moonrise_ts")
    val moonriseTs: Int,
    @SerializedName("moonset_ts")
    val moonsetTs: Int,
    val ozone: Double,
    val pop: Int,
    val precip: Int,
    val pres: Double,
    val rh: Int,
    val slp: Double,
    val snow: Int,
    @SerializedName("snow_depth")
    val snowDepth: Int,
    @SerializedName("sunrise_ts")
    val sunriseTs: Int,
    @SerializedName("sunset_ts")
    val sunsetTs: Int,
    val temp: Double,
    val ts: Int,
    val uv: Double,
    @SerializedName("valid_date")
    val validDate: String,
    val vis: Double,
    val weather: Weather,
    @SerializedName("wind_cdir")
    val windCdir: String,
    @SerializedName("wind_cdir_full")
    val windCdirFull: String,
    @SerializedName("wind_dir")
    val windDir: Int,
    @SerializedName("wind_gust_spd")
    val windGustSpd: Double,
    @SerializedName("wind_spd")
    val windSpd: Double
)