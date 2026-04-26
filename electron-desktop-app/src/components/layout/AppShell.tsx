import { useEffect, useRef, useState } from "react";
import { Link, Outlet, useRouterState } from "@tanstack/react-router";
import clsx from "clsx";
import { HeaderQuickSearch } from "./HeaderQuickSearch";
import { BottomPlayer } from "../player/BottomPlayer";

const sideItems = [
  { label: "صفحه اصلی", to: "/", icon: "home" },
  { label: "خواننده‌ها", to: "/singers", icon: "mic_external_on" },
  { label: "نوازندگان", to: "/players", icon: "music_note" },
  { label: "شنیده‌شده‌های اخیر", to: "/recent", icon: "history" },
  { label: "جستجوی پیشرفته", to: "/search", icon: "search" },
] as const;

const topItems = [
  { label: "خواننده‌های مورد علاقه", to: "/favorite-singers" },
  { label: "نوازندگان مورد علاقه", to: "/favorite-players" },
  { label: "پلی لیست‌های من", to: "/playlists" },
  { label: "محبوب‌ترین برنامه‌ها", to: "/popular" },
] as const;

export function AppShell() {
  const pathname = useRouterState({ select: (state) => state.location.pathname });
  const contentRef = useRef<HTMLElement | null>(null);
  const isBrowserBackRef = useRef(false);
  const [historyStack, setHistoryStack] = useState<string[]>(() => [window.location.pathname]);

  useEffect(() => {
    contentRef.current?.scrollTo({ top: 0, left: 0, behavior: "instant" });
  }, [pathname]);

  useEffect(() => {
    setHistoryStack((current) => {
      if (current[current.length - 1] === pathname) return current;

      if (isBrowserBackRef.current) {
        isBrowserBackRef.current = false;
        const previousIndex = current.lastIndexOf(pathname);
        return previousIndex >= 0 ? current.slice(0, previousIndex + 1) : [pathname];
      }

      return [...current, pathname];
    });
  }, [pathname]);

  function handleBack() {
    if (historyStack.length <= 1) return;
    isBrowserBackRef.current = true;
    window.history.back();
  }

  return (
    <div className="h-screen overflow-hidden bg-surface text-on-surface selection:bg-secondary-container selection:text-on-secondary-container" dir="rtl">
      <aside className="fixed right-0 top-0 z-50 flex h-full w-64 flex-col bg-surface-container py-8">
        <div className="mb-12 px-8 text-right">
          <h1 className="text-2xl font-bold tracking-tight text-primary-container">رادیو گل‌ها</h1>
          <p className="mt-1 text-xs uppercase tracking-widest opacity-60">میراث موسیقی اصیل ایران</p>
        </div>

        <nav className="flex-1 space-y-2">
          {sideItems.map((item) => {
            const active = pathname === item.to;
            return (
              <Link
                key={item.to}
                to={item.to}
                className={clsx(
                  "flex items-center py-3 transition-colors",
                  active
                    ? "border-r-4 border-secondary bg-surface-container-low pr-7 font-bold text-secondary"
                    : "pr-8 text-on-surface hover:bg-surface font-medium",
                )}
              >
                <span className="material-symbols-outlined ml-4">{item.icon}</span>
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        <div className="mt-auto space-y-4 border-t border-on-surface/5 px-8 pt-8">
          <Link className="flex items-center text-sm text-on-surface/70 transition-colors hover:text-secondary" to="/settings">
            <span className="material-symbols-outlined ml-3 text-lg">settings</span>
            تنظیمات
          </Link>
          <Link className="flex items-center text-sm text-on-surface/70 transition-colors hover:text-secondary" to="/help">
            <span className="material-symbols-outlined ml-3 text-lg">help</span>
            راهنما
          </Link>
          <div className="flex items-center gap-3 pt-4">
            <div className="h-10 w-10 overflow-hidden rounded-full bg-surface-variant">
              <img
                alt="حساب متصدی"
                className="h-full w-full object-cover"
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuBQlX0v-Lh1Lj6CxDQYOd-xCKyNArhNLKOXT1e6WR3j251g0iUPqfUXkoPd4gjIMgt1QeRMHZDCbQeXgk7e6hsvtIN7AJzWyjXu8BRxJdWLB1WDSgoyYfG2_3oT6a9fFzoQtYdeZe430yfJn_MdqX-yfmEECMgIIn8tLeQLf8RO78T8vzpr-c1wSqYYgXEUGfN4KzDYQ7vH5P4PMUb9Hd7ufSqPmAeJmC59z2G7fYemqLFnCjTVpN0pHuR295sHnCYO1LAERIc-5CI3"
              />
            </div>
            <div>
              <p className="text-xs font-bold text-primary">حساب متصدی</p>
              <p className="text-[10px] opacity-50">دسترسی به آرشیو</p>
            </div>
          </div>
        </div>
      </aside>

      <main ref={contentRef} className="shamseh-pattern relative mr-64 h-screen overflow-y-auto">
        <header className="app-drag-region sticky top-0 z-40 flex h-24 w-full items-center justify-between bg-surface/80 px-12 pt-4 backdrop-blur-xl">
          <div className="app-no-drag-region flex items-center gap-10">
            <nav className="flex gap-6">
              {topItems.map((item) => {
                const active = pathname === item.to;
                return (
                  <Link
                    key={item.to}
                    to={item.to}
                    className={clsx(
                      "pb-1 text-[13px] font-medium transition-all",
                      active
                        ? "border-b-2 border-secondary font-bold text-primary-container"
                        : "text-on-surface/70 hover:text-secondary",
                    )}
                  >
                    {item.label}
                  </Link>
                );
              })}
            </nav>
          </div>

          <div className="app-no-drag-region flex items-center gap-6">
            <HeaderQuickSearch />
            <button
              className={clsx(
                "grid h-[30px] w-[30px] place-items-center rounded-full bg-surface-container-low transition-colors",
                historyStack.length > 1
                  ? "text-primary/70 hover:text-secondary"
                  : "cursor-not-allowed text-primary/25",
              )}
              aria-label="بازگشت"
              disabled={historyStack.length <= 1}
              onClick={handleBack}
              type="button"
            >
              <span className="material-symbols-outlined text-[18px]">chevron_left</span>
            </button>
          </div>
        </header>

        <div key={pathname} className="page-route-transition">
          <Outlet />
        </div>
      </main>

      <BottomPlayer />
    </div>
  );
}
