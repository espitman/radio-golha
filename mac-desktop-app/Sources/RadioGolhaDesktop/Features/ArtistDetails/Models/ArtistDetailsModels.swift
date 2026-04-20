import Foundation

struct ArtistDetailsItem: Identifiable {
    let id: String
    let name: String
    let role: String
    let imageURL: String
    let stats: [ArtistStatItem]
    let programs: [ArtistProgramRow]
    let collaborators: [ArtistCollaboratorItem]
    let featuredModes: [String]

    init(
        name: String,
        role: String,
        imageURL: String,
        stats: [ArtistStatItem],
        programs: [ArtistProgramRow],
        collaborators: [ArtistCollaboratorItem],
        featuredModes: [String]
    ) {
        self.id = name
        self.name = name
        self.role = role
        self.imageURL = imageURL
        self.stats = stats
        self.programs = programs
        self.collaborators = collaborators
        self.featuredModes = featuredModes
    }
}

struct ArtistStatItem: Identifiable {
    let id = UUID()
    let value: String
    let label: String
}

struct ArtistProgramRow: Identifiable {
    let id = UUID()
    let title: String
    let subtitle: String
    let duration: String
}

struct ArtistCollaboratorItem: Identifiable {
    let id = UUID()
    let name: String
    let role: String
    let imageURL: String
}

enum ArtistDetailsFactory {
    static func fromArtistName(_ name: String) -> ArtistDetailsItem {
        if let singer = HomeMockData.singersPageItems.first(where: { $0.name == name }) {
            return fromSinger(singer)
        }
        if let player = HomeMockData.playersPageItems.first(where: { $0.name == name }) {
            return fromPlayer(player)
        }
        if let homeArtist = (HomeMockData.singers + HomeMockData.instrumentalists).first(where: { $0.name == name }) {
            return fromHomeArtist(homeArtist)
        }
        return make(name: name, role: "هنرمند", imageURL: HomeMockData.singers.first?.imageURL ?? "")
    }

    static func fromHomeArtist(_ artist: ArtistItem) -> ArtistDetailsItem {
        make(name: artist.name, role: artist.role, imageURL: artist.imageURL)
    }

    static func fromSinger(_ singer: SingerListItem) -> ArtistDetailsItem {
        make(name: singer.name, role: "خواننده", imageURL: singer.imageURL)
    }

    static func fromPlayer(_ player: PlayerListItem) -> ArtistDetailsItem {
        make(name: player.name, role: "نوازنده \(player.instrument)", imageURL: player.imageURL)
    }

    private static func make(name: String, role: String, imageURL: String) -> ArtistDetailsItem {
        let stats = statsByArtist[name] ?? defaultStats
        let programsBase = programsByArtist[name] ?? defaultPrograms
        let programs = repeatedPrograms(programsBase, targetCount: 20)
        let collaborators = collaboratorsByArtist[name] ?? defaultCollaborators(excluding: name)
        let modes = modesByArtist[name] ?? defaultModes

        return ArtistDetailsItem(
            name: name,
            role: role,
            imageURL: imageURL,
            stats: stats,
            programs: programs,
            collaborators: collaborators,
            featuredModes: modes
        )
    }

    private static let statsByArtist: [String: [ArtistStatItem]] = [
        "محمدرضا شجریان": [
            .init(value: "۴۸", label: "گلهای تازه"),
            .init(value: "۶۲", label: "گلهای رنگارنگ"),
            .init(value: "۱۰", label: "یک شاخه گل")
        ],
        "جلیل شهناز": [
            .init(value: "۵۶", label: "گلهای تازه"),
            .init(value: "۴۱", label: "گلهای رنگارنگ"),
            .init(value: "۸", label: "یک شاخه گل")
        ]
    ]

    private static let programsByArtist: [String: [ArtistProgramRow]] = [
        "محمدرضا شجریان": [
            .init(title: "گلهای تازه، شماره ۲۵ - آواز شور", subtitle: "برنامه گلهای تازه • فرامرز پایور", duration: "۴۲:۱۵"),
            .init(title: "یک شاخه گل، شماره ۴۰۲", subtitle: "برنامه یک شاخه گل • اسدالله ملک", duration: "۲۸:۴۰"),
            .init(title: "گلهای رنگارنگ، شماره ۵۸۰", subtitle: "برنامه گلهای رنگارنگ • جلیل شهناز", duration: "۳۵:۱۰"),
            .init(title: "گلهای تازه، شماره ۱۰ - ماهور", subtitle: "برنامه گلهای تازه • پرویز یاحقی", duration: "۳۸:۲۰"),
            .init(title: "یک شاخه گل، شماره ۴۱۲", subtitle: "برنامه یک شاخه گل • فرهنگ شریف", duration: "۲۵:۱۵")
        ]
    ]

