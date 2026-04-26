import clsx from "clsx";

function SkeletonBlock({ className = "" }: { className?: string }) {
  return <div className={clsx("skeleton-shimmer rounded-xl bg-surface-container-high", className)} />;
}

export function PageHeaderSkeleton() {
  return (
    <header className="mb-[34px] text-right">
      <SkeletonBlock className="mb-3 h-9 w-48" />
      <SkeletonBlock className="h-4 w-80 max-w-full" />
    </header>
  );
}

export function ArtistGridSkeleton({ count = 8, columns = "lg:grid-cols-4" }: { count?: number; columns?: string }) {
  return (
    <div className={clsx("grid grid-cols-1 gap-8 sm:grid-cols-2", columns)}>
      {Array.from({ length: count }).map((_, index) => (
        <div key={index} className="overflow-hidden rounded-xl border border-outline-variant/10 bg-surface-container-lowest">
          <SkeletonBlock className="aspect-square rounded-none" />
          <div className="p-5 text-right">
            <SkeletonBlock className="mb-3 h-6 w-3/4" />
            <SkeletonBlock className="h-4 w-1/2" />
          </div>
        </div>
      ))}
    </div>
  );
}

export function PillsSkeleton({ count = 12 }: { count?: number }) {
  return (
    <div className="no-scrollbar mb-12 overflow-x-auto">
      <div className="flex min-w-max justify-start gap-2 pb-4">
        {Array.from({ length: count }).map((_, index) => (
          <SkeletonBlock key={index} className="h-9 w-14 rounded-full" />
        ))}
      </div>
    </div>
  );
}

export function TrackListSkeleton({ count = 6 }: { count?: number }) {
  return (
    <div className="overflow-hidden rounded-2xl border border-outline-variant/20 bg-surface-container-low shadow-sm">
      <div className="divide-y divide-on-surface/5">
        {Array.from({ length: count }).map((_, index) => (
          <div key={index} className="flex h-[72px] items-center gap-4 px-4">
            <SkeletonBlock className="h-10 w-10 shrink-0 rounded-full" />
            <div className="min-w-0 flex-1 text-right">
              <SkeletonBlock className="mb-2 h-4 w-52 max-w-full" />
              <SkeletonBlock className="h-3 w-32 max-w-full" />
            </div>
            <SkeletonBlock className="h-3 w-12" />
          </div>
        ))}
      </div>
    </div>
  );
}

export function BannerTracksPageSkeleton({ titleWidth = "w-72" }: { titleWidth?: string }) {
  return (
    <main className="pb-[144px] pt-12">
      <div className="mx-auto max-w-5xl px-12">
        <section className="hero-pattern relative flex h-[260px] w-full items-end overflow-hidden rounded-3xl bg-primary">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(210,167,56,0.18),transparent_35%),linear-gradient(135deg,rgba(9,47,83,0.95),rgba(6,32,58,0.98))]" />
          <div className="relative z-10 w-full px-12 pb-12 text-right">
            <SkeletonBlock className="mb-5 h-6 w-32 rounded-full bg-white/20" />
            <SkeletonBlock className={clsx("mb-5 h-12 bg-white/20", titleWidth)} />
            <SkeletonBlock className="h-5 w-36 bg-white/20" />
          </div>
        </section>
      </div>
      <section className="mx-auto max-w-5xl px-12 py-12">
        <div className="mb-8 flex items-center justify-between border-b border-surface-variant pb-4">
          <SkeletonBlock className="h-8 w-40" />
        </div>
        <TrackListSkeleton count={7} />
      </section>
    </main>
  );
}

