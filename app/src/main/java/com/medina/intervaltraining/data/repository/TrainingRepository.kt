package com.medina.intervaltraining.data.repository

import androidx.annotation.WorkerThread
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.medina.intervaltraining.data.room.ExerciseItem
import com.medina.intervaltraining.data.room.SessionItem
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

    fun getTrainingFlow(uuid: UUID): Flow<TrainingItem?>

    fun exercisesFlow(training: UUID): Flow<List<ExerciseItem>>

    suspend fun exercises(training: UUID) : List<ExerciseItem>

    suspend fun insert(training: TrainingItem)

    suspend fun insert(exercise: ExerciseItem)

    suspend fun insert(session: SessionItem)

    suspend fun deleteTraining(training: UUID)

    suspend fun deleteAllExercises(training: UUID)

    suspend fun deleteExercise(exercise: UUID)

    suspend fun deleteSession(session: UUID)

    fun getTotalSessionTimeSecForTrainingById(id: UUID): Flow<Float>

    fun getTotalSessionTimeSecForDaterange(startDatetime: Long, endDatetime: Long): Flow<Float>
}
class TrainingRoomRepository(
    private val trainingDao: TrainingDao
):TrainingRepository{
    override val trainingsFlow: Flow<List<TrainingItem>> = trainingDao.getTrainingListAsFlow()

    override fun timeForTrainingMinAsFlow(training: UUID) = trainingDao.getTotalTimeSecForTrainingByIdAsFlow(training).map { (it?:0)/60 }

    override fun getTrainingFlow(uuid: UUID): Flow<TrainingItem?> = trainingDao.getTrainingAsFlow(uuid)

    override fun exercisesFlow(training: UUID) = trainingDao.getExercisesForTrainingByIdAsFlow(training)

    override suspend fun exercises(training: UUID) = trainingDao.getExercisesForTrainingById(training)

    override suspend fun deleteTraining(training: UUID) = trainingDao.deleteTraining(training)

    override suspend fun deleteAllExercises(training: UUID) = trainingDao.deleteExercises(training)

    override suspend fun deleteExercise(exercise: UUID) = trainingDao.deleteExercise(exercise)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insert(training: TrainingItem) = trainingDao.insert(training)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insert(exercise: ExerciseItem) = trainingDao.insert(exercise)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insert(session: SessionItem) = trainingDao.insert(session)

    override suspend fun deleteSession(session: UUID) = trainingDao.deleteSession(session)

    override fun getTotalSessionTimeSecForTrainingById(id: UUID) =
        trainingDao.getTotalSessionTimeMilsForTrainingByIdAsFlow(id).map { (it?:0) / 1000f }

    override fun getTotalSessionTimeSecForDaterange(startDatetime: Long, endDatetime: Long) =
        trainingDao.getTotalSessionTimeMilsForDaterangeAsFlow(startDatetime,endDatetime).map { (it?:0) / 1000f }
}

