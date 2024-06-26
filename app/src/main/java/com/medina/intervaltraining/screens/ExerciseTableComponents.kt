package com.medina.intervaltraining.screens


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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.Cancel
import androidx.compose.ui.focus.FocusRequester.Companion.Default
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medina.intervaltraining.R
import com.medina.intervaltraining.data.viewmodel.Exercise
import com.medina.intervaltraining.data.viewmodel.ExerciseIcon
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import com.medina.intervaltraining.ui.drawableId
import com.medina.intervaltraining.ui.iconName
import com.medina.intervaltraining.ui.iconToDrawableResource
import com.medina.intervaltraining.ui.iconToStringResource
import com.medina.intervaltraining.ui.stringForButtonDescription
import com.medina.intervaltraining.ui.stringForIconDescription
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Draws button with.
 *
 * When not visible, will collapse to 16.dp high by default. You can enlarge this with the passed
 * modifier.
 *
 * @param icon (state) the current selected icon
 * @param modifier modifier for this element
 */
@Composable
fun DialogIconButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    iconDescription: String,
    enabled : Boolean = true,
    onClick: ()->Unit,
){
    Button(
        modifier = modifier.height(40.dp),
        enabled = enabled,
        onClick = onClick
    ) {
        Row {
            Icon(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(24.dp),
                imageVector = icon,
                contentDescription = iconDescription
            )
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 8.dp),
                text = text
            )
        }
    }
}

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
    Row(modifier.semantics { this.testTag = "IconRow" }
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
    iconSelectable: @Composable (tint:Color, modifier: Modifier) -> Unit,
    onIconSelected: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val tint = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
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
 * Styled [TextField] for inputting a text
 *
 * @param entryText (state) current text to display
 * @param onSave (event) request to save the value
 * @param timeoutMill millisecond before calling onSave after a change
 * @param modifier the modifier for this element
 * @param placeholder text to show when the entry is empty
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SavableInputText(entryText: String, onSave:(String)->Unit, timeoutMill:Long,
                     modifier: Modifier = Modifier,
                     placeholder: String = "",){

    val (text, setText) = remember(entryText) { mutableStateOf(entryText) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(text) {
        if(text.isEmpty()) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    InputText(
        text = text,
        onTextChange = {
            setText(it)
            scope.launch {
                delay(timeoutMill)
                onSave(it)
            }
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .focusProperties {
                enter = { if (focusRequester.restoreFocusedChild()) Cancel else Default }
            }
        ,placeholder = placeholder
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
        colors = TextFieldDefaults.colors(),
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
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = stringForButtonDescription(id = R.string.input_number_more)
                )
            }
            IconButton(modifier = Modifier
                .weight(0.1f)
                .padding(0.dp), onClick = { onNumberChange(value-1) }) {
                Icon(
                    modifier = Modifier
                        .widthIn(min = 48.dp)
                        .heightIn(min = 24.dp),
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringForButtonDescription(id = R.string.input_number_less)
                )
            }
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
                text = exercise.name ,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 24.sp
                ),
            )
            Row {
                Text(
                    text = stringResource(id = R.string.exercise_label_time_and_rest, exercise.timeSec, exercise.restSec),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp
                    ),
                )
            }
        }
    }
}

@Composable
fun ExerciseLabel(exercise: Exercise, modifier: Modifier = Modifier, shadowElevation: Dp = 1.dp ) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, shadowElevation = shadowElevation) {
        ExerciseLabelBody(exercise = exercise)
    }
}

@Composable
fun ExerciseRunningLabel(exercise: Exercise, currentTimeMillis:Int, modifier: Modifier = Modifier) {
    val totalProgress: Float by animateFloatAsState(
        targetValue = currentTimeMillis / ((exercise.restSec + exercise.timeSec)*1000).toFloat(),
        label = "TotalProgress"
    )
    val isRest = exercise.timeSec*1000 < currentTimeMillis
    val color = if(isRest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    when{
        currentTimeMillis == 0 ->  ExerciseLabel(exercise = exercise,modifier = modifier)
        currentTimeMillis < 0 ->
            Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, shadowElevation = 1.dp) {
                Box(modifier = Modifier
                    .height(2.dp)
                    .fillMaxWidth()
                    .background(Color(0x80808080)))
                Box(modifier = Modifier.background(Color(0x40808080))) {
                    ExerciseLabelBody(exercise = exercise, modifier = modifier.padding(2.dp))
                }
            }
        else -> Surface(modifier = modifier, shape = MaterialTheme.shapes.medium, shadowElevation = 1.dp) {
            Box(modifier = Modifier
                .height(2.dp)
                .fillMaxWidth(totalProgress)
                .background(color))
            ExerciseLabelBody(exercise = exercise, modifier = modifier.padding(2.dp))
        }
    }
}


@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
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
    ExerciseTableIcon(icon = ExerciseIcon.NONE, tint = MaterialTheme.colorScheme.primary)
}

@Preview
@Composable
fun PreviewInputNumber() {
    InputNumber(modifier = Modifier
        .padding(horizontal = 8.dp, vertical = 8.dp)
        .width(160.dp)
        .height(48.dp), 0){}
}

@Preview
@Composable
fun PreviewDialogIconButton() {
    DialogIconButton(modifier = Modifier
        .padding(horizontal = 8.dp, vertical = 8.dp)
        .width(160.dp)
        .height(48.dp),
        text = "Button",
        icon = Icons.Default.Edit,
        iconDescription = "Edit",
        onClick = {}
    )
}