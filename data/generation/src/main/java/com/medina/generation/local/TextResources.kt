package com.medina.generation.local

import android.content.Context
import androidx.annotation.ArrayRes
import com.medina.data.model.ExerciseIcon
import com.medina.generation.R

class TextResources(context: Context) {
    private val resources = context.resources

    val trainingNames: List<String> = stringArrayResource(id = R.array.training_name_hint_list).toList()

    fun randomTrainingName():String = stringRandom(id = R.array.suggested_training_name_list)

    val predefinedExercises = listOf(
        R.array.suggested_exercise_knees_like_list to ExerciseIcon.KNEES,
        R.array.suggested_exercise_plank_like_list to ExerciseIcon.PUSH_UPS,
        R.array.suggested_exercise_lunges_like_list to ExerciseIcon.NONE,
        R.array.suggested_exercise_squats_like_list to ExerciseIcon.SIT_UP,
        R.array.suggested_exercise_crunches_like_list to ExerciseIcon.FLEX,
        R.array.suggested_exercise_burpees_like_list to ExerciseIcon.JUMP
    ).map { (id,icon) -> stringArrayResource(id) to icon }

    private fun stringRandom(@ArrayRes id: Int) = stringArrayResource(id = id).random()

    private fun stringArrayResource(@ArrayRes id: Int): Array<String> {
        return resources.getStringArray(id)
    }

}