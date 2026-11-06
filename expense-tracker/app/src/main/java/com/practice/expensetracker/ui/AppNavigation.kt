package com.practice.expensetracker.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.practice.expensetracker.ExpenseApp
import com.practice.expensetracker.ui.common.LocalSnackbarHostState
import com.practice.expensetracker.ui.common.TestTags
import com.practice.expensetracker.ui.common.appiumRoot
import com.practice.expensetracker.ui.debug.DebugScreen
import com.practice.expensetracker.ui.edit.AddEditExpenseScreen
import com.practice.expensetracker.ui.edit.AddEditViewModel
import com.practice.expensetracker.ui.list.ExpenseListScreen
import com.practice.expensetracker.ui.settings.SettingsScreen
import com.practice.expensetracker.ui.stats.StatsScreen

object Routes {
    const val EXPENSES = "expenses"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val ADD = "add"
    const val EDIT = "edit/{${AddEditViewModel.ARG_EXPENSE_ID}}"
    const val DEBUG = "debug"

    fun edit(id: Long) = "edit/$id"
}

private const val DEEP_LINK = "expensetracker://"

private data class TopLevel(val route: String, val label: String, val icon: ImageVector, val tag: String)

private val topLevel = listOf(
    TopLevel(Routes.EXPENSES, "Expenses", Icons.AutoMirrored.Filled.ReceiptLong, TestTags.NAV_EXPENSES),
    TopLevel(Routes.STATS, "Insights", Icons.Filled.BarChart, TestTags.NAV_STATS),
    TopLevel(Routes.SETTINGS, "Settings", Icons.Filled.Settings, TestTags.NAV_SETTINGS),
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = topLevel.any { it.route == currentRoute }

    val messages = (LocalContext.current.applicationContext as ExpenseApp).container.messages
    LaunchedEffect(Unit) {
        for (message in messages) snackbarHostState.showSnackbar(message)
    }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
    Scaffold(
        modifier = Modifier.fillMaxSize().appiumRoot(),
        contentWindowInsets = if (showBottomBar) WindowInsets(0) else WindowInsets.navigationBars,
        bottomBar = {
            if (showBottomBar) BottomBar(navController, currentRoute)
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.EXPENSES,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.EXPENSES) {
                ExpenseListScreen(
                    snackbarHostState = snackbarHostState,
                    onAddExpense = { navController.navigate(Routes.ADD) },
                    onOpenExpense = { id -> navController.navigate(Routes.edit(id)) },
                    onOpenDebug = { navController.navigate(Routes.DEBUG) },
                )
            }
            composable(
                Routes.STATS,
                deepLinks = listOf(navDeepLink { uriPattern = "${DEEP_LINK}stats" }),
            ) { StatsScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(onOpenDebug = { navController.navigate(Routes.DEBUG) })
            }
            composable(
                Routes.ADD,
                deepLinks = listOf(navDeepLink { uriPattern = "${DEEP_LINK}add" }),
            ) {
                AddEditExpenseScreen(onDone = { navController.popBackStack() })
            }
            composable(
                Routes.EDIT,
                arguments = listOf(navArgument(AddEditViewModel.ARG_EXPENSE_ID) { type = NavType.LongType }),
                deepLinks = listOf(navDeepLink { uriPattern = "${DEEP_LINK}expense/{${AddEditViewModel.ARG_EXPENSE_ID}}" }),
            ) {
                AddEditExpenseScreen(onDone = { navController.popBackStack() })
            }
            composable(
                Routes.DEBUG,
                deepLinks = listOf(navDeepLink { uriPattern = "${DEEP_LINK}debug" }),
            ) {
                DebugScreen(onBack = { navController.popBackStack() })
            }
        }
    }
    }
}

@Composable
private fun BottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar(Modifier.testTag(TestTags.BOTTOM_NAV)) {
        topLevel.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
                modifier = Modifier.testTag(item.tag),
            )
        }
    }
}
