package com.medina.intervaltraining.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medina.intervaltraining.R
import com.medina.intervaltraining.data.model.Session
import com.medina.intervaltraining.data.model.Training
import com.medina.intervaltraining.data.repository.UserDataDummyRepository
import com.medina.intervaltraining.data.model.Exercise
import com.medina.intervaltraining.data.viewmodel.ExerciseViewModel
import com.medina.intervaltraining.data.viewmodel.SettingsViewModel
import com.medina.intervaltraining.data.viewmodel.getCountDownSecs
import com.medina.intervaltraining.data.viewmodel.getTrainingStartDelaySecs
import com.medina.intervaltraining.ui.components.ExercisePlayer
import com.medina.intervaltraining.ui.components.ExerciseRunningLabel
import com.medina.intervaltraining.ui.components.FXSoundPool
import com.medina.intervaltraining.ui.components.playCountdownSound
import com.medina.intervaltraining.ui.components.playExerciseStartSound
import com.medina.intervaltraining.ui.components.playRestStartSound
import com.medina.intervaltraining.ui.components.playTrainingEndSound
import com.medina.intervaltraining.ui.components.playTrainingStartSound
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

@Composable
fun PlayTrainingScreen(
    trainingId : UUID,
    exerciseViewModel: ExerciseViewModel = hiltViewModel<ExerciseViewModel, ExerciseViewModel.ExerciseViewModelFactory> { factory ->
        factory.create(trainingId)
    },
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
    startState: PlayExerciseTableState = PlayExerciseTableState.READY,
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
                soundPool?.playExerciseStartSound(settingsState)
                var currentTimeSec = 0
                do {
                    delay(25L)
                    currentTimeMillis = (Calendar.getInstance().timeInMillis - startTime).toInt()
                    val newTimeSec = currentTimeMillis / 1000
                    if(currentTimeSec != newTimeSec){
                        currentTimeSec = newTimeSec
                        val exercise = items.getOrNull(currentExerciseIndex) ?: return@launch
                        if (currentTimeSec == exercise.let { it.timeSec + it.restSec }) {
                            currentTimeMillis = 0
                            currentExerciseIndex += 1
                            if (currentExerciseIndex < items.size) {
                                soundPool?.playExerciseStartSound(settingsState)
                                startTime = Calendar.getInstance().timeInMillis
                                currentSessionItem.dateTimeEnd = Calendar.getInstance().timeInMillis
                            } else {
                                soundPool?.playTrainingEndSound(settingsState)
                                playState = PlayExerciseTableState.COMPLETE
                                currentSessionItem.complete = true
                            }
                            updateSession(currentSessionItem)
                        }else if(currentTimeSec == exercise.timeSec){
                            soundPool?.playRestStartSound(settingsState)
                        }else{
                            val remainingTime = if(currentTimeSec in 0 until exercise.timeSec) {
                                exercise.timeSec - currentTimeSec
                            }else{
                                exercise.restSec - (currentTimeSec - exercise.timeSec)
                            }
                            if(remainingTime <= settingsState.getCountDownSecs()){
                                soundPool?.playCountdownSound(settingsState)
                            }
                        }
                    }
                } while (playState == PlayExerciseTableState.RUNNING)
            }

            if(playState == PlayExerciseTableState.STARTING){
                val startTime = Calendar.getInstance().timeInMillis - currentTimeMillis
                soundPool?.playTrainingStartSound(settingsState)
                var currentTimeSec = 0
                do {
                    delay(25L)
                    currentTimeMillis = (Calendar.getInstance().timeInMillis - startTime).toInt()
                    val newTimeSec = currentTimeMillis / 1000
                    if(currentTimeSec != newTimeSec) {
                        currentTimeSec = newTimeSec
                        if (currentTimeSec >= startDelay) {
                            playState = PlayExerciseTableState.RUNNING
                        }else{
                            val remainingTime = startDelay - currentTimeSec
                            if(remainingTime <= settingsState.getCountDownSecs()){
                                soundPool?.playCountdownSound(settingsState)
                            }
                        }
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
            onPause = {playState = PlayExerciseTableState.PAUSED },
            onResume = {playState = PlayExerciseTableState.RUNNING },
            onSkip = { toIndex -> currentExerciseIndex = toIndex},
            onRestart = {playState = PlayExerciseTableState.READY }
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
                nextExercise = items.getOrNull(currentExercise+1),
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
            nextExercise = items.getOrNull(currentExercise+1),
            currentTimeMillis = currentTimeMillis,
            startDelay = startDelay,
            onStart = onStart,
            onPause = onPause,
            onResume = onResume,
            onRestart = onRestart
        )
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
                items = (1 until 15).toList().map{ Exercise("Exercise $it") })
        }
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode", widthDp = 720, heightDp = 460, uiMode = Configuration.ORIENTATION_LANDSCAPE)
@Composable
fun PlaybackTabletPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlayExerciseTableView(
                settingsViewModel = SettingsViewModel(userDataRepository = UserDataDummyRepository()),
                training = Training("test",30,5),
                session = Session(UUID.randomUUID()),
                items = (1 until 15).toList().map{ Exercise("Exercise $it") })
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
                items = (1 until 15).toList().map{ Exercise("Exercise $it") })
        }
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode", widthDp = 720, heightDp = 260, uiMode = Configuration.ORIENTATION_LANDSCAPE)
@Composable
fun PlaybackExtraWidePreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlayExerciseTableView(
                settingsViewModel = SettingsViewModel(userDataRepository = UserDataDummyRepository()),
                training = Training("test",30,5),
                session = Session(UUID.randomUUID()),
                items = (1 until 15).toList().map{ Exercise("Exercise $it") })
        }
    }
}