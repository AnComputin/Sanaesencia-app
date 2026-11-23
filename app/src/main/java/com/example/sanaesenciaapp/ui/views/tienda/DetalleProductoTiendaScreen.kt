package com.example.sanaesenciaapp.ui.views.tienda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.sanaesenciaapp.ui.components.SanaBackground
import com.example.sanaesenciaapp.viewmodel.EstadoCatalogoTienda
import com.example.sanaesenciaapp.viewmodel.TiendaViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DetalleProductoTiendaScreen(
    productoId: Int,
    navController: NavController,
    tiendaViewModel: TiendaViewModel
) {
    val catalogo by tiendaViewModel.estadoCatalogo.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Detalle del producto", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) { Text("Volver") }
                }
            )
        }
    ) { padding ->
        SanaBackground {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                when (val estado = catalogo) {
                    EstadoCatalogoTienda.Cargando -> CircularProgressIndicator()
                    EstadoCatalogoTienda.Error -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("No se pudo cargar el producto.", color = MaterialTheme.colorScheme.error)
                        Button(onClick = tiendaViewModel::reintentarCatalogo) { Text("Reintentar") }
                    }
                    EstadoCatalogoTienda.Vacio -> ProductoNoEncontrado(navController)
                    is EstadoCatalogoTienda.Contenido -> {
                        val producto = estado.producto(productoId)
                        if (producto == null || !producto.activo) {
                            ProductoNoEncontrado(navController)
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(producto.icono, fontSize = 56.sp)
                                    Text(producto.nombre, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    Text(producto.categoria, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                    Text(producto.descripcion, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(formatearPrecioTienda(producto.precio), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        if (producto.stock > 0) "Disponibles: ${producto.stock}" else "Sin stock",
                                        color = if (producto.stock > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Button(
                                        onClick = { tiendaViewModel.agregarAlCarrito(producto) },
                                        enabled = producto.stock > 0,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Agregar al carrito")
                                    }
                                    OutlinedButton(
                                        onClick = { navController.popBackStack() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Volver al catálogo") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductoNoEncontrado(navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Producto no disponible", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        Text("Este producto no existe o ya no está activo.")
        Button(onClick = { navController.popBackStack() }) { Text("Volver al catálogo") }
    }
}
