package com.example.sanaesenciaapp.data.remote

import com.example.sanaesenciaapp.data.room.BeneficiarioEntity
import com.example.sanaesenciaapp.data.room.DetalleVentaEntity
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class RemoteAndClimaTest {

    @After
    fun tearDown() {
        RetrofitClient.resetParaPruebas()
    }

    @Test
    fun `TokenManager guarda obtiene y limpia token en memoria cuando no hay contexto`() {
        val manager = TokenManager(null)
        assertNull(manager.obtenerToken())

        manager.guardarToken("jwt-abc-123")
        assertEquals("jwt-abc-123", manager.obtenerToken())

        manager.limpiarToken()
        assertNull(manager.obtenerToken())
    }

    @Test
    fun `AuthInterceptor agrega header Bearer solo cuando existe token valido`() {
        val manager = TokenManager(null)
        var headerCapturado: String? = "SIN_EJECUTAR"

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(manager))
            .addInterceptor { chain ->
                headerCapturado = chain.request().header("Authorization")
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("{}".toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()

        client.newCall(Request.Builder().url("http://localhost/test").build()).execute().close()
        assertNull(headerCapturado)

        manager.guardarToken("   ")
        client.newCall(Request.Builder().url("http://localhost/test").build()).execute().close()
        assertNull(headerCapturado)

        manager.guardarToken("mi-token-jwt")
        client.newCall(Request.Builder().url("http://localhost/test").build()).execute().close()
        assertEquals("Bearer mi-token-jwt", headerCapturado)
    }

    @Test
    fun `RetrofitClient lanza excepcion si se usa antes de inicializar y funciona tras configurarParaPruebas`() {
        RetrofitClient.resetParaPruebas()
        assertFalse(RetrofitClient.estaInicializado())

        assertThrows(IllegalStateException::class.java) {
            RetrofitClient.usuariosApi
        }

        val tokenManager = TokenManager(null).apply { guardarToken("tok") }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("[]".toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()

        RetrofitClient.configurarParaPruebas("http://localhost/", client, tokenManager)

        assertTrue(RetrofitClient.estaInicializado())
        assertEquals("tok", RetrofitClient.getTokenManager().obtenerToken())
        assertNotNull(RetrofitClient.usuariosApi)
        assertNotNull(RetrofitClient.catalogoApi)
        assertNotNull(RetrofitClient.terapeutasApi)
        assertNotNull(RetrofitClient.agendaApi)
        assertNotNull(RetrofitClient.tiendaApi)
    }

    @Test
    fun `ClimaRepository obtiene clima de Open-Meteo exitosamente`() = runBlocking {
        val json = """
            {
              "latitude": -31.9135,
              "longitude": -71.5168,
              "current": {
                "temperature_2m": 19.4,
                "relative_humidity_2m": 68,
                "apparent_temperature": 18.9,
                "weather_code": 0,
                "wind_speed_10m": 14.2
              }
            }
        """.trimIndent()

        val api = crearOpenMeteoApiConInterceptor { chain ->
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(json.toResponseBody("application/json".toMediaType()))
                .build()
        }

        val resultado = ClimaRepository(api).obtenerClimaSede()

        assertTrue(resultado.isSuccess)
        val clima = resultado.getOrThrow()
        assertEquals(19.4, clima.temperaturaC, 0.01)
        assertEquals(18.9, clima.sensacionTermicaC, 0.01)
        assertEquals(68, clima.humedadPorcentaje)
        assertEquals(14.2, clima.vientoKmh, 0.01)
        assertEquals(0, clima.codigoClima)
        assertEquals("☀️", clima.icono)
        assertEquals("Cielo despejado", clima.descripcion)
    }

    @Test
    fun `ClimaRepository retorna failure ante HTTP error cuerpo sin current o excepcion de red`() = runBlocking {
        val apiHttpError = crearOpenMeteoApiConInterceptor { chain ->
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(503)
                .message("Unavailable")
                .body("error".toResponseBody("text/plain".toMediaType()))
                .build()
        }
        assertTrue(ClimaRepository(apiHttpError).obtenerClimaSede().isFailure)

        val apiSinCurrent = crearOpenMeteoApiConInterceptor { chain ->
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("""{"latitude":-31.9,"longitude":-71.5}""".toResponseBody("application/json".toMediaType()))
                .build()
        }
        assertTrue(ClimaRepository(apiSinCurrent).obtenerClimaSede().isFailure)

        val apiRedError = crearOpenMeteoApiConInterceptor {
            throw IOException("sin internet")
        }
        assertTrue(ClimaRepository(apiRedError).obtenerClimaSede().isFailure)
    }

    @Test
    fun `interpretarCodigoWmo cubre todos los grupos climaticos`() {
        assertEquals("☀️", ClimaRepository.interpretarCodigoWmo(0).first)
        assertEquals("🌤️", ClimaRepository.interpretarCodigoWmo(2).first)
        assertEquals("☁️", ClimaRepository.interpretarCodigoWmo(3).first)
        assertEquals("🌫️", ClimaRepository.interpretarCodigoWmo(45).first)
        assertEquals("🌦️", ClimaRepository.interpretarCodigoWmo(53).first)
        assertEquals("🌧️", ClimaRepository.interpretarCodigoWmo(63).first)
        assertEquals("❄️", ClimaRepository.interpretarCodigoWmo(73).first)
        assertEquals("⛈️", ClimaRepository.interpretarCodigoWmo(95).first)
        assertEquals("🌡️", ClimaRepository.interpretarCodigoWmo(999).first)
    }

    @Test
    fun `DTOs remotos conservan todos sus campos`() {
        val usuarioDto = UsuarioDto("a@b.cl", "Ana", "1-9", "9123", "1990-01-01", "Los Vilos", "PACIENTE", true)
        val auth = AuthResponse("jwt", "Bearer", 3600L, usuarioDto)
        val ventaResp = VentaConDetallesResponse(
            id = 5,
            compradorEmail = "a@b.cl",
            compradorNombre = "Ana",
            fechaMillis = 100L,
            total = 5000,
            estado = "PAGADA",
            metodoPago = "Tarjeta",
            detalles = listOf(DetalleVentaEntity(1, 5, 2, "Aceite", 5000, 1, 5000))
        )
        val regReq = RegistroRequest("a@b.cl", "Ana", "123", "1-9", "9123", "1990-01-01", "Los Vilos", "PACIENTE")
        val espReq = CrearEspecialidadRequest("Acupuntura", "Desc", "🪡", "4", "Presencial", false)
        val docReq = CrearEspecialistaRequest("Dr", 1, "Ambas")
        val citaReq = CrearCitaRequest("a@b.cl", 1, "Esp", 1, "Dr", "ONLINE", 10L, 20L, "09:00", "Ana", null, "CONFIRMADA", null)
        val ventaReq = CrearVentaRequest("a@b.cl", "Ana", "Tarjeta", listOf(DetalleVentaItemRequest(1, 2)))
        val openMeteo = OpenMeteoResponse(1.0, 2.0, ClimaActualDto(20.0, 50, 19.0, 1, 10.0))

        assertEquals("jwt", auth.token)
        assertEquals(5000, ventaResp.total)
        assertEquals("PACIENTE", regReq.rol)
        assertEquals("Acupuntura", espReq.nombre)
        assertEquals("Dr", docReq.nombre)
        assertEquals("09:00", citaReq.hora)
        assertEquals(2, ventaReq.detalles.single().cantidad)
        assertEquals(20.0, openMeteo.current!!.temperature2m, 0.01)
    }

    private fun crearOpenMeteoApiConInterceptor(interceptor: Interceptor): OpenMeteoApi {
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        return Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenMeteoApi::class.java)
    }
}
