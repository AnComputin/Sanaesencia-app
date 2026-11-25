package com.example.sanaesenciaapp.ui.views.tienda

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.sanaesenciaapp.ui.components.SanaBackground
import com.example.sanaesenciaapp.viewmodel.LineaCarrito
import com.example.sanaesenciaapp.viewmodel.TiendaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoTiendaScreen(
    navController: NavController,
    tiendaViewModel: TiendaViewModel
) {
    val carrito by tiendaViewModel.carrito.collectAsState()
    val subtotal = carrito.sumOf { it.producto.precio.toLong() * it.cantidad }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Mi carrito", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                },
                navigationIcon = {
                    androidx.compose.material3.TextButton(onClick = { navController.popBackStack() }) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { padding ->
        SanaBackground {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(5.dp))
                if (carrito.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🛍️", fontSize = 42.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Tu carrito está vacío",
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "Agrega productos de la tienda para comenzar.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { navController.navigate("tienda") },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Volver a la tienda")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(carrito, key = { it.producto.id }) { linea ->
                            LineaCarritoCard(
                                linea = linea,
                                onAumentar = { tiendaViewModel.aumentarCantidad(linea.producto.id) },
                                onDisminuir = { tiendaViewModel.disminuirCantidad(linea.producto.id) },
                                onEliminar = { tiendaViewModel.quitarDelCarrito(linea.producto.id) }
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(18.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                                Text(formatearPrecioTienda(subtotal.toInt()), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(Modifier.height(5.dp))
                            Text(
                                "Envío y entrega se coordinan directamente con el centro.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                            Spacer(Modifier.height(13.dp))
                            Button(
                                onClick = { navController.navigate("pago_tienda") },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Continuar al pago · ${formatearPrecioTienda(subtotal.toInt())}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LineaCarritoCard(
    linea: LineaCarrito,
    onAumentar: () -> Unit,
    onDisminuir: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(linea.producto.icono, fontSize = 32.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(linea.producto.nombre, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Text(
                    "${formatearPrecioTienda(linea.producto.precio)} c/u",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    "Total: ${formatearPrecioTienda(linea.producto.precio * linea.cantidad)}",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDisminuir) {
                        Icon(Icons.Default.Remove, contentDescription = "Quitar una unidad")
                    }
                    Text(linea.cantidad.toString(), fontWeight = FontWeight.Bold)
                    IconButton(onClick = onAumentar, enabled = linea.cantidad < linea.producto.stock) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar una unidad")
                    }
                }
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Eliminar producto del carrito",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
