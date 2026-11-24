package com.example.sanaesenciaapp.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sanaesenciaapp.data.AppState
import com.example.sanaesenciaapp.ui.views.tienda.CarritoTiendaScreen
import com.example.sanaesenciaapp.ui.views.tienda.DetalleProductoTiendaScreen
import com.example.sanaesenciaapp.ui.views.tienda.CompraConfirmadaScreen
import com.example.sanaesenciaapp.ui.views.tienda.PagoTiendaScreen
import com.example.sanaesenciaapp.ui.views.tienda.TiendaScreen
import com.example.sanaesenciaapp.viewmodel.TiendaViewModel

internal fun rutaDetalleProducto(productoId: Int): String =
    "producto_detalle/$productoId"

fun NavGraphBuilder.tiendaGraph(
    navController: NavHostController,
    appState: AppState,
    tiendaViewModel: TiendaViewModel
) {
    composable("tienda") {
        TiendaScreen(
            navController = navController,
            tiendaViewModel = tiendaViewModel
        )
    }

    composable("carrito_tienda") {
        CarritoTiendaScreen(
            navController = navController,
            tiendaViewModel = tiendaViewModel
        )
    }

    composable(
        route = "producto_detalle/{productoId}",
        arguments = listOf(navArgument("productoId") { type = NavType.IntType })
    ) { entry ->
        DetalleProductoTiendaScreen(
            productoId = entry.arguments?.getInt("productoId") ?: -1,
            navController = navController,
            tiendaViewModel = tiendaViewModel
        )
    }

    composable("pago_tienda") {
        PagoTiendaScreen(
            navController = navController,
            appState = appState,
            tiendaViewModel = tiendaViewModel
        )
    }

    composable(
        route = "compra_confirmada/{ventaId}/{total}",
        arguments = listOf(
            navArgument("ventaId") { type = NavType.IntType },
            navArgument("total") { type = NavType.IntType }
        )
    ) { entry ->
        CompraConfirmadaScreen(
            navController = navController,
            ventaId = entry.arguments?.getInt("ventaId") ?: 0,
            total = entry.arguments?.getInt("total") ?: 0
        )
    }
}
