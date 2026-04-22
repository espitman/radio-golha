import SwiftUI

struct SettingsContentView: View {
    @State private var isUpdating = false
    @State private var isDownloading = false
    @State private var downloadProgress: Double = 0
    @State private var successMessage: String? = nil
    @State private var errorMessage: String? = nil
    @State private var updatedAtText: String? = nil

    var onDatabaseUpdated: () -> Void = {}

    var body: some View {
        ZStack {
            Palette.surface

            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    VStack(alignment: .leading, spacing: 0) {
                        Text("تنظیمات")
                            .font(.vazir(27, .bold))
                            .foregroundStyle(Palette.primary)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)

                    VStack(alignment: .leading, spacing: 14) {
                        Text("به‌روزرسانی اطلاعات آرشیو")
                            .font(.vazir(15, .bold))
                            .foregroundStyle(Palette.primary)

                        Text("برای دریافت آخرین اطلاعات، از این گزینه استفاده کنید.")
                            .font(.vazir(10))
                            .foregroundStyle(Palette.textMuted)
                            .multilineTextAlignment(.leading)
                            .frame(maxWidth: .infinity, alignment: .leading)

                        if let updatedAtText {
                            Text("آخرین به‌روزرسانی موفق: \(updatedAtText)")
                                .font(.vazir(9.5, .medium))
                                .foregroundStyle(Palette.textMuted)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }

                        HStack(spacing: 10) {
                            Button {
                                updateDatabase(forceDownload: false)
                            } label: {
                                HStack(spacing: 8) {
                                    if isUpdating {
                                        LoadingSpinner(color: .white, size: 12, lineWidth: 2)
                                    } else {
                                        Image(systemName: "arrow.down.circle.fill")
                                            .font(.system(size: 12, weight: .bold))
                                    }
                                    Text(isUpdating ? "در حال دریافت و جایگزینی..." : "به‌روزرسانی دیتابیس")
                                        .font(.vazir(10.5, .bold))
                                }
                                .foregroundStyle(.white)
                                .padding(.horizontal, 18)
                                .padding(.vertical, 11)
                                .background(Palette.primary, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
                            }
                            .buttonStyle(.plain)
                            .disabled(isUpdating)
                            .opacity(isUpdating ? 0.7 : 1)

                            Button {
                                updateDatabase(forceDownload: true)
                            } label: {
                                Text("به‌روزرسانی اجباری")
                                    .font(.vazir(10.5, .bold))
                                    .foregroundStyle(Palette.primary)
                                    .padding(.horizontal, 14)
                                    .padding(.vertical, 11)
                                    .background(Palette.surface, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                                            .stroke(Palette.border, lineWidth: 1)
                                    )
                            }
                            .buttonStyle(.plain)
                            .disabled(isUpdating)
                            .opacity(isUpdating ? 0.7 : 1)
                            .help("دانلود و جایگزینی مجدد حتی اگر نسخه فعلی یکسان باشد")
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)

                        if isDownloading {
                            VStack(alignment: .leading, spacing: 6) {
                                ProgressView(value: downloadProgress, total: 1)
                                    .tint(Palette.secondary)
                                Text("در حال دانلود: \(Int(downloadProgress * 100))٪")
                                    .font(.vazir(9))
                                    .foregroundStyle(Palette.textMuted)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                        }

                        if let successMessage {
                            Text(successMessage)
                                .font(.vazir(9.5, .bold))
                                .foregroundStyle(Color(hex: 0x0A6D31))
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }

                        if let errorMessage {
                            Text(errorMessage)
                                .font(.vazir(9.5, .bold))
                                .foregroundStyle(Color(hex: 0xA73333))
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                    }
                    .padding(20)
                    .background(Palette.surfaceLow, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
                    .overlay(
                        RoundedRectangle(cornerRadius: 16, style: .continuous)
                            .stroke(Palette.border, lineWidth: 1)
                    )
                }
                .padding(.horizontal, 48)
                .padding(.top, 32)
                .padding(.bottom, 24)
                .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
        .background(Palette.surface)
    }

    private func updateDatabase(forceDownload: Bool) {
        isUpdating = true
        isDownloading = false
        downloadProgress = 0
        successMessage = nil
        errorMessage = nil

        Task {
            do {
                let result = try await DesktopDatabaseUpdater.updateFromCdn(forceDownload: forceDownload) { progress in
                    DispatchQueue.main.async {
                        isDownloading = true
                        downloadProgress = max(0, min(1, progress))
                    }
                }
                await MainActor.run {
                    isUpdating = false
                    isDownloading = false
                    if forceDownload {
                        successMessage = result.didUpdate
                            ? "دیتابیس مجدداً دریافت و جایگزین شد."
                            : "دیتابیس همین الان هم به‌روز است."
                    } else {
                        successMessage = result.didUpdate
                            ? "دیتابیس با موفقیت به‌روزرسانی شد."
                            : "دیتابیس همین الان هم به‌روز است."
                    }
                    errorMessage = nil
                    updatedAtText = result.releasedAt
                    if result.didUpdate {
                        onDatabaseUpdated()
                    }
                }
            } catch {
                await MainActor.run {
                    isUpdating = false
                    isDownloading = false
                    successMessage = nil
                    errorMessage = error.localizedDescription
                }
            }
        }
    }
}
