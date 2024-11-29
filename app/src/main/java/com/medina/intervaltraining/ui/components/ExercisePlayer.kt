package com.medina.intervaltraining.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medina.data.model.Exercise
import com.medina.data.model.ExerciseIcon
import com.medina.intervaltraining.R
import com.medina.intervaltraining.ui.screens.PlayExerciseTableState
import com.medina.intervaltraining.ui.stringChosen
import com.medina.intervaltraining.ui.stringForIconDescription
import com.medina.intervaltraining.ui.stringRandomSelected
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import com.medina.intervaltraining.ui.theme.desaturateColor
import com.medina.intervaltraining.ui.theme.mixColor
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ExercisePlayer(
    modifier: Modifier,
    playState: PlayExerciseTableState,
    startDelay: Int,
    runningExercise: Exercise?,
    currentTimeMillis: Int){
    Surface(modifier = modifier.then(
        Modifier.background(
            MaterialTheme.colorScheme.background
        )
    )) {
        when (playState) {
            PlayExerciseTableState.READY -> BigPlayButton(
                modifier = Modifier.fillMaxSize(),
            )

            PlayExerciseTableState.STARTING ->  StartingRoutine(
                modifier = Modifier.fillMaxSize(),
                startDelay = startDelay,
                currentTimeMillis = currentTimeMillis,
                firstExercise = runningExercise
            )

            PlayExerciseTableState.RUNNING -> RunningExercise(
                modifier = Modifier.fillMaxSize(),
                runningExercise = runningExercise,
                currentTimeMillis = currentTimeMillis
            )

            PlayExerciseTableState.PAUSED -> PausedExercise(
                modifier = Modifier.fillMaxSize(),
                runningExercise = runningExercise,
                currentTimeSec = currentTimeMillis / 1000
            )

            PlayExerciseTableState.COMPLETE -> FinishedTraining(
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private const val longNameLimit = 20
private val nameFontSize = 28.sp
private val longnameFontSize = 20.sp
private val nextFontSize = 26.sp
private val pauseFontSize = 48.sp

@Composable
fun BigPlayButton(modifier: Modifier){
    Box(modifier = modifier) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = stringForIconDescription(id = R.string.running_training_play),
            modifier = Modifier
                .size(84.dp)
                .align(Alignment.Center),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun StartingRoutine(
    modifier: Modifier,
    startDelay: Int,
    currentTimeMillis: Int,
    firstExercise: Exercise?
) {
    val text = stringRandomSelected(id = R.array.running_exercise_starting_list)
    val textFirst = firstExercise?.name?.let {
        stringResource(
            R.string.running_exercise_first_exercise_label,
            it
        ) }
    Column(modifier = modifier) {
        OutlinedText(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = text,
            fontSize = nameFontSize,
            color = MaterialTheme.colorScheme.primary,
            outlineColor = MaterialTheme.colorScheme.background,
            shadowColor = MaterialTheme.colorScheme.background
        )
        CounterView(
            modifier = Modifier.weight(1f),
            total = startDelay,
            currentMs = currentTimeMillis,
            backgroundColor = MaterialTheme.colorScheme.background,
            primaryColor = MaterialTheme.colorScheme.primary,
            secondaryColor = MaterialTheme.colorScheme.secondary
        )
        if(textFirst!=null) {
            OutlinedText(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = textFirst,
                fontSize = nextFontSize,
                color = MaterialTheme.colorScheme.primary,
                outlineColor = MaterialTheme.colorScheme.background,
                shadowColor = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
fun RunningExercise(modifier: Modifier, runningExercise: Exercise?, currentTimeMillis: Int){
    if(runningExercise == null) return
    val runTimeMillis = runningExercise.timeSec * 1000
    val restTimeMillis = runningExercise.restSec * 1000
    val totalTimeMillis = runTimeMillis + restTimeMillis
    val isRest = runTimeMillis < currentTimeMillis
    val text = if(isRest)
        stringChosen(id = R.array.running_exercise_rest_list,runningExercise.name.hashCode())
    else
        runningExercise.name

    Column(modifier = modifier) {
        OutlinedText(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = text,
            fontSize = if(text.length> longNameLimit) longnameFontSize else nameFontSize,
            color = MaterialTheme.colorScheme.primary,
            outlineColor = MaterialTheme.colorScheme.background,
            shadowColor = MaterialTheme.colorScheme.background
        )
        ChronometerView(
            modifier = Modifier.weight(1f),
            progressMs = currentTimeMillis,
            partialMs = runTimeMillis,
            completeMs = totalTimeMillis,
            backgroundColor = MaterialTheme.colorScheme.background,
            primaryColor = MaterialTheme.colorScheme.primary,
            secondaryColor = MaterialTheme.colorScheme.secondary,
            thirdColor = MaterialTheme.colorScheme.secondary.mixColor(MaterialTheme.colorScheme.background)
        )
    }
}

@Composable
fun PausedExercise(modifier: Modifier, runningExercise: Exercise?, currentTimeSec: Int){
    if(runningExercise == null) return
    val runTimeMillis = runningExercise.timeSec * 1000
    val restTimeMillis = runningExercise.restSec * 1000
    val totalTimeMillis = runTimeMillis + restTimeMillis
    val isRest = runningExercise.timeSec < currentTimeSec
    val text = stringResource(R.string.running_exercise_paused)
    val exerciseText = if(isRest)
        stringChosen(id = R.array.running_exercise_rest_list,runningExercise.name.hashCode())
    else
        runningExercise.name

    val backgroundColor = MaterialTheme.colorScheme.background.desaturateColor()
    val primaryColor = MaterialTheme.colorScheme.primary.desaturateColor().mixColor(backgroundColor)
    val secondaryColor = MaterialTheme.colorScheme.secondary.desaturateColor().mixColor(backgroundColor)
    val thirdColor = MaterialTheme.colorScheme.tertiary.desaturateColor().mixColor(backgroundColor)

    Box(modifier = modifier) {
        Column {
            OutlinedText(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = exerciseText,
                fontSize = if(text.length> longNameLimit) longnameFontSize else nameFontSize,
                color = primaryColor,
                outlineColor = backgroundColor,
                shadowColor = backgroundColor
            )
            ChronometerView(
                modifier = Modifier.weight(1f),
                progressMs = currentTimeSec * 1000,
                partialMs = runTimeMillis,
                completeMs = totalTimeMillis,
                backgroundColor = backgroundColor,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                thirdColor = thirdColor
            )
        }
        OutlinedText(
            modifier = Modifier.align(Alignment.Center),
            text = text,
            fontSize = pauseFontSize,
            color = MaterialTheme.colorScheme.primary,
            outlineColor = MaterialTheme.colorScheme.background,
        )
    }
}

@Composable
fun FinishedTraining(modifier: Modifier){
    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        OutlinedText(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(R.string.running_exercise_end_top),
            fontSize = longnameFontSize,
            color = MaterialTheme.colorScheme.primary,
            outlineColor = MaterialTheme.colorScheme.background,
            shadowColor = MaterialTheme.colorScheme.background
        )
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = stringForIconDescription(id = R.string.running_training_restart),
            modifier = Modifier
                .size(84.dp)
                .align(Alignment.CenterHorizontally),
            tint = MaterialTheme.colorScheme.primary
        )
        OutlinedText(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(R.string.running_exercise_end_bottom),
            fontSize = longnameFontSize,
            color = MaterialTheme.colorScheme.primary,
            outlineColor = MaterialTheme.colorScheme.background,
            shadowColor = MaterialTheme.colorScheme.background
        )
    }
}

@Composable
private fun CounterView(
modifier: Modifier,
total: Int,
currentMs: Int,
backgroundColor: Color,
primaryColor:Color,
secondaryColor:Color
){
    val currentTimeSec = currentMs/1000
    val timeText = "${total - currentTimeSec}"
    BoxWithConstraints(modifier = modifier) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2.4f
            val dotRadius = size.minDimension / 14
            drawRect(backgroundColor)
            for (i in 0 until total) {
                val angle = ((2 * Math.PI / total) * i ) - (Math.PI / 2)
                val x = center.x + radius * cos(angle).toFloat()
                val y = center.y + radius * sin(angle).toFloat()

                drawCircle(
                    color = secondaryColor,
                    radius = dotRadius,
                    center = Offset(x, y),
                    style = Stroke(width = 4.dp.value)
                )
                drawCircle(
                    color = primaryColor,
                    radius = dotRadius*((currentMs-(i*1000))/1000f).coerceIn(0f,1f),
                    center = Offset(x, y),
                    style = Fill // Optional: Add a stroke for better visibility
                )
            }
        }
        OutlinedText(
            modifier = Modifier.align(Alignment.Center),
            text = timeText,
            fontSize = (min(maxHeight.value, maxWidth.value) * 0.5f).toInt().sp,
            color = primaryColor,
            outlineColor = backgroundColor
        )
    }
}

@Composable
fun ChronometerView(
    modifier: Modifier,
    progressMs: Int,
    partialMs: Int,
    completeMs: Int,
    backgroundColor: Color,
    primaryColor:Color,
    secondaryColor:Color,
    thirdColor: Color,
){
    val isPartial = progressMs < partialMs

    val timeText = if(isPartial)
        "${(partialMs - progressMs)/1000}"
    else
        "${(completeMs - progressMs)/1000}"

    val localProgress: Float by animateFloatAsState( targetValue = if (isPartial) {
        progressMs.toFloat() / partialMs.toFloat()
    }else{
        1 - ((progressMs-partialMs).toFloat() / (completeMs-partialMs).toFloat())
    }, label = "localProgress"
    )

    val totalProgress: Float by animateFloatAsState(
        targetValue = progressMs.toFloat() / completeMs.toFloat(), label = "totalProgress"
    )

    BoxWithConstraints(modifier = modifier) {
        ProgressCircle(
            modifier = Modifier.fillMaxSize(),
            localProgress = localProgress,
            totalProgress = totalProgress,
            primaryColor = primaryColor,
            secondaryColor = if(isPartial) secondaryColor else thirdColor,
            backgroundColor = backgroundColor
        )
        OutlinedText(
            modifier = Modifier.align(Alignment.Center),
            text = timeText,
            fontSize = (min(maxHeight.value, maxWidth.value) * 0.5f).toInt().sp,
            color = if(isPartial) secondaryColor else thirdColor,
            outlineColor = backgroundColor
        )
    }
}

@Composable
fun ProgressCircle(
    modifier: Modifier,
    localProgress: Float,
    totalProgress: Float,
    primaryColor:Color,
    secondaryColor:Color,
    backgroundColor: Color
    ){
    Canvas(modifier = modifier) {
        val padding = 8.dp.toPx()
        drawRect(backgroundColor)
        translate(left = padding, top = padding) {
            drawContext.size = Size(size.width - (padding * 2), size.height - (padding * 2))
            val canvasWidth = size.width
            val canvasHeight = size.height
            val circleSize = min(canvasHeight,canvasWidth)
            val center = Offset(x = canvasWidth / 2, y = canvasHeight / 2)
            val radius = circleSize / 2f
            clipRect(0F, localProgress * canvasHeight, canvasWidth, canvasHeight) {
                drawCircle(
                    color = secondaryColor,
                    center = center,
                    radius = radius - 6.dp.toPx(),
                )
            }
            translate(
                left =  (canvasWidth-circleSize)/2,
                top = (canvasHeight-circleSize)/2,
            ) {
                drawArc(
                    size = Size(circleSize, circleSize),
                    color = primaryColor,
                    useCenter = false,
                    startAngle = 270f,
                    sweepAngle = totalProgress * 360f,
                    style = Stroke(6.dp.toPx()),
                )
            }
        }
    }
}

@Composable
fun OutlinedText(
    modifier: Modifier,
    text:String,
    fontSize: TextUnit,
    color: Color,
    outlineColor: Color,
    outlineWidth: Dp = 2.dp,
    shadowColor: Color = Color.Black
){
    Box(modifier = modifier) {
        Text(
            text = text, style = TextStyle(
                fontSize = fontSize,
                color = outlineColor,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = shadowColor,
                    blurRadius = outlineWidth.value*2,
                    offset = Offset(0f, 5.dp.value)
                ),
                drawStyle = Stroke(width = outlineWidth.value*2)
            ), modifier = Modifier.align(Alignment.Center)
        )
        Text(
            text = text, style = TextStyle(
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                color = color,
            ), modifier = Modifier.align(Alignment.Center)
        )
    }
}


@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "StartEndPreview")
@Preview(name = "Dark Mode", group = "StartEndPreview", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun StartPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            BigPlayButton(modifier = Modifier
                .width(200.dp)
                .height(200.dp)
            )
        }
    }
}
@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "StartEndPreview")
@Preview(name = "Dark Mode", group = "StartEndPreview", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun EndPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            FinishedTraining(
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp))
        }
    }
}



