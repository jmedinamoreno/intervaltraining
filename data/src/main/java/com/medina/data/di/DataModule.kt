package com.medina.data.di

import com.medina.data.Clock
import com.medina.data.RealClock
import com.medina.data.repository.StatsDataRepository
import com.medina.data.repository.StatsRepository
import com.medina.data.repository.TrainingDataRepository
import com.medina.data.repository.TrainingRepository
import com.medina.data.repository.UserInfoDataRepository
import com.medina.data.repository.UserInfoRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// "di" stands as DataInjection

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {
    @Provides
    @Singleton
    internal fun providesClock(): Clock = RealClock()
}

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Singleton
    @Binds
    fun bindsUserInfoRepository(
        userInfoRepository: UserInfoDataRepository
    ): UserInfoRepository

    @Singleton
    @Binds
    fun bindsTrainingRepository(
        trainingRepository: TrainingDataRepository
    ): TrainingRepository

    @Singleton
    @Binds
    fun bindsStatsRepository(
        statsRepository: StatsDataRepository
    ): StatsRepository
}

