import SwiftUI

extension View {
    func trackPlaylistContextMenu(
        trackId: Int64?,
        playlists: [DesktopManualPlaylist],
        onAddToPlaylist: @escaping (Int64, Int64) -> Void,
        onRemoveFromPlaylist: @escaping (Int64, Int64) -> Void,
        onCreatePlaylistAndAdd: @escaping (Int64) -> Void
    ) -> some View {
        desktopCustomContextMenu(actions: {
            guard let trackId else { return [] }
            var actions: [DesktopContextMenuAction] = playlists.map { playlist in
                let containsTrack = playlist.trackIds.contains(trackId)
                return DesktopContextMenuAction(
                    title: containsTrack ? "حذف از \(playlist.name)" : "افزودن به \(playlist.name)",
                    systemImage: containsTrack ? "minus.circle" : "music.note.list",
                    isChecked: containsTrack,
                    role: containsTrack ? .destructive : .normal
                ) {
                    if containsTrack {
                        onRemoveFromPlaylist(playlist.id, trackId)
                    } else {
                        onAddToPlaylist(playlist.id, trackId)
                    }
                }
            }
            actions.append(
                DesktopContextMenuAction(
                    title: "ساخت پلی‌لیست جدید…",
                    systemImage: "plus.circle"
                ) {
                    onCreatePlaylistAndAdd(trackId)
                }
            )
            return actions
        }())
    }
}
