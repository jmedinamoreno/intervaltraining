package com.medina.intervaltraining.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.medina.intervaltraining.R
import com.medina.intervaltraining.ui.stringForButtonDescription
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme

@Composable
fun SettingsEntryNum(
    modifier: Modifier = Modifier,
    labelTitle: String = "",
    labelDescription: String = "",
    extraDescription: String = "",
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    val dialogState: MutableState<Boolean> = rememberSaveable {
        mutableStateOf(false)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
            .clickable {
                dialogState.value = true
            }
    ) {
        Text(
            modifier = Modifier,
            text = labelTitle,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Start
        )
        Text(
            modifier = Modifier,
            text = labelDescription.format(value),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start
        )
    }
    if (dialogState.value) {
        Dialog(
            onDismissRequest = { dialogState.value = false },
            content = {
                DialogInputNumberBody(extraDescription, value){
                    onValueChange(it)
                    dialogState.value = false
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        )
    }
}


@Composable
fun SettingsEntryEnum(
    modifier: Modifier = Modifier,
    labelTitle: String = "",
    labelDescription: String = "",
    extraDescription: String = "",
    values: Map<String, String>,
    selectedValue: String,
    onValueChange: (String) -> Unit,
) {
    val dialogState: MutableState<Boolean> = rememberSaveable {
        mutableStateOf(false)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
            .clickable {
                dialogState.value = true
            }
    ) {
        Text(
            modifier = Modifier,
            text = labelTitle,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Start
        )
        Text(
            modifier = Modifier,
            text = labelDescription.format(values.getOrDefault(selectedValue, "")),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start
        )
    }
    if (dialogState.value) {
        Dialog(
            onDismissRequest = { dialogState.value = false },
            content = {
                DialogInputEnumBody(
                    labelTitle = extraDescription,
                    values = values,
                    selectedValue = selectedValue) {
                    onValueChange(it)
                    dialogState.value = false
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        )
    }
}

@Composable
fun SettingsEntryToggle(
    modifier: Modifier = Modifier,
    labelTitle: String = "",
    labelDescription: String = "",
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                modifier = Modifier,
                text = labelTitle,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Start
            )
            Text(
                modifier = Modifier,
                text = labelDescription,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            modifier = Modifier,
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}


// DIALOGS

@Composable
fun DialogInputNumberBody(
    labelTitle: String = "",
    value: Int,
    onValueSet: (Int) -> Unit){
    val dialogValue: MutableState<Int> = rememberSaveable { mutableIntStateOf(value) }
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Text(
                modifier = Modifier,
                text = labelTitle,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            InputNumber(
                modifier = Modifier
                    .height(64.dp)
                    .align(Alignment.CenterHorizontally),
                value =  dialogValue.value,
                onNumberChange = {
                    dialogValue.value = it
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            DialogIconButton(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp),
                text = stringResource(id = R.string.edit_exercise_dialog_save),
                icon = Icons.Default.Check,
                iconDescription = stringForButtonDescription(id = R.string.edit_exercise_dialog_save),
                onClick = {
                    onValueSet(dialogValue.value)
                }
            )
        }
    }
}

@Composable
fun DialogInputEnumBody(
    labelTitle: String = "",
    values: Map<String, String>,
    selectedValue: String,
    onValueChange: (String) -> Unit){

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .selectableGroup()
                .fillMaxWidth(),
        ) {
            Text(
                modifier = Modifier,
                text = labelTitle,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            values.keys.forEach { key ->
                val text = values[key]
                if(text!=null) {
                    Row(
                        Modifier
                            .height(48.dp)
                            .fillMaxWidth()
                            .selectable(
                                selected = (key == selectedValue),
                                onClick = { onValueChange(key) },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (key == selectedValue),
                            onClick = null // null recommended for accessibility with screenreaders
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

//Previews
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun SettingsEntryTogglePreview() {
    IntervalTrainingTheme {
        SettingsEntryToggle(
            labelTitle = "Title",
            labelDescription = "Description",
            checked = true,
            onCheckedChange = {})
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun SettingsEntryEnumPreview() {
    IntervalTrainingTheme {
        SettingsEntryEnum(
            labelTitle = "Title",
            labelDescription = "Selected '%s' option",
            values = mapOf("A" to "Option A", "B" to "Option B"),
            selectedValue = "A",
            onValueChange = {}
        )
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun SettingsEntryNumPreview() {
    IntervalTrainingTheme {
        SettingsEntryNum(
            labelTitle = "Title",
            labelDescription = "Value: %s units",
            value = 5,
            onValueChange = {}
        )
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun DialogInputNumberBodyPreview() {
    IntervalTrainingTheme {
        Dialog(onDismissRequest = { /*TODO*/ }) {
            DialogInputNumberBody(
                labelTitle = "Title",
                value = 5,
                onValueSet = {}
            )
        }
    }
}


@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun DialogInputEnumBodyPreview() {
    IntervalTrainingTheme {
        Dialog(onDismissRequest = { /*TODO*/ }) {
            DialogInputEnumBody(
                labelTitle = "Title",
                selectedValue = "TWO",
                values = mapOf( Pair("ONE","One"), Pair("TWO","Two"), Pair("THREE","Three")),
                onValueChange = {}
            )
        }
    }
}