package com.medina.domain.data.model

import com.medina.domain.data.room.ExerciseItem
import com.medina.domain.data.room.TrainingItem
import java.util.UUID

fun TrainingItem.toTraining() = Training(
    id = id,
    defaultTimeSec = defaultTimeSec,
    defaultRestSec = defaultRestSec,
    name = name
)

fun Training.toTrainingItem(lastUsed:Long): TrainingItem {
    return TrainingItem(
        id = id,
        defaultTimeSec = defaultTimeSec,
        defaultRestSec = defaultRestSec,
        name = name,
        lastUsed = lastUsed
    )
}

fun Exercise.toExerciseItem(trainingId: UUID, position:Int) = ExerciseItem(
    id = id,
    training = trainingId,
    name = name,
    icon = icon,
    restSec = restSec,
    timeSec = timeSec,
    position = position
)

fun ExerciseItem.toExercise() = Exercise(
    name = name,
    icon = icon,
    timeSec = timeSec,
    restSec = restSec,
    id = id,
)