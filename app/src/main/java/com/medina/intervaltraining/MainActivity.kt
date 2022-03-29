package com.medina.intervaltraining

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
import java.util.*


class MainActivity : ComponentActivity() {
    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
//    private val database by lazy { WordRoomDatabase.getDatabase(this, applicationScope) }
//    private val repository by lazy { WordRepository(database.wordDao()) }
//    private val wordViewModel: WordViewModel by viewModels {
//        WordViewModelFactory(repository)
//    }

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
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
                    color = MaterialTheme.colors.background
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
                trainedHours = 1.5f,
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
            val viewModel:ExerciseViewModel = viewModel(
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
            val viewModel:ExerciseViewModel = viewModel(
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

const val TRAINING_KEY = "trainingkey"
const val IMMEDIATE_FLAG = "inmediate"
