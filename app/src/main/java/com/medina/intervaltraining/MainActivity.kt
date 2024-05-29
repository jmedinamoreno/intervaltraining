package com.medina.intervaltraining

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jetcaster.util.viewModelProviderFactoryOf
import com.medina.intervaltraining.data.repository.TrainingRoomRepository
import com.medina.intervaltraining.data.room.TrainingRoomDatabase
import com.medina.intervaltraining.data.viewmodel.ExerciseViewModel
import com.medina.intervaltraining.data.viewmodel.TrainingViewModel
import com.medina.intervaltraining.data.viewmodel.TrainingViewModelFactory
import com.medina.intervaltraining.screens.EditExerciseTableScreen
import com.medina.intervaltraining.screens.IntervalTrainingScreen
import com.medina.intervaltraining.screens.PlayExerciseTableScreen
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.util.UUID

class MainActivity : ComponentActivity() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { TrainingRoomDatabase.getDatabase(this, applicationScope) }
    private val repository by lazy { TrainingRoomRepository(database.trainingDao()) }
    private val trainingViewModel: TrainingViewModel by viewModels {
        TrainingViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntervalTrainingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    IntervalTrainingApp(trainingViewModel)
                }
            }
        }
    }
}

@Composable
fun IntervalTrainingApp(trainingViewModel: TrainingViewModel) {
    var isFirstRun by remember { mutableStateOf(true) }
    val navController = rememberNavController()
    val backstackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backstackEntry.value?.destination?.route

    IntervalTrainingNavHost(navController,trainingViewModel)
}

enum class IntervalTrainingScreens{
    Welcome,
    Training,
    Editor
}

@Composable
fun IntervalTrainingNavHost(
    navController: NavHostController,
    trainingViewModel: TrainingViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = IntervalTrainingScreens.Welcome.name,
        modifier = modifier
    ) {
        composable(IntervalTrainingScreens.Welcome.name) {
            IntervalTrainingScreen(
                trainingViewModel = trainingViewModel,
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
            val viewModel: ExerciseViewModel = viewModel(
                key = "exercise_model_for_$training",
                factory = viewModelProviderFactoryOf { ExerciseViewModel(
                    repository = trainingViewModel.repository, UUID.fromString(training)
                ) }
            )
            PlayExerciseTableScreen( exerciseViewModel = viewModel,
                immediate = immediate,
                onBack = { navController.popBackStack() },
                onEdit = {
                    navController.navigate("${IntervalTrainingScreens.Editor.name}/${training}")
                },
                updateSession = { session ->
                    trainingViewModel.saveSession(session)
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
            val viewModel: ExerciseViewModel = viewModel(
                key = "exercise_model_for_$training",
                factory = viewModelProviderFactoryOf { ExerciseViewModel(
                    repository = trainingViewModel.repository, training
                ) }
            )
            EditExerciseTableScreen( exerciseViewModel = viewModel,
                onBack = { navController.popBackStack() },
                onDelete = { trainingViewModel.delete(training);navController.popBackStack();navController.popBackStack() },
                onUpdateTraining = {trainingViewModel.update(it)},
                onExerciseListUpdated = { viewModel.saveExerciseList(it) }
            )
        }
    }
}

const val TRAINING_KEY = "training_key"
const val IMMEDIATE_FLAG = "immediate"
