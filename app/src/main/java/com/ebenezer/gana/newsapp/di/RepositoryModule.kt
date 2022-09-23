package com.ebenezer.gana.newsapp.di

import com.ebenezer.gana.newsapp.network.api.NewsApi
import com.ebenezer.gana.newsapp.db.ArticleDao
import com.ebenezer.gana.newsapp.repository.NewsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideNewsRepository(articleDao: ArticleDao,newsApi: NewsApi ) =
        NewsRepository(articleDao, newsApi)

}