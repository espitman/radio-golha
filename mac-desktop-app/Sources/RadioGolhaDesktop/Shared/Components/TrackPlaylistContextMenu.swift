import SwiftUI

extension View {
    func trackPlaylistContextMenu(
        trackId: Int64?,
        playlists: [DesktopManualPlaylist],
        onAddToPlaylist: @escaping (Int64, Int64) -> Void,
        onCreatePlaylistAndAdd: @escaping (Int64) -> Void
    ) -> some View {
        contextMenu {
            if let trackId {
                if playlists.isEmpty {
                    Button("ایجاد لیست جدید و افزودن") {
                        onCreatePlaylistAndAdd(trackId)
                    }
                } else {
                    Menu("افزودن به پلی‌لیست") {
                        ForEach(playlists) { playlist in
                            Button(playlist.name) {
                                onAddToPlaylist(playlist.id, trackId)
                            }
                        }
                    }
                    Button("ایجاد لیست جدید و افزودن") {
                        onCreatePlaylistAndAdd(trackId)
                    }
                }
            } else {
                Text("شناسه ترک موجود نیست")
            }
        }
    }
}
