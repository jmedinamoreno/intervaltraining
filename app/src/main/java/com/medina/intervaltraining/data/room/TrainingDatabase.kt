package com.medina.intervaltraining.data.room

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.medina.intervaltraining.data.viewmodel.ExerciseIcon
import com.medina.intervaltraining.data.viewmodel.Training
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

@Entity(
    tableName = "training_table",
)
data class TrainingItem(
    val name:String,
    val lastUsed:Long,
    val defaultTimeSec:Int,
    val defaultRestSec:Int,
    @PrimaryKey val id: UUID = UUID.randomUUID()
)

@Entity(
    tableName = "exercise_table",
    indices = [
        Index("training"),
        Index("position"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = TrainingItem::class,
            parentColumns = ["id"],
            childColumns = ["training"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExerciseItem(
    val training: UUID,
    val name:String,
    val icon: ExerciseIcon = ExerciseIcon.NONE,
    val position:Int,
    val timeSec:Int,
    val restSec:Int,
    @PrimaryKey val id: UUID = UUID.randomUUID()
)

@Dao
interface TrainingDao {
    @Query("SELECT * FROM training_table ORDER BY lastUsed DESC")
    fun getTrainingListAsFlow(): Flow<List<TrainingItem>>

    @Query("SELECT * FROM training_table ORDER BY lastUsed DESC")
    suspend fun getTrainingList(): List<TrainingItem>

    @Query("SELECT * FROM training_table WHERE id = :id")
    suspend fun getTraining(id: UUID): TrainingItem

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: TrainingItem)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(entity: TrainingItem)

    @Query("SELECT SUM(timeSec + restSec) FROM exercise_table WHERE training = :id")
    fun getTotalTimeSecForTrainingByIdAsFlow(id: UUID): Flow<Int>

    @Query("SELECT SUM(timeSec + restSec) FROM exercise_table WHERE training = :id")
    suspend fun getTotalTimeSecForTrainingById(id: UUID): Int

    @Query("SELECT SUM(timeSec + restSec) FROM exercise_table WHERE training = :id")
    fun getTotalTimeForTrainingByIdNow(id: UUID): Int

    @Query("SELECT SUM(timeSec) FROM exercise_table WHERE training = :id")
    suspend fun getTotalWorkTimeForTrainingById(id: UUID): Int

    @Query("SELECT SUM(restSec) FROM exercise_table WHERE training = :id")
    suspend fun getTotalRestTimeForTrainingById(id: UUID): Int

    @Query("SELECT * FROM exercise_table WHERE training = :id ORDER BY position ASC")
    suspend fun getExercisesForTrainingById(id: UUID): List<ExerciseItem>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: ExerciseItem)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(entity: ExerciseItem)
}

@Database(
    entities = [TrainingItem::class, ExerciseItem::class],
    version = 1,
    exportSchema = false
)
abstract class TrainingRoomDatabase : RoomDatabase() {
    abstract fun trainingDao():TrainingDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: TrainingRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TrainingRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrainingRoomDatabase::class.java,
                    "training_database"
                )
                    .addCallback(TrainingDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    private class TrainingDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.trainingDao())
                }
            }
        }

        suspend fun populateDatabase(trainingDao: TrainingDao) {
            // Add sample training
            val newTraining = TrainingItem(
                name = "Example training",
                lastUsed = Date().time,
                defaultTimeSec = 45,
                defaultRestSec = 15,
            )
            trainingDao.insert(newTraining)
            trainingDao.insert(
                ExerciseItem(
                    training = newTraining.id,
                    name = "Run",
                    icon = ExerciseIcon.RUN,
                    position = 0,
                    timeSec = newTraining.defaultTimeSec,
                    restSec = newTraining.defaultRestSec
                )
            )
            trainingDao.insert(
                ExerciseItem(
                    training = newTraining.id,
                    name = "Jump",
                    icon = ExerciseIcon.JUMP,
                    position = 1,
                    timeSec = newTraining.defaultTimeSec,
                    restSec = newTraining.defaultRestSec
                )
            )
        }
    }
}