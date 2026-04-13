package com.radiogolha.mobile.ui.orchestras

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.OrchestraListItemUiModel
import com.radiogolha.mobile.ui.home.ArtistAvatar

@Composable
fun OrchestraContent(
    orchestras: List<OrchestraListItemUiModel>,
    onOrchestraClick: (Long) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        itemsIndexed(orchestras, key = { _, item -> item.id }) { index, orchestra ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOrchestraClick(orchestra.id) }
                    .padding(horizontal = GolhaSpacing.ScreenHorizontal, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ArtistAvatar(
                    name = orchestra.name,
                    imageUrl = null,
                    tint = GolhaColors.SoftGold,
                    modifier = Modifier.size(76.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = orchestra.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                        ),
                        color = GolhaColors.PrimaryText,
                    )
                    Text(
                        text = "${orchestra.programCount} برنامه",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GolhaColors.SecondaryText,
                    )
                }
            }
            if (index < orchestras.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = GolhaSpacing.ScreenHorizontal + 20.dp),
                    color = GolhaColors.Border.copy(alpha = 0.72f),
                )
            }
        }
    }
}
