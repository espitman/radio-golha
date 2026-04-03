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
  Play
} from 'lucide-react'

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
        <div>
          <h1 className="text-4xl font-black mb-3 bg-gradient-to-l from-white to-slate-400 bg-clip-text text-transparent">خوش آمدید، ادمین عزیز</h1>
          <p className="text-slate-500 font-medium max-w-lg">وضعیت کلی پروژه آرشیو رادیو گلها و پیشرفت اسکرپرها را از این بخش دنبال کنید.</p>
        </div>
        <div className="flex gap-4">
          <button className="flex items-center gap-2 bg-slate-900 border border-white/5 px-5 py-2.5 rounded-2xl hover:bg-slate-800 transition-all font-bold text-sm outline-none">
            <Activity className="w-4 h-4" />
            مشاهده گزارشات
          </button>
          <button className="flex items-center gap-2 bg-teal-500 text-slate-950 px-6 py-2.5 rounded-2xl hover:bg-teal-400 transition-all font-bold text-sm shadow-lg shadow-teal-500/20 outline-none">
            <Play className="w-4 h-4 fill-current" />
            شروع اسکرپ جدید
          </button>
        </div>
      </section>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {STATS.map((stat, i) => (
          <div key={i} className="group bg-slate-900/40 backdrop-blur-md p-6 rounded-3xl border border-white/5 hover:border-teal-500/30 transition-all relative overflow-hidden">
            <div className="flex justify-between items-start mb-6">
              <div className={`${stat.bg} w-12 h-12 rounded-2xl flex items-center justify-center transition-transform group-hover:scale-110 duration-500`}>
                <stat.icon className={`w-6 h-6 ${stat.color}`} />
              </div>
              <div className="flex items-center gap-1 text-[10px] bg-emerald-500/10 text-emerald-400 font-black px-2 py-1 rounded-full border border-emerald-500/20">
                <ArrowUpRight className="w-3 h-3" />
                ۱۲٪+
              </div>
            </div>
            <div>
              <span className="text-slate-500 text-sm font-medium mb-1 block">{stat.label}</span>
              <span className="text-3xl font-black tracking-tight">{stat.value}</span>
            </div>
            
            {/* Background Accent */}
            <div className={`absolute top-0 right-0 w-32 h-32 blur-[80px] -mr-16 -mt-16 opacity-5 bg-teal-500 group-hover:opacity-10 transition-opacity pointer-events-none`} />
          </div>
        ))}
      </div>

      {/* Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Column: Recent Activity */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-slate-900/40 backdrop-blur-md p-8 rounded-[2.5rem] border border-white/5 h-full min-h-[400px]">
             <div className="flex justify-between items-center mb-8">
                <div className="flex items-center gap-3">
                   <div className="w-2 h-2 rounded-full bg-teal-500 shadow-[0_0_8px_rgba(20,184,166,0.6)] animate-pulse" />
                   <h3 className="text-xl font-black">آخرین فعالیت‌های اسکرپر</h3>
                </div>
                <button className="text-teal-400 text-sm font-bold hover:underline">مشاهده همه</button>
             </div>
             
             {/* Activity List Placeholder */}
             <div className="space-y-4">
                {[1, 2, 3].map(i => (
                   <div key={i} className="flex items-center justify-between p-4 bg-white/5 rounded-2xl border border-white/5 border-dashed">
                      <div className="flex items-center gap-4">
                         <div className="w-10 h-10 rounded-xl bg-slate-800 flex items-center justify-center">
                            <Database className="w-5 h-5 text-slate-400" />
                         </div>
                         <div>
                            <p className="text-sm font-bold">استخراج متادیتای برگ سبز شماره {i + 120}</p>
                            <span className="text-[10px] text-slate-500">۲ ساعت پیش • موفقیت‌آمیز</span>
                         </div>
                      </div>
                      <div className="px-3 py-1 bg-emerald-500/10 text-emerald-400 text-[10px] font-black rounded-lg border border-emerald-500/20">
                         ۱۰۰٪
                      </div>
                   </div>
                ))}
             </div>
          </div>
        </div>

        {/* Right Column: Active Jobs / Info */}
        <div className="space-y-6">
          <div className="bg-gradient-to-br from-teal-500/20 to-emerald-500/20 backdrop-blur-md p-8 rounded-[2.5rem] border border-teal-500/20 relative overflow-hidden group">
            <h3 className="text-xl font-black mb-4 relative z-10">مدیریت دیتابیس</h3>
            <p className="text-white/60 text-sm mb-6 relative z-10 leading-relaxed font-medium">دیتابیس SQLite در حال حاضر برای ۱,۵۶۱ رکورد بهینه‌سازی شده است. شاخص‌های جستجو فعال هستند.</p>
            <button className="w-full bg-white text-slate-900 font-black py-4 rounded-2xl hover:scale-[1.02] active:scale-[0.98] transition-all relative z-10 shadow-xl shadow-teal-500/10 outline-none">
               دریافت بک‌آپ (SQL)
            </button>
            <div className="absolute -bottom-10 -left-10 w-40 h-40 bg-teal-500/20 blur-[60px] rounded-full group-hover:scale-150 transition-transform duration-700" />
          </div>

          <div className="bg-slate-900/40 backdrop-blur-md p-8 rounded-[2.5rem) border border-white/5">
             <h3 className="text-xl font-black mb-6">سیستم CDN</h3>
             <div className="flex items-center justify-between p-4 bg-white/5 rounded-2xl border border-white/5 mb-4">
                <span className="text-sm font-bold">وضعیت اتصال</span>
                <span className="text-emerald-400 text-[10px] font-black bg-emerald-400/10 px-2 py-1 rounded-full border border-emerald-400/20">آنلاین</span>
             </div>
             <div className="h-2 bg-slate-800 rounded-full overflow-hidden">
                <div className="w-[65%] h-full bg-gradient-to-r from-teal-500 to-emerald-500 shadow-[0_0_10px_rgba(20,184,166,0.3)]" />
             </div>
             <div className="flex justify-between mt-2">
                <span className="text-[10px] text-slate-600 font-bold">فضای مصرف شده</span>
                <span className="text-[10px] text-slate-400 font-bold">۱۲۸ گیگابایت / ۵۰۰ گیگابایت</span>
             </div>
          </div>
        </div>
      </div>
    </div>
  )
}
