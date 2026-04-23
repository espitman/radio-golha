import Foundation

enum PlayersDataLoader {
    static func load() async -> [PlayerListItem] {
        await Task.detached(priority: .userInitiated) {
            (try? loadSync()) ?? []
        }.value
    }

    private static func loadSync() throws -> [PlayerListItem] {
        let root = try resolveRepoRoot()
        let dbPath = try resolveArchiveDbPath(root: root)
        let payload = try runBridgeMusicians(root: root, dbPath: dbPath)
        guard !payload.isEmpty, !payload.contains("\"error\"") else { return [] }

        let response = try JSONDecoder().decode([MusicianDTO].self, from: Data(payload.utf8))
        return response.map { item in
            PlayerListItem(
                sourceArtistId: item.id,
                name: item.name,
                instrument: item.instrument,
                programsCount: "\(toPersianDigits("\(item.programCount)")) برنامه ثبت شده",
                imageURL: item.avatar ?? ""
            )
        }
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

        throw NSError(domain: "PlayersDataLoader", code: 1, userInfo: [NSLocalizedDescriptionKey: "Could not resolve repository root"])
    }

    private static func resolveArchiveDbPath(root: String) throws -> String {
        if let env = ProcessInfo.processInfo.environment["RADIOGOLHA_ARCHIVE_DB"], !env.isEmpty {
            return env
        }

        let candidate = URL(fileURLWithPath: root).appendingPathComponent("database/golha_database.db").path
        if FileManager.default.fileExists(atPath: candidate) {
            return candidate
        }

        throw NSError(domain: "PlayersDataLoader", code: 2, userInfo: [NSLocalizedDescriptionKey: "Archive DB not found"])
    }

    private static func runBridgeMusicians(root: String, dbPath: String) throws -> String {
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
            arguments: ["musicians-json", "--db", dbPath],
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
        throw NSError(domain: "PlayersDataLoader", code: Int(process.terminationStatus), userInfo: [NSLocalizedDescriptionKey: err])
    }
}

private struct MusicianDTO: Decodable {
    let id: Int64
    let name: String
    let instrument: String
    let avatar: String?
    let programCount: Int64
}
