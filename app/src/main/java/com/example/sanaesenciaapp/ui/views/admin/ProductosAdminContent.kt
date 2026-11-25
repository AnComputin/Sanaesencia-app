package com.example.sanaesenciaapp.ui.views.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanaesenciaapp.data.room.ProductoEntity
import com.example.sanaesenciaapp.ui.views.tienda.formatearPrecioTienda
import com.example.sanaesenciaapp.viewmodel.TiendaViewModel

@Composable
fun ProductosAdminContent(
    productos: List<ProductoEntity>,
    tiendaViewModel: TiendaViewModel,
    modifier: Modifier = Modifier
) {
    var mostrarDialogoNuevo by remember { mutableStateOf(false) }
    var productoPendienteEliminar by remember { mutableStateOf<ProductoEntity?>(null) }
    var productoEnEdicion by remember { mutableStateOf<ProductoEntity?>(null) }
    val mensaje by tiendaViewModel.mensaje.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            if (!mensaje.isNullOrBlank()) {
                Text(
                    text = mensaje.orEmpty(),
                    color = if (mensaje!!.contains("correctamente", ignoreCase = true) || mensaje!!.contains("guardado", ignoreCase = true)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Gestión de productos",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Agrega productos a la tienda, controla sus existencias y retira los que ya no ofrecerás. Los cambios se guardan en el servidor y se sincronizan con la aplicación.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { mostrarDialogoNuevo = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(15.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(7.dp))
                Text("Agregar producto")
            }
        }

        if (productos.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Aún no hay productos registrados.", Modifier.padding(18.dp))
                }
            }
        } else {
            items(productos, key = { it.id }) { producto ->
                ProductoAdminCard(
                    producto = producto,
                    onAumentar = { tiendaViewModel.actualizarStock(producto.id, producto.stock + 1) },
                    onDisminuir = { tiendaViewModel.actualizarStock(producto.id, (producto.stock - 1).coerceAtLeast(0)) },
                    onEditar = { productoEnEdicion = producto },
                    onEliminar = { productoPendienteEliminar = producto }
                )
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }

    if (mostrarDialogoNuevo) {
        DialogoNuevoProducto(
            productoInicial = null,
            onDismiss = { mostrarDialogoNuevo = false },
            onGuardar = { producto ->
                tiendaViewModel.guardarProducto(producto)
                mostrarDialogoNuevo = false
            }
        )
    }

    productoEnEdicion?.let { producto ->
        DialogoNuevoProducto(
            productoInicial = producto,
            onDismiss = { productoEnEdicion = null },
            onGuardar = { actualizado ->
                tiendaViewModel.guardarProducto(actualizado)
                productoEnEdicion = null
            }
        )
    }

    productoPendienteEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoPendienteEliminar = null },
            title = { Text("Quitar producto") },
            text = { Text("¿Deseas quitar '${producto.nombre}' de la tienda? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        tiendaViewModel.eliminarProducto(producto.id)
                        productoPendienteEliminar = null
                    }
                ) { Text("Quitar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { productoPendienteEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ProductoAdminCard(
    producto: ProductoEntity,
    onAumentar: () -> Unit,
    onDisminuir: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(producto.icono, fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(producto.nombre, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(producto.categoria, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar producto", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Quitar producto", tint = MaterialTheme.colorScheme.error)
                }
            }
            Text(producto.descripcion, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Precio", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                    Text(formatearPrecioTienda(producto.precio), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Stock disponible", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDisminuir, enabled = producto.stock > 0) {
                            Icon(Icons.Default.Remove, contentDescription = "Reducir stock")
                        }
                        Text(producto.stock.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onAumentar) {
                            Icon(Icons.Default.Add, contentDescription = "Aumentar stock")
                        }
                    }
                }
            }
            if (producto.stock == 0) {
                Text("Sin stock: no se mostrará como disponible para compra.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun DialogoNuevoProducto(
    productoInicial: ProductoEntity?,
    onDismiss: () -> Unit,
    onGuardar: (ProductoEntity) -> Unit
) {
    var nombre by remember(productoInicial) { mutableStateOf(productoInicial?.nombre.orEmpty()) }
    var descripcion by remember(productoInicial) { mutableStateOf(productoInicial?.descripcion.orEmpty()) }
    var categoria by remember(productoInicial) { mutableStateOf(productoInicial?.categoria ?: "Bienestar") }
    var precio by remember(productoInicial) { mutableStateOf(productoInicial?.precio?.toString().orEmpty()) }
    var stock by remember(productoInicial) { mutableStateOf(productoInicial?.stock?.toString() ?: "0") }
    var icono by remember(productoInicial) { mutableStateOf(productoInicial?.icono ?: "🌿") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (productoInicial == null) "Nuevo producto" else "Editar producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; error = null },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoría") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = precio,
                        onValueChange = { precio = it.filter(Char::isDigit); error = null },
                        label = { Text("Precio ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it.filter(Char::isDigit); error = null },
                        label = { Text("Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = icono,
                    onValueChange = { icono = it },
                    label = { Text("Ícono o emoji") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val precioNumero = precio.toIntOrNull()
                    val stockNumero = stock.toIntOrNull()
                    when {
                        nombre.isBlank() -> error = "Ingresa el nombre del producto."
                        descripcion.isBlank() -> error = "Ingresa una descripción."
                        categoria.isBlank() -> error = "Ingresa una categoría."
                        precioNumero == null || precioNumero <= 0 -> error = "El precio debe ser mayor que cero."
                        stockNumero == null -> error = "Ingresa un stock válido."
                        else -> onGuardar(
                            ProductoEntity(
                                id = productoInicial?.id ?: 0,
                                nombre = nombre.trim(),
                                descripcion = descripcion.trim(),
                                categoria = categoria.trim(),
                                precio = precioNumero,
                                stock = stockNumero,
                                icono = icono.ifBlank { "🌿" }.trim()
                            )
                        )
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
