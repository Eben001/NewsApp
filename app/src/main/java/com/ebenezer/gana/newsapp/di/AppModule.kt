package com.ebenezer.gana.newsapp.di

import android.content.Context
import androidx.room.Room
import com.ebenezer.gana.newsapp.data.local.ArticleDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideArticleDatabase(@ApplicationContext appContext: Context) = Room
        .databaseBuilder(
            appContext.applicationContext,
            ArticleDatabase::class.java,
            "article_db"
        ).build()

    @Provides
    @Singleton
    fun provideArticleDao(articleDatabase: ArticleDatabase) = articleDatabase.getArticleDao()

}