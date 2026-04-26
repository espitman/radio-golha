import { TrackList } from "../../components/track/TrackRow";

type ProgramTrack = {
  title: string;
  subtitle: string;
  duration: string;
};

const heroImage =
  "https://lh3.googleusercontent.com/aida-public/AB6AXuD-pM_C2R5U8iY_z-B1L5m9t8p7jK1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z";

const tracks: ProgramTrack[] = [
  { title: "پیش‌درآمد بیات ترک", subtitle: "جلیل شهناز • تار", duration: "۰۸:۱۵" },
  { title: "آواز بیات ترک - غزل سعدی", subtitle: "محمدرضا شجریان • آواز", duration: "۱۲:۴۰" },
  { title: "تکنوازی نی", subtitle: "محمد موسوی • نی", duration: "۰۵:۱۰" },
  { title: "تصنیف قدیمی - ای امان", subtitle: "محمدرضا شجریان • تصنیف", duration: "۰۶:۲۰" },
  { title: "رنگ بیات ترک", subtitle: "فرامرز پایور • سنتور", duration: "۰۲:۱۵" },
  { title: "اعلام برنامه و اختتامیه", subtitle: "آذر پژوهش • گوینده", duration: "۰۰:۵۵" },
];


export function ProgramDetailsPage() {
  return (
    <main className="program-detail-content pb-32 pt-12">
      <div className="mx-auto max-w-5xl px-12">
        <section className="hero-pattern relative flex h-[360px] w-full items-end justify-center overflow-hidden rounded-3xl bg-primary">
          <div className="absolute inset-0">
            <img alt="دورهمی موسیقی ایرانی" className="h-full w-full object-cover opacity-30 mix-blend-overlay" src={heroImage} />
          </div>
          <div className="relative z-10 w-full px-12 pb-16 text-center">
            <div className="mb-6 inline-block rounded-full bg-secondary/80 px-5 py-1.5 text-[10px] font-black uppercase tracking-widest text-white backdrop-blur-md">
              مجموعه گلهای رنگارنگ
            </div>
            <h1 className="mb-6 text-5xl font-bold leading-tight tracking-tight text-white">گلهای رنگارنگ شماره ۵۸۰</h1>
            <div className="flex items-center justify-center gap-2 text-white/70">
              <span className="material-symbols-outlined text-lg text-secondary">music_note</span>
              <span className="text-base font-bold">۶ قطعه موسیقی</span>
            </div>
          </div>
        </section>
      </div>

      <section className="mx-auto max-w-5xl px-12 py-12">
        <div className="mb-8 flex items-center justify-between border-b border-surface-variant pb-4">
          <h2 className="text-2xl font-black text-primary">فهرست قطعات</h2>
        </div>

<TrackList tracks={tracks} linkMode="title" />
      </section>
    </main>
  );
}
