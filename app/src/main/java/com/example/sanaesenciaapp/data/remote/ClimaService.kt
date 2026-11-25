package com.example.sanaesenciaapp.data.remote

import com.example.sanaesenciaapp.utils.UbicacionHelper
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CancellationException
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class OpenMeteoResponse(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val current: ClimaActualDto? = null
)

data class ClimaActualDto(
    @SerializedName("temperature_2m")
    val temperature2m: Double = 0.0,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity2m: Int = 0,
    @SerializedName("apparent_temperature")
    val apparentTemperature: Double = 0.0,
    @SerializedName("weather_code")
    val weatherCode: Int = 0,
    @SerializedName("wind_speed_10m")
    val windSpeed10m: Double = 0.0
)

data class ClimaSede(
    val temperaturaC: Double,
    val sensacionTermicaC: Double,
    val humedadPorcentaje: Int,
    val vientoKmh: Double,
    val codigoClima: Int,
    val icono: String,
    val descripcion: String,
    val recomendacionBienestar: String
)

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun obtenerClimaActual(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") current: String =
            "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m",
        @Query("timezone") timezone: String = "America/Santiago"
    ): Response<OpenMeteoResponse>
}

class ClimaRepository(
    private val api: OpenMeteoApi = crearApiPorDefecto()
) {
    suspend fun obtenerClimaSede(
        latitud: Double = UbicacionHelper.LAT_CENTRO,
        longitud: Double = UbicacionHelper.LON_CENTRO
    ): Result<ClimaSede> {
        return try {
            val response = api.obtenerClimaActual(latitud = latitud, longitud = longitud)
            if (!response.isSuccessful) {
                return Result.failure(
                    IllegalStateException("No se pudo obtener el clima (HTTP ${response.code()})")
                )
            }
            val current = response.body()?.current
                ?: return Result.failure(IllegalStateException("Respuesta de clima sin datos actuales"))

            val (icono, descripcion, recomendacion) = interpretarCodigoWmo(current.weatherCode)
            Result.success(
                ClimaSede(
                    temperaturaC = current.temperature2m,
                    sensacionTermicaC = current.apparentTemperature,
                    humedadPorcentaje = current.relativeHumidity2m,
                    vientoKmh = current.windSpeed10m,
                    codigoClima = current.weatherCode,
                    icono = icono,
                    descripcion = descripcion,
                    recomendacionBienestar = recomendacion
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val BASE_URL = "https://api.open-meteo.com/"

        fun crearApiPorDefecto(): OpenMeteoApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenMeteoApi::class.java)
        }

        fun interpretarCodigoWmo(codigo: Int): Triple<String, String, String> = when (codigo) {
            0 -> Triple(
                "☀️",
                "Cielo despejado",
                "Ideal para asistir a tu sesión presencial o caminar junto a la costa antes de tu terapia."
            )
            1, 2 -> Triple(
                "🌤️",
                "Parcialmente nublado",
                "Clima templado y agradable en Los Vilos para tu visita al centro."
            )
            3 -> Triple(
                "☁️",
                "Nublado",
                "Día tranquilo; recuerda abrigarte ligeramente si vienes a sesión presencial."
            )
            45, 48 -> Triple(
                "🌫️",
                "Neblina costera",
                "Visibilidad reducida en el sector costero; sal con unos minutos extra si vienes presencial."
            )
            51, 53, 55, 56, 57 -> Triple(
                "🌦️",
                "Llovizna",
                "Lleva paraguas o chaqueta impermeable, o considera cambiar a modalidad online si lo prefieres."
            )
            61, 63, 65, 66, 67, 80, 81, 82 -> Triple(
                "🌧️",
                "Lluvia",
                "Día lluvioso en la sede; nuestra modalidad online está disponible si prefieres quedarte en casa."
            )
            71, 73, 75, 77, 85, 86 -> Triple(
                "❄️",
                "Nieve o aguanieve",
                "Condiciones frías; prioriza tu seguridad en traslados o elige atención online."
            )
            95, 96, 99 -> Triple(
                "⛈️",
                "Tormenta eléctrica",
                "Precaución en desplazamientos hacia la sede."
            )
            else -> Triple(
                "🌡️",
                "Condición variable",
                "Consulta el pronóstico local antes de tu traslado a la sede."
            )
        }
    }
}
