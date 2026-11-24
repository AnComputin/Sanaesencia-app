package com.example.sanaesenciaapp.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val HOST = "127.0.0.1"

    private const val URL_USUARIOS = "http://$HOST:8081/"
    private const val URL_CATALOGO = "http://$HOST:8082/"
    private const val URL_TERAPEUTAS = "http://$HOST:8083/"
    private const val URL_AGENDA = "http://$HOST:8084/"
    private const val URL_TIENDA = "http://$HOST:8085/"

    private var okHttpClient: OkHttpClient? = null
    private lateinit var tokenManager: TokenManager

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

    val usuariosApi: UsuariosApi by lazy { buildRetrofit(URL_USUARIOS).create(UsuariosApi::class.java) }
    val catalogoApi: CatalogoApi by lazy { buildRetrofit(URL_CATALOGO).create(CatalogoApi::class.java) }
    val terapeutasApi: TerapeutasApi by lazy { buildRetrofit(URL_TERAPEUTAS).create(TerapeutasApi::class.java) }
    val agendaApi: AgendaApi by lazy { buildRetrofit(URL_AGENDA).create(AgendaApi::class.java) }
    val tiendaApi: TiendaApi by lazy { buildRetrofit(URL_TIENDA).create(TiendaApi::class.java) }
}