package com.medina.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ExerciseDao {
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

    @Query("DELETE FROM exercise_table WHERE id = :exercise")
    suspend fun deleteExercise(exercise: UUID)
}