import { PageHeader } from "../../components/layout/PageHeader";

export function SettingsPage() {
  return (
    <section className="mx-auto max-w-5xl px-12 pb-32 pt-8 text-right">
      <PageHeader title="تنظیمات" />
      <div className="mt-8 rounded-[28px] bg-white/55 p-7 shadow-sm">
        <h2 className="text-xl font-black">به‌روزرسانی دیتابیس</h2>
        <p className="mt-3 text-sm font-bold leading-7 text-golha-muted">زیرساخت Rust command برای اضافه شدن دانلود دیتابیس آماده است.</p>
        <button className="mt-6 rounded-2xl bg-golha-navy px-7 py-4 text-sm font-black text-white transition hover:bg-golha-gold hover:text-golha-ink">
          بررسی به‌روزرسانی
        </button>
      </div>
    </section>
  );
}
