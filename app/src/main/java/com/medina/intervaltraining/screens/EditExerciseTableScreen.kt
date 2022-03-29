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
    val exerciseList = remember(items) {  mutableStateListOf<Exercise>() }
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
            // add TodoItemInputBackground and TodoItem at the top of TodoScreen
            ItemInputBackground(elevate = true, modifier = Modifier.fillMaxWidth()) {
                ExerciseItemInput( onItemComplete = {
                    exerciseList.add(it)
                    onExerciseListUpdated(exerciseList)
                })
            }
            LazyColumn {
                items(exerciseList) { exercise ->
                    Row() {
                        ExerciseLabel(exercise, Modifier.padding(2.dp))
                        IconButton(onClick = { },) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "#Edit"
                            )
                        }
                        IconButton(onClick = {
                            val pos = exerciseList.indexOf(exercise)
                            exerciseList.add(pos, exercise.copy())
                            onExerciseListUpdated(exerciseList)
                        },) {
                            Icon(
                                imageVector = Icons.Default.ControlPointDuplicate,
                                contentDescription = "#Duplicate"
                            )
                        }
                        IconButton(onClick = { },) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "#Sort"
                            )
                        }
                        IconButton(onClick = {
                             exerciseList.remove(exercise)
                            onExerciseListUpdated(exerciseList)
                        },) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "#Delete"
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun ExerciseItemInput(onItemComplete: (Exercise) -> Unit) {
    val (text, setText) = remember { mutableStateOf("") }
    val (icon, setIcon) = remember { mutableStateOf(ExerciseIcon.NONE) }
    val iconsVisible = text.isNotBlank()
    Column {
        Row(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            InputText(text,setText,
                Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            IconButton(
                onClick = {
                    onItemComplete(Exercise(text, icon))
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
            AnimatedIconRow(icon, setIcon, Modifier.padding(top = 8.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview
@Composable
fun PreviewTodoItemInput() = ExerciseItemInput(onItemComplete = { })

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