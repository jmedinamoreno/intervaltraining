package com.medina.intervaltraining.data.repository

import androidx.annotation.WorkerThread
import com.medina.intervaltraining.data.room.ExerciseItem
import com.medina.intervaltraining.data.room.TrainingDao
import com.medina.intervaltraining.data.room.TrainingItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

interface TrainingRepository{

    val trainingsFlow: Flow<List<TrainingItem>>

    fun timeForTrainingMinAsFlow(training: UUID): Flow<Int>

    suspend fun exercises(training: UUID) : List<ExerciseItem>

    suspend fun timeForTrainingMin(training: UUID):Int

    suspend fun insert(training: TrainingItem)

    suspend fun insert(exercise: ExerciseItem)

    suspend fun getTraining(uuid: UUID): TrainingItem
}

class TrainingRoomRepository(
    private val trainingDao: TrainingDao
):TrainingRepository{
    override val trainingsFlow: Flow<List<TrainingItem>> = trainingDao.getTrainingListAsFlow()

    override fun timeForTrainingMinAsFlow(training: UUID) = trainingDao.getTotalTimeSecForTrainingByIdAsFlow(training).map { it/60 }

    override suspend fun exercises(training: UUID) = trainingDao.getExercisesForTrainingById(training)

    override suspend fun timeForTrainingMin(training: UUID) = trainingDao.getTotalTimeSecForTrainingById(training) / 60

    override suspend fun getTraining(uuid: UUID): TrainingItem = trainingDao.getTraining(uuid)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insert(training: TrainingItem) = trainingDao.insert(training)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insert(exercise: ExerciseItem) = trainingDao.insert(exercise)
}

class TrainingDummyRepository():TrainingRepository{
    override val trainingsFlow: Flow<List<TrainingItem>>
        get() = flow {
            emit(listOf(
                TrainingItem(name = "Training 1", lastUsed = 0, defaultTimeSec = 45, defaultRestSec = 15),
                TrainingItem(name = "Training 2", lastUsed = 0, defaultTimeSec = 45, defaultRestSec = 15),
            ))
        }

    override fun timeForTrainingMinAsFlow(training: UUID): Flow<Int> = flow {
        emit(45)
    }

    override suspend fun exercises(training: UUID): List<ExerciseItem> = listOf(
        ExerciseItem(training = UUID.randomUUID(), name = "Exercise 1", timeSec = 45, restSec = 15, position = 0),
        ExerciseItem(training = UUID.randomUUID(), name = "Exercise 2", timeSec = 45, restSec = 15, position = 1),
        ExerciseItem(training = UUID.randomUUID(), name = "Exercise 3", timeSec = 45, restSec = 15, position = 2),
    )

    override suspend fun timeForTrainingMin(training: UUID): Int = 45

    override suspend fun insert(training: TrainingItem) {
    }

    override suspend fun insert(exercise: ExerciseItem) {
    }

    override suspend fun getTraining(uuid: UUID): TrainingItem = TrainingItem(
        name = "Dummy",
        defaultRestSec = 15,
        defaultTimeSec = 45,
        lastUsed = Date().time
    )

}