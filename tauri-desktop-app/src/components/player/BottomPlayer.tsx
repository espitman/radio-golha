export function BottomPlayer() {
  return (
    <footer className="glass-player fixed bottom-0 left-0 z-50 flex h-24 w-full items-center bg-primary px-12 shadow-[0_-4px_40px_rgba(28,28,23,0.08)]">
      <div className="absolute left-0 right-0 top-0 h-1 overflow-hidden bg-white/10">
        <div className="h-full w-1/3 bg-secondary" />
      </div>
      <div className="flex h-full w-full items-center justify-between">
        <div className="flex w-1/3 items-center gap-4">
          <div className="h-12 w-12 overflow-hidden rounded bg-white/10">
            <img
              alt="تصویر آلبوم در حال پخش"
              className="h-full w-full object-cover"
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuDZhEZKGbL6fIgOSJT9icS7x4wQKVtXxJMnVbUT264Kzn-7AaApCd7gLvEz5HKfz-6dQ5U9bm0b-V_kkP-2vFyvuzAbY8T7bYFl2hFdyMGWRkOf_DyeTW71i7O3abvi8UTaWAyFru_WpGWc_IyVok1sBYbdUzuTFRd3yn6T-w5lf1V7oompAJCM7bJL3T_rMJDNGl062lhaDesinbCbY_THLNtd_cDB1Z0q9LuBxvz5LSdbXWUc3A6rcVmvpUj6b8ZVUEcD9hN5pLmU"
            />
          </div>
          <div className="text-right">
            <p className="text-[10px] font-bold uppercase tracking-widest text-secondary">در حال پخش</p>
            <h5 className="serif text-sm font-bold text-white">آستان جانان - آواز شور</h5>
            <p className="text-[10px] text-white/60">محمدرضا شجریان</p>
          </div>
        </div>

        <div className="flex w-1/3 items-center justify-center gap-6">
          <span className="material-symbols-outlined cursor-pointer text-white/60 hover:text-white">shuffle</span>
          <span className="material-symbols-outlined cursor-pointer text-3xl text-white transition-transform hover:scale-110">skip_next</span>
          <div className="flex h-12 w-12 cursor-pointer items-center justify-center rounded-full bg-white transition-all hover:scale-105 active:scale-95">
            <span className="material-symbols-outlined text-3xl text-primary" style={{ fontVariationSettings: "'FILL' 1" }}>play_arrow</span>
          </div>
          <span className="material-symbols-outlined cursor-pointer text-3xl text-white transition-transform hover:scale-110">skip_previous</span>
          <span className="material-symbols-outlined cursor-pointer text-white/60 hover:text-white">repeat</span>
        </div>

        <div className="flex w-1/3 items-center justify-end gap-4">
          <div className="flex items-center gap-2 border-l border-white/10 pl-6">
            <span className="material-symbols-outlined cursor-pointer text-lg text-white/60 hover:text-white">volume_up</span>
            <span className="material-symbols-outlined cursor-pointer text-lg text-white/60 hover:text-white">playlist_play</span>
          </div>
          <div className="flex h-8 items-center justify-center gap-1 opacity-40">
            <div className="h-3 w-1 rounded-full bg-secondary" />
            <div className="h-6 w-1 rounded-full bg-secondary" />
            <div className="h-4 w-1 rounded-full bg-secondary" />
            <div className="h-8 w-1 rounded-full bg-secondary" />
            <div className="h-5 w-1 rounded-full bg-secondary" />
            <div className="h-2 w-1 rounded-full bg-secondary" />
          </div>
        </div>
      </div>
    </footer>
  );
}
