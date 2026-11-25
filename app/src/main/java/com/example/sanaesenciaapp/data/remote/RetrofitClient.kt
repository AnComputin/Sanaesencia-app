package com.example.sanaesenciaapp.data.remote

import android.content.Context
import androidx.annotation.VisibleForTesting
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Cambia USE_REMOTE_BACKEND a true y coloca tu IP/dominio de AWS cuando el backend esté arriba
    private const val USE_REMOTE_BACKEND = true
    private const val REMOTE_BASE_URL = "http://34.236.155.95/"

    private const val LOCAL_HOST = "127.0.0.1"

    private var baseUrlOverride: String? = null

    private fun urlUsuarios(): String =
        baseUrlOverride ?: if (USE_REMOTE_BACKEND) REMOTE_BASE_URL else "http://$LOCAL_HOST:8081/"

    private fun urlCatalogo(): String =
        baseUrlOverride ?: if (USE_REMOTE_BACKEND) REMOTE_BASE_URL else "http://$LOCAL_HOST:8082/"

    private fun urlTerapeutas(): String =
        baseUrlOverride ?: if (USE_REMOTE_BACKEND) REMOTE_BASE_URL else "http://$LOCAL_HOST:8083/"

    private fun urlAgenda(): String =
        baseUrlOverride ?: if (USE_REMOTE_BACKEND) REMOTE_BASE_URL else "http://$LOCAL_HOST:8084/"

    private fun urlTienda(): String =
        baseUrlOverride ?: if (USE_REMOTE_BACKEND) REMOTE_BASE_URL else "http://$LOCAL_HOST:8085/"

    @Volatile
    private var okHttpClient: OkHttpClient? = null
    private lateinit var tokenManager: TokenManager

    @Volatile private var _usuariosApi: UsuariosApi? = null
    @Volatile private var _catalogoApi: CatalogoApi? = null
    @Volatile private var _terapeutasApi: TerapeutasApi? = null
    @Volatile private var _agendaApi: AgendaApi? = null
    @Volatile private var _tiendaApi: TiendaApi? = null

    fun estaInicializado(): Boolean = okHttpClient != null

    fun init(context: Context) {
        if (okHttpClient != null) return

        tokenManager = TokenManager(context.applicationContext)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @VisibleForTesting
    internal fun configurarParaPruebas(
        baseUrl: String,
        client: OkHttpClient,
        tokenManager: TokenManager
    ) {
        baseUrlOverride = baseUrl
        this.tokenManager = tokenManager
        okHttpClient = client
        limpiarApisCache()
    }

    @VisibleForTesting
    internal fun resetParaPruebas() {
        baseUrlOverride = null
        okHttpClient = null
        limpiarApisCache()
    }

    private fun limpiarApisCache() {
        _usuariosApi = null
        _catalogoApi = null
        _terapeutasApi = null
        _agendaApi = null
        _tiendaApi = null
    }

    fun getTokenManager(): TokenManager = tokenManager

    private fun buildRetrofit(baseUrl: String): Retrofit {
        val client = okHttpClient
            ?: throw IllegalStateException("Debes llamar a RetrofitClient.init(context) antes de usar la red")

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val usuariosApi: UsuariosApi
        get() = _usuariosApi ?: buildRetrofit(urlUsuarios()).create(UsuariosApi::class.java).also {
            _usuariosApi = it
        }

    val catalogoApi: CatalogoApi
        get() = _catalogoApi ?: buildRetrofit(urlCatalogo()).create(CatalogoApi::class.java).also {
            _catalogoApi = it
        }

    val terapeutasApi: TerapeutasApi
        get() = _terapeutasApi ?: buildRetrofit(urlTerapeutas()).create(TerapeutasApi::class.java).also {
            _terapeutasApi = it
        }

    val agendaApi: AgendaApi
        get() = _agendaApi ?: buildRetrofit(urlAgenda()).create(AgendaApi::class.java).also {
            _agendaApi = it
        }

    val tiendaApi: TiendaApi
        get() = _tiendaApi ?: buildRetrofit(urlTienda()).create(TiendaApi::class.java).also {
            _tiendaApi = it
        }
}