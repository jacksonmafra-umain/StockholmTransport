package com.umain.transport.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.umain.transport.app.theme.AppTheme
import com.umain.transport.app.ui.screens.ItemDetailScreen
import com.umain.transport.app.ui.screens.ItemListScreen
import com.umain.transport.app.ui.screens.ModuleSelectionScreen
import kotlinx.serialization.Serializable

@Serializable
data object ModuleSelection

@Serializable
data class ItemList(val moduleId: String)

@Serializable
data class ItemDetail(val moduleId: String, val itemId: String)

@Composable
internal fun App() = AppTheme {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ModuleSelection) {
        composable<ModuleSelection> {
            ModuleSelectionScreen(
                onModuleSelected = { module ->
                    navController.navigate(ItemList(moduleId = module.id))
                }
            )
        }

        composable<ItemList> { backStackEntry ->
            val route: ItemList = backStackEntry.toRoute()

            ItemListScreen(
                moduleId = route.moduleId,
                onBackPressed = { navController.popBackStack() },
                onItemSelected = { selectedModuleId, selectedItemId ->
                    navController.navigate(ItemDetail(moduleId = selectedModuleId, itemId = selectedItemId))
                }
            )
        }

        composable<ItemDetail> { backStackEntry ->
            val route: ItemDetail = backStackEntry.toRoute()

            ItemDetailScreen(
                moduleId = route.moduleId,
                itemId = route.itemId,
                onBackPressed = { navController.popBackStack() }
            )
        }
    }
}