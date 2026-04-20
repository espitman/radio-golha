// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "RadioGolhaDesktop",
    platforms: [.macOS(.v13)],
    products: [
        .executable(name: "RadioGolhaDesktop", targets: ["RadioGolhaDesktop"])
    ],
    targets: [
        .executableTarget(
            name: "RadioGolhaDesktop",
            path: "Sources/RadioGolhaDesktop"
        )
    ]
)
