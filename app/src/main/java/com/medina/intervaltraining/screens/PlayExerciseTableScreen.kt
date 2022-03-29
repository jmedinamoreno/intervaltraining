package com.medina.intervaltraining.screens

import android.content.res.Configuration
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medina.intervaltraining.R
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import com.medina.intervaltraining.ui.theme.Utils
import com.medina.intervaltraining.data.viewmodel.Exercise
import com.medina.intervaltraining.data.viewmodel.ExerciseIcon
import com.medina.intervaltraining.data.viewmodel.ExerciseViewModel
import com.medina.intervaltraining.data.viewmodel.Training

@Composable
fun PlayExerciseTableScreen(
    exerciseViewModel: ExerciseViewModel,
    immediate: Boolean = false,
    onBack:()->Unit,
    onEdit:()->Unit
) {
    val items: List<Exercise> by exerciseViewModel.exercises.observeAsState(listOf())
    val training: Training by exerciseViewModel.training.observeAsState(Training("",0,0))
    PlayExerciseTableView(training = training, onBack = onBack, onEdit = onEdit, items = items)
}

@Composable
fun PlayExerciseTableView(
    training: Training,
    items: List<Exercise>,
    onBack:()->Unit = {},
    onEdit:()->Unit = {},){
    Scaffold(topBar = {
        PlayExerciseTableScreenTopBar(training = training, onBack = onBack, onEdit = onEdit)
    })
    {
        PlayExerciseTableBody(
            items = items,
            playState = PlayExerciseTableState.READY,
            currentExercise = 0,
            currentTimeSec = 0,
            onStart = {},
            onPause = {},
            onResume = {},
            onSkip = {},
            onRestart = {}
        )
    }
}

@Composable
fun PlayExerciseTableScreenTopBar(training: Training, onBack:()->Unit, onEdit:()->Unit ){
    TopAppBar(
        title = { Text(training.name) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            // RowScope here, so these icons will be placed horizontally
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit training")
            }
        }
    )
}

enum class PlayExerciseTableState{ READY,RUNNING,PAUSED,COMPLETE}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayExerciseTableBody(
    items: List<Exercise>,
    playState: PlayExerciseTableState,
    currentExercise: Int,
    currentTimeSec: Int,
    onStart:()->Unit,
    onPause:()->Unit,
    onResume:()->Unit,
    onSkip:(toIndex:Int)->Unit,
    onRestart:()->Unit,
) {
    Column() {
        when(playState){
            PlayExerciseTableState.READY -> BigPlayButton(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f), onStart = onStart)
            PlayExerciseTableState.RUNNING -> RunningExercise(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f), items[currentExercise], onPause = onPause, currentTimeSec = currentTimeSec)
            PlayExerciseTableState.PAUSED -> PausedExercise(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f), items[currentExercise], onResume = onResume, currentTimeSec = currentTimeSec)
            PlayExerciseTableState.COMPLETE -> FinishedTraining(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f), onRestart = onRestart)
        }
        val exercises = items.subList(currentExercise,items.size)
        LazyColumn {
            items(exercises) { exercise ->
                ExerciseLabel(exercise,
                    Modifier
                        .padding(2.dp)
                        .combinedClickable(
                            onLongClick = { onSkip(items.indexOf(exercise)) },
                            onClick = {},
                            enabled = true
                        ))
            }
        }
    }
}

@Composable
fun BigPlayButton(modifier: Modifier, onStart:()->Unit){
    Box(modifier = modifier.clickable { onStart() }) {
        Icon(
            imageVector = Icons.Default.PlayCircleFilled,
            contentDescription = stringResource(id = R.string.ic_description_play_icon),
            Modifier
                .size(84.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun RunningExercise(modifier: Modifier, runningExercise:Exercise, currentTimeSec: Int, onPause:()->Unit){
    val isRest = runningExercise.timeSec < currentTimeSec
    val text = if(isRest) "Rest" else runningExercise.name
    val progress = if (isRest) {
        (currentTimeSec-runningExercise.timeSec).toFloat() / runningExercise.restSec.toFloat()
    }else{
        currentTimeSec.toFloat() / runningExercise.restSec.toFloat()
    }
    val color = if(isRest) MaterialTheme.colors.primary else MaterialTheme.colors.secondary
    Box(modifier = modifier.clickable { onPause() }) {
        Text(text = text, modifier = Modifier
            .align(Alignment.TopCenter))
        CircularProgressIndicator(progress = progress, color = color,
            modifier = Modifier
                .fillMaxWidth()
                .padding(64.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun PausedExercise(modifier: Modifier, runningExercise:Exercise, currentTimeSec: Int, onResume:()->Unit){
    val isRest = runningExercise.timeSec < currentTimeSec
    val text = "PAUSED"
    val progress = if (isRest) {
        (currentTimeSec-runningExercise.timeSec).toFloat() / runningExercise.restSec.toFloat()
    }else{
        currentTimeSec.toFloat() / runningExercise.restSec.toFloat()
    }
    val color = if(isRest) MaterialTheme.colors.primary else MaterialTheme.colors.secondary
    Box(modifier = modifier.clickable { onResume() }) {
        Text(text = text, modifier = Modifier
            .align(Alignment.TopCenter))
        CircularProgressIndicator(progress = progress, color = color,
            modifier = Modifier
                .fillMaxWidth()
                .padding(64.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun FinishedTraining(modifier: Modifier, onRestart:()->Unit){
    Box(modifier = modifier.clickable { onRestart() }) {
        Icon(
            imageVector = Icons.Default.Replay,
            contentDescription = stringResource(id = R.string.ic_description_play_icon),
            Modifier
                .size(84.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun ExerciseLabel(exercise: Exercise, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, elevation = 1.dp) {
        Row (modifier = Modifier.fillMaxWidth()){
            Image(
                painter = painterResource(id = Utils.iconToDrawableResource(exercise.icon)),
                colorFilter = ColorFilter.tint(MaterialTheme.colors.secondaryVariant),
                contentDescription = exercise.name,
                modifier = Modifier
                    .padding(2.dp)
                    .align(Alignment.CenterVertically)
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colors.secondary, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = exercise.name ,
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.subtitle2.copy(
                    fontSize = 28.sp
                ),
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
            )
            IconButton(
                onClick = {  },
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Info")
            }
        }
    }
}


@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true,)
@Composable
fun RowPreview() {
    IntervalTrainingTheme {
        ExerciseLabel(Exercise(
            name = "Run",
            icon = ExerciseIcon.RUN
        ))
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true,)
@Composable
fun PlayerPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colors.background) {
            PlayExerciseTableView(training = Training("",0,0), items = emptyList())
        }
    }
}