package com.medina.intervaltraining.screens

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medina.intervaltraining.R
import com.medina.intervaltraining.data.model.Session
import com.medina.intervaltraining.data.model.Training
import com.medina.intervaltraining.data.repository.UserDataDummyRepository
import com.medina.intervaltraining.data.viewmodel.Exercise
import com.medina.intervaltraining.data.viewmodel.ExerciseIcon
import com.medina.intervaltraining.data.viewmodel.ExerciseViewModel
import com.medina.intervaltraining.data.viewmodel.SettingsViewModel
import com.medina.intervaltraining.data.viewmodel.getTrainingStartDelaySecs
import com.medina.intervaltraining.data.viewmodel.isSoundsEnabledTrainingStart
import com.medina.intervaltraining.ui.components.ExerciseRunningLabel
import com.medina.intervaltraining.ui.components.FXSoundPool
import com.medina.intervaltraining.ui.stringChosen
import com.medina.intervaltraining.ui.stringForIconDescription
import com.medina.intervaltraining.ui.stringRandom
import com.medina.intervaltraining.ui.stringRandomSelected
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun PlayExerciseTableScreen(
    exerciseViewModel: ExerciseViewModel,
    immediate: Boolean = false,
    onBack:()->Unit,
    onEdit:()->Unit,
    updateSession: (session: Session) -> Unit = { _->},
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
    onEdit:()->Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel(),){

    val context = LocalContext.current
    val settingsState by settingsViewModel.settingsUiState.collectAsStateWithLifecycle()
    val startDelay = settingsState.getTrainingStartDelaySecs()
    var soundPool by remember { mutableStateOf<FXSoundPool?>(null) }
    // Initialize SoundPool & dispose it when the composable leaves the composition
    LaunchedEffect(key1 = context) { soundPool = FXSoundPool(context).build() }
    DisposableEffect(Unit) {onDispose {soundPool?.release();soundPool = null}}

    // create variable for value
    var currentExerciseIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    // create variable for current time
    var currentTimeMillis by rememberSaveable {
        mutableIntStateOf(0)
    }
    // create variable for isTimerRunning
    var playState by rememberSaveable {
        mutableStateOf(startState)
    }

    // create session to track
    val currentSessionItem by remember { mutableStateOf(session) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = playState) {
        scope.launch {
            if (playState == PlayExerciseTableState.RUNNING){
                var startTime = Calendar.getInstance().timeInMillis - currentTimeMillis
                if(settingsState.isSoundsEnabledTrainingStart()) {
                    soundPool?.playSound(FXSoundPool.FX.PUIN)
                }
                do {
                    delay(25L)
                    currentTimeMillis = (Calendar.getInstance().timeInMillis - startTime).toInt()
                    val currentTimeSec = currentTimeMillis / 1000
                    val exercise = items.getOrNull(currentExerciseIndex)
                    if (currentTimeSec == exercise?.let { it.timeSec + it.restSec }) {
                        soundPool?.playSound(FXSoundPool.FX.TIN)
                        currentTimeMillis = 0
                        currentExerciseIndex += 1
                        startTime = Calendar.getInstance().timeInMillis
                        currentSessionItem.dateTimeEnd = Calendar.getInstance().timeInMillis
                        updateSession(currentSessionItem)
                    }
                    if (currentExerciseIndex >= items.size) {
                        soundPool?.playSound(FXSoundPool.FX.TINTINONIN)
                        playState = PlayExerciseTableState.COMPLETE
                        currentSessionItem.complete = true
                        updateSession(currentSessionItem)
                    }
                } while (playState == PlayExerciseTableState.RUNNING)
            }

            if(playState == PlayExerciseTableState.STARTING){
                val startTime = Calendar.getInstance().timeInMillis - currentTimeMillis
                if(settingsState.isSoundsEnabledTrainingStart()) {
                    soundPool?.playSound(FXSoundPool.FX.PUIN)
                }
                do {
                    delay(25L)
                    currentTimeMillis = (Calendar.getInstance().timeInMillis - startTime).toInt()
                    val currentTimeSec = currentTimeMillis / 1000
                    if(currentTimeSec>= startDelay){
                        playState = PlayExerciseTableState.RUNNING
                    }
                } while (playState == PlayExerciseTableState.STARTING)
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
            startDelay = startDelay,
            currentExercise = currentExerciseIndex,
            currentTimeMillis = currentTimeMillis,
            onStart = {
                playState = PlayExerciseTableState.STARTING
                currentTimeMillis = 0
                currentSessionItem.dateTimeStart = Calendar.getInstance().timeInMillis
                currentSessionItem.dateTimeEnd = Calendar.getInstance().timeInMillis
                updateSession(currentSessionItem)
            },
            onPause = {playState = PlayExerciseTableState.PAUSED},
            onResume = {playState = PlayExerciseTableState.RUNNING},
            onSkip = { toIndex -> currentExerciseIndex = toIndex},
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
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.app_action_back)
                )
            }
        },
        actions = {
            // RowScope here, so these icons will be placed horizontally
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(id = R.string.play_exercise_edit_training)
                )
            }
        }
    )
}

