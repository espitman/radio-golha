package com.radiogolha.mobile.ui.programs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaElevation
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.ProgramUiModel

@Composable
fun ProgramsScreen(
    programs: List<ProgramUiModel>,
    modifier: Modifier = Modifier,
) {
    if (programs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("برنامه‌ای پیدا نشد", color = GolhaColors.SecondaryText)
        }
        return
    }

    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = GolhaSpacing.ScreenHorizontal,
                vertical = 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(programs) { program ->
                ProgramListCard(program)
            }
        }
    }
}

@Composable
private fun ProgramListCard(item: ProgramUiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        shadowElevation = GolhaElevation.Card,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = GolhaColors.PrimaryText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${item.episodeCount} قسمت",
                style = MaterialTheme.typography.bodySmall,
                color = GolhaColors.SecondaryText,
            )
        }
    }
}
