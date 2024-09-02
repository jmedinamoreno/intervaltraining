package com.medina.intervaltraining

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.medina.intervaltraining.data.Clock
import com.medina.intervaltraining.data.RealClock
import com.medina.intervaltraining.data.repository.StatsRepository
import com.medina.intervaltraining.data.repository.StatsRoomRepository
import com.medina.intervaltraining.data.repository.TrainingRepository
import com.medina.intervaltraining.data.repository.TrainingRoomRepository
import com.medina.intervaltraining.data.repository.UserDataDatastoreRepository
import com.medina.intervaltraining.data.repository.UserDataRepository
import com.medina.intervaltraining.data.room.TrainingDao
import com.medina.intervaltraining.data.room.TrainingRoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

private const val USER_PREFERENCES_NAME = "user_preferences"
private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

@Module
@InstallIn(SingletonComponent::class)
object UserDataModule {

    @Provides
    @Singleton
    internal fun providesUserPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.dataStore

    @Provides
    internal fun providesUserDataRepository(
        userPreferences: DataStore<Preferences>
    ): UserDataRepository = UserDataDatastoreRepository(userPreferences)
}

@Module
@InstallIn(SingletonComponent::class)
object TrainingsDataModule {

    @Provides
    @Singleton
    internal fun providesTrainingRoomDatabase(
        @ApplicationContext context: Context
    ): TrainingRoomDatabase {val applicationScope = CoroutineScope(SupervisorJob())
        return TrainingRoomDatabase.getDatabase(context, applicationScope)
    }

    @Provides
    @Singleton
    internal fun providesClock(): Clock = RealClock()

    @Provides
    internal fun providesTrainingRepository(
        trainingRoomDatabase: TrainingRoomDatabase
    ): TrainingRepository = TrainingRoomRepository(trainingRoomDatabase.trainingDao())

    @Provides
    internal fun providesStatsRepository(
        trainingRoomDatabase: TrainingRoomDatabase
    ): StatsRepository = StatsRoomRepository(trainingRoomDatabase.trainingDao())
}