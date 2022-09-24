package com.ebenezer.gana.newsapp.util

import android.content.Context
import androidx.annotation.StringRes

sealed class Result<out T : Any>(
    val data: T? = null,
    val message: String? = null,
    val resId: Int? = null
) {
    class Loading<T : Any> : Result<T>()
    class Success<T : Any>(data: T) : Result<T>(data)
    data class Failure(val error: Throwable?) : Result<Nothing>()

    data class DynamicString<T : Any>(val dynamicString: String) :
        Result<T>(message = dynamicString)

    class StringResource<T : Any>(@StringRes resId: Int) : Result<T>(resId = resId)

    fun asString(context: Context): Any {
        return when (this) {
            is DynamicString -> dynamicString
            is StringResource -> context.resources.getString(resId!!)
            else -> {}
        }
    }

}



