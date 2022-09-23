package com.ebenezer.gana.newsapp.ui.breakingNews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebenezer.gana.newsapp.models.NewsResponse
import com.ebenezer.gana.newsapp.network.InternetConnectivityChecker
import com.ebenezer.gana.newsapp.repository.NewsRepository
import com.ebenezer.gana.newsapp.util.Constants.Companion.CODE_NIGERIA
import com.ebenezer.gana.newsapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class BreakingNewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val internetConnectivityChecker: InternetConnectivityChecker
) : ViewModel() {

    private val _breakingNewsResult = MutableStateFlow<Resource<NewsResponse>?>(null)
    val breakingNewsResult: StateFlow<Resource<NewsResponse>?> = _breakingNewsResult
        .stateIn(
            initialValue = Resource.Loading(),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    var breakingNewsPage = 1
    private var breakingNewsResponse: NewsResponse? = null


    init {
        getBreakingNews(countryCode = CODE_NIGERIA)
    }

    fun getBreakingNews(countryCode: String) {
        viewModelScope.launch {
            safeBreakingNewsCall(countryCode)
        }
    }

    private suspend fun safeBreakingNewsCall(countryCode: String) {
        _breakingNewsResult.value = Resource.Loading()
        try {
            if (internetConnectivityChecker.hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(countryCode, breakingNewsPage)
                _breakingNewsResult.value = handleBreakingNewsResponse(response)
            } else {
                _breakingNewsResult.value = (Resource.Error("No Internet Connection"))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> _breakingNewsResult.value = (Resource.Error("Network Failure"))
                else -> _breakingNewsResult.value = (Resource.Error("Conversion Error"))
            }
        }
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                breakingNewsPage++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = resultResponse
                } else {
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


}