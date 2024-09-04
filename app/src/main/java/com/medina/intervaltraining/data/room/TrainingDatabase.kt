package com.medina.intervaltraining.data.room

import android.content.Context
import androidx.room.*
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.medina.intervaltraining.data.model.ExerciseIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*

@Entity(
    tableName = "session_table",
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
data class SessionItem(
    val training: UUID,
    var dateTimeStart:Long = 0L,
    var dateTimeEnd:Long = 0L,
    var complete:Boolean = false,
    @PrimaryKey val id: UUID = UUID.randomUUID()
)

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

data class SessionTimePerDay(
    val day: String,
    val totalTimeSec: Long
)

@Dao
interface TrainingDao {
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
    fun getSessionTimeSecForEachDayInMonth(yearMonth: String):  Flow<List<SessionTimePerDay>>
}

@Database(
    entities = [TrainingItem::class, ExerciseItem::class, SessionItem::class],
    version = 2,
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
                    .fallbackToDestructiveMigration()
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
    ) : Callback() {

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