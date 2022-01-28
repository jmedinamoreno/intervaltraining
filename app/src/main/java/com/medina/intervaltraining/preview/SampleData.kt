package com.medina.intervaltraining.preview

import com.medina.intervaltraining.viewmodel.Exercise
import com.medina.intervaltraining.viewmodel.ExerciseIcon


object SampleData {
    val exerciseTable = listOf<Exercise>(
        Exercise(
            name = "Run",
            icon = ExerciseIcon.RUN
        ),
        Exercise(
            name = "Jump",
            icon = ExerciseIcon.JUMP
        ),
        Exercise(
            name = "Sit ups asdasd ad adasd ada dasd asd ",
            icon = ExerciseIcon.SIT_UP
        ),
        Exercise(
            name = "None",
            icon = ExerciseIcon.NONE
        ),
    )
}