export function HomeSkeleton() {
  return (
    <div className="pb-[96px]">
      <section className="mx-auto max-w-5xl px-12 py-8">
        <div className="hero-pattern relative h-[320px] overflow-hidden rounded-3xl bg-primary shadow-2xl shadow-primary/10">
          <div className="absolute inset-0 bg-[linear-gradient(270deg,rgba(0,32,69,0.92),rgba(0,32,69,0.38),transparent)]" />
          <div className="absolute right-16 top-16 w-[520px] max-w-[70%]">
            <SkeletonBlock className="mb-5 h-7 w-24 rounded bg-white/20" />
            <SkeletonBlock className="mb-6 h-12 w-64 bg-white/20" />
            <SkeletonBlock className="mb-3 h-5 w-full bg-white/20" />
            <SkeletonBlock className="mb-8 h-5 w-4/5 bg-white/20" />
            <div className="flex gap-4">
              <SkeletonBlock className="h-12 w-40 rounded-full bg-white/20" />
              <SkeletonBlock className="h-12 w-40 rounded-full bg-white/10" />
            </div>
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-5xl px-12 py-12">
        <SectionTitleSkeleton />
        <div className="grid grid-cols-4 gap-6">
          {Array.from({ length: 4 }).map((_, index) => (
            <div key={index} className="flex items-center gap-4 rounded-2xl border border-outline-variant/20 bg-surface-container-low p-4">
              <SkeletonBlock className="h-16 w-16 rounded-xl" />
              <div className="flex-1">
                <SkeletonBlock className="mb-3 h-5 w-24" />
                <SkeletonBlock className="h-3 w-16" />
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="mx-auto max-w-5xl px-12 py-12">
        <SectionTitleSkeleton />
        <ArtistGridSkeleton count={4} />
      </section>

      <section className="mx-auto max-w-5xl px-12 py-8">
        <SkeletonBlock className="mb-6 h-8 w-44" />
        <div className="flex gap-4 overflow-hidden pb-4">
          {Array.from({ length: 7 }).map((_, index) => <SkeletonBlock key={index} className="h-12 w-28 shrink-0 rounded-full" />)}
        </div>
      </section>

      <section className="mx-auto grid max-w-5xl grid-cols-12 gap-12 px-12 py-12">
        <div className="col-span-6">
          <SectionTitleSkeleton />
          <TrackListSkeleton count={5} />
        </div>
        <div className="col-span-6">
          <SectionTitleSkeleton />
          <TrackListSkeleton count={5} />
        </div>
      </section>
    </div>
  );
}

function SectionTitleSkeleton() {
  return (
    <div className="mb-8 flex items-end justify-between">
      <SkeletonBlock className="h-8 w-44" />
      <SkeletonBlock className="h-5 w-20" />
    </div>
  );
}

export function ArtistsListPageSkeleton({ tabs = "letters", columns = "lg:grid-cols-4" }: { tabs?: "letters" | "instruments"; columns?: string }) {
  return (
    <div className="mx-auto max-w-5xl px-12 pb-[144px] pt-8">
      <PageHeaderSkeleton />
      <PillsSkeleton count={tabs === "letters" ? 16 : 10} />
      <ArtistGridSkeleton count={8} columns={columns} />
    </div>
  );
}

export function ArtistDetailsSkeleton() {
  return (
    <div className="mx-auto max-w-5xl px-12 pb-[144px] pt-12 text-right">
      <section className="grid grid-cols-12 items-end gap-10 pb-12">
        <SkeletonBlock className="col-span-4 aspect-square rounded-lg" />
        <div className="col-span-8 flex h-full flex-col justify-end pb-8">
          <div className="mb-10 max-w-2xl">
            <SkeletonBlock className="mb-4 h-11 w-64" />
            <SkeletonBlock className="mb-5 h-5 w-32" />
            <SkeletonBlock className="h-10 w-44 rounded-full" />
          </div>
          <div className="flex justify-start gap-8">
            {Array.from({ length: 5 }).map((_, index) => (
              <div key={index} className="border-r-2 border-secondary/20 pr-4 first:border-r-0 first:pr-0">
                <SkeletonBlock className="mb-2 h-8 w-12" />
                <SkeletonBlock className="h-3 w-16" />
              </div>
            ))}
          </div>
        </div>
      </section>
      <section className="py-12">
        <div className="mb-6 flex items-end justify-between">
          <SkeletonBlock className="h-8 w-32" />
          <SkeletonBlock className="h-4 w-20" />
        </div>
        <TrackListSkeleton count={7} />
      </section>
    </div>
  );
}

export function TrackDetailsSkeleton() {
  return (
    <main className="mx-auto max-w-5xl px-12 pb-[144px] pt-12">
      <section className="mb-20 flex flex-col items-stretch gap-12 md:flex-row-reverse">
        <div className="w-full md:w-1/3">
          <SkeletonBlock className="aspect-square rounded-xl shadow-2xl" />
        </div>
        <div className="flex w-full flex-col justify-between text-right md:w-2/3">
          <SkeletonBlock className="mb-3 h-12 w-72" />
          <div className="flex flex-grow flex-col justify-center py-8">
            <div className="flex w-full flex-row-reverse items-center justify-between gap-8 border-y border-surface-container-highest py-4">
              {Array.from({ length: 3 }).map((_, index) => (
                <div key={index} className="flex flex-1 flex-col items-center px-8">
                  <SkeletonBlock className="mb-2 h-4 w-20" />
                  <SkeletonBlock className="h-7 w-28" />
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>
      <section className="mb-16">
        <SkeletonBlock className="mb-8 h-8 w-52" />
        <ArtistGridSkeleton count={4} />
      </section>
      <section className="mb-16">
        <SkeletonBlock className="mb-8 h-9 w-32" />
        <div className="space-y-6">
          {Array.from({ length: 4 }).map((_, index) => <SkeletonBlock key={index} className="h-28 rounded-xl" />)}
        </div>
      </section>
    </main>
  );
}

export function SearchFormSkeleton() {
  return (
    <div className="mx-auto max-w-5xl select-none px-12 pb-[144px] pt-8">
      <PageHeaderSkeleton />
      <div className="rounded-[14px] border border-secondary/10 bg-surface-container/50 px-8 py-[30px] backdrop-blur-sm">
        <div className="grid grid-cols-1 gap-x-8 gap-y-7 md:grid-cols-2">
          {Array.from({ length: 11 }).map((_, index) => (
            <div key={index}>
              <div className="mb-3 flex items-center justify-between">
                <SkeletonBlock className="h-4 w-24" />
                <SkeletonBlock className="h-9 w-28 rounded-full" />
              </div>
              <SkeletonBlock className="h-[74px] rounded-xl" />
            </div>
          ))}
          <div>
            <SkeletonBlock className="mb-3 h-4 w-28" />
            <SkeletonBlock className="h-[74px] rounded-xl" />
          </div>
        </div>
        <div className="mt-10 border-t border-on-surface/5 pt-6">
          <SkeletonBlock className="mx-auto h-12 w-44 rounded-full" />
        </div>
      </div>
    </div>
  );
}

export function SearchResultsSkeleton() {
  return (
    <main className="mx-auto max-w-5xl px-12 pb-[144px] pt-8">
      <section className="relative flex h-[232px] items-end justify-center overflow-hidden rounded-3xl bg-primary px-8 pb-12 text-center shadow-2xl shadow-primary/10">
        <div className="absolute inset-0 bg-[linear-gradient(135deg,#001A2F,#002E56,#001A2F)]" />
        <div className="relative z-10 flex flex-col items-center">
          <SkeletonBlock className="mb-4 h-12 w-56 bg-white/20" />
          <SkeletonBlock className="h-4 w-28 bg-white/20" />
        </div>
        <div className="absolute bottom-4 right-6 left-6 flex gap-2">
          {Array.from({ length: 3 }).map((_, index) => <SkeletonBlock key={index} className="h-8 w-32 rounded-full bg-white/20" />)}
        </div>
      </section>
      <section className="mt-6">
        <div className="mb-5 flex items-center justify-between border-b border-surface-variant pb-4">
          <SkeletonBlock className="h-8 w-36" />
          <SkeletonBlock className="h-9 w-44 rounded-lg" />
        </div>
        <TrackListSkeleton count={7} />
      </section>
    </main>
  );
}
