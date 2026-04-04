import { createFileRoute, Link } from '@tanstack/react-router'
import { useState, useEffect } from 'react'
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Search, Music, ExternalLink, Hash, Filter } from 'lucide-react'
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination"

export const Route = createFileRoute('/programs/')({
  component: ProgramsList,
})

function ProgramsList() {
  const [data, setData] = useState<{ rows: any[], total: number, page: number, totalPages: number }>({
    rows: [], total: 0, page: 1, totalPages: 1
  })
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(1)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    fetch(`/api/programs?search=${encodeURIComponent(search)}&page=${page}`)
      .then(res => res.json())
      .then(d => {
        if (d && d.rows) setData(d)
        setLoading(false)
      })
      .catch(() => setLoading(false))
  }, [search, page])

  const handleSearchChange = (val: string) => {
    setSearch(val)
    setPage(1)
  }

  // Generate pagination items
  const renderPagination = () => {
    const items = []
    const total = data.totalPages || 1
    
    // Always show first
    items.push(
      <PaginationItem key="first">
        <PaginationLink onClick={() => setPage(1)} isActive={page === 1} className="cursor-pointer">1</PaginationLink>
      </PaginationItem>
    )

    if (page > 3) {
      items.push(<PaginationItem key="e1"><PaginationEllipsis /></PaginationItem>)
    }

    // Neighbors
    for (let i = Math.max(2, page - 1); i <= Math.min(total - 1, page + 1); i++) {
        if (i === 1 || i === total) continue;
        items.push(
          <PaginationItem key={i}>
            <PaginationLink onClick={() => setPage(i)} isActive={page === i} className="cursor-pointer">{i}</PaginationLink>
          </PaginationItem>
        )
    }

    if (page < total - 2) {
      items.push(<PaginationItem key="e2"><PaginationEllipsis /></PaginationItem>)
    }

    // Always show last if > 1
    if (total > 1) {
      items.push(
        <PaginationItem key="last">
          <PaginationLink onClick={() => setPage(total)} isActive={page === total} className="cursor-pointer">{total}</PaginationLink>
        </PaginationItem>
      )
    }

    return items
  }

  return (
    <div className="p-0 space-y-4 md:space-y-6 animate-in">
      {/* Header Container */}
      <div className="flex flex-col md:flex-row justify-between items-center gap-4 bg-card/60 p-5 rounded-[1.5rem] border border-border/40 shadow-sm backdrop-blur-md">
        <div className="space-y-0.5 text-center md:text-right">
          <h1 className="text-xl font-black text-foreground tracking-tight flex items-center gap-2">
             <Music className="w-6 h-6 text-primary" />
             آرشیو برنامه‌های گل‌ها
          </h1>
          <p className="text-muted-foreground text-[10px] font-medium tracking-tight italic">مدیریت و فیلترینگ هوشمند ۱۴۴۰ قطعه ماندگار موسیقی ایران</p>
        </div>
        
        <div className="relative w-full md:w-80 group">
          <Search className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground group-focus-within:text-primary transition-colors" />
          <Input 
            placeholder="جستجوی نام یا شماره برنامه..." 
            value={search}
            onChange={(e) => handleSearchChange(e.target.value)}
            className="h-10 pr-10 rounded-xl bg-background/80 border-border/60 focus:ring-primary/20 transition-all font-medium text-xs"
          />
        </div>
      </div>

      {/* Table Container */}
      <div className="rounded-[1.5rem] border border-primary/20 bg-card/40 shadow-2xl shadow-primary/5 overflow-hidden backdrop-blur-xl">
        <div className="w-full overflow-x-auto min-h-[450px]">
          <table className="w-full text-right border-collapse">
            <thead className="bg-primary/5 border-b border-primary/20">
              <tr>
                <th className="p-3 text-primary font-black text-[11px] w-[70px]"><Hash className="w-3 h-3"/></th>
                <th className="p-3 text-primary font-black text-[11px] w-[110px]">شماره برنامه</th>
                <th className="p-3 text-primary font-black text-[11px]">عنوان کامل برنامه</th>
                <th className="p-3 text-primary font-black text-[11px] w-[160px]"><Filter className="w-3 h-3 inline-block ml-1"/> دسته</th>
                <th className="p-3 text-center text-primary font-black text-[11px] w-[90px]">عملیات</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-primary/20">
              {loading ? (
                 [...Array(10)].map((_, i) => (
                    <tr key={i} className="animate-pulse bg-white/5 h-12">
                      <td colSpan={5}></td>
                    </tr>
                 ))
              ) : data.rows.length === 0 ? (
                <tr>
                   <td colSpan={5} className="p-20 text-center text-muted-foreground italic font-medium">هیچ داده‌ای یافت نشد.</td>
                </tr>
              ) : (
                data.rows.map((p) => (
                  <tr key={p.id} className="group border-border/10 hover:bg-primary/5 transition-colors duration-200">
                    <td className="p-3 font-mono text-[10px] text-muted-foreground">{p.id}</td>
                    <td className="p-3">
                      <span className="bg-primary/5 text-primary px-2.5 py-1 rounded-lg border border-primary/10 font-black text-[10px] font-mono">
                        {p.no}
                      </span>
                    </td>
                    <td className="p-3 font-bold text-foreground text-sm group-hover:text-primary transition-colors">{p.title}</td>
                    <td className="p-3">
                      <Badge variant="outline" className="bg-secondary/10 text-primary border-primary/20 px-2.5 py-0.5 rounded-full text-[9px] font-black tracking-tight">
                        {p.category_name}
                      </Badge>
                    </td>
                    <td className="p-3 text-center">
                      <Link to="/programs/$programId" params={{ programId: p.id.toString() }}>
                        <button className="p-1.5 rounded-lg bg-primary/5 border border-primary/10 text-primary hover:bg-primary hover:text-white transition-all">
                          <ExternalLink className="w-3.5 h-3.5" />
                        </button>
                      </Link>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Shadcn Pagination Implementation */}
      <div className="p-4 flex flex-col md:flex-row justify-between items-center gap-6 bg-card/40 rounded-[1.5rem] border border-border/40 backdrop-blur-md">
        <Pagination className="justify-start">
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious 
                onClick={() => setPage(p => Math.max(1, p - 1))} 
                className={`cursor-pointer ${page === 1 ? 'opacity-30 pointer-events-none' : ''}`}
              />
            </PaginationItem>
            
            {renderPagination()}

            <PaginationItem>
              <PaginationNext 
                onClick={() => setPage(p => Math.min(data.totalPages, p + 1))} 
                className={`cursor-pointer ${page === data.totalPages ? 'opacity-30 pointer-events-none' : ''}`}
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>
        
        <div className="flex flex-col items-end gap-1 shrink-0">
           <span className="text-[9px] font-black uppercase tracking-widest text-primary/60">
              {data.total || 0} TOTAL PROGRAMMES FOUND
           </span>
           <span className="text-[8px] uppercase font-mono tracking-tighter text-muted-foreground/40 italic">Golha Archive Engine v5.1</span>
        </div>
      </div>
    </div>
  )
}
