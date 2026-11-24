package com.example.sanaesenciaapp.data.remote

import com.example.sanaesenciaapp.data.room.*
import retrofit2.Response
import retrofit2.http.*

// --- DTOs de Autenticación ---
data class LoginRequest(val email: String, val password: String)

data class RegistroRequest(
    val email: String,
    val nombre: String,
    val password: String,
    val rut: String,
    val telefono: String,
    val fechaNacimiento: String, // "YYYY-MM-DD"
    val comuna: String,
    val rol: String
)

data class UsuarioDto(
    val email: String,
    val nombre: String,
    val rut: String,
    val telefono: String,
    val fechaNacimiento: String,
    val comuna: String,
    val rol: String,
    val activo: Boolean
)

data class AuthResponse(
    val token: String,
    val tipo: String,
    val expiraEnSegundos: Long,
    val usuario: UsuarioDto
)

data class CrearEspecialidadRequest(
    val nombre: String,
    val descripcion: String,
    val icono: String,
    val profesional: String,
    val modalidad: String,
    val agendaPausada: Boolean
)

data class CrearEspecialistaRequest(
    val nombre: String,
    val especialidadId: Int,
    val modalidad: String
)



// --- 1. usuarios-service (:8081) ---
interface UsuariosApi {
    @POST("api/usuarios/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/usuarios/registro")
    suspend fun registro(@Body request: RegistroRequest): Response<AuthResponse>

    @GET("api/usuarios/{email}")
    suspend fun obtenerPorEmail(@Path("email") email: String): Response<UsuarioDto>

    @GET("api/beneficiarios")
    suspend fun listarBeneficiarios(@Query("propietarioEmail") email: String): Response<List<BeneficiarioEntity>>

    @POST("api/beneficiarios")
    suspend fun crearBeneficiario(@Body beneficiario: BeneficiarioEntity): Response<BeneficiarioEntity>

    @PUT("api/beneficiarios/{id}")
    suspend fun actualizarBeneficiario(
        @Path("id") id: Int,
        @Body beneficiario: BeneficiarioEntity
    ): Response<BeneficiarioEntity>

    @PATCH("api/beneficiarios/{id}/desactivar")
    suspend fun desactivarBeneficiario(@Path("id") id: Int): Response<BeneficiarioEntity>

    @PATCH("api/beneficiarios/{id}/reactivar")
    suspend fun reactivarBeneficiario(@Path("id") id: Int): Response<BeneficiarioEntity>
}

// --- 2. catalogo-service (:8082) ---
interface CatalogoApi {
    @GET("api/especialidades")
    suspend fun listarEspecialidades(): Response<List<EspecialidadEntity>>

    @POST("api/especialidades")
    suspend fun crearEspecialidad(@Body especialidad: CrearEspecialidadRequest): Response<EspecialidadEntity>
}

// --- 3. terapeutas-service (:8083) ---
interface TerapeutasApi {
    @GET("api/especialistas")
    suspend fun listarEspecialistas(@Query("especialidadId") especialidadId: Int? = null): Response<List<EspecialistaEntity>>

    @POST("api/especialistas")
    suspend fun crearEspecialista(@Body especialista: CrearEspecialistaRequest): Response<EspecialistaEntity>
}

// --- 4. agenda-service (:8084) ---
interface AgendaApi {
    @GET("api/citas")
    suspend fun listarCitas(@Query("usuarioEmail") email: String? = null): Response<List<CitaEntity>>

    @POST("api/citas")
    suspend fun reservarCita(@Body cita: CrearCitaRequest): Response<CitaEntity>

    @PATCH("api/citas/{id}/estado")
    suspend fun cambiarEstadoCita(
        @Path("id") id: Int,
        @Query("estado") estado: String,
        @Query("boxAsignado") boxAsignado: Int? = null
    ): Response<CitaEntity>
}

// --- 5. tienda-service (:8085) ---
data class CrearVentaRequest(
    val compradorEmail: String,
    val compradorNombre: String,
    val metodoPago: String,
    val detalles: List<DetalleVentaItemRequest>
)

data class DetalleVentaItemRequest(
    val productoId: Int,
    val cantidad: Int
)

data class VentaConDetallesResponse(
    val id: Int,
    val compradorEmail: String,
    val compradorNombre: String,
    val fechaMillis: Long,
    val total: Int,
    val estado: String,
    val metodoPago: String,
    val detalles: List<DetalleVentaEntity>
)

// --- 5. tienda-service (:8085) ---
interface TiendaApi {
    @GET("api/productos")
    suspend fun listarProductos(
        @Query("soloActivos") soloActivos: Boolean = false
    ): Response<List<ProductoEntity>>

    @POST("api/productos")
    suspend fun crearProducto(@Body producto: ProductoEntity): Response<ProductoEntity>

    @PUT("api/productos/{id}")
    suspend fun actualizarProducto(
        @Path("id") id: Int,
        @Body producto: ProductoEntity
    ): Response<ProductoEntity>

    @POST("api/ventas")
    suspend fun registrarVenta(@Body venta: CrearVentaRequest): Response<VentaConDetallesResponse>

    @GET("api/ventas")
    suspend fun listarVentas(@Query("compradorEmail") email: String): Response<List<VentaConDetallesResponse>>
}
data class CrearCitaRequest(
    val usuarioEmail: String,
    val especialidadId: Int,
    val especialidadNombre: String,
    val especialistaId: Int,
    val especialistaNombre: String,
    val modalidad: String,
    val inicioMillis: Long,
    val finMillis: Long,
    val hora: String,
    val pacienteNombre: String,
    val beneficiarioId: Int?,
    val estado: String,
    val boxAsignado: Int?
)