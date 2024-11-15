package com.medina.data.local.di

import android.content.Context
import com.medina.data.local.database.ItappDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

// "di" stands for DataInjection

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    internal fun providesTrainingDao(
        itappDatabase: ItappDatabase
    ) = itappDatabase.trainingDao()

    @Provides
    @Singleton
    internal fun providesExerciseDao(
        itappDatabase: ItappDatabase
    ) = itappDatabase.exerciseDao()

    @Provides
    @Singleton
    internal fun providesSessionDao(
        itappDatabase: ItappDatabase
    ) = itappDatabase.sessionDao()

    @Provides
    @Singleton
    internal fun providesItappDatabase(
        @ApplicationContext context: Context
    ): ItappDatabase {val applicationScope = CoroutineScope(SupervisorJob())
        return ItappDatabase.getDatabase(context, applicationScope)
    }
}