    private static let collaboratorsByArtist: [String: [ArtistCollaboratorItem]] = [
        "محمدرضا شجریان": [
            .init(name: "جلیل شهناز", role: "استاد تار", imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuApBy3h3tvM_8zoiSOpKAVBrvz18FtfEruo4fravG9v7kvVDIlDeXPVl2IMscU_y3AXaJH7LGNBM8wDlyaktALxCAoepY-Ediyj-xmtkaodZSZ8fK_bJ3SExfRz7J9jnaiKsvJ1JnaG7MuHLrFWCwGbqQbp6aJ3AcdJZmRkZZMXKGixlZGRDlWxf7Pku3ItvZt5s-g7Aqomo3jTf_U0GaTnhfvpAIVhvXVGjgXMAHxkOEAvOP012-CH9tC33P2RCbOLtqbbYjsgiGXt"),
            .init(name: "فرامرز پایور", role: "استاد سنتور", imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuCsgRdW1B1M3YHnSlBE06hqQ5_l9p20aV1JucoLFUnGdzEPkGVtcQr16mCinUSNpV0_pQ0Xk-DbM0AHf_Cn3qxniTZ6tOQ0Sbt0TACcjITHEDCOcPuPZK3y1zftcfs9ZtoNTdm9TPn_2gzxC7M8rGaor3l-J8gKrvg9WlF0GnClqtWJOwW0rYuEvuUjZ18Z5xY7yZVLJozpjHXtFzN_JsFwHCoZF7JoVm3St5JZFLwoYa7Ny-OS9eohFbhIj_qYQDyMtXZa-Z5M5NIn"),
            .init(name: "حسن کسایی", role: "استاد نی", imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuBySGVnBqhZRyJ-vngmyf-Hd5PQndePKpM0WCl__KpAYTrn0dnSIk9Zjgs-KnsmYUH_ysqpJT1nsIvWCyWYvBv-h1wnzw8n8uLbpN4EzN-J_1IZrcc4ug0Vg-m6fLZnssUMUGYpoB2ZUD7gokhNIF0CyOK4QWM8FX_n-W84HUim4eL-mi0deyVurRese2PR0YMglqNqAZgAaSOubIteZa9EM-CCZeS5IeGJqAURy2yjBe1y-oxNfjXddDA2AHAxlHuzkGgl69d2ANtM"),
            .init(name: "اسدالله ملک", role: "استاد ویولن", imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuB12PkP3YeTLfT9n1_14FZsWkNXHZ6wh8QJKo-HiIww-UqBytZNgmhz5VZ5gPhhFKEzj2DME1rWk9MCpJWg7_bP4HRSCM0Uj_17p_oOEV7e6-I0qFoGkusoP_pxxp5cNZMgbPyBxQ8lkSmutqSOxckrCw3BwUGr0kU7ZJMVSD9ORFYJS6EfgNIKhYenVlgBRglV0cjWoN-8cFd3bLi_yKkSGOR-C-Xt9muu87oaOhQDAS_2douYADV2uRLnJ6jyAlV8QG2G7SFgHLvy")
        ]
    ]

    private static let modesByArtist: [String: [String]] = [
        "محمدرضا شجریان": ["شور", "افشاری", "همایون", "بیات ترک", "سه‌گاه"],
        "جلیل شهناز": ["شور", "ماهور", "اصفهان", "همایون"]
    ]

    private static let defaultStats: [ArtistStatItem] = [
        .init(value: "۲۶", label: "گلهای تازه"),
        .init(value: "۳۸", label: "گلهای رنگارنگ"),
        .init(value: "۷", label: "یک شاخه گل")
    ]

    private static let defaultPrograms: [ArtistProgramRow] = [
        .init(title: "گلهای تازه، شماره ۱۴", subtitle: "برنامه گلهای تازه • ارکستر گلها", duration: "۳۲:۱۰"),
        .init(title: "یک شاخه گل، شماره ۲۶۴", subtitle: "برنامه یک شاخه گل • تکنوازی", duration: "۲۵:۴۰"),
        .init(title: "گلهای رنگارنگ، شماره ۴۲۲", subtitle: "برنامه گلهای رنگارنگ • آواز", duration: "۲۹:۳۵")
    ]

    private static func repeatedPrograms(_ base: [ArtistProgramRow], targetCount: Int) -> [ArtistProgramRow] {
        guard !base.isEmpty, targetCount > 0 else { return [] }
        if base.count >= targetCount { return Array(base.prefix(targetCount)) }

        var result: [ArtistProgramRow] = []
        result.reserveCapacity(targetCount)

        for index in 0..<targetCount {
            let row = base[index % base.count]
            result.append(
                ArtistProgramRow(
                    title: row.title,
                    subtitle: row.subtitle,
                    duration: row.duration
                )
            )
        }

        return result
    }

    private static func defaultCollaborators(excluding currentName: String) -> [ArtistCollaboratorItem] {
        let pool: [ArtistCollaboratorItem] = [
            .init(name: "جلیل شهناز", role: "استاد تار", imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuA2hVZWR2Z0j1a7SSEmfnLEm3SrIidS3j2EnFElicEuVkB32-M3iGqpEBw-59fsUzo6mazQq9Cw5_uhIc6q0Rcdp6R3-3JH6LDovVJpYKoD2OZgVp--I5kblaeCvWspbFKH_5Z31YKu4Jcn8VxJjtibB9JWPWf8dBh-hS--B2f_2L6VGqfVeAQhyaOYc4w5Ybd-9Q_-p0ZRKje4L0BeAxIdyCesXx1A73fXKJdowlExvTeMsSaXPGNKXqiRfR9qy7W8P0EFR629xeny"),
            .init(name: "فرهنگ شریف", role: "استاد تار", imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuBGekEUXBcApNWh17_qhvE5zV19t7t5ysca3oZ9FEtdwVsgnWNzPE4paUYPvmlUTxB_uvpchE7NsLDop42Z2XcKwL3KuNH7xPuKmzxrW9WppCT6K3Ym293tsAh_vm9OS8Hzx6kAiAndqgLmOu222Rv2-SDcDfZmurPQhtjei3LmDPjmABFjBKvYHUWqvHv4YSbsYVrFKSdnEa6IlEZO0z3ZeNFnHgtk_e9PVNz6AVOaBCZJTcau2o2nG16iErn20SyVh-RV53NW5hsg"),
            .init(name: "حسن کسایی", role: "استاد نی", imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuBySGVnBqhZRyJ-vngmyf-Hd5PQndePKpM0WCl__KpAYTrn0dnSIk9Zjgs-KnsmYUH_ysqpJT1nsIvWCyWYvBv-h1wnzw8n8uLbpN4EzN-J_1IZrcc4ug0Vg-m6fLZnssUMUGYpoB2ZUD7gokhNIF0CyOK4QWM8FX_n-W84HUim4eL-mi0deyVurRese2PR0YMglqNqAZgAaSOubIteZa9EM-CCZeS5IeGJqAURy2yjBe1y-oxNfjXddDA2AHAxlHuzkGgl69d2ANtM"),
            .init(name: "اسدالله ملک", role: "استاد ویولن", imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuB12PkP3YeTLfT9n1_14FZsWkNXHZ6wh8QJKo-HiIww-UqBytZNgmhz5VZ5gPhhFKEzj2DME1rWk9MCpJWg7_bP4HRSCM0Uj_17p_oOEV7e6-I0qFoGkusoP_pxxp5cNZMgbPyBxQ8lkSmutqSOxckrCw3BwUGr0kU7ZJMVSD9ORFYJS6EfgNIKhYenVlgBRglV0cjWoN-8cFd3bLi_yKkSGOR-C-Xt9muu87oaOhQDAS_2douYADV2uRLnJ6jyAlV8QG2G7SFgHLvy")
        ]

        return pool.filter { $0.name != currentName }
    }

    private static let defaultModes = ["شور", "همایون", "سه‌گاه", "افشاری"]
}
