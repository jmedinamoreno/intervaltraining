package com.medina.intervaltraining.screens

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.medina.intervaltraining.data.viewmodel.Exercise
import com.medina.intervaltraining.data.viewmodel.ExerciseIcon
import com.medina.intervaltraining.data.viewmodel.ExerciseViewModel
import com.medina.intervaltraining.data.viewmodel.Training
import com.medina.intervaltraining.ui.components.ContentAwareLazyColumn
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import java.util.*

@Composable
fun EditExerciseTableScreenTopBar(trainingTitle: String, onSave:(String)->Unit, onBack:()->Unit, onDelete:()->Unit ){
    TopAppBar(
        title = {
            SavableInputText(
                entryText = trainingTitle,
                onSave = onSave,
                timeoutMill = 2000,
                placeholder = "#NewTrainingName"
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            // RowScope here, so these icons will be placed horizontally
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "#Delete training")
            }
        }
    )
}

@Composable
fun EditExerciseTableScreen(
    exerciseViewModel: ExerciseViewModel,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onUpdateTraining: (newTraining: Training) -> Unit,
    onExerciseListUpdated: (newItems: List<Exercise>) -> Unit = {},
) {
    val items: List<Exercise> by exerciseViewModel.exercises.observeAsState(listOf())
    val training: Training by exerciseViewModel.training.observeAsState(Training("", 0, 0))
    EditExerciseTableView(training, items, onBack, onDelete, onUpdateTraining, onExerciseListUpdated)
}

@Composable
private fun EditExerciseTableView(
    training: Training,
    items: List<Exercise>,
    onBack: () -> Unit = {},
    onDelete: () -> Unit = {},
    onUpdateTraining: (newTraining: Training) -> Unit = {},
    onExerciseListUpdated: (newItems: List<Exercise>) -> Unit = {},
) {
    val exerciseList = ArrayList(items)
    val (currentIndex, setIndex) = remember { mutableStateOf(-1) }

    // Dialog state Manager
    val dialogState: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }
    val dialogExercise: MutableState<Exercise?> = remember {
        mutableStateOf(null)
    }

    Scaffold(topBar = {
        EditExerciseTableScreenTopBar(trainingTitle = training.name, onBack = onBack,
            onSave = {
                if(it.isNotBlank()) {
                    training.name = it
                    onUpdateTraining(training)
                }
             },
            onDelete = onDelete)
    })
    {
        ContentAwareLazyColumn(
            lazyColumnBody = {
                items(exerciseList) { exercise ->
                    val pos = exerciseList.indexOf(exercise)
                    if (pos == currentIndex) {
                        EditExerciseTableItemView(exercise = exercise,
                            onEdit = {
                                dialogState.value = true
                                dialogExercise.value = exercise
                            },
                            onDuplicate = {
                                exerciseList.add(pos, exercise.copy())
                                onExerciseListUpdated(exerciseList)
                            },
                            onRemove = {
                                exerciseList.remove(exercise)
                                onExerciseListUpdated(exerciseList)
                            })
                    } else {
                        ExerciseLabel(
                            exercise = exercise,
                            modifier = Modifier
                                .clickable { setIndex(pos) }
                                .padding(2.dp)
                        )
                    }
                }
            }, footerContent = {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { dialogState.value = true }) {
                    Text(text = "#AddExercise")
                }
            }, showFooter = training.name.isNotEmpty())
        if (dialogState.value) {
            Dialog(
                onDismissRequest = { dialogState.value = false },
                content = {
                    val exerciseToEdit = dialogExercise.value
                    dialogExercise.value = null
                    EditExerciseDialogBody(
                        exerciseToEdit,
                        onItemComplete = { newExercise ->
                        val pos = exerciseList.indexOfFirst { e -> e.id.equals(newExercise.id) }
                        if (pos >= 0) {
                            exerciseList.set(pos, newExercise)
                        } else {
                            exerciseList.add(newExercise)
                        }
                        onExerciseListUpdated(exerciseList)
                        setIndex(-1)
                        dialogState.value = false
                    },
                    {
                        dialogState.value = false
                    })
                },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            )
        }
    }
}

@Composable
fun EditExerciseTableItemView(exercise: Exercise, onEdit:()->Unit, onDuplicate:()->Unit, onRemove:()->Unit){
    Row() {
        ExerciseLabel(exercise,
            Modifier
                .padding(2.dp)
                .weight(0.1f))
        IconButton(onClick = onEdit,) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "#Edit"
            )
        }
        IconButton(onClick = onDuplicate,) {
            Icon(
                imageVector = Icons.Default.FileCopy,
                contentDescription = "#Duplicate"
            )
        }
        IconButton(onClick = onRemove,) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "#Delete"
            )
        }
    }
}


