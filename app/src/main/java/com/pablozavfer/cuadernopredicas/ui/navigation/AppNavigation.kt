package com.pablozavfer.cuadernopredicas.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pablozavfer.cuadernopredicas.data.SermonTemplate
import com.pablozavfer.cuadernopredicas.ui.calendar.CalendarScreen
import com.pablozavfer.cuadernopredicas.ui.donate.DonateScreen
import com.pablozavfer.cuadernopredicas.ui.editor.EditorScreen
import com.pablozavfer.cuadernopredicas.ui.library.LibraryScreen
import com.pablozavfer.cuadernopredicas.ui.preach.PreachScreen
import com.pablozavfer.cuadernopredicas.ui.settings.SettingsScreen
import java.time.LocalDate

object Routes {
    const val LIBRARY = "biblioteca"
    const val CALENDAR = "calendario"
    const val DONATE = "donar"
    const val SETTINGS = "ajustes"
    const val EDITOR = "editor/{id}?plantilla={plantilla}&fecha={fecha}"
    const val PREACH = "predicar/{id}"
    const val NO_DATE = Long.MIN_VALUE

    fun editor(id: Long) = "editor/$id"
    fun newSermon(template: SermonTemplate, date: LocalDate? = null) =
        "editor/0?plantilla=${template.name}&fecha=${date?.toEpochDay() ?: NO_DATE}"
    fun preach(id: Long) = "predicar/$id"
}

private enum class TopLevel(val route: String, val label: String, val icon: ImageVector) {
    LIBRARY(Routes.LIBRARY, "Biblioteca", Icons.AutoMirrored.Filled.List),
    CALENDAR(Routes.CALENDAR, "Calendario", Icons.Filled.DateRange),
    DONATE(Routes.DONATE, "Donar", Icons.Filled.Favorite),
    SETTINGS(Routes.SETTINGS, "Ajustes", Icons.Filled.Settings),
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TopLevel.entries.any { it.route == currentRoute }

    Scaffold(
        // Cada pantalla maneja sus propios márgenes del sistema con su barra superior.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevel.entries.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = { navController.navigateTopLevel(item.route) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LIBRARY,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            composable(Routes.LIBRARY) {
                LibraryScreen(
                    onOpenSermon = { navController.navigate(Routes.editor(it)) },
                    onNewSermon = { template -> navController.navigate(Routes.newSermon(template)) },
                    onPreach = { navController.navigate(Routes.preach(it)) },
                )
            }
            composable(Routes.CALENDAR) {
                CalendarScreen(
                    onOpenSermon = { navController.navigate(Routes.editor(it)) },
                    onNewSermonOn = { date -> navController.navigate(Routes.newSermon(SermonTemplate.BLANK, date)) },
                )
            }
            composable(Routes.DONATE) { DonateScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
            composable(
                Routes.EDITOR,
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType },
                    navArgument("plantilla") { type = NavType.StringType; defaultValue = "" },
                    navArgument("fecha") { type = NavType.LongType; defaultValue = Routes.NO_DATE },
                ),
            ) { entry ->
                val args = entry.arguments
                val epochDay = args?.getLong("fecha") ?: Routes.NO_DATE
                EditorScreen(
                    sermonId = args?.getLong("id") ?: 0L,
                    template = SermonTemplate.fromKey(args?.getString("plantilla")),
                    initialDate = if (epochDay == Routes.NO_DATE) null else LocalDate.ofEpochDay(epochDay),
                    onBack = { navController.popBackStack() },
                    onPreach = { navController.navigate(Routes.preach(it)) },
                )
            }
            composable(Routes.PREACH, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
                PreachScreen(
                    sermonId = entry.arguments?.getLong("id") ?: 0L,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
