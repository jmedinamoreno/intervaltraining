package com.medina.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.medina.data.model.SessionTimePerDay
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ItappDao {
    @Query("SELECT * FROM training_table ORDER BY lastUsed DESC")
    fun getTrainingListAsFlow(): Flow<List<TrainingItem>>

    @Query("SELECT * FROM training_table ORDER BY lastUsed DESC")
    suspend fun getTrainingList(): List<TrainingItem>

    @Query("SELECT * FROM training_table WHERE id = :id")
    fun getTrainingAsFlow(id: UUID): Flow<TrainingItem?>

    @Query("SELECT * FROM training_table WHERE id = :id")
    suspend fun getTraining(id: UUID): TrainingItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TrainingItem)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(entity: TrainingItem)

    @Query("SELECT SUM(timeSec + restSec) FROM exercise_table WHERE training = :id")
    fun getTotalTimeSecForTrainingByIdAsFlow(id: UUID): Flow<Int?>

    @Query("SELECT * FROM exercise_table WHERE training = :id ORDER BY position ASC")
    fun getExercisesForTrainingByIdAsFlow(id: UUID): Flow<List<ExerciseItem>>

    @Query("SELECT * FROM exercise_table WHERE training = :id ORDER BY position ASC")
    suspend fun getExercisesForTrainingById(id: UUID): List<ExerciseItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ExerciseItem)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(entity: ExerciseItem)

    @Query("DELETE FROM exercise_table WHERE training = :training")
    suspend fun deleteExercises(training: UUID)

    @Query("DELETE FROM training_table WHERE id = :training")
    suspend fun deleteTraining(training: UUID)

    @Query("DELETE FROM exercise_table WHERE id = :exercise")
    suspend fun deleteExercise(exercise: UUID)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SessionItem)

    @Query("DELETE FROM session_table WHERE id = :session")
    suspend fun deleteSession(session: UUID)

    @Query("SELECT SUM(dateTimeEnd - dateTimeStart) FROM session_table WHERE training = :id")
    fun getTotalSessionTimeMilsForTrainingByIdAsFlow(id: UUID): Flow<Long?>

    @Query("SELECT SUM(dateTimeEnd - dateTimeStart) FROM session_table WHERE dateTimeStart > :startDatetime AND dateTimeStart < :endDatetime")
    fun getTotalSessionTimeMilsForDateRangeAsFlow(startDatetime: Long, endDatetime: Long): Flow<Long?>

    @Query("SELECT \n" +
            "    strftime('%Y-%m-%d', datetime(dateTimeStart / 1000, 'unixepoch')) AS day,\n" +
            "    SUM(dateTimeEnd - dateTimeStart) / 1000 AS totalTimeSec\n" +
            "FROM \n" +
            "    session_table\n" +
            "WHERE \n" +
            "    strftime('%Y-%m', datetime(dateTimeStart / 1000, 'unixepoch')) = :yearMonth -- Format the year month as: '2023-12'\n" +
            "GROUP BY \n" +
            "    day\n" +
            "ORDER BY \n" +
            "    day;")
    fun getSessionTimeSecForEachDayInMonth(yearMonth: String): Flow<List<SessionTimePerDay>>
}


