import Foundation

enum SearchMatchMode: String {
    case any
    case all
}

struct SearchProgramFilters {
    var transcriptQuery: String?
    var categoryIds: [Int64]
    var singerIds: [Int64]
    var singerMatch: SearchMatchMode
    var modeIds: [Int64]
    var modeMatch: SearchMatchMode
    var orchestraIds: [Int64]
    var orchestraMatch: SearchMatchMode
    var instrumentIds: [Int64]
    var instrumentMatch: SearchMatchMode
    var performerIds: [Int64]
    var performerMatch: SearchMatchMode
    var poetIds: [Int64]
    var poetMatch: SearchMatchMode
    var announcerIds: [Int64]
    var announcerMatch: SearchMatchMode
    var composerIds: [Int64]
    var composerMatch: SearchMatchMode
    var arrangerIds: [Int64]
    var arrangerMatch: SearchMatchMode
    var orchestraLeaderIds: [Int64]
    var orchestraLeaderMatch: SearchMatchMode

    static let empty = SearchProgramFilters(
        transcriptQuery: nil,
        categoryIds: [],
        singerIds: [],
        singerMatch: .any,
        modeIds: [],
        modeMatch: .any,
        orchestraIds: [],
        orchestraMatch: .any,
        instrumentIds: [],
        instrumentMatch: .any,
        performerIds: [],
        performerMatch: .any,
        poetIds: [],
        poetMatch: .any,
        announcerIds: [],
        announcerMatch: .any,
        composerIds: [],
        composerMatch: .any,
        arrangerIds: [],
        arrangerMatch: .any,
        orchestraLeaderIds: [],
        orchestraLeaderMatch: .any
    )

    var hasAnyFilter: Bool {
        let hasIds = !categoryIds.isEmpty || !singerIds.isEmpty || !modeIds.isEmpty || !orchestraIds.isEmpty ||
            !instrumentIds.isEmpty || !performerIds.isEmpty || !poetIds.isEmpty || !announcerIds.isEmpty ||
            !composerIds.isEmpty || !arrangerIds.isEmpty || !orchestraLeaderIds.isEmpty
        return hasIds || !(transcriptQuery?.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ?? true)
    }

    var restoreKey: String {
        func joined(_ values: [Int64]) -> String {
            values.sorted().map(String.init).joined(separator: ",")
        }
        let transcript = transcriptQuery?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        return [
            transcript,
            joined(categoryIds),
            joined(singerIds), singerMatch.rawValue,
            joined(modeIds), modeMatch.rawValue,
            joined(orchestraIds), orchestraMatch.rawValue,
            joined(instrumentIds), instrumentMatch.rawValue,
            joined(performerIds), performerMatch.rawValue,
            joined(poetIds), poetMatch.rawValue,
            joined(announcerIds), announcerMatch.rawValue,
            joined(composerIds), composerMatch.rawValue,
            joined(arrangerIds), arrangerMatch.rawValue,
            joined(orchestraLeaderIds), orchestraLeaderMatch.rawValue
        ].joined(separator: "|")
    }
}

enum SearchFilterKind: String, Hashable {
    case category
    case singer
    case mode
    case orchestra
    case instrument
    case performer
    case poet
    case announcer
    case composer
    case arranger
    case orchestraLeader
    case transcript
}

struct SearchActiveChip: Identifiable, Hashable {
    let kind: SearchFilterKind
    let valueId: Int64?
    let label: String
    var id: String {
        if let valueId {
            return "\(kind.rawValue)-\(valueId)"
        }
        return "\(kind.rawValue)-text"
    }
}

struct SearchProgramResults {
    let tracks: [TrackRowItem]
    let total: Int
}

struct SearchLoadedOptions {
    var categories: [SearchDataOption]
    var singers: [SearchDataOption]
    var modes: [SearchDataOption]
    var orchestras: [SearchDataOption]
    var instruments: [SearchDataOption]
    var performers: [SearchDataOption]
    var poets: [SearchDataOption]
    var announcers: [SearchDataOption]
    var composers: [SearchDataOption]
    var arrangers: [SearchDataOption]
    var orchestraLeaders: [SearchDataOption]
}

struct SearchDataOption {
    let id: String
    let name: String
    let avatarURL: String?
}

enum SearchDataLoader {
    static func load() async -> SearchLoadedOptions? {
        await Task.detached(priority: .userInitiated) {
            try? loadSync()
        }.value
    }

    static func searchPrograms(filters: SearchProgramFilters) async -> SearchProgramResults? {
        await Task.detached(priority: .userInitiated) {
            try? searchProgramsSync(filters: filters)
        }.value
    }

