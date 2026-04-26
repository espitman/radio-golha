import { ArtistCard } from "../../components/artist/ArtistCard";

type Artist = {
  name: string;
  role: string;
  image: string;
};

type TimelineItem = {
  time: string;
  title: string;
  mode: string;
  singer: string;
  musician: string;
  poet: string;
  active?: boolean;
};

const programPortrait =
  "https://lh3.googleusercontent.com/aida-public/AB6AXuCqvh6tKAIQkOdetOD0qllRUTl7anGrnovvZr3o8ZKjbUQYbqcZm6F--TGFWkzKDoSIpeGmNcIpcNzbRgVhbzIHifP_vtmVuQxP5nSbLDEgTK7tH-y-0taGb9slA_xUT0fMqtn8cCFiG25xUD_Dbcwv73c0658MISJKRB_4MDlYIpal9cm_1oLgU3LH6PmpvuhTccP7g3EtTYTQthsyo697xSyFCd461QiwE2sAWvCmYVikNSnBDM-8onlupEdVS0wiuz7pYPP7SzxV";

const artists: Artist[] = [
  {
    name: "محمدرضا شجریان",
    role: "خواننده",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuDpC78r95F6N4DSpF6JDIJMXAQi4tvMueq0yPUhHqYZCSk5lNKfjZKrrOTUkKx6QQoRf8Z9jbQ8m-T5UH-t28cFCaDRZRoOf316ES3fpsedyDiaWDPEIg3cVEDjhwn9FDDHXRU_wNg0OW30DClZNXLYplmzY6BDV0tTPZfCMIuQQqIM5gjATZTql9-ynnp99TYjdzQewqVNID-aqr1fQXjZWRt1ekm4dZrva7XNfGRrFqkpQTMwRqz_j2G7l8y3E7gTvrrMaIpTIZFk",
  },
  {
    name: "جلیل شهناز",
    role: "نوازنده تار",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuBtct8xA3NdiIBc_nmc6_UBePGmV7zRaw9QM4mYowwsdo7DKOb2NINDjmDHvCsPR-XCZerS1MvRbbq3TJZjycSQscDpf6Gi_xDZT5MS0d-4hAfaDt1bMa2zIas8lOtupgA6jqCBNl9NUt815ruFxp0Wt9e6JCOQammBn0YBJFEwc9kF_XLXpMXDqA8YvnWs1eDCMyOLU_EjVs04Py7Njy37HwHchozFVHiuI7FW8iCez4dVlgc-R6g76WVTlWf-76Tkvrqi1_CFm-s4",
  },
  {
    name: "حسن کسایی",
    role: "نوازنده نی",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuA9KbU2OFGcJ6-3JuDo4XeQjSbtk3W90LRKnBMZnArvUsnRxh1MttPGW8vRUNLdQ6YWGumzini0g1-YVpB_T4fz3bSCWftrp8AVlPXwVuMZ1Ok7Bd4CSTOETUV1jEorCRXZ16OhPAsJZZIMJaviJoIkq9mkON7oh7VFG6TMuwjP2UIGqJCG4wGVkUYgn0Q5iBhgmgvWGF4Hrf5rUj0DMgTZ6zgxg_jJj_hUz7BxrT3YsPhuRahvi56ODoGGz-NObPQKKondfcFFJ_R6",
  },
  {
    name: "ناصر فرهنگ‌فر",
    role: "نوازنده تنبک",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuCMq0QcCfVy10DCkq5b4PONI9Ll_ykEMaNSdsP9DZBL9KLD1bporrjxedbDc9_kl2H6o5c-KEw9mbQmJyuzlUqYPTfN7iFcB0OeHV2XnlylHT93K4bSLukqgsnJgYBZQ0lyZqSAvBmLrrzFsDRuKD4Hgq1x1LcHF1-bGnqSZt2eXRBR4zWQ-rPHoWf3mvgvB3vOUAPEel1bGVDgivKDgPWWXu7_nkuPeCglcXZr9HDXDa0Onv0z0VXv6AjgskIdxXJdkdNGL_Wu3Trj",
  },
];

const timeline: TimelineItem[] = [
  { time: "۰۰:۰۰", title: "بیات ترک", mode: "دستگاه", singer: "ارکستر گلها", musician: "مرتضی حنانه", poet: "—" },
  { time: "۰۵:۳۰", title: "بیات ترک (تکنوازی)", mode: "دستگاه", singer: "—", musician: "شهناز / کسایی", poet: "—", active: true },
  { time: "۱۵:۴۵", title: "بیات ترک (آواز)", mode: "دستگاه", singer: "محمدرضا شجریان", musician: "جلیل شهناز", poet: "حافظ" },
  { time: "۲۸:۲۰", title: "بیات ترک (تصنیف)", mode: "دستگاه", singer: "محمدرضا شجریان", musician: "ارکستر گلها", poet: "سعدی" },
];

function MetaBlock({ label, value, bordered = false }: { label: string; value: string; bordered?: boolean }) {
  return (
    <div className={bordered ? "flex flex-1 flex-col items-center border-x border-surface-container-highest px-8" : "flex flex-1 flex-col items-center"}>
      <span className="mb-1 text-sm font-black text-on-surface-variant/60">{label}</span>
      <span className="text-xl font-black text-primary">{value}</span>
    </div>
  );
}


