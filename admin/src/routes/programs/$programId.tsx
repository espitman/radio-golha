import { createFileRoute, Link } from '@tanstack/react-router'
import { type ReactNode, useEffect, useRef, useState } from 'react'
import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import {
  ArrowLeft,
  BookOpen,
  Clock3,
  Disc3,
  Layout,
  Library,
  Mic,
  Music,
  PenTool,
  Play,
  Radio,
  Timer,
  Volume2,
  Waves,
} from "lucide-react"

export const Route = createFileRoute('/programs/$programId')({
  component: ProgramDetail,
})

function timeToSeconds(timeStr: string) {
  if (!timeStr) return 0
  const parts = timeStr.split(':').map(Number)
  if (parts.length === 3) return parts[0] * 3600 + parts[1] * 60 + parts[2]
  return parts[0] * 60 + parts[1] || 0
}

function formatDuration(start?: string, end?: string) {
  const seconds = Math.max(0, timeToSeconds(end || '') - timeToSeconds(start || ''))
  if (!seconds) return 'نامشخص'
  const minutes = Math.floor(seconds / 60)
  const remaining = seconds % 60
  if (minutes && remaining) return `${minutes}:${remaining.toString().padStart(2, '0')}`
  if (minutes) return `${minutes} دقیقه`
  return `${remaining} ثانیه`
}

function MetaSection({
  title,
  icon: Icon,
  accentClass,
  children,
}: {
  title: string
  icon: typeof Library
  accentClass: string
  children: ReactNode
}) {
  return (
    <section className="space-y-3">
      <div className="flex items-center justify-start gap-2.5 text-right">
        <span className={`inline-flex h-8 w-8 items-center justify-center rounded-2xl shadow-sm ${accentClass}`}>
          <Icon className="w-4 h-4" />
        </span>
        <h3 className="text-[11px] font-black text-foreground/90">{title}</h3>
      </div>
      {children}
    </section>
  )
}

