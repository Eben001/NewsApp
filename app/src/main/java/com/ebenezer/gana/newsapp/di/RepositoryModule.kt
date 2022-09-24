package com.ebenezer.gana.newsapp.di

import com.ebenezer.gana.newsapp.network.api.NewsApi
import com.ebenezer.gana.newsapp.db.ArticleDao
import com.ebenezer.gana.newsapp.repository.NewsRepository
import com.ebenezer.gana.newsapp.repository.NewsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun provideNewsRepository(newsRepositoryImpl: NewsRepositoryImpl):NewsRepository

}