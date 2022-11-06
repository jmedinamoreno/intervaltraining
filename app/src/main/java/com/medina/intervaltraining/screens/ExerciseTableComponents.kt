package com.medina.intervaltraining.screens


import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medina.intervaltraining.R
import com.medina.intervaltraining.data.viewmodel.Exercise
import com.medina.intervaltraining.data.viewmodel.ExerciseIcon
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import com.medina.intervaltraining.ui.theme.Utils
import kotlinx.coroutines.delay

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
@OptIn(ExperimentalAnimationApi::class)
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
    Row(modifier) {
        for (i in ExerciseIcon.values()) {
            SelectableIconButton(
                iconSelectable = { tint, modifier ->
                    if(i == ExerciseIcon.NONE){
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = stringResource(id = androidx.appcompat.R.string.abc_menu_delete_shortcut_label),
                            tint = tint,
                            modifier = modifier
                        )
                    }else {
                        ExerciseTableIcon(icon = i, tint = tint, modifier = modifier)
                    }    
                                 },
                onIconSelected = { onIconChange(i) },
                isSelected = i == icon
            )
        }
    }
}

/**
 * Displays a single icon that can be selected.
 *
 * @param iconSelectable the icon to draw
 * @param onIconSelected (event) request this icon be selected
 * @param isSelected (state) selection state
 * @param modifier modifier for this element
 */
