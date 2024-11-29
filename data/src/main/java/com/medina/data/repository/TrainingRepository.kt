package com.medina.data.repository

import androidx.annotation.WorkerThread
import com.medina.data.local.database.ExerciseDao
import com.medina.data.local.database.ExerciseItem
import com.medina.data.local.database.TrainingDao
import com.medina.data.local.database.TrainingItem
import com.medina.data.model.ExerciseIcon
import com.medina.data.model.Training
import com.medina.data.model.toTraining
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
    suspend fun insert(training: TrainingItem)
    suspend fun update(training: TrainingItem)
    suspend fun insert(exercise: ExerciseItem)
    suspend fun deleteTraining(training: UUID)
    suspend fun deleteAllExercises(training: UUID)
    suspend fun deleteExercise(exercise: UUID)
}

class TrainingDataRepository @Inject constructor(
    private val trainingDao: TrainingDao,
    private val exerciseDao: ExerciseDao,
): TrainingRepository {
    override val trainingsFlow: Flow<List<Training>> = trainingDao.getAllTrainingsAsFlow().map { it.map { trainingItem -> trainingItem.toTraining() } }

    override fun getTrainingFlow(uuid: UUID): Flow<TrainingItem?> = trainingDao.getTrainingAsFlow(uuid)

    override fun exercisesFlow(training: UUID) = exerciseDao.getExercisesForTrainingByIdAsFlow(training)

    @WorkerThread
    override suspend fun deleteTraining(training: UUID) = trainingDao.deleteTraining(training)

    @WorkerThread
    override suspend fun deleteAllExercises(training: UUID) = exerciseDao.deleteExercises(training)

    @WorkerThread
    override suspend fun deleteExercise(exercise: UUID) = exerciseDao.deleteExercise(exercise)

    @WorkerThread
    override suspend fun insert(training: TrainingItem) = trainingDao.insert(training)

    @WorkerThread
    override suspend fun update(training: TrainingItem) = trainingDao.update(training)

    @WorkerThread
    override suspend fun insert(exercise: ExerciseItem) = exerciseDao.insert(exercise)

}

class TrainingDummyRepository(numberOfTrainings:Int = 2, numberOfExercises:Int=2) : TrainingRepository {

    val trainingItems: List<TrainingItem> = (0 until numberOfTrainings).toList().map {
        TrainingItem(
            name = "Training ${it+1}",
            defaultTimeSec = 45,
            defaultRestSec = 15,
            lastUsed = 100000L+it
        )
    }
    private var trainingList: List<TrainingItem> = trainingItems
    private var dummyTraining = TrainingItem(
        name = "Example training",
        lastUsed = Date().time,
        defaultTimeSec = 45,
        defaultRestSec = 15,
    )

    private val dummyExercises: List<ExerciseItem> = (1 .. numberOfExercises).toList().map{ dummyExercise(it) }

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


    override fun getTrainingFlow(uuid: UUID): Flow<TrainingItem?> = flowOf(trainingList.getOrNull(0))

    override fun exercisesFlow(training: UUID): Flow<List<ExerciseItem>> = flowOf(dummyExercises)

    override suspend fun insert(training: TrainingItem) { }

    override suspend fun insert(exercise: ExerciseItem) { }

    override suspend fun update(training: TrainingItem) { }

    override suspend fun deleteTraining(training: UUID) { }

    override suspend fun deleteAllExercises(training: UUID) { }

    override suspend fun deleteExercise(exercise: UUID) { }

}