package com.medina.intervaltraining.ui.screens

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.MediumTopAppBar
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.map
import com.medina.data.model.EmptyTraining
import com.medina.data.model.Exercise
import com.medina.data.model.ExerciseIcon
import com.medina.data.model.Training
import com.medina.intervaltraining.R
import com.medina.intervaltraining.ui.components.AnimatedIconRow
import com.medina.intervaltraining.ui.components.DialogIconButton
import com.medina.intervaltraining.ui.components.DraggableItem
import com.medina.intervaltraining.ui.components.ExerciseLabel
import com.medina.intervaltraining.ui.components.ExerciseTableIcon
import com.medina.intervaltraining.ui.components.InputNumber
import com.medina.intervaltraining.ui.components.InputText
import com.medina.intervaltraining.ui.components.NewInputText
import com.medina.intervaltraining.ui.components.rememberDragDropState
import com.medina.intervaltraining.ui.stringForButtonDescription
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import com.medina.intervaltraining.viewmodel.EditExercisesViewModel
import com.medina.intervaltraining.viewmodel.fakeExercisesViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun EditTrainingScreen(
    trainingId: UUID,
    editExercisesViewModel: EditExercisesViewModel = hiltViewModel<EditExercisesViewModel, EditExercisesViewModel.ViewModelFactory> { factory ->
        factory.create(trainingId)
    },
    onBack: () -> Unit,
    onDelete: () -> Unit,
) {
    EditExerciseTableView(
        editExercisesViewModel = editExercisesViewModel,
        onBack = onBack,
        onDeleteTraining = {
            editExercisesViewModel.deleteTraining()
            onDelete()
        },
        onUpdateTrainingName = {newName ->
            editExercisesViewModel.updateTrainingName(newName)
        },
        onUpdateExercise = { newExercise ->
            editExercisesViewModel.updateExercise(newExercise)
        },
        onDuplicateExercise = {index ->
            editExercisesViewModel.duplicateExercise(index)
        },
        onDeleteExercise = {index ->
            editExercisesViewModel.deleteExercise(index)
        },
        onMoveExercise = { oldIndex, toNewIndex ->
            editExercisesViewModel.moveExercise(oldIndex, toNewIndex)
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseTableScreenTopBar(
    editExercisesViewModel: EditExercisesViewModel,
    onSave: (String) -> Unit,
    onBack: () -> Unit,
    onDelete: () -> Unit
){
    val suggestedName: String = remember { editExercisesViewModel.suggestTrainingName() }
    val trainingTitle: String by editExercisesViewModel.training.map { it.name }.observeAsState("")
    val orientation = LocalConfiguration.current.orientation
    val composableTopBar: @Composable (title: @Composable () -> Unit,navigationIcon: @Composable () -> Unit, actions: @Composable RowScope.() -> Unit) -> Unit =
        if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            { title, navigationIcon, actions ->
                TopAppBar(
                    title = title,
                    navigationIcon = navigationIcon,
                    actions = actions
                )
            }
        }else{
            { title, navigationIcon, actions ->
                MediumTopAppBar(
                    title = title,
                    navigationIcon = navigationIcon,
                    actions = actions
                )
            }
        }
    composableTopBar({
            NewInputText(
                text = trainingTitle,
                placeholder = suggestedName,
                onTextChange = onSave,
                contentDescription = stringForButtonDescription(id = R.string.edit_training_name),
            )
        },{
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = null
                )
            }
        },{
            // RowScope here, so these icons will be placed horizontally
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringForButtonDescription(id = R.string.edit_training_delete),
                )
            }
        }
    )
}



