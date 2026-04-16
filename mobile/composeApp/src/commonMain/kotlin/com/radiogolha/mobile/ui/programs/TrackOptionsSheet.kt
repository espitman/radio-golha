package com.radiogolha.mobile.ui.programs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.GolhaIcon
import com.radiogolha.mobile.ui.home.GolhaLineIcon
import com.radiogolha.mobile.ui.home.TrackUiModel

data class PlaylistOptionItem(val id: Long, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOptionsSheet(
    track: TrackUiModel,
    manualPlaylists: List<PlaylistOptionItem>,
    onDismiss: () -> Unit,
    onGoToProgram: () -> Unit = {},
    onGoToArtist: () -> Unit = {},
    onAddToPlaylist: (playlistId: Long) -> Unit = {},
    onCreatePlaylist: (name: String) -> Unit = {},
) {
    var showAddToPlaylist by remember { mutableStateOf(false) }
    var showCreatePlaylist by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = GolhaColors.ScreenBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            ) {
                // Track info header
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(track.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(track.artist, style = MaterialTheme.typography.bodySmall, color = GolhaColors.SecondaryText)
                    }
                }

                HorizontalDivider(color = GolhaColors.Border.copy(alpha = 0.5f))

                val step = if (!showAddToPlaylist) 0 else if (!showCreatePlaylist) 1 else 2
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        (slideInVertically { it / 2 } + fadeIn()).togetherWith(slideOutVertically { -it / 2 } + fadeOut())
                    },
                ) { currentStep ->
                    Column {
                        when (currentStep) {
                            0 -> {
                                OptionRow(icon = GolhaIcon.Library, text = "افزودن به لیست پخش") { showAddToPlaylist = true }
                                OptionRow(icon = GolhaIcon.Info, text = "رفتن به صفحه برنامه") { onGoToProgram(); onDismiss() }
                                if (track.artistId != null) {
                                    OptionRow(icon = GolhaIcon.People, text = "رفتن به صفحه هنرمند") { onGoToArtist(); onDismiss() }
                                }
                            }
                            1 -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("افزودن به لیست", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText, modifier = Modifier.weight(1f))
                                    Surface(
                                        modifier = Modifier.clickable { showCreatePlaylist = true },
                                        shape = RoundedCornerShape(12.dp),
                                        color = GolhaColors.PrimaryAccent,
                                    ) {
                                        Text("+ لیست جدید", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = androidx.compose.ui.graphics.Color.White)
                                    }
                                }
                                if (manualPlaylists.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        Text("لیستی ندارید. یک لیست جدید بسازید.", style = MaterialTheme.typography.bodyMedium, color = GolhaColors.SecondaryText)
                                    }
                                } else {
                                    manualPlaylists.forEach { playlist ->
                                        OptionRow(icon = GolhaIcon.Library, text = playlist.name) { onAddToPlaylist(playlist.id); onDismiss() }
                                    }
                                }
                            }
                            2 -> {
                                Column(modifier = Modifier.padding(horizontal = 24.dp).padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("ساخت لیست جدید", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = GolhaColors.Surface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
                                    ) {
                                        BasicTextField(
                                            value = newPlaylistName, onValueChange = { newPlaylistName = it },
                                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                                            textStyle = TextStyle(color = GolhaColors.PrimaryText, fontSize = 16.sp),
                                            cursorBrush = SolidColor(GolhaColors.PrimaryAccent), singleLine = true,
                                            decorationBox = { inner -> if (newPlaylistName.isEmpty()) Text("نام لیست...", color = GolhaColors.SecondaryText.copy(alpha = 0.5f)); inner() },
                                        )
                                    }
                                    Button(
                                        onClick = { onCreatePlaylist(newPlaylistName); newPlaylistName = ""; showCreatePlaylist = false },
                                        enabled = newPlaylistName.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = GolhaColors.PrimaryAccent),
                                    ) { Text("ساخت و افزودن", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionRow(icon: GolhaIcon, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        GolhaLineIcon(icon = icon, modifier = Modifier.size(20.dp), tint = GolhaColors.SecondaryText)
        Text(text, style = MaterialTheme.typography.bodyLarge, color = GolhaColors.PrimaryText)
    }
}
