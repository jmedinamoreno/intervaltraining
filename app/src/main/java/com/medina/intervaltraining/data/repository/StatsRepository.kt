package com.medina.intervaltraining.data.repository

import androidx.annotation.WorkerThread
import com.medina.intervaltraining.data.room.SessionItem
import com.medina.intervaltraining.data.room.TrainingDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID

interface StatsRepository {
    fun timeForTrainingMinAsFlow(training: UUID): Flow<Int>

    suspend fun insert(session: SessionItem)

    suspend fun deleteSession(session: UUID)

    fun getTotalSessionTimeSecForTrainingById(id: UUID): Flow<Float>

    fun getTotalSessionTimeSecForDateRange(startDatetime: Long, endDatetime: Long): Flow<Float>
}

class StatsRoomRepository(private val trainingDao: TrainingDao
): StatsRepository{
    override fun timeForTrainingMinAsFlow(training: UUID) = trainingDao.getTotalTimeSecForTrainingByIdAsFlow(training).map { (it?:0)/60 }

    @WorkerThread
    override suspend fun insert(session: SessionItem) = trainingDao.insert(session)

    override suspend fun deleteSession(session: UUID) = trainingDao.deleteSession(session)

    override fun getTotalSessionTimeSecForTrainingById(id: UUID) =
        trainingDao.getTotalSessionTimeMilsForTrainingByIdAsFlow(id).map { (it?:0) / 1000f }

    override fun getTotalSessionTimeSecForDateRange(startDatetime: Long, endDatetime: Long) =
        trainingDao.getTotalSessionTimeMilsForDateRangeAsFlow(startDatetime,endDatetime).map { (it?:0) / 1000f }
}

class StatsDummyRepository(): StatsRepository {

    var timeForTrainingMin: Int = 10

    override fun timeForTrainingMinAsFlow(training: UUID): Flow<Int> = flowOf(timeForTrainingMin)

    override suspend fun insert(session: SessionItem) { }

    override suspend fun deleteSession(session: UUID) { }

    override fun getTotalSessionTimeSecForTrainingById(id: UUID): Flow<Float> = MutableStateFlow(3600f)

    override fun getTotalSessionTimeSecForDateRange(
        startDatetime: Long,
        endDatetime: Long
    ): Flow<Float> = flowOf((endDatetime.toFloat()-startDatetime.toFloat())/24000f)

}