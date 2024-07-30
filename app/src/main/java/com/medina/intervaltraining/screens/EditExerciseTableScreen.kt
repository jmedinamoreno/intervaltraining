package com.medina.intervaltraining.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.medina.intervaltraining.R
import com.medina.intervaltraining.data.generation.suggestExercise
import com.medina.intervaltraining.data.viewmodel.Exercise
import com.medina.intervaltraining.data.viewmodel.ExerciseIcon
import com.medina.intervaltraining.data.viewmodel.ExerciseViewModel
import com.medina.intervaltraining.data.viewmodel.Training
import com.medina.intervaltraining.ui.components.InputNumber
import com.medina.intervaltraining.ui.components.InputText
import com.medina.intervaltraining.ui.components.SavableInputText
import com.medina.intervaltraining.ui.dragdroplist.DraggableItem
import com.medina.intervaltraining.ui.dragdroplist.rememberDragDropState
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import com.medina.intervaltraining.ui.stringForButtonDescription
import com.medina.intervaltraining.ui.stringRandom
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseTableScreenTopBar(trainingTitle: String, onSave:(String)->Unit, onBack:()->Unit, onDelete:()->Unit ){
    TopAppBar(
        title = {
            SavableInputText(
                entryText = trainingTitle,
                onSave = onSave,
                timeoutMill = 2000,
                placeholder = stringRandom(id = R.array.training_name_hint_list)
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            // RowScope here, so these icons will be placed horizontally
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringForButtonDescription(id = R.string.edit_training_delete))
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
    val exerciseList: List<Exercise> by exerciseViewModel.exercises.observeAsState(listOf())
    val training: Training by exerciseViewModel.training.observeAsState(Training("x", 0, 0))
    EditExerciseTableView(
        training = training,
        exerciseList = exerciseList,
        onBack = onBack,
        onDeleteTraining = onDelete,
        onUpdateTrainingName = {newName ->
            if(newName.isNotBlank() && newName != training.name) {
                training.name = newName
                onUpdateTraining(training)
            }
        },
        onUpdateExercise = { newExercise ->
            val newList = exerciseList.toMutableList()
            val pos = exerciseList.indexOfFirst { e -> e.id == newExercise.id }
            if (pos >= 0) {
                newList[pos] = newExercise
            }else{
                newList.add(newExercise)
            }
            onExerciseListUpdated(newList)
        },
        onDuplicateExercise = {index ->
            val newList = exerciseList.toMutableList()
            newList.add(index+1, newList[index].newCopy())
            onExerciseListUpdated(newList)
        },
        onDeleteExercise = {index ->
            val newList = exerciseList.toMutableList()
            newList.removeAt(index)
            onExerciseListUpdated(newList)
        },
        onMoveExercise = { oldIndex, toNewIndex ->
            val newList = exerciseList.toMutableList()
            val exercise = newList[oldIndex]
            newList.removeAt(oldIndex)
            newList.add(toNewIndex.coerceAtMost(newList.size-1),exercise)
            onExerciseListUpdated(newList)
        }
    )
}


@Composable
fun EditExerciseTableView(
    training: Training,
    exerciseList: List<Exercise>,
    onBack: () -> Unit = {},
    onDeleteTraining: () -> Unit = {},
    onUpdateTrainingName: (trainingName: String) -> Unit = {},
    onUpdateExercise:(Exercise)->Unit = {},
    onDeleteExercise:(Int)->Unit = {},
    onDuplicateExercise:(Int)->Unit = {},
    onMoveExercise:(Int, Int)->Unit = { _, _ ->},
) {
    // Dialog state Manager
    val dialogState: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }
    val dialogExercise: MutableState<Exercise?> = remember {
        mutableStateOf(null)
    }
    Scaffold(topBar = {
        EditExerciseTableScreenTopBar(trainingTitle = training.name, onBack = onBack,
            onSave = onUpdateTrainingName,
            onDelete = onDeleteTraining)
    })
    { padding ->
        ExerciseTableEditableList(
            modifier = Modifier.padding(padding),
            items = exerciseList,
            training = training,
            onEdit = {
                dialogState.value = true
                dialogExercise.value = exerciseList.getOrNull(it)
            },
            onDuplicate = onDuplicateExercise,
            onRemove = onDeleteExercise,
            onMove = onMoveExercise,
            onNew = { dialogState.value = true }
        )
        if (dialogState.value) {
            Dialog(
                onDismissRequest = { dialogState.value = false },
                content = {
                    val exerciseToEdit = dialogExercise.value
                    dialogExercise.value = null
                    EditExerciseDialogBody(
                        exerciseToEdit,
                        onItemComplete = { newExercise ->
                            onUpdateExercise(newExercise)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseTableEditableList(
    modifier: Modifier=Modifier,
    training: Training,
    items: List<Exercise>,
    onNew:()->Unit = {},
    onEdit: (Int) -> Unit = {},
    onDuplicate: (Int) -> Unit = {},
    onRemove: (Int) -> Unit = {},
    onMove: (Int, Int) -> Unit = { _, _->},
){
    val (selectedIndex, setSelectedIndex) = remember { mutableStateOf(-1) }
    var overscrollJob by remember { mutableStateOf<Job?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val dragDropState = rememberDragDropState(listState,
        onMove = onMove,
        onHover = { _, _, _ ->
            setSelectedIndex(-1)
        }
    )
    val showFullButton by remember {
        derivedStateOf {
            listState.firstVisibleItemScrollOffset <= 0
        }
    }
    Box(modifier = modifier) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .pointerInput(dragDropState) {
                    detectDragGesturesAfterLongPress(
                        onDrag = { change, offset ->
                            change.consume()
                            dragDropState.onDrag(offset = offset)
                            if (overscrollJob?.isActive == true) return@detectDragGesturesAfterLongPress

                            dragDropState
                                .checkForOverScroll()
                                .takeIf { it != 0f }
                                ?.let {
                                    overscrollJob =
                                        scope.launch {
                                            dragDropState.state.animateScrollBy(
                                                it * 1.3f, tween(easing = FastOutLinearInEasing)
                                            )
                                        }
                                }
                                ?: run { overscrollJob?.cancel() }
                        },
                        onDragStart = { offset ->
                            dragDropState.onDragStart(offset)
                        },
                        onDragEnd = {
                            dragDropState.onDragInterrupted()
                            overscrollJob?.cancel()
                        },
                        onDragCancel = {
                            dragDropState.onDragInterrupted()
                            overscrollJob?.cancel()
                        }
                    )
                },
            state = listState,
            content = {
                if (training.name.isNotEmpty()) {
                    itemsIndexed(
                        items = items,
                        key = {_: Int, exercise: Exercise -> exercise.id.toString() }
                    ) {  index, exercise ->
                        DraggableItem(
                            dragDropState = dragDropState,
                            index = index
                        ) { isDragging ->
                            val pos = items.indexOf(exercise)
                            if(index==pos) {
                                EditExerciseTableItemView(
                                    modifier = Modifier.clickable { setSelectedIndex(pos) },
                                    exercise = exercise,
                                    isSelected = pos == selectedIndex,
                                    shadowElevation = if(isDragging) 6.dp else 1.dp,
                                    onEdit = { onEdit(pos) },
                                    onDuplicate = { onDuplicate(pos) },
                                    onRemove = { onRemove(pos) }
                                )
                            }
                        }
                    }
                    item {
                        Box(
                            Modifier
                                .fillParentMaxWidth()
                                .height(82.dp)) {

                        }
                    }
                } else {
                    item {
                        Box(
                            Modifier
                                .fillParentMaxWidth()
                                .padding(32.dp)
                        ) {
                            Text(
                                modifier = Modifier
                                    .align(Alignment.TopCenter),
                                style = MaterialTheme.typography.bodyLarge,
                                text = stringRandom(id = R.array.think_name_training_list),
                            )
                        }
                    }
                }
            }
        )
        if(training.name.isNotEmpty()) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(82.dp)
                    .padding(16.dp)
            ) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.align(
                        if (items.isEmpty()) {
                            Alignment.Center
                        } else {
                            Alignment.CenterEnd
                        }
                    ),
                    expanded = showFullButton,
                    onClick = onNew,
                    icon = { Icon(Icons.Default.Add, stringForButtonDescription(id = R.string.edit_exercise_table_new_exercise))},
                    text = { Text(text = stringResource(id = R.string.edit_exercise_table_new_exercise)) },
                )
            }
        }
    }
}

@Composable
fun EditExerciseTableItemView(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    isSelected:Boolean = false,
    shadowElevation: Dp = 1.dp,
    onEdit:()->Unit,
    onDuplicate:()->Unit,
    onRemove:()->Unit
){
    Row(modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        ExerciseLabel(
            exercise = exercise,
            shadowElevation = shadowElevation,
            modifier = Modifier
                .padding(2.dp)
                .weight(0.1f))
        AnimatedVisibility(visible = isSelected) {
            Row(modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringForButtonDescription(id = R.string.edit_exercise_table_edit)
                    )
                }
                IconButton(onClick = onDuplicate) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringForButtonDescription(id = R.string.edit_exercise_table_duplicate)
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringForButtonDescription(id = R.string.edit_exercise_table_delete)
                    )
                }
            }
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
    val (time, setTime) = remember { mutableIntStateOf(45) }
    val (rest, setRest) = remember { mutableIntStateOf(15) }
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
    val suggestedExercise = suggestExercise()

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            if(text.isBlank()){
                DialogIconButton(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    text = stringResource(id = R.string.edit_exercise_dialog_suggest),
                    icon = Icons.Default.Star,
                    iconDescription = stringForButtonDescription(id = R.string.edit_exercise_dialog_suggest),
                    onClick = {
                        suggestedExercise.let {
                            setText(it.name)
                            setIcon(it.icon)
                            setTime(it.timeSec)
                            setRest(it.restSec)
                            setId(it.id)
                        }
                    }
                )
            }
            Row(
                Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
                    .height(54.dp)
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = {
                        iconsVisible = true
                    },) {
                    ExerciseTableIcon(icon = icon, MaterialTheme.colorScheme.primary)
                }
                InputText(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    text = text,
                    onTextChange = setText,
                    placeholder = stringResource(id = R.string.new_exercise_name_hint)
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
                DialogIconButton(
                    modifier = Modifier.weight(0.5f).padding(end =4.dp),
                    text = stringResource(id = R.string.edit_exercise_dialog_cancel),
                    icon = Icons.Default.Clear,
                    iconDescription = stringForButtonDescription(id = R.string.edit_exercise_dialog_cancel),
                    enabled = text.isNotBlank(),
                    onClick = onCancel
                )
                DialogIconButton(
                    modifier = Modifier.weight(0.5f).padding(start = 4.dp),
                    text = stringResource(id = R.string.edit_exercise_dialog_save),
                    icon = Icons.Default.Check,
                    iconDescription = stringForButtonDescription(id = R.string.edit_exercise_dialog_save),
                    onClick = {
                        onItemComplete(Exercise(text, icon, time, rest, id = id))
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewExerciseInputDialog() = EditExerciseDialogBody(onItemComplete = { }, onCancel = {})


@Preview
@Composable
fun PreviewExerciseListItem() = EditExerciseTableItemView(Exercise("Exercise"), Modifier, true, 1.dp,{},{},{})

@Preview
@Composable
fun EditorPreviewNew() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            EditExerciseTableView(
                training = Training("",0,0),
                exerciseList = emptyList()
            )
        }
    }
}

@Preview
@Composable
fun EditorPreviewEmpty() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            EditExerciseTableView(
                training = Training("Test",0,0),
                exerciseList = emptyList()
            )
        }
    }
}

@Preview
@Composable
fun EditorPreviewSmall() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            EditExerciseTableView(
                training = Training("Test",0,0),
                exerciseList = (1 until 3).toList().map{Exercise("Exercise $it")})
        }
    }
}


@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun EditorPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            EditExerciseTableView(
                training = Training("Test",0,0),
                exerciseList = (1 until 15).toList().map{Exercise("Exercise $it")})
        }
    }
}