@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "StartingPreview")
@Preview(name = "Dark Mode", group = "StartingPreview", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun StartingPreview2p5to5() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StartingRoutine(
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp),
                startDelay = 5,
                currentTimeMillis = 2500,
                firstExercise = Exercise(
                    name = "Exercise",
                    icon = ExerciseIcon.JUMP,
                    restSec = 12,
                    timeSec = 45
                ),
            )
        }
    }
}
@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "StartingPreview")
@Preview(name = "Dark Mode", group = "StartingPreview", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun StartingPreviewZeroTo5() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StartingRoutine(
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp),
                startDelay = 5,
                currentTimeMillis = 0,
                firstExercise = Exercise(
                    name = "Exercise",
                    icon = ExerciseIcon.JUMP,
                    restSec = 12,
                    timeSec = 45
                ),
            )
        }
    }
}
@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "StartingPreview")
@Composable
fun StartingPreviewZeroTo10() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StartingRoutine(
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp),
                startDelay = 10,
                currentTimeMillis = 0,
                firstExercise = Exercise(
                    name = "Exercise",
                    icon = ExerciseIcon.JUMP,
                    restSec = 12,
                    timeSec = 45
                ),
            )
        }
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "StartingPreview")
@Composable
fun StartingPreviewZeroTo2() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StartingRoutine(
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp),
                startDelay = 2,
                currentTimeMillis = 0,
                firstExercise = Exercise(
                    name = "Exercise",
                    icon = ExerciseIcon.JUMP,
                    restSec = 12,
                    timeSec = 45
                ),
            )
        }
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "PlaybackPreview")
@Preview(name = "Dark Mode", group = "PlaybackPreview", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PlaybackPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RunningExercise(
                Modifier
                    .width(200.dp)
                    .height(200.dp),
                Exercise(
                    "Exercise",
                    ExerciseIcon.JUMP,
                    restSec = 12,
                    timeSec = 45
                ),
                currentTimeMillis = 18000
            )
        }
    }
}


