package com.ebenezer.gana.newsapp.data.models

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: MutableList<Article>
)