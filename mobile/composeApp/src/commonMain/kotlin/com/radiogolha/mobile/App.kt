package com.radiogolha.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App() {
    var categories by remember { mutableStateOf<List<CategoryItem>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching { loadCategories() }
            .onSuccess {
                categories = it
                errorMessage = null
            }
            .onFailure {
                categories = emptyList()
                errorMessage = it.message ?: "Failed to load categories"
            }
    }

    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top,
            ) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Box(modifier = Modifier.padding(top = 16.dp)) {
                    when {
                        errorMessage != null -> {
                            Text(
                                text = errorMessage ?: "Failed to load categories",
                                color = MaterialTheme.colorScheme.error,
                            )
                        }

                        categories.isEmpty() -> {
                            Text(
                                text = "Loading...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        else -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(categories) { category ->
                                    Text(text = "${category.id}. ${category.titleFa}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
