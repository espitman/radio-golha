import { createFileRoute, Link } from '@tanstack/react-router'
import { useState, useEffect } from 'react'
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { Card, CardHeader, CardContent, CardTitle } from "@/components/ui/card"
import { Music, Mic, BookOpen, Clock, ArrowLeft, Play, User2, Disc3 } from "lucide-react"

export const Route = createFileRoute('/programs/$programId')({
  component: ProgramDetail,
})

function ProgramDetail() {
  const { programId } = Route.useParams()
  const [data, setData] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetch(`/api/program/${programId}`)
      .then(res => res.json())
      .then(d => {
        setData(d)
        setLoading(false)
      })
  }, [programId])

  if (loading) return <div className="p-10 text-center text-primary font-black animate-pulse text-sm">LOADING ARCHIVE...</div>
  if (!data) return <div className="p-10 text-center text-red-500 font-bold">404: PROGRAM NOT FOUND</div>

  return (
    <div className="p-0 space-y-6 animate-in">
      {/* Compact Stage Header */}
      <div className="relative overflow-hidden rounded-[2rem] bg-gradient-to-br from-primary to-[#0f2e3a] p-6 md:p-8 text-primary-foreground shadow-2xl shadow-primary/10">
        <div className="absolute top-0 right-0 w-48 h-48 bg-secondary/5 rounded-full -translate-y-1/2 translate-x-1/2 blur-2xl" />
        
        <div className="relative z-10 flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="space-y-3 w-full md:w-auto">
            <Link to="/programs" className="inline-flex items-center gap-2 text-white/40 hover:text-white text-[10px] font-bold transition-all bg-white/5 px-3 py-1.5 rounded-full backdrop-blur-md">
               <ArrowLeft className="w-3 h-3 rtl-flip" />
               آرشیو برنامه‌ها
            </Link>
            
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <Badge className="bg-secondary/20 text-secondary border-transparent backdrop-blur-md px-3 py-0.5 rounded-full text-[9px] font-black uppercase tracking-widest">
                    {data.category_name}
                </Badge>
                <span className="text-white/20 text-[10px] font-mono tracking-tighter">REF: #{data.id}</span>
              </div>
              <h1 className="text-3xl md:text-5xl font-black text-white leading-tight drop-shadow-sm">{data.title}</h1>
              <div className="flex items-center gap-3 text-white/50 text-[11px] font-medium">
                <span className="flex items-center gap-1.5"><Disc3 className="w-4 h-4"/> شماره: <span className="text-secondary font-bold font-mono">{data.no}</span></span>
              </div>
            </div>
          </div>

          <div className="w-full md:w-auto p-4 md:p-5 bg-white/5 backdrop-blur-2xl rounded-[1.5rem] border border-white/10 shadow-inner group transition-all hover:bg-white/10">
             <div className="flex flex-col gap-3">
               <div className="flex items-center gap-3">
                 <div className="w-10 h-10 rounded-xl bg-secondary/20 flex items-center justify-center text-secondary">
                   <Play className="w-5 h-5 fill-secondary translate-x-[1px]" />
                 </div>
                 <div className="flex flex-col min-w-[120px]">
                   <span className="text-white text-xs font-black">پخش موسیقی</span>
                   <span className="text-white/30 text-[9px] tracking-widest uppercase">Streaming Now</span>
                 </div>
               </div>
               {data.audio_url && (
                <audio controls src={data.audio_url} className="w-full md:w-64 h-8 rounded-full opacity-60 hover:opacity-100 transition-opacity" />
               )}
             </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left: Artists */}
        <div className="lg:col-span-2 space-y-6">
          <Card className="rounded-[2rem] border-border/40 bg-card/60 shadow-lg overflow-hidden backdrop-blur-md">
            <CardHeader className="bg-secondary/5 border-b border-border/10 p-6">
              <CardTitle className="text-primary text-xl font-black flex items-center gap-2.5">
                <User2 className="w-6 h-6" /> عوامل هنری
              </CardTitle>
            </CardHeader>
            <CardContent className="p-0">
               <div className="divide-y divide-border/10">
                  <div className="p-6">
                    <p className="text-[10px] text-muted-foreground flex items-center gap-2 mb-3 font-black uppercase tracking-widest"> خوانندگان </p>
                    <div className="flex flex-wrap gap-2">
                      {data.singers.map((s: string) => <Badge key={s} className="bg-primary/5 text-primary border-transparent px-3 py-1 rounded-xl text-xs font-bold">{s}</Badge>)}
                    </div>
                  </div>
                  <div className="p-6">
                    <p className="text-[10px] text-muted-foreground flex items-center gap-2 mb-4 font-black uppercase tracking-widest"> نوازندگان </p>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      {data.performers.map((p: any) => (
                        <div key={p.name} className="flex items-center gap-3 bg-white/20 p-3 rounded-2xl border border-border/10">
                          <div className="w-10 h-10 rounded-xl bg-secondary/10 flex items-center justify-center text-primary font-black text-sm">{p.name.charAt(0)}</div>
                          <div className="flex flex-col">
                            <span className="text-foreground font-black text-base leading-none">{p.name}</span>
                            <span className="text-muted-foreground text-[10px] font-medium tracking-tight mt-1">{p.instrument || 'نوازنده'}</span>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                  <div className="p-6">
                    <p className="text-[10px] text-muted-foreground flex items-center gap-2 mb-3 font-black uppercase tracking-widest"> شاعران </p>
                    <div className="flex flex-wrap gap-2">
                      {data.poets.map((s: string) => <Badge key={s} className="bg-secondary/10 text-primary border-transparent px-3 py-1 rounded-xl text-xs font-bold">{s}</Badge>)}
                    </div>
                  </div>
               </div>
            </CardContent>
          </Card>
        </div>

        {/* Right: Timeline */}
        <div className="space-y-6">
            <Card className="rounded-[2rem] border-border/40 bg-primary shadow-2xl shadow-primary/5 h-full overflow-hidden flex flex-col">
              <CardHeader className="p-6 pb-2">
                <CardTitle className="text-secondary text-lg font-black flex items-center gap-2.5">
                  <Clock className="w-5 h-5" /> تایم‌لاین
                </CardTitle>
                <p className="text-white/20 text-[9px] tracking-widest font-mono uppercase">MODES & TIME</p>
              </CardHeader>
              <CardContent className="p-0 flex-1 overflow-y-auto custom-scrollbar">
                 <div className="divide-y divide-white/5">
                    {data.timeline.map((t: any) => (
                      <div key={t.id} className="p-5 hover:bg-white/5 transition-all flex flex-col gap-1">
                        <span className="text-secondary font-black text-base font-mono leading-none tracking-tighter">
                          {t.start_time} - {t.end_time}
                        </span>
                        <span className="text-white font-bold text-lg">{t.mode_name || 'بدون مایه'}</span>
                      </div>
                    ))}
                 </div>
              </CardContent>
              <div className="p-6 bg-black/10 text-center">
                 <span className="text-white/10 text-[9px] uppercase tracking-tighter">Golha Archive Engine v5.1</span>
              </div>
            </Card>
        </div>
      </div>
    </div>
  )
}
