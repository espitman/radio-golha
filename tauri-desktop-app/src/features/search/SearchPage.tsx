import { useEffect, useMemo, useState } from "react";
import { PageHeader } from "../../components/layout/PageHeader";

type MatchMode = "all" | "any";

type SearchOption = {
  id: string;
  name: string;
  avatar?: string;
};

type FilterInput = {
  id: string;
  label: string;
  name: string;
  placeholder: string;
  supportsAvatar?: boolean;
  options: SearchOption[];
};

const peopleOptions: SearchOption[] = [
  { id: "banaan", name: "غلامحسین بنان" },
  { id: "shajarian", name: "محمدرضا شجریان" },
  { id: "qavami", name: "حسین قوامی" },
  { id: "maroufi", name: "جواد معروفی" },
  { id: "khaleghi", name: "روح‌الله خالقی" },
];

const filters: FilterInput[] = [
  {
    id: "category",
    name: "filter_category",
    label: "دسته برنامه",
    placeholder: "گل‌های تازه، گل‌های رنگارنگ...",
    options: ["گل‌های تازه", "گل‌های رنگارنگ", "یک شاخه گل", "برگ سبز", "گل‌های جاویدان"].map((name) => ({ id: name, name })),
  },
  { id: "singer", name: "filter_singer", label: "خواننده", placeholder: "نام خواننده...", supportsAvatar: true, options: peopleOptions },
  {
    id: "mode",
    name: "filter_mode",
    label: "دستگاه",
    placeholder: "شور، ماهور، همایون...",
    options: ["شور", "ماهور", "همایون", "سه‌گاه", "چهارگاه", "راست‌پنجگاه"].map((name) => ({ id: name, name })),
  },
  {
    id: "orchestra",
    name: "filter_orchestra",
    label: "ارکستر",
    placeholder: "نام ارکستر...",
    options: ["ارکستر گل‌ها", "ارکستر رادیو ایران", "ارکستر بزرگ رادیو", "ارکستر شماره یک"].map((name) => ({ id: name, name })),
  },
  {
    id: "instrument",
    name: "filter_instrument",
    label: "ساز",
    placeholder: "تار، نی، پیانو...",
    options: ["تار", "نی", "پیانو", "ویولن", "سنتور", "عود"].map((name) => ({ id: name, name })),
  },
  { id: "performer", name: "filter_performer", label: "نوازنده", placeholder: "نام نوازنده...", supportsAvatar: true, options: peopleOptions },
  { id: "poet", name: "filter_poet", label: "شاعر", placeholder: "حافظ، سعدی، مولانا...", supportsAvatar: true, options: peopleOptions },
  { id: "announcer", name: "filter_announcer", label: "گوینده", placeholder: "نام گوینده...", supportsAvatar: true, options: peopleOptions },
  { id: "composer", name: "filter_composer", label: "آهنگساز", placeholder: "نام آهنگساز...", supportsAvatar: true, options: peopleOptions },
  { id: "arranger", name: "filter_arranger", label: "تنظیم‌کننده", placeholder: "نام تنظیم‌کننده...", supportsAvatar: true, options: peopleOptions },
  { id: "orchestraLeader", name: "filter_orchestra_leader", label: "رهبر ارکستر", placeholder: "نام رهبر ارکستر...", supportsAvatar: true, options: peopleOptions },
];

function normalizeSearch(value: string) {
  return value.trim().replace(/ي/g, "ی").replace(/ك/g, "ک").toLocaleLowerCase("fa-IR");
}

