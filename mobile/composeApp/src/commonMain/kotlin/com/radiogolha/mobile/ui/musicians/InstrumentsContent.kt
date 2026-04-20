package com.radiogolha.mobile.ui.musicians

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.MusicianListItemUiModel
import com.radiogolha.mobile.ui.people.compareByPersianText

@Composable
fun InstrumentsContent(
    musicians: List<MusicianListItemUiModel>,
    onInstrumentClick: (String) -> Unit = {},
) {
    val instruments = remember(musicians) {
        musicians.map { it.instrument }
            .distinct()
            .filter { it.isNotBlank() }
            .sortedWith(compareByPersianText())
    }

    if (instruments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("موردی یافت نشد", color = GolhaColors.SecondaryText)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(instruments) { instrument ->
                InstrumentCard(
                    name = instrument,
                    onClick = { onInstrumentClick(instrument) }
                )
            }
        }
    }
}

@Composable
private fun InstrumentCard(
    name: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GolhaColors.Surface)
            .border(1.dp, GolhaColors.Border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = GolhaColors.PrimaryText,
            textAlign = TextAlign.Center
        )
    }
}
