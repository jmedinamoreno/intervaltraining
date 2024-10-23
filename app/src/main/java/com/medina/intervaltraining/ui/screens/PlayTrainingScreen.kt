package com.medina.intervaltraining.ui.screens

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medina.intervaltraining.R
import com.medina.intervaltraining.data.model.Exercise
import com.medina.intervaltraining.data.model.Session
import com.medina.intervaltraining.data.model.Training
import com.medina.intervaltraining.data.repository.UserDataDummyRepository
import com.medina.intervaltraining.data.viewmodel.ExerciseViewModel
import com.medina.intervaltraining.data.viewmodel.SettingsViewModel
import com.medina.intervaltraining.data.viewmodel.getCountDownSecs
import com.medina.intervaltraining.data.viewmodel.getTrainingStartDelaySecs
import com.medina.intervaltraining.ui.components.ExerciseLabel
import com.medina.intervaltraining.ui.components.ExerciseLabelBody
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
                            currentExerciseIndex = currentExerciseIndex.inc()
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
            currentExercise = currentExerciseIndex.coerceAtMost((items.size-1).coerceAtLeast(0)),
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
    Box(modifier = modifier) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            PlayExerciseTableBodyHorizontal(
                modifier = Modifier,
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
                modifier = Modifier,
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
        Card(
            shape = RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 0.dp,
                bottomStart = 60.dp,
                bottomEnd = 0.dp
            ),
            modifier = Modifier
                .width(80.dp)
                .height(80.dp)
                .align(Alignment.TopEnd),
        ) {
            Text(text = "${currentExercise+1} / ${items.size}",
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp, end = 8.dp))
            val totalMinutes = currentTimeMillis/60000 + items.subList(0,currentExercise).sumOf { it.timeSec+it.restSec }/60
            Text(text = "$totalMinutes' / ${(items.sumOf { it.timeSec+it.restSec })/60}'",
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 2.dp, end = 8.dp))
        }
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
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    // Control automatic scrolling based on conditions (e.g., initial load)
    LaunchedEffect(key1 = currentExercise) {
        delay(1000)// Initial delay (optional)
        coroutineScope.launch {
            Log.d("SCROLL", "Scrolling to $currentExercise")
            listState.animateScrollToItem(currentExercise)
        }
    }
    LazyColumn(
        modifier = modifier,
        state = listState,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(items) { exercise ->
            when {
                items.indexOf(exercise) > currentExercise ->
                    ExerciseLabel(
                        exercise = exercise,
                        modifier = Modifier
                            .padding(2.dp)
                            .combinedClickable {
                                onSkip(items.indexOf(exercise))
                            }
                    )
                items.indexOf(exercise) < currentExercise ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        shadowElevation = 1.dp,
                        modifier = modifier
                            .padding(2.dp)
                            .combinedClickable {
                                onSkip(items.indexOf(exercise))
                            }
                    ) {
                        Box(modifier = Modifier.background(Color(0x80808080))) {
                            ExerciseLabelBody(exercise = exercise, modifier = modifier.padding(2.dp))
                        }
                    }
                else ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .padding(2.dp)
                            .combinedClickable(
                                onLongClick = { onSkip(items.indexOf(exercise)) },
                                onClick = {
                                    when (playState) {
                                        PlayExerciseTableState.READY -> onStart()
                                        PlayExerciseTableState.STARTING ->  onPause()
                                        PlayExerciseTableState.RUNNING -> onPause()
                                        PlayExerciseTableState.PAUSED -> onResume()
                                        PlayExerciseTableState.COMPLETE -> onRestart()
                                    }
                                },
                                enabled = true
                            )
                    ) {
                        ExercisePlayer(
                            modifier =Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(bottom = 20.dp),
                            playState = playState,
                            startDelay = startDelay,
                            runningExercise = exercise,
                            currentTimeMillis = currentTimeMillis
                        )
                    }
            }
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
                when {
                    items.indexOf(exercise) > currentExercise ->
                        ExerciseLabel(
                            exercise = exercise,
                            modifier = Modifier
                                .padding(2.dp)
                                .combinedClickable {
                                    onSkip(items.indexOf(exercise))
                                }
                        )
                    items.indexOf(exercise) < currentExercise ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            shadowElevation = 1.dp,
                            modifier = modifier
                                .padding(2.dp)
                                .combinedClickable {
                                    onSkip(items.indexOf(exercise))
                                }
                        ) {
                            Box(modifier = Modifier.background(Color(0x80808080))) {
                                ExerciseLabelBody(exercise = exercise, modifier = modifier.padding(2.dp))
                            }
                        }
                    else ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .padding(2.dp)
                                .clickable {
                                    when (playState) {
                                        PlayExerciseTableState.READY -> onStart()
                                        PlayExerciseTableState.STARTING ->  onPause()
                                        PlayExerciseTableState.RUNNING -> onPause()
                                        PlayExerciseTableState.PAUSED -> onResume()
                                        PlayExerciseTableState.COMPLETE -> onRestart()
                                    }
                                }
                        ) {
                            ExerciseRunningLabel(exercise = exercise, currentTimeMillis, modifier = modifier.padding(2.dp))
                        }
                }
            }
        }
        Surface(
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 1.dp,
            modifier = Modifier.aspectRatio(1f)
                .padding(2.dp)
                .clickable {
                    when (playState) {
                        PlayExerciseTableState.READY -> onStart()
                        PlayExerciseTableState.STARTING ->  onPause()
                        PlayExerciseTableState.RUNNING -> onPause()
                        PlayExerciseTableState.PAUSED -> onResume()
                        PlayExerciseTableState.COMPLETE -> onRestart()
                    }
                }
        ) {
            ExercisePlayer(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 20.dp),
                playState = playState,
                startDelay = startDelay,
                runningExercise = items.getOrNull(currentExercise),
                currentTimeMillis = currentTimeMillis
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
@Preview(name = "Light Mode", group = "TabletBody")
@Preview(name = "Dark Mode", group = "TabletBody", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Light Mode", group = "TabletBodyHorizontal", widthDp = 720, heightDp = 460, uiMode = Configuration.ORIENTATION_LANDSCAPE)
@Preview(name = "Dark Mode", group = "TabletBodyHorizontal", widthDp = 720, heightDp = 460, uiMode = Configuration.ORIENTATION_LANDSCAPE or Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light Mode", group = "TabletBodyHorizontal", widthDp = 720, heightDp = 260, uiMode = Configuration.ORIENTATION_LANDSCAPE)
@Preview(name = "Dark Mode", group = "TabletBodyHorizontal", widthDp = 720, heightDp = 260, uiMode = Configuration.ORIENTATION_LANDSCAPE or Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PlaybackTabletBodyPreview(@PreviewParameter(PlaybackTableBodyArgsProvider::class) param: PlaybackTableBodyArgs) {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlayExerciseTableBody(
                modifier = Modifier.padding(8.dp),
                items = (1 until 15).toList().map{ Exercise("Exercise $it", timeSec = 45, restSec = 15) },
                playState = param.playState,
                startDelay = 0,
                currentExercise = param.currentExercise,
                currentTimeMillis = param.currentTimeMillis,
                onStart = { },
                onPause = { },
                onResume = {  },
                onSkip = {  },
                onRestart = {  }
            )
        }
    }
}
data class PlaybackTableBodyArgs(
    val playState:PlayExerciseTableState,
    val currentExercise: Int,
    val currentTimeMillis: Int,
)
class PlaybackTableBodyArgsProvider : PreviewParameterProvider<PlaybackTableBodyArgs> {
    override val values = sequenceOf(
        PlaybackTableBodyArgs(
            playState = PlayExerciseTableState.READY,
            currentExercise = 0,
            currentTimeMillis = 0
        ),
        PlaybackTableBodyArgs(
            playState = PlayExerciseTableState.STARTING,
            currentExercise = 0,
            currentTimeMillis = 500
        ),
        PlaybackTableBodyArgs(
            playState = PlayExerciseTableState.RUNNING,
            currentExercise = 2,
            currentTimeMillis = 15000
        ),
    )
}