@Composable
private fun SelectableIconButton(
    iconSelectable: @Composable() (tint:Color, modifier: Modifier) -> Unit,
    onIconSelected: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val tint = if (isSelected) {
        MaterialTheme.colors.primary
    } else {
        MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
    }
    TextButton(
        onClick = { onIconSelected() },
        shape = CircleShape,
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            iconSelectable(tint,Modifier)
            if (isSelected) {
                Box(
                    Modifier
                        .padding(top = 3.dp)
                        .width(24.dp)
                        .height(1.dp)
                        .background(tint)
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * Draw a background based on MaterialTheme.colors.onSurface that animates resizing and elevation
 * changes.
 *
 * @param elevate draw a shadow, changes to this will be animated
 * @param modifier modifier for this element
 * @param content (slot) content to draw in the background
 */
@Composable
fun ItemInputBackground(
    elevate: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val animatedElevation by animateDpAsState(if (elevate) 1.dp else 0.dp, TweenSpec(500))
    Surface(
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.05f),
        elevation = animatedElevation,
        shape = RectangleShape,
    ) {
        Row(
            modifier = modifier.animateContentSize(animationSpec = TweenSpec(300)),
            content = content
        )
    }
}

/**
 * Styled [TextField] for inputting a text
 *
 * @param entryText (state) current text to display
 * @param onSave (event) request to save the value
 * @param timeoutMill millisecond before calling onSave after a change
 * @param modifier the modifier for this element
 * @param placeholder text to show when the entry is empty
 */
@Composable
fun SavableInputText(entryText: String, onSave:(String)->Unit, timeoutMill:Long,
                     modifier: Modifier = Modifier,
                     placeholder: String = "",){

    val (text, setText) = remember(entryText) { mutableStateOf(entryText) }
    LaunchedEffect(text){
        delay(timeoutMill)
        onSave(text)
    }
    InputText(
        text = text,
        onTextChange = {
            setText(it)
        },
        modifier = modifier,
        placeholder = placeholder
    )
}
/**
 * Styled [TextField] for inputting a text
 *
 * @param text (state) current text to display
 * @param onTextChange (event) request the text change state
 * @param modifier the modifier for this element
 * @param onImeAction (event) notify caller of [ImeAction.Done] events
 * @param placeholder text to show when the entry is empty
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputText(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onImeAction: () -> Unit = {},
    placeholder: String = "",
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    TextField(
        value = text,
        onValueChange = onTextChange,
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
        maxLines = 1,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            onImeAction()
            keyboardController?.hide()
        }),
        modifier = modifier,
        placeholder = {Text(placeholder)},
        singleLine = true
    )
}

/**
 * Styled [TextField] for inputting a number
 *
 * @param value current number to display
 * @param modifier the modifier for this element
 * @param onNumberChange (event) notify value changed
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputNumber(
    modifier: Modifier = Modifier,
    value: Int = 0,
    onNumberChange: (Int) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = value.toString(),
            onValueChange = { if(it.isNotEmpty()){ onNumberChange(it.toInt()) } },
            maxLines = 1,
            textStyle = TextStyle(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done, keyboardType = KeyboardType.Number),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
            }),
            modifier = Modifier
                .weight(0.1f)
                .padding(0.dp),
            singleLine = true
        )
        Column(modifier = Modifier
            .padding(0.dp)) {
            IconButton(modifier = Modifier
                .weight(0.1f)
                .padding(0.dp), onClick = { onNumberChange(value+1) }) {
                Icon(
                    modifier = Modifier
                        .widthIn(min = 48.dp)
                        .heightIn(min = 24.dp),
                    imageVector = Icons.Default.ExpandLess,
                    contentDescription = "#More"
                )
            }
            IconButton(modifier = Modifier
                .weight(0.1f)
                .padding(0.dp), onClick = { onNumberChange(value-1) }) {
                Icon(
                    modifier = Modifier
                        .widthIn(min = 48.dp)
                        .heightIn(min = 24.dp),
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "#Less"
                )
            }
        }
    }
}

@Composable
fun ExerciseTableIcon(icon: ExerciseIcon, tint:Color, modifier: Modifier = Modifier,){
    Icon(
        modifier = modifier,
        imageVector = when (icon) {
            ExerciseIcon.RUN -> ImageVector.vectorResource(R.drawable.ic_exercise_run)
            ExerciseIcon.JUMP -> ImageVector.vectorResource(R.drawable.ic_exercise_jump)
            else -> ImageVector.vectorResource(R.drawable.ic_exercise_none)
        },
        tint = tint,
        contentDescription = stringResource(id = R.string.ic_description_exercise_icon),
    )
}


@Composable
fun ExerciseLabelBody(exercise: Exercise, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Image(
            painter = painterResource(id = Utils.iconToDrawableResource(exercise.icon)),
            colorFilter = ColorFilter.tint(MaterialTheme.colors.secondary.copy(alpha = 0.6f)),
            contentDescription = exercise.name,
            modifier = Modifier
                .padding(2.dp)
                .align(Alignment.CenterVertically)
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colors.secondary, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier
            .weight(0.2f)
            .align(Alignment.CenterVertically)) {
            Text(text = exercise.name ,
                style = MaterialTheme.typography.h3.copy(
                    fontSize = 18.sp
                ),
            )
            Row {
                Text(text = "#Time/Rest: ${exercise.timeSec}/${exercise.restSec}" ,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.h3.copy(
                        fontSize = 14.sp
                    ),
                )
            }
        }
    }
}

@Composable
fun ExerciseLabel(exercise: Exercise, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, elevation = 1.dp) {
        ExerciseLabelBody(exercise = exercise)
    }
}

@Composable
fun ExerciseRunningLabel(exercise: Exercise, currentTimeMilis:Int, modifier: Modifier = Modifier) {
    val totalProgress: Float by animateFloatAsState(currentTimeMilis / ((exercise.restSec + exercise.timeSec)*1000).toFloat())
    val isRest = exercise.timeSec*1000 < currentTimeMilis
    val color = if(isRest) MaterialTheme.colors.primary else MaterialTheme.colors.secondary

    when{
        currentTimeMilis == 0 ->  ExerciseLabel(exercise = exercise,modifier = modifier)
        currentTimeMilis < 0 ->
            Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, elevation = 1.dp) {
                Box(modifier = Modifier.height(2.dp).fillMaxWidth().background(Color(0x80808080)))
                Box(modifier = Modifier.background(Color(0x40808080))) {
                    ExerciseLabelBody(exercise = exercise, modifier = modifier.padding(2.dp))
                }
            }
        else -> Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, elevation = 1.dp) {
            Box(modifier = Modifier.height(2.dp).fillMaxWidth(totalProgress).background(color))
            ExerciseLabelBody(exercise = exercise, modifier = modifier.padding(2.dp))
        }
    }
}


@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true,)
@Composable
fun RowPreview() {
    IntervalTrainingTheme {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .fillMaxWidth()
                .height(420.dp)
        ) {
            ExerciseLabel(
                Exercise(
                    name = "Jump",
                    icon = ExerciseIcon.JUMP
                )
            )
            ExerciseRunningLabel(exercise =
                Exercise(
                    name = "Run",
                    icon = ExerciseIcon.RUN
                ),
                0
            )
            ExerciseRunningLabel(exercise =
            Exercise(
                name = "Run",
                icon = ExerciseIcon.RUN
            ),
                -1
            )
            ExerciseRunningLabel(exercise =
            Exercise(
                name = "Run",
                icon = ExerciseIcon.RUN,
                timeSec = 40,
                restSec = 20,
            ),
                20
            )
            ExerciseRunningLabel(exercise =
            Exercise(
                name = "Run",
                icon = ExerciseIcon.RUN,
                timeSec = 40,
                restSec = 20,
            ),
                50
            )

        }
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
    ExerciseTableIcon(icon = ExerciseIcon.NONE, tint = MaterialTheme.colors.primary)
}

@Preview
@Composable
fun PreviewInputNumber() {
    InputNumber(modifier = Modifier
        .padding(horizontal = 8.dp, vertical = 8.dp)
        .width(160.dp)
        .height(48.dp), 0, {})
}