@Composable
fun EditExerciseTableView(
    editExercisesViewModel: EditExercisesViewModel,
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

    val exerciseList: List<Exercise> by editExercisesViewModel.exercises.observeAsState(listOf())
    Scaffold(topBar = {
        EditExerciseTableScreenTopBar(
            editExercisesViewModel = editExercisesViewModel,
            onBack = onBack,
            onSave = onUpdateTrainingName,
            onDelete = onDeleteTraining
        )
    })
    { padding ->
        ExerciseTableEditableBody(
            modifier = Modifier.padding(padding),
            editExercisesViewModel = editExercisesViewModel,
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
                        currentExercise = exerciseToEdit,
                        onItemComplete = { newExercise ->
                            onUpdateExercise(newExercise)
                            dialogState.value = false
                        },
                        onSuggestExercise = {
                            editExercisesViewModel.suggestExercise()
                        },
                        onCancel = {dialogState.value = false
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
fun ExerciseTableEditableBody(
    editExercisesViewModel: EditExercisesViewModel,
    modifier: Modifier=Modifier,
    onNew:()->Unit = {},
    onEdit: (Int) -> Unit = {},
    onDuplicate: (Int) -> Unit = {},
    onRemove: (Int) -> Unit = {},
    onMove: (Int, Int) -> Unit = { _, _->},
){
    val exerciseList: List<Exercise> by editExercisesViewModel.exercises.observeAsState(listOf())
    val training: Training by editExercisesViewModel.training.observeAsState(EmptyTraining)
    val orientation = LocalConfiguration.current.orientation
    Box(modifier = modifier) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ExerciseTableEditableListHorizontal(
                modifier = Modifier,
                exerciseList = exerciseList,
                training = training,
                onNew = onNew,
                onEdit = onEdit,
                onDuplicate = onDuplicate,
                onRemove = onRemove,
                onMove = onMove,
            )
        }else{
            ExerciseTableEditableListVertical(
                modifier = Modifier,
                exerciseList = exerciseList,
                training = training,
                onNew = onNew,
                onEdit = onEdit,
                onDuplicate = onDuplicate,
                onRemove = onRemove,
                onMove = onMove,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseTableEditableListVertical(
    exerciseList: List<Exercise>,
    training: Training,
    modifier: Modifier = Modifier,
    onNew:()->Unit = {},
    onEdit: (Int) -> Unit = {},
    onDuplicate: (Int) -> Unit = {},
    onRemove: (Int) -> Unit = {},
    onMove: (Int, Int) -> Unit = { _, _->},
){
    val (selectedIndex, setSelectedIndex) = remember { mutableIntStateOf(-1) }
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
    val newTraining = training.name.isEmpty()
    Box(modifier = modifier.fillMaxSize()) {
        if(exerciseList.isNotEmpty()) {
            ExerciseTableHeader(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp),
                exercises = exerciseList,
            )
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier
                .padding(top = 48.dp, bottom = 0.dp)
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
                if(newTraining){
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
                                text = stringArrayResource(id = R.array.think_name_training_list).random(),
                            )
                        }
                    }
                }else{
                    itemsIndexed(
                        items = exerciseList,
                        key = { _: Int, exercise: Exercise -> exercise.id.toString() }
                    ) { index, exercise ->
                        DraggableItem(
                            dragDropState = dragDropState,
                            index = index
                        ) { isDragging ->
                            val pos = exerciseList.indexOf(exercise)
                            if (index == pos) {
                                EditExerciseTableItemView(
                                    modifier = Modifier
                                        .clickable(
                                            onClick = {
                                                if (pos == selectedIndex) {
                                                    onEdit(pos)
                                                } else {
                                                    setSelectedIndex(pos)
                                                }
                                            },
                                            onClickLabel = if (pos == selectedIndex) {
                                                stringForButtonDescription(id = R.string.edit_exercise_table_edit)
                                            } else {
                                                exercise.name
                                            }
                                        ),
                                    exercise = exercise,
                                    isSelected = pos == selectedIndex,
                                    shadowElevation = if (isDragging) 6.dp else 1.dp,
                                    onEdit = { onEdit(pos) },
                                    onDuplicate = { onDuplicate(pos) },
                                    onRemove = { onRemove(pos) }
                                )
                            }
                        }
                    }
                }
            }
        )
        if (!newTraining) {
            NewExerciseButton(
                modifier = Modifier
                    .align(
                        if (exerciseList.isEmpty()) {
                            Alignment.TopCenter
                        } else {
                            Alignment.BottomEnd
                        }
                    ),
                expanded = showFullButton,
                onClick = onNew,
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseTableEditableListHorizontal(
    exerciseList: List<Exercise>,
    training: Training,
    modifier: Modifier = Modifier,
    onNew:()->Unit = {},
    onEdit: (Int) -> Unit = {},
    onDuplicate: (Int) -> Unit = {},
    onRemove: (Int) -> Unit = {},
    onMove: (Int, Int) -> Unit = { _, _->},
){
    val (selectedIndex, setSelectedIndex) = remember { mutableIntStateOf(-1) }
    var overscrollJob by remember { mutableStateOf<Job?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val dragDropState = rememberDragDropState(listState,
        onMove = onMove,
        onHover = { _, _, _ ->
            setSelectedIndex(-1)
        }
    )
    val newTraining = training.name.isEmpty()
    Box(modifier = modifier.fillMaxSize()) {
        if(exerciseList.isNotEmpty()) {
            ExerciseTableHeader(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                exercises = exerciseList,
            )
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxWidth(if (newTraining) 1f else 0.7f)
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
                if(newTraining){
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
                                text = stringArrayResource(id = R.array.think_name_training_list).random(),
                            )
                        }
                    }
                }else{
                    itemsIndexed(
                        items = exerciseList,
                        key = { _: Int, exercise: Exercise -> exercise.id.toString() }
                    ) { index, exercise ->
                        DraggableItem(
                            dragDropState = dragDropState,
                            index = index
                        ) { isDragging ->
                            val pos = exerciseList.indexOf(exercise)
                            if (index == pos) {
                                EditExerciseTableItemView(
                                    modifier = Modifier
                                        .clickable(
                                            onClick = {
                                                if (pos == selectedIndex) {
                                                    onEdit(pos)
                                                } else {
                                                    setSelectedIndex(pos)
                                                }
                                            },
                                            onClickLabel = if (pos == selectedIndex) {
                                                stringForButtonDescription(id = R.string.edit_exercise_table_edit)
                                            } else {
                                                exercise.name
                                            }
                                        ),
                                    exercise = exercise,
                                    isSelected = pos == selectedIndex,
                                    shadowElevation = if (isDragging) 6.dp else 1.dp,
                                    onEdit = { onEdit(pos) },
                                    onDuplicate = { onDuplicate(pos) },
                                    onRemove = { onRemove(pos) }
                                )
                            }
                        }
                    }
                }
            }
        )
        if (!newTraining) {
            NewExerciseButton(
                modifier = Modifier
                    .align(
                        if (exerciseList.isEmpty()) {
                            Alignment.TopCenter
                        } else {
                            Alignment.CenterEnd
                        }
                    ),
                onClick = onNew,
            )
        }
    }
}

@Composable
fun NewExerciseButton(modifier: Modifier = Modifier, onClick: () -> Unit, expanded:Boolean=true) {
    ExtendedFloatingActionButton(
        modifier = modifier
            .height(82.dp)
            .padding(16.dp),
        expanded = expanded,
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = MaterialTheme.shapes.small,
        icon = {
            Icon(
                Icons.Default.Add,
                stringForButtonDescription(id = R.string.edit_exercise_table_new_exercise)
            )
        },
        text = { Text(text = stringResource(id = R.string.edit_exercise_table_new_exercise)) },
    )
}

@Composable
fun ExerciseTableHeader(modifier: Modifier, exercises: List<Exercise>) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "${exercises.size} exercises",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${(exercises.sumOf { it.timeSec+it.restSec }) / 60} min",
            style = MaterialTheme.typography.titleMedium,
        )
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
                .weight(0.1f),
        )
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
    onSuggestExercise: ()->Exercise,
    onCancel: ()->Unit){

    val (text, setText) = remember { mutableStateOf(currentExercise?.name ?: "") }
    val (icon, setIcon) = remember { mutableStateOf(currentExercise?.icon ?: ExerciseIcon.NONE) }
    val (time, setTime) = remember { mutableIntStateOf(currentExercise?.timeSec ?: 45) }
    val (rest, setRest) = remember { mutableIntStateOf(currentExercise?.restSec ?:15) }
    val (id, setId) = remember { mutableStateOf(currentExercise?.id ?:UUID.randomUUID()) }
    var iconsVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f),
        shape = MaterialTheme.shapes.extraLarge,
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
                        onSuggestExercise().let {
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
                    },
                ) {
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
            Row (
                Modifier
                    .padding(16.dp)
                    .height(48.dp)
            ){
                InputNumber(modifier = Modifier.weight(0.5f), value = time , onNumberChange = setTime)
                InputNumber(modifier = Modifier.weight(0.5f), value = rest , onNumberChange = setRest)
            }
            Row(
                Modifier
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DialogIconButton(
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(end = 4.dp),
                    text = stringResource(id = R.string.edit_exercise_dialog_cancel),
                    icon = Icons.Default.Clear,
                    iconDescription = stringForButtonDescription(id = R.string.edit_exercise_dialog_cancel),
                    enabled = text.isNotBlank(),
                    onClick = onCancel
                )
                DialogIconButton(
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(start = 4.dp),
                    text = stringResource(id = R.string.edit_exercise_dialog_save),
                    icon = Icons.Default.Check,
                    iconDescription = stringForButtonDescription(id = R.string.edit_exercise_dialog_save),
                    onClick = {
                        onItemComplete(
                            Exercise(
                                text,
                                icon,
                                time,
                                rest,
                                id = id
                            )
                        )
                    },
                )
            }
        }
    }
}

