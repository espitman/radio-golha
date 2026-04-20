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

    static let singersPageAlphabet: [String] = [
        "همه", "الف", "ب", "پ", "ت", "ج", "چ", "ح", "خ", "د", "ر", "ز", "س", "ش", "ع", "ق", "م", "ن", "و", "ه", "ی"
    ]

    static let singersPageItems: [SingerListItem] = [
        .init(
            name: "محمدرضا شجریان",
            programsCount: "۱۲۰ برنامه ثبت شده",
            imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuAWAuv8u0v1OIdyaGEUsfEcihpkz_8B_2gjKaFA66dCpN-Bqm_q4O4aJtGXvZ_0sInBsWn3IeZK0MlQA3l3x1RaiBvHutcVq9wu7H3UxxIJfFUpoqyl_Srw8haM4QQvjRRSHmLVYdboxoZeraXwY6SZucALPrPCdVuwEU5aNdf4mXG7Qwymhth2R1d4g90KlYTTsV3t0IUtD6Rlzg-C8o6AR9RmrXkr11rSrhha2h2uWhGvc6hILiGtcl7JdaCpPvx0APRgcdUscMPr"
        ),
        .init(
            name: "بانو مرضیه",
            programsCount: "۸۵ برنامه ثبت شده",
            imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuACrGUSeZphvMCbjU0pye8xCe_PoOpzcMulvXk3mHMDKbA3OPCh63S7Aan_HtY6ayCTALorUGT3Z-wU9diP_81YzbfI1u98A80KxnG54vIRcXU0-ysHEXJ-kJJ_S3AxSG27fRfR2cN1vP1VWZIoQI1Kz-zyVjHl9E5crKA0OupEphOwxh_kiUFRyfyrv3py1STGV3Mp_O3JA6O4Web1Pu3U0BO1irzC7NuVwUaJuUR3zz1ipmZ_JsGbMO6Uit47YeaVb-fDS8QMTLcY"
        ),
        .init(
            name: "غلامحسین بنان",
            programsCount: "۹۴ برنامه ثبت شده",
            imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuA7uzrhoj_jfgLEXEqe_zHCIBmD2nK7-Hu4ebRr9XEuIgj0FjXvjDsaxw6XEr9_AbHIU6FnIUgl8L8Yvn5F1pOJlK860RIOHTB4iq1OGwvbiPwPJM8CF__oLq3j0iJmWfzrpAKzwTBh1HEkuW_kjpE8UgxZpJYilRpvDh2v4DXxD_pBqYiV1REoplA8Xs1HhUHLvtO9GedBX4znf3_Kojkqzl_SJ7c_KEC_HGH2o946wPNz5Cy89q-WpPIwbOZ9Ue7Ex_34FrGLs1Xc"
        ),
        .init(
            name: "بانو دلکش",
            programsCount: "۷۲ برنامه ثبت شده",
            imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuAyMXtTSRs5UqZMcg51Kxydy5fF2sQHZAFszIOcUBUqZPdvqJjiwNY2kNPAluK1GBXCFc9CTB2XY06W9DCxrlvwodZOw44gPK-RwHjOoeD3Uoj7H8ooUMUsiab4sGaPqMDJDmJM8Il6IJ7HOo17XtQVcVAdfXKAc3G8bagN2UZxoNN2D7MYL1dj82NYQfp8Qt7QsU7wvTupq9FxwT08lhYnGsctnfQ8vr39s6rap1GJURZDRqzwRDbKuofgcOaeojycQpuwDd38JCCz"
        ),
        .init(
            name: "جلیل شهناز",
            programsCount: "۱۱۵ برنامه ثبت شده",
            imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuA2hVZWR2Z0j1a7SSEmfnLEm3SrIidS3j2EnFElicEuVkB32-M3iGqpEBw-59fsUzo6mazQq9Cw5_uhIc6q0Rcdp6R3-3JH6LDovVJpYKoD2OZgVp--I5kblaeCvWspbFKH_5Z31YKu4Jcn8VxJjtibB9JWPWf8dBh-hS--B2f_2L6VGqfVeAQhyaOYc4w5Ybd-9Q_-p0ZRKje4L0BeAxIdyCesXx1A73fXKJdowlExvTeMsSaXPGNKXqiRfR9qy7W8P0EFR629xeny"
        ),
        .init(
            name: "پرویز یاحقی",
            programsCount: "۱۰۸ برنامه ثبت شده",
            imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuBySGVnBqhZRyJ-vngmyf-Hd5PQndePKpM0WCl__KpAYTrn0dnSIk9Zjgs-KnsmYUH_ysqpJT1nsIvWCyWYvBv-h1wnzw8n8uLbpN4EzN-J_1IZrcc4ug0Vg-m6fLZnssUMUGYpoB2ZUD7gokhNIF0CyOK4QWM8FX_n-W84HUim4eL-mi0deyVurRese2PR0YMglqNqAZgAaSOubIteZa9EM-CCZeS5IeGJqAURy2yjBe1y-oxNfjXddDA2AHAxlHuzkGgl69d2ANtM"
        ),
        .init(
            name: "حسن کسایی",
            programsCount: "۹۶ برنامه ثبت شده",
            imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuBySGVnBqhZRyJ-vngmyf-Hd5PQndePKpM0WCl__KpAYTrn0dnSIk9Zjgs-KnsmYUH_ysqpJT1nsIvWCyWYvBv-h1wnzw8n8uLbpN4EzN-J_1IZrcc4ug0Vg-m6fLZnssUMUGYpoB2ZUD7gokhNIF0CyOK4QWM8FX_n-W84HUim4eL-mi0deyVurRese2PR0YMglqNqAZgAaSOubIteZa9EM-CCZeS5IeGJqAURy2yjBe1y-oxNfjXddDA2AHAxlHuzkGgl69d2ANtM"
        ),
        .init(
            name: "فرهنگ شریف",
            programsCount: "۸۷ برنامه ثبت شده",
            imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuBGekEUXBcApNWh17_qhvE5zV19t7t5ysca3oZ9FEtdwVsgnWNzPE4paUYPvmlUTxB_uvpchE7NsLDop42Z2XcKwL3KuNH7xPuKmzxrW9WppCT6K3Ym293tsAh_vm9OS8Hzx6kAiAndqgLmOu222Rv2-SDcDfZmurPQhtjei3LmDPjmABFjBKvYHUWqvHv4YSbsYVrFKSdnEa6IlEZO0z3ZeNFnHgtk_e9PVNz6AVOaBCZJTcau2o2nG16iErn20SyVh-RV53NW5hsg"
        )
    ]
}
