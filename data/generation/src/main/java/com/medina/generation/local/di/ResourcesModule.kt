package com.medina.generation.local.di

import android.content.Context
import com.medina.generation.local.TextResources
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ResourcesModule {

    @Provides
    @Singleton
    internal fun providesTextResources(
        @ApplicationContext context: Context
    ): TextResources = TextResources(context)
}
