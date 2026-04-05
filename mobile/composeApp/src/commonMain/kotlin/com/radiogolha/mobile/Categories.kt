package com.radiogolha.mobile

data class CategoryItem(
    val id: Long,
    val titleFa: String,
)

expect fun loadCategories(): List<CategoryItem>
