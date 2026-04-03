import { Outlet, createRootRoute, Link, useLocation } from '@tanstack/react-router'
import { TanStackRouterDevtoolsPanel } from '@tanstack/react-router-devtools'
import { TanStackDevtools } from '@tanstack/react-devtools'
import { 
  LayoutDashboard, 
  Database, 
  Music2, 
  Users, 
  Settings, 
  Search,
  Bell,
  Menu,
  Music
} from 'lucide-react'
import { useState } from 'react'
import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

import '../styles.css'

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

const NAV_ITEMS = [
  { icon: LayoutDashboard, label: 'داشبورد', path: '/' },
  { icon: Database, label: 'مدیریت داده‌ها', path: '/database' },
  { icon: Music2, label: 'برنامه‌ها', path: '/programmes' },
  { icon: Users, label: 'هنرمندان', path: '/artists' },
  { icon: Settings, label: 'تنظیمات', path: '/settings' },
]

export const Route = createRootRoute({
  component: RootComponent,
})

function RootComponent() {
  const [isSidebarOpen, setSidebarOpen] = useState(true)
  const location = useLocation()

  return (
    <div className="flex h-screen bg-slate-950 font-vazir text-slate-100 overflow-hidden" dir="rtl">
      {/* Sidebar */}
      <aside 
        className={cn(
          "relative h-full bg-slate-900/50 backdrop-blur-xl border-l border-slate-800/50 transition-all duration-300 ease-in-out flex flex-col",
          isSidebarOpen ? "w-72" : "w-20"
        )}
      >
        {/* Sidebar Header */}
        <div className="h-20 flex items-center px-6 border-b border-white/5 gap-4">
          <div className="w-10 h-10 rounded-xl bg-teal-500/20 flex items-center justify-center border border-teal-500/30 shrink-0">
            <Music className="w-6 h-6 text-teal-400" />
          </div>
          <span className={cn(
            "font-black text-xl tracking-tight transition-opacity duration-200",
            !isSidebarOpen && "opacity-0 pointer-events-none"
          )}>
            رادیو گلها
          </span>
        </div>

        {/* Navigation */}
        <nav className="flex-1 py-6 px-4 space-y-2 overflow-y-auto">
          {NAV_ITEMS.map((item) => {
            const isActive = location.pathname === item.path
            return (
              <Link
                key={item.path}
                to={item.path as any}
                className={cn(
                  "flex items-center gap-4 px-4 py-3 rounded-xl transition-all group relative overflow-hidden",
                  isActive 
                    ? "bg-teal-500/10 text-teal-400 border border-teal-500/20" 
                    : "text-slate-400 hover:text-slate-100 hover:bg-white/5"
                )}
              >
                <item.icon className={cn(
                  "w-5 h-5 shrink-0 transition-transform group-hover:scale-110",
                  isActive ? "text-teal-400" : "text-slate-400"
                )} />
                <span className={cn(
                  "font-medium transition-all duration-200 whitespace-nowrap",
                  !isSidebarOpen && "opacity-0 translate-x-10 pointer-events-none"
                )}>
                  {item.label}
                </span>
                {isActive && (
                  <div className="absolute right-0 top-1/2 -translate-y-1/2 w-1 h-6 bg-teal-400 rounded-l-full" />
                )}
              </Link>
            )
          })}
        </nav>

        {/* Sidebar Footer */}
        <div className="p-4 border-t border-white/5">
          <button 
            onClick={() => setSidebarOpen(!isSidebarOpen)}
            className="flex items-center gap-4 w-full px-4 py-3 rounded-xl text-slate-400 hover:text-slate-100 hover:bg-white/5 transition-all group"
          >
            <Menu className={cn(
              "w-5 h-5 shrink-0 transition-transform",
              !isSidebarOpen && "rotate-180"
            )} />
            <span className={cn(
              "font-medium transition-all",
              !isSidebarOpen && "opacity-0 pointer-events-none"
            )}>
              بستن منو
            </span>
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col min-w-0">
        {/* Header */}
        <header className="h-20 flex items-center justify-between px-8 bg-slate-950/50 backdrop-blur-md border-b border-white/5 z-10 shrink-0">
          <div className="flex items-center gap-6 flex-1 max-w-xl">
            <div className="relative w-full group">
              <Search className="absolute right-4 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500 group-focus-within:text-teal-400 transition-colors" />
              <input 
                type="text" 
                placeholder="جستجو در آرشیو، هنرمندان، اشعار..."
                className="w-full bg-slate-900 border border-white/5 rounded-2xl py-2.5 pr-11 pl-4 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500/30 transition-all placeholder:text-slate-600 outline-none"
              />
            </div>
          </div>

          <div className="flex items-center gap-4 mr-6">
            <button className="w-10 h-10 rounded-full border border-white/5 flex items-center justify-center text-slate-400 hover:bg-white/5 hover:text-slate-100 transition-all relative">
              <Bell className="w-5 h-5" />
              <span className="absolute top-2.5 right-2.5 w-2 h-2 bg-teal-500 rounded-full border-2 border-slate-950"></span>
            </button>
            <div className="h-8 w-px bg-white/5 mx-2" />
            <div className="flex items-center gap-3">
              <div className="text-left flex flex-col items-end">
                <span className="text-sm font-bold block">ادمین پروژه</span>
                <span className="text-[10px] text-slate-500 bg-slate-900 px-2 rounded-full border border-white/5">سطح دسترسی کامل</span>
              </div>
              <div className="w-10 h-10 rounded-2xl bg-gradient-to-br from-teal-400 to-emerald-500 overflow-hidden border-2 border-white/10 shadow-lg shadow-teal-500/20">
                {/* User Avatar Placeholder */}
              </div>
            </div>
          </div>
        </header>

        {/* Page Content */}
        <div className="flex-1 overflow-y-auto p-8 custom-scrollbar">
           <Outlet />
        </div>
      </main>

      <TanStackDevtools
        config={{
          position: 'bottom-right',
        }}
        plugins={[
          {
            name: 'TanStack Router',
            render: <TanStackRouterDevtoolsPanel />,
          },
        ]}
      />
    </div>
  )
}
