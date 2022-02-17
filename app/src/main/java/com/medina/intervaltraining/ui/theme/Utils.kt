package com.medina.intervaltraining.ui.theme

import com.medina.intervaltraining.R
import com.medina.intervaltraining.data.viewmodel.ExerciseIcon

object Utils {
    fun iconToDrawableResource(icon: ExerciseIcon):Int = when(icon){
        ExerciseIcon.RUN -> R.drawable.ic_exercise_run
        ExerciseIcon.JUMP -> R.drawable.ic_exercise_jump
        else -> R.drawable.ic_exercise_none
    }
}