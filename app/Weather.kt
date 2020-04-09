
import com.google.gson.annotations.SerializedName

data class Weather(
    val code: Int,
    val description: String,
    val icon: String
)