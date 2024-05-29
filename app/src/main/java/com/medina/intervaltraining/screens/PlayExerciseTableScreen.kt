package com.medina.intervaltraining.screens

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.medina.intervaltraining.data.viewmodel.Exercise
import com.medina.intervaltraining.data.viewmodel.ExerciseIcon
import com.medina.intervaltraining.data.viewmodel.ExerciseViewModel
import com.medina.intervaltraining.data.viewmodel.Session
import com.medina.intervaltraining.data.viewmodel.Training
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.UUID
import kotlin.math.min

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
        mutableIntStateOf(0)
    }

    // create session to track
    val currentSessionItem by remember { mutableStateOf(session) }

    // create variable for current time
    var currentTimeMillis by remember {
        mutableIntStateOf(0)
    }
    // create variable for isTimerRunning
    var playState by remember {
        mutableStateOf(startState)
    }

    LaunchedEffect(key1 = currentTimeMillis, key2 = playState) {
        if(playState == PlayExerciseTableState.RUNNING) {
            delay(25L)
            currentTimeMillis += 25
            val currentTimeSec = currentTimeMillis/1000
            if(currentTimeSec == items[currentExercise].timeSec + items[currentExercise].restSec){
                currentTimeMillis = 0
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
    { contentPadding ->
        PlayExerciseTableBody(
            modifier = Modifier.padding(contentPadding),
            items = items,
            playState = playState,
            currentExercise = currentExercise,
            currentTimeMillis = currentTimeMillis,
            onStart = {
                playState = PlayExerciseTableState.RUNNING
                currentTimeMillis = 0
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayExerciseTableScreenTopBar(training: Training?, onBack:()->Unit, onEdit:()->Unit ){
    TopAppBar(
        title = { Text(training?.name?:"") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
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
    modifier: Modifier,
    items: List<Exercise>,
    playState: PlayExerciseTableState,
    currentExercise: Int,
    currentTimeMillis: Int,
    onStart:()->Unit,
    onPause:()->Unit,
    onResume:()->Unit,
    onSkip:(toIndex:Int)->Unit,
    onRestart:()->Unit,
) {
    val constPlayerSize = 0.3f
    LazyColumn(modifier = modifier) {
        stickyHeader {
            when(playState){
                PlayExerciseTableState.READY -> BigPlayButton(
                    Modifier.fillParentMaxHeight(constPlayerSize).fillMaxWidth(), onStart = onStart)
                PlayExerciseTableState.RUNNING -> RunningExercise(
                    Modifier.fillParentMaxHeight(constPlayerSize).fillMaxWidth(), items.getOrNull(currentExercise), onPause = onPause, currentTimeMillis = currentTimeMillis)
                PlayExerciseTableState.PAUSED -> PausedExercise(
                    Modifier.fillParentMaxHeight(constPlayerSize).fillMaxWidth(), items.getOrNull(currentExercise), onResume = onResume, currentTimeSec = currentTimeMillis/1000)
                PlayExerciseTableState.COMPLETE -> FinishedTraining(
                    Modifier.fillParentMaxHeight(constPlayerSize).fillMaxWidth(), onRestart = onRestart)
            }
        }
        items(items) { exercise ->
            ExerciseRunningLabel(
                exercise = exercise,
                currentTimeMillis = when{
                    items.indexOf(exercise) > currentExercise -> 0
                    items.indexOf(exercise) < currentExercise -> -1
                    else -> currentTimeMillis
                },
                modifier = Modifier
                    .padding(2.dp)
                    .combinedClickable(
                        onLongClick = { onSkip(items.indexOf(exercise)) },
                        onClick = {},
                        enabled = true
                    ))
        }
    }
}

@Composable
fun BigPlayButton(modifier: Modifier, onStart:()->Unit){
    Box(modifier = modifier.clickable { onStart() }) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = stringResource(id = R.string.ic_description_play_icon),
            Modifier
                .size(84.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun RunningExercise(modifier: Modifier, runningExercise:Exercise?, currentTimeMillis: Int, onPause:()->Unit){
    if(runningExercise == null) return
    val runTimeMillis = runningExercise.timeSec * 1000
    val restTimeMillis = runningExercise.restSec * 1000
    val totalTimeMillis = runTimeMillis + restTimeMillis
    val isRest = runTimeMillis < currentTimeMillis
    val text = if(isRest) "#Rest" else runningExercise.name
    val timeText = if(isRest)
        "${(runningExercise.restSec + runningExercise.timeSec) - currentTimeMillis/1000}"
    else
        "${runningExercise.timeSec - currentTimeMillis/1000}"

    val localProgress: Float by animateFloatAsState( targetValue = if (isRest) {
        1 - ((currentTimeMillis-runTimeMillis).toFloat() / restTimeMillis.toFloat())
    }else{
        currentTimeMillis.toFloat() / runTimeMillis.toFloat()
    }, label = "localProgress"
    )

    val totalProgress: Float by animateFloatAsState(
        targetValue = currentTimeMillis.toFloat() / totalTimeMillis.toFloat(), label = "localProgress"
       )

    val backgroundColor = if(isRest) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.secondary
    val color = if(isRest) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary

    BoxWithConstraints(modifier = modifier.clickable { onPause() }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val padding = 8.dp.toPx()
            inset(left = padding, right = padding, top = padding, bottom = padding) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val size = min(canvasHeight,canvasWidth)
                val center = Offset(x = canvasWidth / 2, y = canvasHeight / 2)
                val radius = size / 2f
                clipRect(0F, localProgress * canvasHeight, canvasWidth, canvasHeight) {
                    drawCircle(
                        color = backgroundColor,
                        center = center,
                        radius = radius,
                    )
                }
                inset(
                    left =  (canvasWidth-size)/2,
                    right =  (canvasWidth-size)/2,
                    top = (canvasHeight-size)/2,
                    bottom = (canvasHeight-size)/2,
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
            fontSize = (min(maxHeight.value,maxWidth.value)*0.7f).toInt().sp,
            color = Color(1f,1f,1f,0.7f),
            shadow = Shadow(
                color =  Color(0f,0f,0f,0.7f),
                blurRadius = 5f
            )
        ), modifier = Modifier.align(Alignment.Center))
        Text(text = text, style = TextStyle(
            fontSize = (min(maxHeight.value,maxWidth.value)*0.3f).toInt().sp,
            color = Color.White,
            shadow = Shadow(
                color = Color.Black,
                blurRadius = 5f
            )
        ), modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun PausedExercise(modifier: Modifier, runningExercise:Exercise?, currentTimeSec: Int, onResume:()->Unit){
    if(runningExercise == null) return
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
    val color = if(isRest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val backgroundColor = if(isRest) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.secondary

    BoxWithConstraints(modifier = modifier.clickable { onResume() }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val padding = 8.dp.toPx()
            inset(left = padding, right = padding, top = padding, bottom = padding) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val size = min(canvasHeight,canvasWidth)
                val center = Offset(x = canvasWidth / 2, y = canvasHeight / 2)
                val radius = size / 2f
                clipRect(0F, localProgress * canvasHeight, canvasWidth, canvasHeight) {
                    drawCircle(
                        color = backgroundColor,
                        center = center,
                        radius = radius,
                    )
                }
                inset(
                    left =  (canvasWidth-size)/2,
                    right =  (canvasWidth-size)/2,
                    top = (canvasHeight-size)/2,
                    bottom = (canvasHeight-size)/2,
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
            fontSize = (min(maxHeight.value,maxWidth.value)*0.7f).toInt().sp,
            color = Color(1f,1f,1f,0.7f),
            shadow = Shadow(
                color =  Color(0f,0f,0f,0.7f),
                blurRadius = 5f
            )
        ), modifier = Modifier.align(Alignment.Center))
        Text(text = text, style = TextStyle(
            fontSize = (min(maxHeight.value,maxWidth.value)*0.3f).toInt().sp,
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
            imageVector = Icons.Default.Refresh,
            contentDescription = stringResource(id = R.string.ic_description_play_icon),
            Modifier
                .size(84.dp)
                .align(Alignment.Center)
        )
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PlaybackPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RunningExercise(
                Modifier
                    .width(200.dp)
                    .height(150.dp),
                Exercise("Test",ExerciseIcon.JUMP, restSec = 12, timeSec = 45),
                currentTimeMillis = 18000,
                onPause = {}
            )
        }
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PlayerPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlayExerciseTableView(
                training = Training("test",30,5),
                session = Session(UUID.randomUUID()),
                items = emptyList())
        }
    }
}