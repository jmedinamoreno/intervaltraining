package com.medina.intervaltraining.screens

import android.content.res.Configuration
import android.widget.ProgressBar
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medina.intervaltraining.R
import com.medina.intervaltraining.preview.SampleData
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import com.medina.intervaltraining.ui.theme.Utils
import com.medina.intervaltraining.viewmodel.Exercise
import com.medina.intervaltraining.viewmodel.ExerciseIcon
import com.medina.intervaltraining.viewmodel.Training

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

@Composable
fun EditExerciseTableScreenTopBar(training: Training, onBack:()->Unit, onDelete:()->Unit ){
    TopAppBar(
        title = { Text(training.name) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            // RowScope here, so these icons will be placed horizontally
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete training")
            }
        }
    )
}

@Composable
fun ExerciseTableScreen(
    training: Training,
    immediate: Boolean = false,
    onBack:()->Unit,
    onEdit:()->Unit
) {
    val items = training.exerciseTable
    Scaffold(topBar = {
        PlayExerciseTableScreenTopBar(training = training, onBack = onBack, onEdit = onEdit)})
    {
        PlayExerciseTableScreen(
            items = items,
            playState = PlayExerciseTableState.RUNNING,
            currentExercise = 0,
            currentTimeSec = 190,
            onStart = { /*TODO*/ },
            onPause = { /*TODO*/ },
            onResume = { /*TODO*/ },
            onSkip = {},
            onRestart = {}
        )
    }
}

enum class PlayExerciseTableState{ READY,RUNNING,PAUSED,COMPLETE}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayExerciseTableScreen(
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
fun EditExerciseTableScreen(
    training: Training,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onUpdateTraining: (newTraining:Training) -> Unit
) {
    val items = training.exerciseTable
    Scaffold(topBar = {
        EditExerciseTableScreenTopBar(training = training, onBack = onBack, onDelete = {

        })
        })
    {
        Column() {
            // add TodoItemInputBackground and TodoItem at the top of TodoScreen
            TodoItemInputBackground(elevate = true, modifier = Modifier.fillMaxWidth()) {
                TodoItemInput(onItemComplete = {})
            }
            LazyColumn {
                items(items) { exercise ->
                    Row() {
                        ExerciseLabel(exercise, Modifier.padding(2.dp))
                        IconButton(onClick = { },) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit"
                            )
                        }
                        IconButton(onClick = { },) {
                            Icon(
                                imageVector = Icons.Default.ControlPointDuplicate,
                                contentDescription = "Duplicate"
                            )
                        }
                        IconButton(onClick = { },) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort"
                            )
                        }
                        IconButton(onClick = { },) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun ExerciseTable(exercises:List<Exercise>){
    LazyColumn {
        items(exercises) { exercise ->
            ExerciseLabel(exercise, Modifier.padding(2.dp))
        }
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

@Composable
fun TodoItemInput(onItemComplete: (Exercise) -> Unit) {
    val (text, setText) = remember { mutableStateOf("") }
    val (icon, setIcon) = remember { mutableStateOf(ExerciseIcon.NONE)}
    val iconsVisible = text.isNotBlank()
   Column {
       Row(
           Modifier
               .padding(horizontal = 16.dp)
               .padding(top = 16.dp)
       ) {
           TodoInputTextField(text,setText,
               Modifier
                   .weight(1f)
                   .padding(end = 8.dp)
           )
           TodoEditButton(
               onClick = {
                            onItemComplete(Exercise(text))
                            setText("") // clear the internal text
                         },
               text = "Add",
               enabled = text.isNotBlank(),
               modifier = Modifier.align(Alignment.CenterVertically)
           )
       }
       if (iconsVisible) {
           AnimatedIconRow(icon, setIcon, Modifier.padding(top = 8.dp))
       } else {
           Spacer(modifier = Modifier.height(16.dp))
       }
   }
}

@Composable
fun TodoInputTextField(text: String, onTextChange: (String) -> Unit,modifier: Modifier) {
    TodoInputText(text, onTextChange, modifier)
}

@Preview
@Composable
fun PreviewTodoItemInput() = TodoItemInput(onItemComplete = { })

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true,)
@Composable
fun EditorPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colors.background) {
            ExerciseTableScreen(SampleData.training, false, {},{})
        }
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true,)
@Composable
fun RowPreview() {
    IntervalTrainingTheme {
        ExerciseLabel(SampleData.exerciseTable[0])
    }
}
