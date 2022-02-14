package com.medina.intervaltraining.preview

import com.medina.intervaltraining.viewmodel.Exercise
import com.medina.intervaltraining.viewmodel.ExerciseIcon
import com.medina.intervaltraining.viewmodel.Training


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
    val training = Training(45,"My training", exerciseTable)

    val trainingList = mutableListOf(
        Training(45,"My training 1", exerciseTable),
        Training(25,"My training 2", exerciseTable),
        Training(60,"My training 3", exerciseTable)
    )
}