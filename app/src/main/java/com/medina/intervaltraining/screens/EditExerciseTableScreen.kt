package com.medina.intervaltraining.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medina.intervaltraining.data.viewmodel.Exercise
import com.medina.intervaltraining.data.viewmodel.ExerciseIcon
import com.medina.intervaltraining.data.viewmodel.ExerciseViewModel
import com.medina.intervaltraining.data.viewmodel.Training
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import java.util.*
import kotlin.collections.ArrayList

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
        Column() {
            if(training.name.isNotEmpty()) {
                ItemInputBackground(elevate = true, modifier = Modifier.fillMaxWidth()) {
                    ExerciseItemInput(onItemComplete = { newExercise ->
                        val pos = exerciseList.indexOfFirst { e -> e.id.equals(newExercise.id) }
                        if(pos>=0) {
                            exerciseList.set(pos,newExercise)
                        }else {
                            exerciseList.add(newExercise)
                        }
                        onExerciseListUpdated(exerciseList)
                        setIndex(-1)
                    }, exerciseList.getOrNull(currentIndex))
                    setIndex(-1)
                }
            }
            LazyColumn {
                items(exerciseList) { exercise ->
                    EditExerciseTableItemView(exercise = exercise,
                        onEdit = {
                            val pos = exerciseList.indexOf(exercise)
                            setIndex(pos)
                            onExerciseListUpdated(exerciseList)
                        },
                        onDuplicate = {
                            val pos = exerciseList.indexOf(exercise)
                            exerciseList.add(pos, exercise.copy())
                            onExerciseListUpdated(exerciseList)
                        },
                    onRemove = {
                        exerciseList.remove(exercise)
                        onExerciseListUpdated(exerciseList)
                    })
                }
            }
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
fun PreviewExerciseListItem() = EditExerciseTableItemView(Exercise("Exercise"),{},{},{})


@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true,)
@Composable
fun EditorPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colors.background) {
            EditExerciseTableView(training = Training("",0,0), items = emptyList())
        }
    }
}