package com.radiogolha.mobile.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.radiogolha.mobile.theme.GolhaColors

internal data class TvArtistCardItem(
    val id: Long,
    val name: String,
    val role: String,
    val imageUrl: String? = null,
)

@Composable
internal fun TvArtistCard(
    item: TvArtistCardItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    val cardWidth = 156.dp

    Column(
        modifier = modifier
            .width(cardWidth)
            .height(225.dp)
            .clip(shape)
            .background(Color.White)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) GolhaColors.BannerDetail else Color(0x1AC4C6CF),
                shape = shape,
            )
            .clickable { onClick() }
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .padding(0.dp),
    ) {
        Box(
            modifier = Modifier
                .size(cardWidth)
                .background(Color(0xFFE5E2DA)),
            contentAlignment = Alignment.Center,
        ) {
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = GolhaColors.SecondaryText.copy(alpha = 0.68f),
                    modifier = Modifier.size(44.dp),
                )
            }

            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(3.dp, GolhaColors.BannerDetail, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(69.dp)
                .background(Color.White)
                .padding(horizontal = 15.dp, vertical = 13.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = item.name.replace("‌", " "),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.4.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = GolhaColors.PrimaryText,
                maxLines = 1,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = item.role,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
                color = Color(0xFF43474E),
                maxLines = 1,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            )
        }
    }
}
