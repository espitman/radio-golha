import SwiftUI

struct ProgramsSection: View {
    var body: some View {
        VStack(spacing: 32) {
            SectionHeader(title: "برنامه‌ها")

            HStack(spacing: 24) {
                ForEach(HomeMockData.programs) { item in
                    ProgramCard(item: item)
                }
            }
        }
    }
}
