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
  Music
} from 'lucide-react'
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarProvider,
  SidebarTrigger,
  SidebarRail,
  SidebarInset,
} from "@/components/ui/sidebar"
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/components/ui/avatar"
import { Input } from "@/components/ui/input"
import { Separator } from "@/components/ui/separator"
import { TooltipProvider } from "@/components/ui/tooltip"

import '../styles.css'

const NAV_ITEMS = [
  { icon: LayoutDashboard, label: 'داشبورد', path: '/' },
  { icon: Database, label: 'مدیریت داده‌ها', path: '/database' },
  { icon: Music2, label: 'برنامه‌ها', path: '/programs' },
  { icon: Users, label: 'هنرمندان', path: '/artists' },
  { icon: Settings, label: 'تنظیمات', path: '/settings' },
]

export const Route = createRootRoute({
  component: RootComponent,
})

function RootComponent() {
  const location = useLocation()

  return (
    <TooltipProvider>
      <SidebarProvider>
        <div className="flex h-screen bg-background font-vazir text-foreground w-full" dir="rtl">
          {/* Main Sidebar */}
          <Sidebar side="right" variant="inset" collapsible="icon">
            <SidebarHeader className="h-16 flex flex-row items-center px-4 gap-4">
              <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center border border-primary/20 shrink-0">
                <Music className="w-5 h-5 text-primary" />
              </div>
              <span className="font-black text-lg tracking-tight sidebar-hide-on-collapse">
                رادیو گلها
              </span>
            </SidebarHeader>

            <SidebarContent className="px-2 py-4">
              <SidebarMenu>
                {NAV_ITEMS.map((item) => {
                  const isActive = location.pathname.startsWith(item.path) && (item.path !== '/' || location.pathname === '/')
                  return (
                    <SidebarMenuItem key={item.path}>
                      <SidebarMenuButton 
                        isActive={isActive} 
                        tooltip={item.label}
                        className="h-10 rounded-lg"
                      >
                        <Link to={item.path as any} className="flex items-center gap-2.5 w-full h-full p-2">
                          <item.icon className="w-4.5 h-4.5 shrink-0" />
                          <span className="font-medium text-xs truncate">{item.label}</span>
                        </Link>
                      </SidebarMenuButton>
                    </SidebarMenuItem>
                  )
                })}
              </SidebarMenu>
            </SidebarContent>

            <SidebarFooter className="p-3 border-t border-border/50">
              <div className="flex items-center gap-3 sidebar-hide-on-collapse">
                <Avatar className="h-8 w-8 border border-border">
                  <AvatarImage src="" />
                  <AvatarFallback className="bg-teal-500/20 text-teal-400 text-[10px] font-bold">AG</AvatarFallback>
                </Avatar>
                <div className="flex flex-col text-right">
                  <span className="text-[11px] font-bold">ادمین پروژه</span>
                  <span className="text-[9px] text-muted-foreground uppercase tracking-widest font-black">Level 1</span>
                </div>
              </div>
            </SidebarFooter>
            <SidebarRail />
          </Sidebar>

          {/* Main Content Inset */}
          <SidebarInset className="bg-background flex flex-col min-w-0 h-screen overflow-hidden">
            {/* Header */}
            <header className="h-16 flex items-center justify-between px-6 bg-background/50 backdrop-blur-md border-b border-border z-10 shrink-0">
              <div className="flex items-center gap-4 flex-1 max-w-xl">
                <SidebarTrigger className="rtl-flip p-1" />
                <Separator orientation="vertical" className="h-4 bg-border/20" />
                <div className="relative w-full group max-w-md">
                   <div className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground group-focus-within:text-primary transition-colors">
                     <Search className="w-4 h-4" />
                   </div>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <button className="w-9 h-9 rounded-full border border-border flex items-center justify-center text-muted-foreground hover:bg-secondary hover:text-foreground transition-all relative">
                  <Bell className="w-4 h-4" />
                  <span className="absolute top-2 right-2 w-1.5 h-1.5 bg-teal-500 rounded-full border border-background"></span>
                </button>
              </div>
            </header>

            {/* Page Content */}
            <div className="flex-1 overflow-y-auto p-4 md:p-6 custom-scrollbar bg-secondary/5">
              <Outlet />
            </div>
          </SidebarInset>

          {/* Devtools */}
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
      </SidebarProvider>
    </TooltipProvider>
  )
}
