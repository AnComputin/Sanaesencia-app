package com.example.sanaesenciaapp.ui.views.tienda

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sanaesenciaapp.R
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.components.SanaBackground
import com.example.sanaesenciaapp.viewmodel.TiendaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagoTiendaScreen(
    navController: NavController,
    appState: AppState,
    tiendaViewModel: TiendaViewModel
) {
    val usuario = appState.usuarioActual
    val carrito by tiendaViewModel.carrito.collectAsState()
    val estadoPago by tiendaViewModel.estadoPago.collectAsState()
    val total = carrito.sumOf { it.producto.precio.toLong() * it.cantidad }.toInt()
    var metodoPago by remember { mutableStateOf("Tarjeta de prueba") }

    LaunchedEffect(estadoPago.resultado) {
        val resultado = estadoPago.resultado
        if (resultado?.exito == true && resultado.ventaId != null) {
            tiendaViewModel.limpiarResultadoPago()
            navController.navigate("compra_confirmada/${resultado.ventaId}/${resultado.total}") {
                popUpTo("tienda") { inclusive = false }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Pago de tu compra", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary) },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) { Text("Volver") }
                }
            )
        }
    ) { padding ->
        SanaBackground {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Resumen de compra",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        carrito.forEach { linea ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${linea.cantidad} × ${linea.producto.nombre}", modifier = Modifier.weight(1f))
                                Text(formatearPrecioTienda(linea.producto.precio * linea.cantidad), fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(formatearPrecioTienda(total), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }

                Text("Método de pago", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = metodoPago == "Tarjeta de prueba",
                        onClick = { metodoPago = "Tarjeta de prueba" },
                        label = { Text("Tarjeta de prueba") }
                    )
                    FilterChip(
                        selected = metodoPago == "Transferencia de prueba",
                        onClick = { metodoPago = "Transferencia de prueba" },
                        label = { Text("Transferencia") }
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Pago de demostración", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.height(5.dp))
                        Text(
                            "Esta operación es completamente simulada. No se realizará ningún cobro ni se solicitarán o almacenarán datos bancarios reales.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (usuario == null) {
                    Text("Debes iniciar sesión para completar tu compra.", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { navController.navigate("login") }, modifier = Modifier.fillMaxWidth()) {
                        Text("Iniciar sesión")
                    }
                } else if (carrito.isEmpty()) {
                    Text("No hay productos en tu carrito.")
                    Button(onClick = { navController.navigate("tienda") }, modifier = Modifier.fillMaxWidth()) {
                        Text("Volver a la tienda")
                    }
                } else {
                    estadoPago.resultado?.takeIf { !it.exito }?.let { error ->
                        Text(error.mensaje, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { tiendaViewModel.simularPago(usuario.email, usuario.nombre, metodoPago) },
                        enabled = !estadoPago.procesando,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(if (estadoPago.procesando) "Procesando..." else "Confirmar pago simulado · ${formatearPrecioTienda(total)}")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompraConfirmadaScreen(
    navController: NavController,
    ventaId: Int,
    total: Int
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Compra confirmada", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary) }
            )
        }
    ) { padding ->
        SanaBackground {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_sanaesencia_logo),
                            contentDescription = "SanaEsencia",
                            modifier = Modifier.size(64.dp)
                        )
                        Text("¡Gracias por tu compra!", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text("Tu pago fue registrado en modo de demostración.")
                        Text("Número de pedido: #$ventaId", fontWeight = FontWeight.SemiBold)
                        Text("Total: ${formatearPrecioTienda(total)}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(5.dp))
                        Button(
                            onClick = {
                                navController.navigate("tienda") {
                                    popUpTo("tienda") { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Volver a la tienda")
                        }
                    }
                }
            }
        }
    }
}
