package com.ebenezer.gana.newsapp.repository

import com.ebenezer.gana.newsapp.models.Article
import com.ebenezer.gana.newsapp.models.NewsResponse
import com.ebenezer.gana.newsapp.util.Result
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface NewsRepository {

    suspend fun getBreakingNews(
        countryCode: String,
        pageNumber: Int
    ): Result<Response<NewsResponse>>

    suspend fun searchNews(searchQuery: String, pageNumber: Int): Result<Response<NewsResponse>>

    suspend fun upsert(article: Article): Long

    fun getSavedNews(): Flow<List<Article>>

    suspend fun deleteArticle(article: Article)

}