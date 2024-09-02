package com.medina.intervaltraining.data.repository

import androidx.annotation.WorkerThread
import com.medina.intervaltraining.data.room.ExerciseItem
import com.medina.intervaltraining.data.room.TrainingDao
import com.medina.intervaltraining.data.room.TrainingItem
import com.medina.intervaltraining.data.model.ExerciseIcon
import com.medina.intervaltraining.data.model.Training
import com.medina.intervaltraining.data.model.toTraining
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject

interface TrainingRepository{

    val trainingsFlow: Flow<List<Training>>

    fun getTrainingFlow(uuid: UUID): Flow<TrainingItem?>

    fun exercisesFlow(training: UUID): Flow<List<ExerciseItem>>

    suspend fun exercises(training: UUID) : List<ExerciseItem>

    suspend fun insert(training: TrainingItem)

    suspend fun update(training: TrainingItem)

    suspend fun insert(exercise: ExerciseItem)

    suspend fun deleteTraining(training: UUID)

    suspend fun deleteAllExercises(training: UUID)

    suspend fun deleteExercise(exercise: UUID)
}

class TrainingRoomRepository  @Inject constructor(
    private val trainingDao: TrainingDao
):TrainingRepository{
    override val trainingsFlow: Flow<List<Training>> = trainingDao.getTrainingListAsFlow().map { it.map { trainingItem -> trainingItem.toTraining() } }

    override fun getTrainingFlow(uuid: UUID): Flow<TrainingItem?> = trainingDao.getTrainingAsFlow(uuid)

    override fun exercisesFlow(training: UUID) = trainingDao.getExercisesForTrainingByIdAsFlow(training)

    override suspend fun exercises(training: UUID) = trainingDao.getExercisesForTrainingById(training)

    override suspend fun deleteTraining(training: UUID) = trainingDao.deleteTraining(training)

    override suspend fun deleteAllExercises(training: UUID) = trainingDao.deleteExercises(training)

    override suspend fun deleteExercise(exercise: UUID) = trainingDao.deleteExercise(exercise)

    @WorkerThread
    override suspend fun insert(training: TrainingItem) = trainingDao.insert(training)

    @WorkerThread
    override suspend fun update(training: TrainingItem) = trainingDao.update(training)

    @WorkerThread
    override suspend fun insert(exercise: ExerciseItem) = trainingDao.insert(exercise)

}

class TrainingDummyRepository :TrainingRepository {

    val items: List<TrainingItem> = listOf(
        TrainingItem(name = "Training 1", defaultTimeSec = 45, defaultRestSec = 15, lastUsed = 100000),
        TrainingItem(name = "Training 2", defaultTimeSec = 45, defaultRestSec = 15, lastUsed = 100001),
    )
    private var trainingList: List<TrainingItem> = items
    private var dummyTraining = TrainingItem(
        name = "Example training",
        lastUsed = Date().time,
        defaultTimeSec = 45,
        defaultRestSec = 15,
    )

    private val dummyExercises: List<ExerciseItem> = (1 until 3).toList().map{ dummyExercise(it) }

    private fun dummyExercise(position:Int) = ExerciseItem(
        training = dummyTraining.id,
        name = "Exercise $position",
        icon = ExerciseIcon.RUN,
        position = position,
        timeSec = dummyTraining.defaultTimeSec,
        restSec = dummyTraining.defaultRestSec
    )

    override val trainingsFlow: Flow<List<Training>>
        get() = flowOf(trainingList).map { it.map { trainingItem -> trainingItem.toTraining() } }


    override fun getTrainingFlow(uuid: UUID): Flow<TrainingItem?> = flowOf(dummyTraining)

    override fun exercisesFlow(training: UUID): Flow<List<ExerciseItem>> = flowOf(dummyExercises)

    override suspend fun exercises(training: UUID): List<ExerciseItem> = dummyExercises

    override suspend fun insert(training: TrainingItem) { }

    override suspend fun insert(exercise: ExerciseItem) { }

    override suspend fun update(training: TrainingItem) { }

    override suspend fun deleteTraining(training: UUID) { }

    override suspend fun deleteAllExercises(training: UUID) { }

    override suspend fun deleteExercise(exercise: UUID) { }

}

