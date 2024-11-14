package com.medina.generation

import androidx.annotation.ArrayRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import com.medina.domain.data.model.Training
import com.medina.domain.data.model.Exercise
import com.medina.domain.data.model.ExerciseIcon

@Composable
fun suggestTraining(): Training = Training(
    name = randomTrainingName(),
    defaultTimeSec = 45,
    defaultRestSec = 15,
)
@Composable
fun stringRandom(@ArrayRes id: Int) = stringArrayResource(id = id).random()
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