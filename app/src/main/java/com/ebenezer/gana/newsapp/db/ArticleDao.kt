package com.ebenezer.gana.newsapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ebenezer.gana.newsapp.models.Article
import com.ebenezer.gana.newsapp.util.Result
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: Article):Long

    @Query("SELECT * FROM articles")
    fun getAllArticles():Flow<List<Article>>

    @Delete
    suspend fun deleteArticle(article: Article)
}