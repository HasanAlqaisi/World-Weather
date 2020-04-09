
import com.google.gson.annotations.SerializedName

data class SixteenWeatherModel(
    @SerializedName("city_name")
    val cityName: String,
    @SerializedName("country_code")
    val countryCode: String,
    val `data`: List<Data>,
    val lat: String,
    val lon: String,
    @SerializedName("state_code")
    val stateCode: String,
    val timezone: String
)