package com.danilkha.trainstats.di

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
object AndroidModule {

    @Provides
    fun provideContentResolver(
        @ApplicationContext context: Context
    ): ContentResolver {
        return context.contentResolver
    }
}