package com.radiogolha.mobile.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiogolha.mobile.data.updateArchiveDatabaseFromCdn
import com.radiogolha.mobile.theme.GolhaColors
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalComposeUiApi::class)
internal fun TvSettingsScreen(
    entryFocusRequester: FocusRequester,
    topEntryRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
    playerFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var isUpdating by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 7.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "تنظیمات",
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = GolhaColors.PrimaryText,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF1EEE5).copy(alpha = 0.92f))
                .border(1.dp, GolhaColors.Border.copy(alpha = 0.75f), RoundedCornerShape(16.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "به‌روزرسانی اطلاعات آرشیو",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = GolhaColors.PrimaryText,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = "برای دریافت آخرین اطلاعات، از این گزینه استفاده کنید.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = GolhaColors.SecondaryText,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )

            TvSettingsUpdateButton(
                isUpdating = isUpdating,
                modifier = Modifier
                    .focusRequester(entryFocusRequester)
                    .focusProperties {
                        up = topEntryRequester
                        down = playerFocusRequester
                        right = sidebarEntryRequester
                        left = FocusRequester.Cancel
                    },
                onClick = {
                    if (isUpdating) return@TvSettingsUpdateButton
                    isUpdating = true
                    isDownloading = false
                    progress = 0f
                    successMessage = null
                    errorMessage = null
                    scope.launch {
                        val result = updateArchiveDatabaseFromCdn(forceDownload = false) { value ->
                            isDownloading = true
                            progress = value.coerceIn(0f, 1f)
                        }
                        isUpdating = false
                        isDownloading = false
                        if (result.success) {
                            successMessage = if (result.didUpdate) {
                                "دیتابیس با موفقیت به‌روزرسانی شد."
                            } else {
                                "دیتابیس همین الان هم به‌روز است."
                            }
                            errorMessage = null
                        } else {
                            successMessage = null
                            errorMessage = result.message
                        }
                    }
                },
            )

            if (isDownloading) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = GolhaColors.BannerDetail,
                        trackColor = GolhaColors.Border.copy(alpha = 0.45f),
                    )
                    Text(
                        text = "در حال دانلود: ${((progress * 100).toInt()).coerceIn(0, 100)}٪",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                        color = GolhaColors.SecondaryText,
                    )
                }
            }

            successMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color(0xFF0A6D31),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color(0xFFA73333),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun TvSettingsUpdateButton(
    isUpdating: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(12.dp)
    val backgroundColor = if (isFocused) Color(0xFFFFC93A) else GolhaColors.PrimaryText
    val contentColor = if (isFocused) GolhaColors.PrimaryText else Color.White

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(2.dp, if (isFocused) Color.White else Color.Transparent, shape)
            .clickable(
                enabled = !isUpdating,
                interactionSource = interactionSource,
                indication = null,
            ) { onClick() }
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 18.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isUpdating) {
                CircularProgressIndicator(
                    color = contentColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(12.dp),
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = if (isUpdating) "در حال دریافت و جایگزینی..." else "به‌روزرسانی دیتابیس",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = contentColor,
            )
        }
    }
}