function MatchModeToggle({ mode, onChange }: { mode: MatchMode; onChange: (mode: MatchMode) => void }) {
  return (
    <div className="relative grid h-7 w-[124px] grid-cols-2 rounded-full bg-surface-variant p-0.5 text-[9.75px] font-bold" dir="ltr">
      <span
        className={`absolute top-0.5 h-6 w-[60px] rounded-full bg-white shadow-sm transition-transform duration-200 ${
          mode === "any" ? "translate-x-[60px]" : "translate-x-0"
        }`}
      />
      <button className={`relative z-10 rounded-full transition-colors ${mode === "all" ? "text-on-surface" : "text-on-surface-variant"}`} onClick={() => onChange("all")} type="button">
        همه
      </button>
      <button className={`relative z-10 rounded-full transition-colors ${mode === "any" ? "text-on-surface" : "text-on-surface-variant"}`} onClick={() => onChange("any")} type="button">
        هرکدام
      </button>
    </div>
  );
}

function AvatarThumb() {
  return (
    <span className="grid size-[18px] shrink-0 place-items-center rounded-full bg-surface-container-low text-primary/70">
      <span className="material-symbols-outlined text-[12px]">person</span>
    </span>
  );
}

function SelectedBadge({ label, supportsAvatar, onRemove }: { label: string; supportsAvatar?: boolean; onRemove: () => void }) {
  return (
    <span className="flex shrink-0 items-center gap-1.5 rounded-full border border-primary/15 bg-primary/10 px-2 py-1 text-[9.5px] font-bold text-primary">
      {supportsAvatar ? <AvatarThumb /> : null}
      <span>{label}</span>
      <button className="grid size-4 place-items-center text-primary/75" onClick={onRemove} type="button" aria-label="حذف فیلتر">
        <span className="material-symbols-outlined text-[12px]">close</span>
      </button>
    </span>
  );
}

