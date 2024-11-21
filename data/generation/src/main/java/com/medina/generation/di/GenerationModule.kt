package com.medina.generation.di

import com.medina.generation.repository.GenerationDataRepository
import com.medina.generation.repository.GenerationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface GenerationModule {

    @Singleton
    @Binds
    fun bindsGenerationRepository(
        generationRepository: GenerationDataRepository
    ): GenerationRepository
}