import { createFileRoute, Link } from '@tanstack/react-router'
import { useState, useEffect } from 'react'
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"

export const Route = createFileRoute('/programs/')({
  component: ProgramsList,
})

function ProgramsList() {
  const [programs, setPrograms] = useState<any[]>([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetch(`/api/programs?search=${search}`)
      .then(res => res.json())
      .then(data => {
        setPrograms(data)
        setLoading(false)
      })
  }, [search])

  return (
    <div className="p-6 space-y-6 max-w-7xl mx-auto">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-sky-400">💎 لیست برنامه‌های گل‌ها</h1>
        <div className="w-72">
          <Input 
            placeholder="جستجو در میان برنامه ها..." 
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="bg-slate-900 border-slate-700"
          />
        </div>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {[...Array(8)].map((_, i) => (
             <div key={i} className="h-40 bg-slate-900 animate-pulse rounded-xl" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {programs.map((p) => (
            <Link key={p.id} to="/programs/$programId" params={{ programId: p.id.toString() }}>
              <Card className="hover:scale-105 transition-transform cursor-pointer bg-slate-900 border-slate-800 hover:border-sky-500 overflow-hidden">
                <CardHeader className="p-4 pb-2">
                   <div className="flex justify-between items-start">
                     <Badge variant="outline" className="text-sky-400 border-sky-400/30 font-bold">
                        {p.category_name}
                     </Badge>
                     <span className="text-slate-500 text-xs font-mono">#{p.no}</span>
                   </div>
                   <CardTitle className="text-lg mt-2 truncate text-slate-200">{p.title}</CardTitle>
                </CardHeader>
                <CardContent className="p-4 pt-0">
                  <p className="text-slate-400 text-sm truncate">شناسه یکتا: {p.id}</p>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
