package com.ebenezer.gana.newsapp.ui.savedNews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebenezer.gana.newsapp.R
import com.ebenezer.gana.newsapp.models.Article
import com.ebenezer.gana.newsapp.repository.NewsRepository
import com.ebenezer.gana.newsapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedNewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
) : ViewModel() {

    private val _resultSharedFlow = MutableSharedFlow<Result.StringResource<Int>>()
    val resultSharedFlow = _resultSharedFlow.asSharedFlow()


    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getSavedNews(): Flow<List<Article>> = newsRepository.getSavedNews()


    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
        _resultSharedFlow.emit(Result.StringResource(R.string.deleted_successfully))




    }
}
