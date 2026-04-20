import Foundation

enum HomeMockData {
    static let programs: [ProgramItem] = [
        .init(title: "گل‌های تازه", count: "۱۵۵ برنامه", symbol: "seal"),
        .init(title: "برگ سبز", count: "۳۱۲ برنامه", symbol: "leaf"),
        .init(title: "یک شاخه گل", count: "۴۶۵ برنامه", symbol: "sparkles"),
        .init(title: "گل‌های\nجاویدان", count: "۱۰۱ برنامه", symbol: "hexagon")
    ]

    static let singers: [ArtistItem] = [
        .init(
            name: "محمدرضا شجریان",
            role: "خواننده",
            imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuBQlX0v-Lh1Lj6CxDQYOd-xCKyNArhNLKOXT1e6WR3j251g0iUPqfUXkoPd4gjIMgt1QeRMHZDCbQeXgk7e6hsvtIN7AJzWyjXu8BRxJdWLB1WDSgoyYfG2_3oT6a9fFzoQtYdeZe430yfJn_MdqX-yfmEECMgIIn8tLeQLf8RO78T8vzpr-c1wSqYYgXEUGfN4KzDYQ7vH5P4PMUb9Hd7ufSqPmAeJmC59z2G7fYemqLFnCjTVpN0pHuR295sHnCYO1LAERIc-5CI3"
        ),
        .init(
            name: "غلامحسین بنان",
            role: "خواننده",
            imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuCdxuO-btnXsk1jNpqhy440lM0cy98lRfDi1JAptVTgEX2Fsd20qzP5qdPm4Uyu9rzx-xCJKzIIKMklM9eum8R1VtG4twX-3SPLQQUBnnXlPCetNsBS9VQc3tYQgP5sjejwOy4-lmGLkfbTz8936tpKD_B0OJPamCaYwMrOV8wuT0xTtpzprbbol2QJem2cYn7aRphjkJD3NvDSv2m_WSAYqqSfcL9czHeJl__fvDr074p20jojBew89tr1330zt2CEnUGhkGTXOAdO"
        ),
        .init(
            name: "بانو دلکش",
            role: "خواننده",
            imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuA2hVZWR2Z0j1a7SSEmfnLEm3SrIidS3j2EnFElicEuVkB32-M3iGqpEBw-59fsUzo6mazQq9Cw5_uhIc6q0Rcdp6R3-3JH6LDovVJpYKoD2OZgVp--I5kblaeCvWspbFKH_5Z31YKu4Jcn8VxJjtibB9JWPWf8dBh-hS--B2f_2L6VGqfVeAQhyaOYc4w5Ybd-9Q_-p0ZRKje4L0BeAxIdyCesXx1A73fXKJdowlExvTeMsSaXPGNKXqiRfR9qy7W8P0EFR629xeny"
        ),
        .init(
            name: "بانو مرضیه",
            role: "خواننده",
            imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuBySGVnBqhZRyJ-vngmyf-Hd5PQndePKpM0WCl__KpAYTrn0dnSIk9Zjgs-KnsmYUH_ysqpJT1nsIvWCyWYvBv-h1wnzw8n8uLbpN4EzN-J_1IZrcc4ug0Vg-m6fLZnssUMUGYpoB2ZUD7gokhNIF0CyOK4QWM8FX_n-W84HUim4eL-mi0deyVurRese2PR0YMglqNqAZgAaSOubIteZa9EM-CCZeS5IeGJqAURy2yjBe1y-oxNfjXddDA2AHAxlHuzkGgl69d2ANtM"
        )
    ]

    static let instrumentalists: [ArtistItem] = [
        .init(
            name: "جلیل شهناز",
            role: "نوازنده تار",
            imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuA2hVZWR2Z0j1a7SSEmfnLEm3SrIidS3j2EnFElicEuVkB32-M3iGqpEBw-59fsUzo6mazQq9Cw5_uhIc6q0Rcdp6R3-3JH6LDovVJpYKoD2OZgVp--I5kblaeCvWspbFKH_5Z31YKu4Jcn8VxJjtibB9JWPWf8dBh-hS--B2f_2L6VGqfVeAQhyaOYc4w5Ybd-9Q_-p0ZRKje4L0BeAxIdyCesXx1A73fXKJdowlExvTeMsSaXPGNKXqiRfR9qy7W8P0EFR629xeny"
        ),
        .init(
            name: "حسن کسایی",
            role: "نوازنده نی",
            imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuBySGVnBqhZRyJ-vngmyf-Hd5PQndePKpM0WCl__KpAYTrn0dnSIk9Zjgs-KnsmYUH_ysqpJT1nsIvWCyWYvBv-h1wnzw8n8uLbpN4EzN-J_1IZrcc4ug0Vg-m6fLZnssUMUGYpoB2ZUD7gokhNIF0CyOK4QWM8FX_n-W84HUim4eL-mi0deyVurRese2PR0YMglqNqAZgAaSOubIteZa9EM-CCZeS5IeGJqAURy2yjBe1y-oxNfjXddDA2AHAxlHuzkGgl69d2ANtM"
        ),
        .init(
            name: "فرهنگ شریف",
            role: "نوازنده تار",
            imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuBGekEUXBcApNWh17_qhvE5zV19t7t5ysca3oZ9FEtdwVsgnWNzPE4paUYPvmlUTxB_uvpchE7NsLDop42Z2XcKwL3KuNH7xPuKmzxrW9WppCT6K3Ym293tsAh_vm9OS8Hzx6kAiAndqgLmOu222Rv2-SDcDfZmurPQhtjei3LmDPjmABFjBKvYHUWqvHv4YSbsYVrFKSdnEa6IlEZO0z3ZeNFnHgtk_e9PVNz6AVOaBCZJTcau2o2nG16iErn20SyVh-RV53NW5hsg"
        ),
        .init(
            name: "حبیب‌الله بدیعی",
            role: "نوازنده ویولن",
            imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuBySGVnBqhZRyJ-vngmyf-Hd5PQndePKpM0WCl__KpAYTrn0dnSIk9Zjgs-KnsmYUH_ysqpJT1nsIvWCyWYvBv-h1wnzw8n8uLbpN4EzN-J_1IZrcc4ug0Vg-m6fLZnssUMUGYpoB2ZUD7gokhNIF0CyOK4QWM8FX_n-W84HUim4eL-mi0deyVurRese2PR0YMglqNqAZgAaSOubIteZa9EM-CCZeS5IeGJqAURy2yjBe1y-oxNfjXddDA2AHAxlHuzkGgl69d2ANtM"
        )
    ]

    static let modes: [ModeItem] = [
        .init(title: "شور"), .init(title: "ماهور"), .init(title: "همایون"), .init(title: "اصفهان"),
        .init(title: "سه‌گاه"), .init(title: "چهارگاه"), .init(title: "راست‌پنجگاه")
    ]

    static let topProgramsRows: [TrackRowItem] = [
        .init(title: "گلهای تازه شماره ۱۴", subtitle: "الهه، خالقی، بدیعی", duration: "۱۰:۱۵"),
        .init(title: "جاویدان شماره ۲۵", subtitle: "بنان، معروفی، فرهنگ", duration: "۰۶:۳۰")
    ]

    static let latestTracksRows: [TrackRowItem] = [
        .init(title: "آستان جانان", subtitle: "محمدرضا شجریان", duration: "۱۰:۱۵"),
        .init(title: "کاروان", subtitle: "غلامحسین بنان", duration: "۰۶:۳۰")
    ]
}
