package com.medina.intervaltraining.ui

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import com.medina.intervaltraining.R
import com.medina.intervaltraining.data.model.ExerciseIcon
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Input a float with the number of hours
 * Output a string with the number of hours in hours and minutes
 */
@Composable
fun floatToHours(float: Float):String{
    val hours = float.roundToInt()
    val minutes = ((float - hours) * 60).toInt()
    return if(minutes==0){
        stringResource(id = R.string.label_hours,hours)
    }else {
        stringResource(id = R.string.label_hours_and_minutes, hours, minutes)
    }
}

fun iconToDrawableResource(icon: ExerciseIcon):Int = when(icon){
    ExerciseIcon.RUN -> R.drawable.ic_exercise_run
    ExerciseIcon.JUMP -> R.drawable.ic_exercise_jump
    ExerciseIcon.KNEES -> R.drawable.ic_exercise_knees
    ExerciseIcon.SIT_UP -> R.drawable.ic_exercise_sit_up
    ExerciseIcon.PUSH_UPS -> R.drawable.ic_exercise_push_ups
    ExerciseIcon.FLEX -> R.drawable.ic_exercise_flex
    ExerciseIcon.LEFT_ARM -> R.drawable.ic_exercise_left_arm
    ExerciseIcon.RIGHT_ARM -> R.drawable.ic_exercise_right_arm
    else -> R.drawable.ic_exercise_none
}

fun iconToStringResource(icon: ExerciseIcon):Int = when(icon){
    ExerciseIcon.RUN -> R.string.ic_name_exercise_run
    ExerciseIcon.JUMP -> R.string.ic_name_exercise_jump
    ExerciseIcon.KNEES -> R.string.ic_name_exercise_knees
    ExerciseIcon.SIT_UP -> R.string.ic_name_exercise_sit_up
    ExerciseIcon.PUSH_UPS -> R.string.ic_name_exercise_push_ups
    ExerciseIcon.FLEX -> R.string.ic_name_exercise_flex
    ExerciseIcon.LEFT_ARM -> R.string.ic_name_exercise_left
    ExerciseIcon.RIGHT_ARM -> R.string.ic_name_exercise_right
    else -> R.string.ic_name_exercise_none
}

val SemanticsKeyIconName = SemanticsPropertyKey<String>("IconName")
var SemanticsPropertyReceiver.iconName by SemanticsKeyIconName
val SemanticsKeyDrawableId = SemanticsPropertyKey<Int>("DrawableResId")
var SemanticsPropertyReceiver.drawableId by SemanticsKeyDrawableId

@Composable
fun stringForButtonDescription(@StringRes id: Int) =
    stringResource(id = R.string.button_description_template,stringResource(id = id))
@Composable
fun stringForIconDescription(@StringRes id: Int) =
    stringResource(id = R.string.icon_description_template,stringResource(id = id))

@Composable
fun stringRandom(@ArrayRes id: Int) = stringArrayResource(id = id).random()

@Composable
fun stringRandomSelected(@ArrayRes id: Int):String {
    val chosen by remember { mutableIntStateOf(Math.random().toInt()) }
    return stringChosen(id = id, chosen)
}

@Composable
fun stringChosen(@ArrayRes id: Int, chosen: Int) = stringArrayResource(id = id).let {
    it[chosen.absoluteValue%(it.size-1)]
}