function Equalizer() {
  return (
    <div className="flex h-6 items-end gap-1 pb-1">
      <span className="h-3 w-1 animate-pulse bg-secondary" />
      <span className="h-5 w-1 animate-pulse bg-secondary [animation-delay:0.2s]" />
      <span className="h-2 w-1 animate-pulse bg-secondary [animation-delay:0.4s]" />
    </div>
  );
}

function TimelineRow({ item }: { item: TimelineItem }) {
  return (
    <button
      className={
        item.active
          ? "relative flex w-full flex-col gap-4 rounded-xl border-r-8 border-secondary bg-white p-6 text-right shadow-md ring-1 ring-secondary/10 transition-all md:flex-row-reverse"
          : "group relative flex w-full cursor-pointer flex-col gap-4 rounded-xl border border-transparent bg-surface-container-low p-6 text-right shadow-sm transition-all hover:border-outline-variant/30 hover:bg-white md:flex-row-reverse"
      }
    >
      <div className="flex items-center justify-between gap-2 md:min-w-[100px] md:flex-col md:items-end md:justify-center">
        <span className="text-lg font-black text-secondary">{item.time}</span>
        {item.active ? <Equalizer /> : <span className="material-symbols-outlined text-3xl text-stone-300 group-hover:text-secondary">play_arrow</span>}
      </div>
      <div className="grid flex-grow grid-cols-1 items-center gap-4 text-right md:grid-cols-4">
        <div className="md:col-span-1">
          <h4 className="text-lg font-black text-primary">{item.title}</h4>
          <p className="text-[10px] font-black uppercase tracking-wide text-on-surface-variant/60">{item.mode}</p>
        </div>
        <div className="flex flex-col">
          <span className="text-sm font-bold text-on-surface">{item.singer}</span>
          <span className="text-[10px] text-on-surface-variant/60">خواننده</span>
        </div>
        <div className="flex flex-col">
          <span className="text-sm font-bold text-on-surface">{item.musician}</span>
          <span className="text-[10px] text-on-surface-variant/60">نوازنده</span>
        </div>
        <div className="flex flex-col">
          <span className="text-sm font-bold text-on-surface">{item.poet}</span>
          <span className="text-[10px] text-on-surface-variant/60">شاعر</span>
        </div>
      </div>
    </button>
  );
}

export function TrackDetailsPage() {
  return (
    <main className="mx-auto max-w-5xl px-12 pb-32 pt-12">
      <section className="mb-20 flex flex-col items-stretch gap-12 md:flex-row-reverse">
        <div className="w-full md:w-1/3">
          <div className="relative aspect-square overflow-hidden rounded-xl border-4 border-surface-container-highest shadow-2xl">
            <img alt="گلهای رنگارنگ ۵۸۰" className="h-full w-full object-cover" src={programPortrait} />
          </div>
        </div>

        <div className="flex w-full flex-col justify-between text-right md:w-2/3">
          <div>
            <h1 className="mb-3 text-4xl font-bold leading-tight text-primary">گلهای رنگارنگ ۵۸۰</h1>
          </div>

          <div className="flex flex-grow flex-col justify-center py-8">
            <div className="flex w-full flex-row-reverse items-center justify-between gap-8 border-y border-surface-container-highest py-4">
              <MetaBlock label="دستگاه / آواز" value="بیات ترک" />
              <MetaBlock label="زمان کل" value="۳۵:۱۰" bordered />
              <MetaBlock label="ارکستر" value="مرتضی حنانه" />
            </div>
          </div>

          <section className="rounded-xl border border-outline-variant/20 bg-surface-container-low p-6 shadow-sm" dir="ltr">
            <div className="flex items-center gap-6">
              <button className="flex h-12 w-12 items-center justify-center rounded-full bg-primary-container text-white shadow-md transition-all hover:scale-105 active:scale-95">
                <span className="material-symbols-outlined text-3xl" style={{ fontVariationSettings: "'FILL' 1" }}>play_arrow</span>
              </button>
              <div className="flex flex-grow flex-col justify-center gap-2">
                <div className="relative h-1.5 w-full cursor-pointer overflow-hidden rounded-full bg-surface-variant">
                  <div className="absolute left-0 top-0 h-full w-[15%] rounded-full bg-primary" />
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-bold text-primary/60">05:12</span>
                  <span className="text-[10px] font-bold text-primary/60">35:10</span>
                </div>
              </div>
            </div>
          </section>
        </div>
      </section>

      <section className="mb-16">
        <h2 className="mb-8 border-b border-surface-container-highest pb-4 text-right text-2xl font-black text-primary">هنرمندان این برنامه</h2>
        <div className="grid grid-cols-2 gap-10 md:grid-cols-4">
          {artists.map((artist) => (
            <ArtistCard key={artist.name} align="center" artist={{ name: artist.name, subtitle: artist.role, image: artist.image }} />
          ))}
        </div>
      </section>

      <section className="mb-16">
        <div className="mb-8 flex items-center justify-between border-b border-surface-container-highest">
          <div className="flex gap-8">
            <button className="border-b-4 border-secondary px-2 pb-4 text-xl font-black text-primary transition-all">تایملاین</button>
            <button className="px-2 pb-4 text-xl font-bold text-on-surface-variant/60 transition-all hover:text-primary">اشعار</button>
          </div>
        </div>
        <div className="space-y-6">
          {timeline.map((item) => (
            <TimelineRow key={item.time} item={item} />
          ))}
        </div>
      </section>
    </main>
  );
}
