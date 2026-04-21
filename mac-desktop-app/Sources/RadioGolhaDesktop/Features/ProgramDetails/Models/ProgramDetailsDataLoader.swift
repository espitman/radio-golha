import Foundation

enum ProgramDetailsDataLoader {
    static func load(programId: Int64, fallbackTitle: String) async -> ProgramDetailsItem? {
        await Task.detached(priority: .userInitiated) {
            try? loadSync(programId: programId, fallbackTitle: fallbackTitle)
        }.value
    }

    private static func loadSync(programId: Int64, fallbackTitle: String) throws -> ProgramDetailsItem {
        let root = try resolveRepoRoot()
        let dbPath = try resolveArchiveDbPath(root: root)
        let payload = try runBridgeProgramDetail(root: root, dbPath: dbPath, programId: programId)
        guard !payload.isEmpty, !payload.contains("\"error\"") else {
            return ProgramDetailsFactory.fromTrackTitle(fallbackTitle)
        }

        let response = try JSONDecoder().decode(ProgramDetailBridgeResponse?.self, from: Data(payload.utf8))
        guard let response else {
            return ProgramDetailsFactory.fromTrackTitle(fallbackTitle)
        }

        let artists = mapArtists(from: response)
        let timeline = response.timeline.map { segment in
            ProgramTimelineItem(
                time: normalizeTime(segment.startTime),
                segmentTitle: segment.modeName?.trimmingCharacters(in: .whitespacesAndNewlines).nilIfBlank ?? "بخش برنامه",
                singer: segment.singers.joined(separator: " / ").nilIfBlank ?? "—",
                musician: segment.performers.map { $0.name }.joined(separator: " / ").nilIfBlank ?? "—",
                poet: segment.poets.joined(separator: " / ").nilIfBlank ?? "—"
            )
        }

        let lyrics = response.transcript
            .sorted { lhs, rhs in
                if lhs.segmentOrder == rhs.segmentOrder {
                    return lhs.verseOrder < rhs.verseOrder
                }
                return lhs.segmentOrder < rhs.segmentOrder
            }
            .map { ProgramLyricItem(text: $0.text) }

        let title = response.title.trimmingCharacters(in: .whitespacesAndNewlines)
        let cover = artists.compactMap { $0.imageURL.nilIfBlank }.first ?? ""
        let orchestra = response.orchestraLeaders.first?.orchestra
            ?? response.orchestras.first?.name
            ?? "—"

        return ProgramDetailsItem(
            programId: response.id,
            title: title.isEmpty ? fallbackTitle : title,
            modeTitle: response.modes.first?.nilIfBlank ?? "—",
            totalDuration: normalizeTime(response.duration),
            orchestra: orchestra,
            coverImageURL: cover,
            artists: artists,
            timeline: timeline,
            lyrics: lyrics
        )
    }

    private static func mapArtists(from response: ProgramDetailBridgeResponse) -> [ProgramArtistItem] {
        var seen = Set<String>()
        var result: [ProgramArtistItem] = []

        func append(_ name: String, _ role: String, _ avatar: String?) {
            let trimmedName = name.trimmingCharacters(in: .whitespacesAndNewlines)
            guard !trimmedName.isEmpty else { return }
            let key = "\(trimmedName)|\(role)"
            guard seen.insert(key).inserted else { return }
            result.append(
                ProgramArtistItem(
                    name: trimmedName,
                    role: role,
                    imageURL: avatar ?? ""
                )
            )
        }

        response.singers.forEach { append($0.name, "خواننده", $0.avatar) }
        response.performers.forEach {
            let role = $0.instrument?
                .trimmingCharacters(in: .whitespacesAndNewlines)
                .nilIfBlank
                .map { "نوازنده \($0)" } ?? "نوازنده"
            append($0.name, role, $0.avatar)
        }
        response.poets.forEach { append($0.name, "شاعر", $0.avatar) }
        response.announcers.forEach { append($0.name, "گوینده", $0.avatar) }
        response.composers.forEach { append($0.name, "آهنگساز", $0.avatar) }
        response.arrangers.forEach { append($0.name, "تنظیم‌کننده", $0.avatar) }

        return result
    }

    private static func normalizeTime(_ value: String?) -> String {
        value?.trimmingCharacters(in: .whitespacesAndNewlines).nilIfBlank ?? "۰۰:۰۰"
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

        throw NSError(domain: "ProgramDetailsDataLoader", code: 1, userInfo: [NSLocalizedDescriptionKey: "Could not resolve repository root"])
    }

    private static func resolveArchiveDbPath(root: String) throws -> String {
        if let env = ProcessInfo.processInfo.environment["RADIOGOLHA_ARCHIVE_DB"], !env.isEmpty {
            return env
        }

        let candidate = URL(fileURLWithPath: root).appendingPathComponent("database/golha_database.db").path
        if FileManager.default.fileExists(atPath: candidate) {
            return candidate
        }

        throw NSError(domain: "ProgramDetailsDataLoader", code: 2, userInfo: [NSLocalizedDescriptionKey: "Archive DB not found"])
    }

    private static func runBridgeProgramDetail(root: String, dbPath: String, programId: Int64) throws -> String {
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
            arguments: ["program-detail-json", "--db", dbPath, "--program-id", String(programId)],
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
        throw NSError(domain: "ProgramDetailsDataLoader", code: Int(process.terminationStatus), userInfo: [NSLocalizedDescriptionKey: err])
    }
}

private struct ProgramDetailBridgeResponse: Decodable {
    let id: Int64
    let title: String
    let duration: String?
    let singers: [ProgramDetailArtistCreditDTO]
    let poets: [ProgramDetailArtistCreditDTO]
    let announcers: [ProgramDetailArtistCreditDTO]
    let composers: [ProgramDetailArtistCreditDTO]
    let arrangers: [ProgramDetailArtistCreditDTO]
    let modes: [String]
    let orchestras: [ProgramDetailArtistCreditDTO]
    let orchestraLeaders: [ProgramDetailOrchestraLeaderDTO]
    let performers: [ProgramDetailPerformerDTO]
    let timeline: [ProgramDetailTimelineSegmentDTO]
    let transcript: [ProgramDetailTranscriptVerseDTO]
}

private struct ProgramDetailArtistCreditDTO: Decodable {
    let name: String
    let avatar: String?
}

private struct ProgramDetailPerformerDTO: Decodable {
    let name: String
    let avatar: String?
    let instrument: String?
}

private struct ProgramDetailOrchestraLeaderDTO: Decodable {
    let orchestra: String
}

private struct ProgramDetailTimelineSegmentDTO: Decodable {
    let startTime: String?
    let modeName: String?
    let singers: [String]
    let poets: [String]
    let performers: [ProgramDetailPerformerDTO]
}

private struct ProgramDetailTranscriptVerseDTO: Decodable {
    let segmentOrder: Int
    let verseOrder: Int
    let text: String
}

private extension String {
    var nilIfBlank: String? {
        trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? nil : self
    }
}
