package com.medina.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medina.data.model.SessionTimePerDay
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SessionItem)

    @Query("DELETE FROM session_table WHERE id = :session")
    suspend fun deleteSession(session: UUID)

    @Query("SELECT SUM(dateTimeEnd - dateTimeStart) FROM session_table WHERE training = :id")
    fun getTotalSessionTimeMilsForTrainingByIdAsFlow(id: UUID): Flow<Long?>

    @Query("SELECT SUM(dateTimeEnd - dateTimeStart) FROM session_table WHERE dateTimeStart > :startDatetime AND dateTimeStart < :endDatetime")
    fun getTotalSessionTimeMilsForDateRangeAsFlow(
        startDatetime: Long,
        endDatetime: Long
    ): Flow<Long?>

    @Query(
        "SELECT \n" +
                "    strftime('%Y-%m-%d', datetime(dateTimeStart / 1000, 'unixepoch')) AS day,\n" +
                "    SUM(dateTimeEnd - dateTimeStart) / 1000 AS totalTimeSec\n" +
                "FROM \n" +
                "    session_table\n" +
                "WHERE \n" +
                "    strftime('%Y-%m', datetime(dateTimeStart / 1000, 'unixepoch')) = :yearMonth -- Format the year month as: '2023-12'\n" +
                "GROUP BY \n" +
                "    day\n" +
                "ORDER BY \n" +
                "    day;"
    )
    fun getSessionTimeSecForEachDayInMonth(yearMonth: String): Flow<List<SessionTimePerDay>>
}