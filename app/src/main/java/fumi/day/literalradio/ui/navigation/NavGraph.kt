package fumi.day.literalradio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fumi.day.literalradio.ui.AppViewModel
import fumi.day.literalradio.ui.list.StationListScreen
import fumi.day.literalradio.ui.player.PlayerScreen
import fumi.day.literalradio.ui.settings.SettingsScreen

object Routes {
    const val LIST = "list"
    const val PLAYER = "player"
    const val SETTINGS = "settings"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    appViewModel: AppViewModel,
) {
    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            StationListScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToPlayer = { navController.navigate(Routes.PLAYER) },
                appViewModel = appViewModel,
            )
        }
        composable(Routes.PLAYER) {
            PlayerScreen(
                onBack = { navController.popBackStack() },
                appViewModel = appViewModel,
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