function SearchComboField({
  filter,
  isExpanded,
  onExpandedChange,
}: {
  filter: FilterInput;
  isExpanded: boolean;
  onExpandedChange: (expanded: boolean) => void;
}) {
  const [mode, setMode] = useState<MatchMode>(filter.id === "category" ? "all" : "any");
  const [query, setQuery] = useState("");
  const [selected, setSelected] = useState<SearchOption[]>([]);
  const suggestions = useMemo(() => {
    const selectedIds = new Set(selected.map((item) => item.id));
    const base = filter.options.filter((item) => !selectedIds.has(item.id));
    const normalized = normalizeSearch(query);
    if (!normalized) return base;
    return base.filter((item) => normalizeSearch(item.name).includes(normalized));
  }, [filter.options, query, selected]);

  const title = selected.length ? `${filter.label} (${selected.length})` : filter.label;

  return (
    <div data-search-combo className={`relative flex flex-col gap-2 text-right ${isExpanded ? "z-[1000]" : "z-0"} focus-within:z-[1000]`}>
      <div className="flex items-center justify-between px-1">
        <span className="text-[10.5px] font-bold text-primary">{title}</span>
        <MatchModeToggle mode={mode} onChange={setMode} />
      </div>

      <div className="relative">
        <div className="flex h-[38px] items-center gap-2 rounded-lg border border-outline-variant bg-white/55 px-3 transition focus-within:border-secondary focus-within:ring-1 focus-within:ring-secondary/20">
          <button
            className={`grid size-5 shrink-0 place-items-center text-on-surface-variant/60 transition-transform ${isExpanded ? "rotate-180" : ""}`}
            onClick={() => onExpandedChange(!isExpanded)}
            type="button"
            aria-label="باز کردن گزینه‌ها"
          >
            <span className="material-symbols-outlined text-[18px]">expand_more</span>
          </button>

          <div className="no-scrollbar flex min-w-0 flex-1 items-center gap-1.5 overflow-x-auto" dir="rtl">
            {selected.map((item) => (
              <SelectedBadge
                key={item.id}
                label={item.name}
                supportsAvatar={filter.supportsAvatar}
                onRemove={() => setSelected((items) => items.filter((selectedItem) => selectedItem.id !== item.id))}
              />
            ))}
            <input
              className="min-w-[110px] flex-1 select-text bg-transparent text-right text-[10.5px] text-on-surface outline-none placeholder:text-on-surface-variant/60"
              onChange={(event) => {
                setQuery(event.target.value);
                onExpandedChange(true);
              }}
              onFocus={() => onExpandedChange(true)}
              placeholder={selected.length ? "" : filter.placeholder}
              value={query}
              type="text"
            />
          </div>
        </div>

        {isExpanded ? (
          <div className="absolute right-0 top-11 z-[200] w-full overflow-hidden rounded-lg border border-outline-variant bg-white/95 shadow-xl shadow-primary/10">
            <div className="no-scrollbar max-h-[190px] overflow-y-auto">
              {suggestions.length ? (
                suggestions.slice(0, 8).map((option, index) => (
                  <button
                    key={option.id}
                    className="flex h-[38px] w-full items-center gap-2 border-b border-on-surface/5 px-2.5 text-right text-[10px] text-on-surface transition last:border-b-0 hover:bg-primary/10"
                    onClick={() => {
                      setSelected((items) => [...items, option]);
                      setQuery("");
                      onExpandedChange(true);
                    }}
                    type="button"
                  >
                    {filter.supportsAvatar ? <AvatarThumb /> : null}
                    <span className="min-w-0 flex-1 truncate">{option.name}</span>
                    {index === 0 ? <span className="rounded-full bg-tertiary-container/30 px-2 py-0.5 text-[8px] text-tertiary">پیشنهادی</span> : null}
                  </button>
                ))
              ) : (
                <div className="flex h-[38px] items-center px-2.5 text-[9.5px] text-on-surface-variant">موردی یافت نشد</div>
              )}
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
}

function TranscriptField() {
  return (
    <div className="flex flex-col gap-2 text-right md:col-span-2">
      <label className="px-1 text-[10.5px] font-bold text-primary">متن برنامه</label>
      <input
        className="h-[38px] w-full select-text rounded-lg border border-outline-variant bg-white/55 px-4 text-right text-[10.5px] text-on-surface outline-none placeholder:text-on-surface-variant/60 focus:border-secondary focus:ring-1 focus:ring-secondary/20"
        placeholder="جستجوی کلمات در اشعار و توضیحات..."
        type="text"
      />
    </div>
  );
}

export function SearchPage() {
  const [expandedFilterId, setExpandedFilterId] = useState<string | null>(null);

  useEffect(() => {
    function closeOnOutsideClick(event: PointerEvent) {
      const target = event.target as Element | null;
      if (!target?.closest("[data-search-combo]")) {
        setExpandedFilterId(null);
      }
    }

    document.addEventListener("pointerdown", closeOnOutsideClick);
    return () => document.removeEventListener("pointerdown", closeOnOutsideClick);
  }, []);

  return (
    <div className="mx-auto max-w-5xl select-none px-12 pb-32 pt-8">
      <PageHeader title="جستجوی پیشرفته در گنجینه" subtitle="با استفاده از فیلترهای زیر، در میراث موسیقی اصیل ایران کاوش کنید" />

      <form className="overflow-visible rounded-[14px] border border-secondary/10 bg-surface-container/50 px-8 py-[30px] backdrop-blur-sm">
        <div className="grid grid-cols-1 gap-x-8 gap-y-7 overflow-visible md:grid-cols-2">
          {filters.map((filter) => (
            <SearchComboField
              key={filter.id}
              filter={filter}
              isExpanded={expandedFilterId === filter.id}
              onExpandedChange={(expanded) => setExpandedFilterId(expanded ? filter.id : null)}
            />
          ))}
          <TranscriptField />
        </div>

        <div className="mt-10 border-t border-on-surface/5 pt-6">
          <div className="flex justify-center">
            <button
              className="flex items-center gap-2 rounded-full bg-primary px-[34px] py-3 text-[11px] font-bold text-white shadow-md shadow-primary/10 transition hover:bg-surface-tint active:scale-95"
              type="submit"
            >
              <span className="material-symbols-outlined text-[18px]">search</span>
              <span>جستجو کن</span>
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}
