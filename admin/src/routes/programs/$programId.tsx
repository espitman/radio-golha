import { createFileRoute, Link } from '@tanstack/react-router'
import { useState, useEffect } from 'react'
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { Card, CardHeader, CardContent, CardTitle } from "@/components/ui/card"
import { Music, Mic, BookOpen } from "lucide-react"

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

  if (loading) return <div className="p-10 animate-pulse text-sky-400">Loading program details...</div>
  if (!data) return <div className="p-10 text-red-500">Program not found!</div>

  return (
    <div className="p-6 space-y-8 max-w-6xl mx-auto">
      {/* Header Section */}
      <div className="flex flex-col md:flex-row justify-between items-start gap-4">
        <div className="space-y-2">
          <Link to="/programs" className="text-slate-500 hover:text-sky-400 text-sm">← بازگشت به لیست</Link>
          <div className="flex gap-2 items-center">
            <h1 className="text-4xl font-black text-slate-100">{data.title}</h1>
            <Badge className="bg-sky-500/10 text-sky-400 border-sky-400/50 mt-1">{data.category_name}</Badge>
          </div>
          <p className="text-slate-500 font-mono">شماره برنامه: {data.no}</p>
        </div>
        
        {data.audio_url && (
          <audio controls src={data.audio_url} className="w-full md:w-80 h-10 rounded-full bg-slate-200" />
        )}
      </div>

      <Separator className="bg-slate-800" />

      {/* Main Grid: Metadata & Performers */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Left Side: Summary Info */}
        <div className="md:col-span-2 space-y-6">
          <Card className="bg-slate-900 border-slate-800 shadow-2xl overflow-hidden">
            <CardHeader className="border-b border-slate-800 bg-slate-900/50">
              <CardTitle className="text-sky-400 flex items-center gap-2"><Music className="w-5 h-5"/> هنرمندان و عوامل اصلی</CardTitle>
            </CardHeader>
            <CardContent className="p-0">
               <div className="divide-y divide-slate-800/80">
                  {/* Singers */}
                  <div className="p-4 bg-slate-900/40">
                    <p className="text-xs text-slate-500 flex items-center gap-1 mb-2"><Mic className="w-3 h-3"/> خوانندگان</p>
                    <div className="flex flex-wrap gap-2">
                      {data.singers.map((s: string) => <Badge key={s} className="bg-slate-800 text-slate-200">{s}</Badge>)}
                    </div>
                  </div>
                  {/* Performers */}
                  <div className="p-4">
                    <p className="text-xs text-slate-500 flex items-center gap-1 mb-2"><Music className="w-3 h-3"/> نوازندگان</p>
                    <div className="grid grid-cols-2 gap-4">
                      {data.performers.map((p: any) => (
                        <div key={p.name} className="flex flex-col">
                          <span className="text-slate-300 font-bold">{p.name}</span>
                          <span className="text-slate-500 text-xs italic">{p.instrument || 'نوازنده'}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                  {/* Poets */}
                  <div className="p-4 bg-slate-900/40">
                    <p className="text-xs text-slate-500 flex items-center gap-1 mb-2"><BookOpen className="w-3 h-3"/> شاعران</p>
                    <div className="flex flex-wrap gap-2">
                      {data.poets.map((s: string) => <Badge key={s} className="bg-sky-500/10 text-sky-400 border-sky-400/20">{s}</Badge>)}
                    </div>
                  </div>
               </div>
            </CardContent>
          </Card>
        </div>

        {/* Right Side: Timeline Table */}
        <div className="space-y-6">
           <Card className="bg-slate-900 border-slate-800 h-full">
            <CardHeader className="border-b border-slate-800">
              <CardTitle className="text-sky-400 flex items-center gap-2 text-md">⏱️ تایم لاین اجرایی</CardTitle>
            </CardHeader>
            <CardContent className="p-0 max-h-[500px] overflow-y-auto font-mono text-center">
               <table className="w-full text-xs">
                 <thead>
                    <tr className="border-b border-slate-800 bg-slate-950 text-slate-500">
                      <th className="p-2">زمان</th>
                      <th className="p-2">مایه/دستگاه</th>
                    </tr>
                 </thead>
                 <tbody className="divide-y divide-slate-800">
                    {data.timeline.map((t: any) => (
                      <tr key={t.id} className="hover:bg-sky-400/5 transition-colors">
                        <td className="p-2 text-sky-400 font-bold">{t.start_time} - {t.end_time}</td>
                        <td className="p-2 text-slate-400">{t.mode_name || 'بدون مایه'}</td>
                      </tr>
                    ))}
                 </tbody>
               </table>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
