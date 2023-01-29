package com.medina.intervaltraining.screens

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medina.intervaltraining.R
import com.medina.intervaltraining.data.room.SessionItem
import com.medina.intervaltraining.data.viewmodel.*
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import com.medina.intervaltraining.ui.components.ContentAwareLazyColumn
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun PlayExerciseTableScreen(
    exerciseViewModel: ExerciseViewModel,
    immediate: Boolean = false,
    onBack:()->Unit,
    onEdit:()->Unit,
    updateSession: (session:Session) -> Unit = {_->},
) {
    val items: List<Exercise> by exerciseViewModel.exercises.observeAsState(listOf())
    val training: Training? by exerciseViewModel.training.observeAsState()
    val session: Session = exerciseViewModel.session
    PlayExerciseTableView(
        training = training,
        session = session,
        onBack = onBack,
        onEdit = onEdit,
        items = items,
        updateSession = updateSession
    )
}

@Composable
fun PlayExerciseTableView(
    training: Training?,
    session: Session,
    items: List<Exercise>,
    startState:PlayExerciseTableState = PlayExerciseTableState.READY,
    updateSession: (session:Session) -> Unit = {_->},
    onBack:()->Unit = {},
    onEdit:()->Unit = {},){
    // create variable for value
    var currentExercise by remember {
        mutableStateOf(0)
    }

    // create session to track
    val currentSessionItem by remember { mutableStateOf(session) }

    // create variable for current time
    var currentTimeMilis by remember {
        mutableStateOf(0)
    }
    // create variable for isTimerRunning
    var playState by remember {
        mutableStateOf(startState)
    }

    LaunchedEffect(key1 = currentTimeMilis, key2 = playState) {
        if(playState == PlayExerciseTableState.RUNNING) {
            delay(25L)
            currentTimeMilis += 25
            val currentTimeSec = currentTimeMilis/1000
            if(currentTimeSec == items[currentExercise].timeSec + items[currentExercise].restSec){
                currentTimeMilis = 0
                currentExercise += 1
                currentSessionItem.dateTimeEnd = Calendar.getInstance().timeInMillis
                updateSession(currentSessionItem)
            }
            if(currentExercise>=items.size){
                currentExercise = items.size-1
                playState = PlayExerciseTableState.COMPLETE
                currentSessionItem.complete = true
                updateSession(currentSessionItem)
            }
        }
    }
    Scaffold(topBar = {
        PlayExerciseTableScreenTopBar(training = training, onBack = onBack, onEdit = onEdit)
    })
    {
        PlayExerciseTableBody(
            items = items,
            playState = playState,
            currentExercise = currentExercise,
            currentTimeMilis = currentTimeMilis,
            onStart = {
                playState = PlayExerciseTableState.RUNNING
                currentTimeMilis = 0
                currentSessionItem.dateTimeStart = Calendar.getInstance().timeInMillis
                currentSessionItem.dateTimeEnd = Calendar.getInstance().timeInMillis
                updateSession(currentSessionItem)
            },
            onPause = {playState = PlayExerciseTableState.PAUSED},
            onResume = {playState = PlayExerciseTableState.RUNNING},
            onSkip = { toIndex -> currentExercise = toIndex},
            onRestart = {playState = PlayExerciseTableState.READY}
        )
    }
}

