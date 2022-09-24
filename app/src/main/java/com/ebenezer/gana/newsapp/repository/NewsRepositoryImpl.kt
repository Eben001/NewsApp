package com.ebenezer.gana.newsapp.repository

import com.ebenezer.gana.newsapp.db.ArticleDao
import com.ebenezer.gana.newsapp.models.Article
import com.ebenezer.gana.newsapp.models.NewsResponse
import com.ebenezer.gana.newsapp.network.api.NewsApi
import com.ebenezer.gana.newsapp.util.Result
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import retrofit2.Response
import java.net.UnknownHostException
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val articleDao: ArticleDao,
    private val newsApi: NewsApi
) : NewsRepository {

    override suspend fun getBreakingNews(
        countryCode: String,
        pageNumber: Int
    ): Result<Response<NewsResponse>> {
        return try {
            val data = newsApi.getBreakingNews(countryCode, pageNumber)
            Result.Success(data)
        } catch (error: HttpException) {
            Result.Failure(error)
        } catch (error: UnknownHostException) {
            Result.Failure(error)
        } catch (error: Exception) {
            Result.Failure(error)
        }
    }

    override suspend fun searchNews(
        searchQuery: String,
        pageNumber: Int
    ): Result<Response<NewsResponse>> {
        return try {
            val data = newsApi.searchNews(searchQuery, pageNumber)
            Result.Success(data)
        } catch (error: HttpException) {
            Result.Failure(error)
        } catch (error: UnknownHostException) {
            Result.Failure(error)
        } catch (error: Exception) {
            Result.Failure(error)
        }
    }

    override suspend fun upsert(article: Article) = articleDao.upsert(article)

    override fun getSavedNews(): Flow<List<Article>> = articleDao.getAllArticles()

    override suspend fun deleteArticle(article: Article) = articleDao.deleteArticle(article)
}