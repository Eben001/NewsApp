package com.ebenezer.gana.newsapp.ui.articleDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebenezer.gana.newsapp.R
import com.ebenezer.gana.newsapp.models.Article
import com.ebenezer.gana.newsapp.network.InternetConnectivityChecker
import com.ebenezer.gana.newsapp.repository.NewsRepository
import com.ebenezer.gana.newsapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleDetailsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val internetConnectivityChecker: InternetConnectivityChecker
) : ViewModel() {

    private val _resultSharedFlow = MutableSharedFlow<Result.StringResource<Int>>()
    val resultSharedFlow = _resultSharedFlow.asSharedFlow()

    fun saveArticle(article: Article) = viewModelScope.launch {
        if (internetConnectivityChecker.hasInternetConnection()) {
            newsRepository.upsert(article)
            _resultSharedFlow.emit(Result.StringResource(R.string.saved_successfully))
        } else {
            _resultSharedFlow.emit(Result.StringResource(R.string.err_check_internet_connection))
        }
    }
}