    private static func loadSync() throws -> SearchLoadedOptions {
        let root = try resolveRepoRoot()
        let dbPath = try resolveArchiveDbPath(root: root)

        let optionsPayload = try runBridge(
            root: root,
            arguments: ["search-options-json", "--db", dbPath]
        )
        guard !optionsPayload.isEmpty, !optionsPayload.contains("\"error\"") else {
            throw NSError(domain: "SearchDataLoader", code: 3, userInfo: [NSLocalizedDescriptionKey: "Empty search options payload"])
        }

        let options = try JSONDecoder().decode(SearchOptionsBridgeResponse.self, from: Data(optionsPayload.utf8))

        let singersPayload = (try? runBridge(root: root, arguments: ["singers-json", "--db", dbPath])) ?? "[]"
        let performersPayload = (try? runBridge(root: root, arguments: ["musicians-json", "--db", dbPath])) ?? "[]"

        let singers = (try? JSONDecoder().decode([SearchArtistDTO].self, from: Data(singersPayload.utf8))) ?? []
        let performers = (try? JSONDecoder().decode([SearchMusicianDTO].self, from: Data(performersPayload.utf8))) ?? []

        let singerAvatarByName = Dictionary(uniqueKeysWithValues: singers.map { (normalizeName($0.name), $0.avatar) })
        let performerAvatarByName = Dictionary(uniqueKeysWithValues: performers.map { (normalizeName($0.name), $0.avatar) })

        func toOption(_ item: SearchOptionDTO) -> SearchDataOption {
            let title = (item.name ?? item.titleFa ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
            return SearchDataOption(
                id: String(item.id),
                name: title,
                avatarURL: nil
            )
        }

        func dedupe(_ items: [SearchDataOption]) -> [SearchDataOption] {
            var seen = Set<String>()
            return items.filter { !($0.name.isEmpty) && seen.insert($0.id).inserted }
        }

        let singerOptions = dedupe(options.singers.map { item in
            let base = toOption(item)
            return SearchDataOption(id: base.id, name: base.name, avatarURL: singerAvatarByName[normalizeName(base.name)] ?? nil)
        })

        let performerOptions = dedupe(options.performers.map { item in
            let base = toOption(item)
            return SearchDataOption(id: base.id, name: base.name, avatarURL: performerAvatarByName[normalizeName(base.name)] ?? nil)
        })

        return SearchLoadedOptions(
            categories: dedupe(options.categories.map(toOption)),
            singers: singerOptions,
            modes: dedupe(options.modes.map(toOption)),
            orchestras: dedupe(options.orchestras.map(toOption)),
            instruments: dedupe(options.instruments.map(toOption)),
            performers: performerOptions,
            poets: dedupe(options.poets.map(toOption)),
            announcers: dedupe(options.announcers.map(toOption)),
            composers: dedupe(options.composers.map(toOption)),
            arrangers: dedupe(options.arrangers.map(toOption)),
            orchestraLeaders: dedupe(options.orchestraLeaders.map(toOption))
        )
    }

    private static func searchProgramsSync(filters: SearchProgramFilters) throws -> SearchProgramResults {
        let root = try resolveRepoRoot()
        let dbPath = try resolveArchiveDbPath(root: root)

        let request = SearchProgramsRequestDTO(
            transcriptQuery: filters.transcriptQuery?.trimmingCharacters(in: .whitespacesAndNewlines).nilIfEmpty,
            page: 1,
            categoryIds: filters.categoryIds,
            singerIds: filters.singerIds,
            singerMatch: filters.singerMatch.rawValue,
            modeIds: filters.modeIds,
            modeMatch: filters.modeMatch.rawValue,
            orchestraIds: filters.orchestraIds,
            orchestraMatch: filters.orchestraMatch.rawValue,
            instrumentIds: filters.instrumentIds,
            instrumentMatch: filters.instrumentMatch.rawValue,
            performerIds: filters.performerIds,
            performerMatch: filters.performerMatch.rawValue,
            poetIds: filters.poetIds,
            poetMatch: filters.poetMatch.rawValue,
            announcerIds: filters.announcerIds,
            announcerMatch: filters.announcerMatch.rawValue,
            composerIds: filters.composerIds,
            composerMatch: filters.composerMatch.rawValue,
            arrangerIds: filters.arrangerIds,
            arrangerMatch: filters.arrangerMatch.rawValue,
            orchestraLeaderIds: filters.orchestraLeaderIds,
            orchestraLeaderMatch: filters.orchestraLeaderMatch.rawValue
        )
        let filtersJson = String(data: try JSONEncoder().encode(request), encoding: .utf8) ?? "{}"

        let payload = try runBridge(
            root: root,
            arguments: ["search-programs-json", "--db", dbPath, "--filters-json", filtersJson]
        )
        guard !payload.isEmpty, !payload.contains("\"error\"") else {
            return SearchProgramResults(tracks: [], total: 0)
        }

        let response = try JSONDecoder().decode(SearchProgramsResponseDTO.self, from: Data(payload.utf8))
        let rows = response.rows.map { row in
            let title = row.title?.trimmingCharacters(in: .whitespacesAndNewlines).nilIfEmpty ?? "برنامه \(row.no)"
            return TrackRowItem(
                id: "track-\(row.id)",
                trackId: row.id,
                title: title,
                subtitle: row.artist?.trimmingCharacters(in: .whitespacesAndNewlines).nilIfEmpty ?? row.categoryName,
                duration: row.duration?.trimmingCharacters(in: .whitespacesAndNewlines).nilIfEmpty ?? "۰۰:۰۰",
                audioURL: row.audioUrl,
                artworkURLs: []
            )
        }

        return SearchProgramResults(tracks: rows, total: Int(response.total))
    }

    private static func normalizeName(_ value: String) -> String {
        value
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: "ي", with: "ی")
            .replacingOccurrences(of: "ك", with: "ک")
            .folding(options: [.diacriticInsensitive, .caseInsensitive], locale: Locale(identifier: "fa_IR"))
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

        throw NSError(domain: "SearchDataLoader", code: 1, userInfo: [NSLocalizedDescriptionKey: "Could not resolve repository root"])
    }

