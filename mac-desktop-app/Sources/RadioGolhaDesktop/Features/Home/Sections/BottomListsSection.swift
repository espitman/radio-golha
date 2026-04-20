import SwiftUI

struct BottomListsSection: View {
    var body: some View {
        HStack(spacing: 24) {
            TrackListCard(title: "برترین برنامه‌ها", rows: HomeMockData.topProgramsRows)
            TrackListCard(title: "شنیده‌شده‌های اخیر", rows: HomeMockData.latestTracksRows)
        }
    }
}
