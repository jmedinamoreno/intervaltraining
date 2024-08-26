package com.medina.intervaltraining.data.generation

import androidx.annotation.ArrayRes
import androidx.compose.runtime.Composable
import com.medina.intervaltraining.R
import com.medina.intervaltraining.data.model.Training
import com.medina.intervaltraining.data.viewmodel.Exercise
import com.medina.intervaltraining.data.viewmodel.ExerciseIcon
import com.medina.intervaltraining.ui.stringRandom

@Composable
fun suggestTraining(): Training = Training(
    name = randomTrainingName(),
    defaultTimeSec = 45,
    defaultRestSec = 15,
)
@Composable
fun randomTrainingName():String = stringRandom(id = R.array.suggested_training_name_list)
@Composable
fun suggestExercise(): Exercise = predefinedTrainings.random().let {
    Exercise(
        name = stringRandom(id = it.nameListId),
        icon = it.icon,
        timeSec = 45,
        restSec = 15
    )
}

data class PredefinedTraining(@ArrayRes val nameListId: Int, val icon: ExerciseIcon)
private val predefinedTrainings = listOf(
    PredefinedTraining(R.array.suggested_exercise_knees_like_list, ExerciseIcon.KNEES),
    PredefinedTraining(R.array.suggested_exercise_plank_like_list, ExerciseIcon.PUSH_UPS),
    PredefinedTraining(R.array.suggested_exercise_lunges_like_list, ExerciseIcon.NONE),
    PredefinedTraining(R.array.suggested_exercise_squats_like_list, ExerciseIcon.SIT_UP),
    PredefinedTraining(R.array.suggested_exercise_crunches_like_list, ExerciseIcon.FLEX),
    PredefinedTraining(R.array.suggested_exercise_burpees_like_list, ExerciseIcon.JUMP),
)