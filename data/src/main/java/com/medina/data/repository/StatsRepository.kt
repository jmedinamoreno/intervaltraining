package com.medina.data.repository

import androidx.annotation.WorkerThread
import com.medina.data.local.database.SessionItem
import com.medina.data.local.database.ItappDao
import com.medina.data.local.database.SessionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

interface StatsRepository {
    suspend fun insert(session: SessionItem)
    suspend fun deleteSession(session: UUID)
    fun getTotalSessionTimeSecForTrainingById(id: UUID): Flow<Float>
    fun getTotalSessionTimeSecForDateRange(startDatetime: Long, endDatetime: Long): Flow<Float>
    fun getSessionTimeSecForEachDayInMonth(calendar: Calendar): Flow<List<Long>>
}

class StatsDataRepository @Inject constructor(
    private val sessionDao: SessionDao,
):StatsRepository{

    @WorkerThread
    override suspend fun insert(session: SessionItem) = sessionDao.insert(session)

    override suspend fun deleteSession(session: UUID) = sessionDao.deleteSession(session)

    override fun getTotalSessionTimeSecForTrainingById(id: UUID) =
        sessionDao.getTotalSessionTimeMilsForTrainingByIdAsFlow(id).map { (it?:0) / 1000f }

    override fun getTotalSessionTimeSecForDateRange(startDatetime: Long, endDatetime: Long) =
        sessionDao.getTotalSessionTimeMilsForDateRangeAsFlow(startDatetime,endDatetime).map { (it?:0) / 1000f }

    override fun getSessionTimeSecForEachDayInMonth(calendar: Calendar): Flow<List<Long>> {
        val yearMonth = calendar.let {
            "${it.get(Calendar.YEAR)}-${it.get(Calendar.MONTH) + 1}"
        }
        return sessionDao.getSessionTimeSecForEachDayInMonth(yearMonth).map { list ->
            list.map { day -> day.totalTimeSec }
        }
    }
}

class StatsDummyRepository : StatsRepository {
    override suspend fun insert(session: SessionItem) { }
    override suspend fun deleteSession(session: UUID) { }
    override fun getTotalSessionTimeSecForTrainingById(id: UUID): Flow<Float> =
        MutableStateFlow(3600f)
    override fun getTotalSessionTimeSecForDateRange(
        startDatetime: Long,
        endDatetime: Long
    ): Flow<Float> = flowOf((endDatetime.toFloat() - startDatetime.toFloat()) / 24000f)
    override fun getSessionTimeSecForEachDayInMonth(calendar: Calendar): Flow<List<Long>>
            = flowOf((1..31).map { (375 * ((it * 5) % 25)).toLong() })
}