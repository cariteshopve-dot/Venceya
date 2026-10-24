package agency.lacosta.venceya.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import agency.lacosta.venceya.ui.home.HomeScreen
import agency.lacosta.venceya.ui.item.ItemScreen
import agency.lacosta.venceya.ui.item.ItemViewModel
import agency.lacosta.venceya.ui.settings.SettingsScreen

private object Routes {
    const val HOME = "home"
    const val ITEM = "item?${ItemViewModel.ARG_ID}={${ItemViewModel.ARG_ID}}"
    const val SETTINGS = "settings"

    fun item(id: Long? = null) = if (id == null) "item" else "item?${ItemViewModel.ARG_ID}=$id"
}

@Composable
fun VenceYaNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onAdd = { nav.navigate(Routes.item()) },
                onOpen = { nav.navigate(Routes.item(it)) },
                onSettings = { nav.navigate(Routes.SETTINGS) },
            )
        }
        composable(
            Routes.ITEM,
            arguments = listOf(navArgument(ItemViewModel.ARG_ID) { type = NavType.LongType; defaultValue = 0L }),
        ) {
            ItemScreen(onDone = { nav.popBackStack(Routes.HOME, inclusive = false) })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
    }
}