@Preview(name = "Light Mode", group = "Components")
@Preview(name = "Dark Mode", group = "Components", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewExerciseInputDialog(){
    IntervalTrainingTheme {
        EditExerciseDialogBody(
            onItemComplete = { },
            onSuggestExercise = { Exercise("Exercise") },
            onCancel = {}
        )
    }
}

@Preview(name = "Light Mode", group = "Components")
@Preview(name = "Dark Mode", group = "Components", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewExerciseListItem(){
    IntervalTrainingTheme {
        EditExerciseTableItemView(
            exercise = Exercise(name = "Exercise"),
            modifier = Modifier,
            isSelected = true,
            shadowElevation = 1.dp,
            onEdit = {},
            onDuplicate = {},
            onRemove = {}
        )
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "Components", widthDp = 360, heightDp = 160)
@Preview(name = "Dark Mode", group = "Components", widthDp = 360, heightDp = 160, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Light Mode", group = "Components", widthDp = 720, heightDp = 160, uiMode = Configuration.ORIENTATION_LANDSCAPE)
@Preview(name = "Dark Mode", group = "Components", widthDp = 720, heightDp = 160, uiMode = Configuration.ORIENTATION_LANDSCAPE or Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewEditorTopBar() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Scaffold(topBar = {
                EditExerciseTableScreenTopBar(
                    editExercisesViewModel = fakeExercisesViewModel(numberOfExercises = 0),
                    onBack = {  },
                    onSave = {},
                    onDelete = {}
                )
            }){ padding ->
                Box(modifier = Modifier.padding(padding),)
            }
        }
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "Vertical")
@Preview(name = "Dark Mode", group = "Vertical", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Light Mode", group = "Horizontal", widthDp = 720, heightDp = 260, uiMode = Configuration.ORIENTATION_LANDSCAPE)
@Preview(name = "Dark Mode", group = "Horizontal", widthDp = 720, heightDp = 260, uiMode = Configuration.ORIENTATION_LANDSCAPE or Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light Mode", group = "TabletHorizontal", widthDp = 720, heightDp = 460, uiMode = Configuration.ORIENTATION_LANDSCAPE)
@Preview(name = "Dark Mode", group = "TabletHorizontal", widthDp = 720, heightDp = 460, uiMode = Configuration.ORIENTATION_LANDSCAPE or Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditorPreviewNew() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            EditExerciseTableView(
                editExercisesViewModel = fakeExercisesViewModel(numberOfTrainings = 0, numberOfExercises = 0)
            )
        }
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "Vertical")
@Preview(name = "Dark Mode", group = "Vertical", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Light Mode", group = "Horizontal", widthDp = 720, heightDp = 260, uiMode = Configuration.ORIENTATION_LANDSCAPE)
@Preview(name = "Dark Mode", group = "Horizontal", widthDp = 720, heightDp = 260, uiMode = Configuration.ORIENTATION_LANDSCAPE or Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light Mode", group = "TabletHorizontal", widthDp = 720, heightDp = 460, uiMode = Configuration.ORIENTATION_LANDSCAPE)
@Preview(name = "Dark Mode", group = "TabletHorizontal", widthDp = 720, heightDp = 460, uiMode = Configuration.ORIENTATION_LANDSCAPE or Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditorPreviewEmpty() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            EditExerciseTableView(
                editExercisesViewModel = fakeExercisesViewModel(numberOfExercises = 0)
            )
        }
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "Vertical")
@Preview(name = "Dark Mode", group = "Vertical", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Light Mode", group = "Horizontal", widthDp = 720, heightDp = 260, uiMode = Configuration.ORIENTATION_LANDSCAPE)
@Preview(name = "Dark Mode", group = "Horizontal", widthDp = 720, heightDp = 260, uiMode = Configuration.ORIENTATION_LANDSCAPE or Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light Mode", group = "TabletHorizontal", widthDp = 720, heightDp = 460, uiMode = Configuration.ORIENTATION_LANDSCAPE)
@Preview(name = "Dark Mode", group = "TabletHorizontal", widthDp = 720, heightDp = 460, uiMode = Configuration.ORIENTATION_LANDSCAPE or Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditorPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            EditExerciseTableView(
                editExercisesViewModel = fakeExercisesViewModel(numberOfExercises = 6)
            )
        }
    }
}

@ExperimentalFoundationApi
@Preview(name = "Light Mode", group = "Vertical")
@Preview(name = "Dark Mode", group = "Vertical", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun EditorPreviewMany() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            EditExerciseTableView(
                editExercisesViewModel = fakeExercisesViewModel(numberOfExercises = 12)
            )
        }
    }
}