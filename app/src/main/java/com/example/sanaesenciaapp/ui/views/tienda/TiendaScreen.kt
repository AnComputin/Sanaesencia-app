package com.example.sanaesenciaapp.ui.views.tienda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sanaesenciaapp.data.room.ProductoEntity
import com.example.sanaesenciaapp.navigation.rutaDetalleProducto
import com.example.sanaesenciaapp.ui.components.SanaBackground
import com.example.sanaesenciaapp.viewmodel.EstadoCatalogoTienda
import com.example.sanaesenciaapp.viewmodel.TiendaViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiendaScreen(
    navController: NavController,
    tiendaViewModel: TiendaViewModel
) {
    val estadoCatalogo by tiendaViewModel.estadoCatalogo.collectAsState()
    val carrito by tiendaViewModel.carrito.collectAsState()
    val mensaje by tiendaViewModel.mensaje.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tienda SanaEsencia",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    androidx.compose.material3.TextButton(onClick = { navController.popBackStack() }) {
                        Text("Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("carrito_tienda") }) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Abrir carrito",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = carrito.sumOf { it.cantidad }.toString(),
                        modifier = Modifier.padding(end = 14.dp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            )
        }
    ) { padding ->
        SanaBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Pequeños rituales para tu bienestar",
                        fontSize = 25.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Descubre productos seleccionados para acompañar tus momentos de calma, cuidado y conexión personal.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.76f)
                    )
                    if (mensaje != null) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = mensaje.orEmpty(),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }

                when (val estado = estadoCatalogo) {
                    EstadoCatalogoTienda.Cargando -> item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    EstadoCatalogoTienda.Vacio -> item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "En este momento no hay productos disponibles.",
                                modifier = Modifier.padding(20.dp),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    EstadoCatalogoTienda.Error -> item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("No se pudo cargar el catálogo de productos.", color = MaterialTheme.colorScheme.onErrorContainer)
                                Button(onClick = tiendaViewModel::reintentarCatalogo) { Text("Reintentar") }
                            }
                        }
                    }
                    is EstadoCatalogoTienda.Contenido -> items(estado.productos, key = { it.id }) { producto ->
                        ProductoTiendaCard(
                            producto = producto,
                            onAgregar = { tiendaViewModel.agregarAlCarrito(producto) },
                            onDetalle = { navController.navigate(rutaDetalleProducto(producto.id)) }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { navController.navigate("carrito_tienda") },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Ver carrito (${carrito.sumOf { it.cantidad }})")
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ProductoTiendaCard(
    producto: ProductoEntity,
    onAgregar: () -> Unit,
    onDetalle: () -> Unit
) {
    Card(
        onClick = onDetalle,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = producto.icono, fontSize = 38.sp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = producto.descripcion,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = producto.categoria,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatearPrecioTienda(producto.precio),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Stock: ${producto.stock}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }
                Spacer(Modifier.height(9.dp))
                Button(
                    onClick = onAgregar,
                    enabled = producto.stock > 0,
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text(if (producto.stock > 0) "Agregar al carrito" else "Sin stock")
                }
            }
        }
    }
}

internal fun formatearPrecioTienda(valor: Int): String {
    val numero = NumberFormat.getIntegerInstance(Locale("es", "CL")).format(valor)
    return "\$$numero"
}
