import Foundation

enum ArtistDetailsDataLoader {
    static func load(artistId: Int64, fallback: ArtistDetailsItem) async -> ArtistDetailsItem? {
        await Task.detached(priority: .userInitiated) {
            try? loadSync(artistId: artistId, fallback: fallback)
        }.value
    }

    private static func loadSync(artistId: Int64, fallback: ArtistDetailsItem) throws -> ArtistDetailsItem {
        let root = try resolveRepoRoot()
        let dbPath = try resolveArchiveDbPath(root: root)
        let payload = try runBridgeArtistDetails(root: root, dbPath: dbPath, artistId: artistId)
        guard !payload.isEmpty, !payload.contains("\"error\"") else {
            return fallback
        }

        let response = try JSONDecoder().decode(ArtistDetailBridgeResponse.self, from: Data(payload.utf8))
        let count = max(Int(response.trackCount), response.tracks.count)
        let stats: [ArtistStatItem] = {
            if !response.categoryCounts.isEmpty {
                return response.categoryCounts.prefix(4).map { item in
                    ArtistStatItem(value: toPersianDigits("\(item.count)"), label: item.title)
                }
            }
            return [ArtistStatItem(value: toPersianDigits("\(count)"), label: "برنامه")]
        }()

        let programs = response.tracks.map { item in
            let singers = item.artist.trimmingCharacters(in: .whitespacesAndNewlines)
            return ArtistProgramRow(
                title: item.title,
                subtitle: singers.isEmpty ? "—" : singers,
                duration: normalizedDuration(item.duration)
            )
        }

        let collaborators = response.collaborators.map { item in
            ArtistCollaboratorItem(
                sourceArtistId: item.id,
                name: item.name,
                role: item.role,
                imageURL: item.avatar ?? ""
            )
        }

        return ArtistDetailsItem(
            artistId: response.id,
            name: response.name,
            role: response.instrument?.isEmpty == false ? "نوازنده \(response.instrument!)" : fallback.role,
            imageURL: response.avatar ?? fallback.imageURL,
            totalProgramsText: toPersianDigits("\(count)"),
            stats: stats,
            programs: programs.isEmpty ? fallback.programs : programs,
            collaborators: collaborators.isEmpty ? fallback.collaborators : collaborators,
            featuredModes: response.topModes.isEmpty ? fallback.featuredModes : Array(response.topModes.prefix(4))
        )
    }

    private static func normalizedDuration(_ value: String?) -> String {
        guard let value, !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            return "۰۰:۰۰"
        }
        return value
    }

    private static func toPersianDigits(_ value: String) -> String {
        value
            .replacingOccurrences(of: "0", with: "۰")
            .replacingOccurrences(of: "1", with: "۱")
            .replacingOccurrences(of: "2", with: "۲")
            .replacingOccurrences(of: "3", with: "۳")
            .replacingOccurrences(of: "4", with: "۴")
            .replacingOccurrences(of: "5", with: "۵")
            .replacingOccurrences(of: "6", with: "۶")
            .replacingOccurrences(of: "7", with: "۷")
            .replacingOccurrences(of: "8", with: "۸")
            .replacingOccurrences(of: "9", with: "۹")
    }

    private static func resolveRepoRoot() throws -> String {
        let fileManager = FileManager.default
        let seeds = [
            fileManager.currentDirectoryPath,
            URL(fileURLWithPath: #filePath).deletingLastPathComponent().path
        ]

        for seed in seeds {
            var current = URL(fileURLWithPath: seed)
            while true {
                let marker = current.appendingPathComponent("core").path
                if fileManager.fileExists(atPath: marker) {
                    return current.path
                }

                let parent = current.deletingLastPathComponent()
                if parent.path == current.path {
                    break
                }
                current = parent
            }
        }

        throw NSError(domain: "ArtistDetailsDataLoader", code: 1, userInfo: [NSLocalizedDescriptionKey: "Could not resolve repository root"])
    }

    private static func resolveArchiveDbPath(root: String) throws -> String {
        if let env = ProcessInfo.processInfo.environment["RADIOGOLHA_ARCHIVE_DB"], !env.isEmpty {
            return env
        }

        let candidate = URL(fileURLWithPath: root).appendingPathComponent("database/golha_database.db").path
        if FileManager.default.fileExists(atPath: candidate) {
            return candidate
        }

        throw NSError(domain: "ArtistDetailsDataLoader", code: 2, userInfo: [NSLocalizedDescriptionKey: "Archive DB not found"])
    }

    private static func runBridgeArtistDetails(root: String, dbPath: String, artistId: Int64) throws -> String {
        let manifest = URL(fileURLWithPath: root).appendingPathComponent("core/adapters/macos/Cargo.toml").path
        let binary = URL(fileURLWithPath: root).appendingPathComponent("core/adapters/macos/target/debug/radiogolha-macos-bridge-cli").path

        if !FileManager.default.isExecutableFile(atPath: binary) {
            _ = try runProcess(
                launchPath: "/usr/bin/env",
                arguments: ["cargo", "build", "--manifest-path", manifest, "--bin", "radiogolha-macos-bridge-cli"],
                currentDirectory: root
            )
        }

        return try runProcess(
            launchPath: binary,
            arguments: ["artist-detail-json", "--db", dbPath, "--artist-id", String(artistId)],
            currentDirectory: root
        )
    }

    private static func runProcess(launchPath: String, arguments: [String], currentDirectory: String) throws -> String {
        let process = Process()
        process.executableURL = URL(fileURLWithPath: launchPath)
        process.arguments = arguments
        process.currentDirectoryURL = URL(fileURLWithPath: currentDirectory)

        let stdout = Pipe()
        let stderr = Pipe()
        process.standardOutput = stdout
        process.standardError = stderr

        let group = DispatchGroup()
        var outData = Data()
        var errData = Data()

        group.enter()
        DispatchQueue.global(qos: .userInitiated).async {
            outData = stdout.fileHandleForReading.readDataToEndOfFile()
            group.leave()
        }

        group.enter()
        DispatchQueue.global(qos: .userInitiated).async {
            errData = stderr.fileHandleForReading.readDataToEndOfFile()
            group.leave()
        }

        try process.run()
        process.waitUntilExit()
        group.wait()

        let out = String(data: outData, encoding: .utf8) ?? ""
        if process.terminationStatus == 0 {
            return out.trimmingCharacters(in: .whitespacesAndNewlines)
        }

        let err = String(data: errData, encoding: .utf8) ?? "Unknown process error"
        throw NSError(domain: "ArtistDetailsDataLoader", code: Int(process.terminationStatus), userInfo: [NSLocalizedDescriptionKey: err])
    }
}

private struct ArtistDetailBridgeResponse: Decodable {
    let id: Int64
    let name: String
    let avatar: String?
    let instrument: String?
    let trackCount: Int64
    let tracks: [ArtistDetailTrackDTO]
    let categoryCounts: [ArtistCategoryCountDTO]
    let collaborators: [ArtistCollaboratorDTO]
    let topModes: [String]
}

private struct ArtistDetailTrackDTO: Decodable {
    let id: Int64
    let title: String
    let no: Int64
    let artist: String
    let mode: String?
    let duration: String?
    let audioUrl: String?
}

private struct ArtistCategoryCountDTO: Decodable {
    let categoryId: Int64
    let title: String
    let count: Int64
}

private struct ArtistCollaboratorDTO: Decodable {
    let id: Int64
    let name: String
    let avatar: String?
    let kind: String
    let role: String
    let sharedCount: Int64
}