@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "PlaybackPreview")
@Preview(name = "Dark Mode", group = "PlaybackPreview", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PlaybackPreviewRest() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RunningExercise(
                Modifier
                    .width(200.dp)
                    .height(200.dp),
                Exercise(
                    "Exercise",
                    ExerciseIcon.JUMP,
                    restSec = 12,
                    timeSec = 45
                ),
                currentTimeMillis = 50000
            )
        }
    }
}


@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "PlaybackPreview")
@Preview(name = "Dark Mode", group = "PlaybackPreview", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PlaybackPreviewLongName() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RunningExercise(
                Modifier
                    .width(200.dp)
                    .height(200.dp),
                Exercise(
                    "Exercise with a long name",
                    ExerciseIcon.JUMP,
                    restSec = 12,
                    timeSec = 45
                ),
                currentTimeMillis = 18000
            )
        }
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "PlaybackPreview")
@Preview(name = "Dark Mode", group = "PlaybackPreview", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PlaybackPreviewPaused() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PausedExercise(
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp),
                runningExercise = Exercise(
                    "Exercise",
                    ExerciseIcon.JUMP,
                    restSec = 12,
                    timeSec = 45
                ),
                currentTimeSec = 18
            )
        }
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "ChronoPreview")
@Preview(name = "Dark Mode", group = "ChronoPreview", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ChronoPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ChronometerView(
                Modifier
                    .width(200.dp)
                    .height(150.dp),
                partialMs = 45000,
                completeMs = 60000,
                progressMs = 15000,
                backgroundColor = MaterialTheme.colorScheme.background,
                primaryColor = MaterialTheme.colorScheme.primary,
                secondaryColor = MaterialTheme.colorScheme.secondary,
                thirdColor = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "ChronoPreview")
@Preview(name = "Dark Mode", group = "ChronoPreview", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ChronoPreviewPartial() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ChronometerView(
                Modifier
                    .width(200.dp)
                    .height(150.dp),
                partialMs = 45000,
                completeMs = 60000,
                progressMs = 55000,
                backgroundColor = MaterialTheme.colorScheme.background,
                primaryColor = MaterialTheme.colorScheme.primary,
                secondaryColor = MaterialTheme.colorScheme.secondary,
                thirdColor = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}