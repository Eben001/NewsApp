package com.ebenezer.gana.newsapp.ui.searchNews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebenezer.gana.newsapp.models.NewsResponse
import com.ebenezer.gana.newsapp.network.InternetConnectivityChecker
import com.ebenezer.gana.newsapp.repository.NewsRepository
import com.ebenezer.gana.newsapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SearchNewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val internetConnectivityChecker: InternetConnectivityChecker
) : ViewModel() {

    private val _searchNewsResult = MutableStateFlow<Resource<NewsResponse>?>(null)
    val searchNewsResult: StateFlow<Resource<NewsResponse>?> = _searchNewsResult.asStateFlow()
        .stateIn(
            initialValue = Resource.Loading(),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    var searchNewsPage = 1
    private var searchNewsResponse: NewsResponse? = null


    fun searchNews(searchQuery: String) = viewModelScope.launch {
        safeSearchNewsCall(searchQuery)
    }

    private suspend fun safeSearchNewsCall(searchQuery: String) {
        _searchNewsResult.value = Resource.Loading()
        try {
            if (internetConnectivityChecker.hasInternetConnection()) {
                val response = newsRepository.searchNews(searchQuery, searchNewsPage)
                _searchNewsResult.value = handleSearchNewsResponse(response)
            } else {
                _searchNewsResult.value = Resource.Error("No internet connection")
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> _searchNewsResult.value = Resource.Error("Network Failure")
                else -> _searchNewsResult.value = Resource.Error("Conversion Error")
            }
        }
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = resultResponse
                } else {
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

}