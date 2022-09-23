package com.ebenezer.gana.newsapp.repository

import com.ebenezer.gana.newsapp.network.api.NewsApi
import com.ebenezer.gana.newsapp.db.ArticleDao
import com.ebenezer.gana.newsapp.models.Article
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val articleDao: ArticleDao,
    private val newsApi: NewsApi
) {

    suspend fun getBreakingNews(countryCode: String, pageNumber: Int) =
        newsApi.getBreakingNews(countryCode, pageNumber)

    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        newsApi.searchNews(searchQuery, pageNumber)

    suspend fun upsert(article: Article) = articleDao.upsert(article)

    fun getSavedNews():Flow<List<Article>> = articleDao.getAllArticles()


    suspend fun deleteArticle(article: Article) = articleDao.deleteArticle(article)

}