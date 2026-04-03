import { createFileRoute } from '@tanstack/react-router'
import { 
  TrendingUp, 
  Users, 
  Music, 
  Clock, 
  ArrowUpRight,
  Database,
  Search,
  Activity,
  Play,
  CheckCircle2
} from 'lucide-react'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  CardFooter,
} from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"

export const Route = createFileRoute('/')({
  component: Dashboard,
})

const STATS = [
  { label: 'کل برنامه‌ها', value: '۱,۵۶۱', icon: Music, color: 'text-blue-400', bg: 'bg-blue-400/10' },
  { label: 'هنرمندان ثبت شده', value: '۸۷۲', icon: Users, color: 'text-teal-400', bg: 'bg-teal-400/10' },
  { label: 'زمان کل آرشیو', value: '۴۲۰ ساعت', icon: Clock, color: 'text-emerald-400', bg: 'bg-emerald-400/10' },
  { label: 'تعداد جستجوها', value: '۱۲,۴۰۰', icon: Search, color: 'text-rose-400', bg: 'bg-rose-400/10' },
]

function Dashboard() {
  return (
    <div className="space-y-10 animate-in animate-out">
      {/* Welcome Section */}
      <section className="flex flex-col md:flex-row justify-between items-end gap-6">
        <div className="space-y-2">
          <Badge variant="outline" className="bg-primary/5 text-primary border-primary/20 px-3 py-1 font-black rounded-full">سیستم آنلاین</Badge>
          <h1 className="text-4xl font-black bg-gradient-to-l from-foreground to-muted-foreground bg-clip-text text-transparent">خوش آمدید، ادمین عزیز</h1>
          <p className="text-muted-foreground font-medium max-w-lg">وضعیت کلی پروژه آرشیو رادیو گلها و پیشرفت اسکرپرها را از این بخش دنبال کنید.</p>
        </div>
        <div className="flex gap-4">
          <Button variant="secondary" className="px-6 h-11 font-bold rounded-xl space-x-2 rtl:space-x-reverse">
            <Activity className="w-4 h-4" />
            <span>مشاهده گزارشات</span>
          </Button>
          <Button className="px-8 h-11 font-bold rounded-xl space-x-2 rtl:space-x-reverse shadow-lg shadow-primary/20">
            <Play className="w-4 h-4 fill-current" />
            <span>شروع اسکرپ جدید</span>
          </Button>
        </div>
      </section>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {STATS.map((stat, i) => (
          <Card key={i} className="group border-border/50 bg-card/50 backdrop-blur-md hover:border-primary/30 transition-all duration-300 rounded-[2rem] overflow-hidden">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-6">
              <div className={`${stat.bg} w-12 h-12 rounded-2xl flex items-center justify-center transition-transform group-hover:scale-110 duration-500`}>
                <stat.icon className={`w-6 h-6 ${stat.color}`} />
              </div>
              <Badge variant="outline" className="bg-emerald-500/10 text-emerald-400 border-none px-2 py-1 font-black">
                ۱۲٪+
              </Badge>
            </CardHeader>
            <CardContent>
              <div className="text-muted-foreground text-sm font-medium mb-1">{stat.label}</div>
              <div className="text-3xl font-black tracking-tight">{stat.value}</div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Column: Recent Activity */}
        <div className="lg:col-span-2 space-y-6">
          <Card className="border-border/50 bg-card/40 backdrop-blur-md rounded-[2.5rem] h-full overflow-hidden">
            <CardHeader className="flex flex-row items-center justify-between pb-8 pt-8 px-8">
              <div className="flex items-center gap-3">
                 <div className="w-2 h-2 rounded-full bg-primary shadow-[0_0_8px_rgba(var(--primary),0.6)] animate-pulse" />
                 <CardTitle className="text-xl font-black">آخرین فعالیت‌های اسکرپر</CardTitle>
              </div>
              <Button variant="ghost" className="text-primary hover:text-primary hover:bg-primary/5 font-bold transition-all">مشاهده همه</Button>
            </CardHeader>
            <CardContent className="px-8 pb-8 space-y-4">
              {[1, 2, 3].map(i => (
                 <div key={i} className="flex items-center justify-between p-4 bg-muted/30 rounded-2xl border border-border/50 group hover:bg-muted/50 transition-all">
                    <div className="flex items-center gap-4">
                       <div className="w-10 h-10 rounded-xl bg-background flex items-center justify-center border border-border/50 group-hover:border-primary/20 transition-all">
                          <Database className="w-5 h-5 text-muted-foreground group-hover:text-primary transition-all" />
                       </div>
                       <div>
                          <p className="text-sm font-bold">استخراج متادیتای برگ سبز شماره {i + 120}</p>
                          <span className="text-[10px] text-muted-foreground">۲ ساعت پیش • موفقیت‌آمیز</span>
                       </div>
                    </div>
                    <Badge className="bg-emerald-500/10 text-emerald-400 border-none font-black rounded-lg">۱۰۰٪</Badge>
                 </div>
              ))}
              <div className="flex items-center gap-4 p-4 opacity-50 italic text-xs font-medium">
                  پس از انجام اسکرپ، داده‌ها در دیتابیس لوکال ذخیره می‌شوند...
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right Column: Active Jobs / Info */}
        <div className="space-y-6">
          <Card className="bg-gradient-to-br from-primary/20 to-teal-500/10 backdrop-blur-md rounded-[2.5rem] border-primary/20 relative overflow-hidden group p-2">
            <CardHeader className="pt-6 px-6">
              <CardTitle className="text-xl font-black">مدیریت دیتابیس</CardTitle>
              <CardDescription className="text-foreground/70 font-medium leading-relaxed pt-2">
                دیتابیس SQLite در حال حاضر برای ۱,۵۶۱ رکورد بهینه‌سازی شده است. تمام جداول نرمال هستند.
              </CardDescription>
            </CardHeader>
            <CardFooter className="px-6 pb-6 pt-4">
              <Button className="w-full bg-foreground text-background font-black py-6 rounded-2xl hover:scale-[1.02] active:scale-[0.98] transition-all shadow-xl shadow-primary/10">
                 دریافت بک‌آپ (SQL)
              </Button>
            </CardFooter>
            <div className="absolute -bottom-10 -left-10 w-40 h-40 bg-primary/20 blur-[60px] rounded-full group-hover:scale-150 transition-transform duration-700 pointer-events-none" />
          </Card>

          <Card className="border-border/50 bg-card/40 backdrop-blur-md rounded-[2.5rem] overflow-hidden">
             <CardHeader className="pt-8 px-8">
               <CardTitle className="text-xl font-black">سیستم CDN</CardTitle>
             </CardHeader>
             <CardContent className="px-8 pb-8 space-y-6">
               <div className="flex items-center justify-between p-4 bg-muted/40 rounded-2xl border border-border/50">
                  <span className="text-sm font-bold">وضعیت اتصال</span>
                  <Badge className="bg-emerald-500/10 text-emerald-400 border-none font-black flex gap-1.5 items-center">
                    <CheckCircle2 className="w-3 h-3" />
                    آنلاین
                  </Badge>
               </div>
               <div className="space-y-3">
                  <div className="h-2.5 bg-muted rounded-full overflow-hidden border border-border/50">
                    <div className="w-[65%] h-full bg-gradient-to-r from-primary to-teal-500 shadow-[0_0_15px_rgba(var(--primary),0.3)] shadow-primary/30" />
                  </div>
                  <div className="flex justify-between">
                    <span className="text-[10px] text-muted-foreground font-black">فضای مصرف شده</span>
                    <span className="text-[10px] text-foreground font-black">۱۲۸ گیگابایت / ۵۰۰ گیگابایت</span>
                  </div>
               </div>
             </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
