package com.medina.intervaltraining

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medina.intervaltraining.preview.SampleData
import com.medina.intervaltraining.screens.ExerciseTableScreen
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import com.medina.intervaltraining.viewmodel.Exercise
import com.medina.intervaltraining.viewmodel.ExerciseViewModel

class MainActivity : ComponentActivity() {

    private val exerciseViewModel by viewModels<ExerciseViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntervalTrainingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    IntervalTrainingApp(exerciseViewModel)
                }
            }
        }
    }
}

@Composable
fun IntervalTrainingApp(exerciseViewModel: ExerciseViewModel) {

    val items: List<Exercise> by exerciseViewModel.exerciseItems.observeAsState(listOf())
    var shouldShowOnboarding by remember { mutableStateOf(true) }

    if (shouldShowOnboarding) {
        OnboardingScreen(onStart = {shouldShowOnboarding = false})
    } else {
        ExerciseTableScreen(
            items = items,
            onAddItem = {exerciseViewModel.addItem(it)},
            onRemoveItem = {exerciseViewModel.removeItem(it)}
        )
    }
}

@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to the Basics Codelab!")
            Button(
                modifier = Modifier.padding(vertical = 24.dp),
                onClick = onStart
            ) {
                Text("Continue")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardingPreview() {
    IntervalTrainingTheme {
        OnboardingScreen(onStart = {})
    }
}

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
        ExerciseTableScreen(SampleData.exerciseTable, {}, {})
    }
}
