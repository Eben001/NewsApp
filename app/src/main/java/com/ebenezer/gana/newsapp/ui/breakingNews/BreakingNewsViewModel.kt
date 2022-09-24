package com.ebenezer.gana.newsapp.ui.breakingNews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebenezer.gana.newsapp.R
import com.ebenezer.gana.newsapp.data.models.NewsResponse
import com.ebenezer.gana.newsapp.data.remote.InternetConnectivityChecker
import com.ebenezer.gana.newsapp.repository.NewsRepository
import com.ebenezer.gana.newsapp.util.Constants.Companion.CODE_NIGERIA
import com.ebenezer.gana.newsapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class BreakingNewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val internetConnectivityChecker: InternetConnectivityChecker
) : ViewModel() {

    private val _breakingNewsResult = MutableStateFlow<Result<NewsResponse>?>(null)
    val breakingNewsResult: StateFlow<Result<NewsResponse>?> = _breakingNewsResult
        .stateIn(
            initialValue = Result.Loading(),
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
        _breakingNewsResult.value = Result.Loading()
        if (internetConnectivityChecker.hasInternetConnection()) {
            when (val response = newsRepository.getBreakingNews(countryCode, breakingNewsPage)) {
                is Result.Success -> {
                    _breakingNewsResult.value = handleBreakingNewsResponse(response.data!!)
                }
                is Result.Failure -> {

                    when (response.error) {
                        is HttpException ->
                            _breakingNewsResult.value =
                                Result.StringResource(R.string.err_http_error)

                        is UnknownHostException ->
                            _breakingNewsResult.value =
                                Result.StringResource(R.string.err_check_internet_connection)

                        is IOException ->
                            _breakingNewsResult.value =
                                Result.StringResource(R.string.err_network_failure)

                        is Exception ->
                            _breakingNewsResult.value =
                                Result.StringResource(R.string.general_exception)


                    }
                }
                else -> {}
            }

        } else {
            _breakingNewsResult.value = (Result.StringResource(R.string.err_no_internet))
        }

        /* try {
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
         }*/
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Result<NewsResponse> {
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
                return Result.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return Result.DynamicString(response.message())
    }


}