@Composable
fun EditExerciseDialogBody(
    currentExercise: Exercise? = null,
    onItemComplete: (Exercise) -> Unit,
    onCancel: ()->Unit){

    val (text, setText) = remember { mutableStateOf("") }
    val (icon, setIcon) = remember { mutableStateOf(ExerciseIcon.NONE) }
    val (time, setTime) = remember { mutableStateOf(45) }
    val (rest, setRest) = remember { mutableStateOf(15) }
    val (id, setId) = remember { mutableStateOf(UUID.randomUUID()) }
    var iconsVisible by remember { mutableStateOf(false) }
    val timesVisible = true //text.isNotBlank()

    if(currentExercise!=null){
        setText(currentExercise.name)
        setIcon(currentExercise.icon)
        setTime(currentExercise.timeSec)
        setRest(currentExercise.restSec)
        setId(currentExercise.id)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            Row(
                Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
                    .height(54.dp)
            ) {
                IconButton(
                    onClick = {
                        iconsVisible = true
                    },) {
                    ExerciseTableIcon(icon = icon, MaterialTheme.colors.primary)
                }
                InputText(text,setText,
                    Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
            }
            if (iconsVisible) {
                AnimatedIconRow(icon, { setIcon(it); iconsVisible=false; },
                    Modifier
                        .padding(top = 8.dp)
                        .height(48.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (timesVisible) {
                Row (
                    Modifier
                        .padding(16.dp)
                        .height(48.dp)
                ){
                    InputNumber(modifier = Modifier.weight(0.5f), value = time , onNumberChange = setTime)
                    InputNumber(modifier = Modifier.weight(0.5f), value = rest , onNumberChange = setRest)
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
            Row(
                Modifier
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(modifier = Modifier.weight(0.5f), onClick = onCancel) {
                    Row {
                       Text(text = "#Cancel")
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "#Cancel"
                        )
                    }
                }
                Button( modifier = Modifier.weight(0.5f),
                    onClick = {
                        onItemComplete(Exercise(text, icon, time, rest, id = id))
                    },
                    enabled = text.isNotBlank(),
                ) {
                    Row {
                        Text(text = "#Save")
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "#Save"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseItemInput(onItemComplete: (Exercise) -> Unit, currentExercise: Exercise? = null) {
    val (text, setText) = remember { mutableStateOf("") }
    val (icon, setIcon) = remember { mutableStateOf(ExerciseIcon.NONE) }
    val (time, setTime) = remember { mutableStateOf(45) }
    val (rest, setRest) = remember { mutableStateOf(15) }
    val (id, setId) = remember { mutableStateOf(UUID.randomUUID()) }
    var iconsVisible by remember { mutableStateOf(false) }
    val timesVisible = true //text.isNotBlank()

    if(currentExercise!=null){
        setText(currentExercise.name)
        setIcon(currentExercise.icon)
        setTime(currentExercise.timeSec)
        setRest(currentExercise.restSec)
        setId(currentExercise.id)
    }

    Column {
        Row(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .height(54.dp)
        ) {
            IconButton(
                onClick = {
                    iconsVisible = true
                },) {
                ExerciseTableIcon(icon = icon, MaterialTheme.colors.primary)
            }
            InputText(text,setText,
                Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            IconButton(
                onClick = {
                    onItemComplete(Exercise(text, icon, time, rest, id = id))
                    setText("")
                },
                enabled = text.isNotBlank(),) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "#Save"
                )
            }
        }
        if (iconsVisible) {
            AnimatedIconRow(icon, { setIcon(it); iconsVisible=false; },
                Modifier
                    .padding(top = 8.dp)
                    .height(48.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (timesVisible) {
            Row (
                Modifier
                    .padding(16.dp)
                    .height(48.dp)
            ){
                InputNumber(modifier = Modifier.weight(0.5f), value = time , onNumberChange = setTime)
                InputNumber(modifier = Modifier.weight(0.5f), value = rest , onNumberChange = setRest)
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview
@Composable
fun PreviewTodoItemInput() = ExerciseItemInput(onItemComplete = { })


@Preview
@Composable
fun PreviewExerciseInputDialog() = EditExerciseDialogBody(onItemComplete = { }, onCancel = {})


@Preview
@Composable
fun PreviewExerciseListItem() = EditExerciseTableItemView(Exercise("Exercise"),{},{},{})


@ExperimentalMaterialApi
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true,)
@Composable
fun EditorPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colors.background) {
            EditExerciseTableView(training = Training("Test",0,0), items = listOf(Exercise("Exercise 1"), Exercise("Exercise 2")))
        }
    }
}