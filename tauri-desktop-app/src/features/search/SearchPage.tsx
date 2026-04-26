type FilterInput = {
  id: string;
  label: string;
  name: string;
  placeholder: string;
  icon?: string;
};

const filters: FilterInput[] = [
  { id: "cat", name: "filter_cat", label: "دسته برنامه", placeholder: "جستجو و انتخاب...", icon: "keyboard_arrow_down" },
  { id: "singer", name: "filter_singer", label: "خواننده", placeholder: "نام هنرمند...", icon: "person_search" },
  { id: "mode", name: "filter_mode", label: "دستگاه", placeholder: "شور، ماهور، همایون...", icon: "library_music" },
  { id: "inst", name: "filter_inst", label: "ساز", placeholder: "تار، نی، پیانو..." },
  { id: "perf", name: "filter_perf", label: "نوازنده", placeholder: "نام نوازنده..." },
  { id: "poet", name: "filter_poet", label: "شاعر", placeholder: "حافظ، سعدی، مولانا..." },
  { id: "speak", name: "filter_speak", label: "گوینده", placeholder: "نام گوینده..." },
  { id: "comp", name: "filter_comp", label: "آهنگساز", placeholder: "نام آهنگساز..." },
  { id: "arr", name: "filter_arr", label: "تنظیم کننده", placeholder: "نام تنظیم‌کننده..." },
];

function SegmentedControl({ id, name }: { id: string; name: string }) {
  return (
    <div className="segmented-control">
      <input defaultChecked id={`${id}_all`} name={name} type="radio" />
      <label htmlFor={`${id}_all`}>همه</label>
      <input id={`${id}_select`} name={name} type="radio" />
      <label htmlFor={`${id}_select`}>انتخاب کنید</label>
      <div className="selection-slider" />
    </div>
  );
}

function FilterField({ filter }: { filter: FilterInput }) {
  return (
    <div className="flex flex-col gap-2">
      <div className="flex items-center justify-between px-1">
        <label className="text-sm font-bold text-primary">{filter.label}</label>
        <SegmentedControl id={filter.id} name={filter.name} />
      </div>
      <div className="relative">
        <input
          className="w-full rounded-lg border border-outline-variant bg-white/50 px-4 py-2.5 text-right text-sm transition-all outline-none focus:border-secondary focus:ring-1 focus:ring-secondary/20"
          placeholder={filter.placeholder}
          type="text"
        />
        {filter.icon ? (
          <span className="material-symbols-outlined absolute left-3 top-2.5 text-xl text-on-surface-variant/50">{filter.icon}</span>
        ) : null}
      </div>
    </div>
  );
}

export function SearchPage() {
  return (
    <div className="mx-auto max-w-5xl px-12 pb-32 pt-12">
      <div className="mb-10 text-right">
        <h1 className="mb-3 text-4xl font-bold text-primary">جستجوی پیشرفته در گنجینه</h1>
        <p className="max-w-2xl text-right leading-relaxed text-on-surface-variant">با استفاده از فیلترهای زیر، در میراث موسیقی اصیل ایران کاوش کنید</p>
      </div>

      <form className="space-y-8 rounded-xl border border-secondary/10 bg-surface-container/30 p-8 backdrop-blur-sm">
        <div className="grid grid-cols-1 gap-x-8 gap-y-10 md:grid-cols-2">
          {filters.map((filter) => (
            <FilterField key={filter.id} filter={filter} />
          ))}

          <div className="flex flex-col gap-2">
            <label className="px-1 text-sm font-bold text-primary">ارکستر</label>
            <select
              className="no-scrollbar h-24 w-full rounded-lg border border-outline-variant bg-white/50 px-4 py-2 text-sm transition-all outline-none focus:border-secondary focus:ring-1 focus:ring-secondary/20"
              multiple
            >
              <option>ارکستر گلها</option>
              <option>ارکستر رادیو ایران</option>
              <option>ارکستر مجلسی</option>
              <option>ارکستر بزرگ رادیو</option>
            </select>
          </div>

          <div className="flex flex-col gap-2">
            <label className="px-1 text-sm font-bold text-primary">رهبر ارکستر</label>
            <select
              className="no-scrollbar h-24 w-full rounded-lg border border-outline-variant bg-white/50 px-4 py-2 text-sm transition-all outline-none focus:border-secondary focus:ring-1 focus:ring-secondary/20"
              multiple
            >
              <option>روح‌الله خالقی</option>
              <option>مرتضی حنانه</option>
              <option>جواد معروفی</option>
              <option>همایون خرم</option>
            </select>
          </div>

          <div className="flex flex-col gap-2 md:col-span-1 lg:col-span-1">
            <label className="px-1 text-sm font-bold text-primary">متن برنامه</label>
            <textarea
              className="h-24 w-full resize-none rounded-lg border border-outline-variant bg-white/50 px-4 py-2.5 text-right text-sm transition-all outline-none focus:border-secondary focus:ring-1 focus:ring-secondary/20"
              placeholder="جستجوی کلمات در اشعار و توضیحات..."
            />
          </div>
        </div>

        <div className="flex justify-center border-t border-on-surface/5 pt-6">
          <button
            className="flex items-center gap-2 rounded-full bg-primary px-10 py-3 font-bold text-white shadow-md transition-all hover:bg-surface-tint active:scale-95"
            type="submit"
          >
            <span className="material-symbols-outlined text-xl">search</span>
            <span>جستجوی هوشمند در آرشیو</span>
          </button>
        </div>
      </form>
    </div>
  );
}
