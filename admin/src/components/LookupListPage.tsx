import { useEffect, useState } from 'react'
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination"
import { Hash, Search } from 'lucide-react'

type LookupRow = {
  id: number
  name: string
  usage_count: number
}

type LookupResponse = {
  rows: LookupRow[]
  stats: {
    total_items: number
    total_usage: number
  }
  total: number
  page: number
  totalPages: number
}

type LookupListPageProps = {
  endpoint: '/api/orchestras' | '/api/instruments' | '/api/modes'
  title: string
  description: string
  singularLabel: string
  usageLabel: string
}

export function LookupListPage({
  endpoint,
  title,
  description,
  singularLabel,
  usageLabel,
}: LookupListPageProps) {
  const [data, setData] = useState<LookupResponse>({
    rows: [],
    stats: {
      total_items: 0,
      total_usage: 0,
    },
    total: 0,
    page: 1,
    totalPages: 1,
  })
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(1)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    const params = new URLSearchParams({
      search,
      page: page.toString(),
    })

    fetch(`${endpoint}?${params.toString()}`)
      .then((res) => res.json())
      .then((response) => {
        if (response?.rows) setData(response)
        setLoading(false)
      })
      .catch(() => setLoading(false))
  }, [endpoint, page, search])

  const renderPagination = () => {
    const items = []
    const total = data.totalPages || 1

    items.push(
      <PaginationItem key="first">
        <PaginationLink size="default" onClick={() => setPage(1)} isActive={page === 1} className="cursor-pointer">
          1
        </PaginationLink>
      </PaginationItem>
    )

    if (page > 3) items.push(<PaginationItem key="e1"><PaginationEllipsis /></PaginationItem>)

    for (let i = Math.max(2, page - 1); i <= Math.min(total - 1, page + 1); i++) {
      if (i === 1 || i === total) continue
      items.push(
        <PaginationItem key={i}>
          <PaginationLink size="default" onClick={() => setPage(i)} isActive={page === i} className="cursor-pointer">
            {i}
          </PaginationLink>
        </PaginationItem>
      )
    }

    if (page < total - 2) items.push(<PaginationItem key="e2"><PaginationEllipsis /></PaginationItem>)

    if (total > 1) {
      items.push(
        <PaginationItem key="last">
          <PaginationLink size="default" onClick={() => setPage(total)} isActive={page === total} className="cursor-pointer">
            {total}
          </PaginationLink>
        </PaginationItem>
      )
    }

    return items
  }

  return (
    <div className="space-y-4 animate-in" dir="rtl">
      <section className="rounded-[1.8rem] border border-primary/10 bg-white/80 p-5 shadow-[0_18px_50px_rgba(31,78,95,0.06)] backdrop-blur-md">
        <div className="flex flex-col gap-4">
          <div className="flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
            <div className="space-y-2 text-right">
              <h1 className="text-2xl font-black tracking-tight text-foreground">{title}</h1>
              <p className="text-[12px] font-bold text-muted-foreground">{description}</p>
            </div>

            <div className="flex items-center justify-end gap-2">
              <Badge className="rounded-full border border-primary/15 bg-primary/5 px-3 py-1 text-[10px] font-black text-primary">
                {data.stats.total_usage} مورد استفاده
              </Badge>
              <Badge className="rounded-full border-none bg-primary px-3 py-1 text-[10px] font-black text-white">
                {data.total} {singularLabel}
              </Badge>
            </div>
          </div>

          <div className="rounded-[1.5rem] border border-primary/10 bg-background/70 p-4">
            <div className="relative max-w-md">
              <Search className="absolute right-4 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder={`جستجوی ${singularLabel}...`}
                value={search}
                onChange={(e) => {
                  setSearch(e.target.value)
                  setPage(1)
                }}
                className="h-12 rounded-2xl border-primary/10 bg-white pr-11 text-sm font-bold shadow-none focus-visible:ring-primary/15"
              />
            </div>
          </div>
        </div>
      </section>

      <section className="overflow-hidden rounded-[1.8rem] border border-primary/10 bg-white/85 shadow-[0_18px_45px_rgba(31,78,95,0.06)] backdrop-blur-md">
        <div className="flex items-center justify-between border-b border-primary/8 px-5 py-4">
          <div className="text-right">
            <div className="text-[11px] font-bold text-muted-foreground">فهرست کامل {singularLabel}ها در آرشیو</div>
          </div>
          <div className="flex items-center gap-2 text-sm font-black text-primary">
            <span>{title}</span>
          </div>
        </div>

        <div className="w-full overflow-x-auto">
          <table className="w-full border-collapse text-right">
            <thead className="bg-primary/[0.03]">
              <tr>
                <th className="px-5 py-4 text-[11px] font-black text-primary"><Hash className="h-3.5 w-3.5" /></th>
                <th className="px-5 py-4 text-[11px] font-black text-primary">نام</th>
                <th className="px-5 py-4 text-center text-[11px] font-black text-primary">{usageLabel}</th>
              </tr>
            </thead>

            <tbody className="divide-y divide-primary/8">
              {loading ? (
                [...Array(10)].map((_, index) => (
                  <tr key={index} className="animate-pulse">
                    <td colSpan={3} className="h-16 bg-white/40" />
                  </tr>
                ))
              ) : data.rows.length === 0 ? (
                <tr>
                  <td colSpan={3} className="px-6 py-20 text-center">
                    <div className="space-y-2">
                      <div className="text-lg font-black text-primary">{singularLabel}ی پیدا نشد</div>
                      <p className="text-sm font-bold text-muted-foreground">عبارت جستجو را تغییر بده.</p>
                    </div>
                  </td>
                </tr>
              ) : (
                data.rows.map((row) => (
                  <tr key={row.id} className="group transition-colors hover:bg-primary/[0.035]">
                    <td className="px-5 py-4">
                      <span className="inline-flex rounded-xl border border-primary/10 bg-primary/5 px-3 py-1.5 text-[10px] font-mono font-black text-primary">
                        {row.id}
                      </span>
                    </td>
                    <td className="px-5 py-4">
                      <div className="text-sm font-black text-foreground transition-colors group-hover:text-primary">
                        {row.name}
                      </div>
                    </td>
                    <td className="px-5 py-4 text-center">
                      <Badge variant="outline" className="rounded-full border-primary/20 bg-secondary/10 px-3 py-1 text-[10px] font-black text-primary">
                        {row.usage_count}
                      </Badge>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>

      <div className="flex flex-col gap-4 rounded-[1.4rem] border border-border/40 bg-card/50 p-4 backdrop-blur-md md:flex-row md:items-center md:justify-between">
        <Pagination className="justify-start">
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious
                onClick={() => setPage((current) => Math.max(1, current - 1))}
                className={`cursor-pointer ${page === 1 ? 'pointer-events-none opacity-30' : ''}`}
              />
            </PaginationItem>

            {renderPagination()}

            <PaginationItem>
              <PaginationNext
                onClick={() => setPage((current) => Math.min(data.totalPages, current + 1))}
                className={`cursor-pointer ${page === data.totalPages ? 'pointer-events-none opacity-30' : ''}`}
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>

        <div className="flex flex-col items-end gap-1 shrink-0">
          <span className="text-[10px] font-black uppercase tracking-[0.3em] text-primary/45">
            {data.total} TOTAL {singularLabel.toUpperCase()}
          </span>
          <span className="text-[11px] font-bold text-muted-foreground">
            صفحه {data.page} از {data.totalPages}
          </span>
        </div>
      </div>
    </div>
  )
}
