package com.medina.intervaltraining.screens

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medina.intervaltraining.R
import com.medina.intervaltraining.data.viewmodel.Training
import com.medina.intervaltraining.data.viewmodel.TrainingViewModel
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme

@Composable
fun IntervalTrainingScreen(
    onNewTraining: () -> Unit = {},
    onPlay: (training: Training, immediate:Boolean) -> Unit = { _, _->},
    trainingViewModel: TrainingViewModel
){
    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = onNewTraining) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "New")
        }}
        ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TrainedHoursComponent(trainingViewModel = trainingViewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp))
            TrainingListComponent(trainingViewModel = trainingViewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp)
                    .weight(1f),
                onPlay = onPlay
            )
        }
    }
}

@Composable
fun TrainedHoursComponent(trainingViewModel:TrainingViewModel, modifier: Modifier){
    Column(modifier = modifier) {
        val hours:Float by trainingViewModel.getTrainedThisWeek().observeAsState(initial = 0f)
        Text(stringResource(id = R.string.welcome_trained_time_first_line),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(stringResource(id = R.string.welcome_trained_time_second_line,hours),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(stringResource(id = R.string.welcome_trained_time_third_line),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun TrainingListComponent(
    modifier: Modifier,
    trainingViewModel: TrainingViewModel,
    onPlay: (training: Training, immediate:Boolean) -> Unit = { _, _->},){
    val items: List<Training> by trainingViewModel.trainingList.observeAsState(listOf())
    LazyColumn(modifier = modifier) {
        items(items) { training ->
            val timeMin: Int by trainingViewModel.getTimeForTrainingLiveData(training.id).observeAsState(0)
            TrainingItemComponent(training, timeMin, Modifier.padding(2.dp),
                {onPlay(training,false)},{onPlay(training,true)})
        }
    }
}

@Composable
fun TrainingItemComponent(training: Training, timeMin: Int, modifier: Modifier, onClick:()->Unit, onPlay:()->Unit){
    Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, shadowElevation = 1.dp) {
        Row (modifier = Modifier.fillMaxWidth()){
            Text(stringResource(id = R.string.welcome_training_min,timeMin),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .padding(start = 8.dp, end = 4.dp)
                    .align(Alignment.CenterVertically),
            )
            Text(training.name,  modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
                .align(Alignment.CenterVertically)
                .clickable { onClick() },)
            IconButton(onClick = onPlay, modifier = Modifier.align(Alignment.CenterVertically) ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = stringResource(id = R.string.ic_description_play_icon))
            }
        }
    }
}

// PREVIEWS
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun TrainingListPreview() {
    IntervalTrainingTheme {
        val items: List<Training> = listOf(
            Training(name = "Training 1", defaultTimeSec = 45, defaultRestSec = 15),
            Training(name = "Training 2", defaultTimeSec = 45, defaultRestSec = 15),
        )
        LazyColumn(modifier = Modifier) {
            items(items) { training ->
                TrainingItemComponent(training, 45, Modifier.padding(2.dp), {},{})
            }
        }
    }
}

// PREVIEWS
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun InternalTrainingScreenPreview() {
    IntervalTrainingTheme {
        IntervalTrainingScreen(trainingViewModel = viewModel())
    }
}
