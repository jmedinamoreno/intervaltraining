package com.medina.intervaltraining.ui.components


import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.medina.data.model.Exercise
import com.medina.data.model.ExerciseIcon
import com.medina.intervaltraining.R
import com.medina.intervaltraining.ui.drawableId
import com.medina.intervaltraining.ui.iconName
import com.medina.intervaltraining.ui.iconToDrawableResource
import com.medina.intervaltraining.ui.iconToStringResource
import com.medina.intervaltraining.ui.screens.PlayExerciseTableState
import com.medina.intervaltraining.ui.stringForButtonDescription
import com.medina.intervaltraining.ui.stringForIconDescription
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme


/**
 * Draws a row of [ExerciseTableIcon] with visibility changes animated.
 *
 * When not visible, will collapse to 16.dp high by default. You can enlarge this with the passed
 * modifier.
 *
 * @param icon (state) the current selected icon
 * @param onIconChange (event) request the selected icon change
 * @param modifier modifier for this element
 * @param visible (state) if the icon should be shown
 */
@Composable
fun AnimatedIconRow(
    icon: ExerciseIcon,
    onIconChange: (ExerciseIcon) -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    // remember these specs so they don't restart if recomposing during the animation
    // this is required since TweenSpec restarts on interruption
    val enter = remember { fadeIn(animationSpec = TweenSpec(300, easing = FastOutLinearInEasing)) }
    val exit = remember { fadeOut(animationSpec = TweenSpec(100, easing = FastOutSlowInEasing)) }
    Box(modifier.defaultMinSize(minHeight = 16.dp)) {
        AnimatedVisibility(
            visible = visible,
            enter = enter,
            exit = exit,
        ) {
            IconRow(icon, onIconChange)
        }
    }
}

/**
 * Displays a row of selectable [ExerciseTableIcon]
 *
 * @param icon (state) the current selected icon
 * @param onIconChange (event) request the selected icon change
 * @param modifier modifier for this element
 */
@Composable
fun IconRow(
    icon: ExerciseIcon,
    onIconChange: (ExerciseIcon) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .semantics { this.testTag = "IconRow" }
            .selectableGroup()
    ) {
        for (exerciseIcon in ExerciseIcon.entries) {
            SelectableIconButton(
                modifier = Modifier.selectable(exerciseIcon == icon){ onIconChange(exerciseIcon) },
                iconSelectable = { tint, modifier ->
                    if(exerciseIcon == ExerciseIcon.NONE){
                        Icon(
                            imageVector = Icons.Default.Clear,
                            modifier = modifier.semantics { iconName = Icons.Default.Clear.name },
                            contentDescription = stringForButtonDescription(id = R.string.exercise_icon_delete),
                            tint = tint,
                        )
                    }else {
                        ExerciseTableIcon(
                            icon = exerciseIcon,
                            modifier = modifier,
                            tint = tint
                        )
                    }    
                                 },
                onIconSelected = { onIconChange(exerciseIcon) },
                isSelected = exerciseIcon == icon
            )
        }
    }
}


@Composable
fun ExerciseTableIcon(icon: ExerciseIcon, tint:Color, modifier: Modifier = Modifier){
    val drawableId = iconToDrawableResource(icon)
    Icon(
        modifier = modifier.semantics { this.iconName = icon.name; this.drawableId = drawableId },
        imageVector = ImageVector.vectorResource(drawableId),
        tint = tint,
        contentDescription = stringForIconDescription(id = iconToStringResource(icon)),
    )
}


@Composable
fun ExerciseLabelBody(exercise: Exercise, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(4.dp)) {
        Image(
            painter = painterResource(id = iconToDrawableResource(exercise.icon)),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            contentDescription = exercise.name,
            modifier = Modifier
                .padding(2.dp)
                .align(Alignment.CenterVertically)
                .size(48.dp)
                .aspectRatio(1f)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier
            .weight(1f)
            .align(Alignment.CenterVertically)) {
            Text(
                text = exercise.name,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyLarge,
            )
            Row {
                Text(
                    text = stringResource(id = R.string.exercise_label_time_and_rest, exercise.timeSec, exercise.restSec),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
fun ExerciseLabel(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    shadowElevation: Dp = 1.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = MaterialTheme.shapes.medium,
        shadowElevation = shadowElevation
    ) {
        ExerciseLabelBody(exercise = exercise)
    }
}

@Composable
fun ExerciseRunningLabel(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    currentTimeMillis: Int,
    shadowElevation: Dp = 1.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    val totalProgress: Float by animateFloatAsState(
        targetValue = currentTimeMillis / ((exercise.restSec + exercise.timeSec)*1000).toFloat(),
        label = "TotalProgress"
    )
    val isRest = exercise.timeSec*1000 < currentTimeMillis
    val color = if(isRest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        shadowElevation = shadowElevation,
        color = backgroundColor
    ) {
        Box {
            ExerciseLabelBody(
                exercise = exercise,
                modifier = modifier.padding(
                    top = 2.dp,
                    start = 2.dp,
                    end = 2.dp,
                    bottom = 10.dp
                )
            )
            Box(
                modifier = Modifier
                    .height(12.dp)
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(totalProgress)
                    .background(color, RoundedCornerShape(topEnd = 12.dp))
            )
        }
    }
}


@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun LabelPreview() {
    IntervalTrainingTheme {
        ExerciseLabel(exercise =
        Exercise(
            name = "Run",
            icon = ExerciseIcon.RUN
        )
        )
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun RunningLabelPreview() {
    IntervalTrainingTheme {
        ExerciseRunningLabel(exercise =
        Exercise(
            name = "Run",
            icon = ExerciseIcon.RUN,
            timeSec = 10,
            restSec = 5
        ),
                currentTimeMillis = 3000
            )
    }
}


@Preview
@Composable
fun PreviewIconRow() {
    IconRow(icon = ExerciseIcon.NONE, onIconChange = {})
}

@Preview
@Composable
fun PreviewIcon() {
    ExerciseTableIcon(icon = ExerciseIcon.NONE, tint = MaterialTheme.colorScheme.primary)
}
