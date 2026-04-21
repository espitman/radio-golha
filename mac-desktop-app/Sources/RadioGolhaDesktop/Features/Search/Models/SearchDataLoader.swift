import Foundation

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
