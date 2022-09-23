package com.ebenezer.gana.newsapp.models

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: MutableList<Article>
)