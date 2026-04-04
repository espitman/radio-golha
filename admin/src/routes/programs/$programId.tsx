import { createFileRoute, Link } from '@tanstack/react-router'
import { useState, useEffect, useRef } from 'react'
import { Badge } from "@/components/ui/badge"
import { Card, CardHeader, CardContent, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { Music, Mic, BookOpen, Clock, ArrowLeft, Play, User2, Disc3, Timer, Volume2, Radio, Library, PenTool, Layout } from "lucide-react"

export const Route = createFileRoute('/programs/$programId')({
  component: ProgramDetail,
})

function ProgramDetail() {
  const { programId } = Route.useParams()
  const [data, setData] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const audioRef = useRef<HTMLAudioElement>(null)

  useEffect(() => {
    fetch(`/api/program/${programId}`)
      .then(res => res.json())
      .then(d => { setData(d); setLoading(false); })
  }, [programId])

  const timeToSeconds = (timeStr: string) => {
    if (!timeStr) return 0;
    const parts = timeStr.split(':').map(Number);
    if (parts.length === 3) return parts[0] * 3600 + parts[1] * 60 + parts[2];
    return parts[0] * 60 + parts[1] || 0;
  }

  const seekTo = (time: string) => {
    if (audioRef.current) {
        audioRef.current.currentTime = timeToSeconds(time);
        audioRef.current.play();
    }
  }

  if (loading) return <div className="p-10 text-center text-primary font-black animate-pulse text-[10px]">در حال بازیابی فایل‌های صوتی...</div>
  if (!data) return <div className="p-10 text-center text-red-500 font-bold">برنامه یافت نشد!</div>

  return (
    <div className="p-0 space-y-4 animate-in" dir="rtl">
      {/* PROFESSIONAL ELEGANT HEADER */}
      <div className="relative overflow-hidden rounded-[1.5rem] bg-gradient-to-br from-primary to-[#0c242c] p-4 md:p-6 text-primary-foreground shadow-2xl border border-white/5">
        <div className="relative z-10 flex flex-col md:flex-row justify-between items-center gap-4">
          <div className="space-y-2 text-right">
            <Link to="/programs" className="inline-flex items-center gap-1.5 text-white/40 hover:text-white text-[9px] font-black transition-all bg-white/5 px-2.5 py-1 rounded-full border border-white/5">
               آرشیو آرایه‌ها <ArrowLeft className="w-3 h-3" />
            </Link>
            <div className="space-y-0.5">
              <div className="flex items-center gap-2 justify-end">
                <Badge className="bg-secondary text-primary border-transparent px-2 py-0 rounded-full text-[8px] font-black">{data.category_name}</Badge>
                <span className="text-white/20 text-[9px] font-mono tracking-tighter">REF_#{data.id}</span>
              </div>
              <h1 className="text-2xl md:text-3xl font-black text-white leading-tight">{data.title}</h1>
              <div className="flex items-center gap-3 text-white/30 text-[10px] font-medium justify-end font-mono">NO: {data.no}</div>
            </div>
          </div>
          <div className="p-4 bg-white/5 backdrop-blur-3xl rounded-[1.5rem] border border-white/5 shadow-inner">
             <div className="flex items-center gap-3">
               <button onClick={() => audioRef.current?.play()} className="w-10 h-10 rounded-xl bg-secondary text-primary flex items-center justify-center hover:scale-105 active:scale-95 shadow-lg"><Play className="w-4 h-4 fill-primary rotate-180 translate-x-[2px]"/></button>
               {data.audio_url && <audio ref={audioRef} controls src={data.audio_url} className="h-7 opacity-60 w-full md:w-56" />}
             </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-4 items-start">
        {/* COMPACT MASTER TIMELINE (3/4 Width) */}
        <div className="lg:col-span-3 space-y-4">
            <Card className="rounded-[1.5rem] border-primary/10 bg-white/95 shadow-xl flex flex-col border-r-4 border-r-primary">
              <CardHeader className="p-4 py-3 border-b border-primary/5 flex flex-row items-center justify-between">
                <CardTitle className="text-primary text-sm font-black flex items-center gap-2">تایم‌لاین تحلیلی قطعه <Timer className="w-4 h-4"/></CardTitle>
                <span className="text-[8px] text-muted-foreground font-mono uppercase opacity-30">Analytical Forensic v5.2</span>
              </CardHeader>
              <CardContent className="p-0 overflow-y-auto max-h-[700px] custom-scrollbar">
                 <div className="relative p-4 pr-10">
                    <div className="absolute right-[2.1rem] top-4 bottom-4 w-px bg-primary/10" />
                    <div className="space-y-4">
                    {data.timeline.map((t: any, index: number) => (
                      <div key={index} className="relative pr-6 group cursor-pointer" onClick={() => seekTo(t.start_time)}>
                        <div className="absolute right-[-2.5px] top-2 w-2 h-2 rounded-full bg-white border border-primary group-hover:bg-secondary group-hover:border-secondary transition-all z-10 shadow-sm" />
                        <div className="bg-card/20 p-4 rounded-xl border border-border/40 group-hover:bg-white group-hover:border-primary/20 transition-all shadow-sm">
                          <div className="flex justify-between items-center mb-3">
                             <div className="flex items-center gap-2">
                                <span className="bg-primary/5 text-primary font-black px-2 py-0.5 rounded-lg text-[10px] font-mono border border-primary/10">{t.start_time}</span>
                                <span className="text-foreground font-black text-sm group-hover:text-primary transition-all">{t.mode_name || 'تکه اثر'}</span>
                             </div>
                             <Volume2 className="w-3.5 h-3.5 text-primary opacity-20 group-hover:opacity-100" />
                          </div>
                          
                          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-y-4 gap-x-4 pt-3 border-t border-primary/5">
                               {t.singers.length > 0 && (
                                 <div className="space-y-1">
                                   <span className="text-[8px] text-muted-foreground font-black flex items-center gap-1 uppercase tracking-tight"><Mic className="w-2.5 h-2.5 text-primary"/> خواننده آواز این بخش</span>
                                   <span className="text-xs font-black text-primary/80 leading-tight block">{t.singers.join('، ')}</span>
                                 </div>
                               )}
                               {t.performers.length > 0 && (
                                 <div className="space-y-2 sm:col-span-2">
                                   <span className="text-[8px] text-muted-foreground font-black flex items-center gap-1 uppercase tracking-tight"><Music className="w-2.5 h-2.5 text-secondary"/> نوازندگی و تکنوازی</span>
                                   <div className="flex flex-wrap gap-1.5">
                                     {t.performers.map((p: any) => (
                                       <span key={p.name} className="text-[9px] font-black text-foreground/80 bg-secondary/10 px-2 py-0.5 rounded-md border border-secondary/20 transition-all hover:bg-secondary/30">
                                         {p.name} <span className="text-[8px] font-medium text-muted-foreground italic">({p.instrument})</span>
                                       </span>
                                     ))}
                                   </div>
                                 </div>
                               )}
                               {t.poets.length > 0 && (
                                 <div className="space-y-1">
                                   <span className="text-[8px] text-muted-foreground font-black flex items-center gap-1 uppercase tracking-tight"><BookOpen className="w-2.5 h-2.5 text-primary/60"/> شاعر ابیات این بخش</span>
                                   <span className="text-xs font-black text-foreground/70">{t.poets.join('، ')}</span>
                                 </div>
                               )}
                               {t.announcers.length > 0 && (
                                 <div className="space-y-1">
                                   <span className="text-[8px] text-muted-foreground font-black flex items-center gap-1 uppercase tracking-tight"><Radio className="w-2.5 h-2.5 text-primary/40"/> گوینده</span>
                                   <span className="text-[11px] font-bold text-foreground/50">{t.announcers.join('، ')}</span>
                                 </div>
                               )}
                          </div>
                        </div>
                      </div>
                    ))}
                    </div>
                 </div>
              </CardContent>
            </Card>
        </div>

        {/* COMPACT SIDE INFO (1/4 Width) - FULL METADATA شناسنامه کلی */}
        <div className="space-y-4 lg:sticky lg:top-4 overflow-y-auto max-h-[calc(100vh-100px)] custom-scrollbar pl-1">
           <Card className="rounded-[1.5rem] border-border/40 bg-card/40 backdrop-blur-md shadow-lg overflow-hidden flex flex-col">
             <CardHeader className="bg-primary/5 border-b border-border/10 p-4">
                <CardTitle className="text-primary text-xs font-black flex items-center gap-2 justify-end text-right">شناسنامه‌ی نهایی اثر <Library className="w-4 h-4"/></CardTitle>
             </CardHeader>
             <CardContent className="p-4 space-y-4 text-right">
                
                {/* 1. SINGERS & MUSICIANS (Primary) */}
                <div className="space-y-3">
                   <h4 className="text-[9px] text-muted-foreground font-black uppercase border-r-2 border-primary pr-2"> خوانندگان </h4>
                   <div className="flex flex-wrap gap-1 justify-end">
                     {data.singers?.map((s: string) => <Badge key={s} className="bg-primary text-white border-transparent px-2 py-0.5 rounded-lg text-[9px] font-bold">{s}</Badge>)}
                   </div>
                </div>

                <div className="space-y-3">
                   <h4 className="text-[9px] text-muted-foreground font-black uppercase border-r-2 border-secondary pr-2"> نوازندگان اصلی </h4>
                   <div className="grid grid-cols-1 gap-2">
                     {data.performers?.map((p: any) => (
                       <div key={p.name} className="flex flex-col bg-white/40 p-2 rounded-xl border border-border/10 shadow-sm">
                           <span className="text-foreground font-black text-[11px]">{p.name}</span>
                           <span className="text-primary/60 text-[8px] font-bold mt-0.5">{p.instrument}</span>
                       </div>
                     ))}
                   </div>
                </div>

                <Separator className="bg-primary/5" />

                {/* 2. POETS, COMPOSERS, ARRANGERS (Secondary) */}
                <div className="grid grid-cols-1 gap-4 pt-2">
                   {data.poets?.length > 0 && (
                     <div className="space-y-1.5">
                       <h4 className="text-[9px] text-muted-foreground font-black uppercase border-r-2 border-primary/30 pr-2"> شاعران </h4>
                       <span className="text-xs font-black text-foreground/80 block">{data.poets.join('، ')}</span>
                     </div>
                   )}
                   {data.composers?.length > 0 && (
                     <div className="space-y-1.5">
                       <h4 className="text-[9px] text-muted-foreground font-black uppercase border-r-2 border-primary/30 pr-2"><PenTool className="w-2.5 h-2.5 inline ml-1"/> آهنگساز </h4>
                       <span className="text-xs font-black text-primary/80 block">{data.composers.join('، ')}</span>
                     </div>
                   )}
                   {data.arrangers?.length > 0 && (
                     <div className="space-y-1.5">
                       <h4 className="text-[9px] text-muted-foreground font-black uppercase border-r-2 border-primary/30 pr-2"><Layout className="w-2.5 h-2.5 inline ml-1"/> تنظیم‌کننده </h4>
                       <span className="text-xs font-black text-primary/80 block">{data.arrangers.join('، ')}</span>
                     </div>
                   )}
                </div>

                <Separator className="bg-primary/5" />

                {/* 3. ANNOUNCERS & ORCHESTRAS (Tertiary) */}
                <div className="grid grid-cols-1 gap-4">
                   {data.announcers?.length > 0 && (
                     <div className="space-y-1.5">
                       <h4 className="text-[9px] text-muted-foreground font-black uppercase border-r-2 border-primary/30 pr-1 flex items-center justify-end gap-1"><Radio className="w-2.5 h-2.5"/> گوینده برنامه </h4>
                       <span className="text-xs font-black text-foreground/60 block">{data.announcers.join('، ')}</span>
                     </div>
                   )}
                   {data.orchestras?.length > 0 && (
                     <div className="space-y-1.5">
                       <h4 className="text-[9px] text-muted-foreground font-black uppercase border-r-2 border-primary/30 pr-1 flex items-center justify-end gap-1"><Library className="w-2.5 h-2.5"/> ارکستر </h4>
                       <span className="text-xs font-black text-foreground/60 block">{data.orchestras.join('، ')}</span>
                     </div>
                   )}
                </div>

             </CardContent>
           </Card>
        </div>
      </div>
    </div>
  )
}