enum class PlayExerciseTableState{ READY,STARTING,RUNNING,PAUSED,COMPLETE}
@Composable
fun PlayExerciseTableBody(
    modifier: Modifier,
    items: List<Exercise>,
    playState: PlayExerciseTableState,
    startDelay: Int,
    currentExercise: Int,
    currentTimeMillis: Int,
    onStart:()->Unit,
    onPause:()->Unit,
    onResume:()->Unit,
    onSkip:(toIndex:Int)->Unit,
    onRestart:()->Unit,
) {
    val orientation = LocalConfiguration.current.orientation
    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        PlayExerciseTableBodyHorizontal(
            modifier = modifier,
            items = items,
            playState = playState,
            currentExercise = currentExercise,
            currentTimeMillis = currentTimeMillis,
            startDelay = startDelay,
            onStart = onStart,
            onPause = onPause,
            onResume = onResume,
            onSkip = onSkip,
            onRestart = onRestart
        )
    } else {
        PlayExerciseTableBodyVertical(
            modifier = modifier,
            items = items,
            playState = playState,
            currentExercise = currentExercise,
            currentTimeMillis = currentTimeMillis,
            startDelay = startDelay,
            onStart = onStart,
            onPause = onPause,
            onResume = onResume,
            onSkip = onSkip,
            onRestart = onRestart
        )
    }

}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayExerciseTableBodyVertical(
    modifier: Modifier,
    items: List<Exercise>,
    playState: PlayExerciseTableState,
    startDelay: Int,
    currentExercise: Int,
    currentTimeMillis: Int,
    onStart:()->Unit,
    onPause:()->Unit,
    onResume:()->Unit,
    onSkip:(toIndex:Int)->Unit,
    onRestart:()->Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        stickyHeader {
            ExercisePlayer(
                modifier = Modifier.aspectRatio(4f/3f),
                playState = playState,
                runningExercise = items.getOrNull(currentExercise),
                currentTimeMillis = currentTimeMillis,
                startDelay = startDelay,
                onStart = onStart,
                onPause = onPause,
                onResume = onResume,
                onRestart = onRestart
            )
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayExerciseTableBodyHorizontal(
    modifier: Modifier,
    items: List<Exercise>,
    playState: PlayExerciseTableState,
    startDelay: Int,
    currentExercise: Int,
    currentTimeMillis: Int,
    onStart:()->Unit,
    onPause:()->Unit,
    onResume:()->Unit,
    onSkip:(toIndex:Int)->Unit,
    onRestart:()->Unit,
) {
    Row (modifier = modifier.fillMaxWidth()){
        LazyColumn(modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
        ) {
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
        ExercisePlayer(
            modifier = Modifier.aspectRatio(1f),
            playState = playState,
            runningExercise = items.getOrNull(currentExercise),
            currentTimeMillis = currentTimeMillis,
            startDelay = startDelay,
            onStart = onStart,
            onPause = onPause,
            onResume = onResume,
            onRestart = onRestart
        )
    }
}

@Composable
fun ExercisePlayer(
    modifier: Modifier,
    playState: PlayExerciseTableState,
    startDelay: Int,
    runningExercise: Exercise?,
    currentTimeMillis: Int,
    onStart:()->Unit,
    onPause:()->Unit,
    onResume:()->Unit,
    onRestart: () -> Unit,){
    Surface(modifier = modifier.then(
        Modifier.background(
            MaterialTheme.colorScheme.background
        )
    )) {
        when (playState) {
            PlayExerciseTableState.READY -> BigPlayButton(
                Modifier.fillMaxSize(),
                onStart = onStart
            )
            PlayExerciseTableState.STARTING ->  StartingRoutine(
                Modifier.fillMaxSize(),
                startDelay = startDelay,
                currentTimeMillis = currentTimeMillis,
                onPause = onPause
            )

            PlayExerciseTableState.RUNNING -> RunningExercise(
                Modifier.fillMaxSize(),
                runningExercise,
                onPause = onPause,
                currentTimeMillis = currentTimeMillis
            )

            PlayExerciseTableState.PAUSED -> PausedExercise(
                Modifier.fillMaxSize(),
                runningExercise,
                onResume = onResume,
                currentTimeSec = currentTimeMillis / 1000
            )

            PlayExerciseTableState.COMPLETE -> FinishedTraining(
                Modifier.fillMaxSize(),
                onRestart = onRestart
            )
        }
    }
}

@Composable
fun StartingRoutine(
    modifier: Modifier,
    startDelay: Int,
    currentTimeMillis: Int,
    onPause: () -> Unit
) {
    val text = stringRandomSelected(id = R.array.running_exercise_rest_list)
    val currentTimeSec = currentTimeMillis/1000
    val timeText = "${startDelay - currentTimeSec}"
    val backgroundColor =  MaterialTheme.colorScheme.secondary
    val color = MaterialTheme.colorScheme.primary

    BoxWithConstraints(modifier = modifier.clickable { onPause() }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val padding = 8.dp.toPx()
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 3
            val dotRadius = size.minDimension / ((startDelay*2)+1)

            for (i in 0 until startDelay) {
                val angle = ((2 * Math.PI / startDelay) * i ) - (Math.PI / 2)
                val x = center.x + radius * cos(angle).toFloat()
                val y = center.y + radius * sin(angle).toFloat()

                drawCircle(
                    color = if(currentTimeSec <= i) color else backgroundColor,
                    radius = dotRadius,
                    center = Offset(x, y),
                    style = Fill // Optional: Add a stroke for better visibility
                )
            }
        }
        PlayItemTexts(
            modifier = Modifier.align(Alignment.Center),
            timeText = timeText,
            labelText = text,
            maxHeight = maxHeight.value,
            maxWidth = maxWidth.value
        )
    }
}

@Composable
fun BigPlayButton(modifier: Modifier, onStart:()->Unit){
    Box(modifier = modifier.clickable { onStart() }) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = stringForIconDescription(id = R.string.running_training_play),
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
    val text = if(isRest)
        stringChosen(id = R.array.running_exercise_rest_list,runningExercise.name.hashCode())
    else
        runningExercise.name

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
        PlayItemTexts(
            modifier = Modifier.align(Alignment.Center),
            timeText = timeText,
            labelText = text,
            maxHeight = maxHeight.value,
            maxWidth = maxWidth.value
        )
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
        PlayItemTexts(
            modifier = Modifier.align(Alignment.Center),
            timeText = timeText,
            labelText = text,
            maxHeight = maxHeight.value,
            maxWidth = maxWidth.value
        )
    }
}

@Composable
private fun PlayItemTexts(
    modifier: Modifier,
    timeText:String,
    labelText:String,
    maxHeight:Float,
    maxWidth:Float
){
    Text(text = timeText, style = TextStyle(
        fontSize = (min(maxHeight,maxWidth)*0.7f).toInt().sp,
        color = Color(1f,1f,1f,0.7f),
        shadow = Shadow(
            color =  Color(0f,0f,0f,0.7f),
            blurRadius = 5f
        )
    ), modifier = modifier)
    Text(text = labelText, style = TextStyle(
        fontSize = (min(maxHeight,maxWidth)*0.3f).toInt().sp,
        color = Color.White,
        shadow = Shadow(
            color = Color.Black,
            blurRadius = 5f
        )
    ), modifier = modifier)
}

@Composable
fun FinishedTraining(modifier: Modifier, onRestart:()->Unit){
    Box(modifier = modifier.clickable { onRestart() }) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = stringForIconDescription(id = R.string.running_training_restart),
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
fun StartingPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StartingRoutine(
                Modifier
                    .width(200.dp)
                    .height(150.dp),
                5,
                currentTimeMillis = 2200,
                onPause = {}
            )
        }
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
                settingsViewModel = SettingsViewModel(userDataRepository = UserDataDummyRepository()),
                training = Training("test",30,5),
                session = Session(UUID.randomUUID()),
                items = (1 until 15).toList().map{Exercise("Exercise $it")})
        }
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode", widthDp = 720, heightDp = 360, uiMode = Configuration.ORIENTATION_LANDSCAPE)
@Composable
fun PlaybackHorizontalPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlayExerciseTableView(
                settingsViewModel = SettingsViewModel(userDataRepository = UserDataDummyRepository()),
                training = Training("test",30,5),
                session = Session(UUID.randomUUID()),
                items = (1 until 15).toList().map{Exercise("Exercise $it")})
        }
    }
}