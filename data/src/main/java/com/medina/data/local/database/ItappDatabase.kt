package com.medina.data.local.database

import android.content.Context
import androidx.room.*
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.medina.data.model.ExerciseIcon
import com.medina.data.model.Training
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * Itapp stands for Interval Training App
 * Database class with a singleton INSTANCE object.
 */
@Database(
    entities = [
        TrainingItem::class,
        ExerciseItem::class,
        SessionItem::class
               ],
    version = 3,
    exportSchema = false
)
abstract class ItappDatabase : RoomDatabase() {
    abstract fun itappDao(): ItappDao
    abstract fun trainingDao(): TrainingDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun sessionDao(): SessionDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: ItappDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ItappDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ItappDatabase::class.java,
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
                    populateDatabase(
                        trainingDao = database.trainingDao(),
                        exerciseDao = database.exerciseDao()
                    )
                }
            }
        }

        suspend fun populateDatabase(trainingDao: TrainingDao, exerciseDao: ExerciseDao) {
            // Add sample training
            val newTraining = TrainingItem(
                name = "Example training",
                lastUsed = Date().time,
                totalTimeSec = 120,
                defaultTimeSec = 45,
                defaultRestSec = 15,
            )
            trainingDao.insert(newTraining)
            exerciseDao.insert(
                ExerciseItem(
                    training = newTraining.id,
                    name = "Run",
                    icon = ExerciseIcon.RUN,
                    position = 0,
                    timeSec = newTraining.defaultTimeSec,
                    restSec = newTraining.defaultRestSec
                )
            )
            exerciseDao.insert(
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