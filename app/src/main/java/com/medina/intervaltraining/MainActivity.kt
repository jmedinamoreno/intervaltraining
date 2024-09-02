package com.medina.intervaltraining

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.medina.intervaltraining.data.model.DarkThemeConfig
import com.medina.intervaltraining.data.repository.TrainingRoomRepository
import com.medina.intervaltraining.data.room.TrainingRoomDatabase
import com.medina.intervaltraining.data.viewmodel.MainActivityUiState
import com.medina.intervaltraining.data.viewmodel.MainActivityViewModel
import com.medina.intervaltraining.ui.screens.EditTrainingScreen
import com.medina.intervaltraining.ui.screens.IntervalTrainingScreen
import com.medina.intervaltraining.ui.screens.PlayTrainingScreen
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID

private const val USER_PREFERENCES_NAME = "user_preferences"

private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)

        // Update the uiState
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach { uiState = it }
                    .collect()
            }
        }

        // Keep the splash screen on-screen until the UI state is loaded. This condition is
        // evaluated each time the app needs to be redrawn so it should be fast to avoid blocking
        // the UI.
        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                MainActivityUiState.Loading -> true
                is MainActivityUiState.Success -> false
            }
        }

        setContent {
            IntervalTrainingTheme(
                darkTheme = shouldUseDarkTheme(uiState = uiState),
                useDynamicTheming = shouldUseDynamicTheming(uiState = uiState)
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    IntervalTrainingApp()
                }
            }
        }
    }
}

/**
 * Returns `true` if the dynamic color is disabled, as a function of the [uiState].
 */
@Composable
private fun shouldUseDynamicTheming(
    uiState: MainActivityUiState,
): Boolean = when (uiState) {
    MainActivityUiState.Loading -> false
    is MainActivityUiState.Success -> uiState.uiData.userData.useDynamicColor
}

/**
 * Returns `true` if dark theme should be used, as a function of the [uiState] and the
 * current system context.
 */
@Composable
private fun shouldUseDarkTheme(
    uiState: MainActivityUiState,
): Boolean = when (uiState) {
    MainActivityUiState.Loading -> isSystemInDarkTheme()
    is MainActivityUiState.Success -> when (uiState.uiData.userData.darkThemeConfig) {
        DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        DarkThemeConfig.LIGHT -> false
        DarkThemeConfig.DARK -> true
    }
}


@Composable
fun IntervalTrainingApp() {
    val navController = rememberNavController()
//    val backstackEntry = navController.currentBackStackEntryAsState()
//    val currentRoute = backstackEntry.value?.destination?.route
    IntervalTrainingNavHost(navController)
}

enum class IntervalTrainingScreens{
    Welcome,
    Training,
    Editor
}

@Composable
fun IntervalTrainingNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = IntervalTrainingScreens.Welcome.name,
        modifier = modifier
    ) {
        composable(IntervalTrainingScreens.Welcome.name) {
            IntervalTrainingScreen(
                onNewTraining = {
                    navController.navigate("${IntervalTrainingScreens.Editor.name}/${UUID.randomUUID()}")
                },
                onPlay = {training, immediate ->
                    navController.navigate("${IntervalTrainingScreens.Training.name}/${training.id}/$immediate")
                }
            )
        }
        composable(
            "${IntervalTrainingScreens.Training.name}/{$TRAINING_KEY}/{$IMMEDIATE_FLAG}",
            arguments = listOf(
                navArgument(TRAINING_KEY) {
                    type = NavType.StringType
                },
                navArgument(IMMEDIATE_FLAG){
                    type = NavType.BoolType
                }
            ),
        ) { entry ->
            val immediate = entry.arguments?.getBoolean(IMMEDIATE_FLAG)?:false
            val training = entry.arguments?.getString(TRAINING_KEY)
            PlayTrainingScreen(
                trainingId = UUID.fromString(training),
                immediate = immediate,
                onBack = { navController.popBackStack() },
                onEdit = {
                    navController.navigate("${IntervalTrainingScreens.Editor.name}/${training}")
                }
            )
        }
        composable(
            "${IntervalTrainingScreens.Editor.name}/{$TRAINING_KEY}",
            arguments = listOf(
                navArgument(TRAINING_KEY) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
        ) { entry ->
            val training = entry.arguments?.getString(TRAINING_KEY)?.let { UUID.fromString(it) } ?: UUID.randomUUID()
            EditTrainingScreen(
                trainingId = training,
                onBack = { navController.popBackStack() },
                onDelete = { navController.clearBackStack(IntervalTrainingScreens.Welcome.name)},
            )
        }
    }
}

const val TRAINING_KEY = "training_key"
const val IMMEDIATE_FLAG = "immediate"