    private static func resolveArchiveDbPath(root: String) throws -> String {
        if let env = ProcessInfo.processInfo.environment["RADIOGOLHA_ARCHIVE_DB"], !env.isEmpty {
            return env
        }

        let candidate = URL(fileURLWithPath: root).appendingPathComponent("database/golha_database.db").path
        if FileManager.default.fileExists(atPath: candidate) {
            return candidate
        }

        throw NSError(domain: "SearchDataLoader", code: 2, userInfo: [NSLocalizedDescriptionKey: "Archive DB not found"])
    }

    private static func runBridge(root: String, arguments: [String]) throws -> String {
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
            arguments: arguments,
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
        throw NSError(domain: "SearchDataLoader", code: Int(process.terminationStatus), userInfo: [NSLocalizedDescriptionKey: err])
    }
}

private extension String {
    var nilIfEmpty: String? {
        isEmpty ? nil : self
    }
}

private struct SearchOptionsBridgeResponse: Decodable {
    let categories: [SearchOptionDTO]
    let singers: [SearchOptionDTO]
    let modes: [SearchOptionDTO]
    let orchestras: [SearchOptionDTO]
    let instruments: [SearchOptionDTO]
    let performers: [SearchOptionDTO]
    let poets: [SearchOptionDTO]
    let announcers: [SearchOptionDTO]
    let composers: [SearchOptionDTO]
    let arrangers: [SearchOptionDTO]
    let orchestraLeaders: [SearchOptionDTO]

    enum CodingKeys: String, CodingKey {
        case categories
        case singers
        case modes
        case orchestras
        case instruments
        case performers
        case poets
        case announcers
        case composers
        case arrangers
        case orchestraLeaders
    }
}

private struct SearchOptionDTO: Decodable {
    let id: Int64
    let name: String?
    let titleFa: String?
}

private struct SearchArtistDTO: Decodable {
    let id: Int64
    let name: String
    let avatar: String?
}

private struct SearchMusicianDTO: Decodable {
    let id: Int64
    let name: String
    let instrument: String
    let avatar: String?
}

private struct SearchProgramsRequestDTO: Encodable {
    let transcriptQuery: String?
    let page: Int
    let categoryIds: [Int64]
    let singerIds: [Int64]
    let singerMatch: String
    let modeIds: [Int64]
    let modeMatch: String
    let orchestraIds: [Int64]
    let orchestraMatch: String
    let instrumentIds: [Int64]
    let instrumentMatch: String
    let performerIds: [Int64]
    let performerMatch: String
    let poetIds: [Int64]
    let poetMatch: String
    let announcerIds: [Int64]
    let announcerMatch: String
    let composerIds: [Int64]
    let composerMatch: String
    let arrangerIds: [Int64]
    let arrangerMatch: String
    let orchestraLeaderIds: [Int64]
    let orchestraLeaderMatch: String
}

private struct SearchProgramsResponseDTO: Decodable {
    let rows: [SearchProgramRowDTO]
    let total: Int64
    let page: Int64
    let totalPages: Int64?
}

private struct SearchProgramRowDTO: Decodable {
    let id: Int64
    let title: String?
    let categoryName: String
    let no: Int64
    let subNo: String?
    let duration: String?
    let audioUrl: String?
    let artist: String?

    enum CodingKeys: String, CodingKey {
        case id
        case title
        case categoryName = "category_name"
        case no
        case subNo = "sub_no"
        case duration
        case audioUrl = "audio_url"
        case artist
    }
}
