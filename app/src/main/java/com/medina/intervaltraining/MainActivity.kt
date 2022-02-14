package com.medina.intervaltraining

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.medina.intervaltraining.preview.SampleData
import com.medina.intervaltraining.room.*
import com.medina.intervaltraining.screens.*
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import com.medina.intervaltraining.viewmodel.Training
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob



class MainActivity : ComponentActivity() {
    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    private val database by lazy { WordRoomDatabase.getDatabase(this, applicationScope) }
    private val repository by lazy { WordRepository(database.wordDao()) }
    private val wordViewModel: WordViewModel by viewModels {
        WordViewModelFactory(repository)
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
                    IntervalTrainingApp(wordViewModel)
                }
            }
        }
    }
}

@Composable
fun IntervalTrainingApp(wordViewModel: WordViewModel) {
    var isFirstRun by remember { mutableStateOf(true) }
    val navController = rememberNavController()
    val backstackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backstackEntry.value?.destination?.route

    if (isFirstRun) {
        FirstRunScreen(onStart = { isFirstRun = false })
    } else {
        IntervalTrainingNavHost(navController,wordViewModel)
    }
}

enum class IntervalTrainingScreens{
    Welcome,
    Training,
    Editor
}

@Composable
fun IntervalTrainingNavHost(
    navController: NavHostController,
    wordViewModel: WordViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = IntervalTrainingScreens.Welcome.name,
        modifier = modifier
    ) {
        composable(IntervalTrainingScreens.Welcome.name) {
            IntervalTrainingScreen(
                wordViewModel = wordViewModel,
                trainingList = SampleData.trainingList,
                trainedHours = 1.5f,
                onNewTraining = {
                    wordViewModel.insert(Word("Test"))
                   // navController.navigate(IntervalTrainingScreens.Editor.name)
                },
                onPlay = {training, immediate ->
                    val index = SampleData.trainingList.indexOf(training)
                    navController.navigate("${IntervalTrainingScreens.Training.name}/$index/$immediate")
                }
            )
        }
        composable(
            "${IntervalTrainingScreens.Training.name}/{$TRAINING_INDEX_KEY}/{immediate}",
            arguments = listOf(
                navArgument(TRAINING_INDEX_KEY) {
                    type = NavType.IntType
                },
                navArgument("immediate"){
                    type = NavType.BoolType
                }
            ),
        ) { entry ->
            val immediate = entry.arguments?.getBoolean("immediate")?:false
            val training = getTrainingByNavEntry(entry)
            ExerciseTableScreen(training = training,
                immediate = immediate,
                onBack = { navController.popBackStack() },
                onEdit = {
                    val index = SampleData.trainingList.indexOf(training)
                    navController.navigate("${IntervalTrainingScreens.Editor.name}/$index")
                }
            )
        }
        composable(
            "${IntervalTrainingScreens.Editor.name}/{$TRAINING_INDEX_KEY}",
            arguments = listOf(
                navArgument(TRAINING_INDEX_KEY) {
                    type = NavType.IntType
                },
            ),
        ) { entry ->
            val training = getTrainingByNavEntry(entry)
            EditExerciseTableScreen(training = training,
                onBack = { navController.popBackStack() },
                onDelete = {},
                onUpdateTraining = {}
            )
        }
    }
}

fun getTrainingByNavEntry(navBackStackEntry:NavBackStackEntry):Training{
    val index = navBackStackEntry.arguments?.getInt(TRAINING_INDEX_KEY)?:0
    return SampleData.trainingList[index]
}

const val TRAINING_INDEX_KEY = "index"

@Preview
@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun DefaultPreview() {
    IntervalTrainingTheme {
        IntervalTrainingApp(viewModel())
    }
}
