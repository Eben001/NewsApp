package com.ebenezer.gana.newsapp.ui.searchNews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebenezer.gana.newsapp.R
import com.ebenezer.gana.newsapp.models.NewsResponse
import com.ebenezer.gana.newsapp.network.InternetConnectivityChecker
import com.ebenezer.gana.newsapp.repository.NewsRepository
import com.ebenezer.gana.newsapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class SearchNewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val internetConnectivityChecker: InternetConnectivityChecker
) : ViewModel() {

    private val _searchNewsResult = MutableStateFlow<Result<NewsResponse>?>(null)
    val searchNewsResult: StateFlow<Result<NewsResponse>?> = _searchNewsResult.asStateFlow()
        .stateIn(
            initialValue = Result.Loading(),
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    var searchNewsPage = 1
    private var searchNewsResponse: NewsResponse? = null


    fun searchNews(searchQuery: String) = viewModelScope.launch {
        safeSearchNewsCall(searchQuery)
    }

    private suspend fun safeSearchNewsCall(searchQuery: String) {
        _searchNewsResult.value = Result.Loading()
        if (internetConnectivityChecker.hasInternetConnection()) {
            when (val response = newsRepository.searchNews(searchQuery, searchNewsPage)) {
                is Result.Success -> {
                    _searchNewsResult.value = handleSearchNewsResponse(response.data!!)
                }
                is Result.Failure -> {

                    when (response.error) {
                        is HttpException ->
                            _searchNewsResult.value =
                                Result.StringResource(R.string.err_http_error)

                        is UnknownHostException ->
                            _searchNewsResult.value =
                                Result.StringResource(R.string.err_check_internet_connection)

                        is IOException ->
                            _searchNewsResult.value =
                                Result.StringResource(R.string.err_network_failure)

                        is Exception ->
                            _searchNewsResult.value =
                                Result.StringResource(R.string.general_exception)


                    }
                }
                else -> {}
            }

        } else {
            _searchNewsResult.value = (Result.StringResource(R.string.err_no_internet))
        }
    }


    /*try {
        if (internetConnectivityChecker.hasInternetConnection()) {
            val response = newsRepository.searchNews(searchQuery, searchNewsPage)
            _searchNewsResult.value = handleSearchNewsResponse(response)
        } else {
            _searchNewsResult.value = Result.ErrorMessage("No internet connection")
        }

    } catch (t: Throwable) {
        when (t) {
            is IOException -> _searchNewsResult.value = Resource.ErrorMessage("Network Failure")
            else -> _searchNewsResult.value = Resource.ErrorMessage("Conversion Error")
        }
    }*/

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Result<NewsResponse> {
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
                return Result.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Result.DynamicString(response.message())
    }

}