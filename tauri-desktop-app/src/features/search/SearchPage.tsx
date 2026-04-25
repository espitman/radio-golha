export function SearchPage() {
  return (
    <section className="mx-auto max-w-5xl pt-3 text-right">
      <h1 className="text-4xl font-black tracking-[-0.05em]">جستجوی پیشرفته</h1>
      <p className="mt-4 text-sm font-bold text-golha-muted">فرم اصلی جستجو در فاز اتصال دیتابیس اضافه می‌شود.</p>
      <div className="mt-8 grid grid-cols-2 gap-5">
        {['دسته برنامه', 'خواننده', 'دستگاه', 'ساز'].map((label) => (
          <label key={label} className="block rounded-3xl bg-white/55 p-5 shadow-sm">
            <span className="block text-sm font-black">{label}</span>
            <input className="mt-3 h-12 w-full rounded-2xl border border-black/10 bg-white px-4 text-right outline-none focus:border-golha-gold" placeholder="انتخاب کنید..." />
          </label>
        ))}
      </div>
    </section>
  );
}
