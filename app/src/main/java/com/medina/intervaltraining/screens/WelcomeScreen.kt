package com.medina.intervaltraining.screens

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medina.intervaltraining.R
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import com.medina.intervaltraining.viewmodel.Training

@Composable
fun FirstRunScreen(onStart: () -> Unit) {
    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to the Interval Training app!")
            Button(
                modifier = Modifier.padding(vertical = 24.dp),
                onClick = onStart
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
fun IntervalTrainingScreen(){
    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "New")
        }}
        ) {
        Column() {
            TrainedHoursComponent(hours = 1.5f,
                modifier = Modifier.fillMaxWidth().padding(all=16.dp))
            TrainingListComponent(trainingList = listOf(Training(45,"Mi tabla de ejercicios 1")),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all=16.dp)
                    .weight(1f))
        }
    }
}

@Composable
fun TrainedHoursComponent(hours:Float, modifier: Modifier){
    Column(modifier = modifier) {
        Text(stringResource(id = R.string.welcome_trained_time_first_line),
            style = MaterialTheme.typography.h3,
            modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(stringResource(id = R.string.welcome_trained_time_second_line,hours),
            style = MaterialTheme.typography.h1,
            modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(stringResource(id = R.string.welcome_trained_time_third_line),
            style = MaterialTheme.typography.h3,
            modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun TrainingListComponent(trainingList:List<Training>,modifier: Modifier){
    LazyColumn(modifier = modifier) {
        items(trainingList) { training ->
            TrainingItemComponent(training, Modifier.padding(2.dp), {},{})
        }
    }
}

@Composable
fun TrainingItemComponent(training: Training, modifier: Modifier, onClick:()->Unit, onPlay:()->Unit){
    Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, elevation = 1.dp) {
        Row (modifier = Modifier.fillMaxWidth()){
            Text(stringResource(id = R.string.welcome_training_min,training.timeMin),
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.button,
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
                Icon(imageVector = Icons.Default.PlayCircle, contentDescription = stringResource(id = R.string.ic_description_play_icon))
            }
        }
    }
}

// PREVIEWS
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true,)
@Composable
fun TrainingListPreview() {
    IntervalTrainingTheme {
        TrainingListComponent(modifier = Modifier,
            trainingList = listOf(
                Training(45,"Mi tabla de ejercicios 1"),
                Training(25,"Mi tabla de ejercicios 2"),
                Training(95,"Mi tabla de ejercicios 3"),
            ))
    }
}

// PREVIEWS
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true,)
@Composable
fun InternalTrainingScreenPreview() {
    IntervalTrainingTheme {
        IntervalTrainingScreen()
    }
}
