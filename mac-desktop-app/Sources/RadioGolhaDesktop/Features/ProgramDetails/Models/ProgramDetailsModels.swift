import Foundation

struct ProgramDetailsItem: Identifiable {
    let id: String
    let title: String
    let modeTitle: String
    let totalDuration: String
    let orchestra: String
    let coverImageURL: String
    let artists: [ProgramArtistItem]
    let timeline: [ProgramTimelineItem]
    let lyrics: [ProgramLyricItem]

    init(
        title: String,
        modeTitle: String,
        totalDuration: String,
        orchestra: String,
        coverImageURL: String,
        artists: [ProgramArtistItem],
        timeline: [ProgramTimelineItem],
        lyrics: [ProgramLyricItem]
    ) {
        self.id = title
        self.title = title
        self.modeTitle = modeTitle
        self.totalDuration = totalDuration
        self.orchestra = orchestra
        self.coverImageURL = coverImageURL
        self.artists = artists
        self.timeline = timeline
        self.lyrics = lyrics
    }
}

struct ProgramArtistItem: Identifiable {
    let id = UUID()
    let name: String
    let role: String
    let imageURL: String
}

struct ProgramTimelineItem: Identifiable {
    let id = UUID()
    let time: String
    let segmentTitle: String
    let singer: String
    let musician: String
    let poet: String
}

struct ProgramLyricItem: Identifiable {
    let id = UUID()
    let text: String
}

enum ProgramDetailsFactory {
    static func fromTrackTitle(_ trackTitle: String) -> ProgramDetailsItem {
        let normalized = trackTitle.replacingOccurrences(of: "\n", with: " ")

        if normalized.contains("گلهای تازه") {
            return baseProgram(title: "گلهای تازه ۱۲۰")
        }
        if normalized.contains("تکنوازان") {
            return baseProgram(title: "تکنوازان ۲۵")
        }
        if normalized.contains("کاروان") {
            return baseProgram(title: "گلهای رنگارنگ ۴۲۲")
        }

        return baseProgram(title: "گلهای رنگارنگ ۵۸۰")
    }

    private static func baseProgram(title: String) -> ProgramDetailsItem {
        ProgramDetailsItem(
            title: title,
            modeTitle: "بیات ترک",
            totalDuration: "۳۵:۱۰",
            orchestra: "مرتضی حنانه",
            coverImageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuCqvh6tKAIQkOdetOD0qllRUTl7anGrnovvZr3o8ZKjbUQYbqcZm6F--TGFWkzKDoSIpeGmNcIpcNzbRgVhbzIHifP_vtmVuQxP5nSbLDEgTK7tH-y-0taGb9slA_xUT0fMqtn8cCFiG25xUD_Dbcwv73c0658MISJKRB_4MDlYIpal9cm_1oLgU3LH6PmpvuhTccP7g3EtTYTQthsyo697xSyFCd461QiwE2sAWvCmYVikNSnBDM-8onlupEdVS0wiuz7pYPP7SzxV",
            artists: [
                .init(name: "محمدرضا شجریان", role: "خواننده", imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuDpC78r95F6N4DSpF6JDIJMXAQi4tvMueq0yPUhHqYZCSk5lNKfjZKrrOTUkKx6QQoRf8Z9jbQ8m-T5UH-t28cFCaDRZRoOf316ES3fpsedyDiaWDPEIg3cVEDjhwn9FDDHXRU_wNg0OW30DClZNXLYplmzY6BDV0tTPZfCMIuQQqIM5gjATZTql9-ynnp99TYjdzQewqVNID-aqr1fQXjZWRt1ekm4dZrva7XNfGRrFqkpQTMwRqz_j2G7l8y3E7gTvrrMaIpTIZFk"),
                .init(name: "جلیل شهناز", role: "نوازنده تار", imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuBtct8xA3NdiIBc_nmc6_UBePGmV7zRaw9QM4mYowwsdo7DKOb2NINDjmDHvCsPR-XCZerS1MvRbbq3TJZjycSQscDpf6Gi_xDZT5MS0d-4hAfaDt1bMa2zIas8lOtupgA6jqCBNl9NUt815ruFxp0Wt9e6JCOQammBn0YBJFEwc9kF_XLXpMXDqA8YvnWs1eDCMyOLU_EjVs04Py7Njy37HwHchozFVHiuI7FW8iCez4dVlgc-R6g76WVTlWf-76Tkvrqi1_CFm-s4"),
                .init(name: "حسن کسایی", role: "نوازنده نی", imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuA9KbU2OFGcJ6-3JuDo4XeQjSbtk3W90LRKnBMZnArvUsnRxh1MttPGW8vRUNLdQ6YWGumzini0g1-YVpB_T4fz3bSCWftrp8AVlPXwVuMZ1Ok7Bd4CSTOETUV1jEorCRXZ16OhPAsJZZIMJaviJoIkq9mkON7oh7VFG6TMuwjP2UIGqJCG4wGVkUYgn0Q5iBhgmgvWGF4Hrf5rUj0DMgTZ6zgxg_jJj_hUz7BxrT3YsPhuRahvi56ODoGGz-NObPQKKondfcFFJ_R6"),
                .init(name: "ناصر فرهنگ‌فر", role: "نوازنده تنبک", imageURL: "https://lh3.googleusercontent.com/aida-public/AB6AXuCMq0QcCfVy10DCkq5b4PONI9Ll_ykEMaNSdsP9DZBL9KLD1bporrjxedbDc9_kl2H6o5c-KEw9mbQmJyuzlUqYPTfN7iFcB0OeHV2XnlylHT93K4bSLukqgsnJgYBZQ0lyZqSAvBmLrrzFsDRuKD4Hgq1x1LcHF1-bGnqSZt2eXRBR4zWQ-rPHoWf3mvgvB3vOUAPEel1bGVDgivKDgPWWXu7_nkuPeCglcXZr9HDXDa0Onv0z0VXv6AjgskIdxXJdkdNGL_Wu3Trj")
            ],
            timeline: [
                .init(time: "۰۰:۰۰", segmentTitle: "بیات ترک", singer: "ارکستر گلها", musician: "مرتضی حنانه", poet: "—"),
                .init(time: "۰۵:۳۰", segmentTitle: "بیات ترک (تکنوازی)", singer: "—", musician: "شهناز / کسایی", poet: "—"),
                .init(time: "۱۵:۴۵", segmentTitle: "بیات ترک (آواز)", singer: "محمدرضا شجریان", musician: "جلیل شهناز", poet: "حافظ"),
                .init(time: "۲۸:۲۰", segmentTitle: "بیات ترک (تصنیف)", singer: "محمدرضا شجریان", musician: "ارکستر گلها", poet: "رهی معیری")
            ],
            lyrics: [
                .init(
                    text: "الا یا ایها الساقی ادر کاسا و ناولها\nکه عشق آسان نمود اول ولی افتاد مشکل‌ها"
                ),
                .init(
                    text: "شب تاریک و بیم موج و گردابی چنین هایل\nکجا دانند حال ما سبکباران ساحل‌ها"
                ),
                .init(
                    text: "از غم عشق تو ای دلبر شیرین سخنم\nدل چو آیینه روشن شد و شد مست غمم"
                )
            ]
        )
    }
}
