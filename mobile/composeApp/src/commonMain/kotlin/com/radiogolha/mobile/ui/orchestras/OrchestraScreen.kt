package com.radiogolha.mobile.ui.orchestras

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.OrchestraListItemUiModel
import com.radiogolha.mobile.ui.people.BrowsePersonRowUiModel
import com.radiogolha.mobile.ui.people.PeopleListRow

@Composable
fun OrchestraContent(
    orchestras: List<OrchestraListItemUiModel>,
    onOrchestraClick: (Long) -> Unit = {},
) {
    val items = orchestras.map { orchestra ->
        BrowsePersonRowUiModel(
            artistId = orchestra.id,
            name = orchestra.name,
            imageUrl = null,
            primaryMeta = "${orchestra.programCount} برنامه",
            groupLabel = "",
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        itemsIndexed(items, key = { _, item -> "orchestra-${item.artistId}" }) { index, item ->
            PeopleListRow(
                item = item,
                onClick = { item.artistId?.let(onOrchestraClick) },
                tint = GolhaColors.SoftGold,
                modifier = Modifier.padding(horizontal = GolhaSpacing.ScreenHorizontal),
            )
            if (index < items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = GolhaSpacing.ScreenHorizontal + 20.dp),
                    color = GolhaColors.Border.copy(alpha = 0.72f),
                )
            }
        }
    }
}
