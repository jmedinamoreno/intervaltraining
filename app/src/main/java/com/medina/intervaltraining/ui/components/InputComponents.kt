package com.medina.intervaltraining.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medina.intervaltraining.R
import com.medina.intervaltraining.ui.stringForButtonDescription
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Displays a single icon that can be selected.
 *
 * @param iconSelectable the icon to draw
 * @param onIconSelected (event) request this icon be selected
 * @param isSelected (state) selection state
 * @param modifier modifier for this element
 */
@Composable
fun SelectableIconButton(
    iconSelectable: @Composable (tint: Color, modifier: Modifier) -> Unit,
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
            iconSelectable(tint, Modifier)
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
                enter = { if (focusRequester.restoreFocusedChild()) FocusRequester.Cancel else FocusRequester.Default }
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
        placeholder = { Text(placeholder) },
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


@Preview
@Composable
fun PreviewInputNumber() {
    InputNumber(modifier = Modifier
        .padding(horizontal = 8.dp, vertical = 8.dp)
        .width(160.dp)
        .height(48.dp), 0){}
}