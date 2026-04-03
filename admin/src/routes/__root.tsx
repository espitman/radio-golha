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
  PanelLeft,
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
  { icon: Music2, label: 'برنامه‌ها', path: '/programmes' },
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
            <SidebarHeader className="h-20 flex flex-row items-center px-6 gap-4">
              <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center border border-primary/20 shrink-0">
                <Music className="w-6 h-6 text-primary" />
              </div>
              <span className="font-black text-xl tracking-tight sidebar-hide-on-collapse">
                رادیو گلها
              </span>
            </SidebarHeader>

            <SidebarContent className="px-3 py-6">
              <SidebarMenu>
                {NAV_ITEMS.map((item) => {
                  const isActive = location.pathname === item.path
                  return (
                    <SidebarMenuItem key={item.path}>
                      <SidebarMenuButton 
                        asChild 
                        isActive={isActive} 
                        tooltip={item.label}
                        className="py-6 px-4 h-12 rounded-xl"
                      >
                        <Link to={item.path as any}>
                          <item.icon className="w-5 h-5" />
                          <span className="font-medium text-sm">{item.label}</span>
                        </Link>
                      </SidebarMenuButton>
                    </SidebarMenuItem>
                  )
                })}
              </SidebarMenu>
            </SidebarContent>

            <SidebarFooter className="p-4 border-t border-border/50">
              <div className="flex items-center gap-3 sidebar-hide-on-collapse">
                <Avatar className="h-10 w-10 border border-border">
                  <AvatarImage src="" />
                  <AvatarFallback className="bg-teal-500/20 text-teal-400 text-xs font-bold">AG</AvatarFallback>
                </Avatar>
                <div className="flex flex-col text-right">
                  <span className="text-xs font-bold">ادمین پروژه</span>
                  <span className="text-[10px] text-muted-foreground uppercase tracking-widest font-black">Level 1</span>
                </div>
              </div>
            </SidebarFooter>
            <SidebarRail />
          </Sidebar>

          {/* Main Content Inset */}
          <SidebarInset className="bg-background flex flex-col min-w-0 h-screen overflow-hidden">
            {/* Header */}
            <header className="h-20 flex items-center justify-between px-8 bg-background/50 backdrop-blur-md border-b border-border z-10 shrink-0">
              <div className="flex items-center gap-6 flex-1 max-w-xl">
                <SidebarTrigger className="rtl-flip" />
                <Separator orientation="vertical" className="h-4 bg-border/20" />
                <div className="relative w-full group max-w-md">
                   <div className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground group-focus-within:text-primary transition-colors">
                     <Search className="w-4 h-4" />
                   </div>
                   <Input 
                    placeholder="جستجو در آرشیو..."
                    className="w-full bg-secondary/30 border-border/50 pr-10 focus:bg-background transition-all h-10 rounded-xl placeholder:text-muted-foreground placeholder:text-xs"
                   />
                </div>
              </div>

              <div className="flex items-center gap-4">
                <button className="w-10 h-10 rounded-full border border-border flex items-center justify-center text-muted-foreground hover:bg-secondary hover:text-foreground transition-all relative">
                  <Bell className="w-5 h-5" />
                  <span className="absolute top-2.5 right-2.5 w-2 h-2 bg-teal-500 rounded-full border-2 border-background"></span>
                </button>
              </div>
            </header>

            {/* Page Content */}
            <div className="flex-1 overflow-y-auto p-8 custom-scrollbar bg-secondary/5">
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
