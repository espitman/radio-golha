import { createFileRoute, Link } from '@tanstack/react-router'
import { useEffect, useMemo, useState } from 'react'
import {
  Activity,
  AudioLines,
  Disc3,
  Library,
  Mic2,
  Music2,
  Shapes,
  TimerReset,
  Waves,
} from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

type DashboardResponse = {
  summary: {
    totalPrograms: number
    totalArtists: number
    totalSegments: number
    totalModes: number
    programsWithAudio: number
    programsWithTimeline: number
    totalCategories: number
    totalOrchestras: number
    totalInstruments: number
  }
  categoryBreakdown: Array<{ name: string; total: number }>
  topSingers: Array<{ name: string; total: number }>
  topModes: Array<{ name: string; total: number }>
  topOrchestras: Array<{ name: string; total: number }>
  recentPrograms: Array<{ id: number; title: string; no: number; sub_no: string | null; category_name: string }>
}

export const Route = createFileRoute('/')({
  component: Dashboard,
})

function toPersianNumber(value: number) {
  return new Intl.NumberFormat('fa-IR').format(value)
}

function Dashboard() {
  const [data, setData] = useState<DashboardResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    fetch('/api/dashboard')
      .then((res) => res.json())
      .then((response) => {
        setData(response)
        setLoading(false)
      })
      .catch(() => setLoading(false))
  }, [])

  const metrics = useMemo(() => {
    if (!data) return []
    return [
      {
        label: 'کل برنامه‌ها',
        value: toPersianNumber(data.summary.totalPrograms),
        icon: Music2,
        tone: 'bg-primary text-white',
      },
      {
        label: 'هنرمندان ثبت‌شده',
        value: toPersianNumber(data.summary.totalArtists),
        icon: Mic2,
        tone: 'bg-secondary/30 text-primary',
      },
      {
        label: 'سگمنت‌های تایم‌لاین',
        value: toPersianNumber(data.summary.totalSegments),
        icon: TimerReset,
        tone: 'bg-emerald-100 text-emerald-900',
      },
      {
        label: 'دستگاه‌های یکتا',
        value: toPersianNumber(data.summary.totalModes),
        icon: Waves,
        tone: 'bg-amber-100 text-amber-900',
      },
    ]
  }, [data])

  if (loading) {
    return <div className="p-10 text-center text-primary font-black animate-pulse text-[11px]">در حال بازیابی داشبورد...</div>
  }

  if (!data) {
    return <div className="p-10 text-center text-red-500 font-bold">داده داشبورد بازیابی نشد.</div>
  }

  const audioCoverage = Math.round((data.summary.programsWithAudio / data.summary.totalPrograms) * 100)
  const timelineCoverage = Math.round((data.summary.programsWithTimeline / data.summary.totalPrograms) * 100)

  return (
    <div className="space-y-6 animate-in" dir="rtl">
      <section className="rounded-[2rem] border border-primary/10 bg-gradient-to-l from-primary to-[#173d49] p-6 text-white shadow-[0_24px_80px_rgba(12,36,44,0.18)]">
        <div className="grid gap-6 xl:grid-cols-[1.4fr_0.9fr] xl:items-end">
          <div className="space-y-4 text-right">
            <Badge className="rounded-full border-none bg-white/12 px-3 py-1 text-[10px] font-black text-white">
              داشبورد زنده آرشیو
            </Badge>
            <div>
              <h1 className="text-3xl font-black md:text-4xl">نمای کلی داده‌های واقعی رادیو گلها</h1>
              <p className="mt-2 max-w-3xl text-[13px] font-bold text-white/70">
                این صفحه مستقیماً از SQLite خوانده می‌شود و وضعیت برنامه‌ها، پوشش متادیتا، دسته‌بندی‌ها و پراکندگی داده‌ها را نشان می‌دهد.
              </p>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            {metrics.map((metric) => (
              <div key={metric.label} className="rounded-[1.5rem] border border-white/10 bg-white/8 p-4 backdrop-blur-md">
                <div className="mb-4 flex items-center justify-between">
                  <span className={`inline-flex h-11 w-11 items-center justify-center rounded-2xl ${metric.tone}`}>
                    <metric.icon className="h-5 w-5" />
                  </span>
                  <span className="text-2xl font-black">{metric.value}</span>
                </div>
                <div className="text-[11px] font-black text-white/65">{metric.label}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <div className="grid gap-5 xl:grid-cols-[1.2fr_0.8fr]">
        <Card className="overflow-hidden rounded-[1.8rem] border-primary/10 bg-white/85 !pt-0 shadow-[0_18px_45px_rgba(31,78,95,0.06)]">
          <CardHeader className="border-b border-primary/8 bg-primary/[0.03] px-5 py-4">
            <CardTitle className="text-right text-base font-black text-primary">پوشش داده و سلامت آرشیو</CardTitle>
          </CardHeader>
          <CardContent className="grid gap-4 p-5 md:grid-cols-2">
            <div className="rounded-[1.4rem] border border-primary/10 bg-background/70 p-4 text-right">
              <div className="mb-3 flex items-center justify-between">
                <span className="text-[11px] font-black text-muted-foreground">پوشش فایل صوتی</span>
                <AudioLines className="h-4 w-4 text-primary/70" />
              </div>
              <div className="text-3xl font-black text-primary">{toPersianNumber(audioCoverage)}٪</div>
              <div className="mt-2 text-[11px] font-bold text-muted-foreground">
                {toPersianNumber(data.summary.programsWithAudio)} از {toPersianNumber(data.summary.totalPrograms)} برنامه فایل صوتی دارند
              </div>
            </div>

            <div className="rounded-[1.4rem] border border-primary/10 bg-background/70 p-4 text-right">
              <div className="mb-3 flex items-center justify-between">
                <span className="text-[11px] font-black text-muted-foreground">پوشش تایم‌لاین</span>
                <Activity className="h-4 w-4 text-primary/70" />
              </div>
              <div className="text-3xl font-black text-primary">{toPersianNumber(timelineCoverage)}٪</div>
              <div className="mt-2 text-[11px] font-bold text-muted-foreground">
                {toPersianNumber(data.summary.programsWithTimeline)} برنامه segment-based timeline دارند
              </div>
            </div>

            <div className="rounded-[1.4rem] border border-primary/10 bg-background/70 p-4 text-right">
              <div className="mb-3 flex items-center justify-between">
                <span className="text-[11px] font-black text-muted-foreground">دسته‌های فعال</span>
                <Shapes className="h-4 w-4 text-primary/70" />
              </div>
              <div className="text-3xl font-black text-primary">{toPersianNumber(data.summary.totalCategories)}</div>
              <div className="mt-2 text-[11px] font-bold text-muted-foreground">
                تعداد دسته‌های اصلی که در آرشیو برنامه دارند
              </div>
            </div>

            <div className="rounded-[1.4rem] border border-primary/10 bg-background/70 p-4 text-right">
              <div className="mb-3 flex items-center justify-between">
                <span className="text-[11px] font-black text-muted-foreground">ارکستر و ساز</span>
                <Disc3 className="h-4 w-4 text-primary/70" />
              </div>
              <div className="text-3xl font-black text-primary">
                {toPersianNumber(data.summary.totalOrchestras)} / {toPersianNumber(data.summary.totalInstruments)}
              </div>
              <div className="mt-2 text-[11px] font-bold text-muted-foreground">
                ارکسترهای canonical در برابر سازهای یکتای ثبت‌شده
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="overflow-hidden rounded-[1.8rem] border-primary/10 bg-white/85 !pt-0 shadow-[0_18px_45px_rgba(31,78,95,0.06)]">
          <CardHeader className="border-b border-primary/8 bg-primary/[0.03] px-5 py-4">
            <CardTitle className="text-right text-base font-black text-primary">توزیع دسته‌ها</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 p-5">
            {data.categoryBreakdown.map((item) => {
              const percent = Math.max(4, Math.round((item.total / data.summary.totalPrograms) * 100))
              return (
                <div key={item.name} className="space-y-2">
                  <div className="flex items-center justify-between gap-3 text-[12px] font-black">
                    <span className="text-primary">{toPersianNumber(item.total)} برنامه</span>
                    <span className="text-foreground">{item.name}</span>
                  </div>
                  <div className="h-2.5 overflow-hidden rounded-full bg-primary/8">
                    <div className="h-full rounded-full bg-gradient-to-l from-primary to-secondary" style={{ width: `${percent}%` }} />
                  </div>
                </div>
              )
            })}
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-5 xl:grid-cols-3">
        <Card className="overflow-hidden rounded-[1.8rem] border-primary/10 bg-white/85 !pt-0 shadow-[0_18px_45px_rgba(31,78,95,0.06)]">
          <CardHeader className="border-b border-primary/8 bg-primary/[0.03] px-5 py-4">
            <CardTitle className="text-right text-sm font-black text-primary">خواننده‌های پرتکرار</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 p-5">
            {data.topSingers.map((item, index) => (
              <div key={item.name} className="flex items-center justify-between rounded-[1.2rem] border border-primary/10 bg-background/60 px-3 py-3">
                <div className="text-right">
                  <div className="text-sm font-black text-foreground">{item.name}</div>
                  <div className="text-[10px] font-bold text-muted-foreground">رتبه {toPersianNumber(index + 1)}</div>
                </div>
                <Badge className="rounded-full border-none bg-primary px-2.5 py-1 text-[9px] font-black text-white">
                  {toPersianNumber(item.total)}
                </Badge>
              </div>
            ))}
          </CardContent>
        </Card>

        <Card className="overflow-hidden rounded-[1.8rem] border-primary/10 bg-white/85 !pt-0 shadow-[0_18px_45px_rgba(31,78,95,0.06)]">
          <CardHeader className="border-b border-primary/8 bg-primary/[0.03] px-5 py-4">
            <CardTitle className="text-right text-sm font-black text-primary">دستگاه‌های پرتکرار</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 p-5">
            {data.topModes.map((item) => (
              <div key={item.name} className="flex items-center justify-between rounded-[1.2rem] border border-primary/10 bg-background/60 px-3 py-3">
                <div className="text-right text-sm font-black text-foreground">{item.name}</div>
                <Badge variant="outline" className="rounded-full border-primary/20 bg-secondary/10 px-2.5 py-1 text-[9px] font-black text-primary">
                  {toPersianNumber(item.total)}
                </Badge>
              </div>
            ))}
          </CardContent>
        </Card>

        <Card className="overflow-hidden rounded-[1.8rem] border-primary/10 bg-white/85 !pt-0 shadow-[0_18px_45px_rgba(31,78,95,0.06)]">
          <CardHeader className="border-b border-primary/8 bg-primary/[0.03] px-5 py-4">
            <CardTitle className="text-right text-sm font-black text-primary">آخرین برنامه‌های واردشده</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 p-5">
            {data.recentPrograms.map((program) => (
              <Link
                key={program.id}
                to="/programs/$programId"
                params={{ programId: program.id.toString() }}
                className="block rounded-[1.2rem] border border-primary/10 bg-background/60 px-3 py-3 transition-all hover:border-primary/25 hover:bg-white"
              >
                <div className="mb-2 flex items-center justify-between gap-3">
                  <Badge className="rounded-full border-none bg-primary px-2.5 py-1 text-[9px] font-black text-white">
                    {program.sub_no ? `${toPersianNumber(program.no)} ${program.sub_no}` : toPersianNumber(program.no)}
                  </Badge>
                  <span className="text-[10px] font-bold text-muted-foreground">#{toPersianNumber(program.id)}</span>
                </div>
                <div className="text-right">
                  <div className="line-clamp-1 text-sm font-black text-foreground">{program.title}</div>
                  <div className="mt-1 text-[10px] font-bold text-muted-foreground">{program.category_name}</div>
                </div>
              </Link>
            ))}
          </CardContent>
        </Card>
      </div>

      <Card className="overflow-hidden rounded-[1.8rem] border-primary/10 bg-white/85 !pt-0 shadow-[0_18px_45px_rgba(31,78,95,0.06)]">
        <CardHeader className="border-b border-primary/8 bg-primary/[0.03] px-5 py-4">
          <CardTitle className="text-right text-sm font-black text-primary">خلاصه ارکسترهای فعال</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-4 p-5 md:grid-cols-2">
          {data.topOrchestras.map((item) => (
            <div key={item.name} className="flex items-center justify-between rounded-[1.3rem] border border-primary/10 bg-background/60 px-4 py-4">
              <div className="flex items-center gap-2 text-right">
                <Library className="h-4 w-4 text-primary/70" />
                <span className="text-sm font-black text-foreground">{item.name}</span>
              </div>
              <Badge variant="outline" className="rounded-full border-primary/20 bg-secondary/10 px-3 py-1 text-[10px] font-black text-primary">
                {toPersianNumber(item.total)} برنامه
              </Badge>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  )
}
