package com.medina.intervaltraining.ui.theme

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.medina.intervaltraining.R
import com.medina.intervaltraining.data.viewmodel.ExerciseIcon

object Utils {
    fun iconToDrawableResource(icon: ExerciseIcon):Int = when(icon){
        ExerciseIcon.RUN -> R.drawable.ic_exercise_run
        ExerciseIcon.JUMP -> R.drawable.ic_exercise_jump
        else -> R.drawable.ic_exercise_none
    }
}

@Composable
fun stringForButtonDescription(@StringRes id: Int) =
    stringResource(id = R.string.button_description_template,stringResource(id = id))
@Composable
fun stringForIconDescription(@StringRes id: Int) =
    stringResource(id = R.string.icon_description_template,stringResource(id = id))

@Composable
fun stringRandom(@ArrayRes id: Int) = stringArrayResource(id = id).random()