@Composable
fun PlayExerciseTableScreenTopBar(training: Training?, onBack:()->Unit, onEdit:()->Unit ){
    TopAppBar(
        title = { Text(training?.name?:"") },
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
    currentTimeMilis: Int,
    onStart:()->Unit,
    onPause:()->Unit,
    onResume:()->Unit,
    onSkip:(toIndex:Int)->Unit,
    onRestart:()->Unit,
) {
    val constPlayerSize = 0.6f
    Column() {
        when(playState){
            PlayExerciseTableState.READY -> BigPlayButton(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(constPlayerSize), onStart = onStart)
            PlayExerciseTableState.RUNNING -> RunningExercise(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(constPlayerSize), items[currentExercise], onPause = onPause, currentTimeMilis = currentTimeMilis)
            PlayExerciseTableState.PAUSED -> PausedExercise(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(constPlayerSize), items[currentExercise], onResume = onResume, currentTimeSec = currentTimeMilis/1000)
            PlayExerciseTableState.COMPLETE -> FinishedTraining(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(constPlayerSize), onRestart = onRestart)
        }
        ContentAwareLazyColumn(lazyColumnBody = {
            items(items) { exercise ->
                ExerciseRunningLabel(
                    exercise = exercise,
                    currentTimeMilis = when{
                        items.indexOf(exercise) > currentExercise -> 0
                        items.indexOf(exercise) < currentExercise -> -1
                        else -> currentTimeMilis
                    },
                    modifier = Modifier
                        .padding(2.dp)
                        .combinedClickable(
                            onLongClick = { onSkip(items.indexOf(exercise)) },
                            onClick = {},
                            enabled = true
                        ))
            }
        })
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
fun RunningExercise(modifier: Modifier, runningExercise:Exercise, currentTimeMilis: Int, onPause:()->Unit){
    val runTimeMilis = runningExercise.timeSec * 1000
    val restTimeMilis = runningExercise.restSec * 1000
    val totalTimeMilis = runTimeMilis + restTimeMilis
    val isRest = runTimeMilis < currentTimeMilis
    val text = if(isRest) "#Rest" else runningExercise.name
    val timeText = if(isRest)
        "${(runningExercise.restSec + runningExercise.timeSec) - currentTimeMilis/1000}"
    else
        "${runningExercise.timeSec - currentTimeMilis/1000}"

    val localProgress: Float by animateFloatAsState( if (isRest) {
        1 - ((currentTimeMilis-runTimeMilis).toFloat() / restTimeMilis.toFloat())
    }else{
        currentTimeMilis.toFloat() / runTimeMilis.toFloat()
    })

    val totalProgress: Float by animateFloatAsState(
        currentTimeMilis.toFloat() / totalTimeMilis.toFloat()
       )

    val backgroundColor = if(isRest) MaterialTheme.colors.secondaryVariant else MaterialTheme.colors.secondary
    val color = if(isRest) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.primary

    BoxWithConstraints(modifier = modifier.clickable { onPause() }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val padding = 8.dp.toPx()
            inset(left = padding, right = padding, top = padding, bottom = padding) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val center = Offset(x = canvasWidth / 2, y = canvasHeight / 2)
                val radius = canvasHeight / 2f
                clipRect(0F, localProgress * canvasHeight, canvasWidth, canvasHeight) {
                    drawCircle(
                        color = backgroundColor,
                        center = center,
                        radius = radius,
                    )
                }
                inset(
                    left = (canvasWidth / 2) - radius,
                    right = (canvasWidth / 2) - radius,
                    top = 0f,
                    bottom = 0f
                ) {
                    drawArc(
                        color = color,
                        useCenter = false,
                        startAngle = 270f,
                        sweepAngle = totalProgress * 360f,
                        style = Stroke(5.dp.toPx()),
                    )
                }
            }
        }
        Text(text = timeText, style = TextStyle(
            fontSize = (maxHeight.value*0.7f).toInt().sp,
            color = Color(1f,1f,1f,0.7f),
            shadow = Shadow(
                color =  Color(0f,0f,0f,0.7f),
                blurRadius = 5f
            )
        ), modifier = Modifier.align(Alignment.Center))
        Text(text = text, style = TextStyle(
            fontSize = (maxHeight.value*0.3f).toInt().sp,
            color = Color.White,
            shadow = Shadow(
                color = Color.Black,
                blurRadius = 5f
            )
        ), modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun PausedExercise(modifier: Modifier, runningExercise:Exercise, currentTimeSec: Int, onResume:()->Unit){
    val isRest = runningExercise.timeSec < currentTimeSec
    val text = "PAUSED"
    val timeText = if(isRest)
        "${(runningExercise.restSec + runningExercise.timeSec) - currentTimeSec}"
    else
        "${runningExercise.timeSec - currentTimeSec}"

    val localProgress: Float = if (isRest) {
        (currentTimeSec-runningExercise.timeSec).toFloat() / runningExercise.restSec.toFloat()
    }else{
        currentTimeSec.toFloat() / runningExercise.timeSec.toFloat()
    }

    val totalProgress: Float = currentTimeSec.toFloat() / (runningExercise.restSec + runningExercise.timeSec).toFloat()
    val color = if(isRest) MaterialTheme.colors.primary else MaterialTheme.colors.secondary
    val backgroundColor = if(isRest) MaterialTheme.colors.secondaryVariant else MaterialTheme.colors.secondary

    BoxWithConstraints(modifier = modifier.clickable { onResume() }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val padding = 8.dp.toPx()
            inset(left = padding, right = padding, top = padding, bottom = padding) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val center = Offset(x = canvasWidth / 2, y = canvasHeight / 2)
                val radius = canvasHeight / 2f
                clipRect(0F, localProgress * canvasHeight, canvasWidth, canvasHeight) {
                    drawCircle(
                        color = backgroundColor,
                        center = center,
                        radius = radius,
                    )
                }
                inset(
                    left = (canvasWidth / 2) - radius,
                    right = (canvasWidth / 2) - radius,
                    top = 0f,
                    bottom = 0f
                ) {
                    drawArc(
                        color = color,
                        useCenter = false,
                        startAngle = 270f,
                        sweepAngle = totalProgress * 360f,
                        style = Stroke(5.dp.toPx()),
                    )
                }
            }
        }
        Text(text = timeText, style = TextStyle(
            fontSize = (maxHeight.value*0.7f).toInt().sp,
            color = Color(1f,1f,1f,0.7f),
            shadow = Shadow(
                color =  Color(0f,0f,0f,0.7f),
                blurRadius = 5f
            )
        ), modifier = Modifier.align(Alignment.Center))
        Text(text = text, style = TextStyle(
            fontSize = (maxHeight.value*0.3f).toInt().sp,
            color = Color.White,
            shadow = Shadow(
                color = Color.Black,
                blurRadius = 5f
            )
        ), modifier = Modifier.align(Alignment.Center))
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

@ExperimentalFoundationApi
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true,)
@Composable
fun PlaybackPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colors.background) {
            RunningExercise(
                Modifier
                    .width(300.dp)
                    .height(100.dp),
                Exercise("Test",ExerciseIcon.JUMP, restSec = 12, timeSec = 45),
                currentTimeMilis = 5000,
                onPause = {}
            )
        }
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true,)
@Composable
fun PlayerPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colors.background) {
            PlayExerciseTableView(
                training = Training("test",30,5),
                session = Session(UUID.randomUUID()),
                items = emptyList())
        }
    }
}