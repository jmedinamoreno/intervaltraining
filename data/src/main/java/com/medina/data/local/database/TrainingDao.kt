package com.medina.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface TrainingDao {
    @Query("SELECT * FROM training_table ORDER BY lastUsed DESC")
    fun getAllTrainingsAsFlow(): Flow<List<TrainingItem>>

    @Query("SELECT * FROM training_table ORDER BY lastUsed DESC")
    suspend fun getAllTrainings(): List<TrainingItem>

    @Query("SELECT * FROM training_table WHERE id = :id")
    fun getTrainingAsFlow(id: UUID): Flow<TrainingItem?>

    @Query("SELECT * FROM training_table WHERE id = :id")
    suspend fun getTraining(id: UUID): TrainingItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TrainingItem)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(entity: TrainingItem)

    @Query("DELETE FROM training_table WHERE id = :training")
    suspend fun deleteTraining(training: UUID)
}