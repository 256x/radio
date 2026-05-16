package fumi.day.literalradio.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import fumi.day.literalradio.ui.navigation.NavGraph
import fumi.day.literalradio.ui.theme.LiteralRadioTheme

@Composable
fun App(appViewModel: AppViewModel = hiltViewModel()) {
    val prefs by appViewModel.userPrefs.collectAsState()
    val navController = rememberNavController()

    LiteralRadioTheme(userPrefs = prefs) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            NavGraph(navController = navController, appViewModel = appViewModel)
        }
    }
}