function ProgramDetail() {
  const { programId } = Route.useParams()
  const [data, setData] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [currentTime, setCurrentTime] = useState(0)
  const audioRef = useRef<HTMLAudioElement>(null)

  useEffect(() => {
    setLoading(true)
    fetch(`/api/program/${programId}`)
      .then((res) => res.json())
      .then((d) => {
        setData(d)
        setLoading(false)
      })
      .catch(() => setLoading(false))
  }, [programId])

  useEffect(() => {
    const audio = audioRef.current
    if (!audio) return
    const handleTimeUpdate = () => setCurrentTime(audio.currentTime)
    const handleEnded = () => setCurrentTime(0)
    audio.addEventListener('timeupdate', handleTimeUpdate)
    audio.addEventListener('ended', handleEnded)
    return () => {
      audio.removeEventListener('timeupdate', handleTimeUpdate)
      audio.removeEventListener('ended', handleEnded)
    }
  }, [data?.id])

  const seekTo = (time: string) => {
    if (!audioRef.current) return
    audioRef.current.currentTime = timeToSeconds(time)
    audioRef.current.play()
  }

  if (loading) return <div className="p-10 text-center text-primary font-black animate-pulse text-[10px]">در حال بازیابی فایل‌های صوتی...</div>
  if (!data) return <div className="p-10 text-center text-red-500 font-bold">برنامه یافت نشد!</div>

  return (
    <div className="space-y-6 animate-in" dir="rtl">
      {/* REFINED MINIMALIST PRESTIGE HEADER */}
      <header className="relative overflow-hidden rounded-[2.5rem] border border-primary/5 bg-[#0c242c] p-6 md:p-10 text-white shadow-2xl">
        <div className="absolute inset-0 bg-gradient-to-br from-primary/20 via-transparent to-black/40" />
        <div className="absolute -top-24 -right-24 h-64 w-64 rounded-full bg-primary/10 blur-[100px]" />
        
        <div className="relative z-10 flex flex-col gap-8 md:flex-row md:items-center md:justify-between">
          <div className="space-y-4 text-right">
             <Link to="/programs" className="inline-flex items-center gap-2 text-white/40 hover:text-white text-[10px] font-black transition-all bg-white/5 px-4 py-2 rounded-full border border-white/5">
                <ArrowLeft className="w-3.5 h-3.5" /> برگشت به آرشیو
             </Link>
             
             <div className="space-y-2">
                <div className="flex items-center gap-3 justify-end">
                   <Badge className="bg-secondary text-primary border-none px-3 py-1 rounded-full text-[9px] font-black uppercase tracking-tighter">
                      {data.category_name}
                   </Badge>
                   <span className="text-white/20 text-[10px] font-mono tracking-widest">ID: #{data.id}</span>
                </div>
                <h1 className="text-4xl md:text-5xl font-black leading-tight drop-shadow-2xl">{data.title}</h1>
                <div className="text-secondary/60 text-xs font-black font-mono">PROGRAM NO. {data.no}</div>
             </div>
          </div>

          <div className="flex flex-col items-center md:items-end gap-4">
             <div className="flex items-center gap-4 bg-white/5 backdrop-blur-md p-4 rounded-[2rem] border border-white/10 w-full md:w-auto">
                <button onClick={() => audioRef.current?.play()} className="w-14 h-14 rounded-2xl bg-secondary text-primary flex items-center justify-center hover:scale-105 active:scale-95 transition-all shadow-xl shadow-black/20">
                   <Play className="w-6 h-6 fill-primary rotate-180 translate-x-[2px]" />
                </button>
                <div className="flex flex-col flex-1">
                   {data.audio_url ? (
                     <>
                        <audio ref={audioRef} src={data.audio_url} className="h-8 opacity-70 w-full md:w-64" controls />
                        <div className="mt-2 text-[9px] text-white/30 font-black flex items-center gap-2 justify-end">
                           <Waves className="w-3 h-3 text-secondary animate-pulse" /> برای پرش به هر بخش روی کارت تایم‌لاین کلیک کن
                        </div>
                     </>
                   ) : (
                     <span className="text-white/30 text-[10px] font-bold">فایل صوتی یافت نشد</span>
                   )}
                </div>
             </div>
          </div>
        </div>
      </header>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-[400px_1fr] items-start">
        {/* SHINAHNAMEH SIDEBAR (RTL Rightmost in Grid) */}
        <aside className="space-y-6">
          <Card className="rounded-[2.5rem] border-primary/10 bg-white/95 shadow-xl overflow-hidden border-r-8 border-r-primary">
            <CardHeader className="bg-primary/5 border-b border-primary/5 p-6">
               <CardTitle className="text-primary text-base font-black flex items-center gap-2 justify-end text-right">
                  شناسنامه نهایی اثر <Library className="w-5 h-5" />
               </CardTitle>
            </CardHeader>
            <CardContent className="p-6 space-y-6 text-right scroll-smooth overflow-y-auto max-h-[800px] custom-scrollbar">
              <MetaSection title="خوانندگان اصلی" icon={Mic} accentClass="bg-primary/10 text-primary">
                <div className="flex flex-wrap gap-1.5 justify-start">
                  {(data.singers || []).map((name: string) => <Badge key={name} className="bg-primary text-white border-none px-3 py-1 rounded-lg text-[10px] font-bold">{name}</Badge>)}
                </div>
              </MetaSection>

              <MetaSection title="نوازندگی و تکنوازی" icon={Music} accentClass="bg-secondary/20 text-primary">
                <div className="grid gap-2">
                  {(data.performers || []).map((p: any) => (
                    <div key={p.name} className="flex items-center justify-between p-3 rounded-xl bg-white border border-border/40 shadow-sm transition-all hover:border-primary/20">
                       <span className="bg-primary/5 text-primary text-[9px] font-black px-2 py-0.5 rounded-lg border border-primary/10 italic">{p.instrument}</span>
                       <span className="text-[12px] font-black text-foreground">{p.name}</span>
                    </div>
                  ))}
                </div>
              </MetaSection>

              <Separator className="bg-primary/5" />

              <div className="grid gap-6">
                 {data.poets?.length > 0 && (
                   <MetaSection title="شاعران" icon={BookOpen} accentClass="bg-amber-100/50 text-amber-800">
                      <p className="text-[12px] font-black text-foreground/70 leading-relaxed bg-amber-50/50 p-3 rounded-xl border border-amber-200/50">{data.poets.join('، ')}</p>
                   </MetaSection>
                 )}
                 {data.composers?.length > 0 && (
                   <MetaSection title="آهنگساز" icon={PenTool} accentClass="bg-primary/10 text-primary">
                      <div className="text-[12px] font-black text-primary p-3 rounded-xl bg-primary/5 border border-primary/10">{data.composers.join('، ')}</div>
                   </MetaSection>
                 )}
              </div>

              {(data.announcers?.length > 0 || data.orchestras?.length > 0) && <Separator className="bg-primary/5" />}

              <div className="space-y-4">
                 {data.announcers?.length > 0 && (
                   <div className="space-y-1">
                      <span className="text-[9px] text-muted-foreground font-black block">گویندگان</span>
                      <span className="text-xs font-bold text-foreground/60">{data.announcers.join('، ')}</span>
                   </div>
                 )}
                 {data.orchestras?.length > 0 && (
                   <div className="space-y-1">
                      <span className="text-[9px] text-muted-foreground font-black block">ارکستر</span>
                      <span className="text-xs font-bold text-foreground/60">{data.orchestras.join('، ')}</span>
                   </div>
                 )}
              </div>
            </CardContent>
          </Card>
        </aside>

        {/* TIMELINE MAIN VIEW */}
        <main className="space-y-6">
           <Card className="rounded-[2.5rem] border-primary/10 bg-white shadow-2xl overflow-hidden">
              <CardHeader className="p-6 pb-4 border-b border-primary/5 flex flex-row items-center justify-between">
                 <CardTitle className="text-primary text-base font-black flex items-center gap-2">تایم‌لاین تحلیلی <Timer className="w-5 h-5"/></CardTitle>
                 <span className="text-[9px] font-mono opacity-30">Analysis v5.3</span>
              </CardHeader>
              <CardContent className="p-6 overflow-y-auto max-h-[800px] custom-scrollbar">
                 <div className="relative pr-10">
                    <div className="absolute right-[1.55rem] top-2 bottom-2 w-px bg-primary/10" />
                    <div className="space-y-4">
                       {data.timeline.map((segment: any, index: number) => {
                          const startSec = timeToSeconds(segment.start_time);
                          const isActive = currentTime >= startSec && (index === data.timeline.length - 1 || currentTime < timeToSeconds(data.timeline[index + 1].start_time));
                          
                          return (
                            <div key={index} onClick={() => seekTo(segment.start_time)} className={`relative pr-6 group cursor-pointer transition-all ${isActive ? 'scale-[1.01]' : ''}`}>
                               <div className={`absolute right-[-2.5px] top-4 w-2 h-2 rounded-full border shadow-sm transition-all z-10 ${isActive ? 'bg-secondary border-secondary scale-125' : 'bg-white border-primary/20 group-hover:bg-primary'}`} />
                               <div className={`p-4 rounded-2xl border transition-all ${isActive ? 'bg-primary/[0.03] border-primary/20 shadow-lg' : 'bg-white border-border/40 hover:border-primary/10'}`}>
                                  <div className="flex justify-between items-center mb-4">
                                     <div className="flex items-center gap-3">
                                        <span className="bg-primary text-white font-mono text-[10px] px-2 py-0.5 rounded-lg">{segment.start_time}</span>
                                        <h4 className="text-sm font-black text-foreground">{segment.mode_name || 'بخش اجرایی'}</h4>
                                     </div>
                                     {isActive && <Badge className="bg-primary/10 text-primary border-none text-[8px] animate-pulse">در حال پخش</Badge>}
                                  </div>
                                  
                                  <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                                     {segment.singers?.length > 0 && (
                                       <div className="space-y-1">
                                          <span className="text-[9px] text-muted-foreground font-black flex items-center gap-1"><Mic className="w-2.5 h-2.5"/> خواننده</span>
                                          <div className="text-[11px] font-black text-primary/80">{segment.singers.join('، ')}</div>
                                       </div>
                                     )}
                                     {segment.poets?.length > 0 && (
                                       <div className="space-y-1">
                                          <span className="text-[9px] text-muted-foreground font-black flex items-center gap-1"><BookOpen className="w-2.5 h-2.5"/> شاعر</span>
                                          <div className="text-[11px] font-black text-foreground/60">{segment.poets.join('، ')}</div>
                                       </div>
                                     )}
                                     {segment.performers?.length > 0 && (
                                       <div className="sm:col-span-3 pt-3 border-t border-primary/5">
                                          <div className="flex flex-wrap gap-2">
                                             {segment.performers.map((p: any) => (
                                                <span key={p.name} className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-lg bg-secondary/10 border border-secondary/20 text-[10px] font-black text-foreground/70">
                                                   <span className="text-[8px] text-muted-foreground opacity-70">({p.instrument})</span> {p.name}
                                                </span>
                                             ))}
                                          </div>
                                       </div>
                                     )}
                                  </div>
                               </div>
                            </div>
                          )
                       })}
                    </div>
                 </div>
              </CardContent>
           </Card>
        </main>
      </div>
    </div>
  )
}
