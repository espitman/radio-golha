import { createFileRoute, Link } from '@tanstack/react-router'
import { useEffect, useMemo, useRef, useState } from 'react'
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination"
import {
  Check,
  ChevronsUpDown,
  ExternalLink,
  Filter,
  Hash,
  Music2,
  Search,
  SlidersHorizontal,
} from 'lucide-react'

type Category = {
  id: number
  title_fa: string
}

type Singer = {
  id: number
  name: string
}

type ProgramRow = {
  id: number
  no: string | number
  title: string
  category_name: string
}

type ProgramsResponse = {
  rows: ProgramRow[]
  categories: Category[]
  singers: Singer[]
  total: number
  page: number
  totalPages: number
  activeCategoryId: number | null
  activeSingerId: number | null
}

export const Route = createFileRoute('/programs/')({
  component: ProgramsList,
})

function ProgramsList() {
  const [data, setData] = useState<ProgramsResponse>({
    rows: [],
    categories: [],
    singers: [],
    total: 0,
    page: 1,
    totalPages: 1,
    activeCategoryId: null,
    activeSingerId: null,
  })
  const [page, setPage] = useState(1)
  const [categoryId, setCategoryId] = useState<number | null>(null)
  const [singerId, setSingerId] = useState<number | null>(null)
  const [singerQuery, setSingerQuery] = useState('')
  const [singerOpen, setSingerOpen] = useState(false)
  const [loading, setLoading] = useState(true)
  const singerBoxRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    setLoading(true)
    const params = new URLSearchParams({
      page: page.toString(),
    })
    if (categoryId) params.set('categoryId', categoryId.toString())
    if (singerId) params.set('singerId', singerId.toString())

    fetch(`/api/programs?${params.toString()}`)
      .then((res) => res.json())
      .then((response) => {
        if (response?.rows) setData(response)
        setLoading(false)
      })
      .catch(() => setLoading(false))
  }, [page, categoryId, singerId])

  const activeCategoryTitle = useMemo(() => {
    if (!categoryId) return 'همه دسته‌ها'
    return data.categories.find((item) => item.id === categoryId)?.title_fa || 'فیلتر شده'
  }, [categoryId, data.categories])

  const activeSingerTitle = useMemo(() => {
    if (!singerId) return 'همه خوانندگان'
    return data.singers.find((item) => item.id === singerId)?.name || 'فیلتر شده'
  }, [singerId, data.singers])

  const filteredSingers = useMemo(() => {
    const normalized = singerQuery.trim()
    if (!normalized) return data.singers
    return data.singers.filter((item) => item.name.includes(normalized))
  }, [data.singers, singerQuery])

  const toggleCategory = (id: number | null) => {
    setCategoryId(id)
    setPage(1)
  }

  const toggleSinger = (id: number | null) => {
    setSingerId(id)
    setPage(1)
    setSingerOpen(false)
    setSingerQuery('')
  }

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (!singerBoxRef.current?.contains(event.target as Node)) {
        setSingerOpen(false)
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

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

    if (page > 3) {
      items.push(<PaginationItem key="e1"><PaginationEllipsis /></PaginationItem>)
    }

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

    if (page < total - 2) {
      items.push(<PaginationItem key="e2"><PaginationEllipsis /></PaginationItem>)
    }

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
      <section className="relative z-20 rounded-[1.8rem] border border-primary/10 bg-white/80 p-5 shadow-[0_18px_50px_rgba(31,78,95,0.06)] backdrop-blur-md">
        <div className="flex flex-col gap-4">
          <div className="flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
            <div className="space-y-2 text-right">
              <div className="flex items-center justify-start gap-3">
                <div className="flex h-12 w-12 items-center justify-center rounded-[1.4rem] bg-primary text-white shadow-lg shadow-primary/10">
                  <Music2 className="h-6 w-6" />
                </div>
                <div>
                  <h1 className="text-2xl font-black tracking-tight text-foreground">آرشیو برنامه‌های گل‌ها</h1>
                  <p className="text-[12px] font-bold text-muted-foreground">
                    مرور سریع برنامه‌ها برای بررسی داده و ورود مستقیم به صفحه جزئیات
                  </p>
                </div>
              </div>
            </div>

            <div className="flex items-center justify-end gap-2">
              <Badge className="rounded-full border border-primary/15 bg-primary/5 px-3 py-1 text-[10px] font-black text-primary">
                {activeSingerTitle}
              </Badge>
              <Badge className="rounded-full border border-primary/15 bg-primary/5 px-3 py-1 text-[10px] font-black text-primary">
                {activeCategoryTitle}
              </Badge>
              <Badge className="rounded-full border-none bg-primary px-3 py-1 text-[10px] font-black text-white">
                {data.total} برنامه
              </Badge>
            </div>
          </div>

          <div className="relative overflow-visible rounded-[1.5rem] border border-primary/10 bg-background/70 p-4">
            <div className="space-y-4">

              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2 text-[11px] font-black text-muted-foreground">
                    <SlidersHorizontal className="h-4 w-4 text-primary/70" />
                    فیلتر برنامه‌ها
                  </div>
                  <button
                    type="button"
                    onClick={() => {
                      toggleCategory(null)
                      toggleSinger(null)
                    }}
                    className={`rounded-full px-3 py-1.5 text-[10px] font-black transition-all ${
                      categoryId === null && singerId === null
                        ? 'bg-primary text-white'
                        : 'bg-primary/5 text-primary hover:bg-primary/10'
                    }`}
                  >
                    پاک کردن فیلترها
                  </button>
                </div>

                <div className="space-y-2">
                  <div className="flex items-center justify-start gap-2 text-[10px] font-black text-muted-foreground">
                    <span>دسته‌بندی</span>
                    <Filter className="h-3.5 w-3.5 text-primary/70" />
                  </div>
                  <div className="flex flex-wrap justify-start gap-2">
                    {data.categories.map((category) => {
                      const active = categoryId === category.id
                      return (
                        <button
                          key={category.id}
                          type="button"
                          onClick={() => toggleCategory(active ? null : category.id)}
                          className={`rounded-full border px-4 py-2 text-[11px] font-black transition-all ${
                            active
                              ? 'border-primary bg-primary text-white shadow-md shadow-primary/15'
                              : 'border-primary/15 bg-white text-primary hover:border-primary/30 hover:bg-primary/5'
                          }`}
                        >
                          {category.title_fa}
                        </button>
                      )
                    })}
                  </div>
                </div>

                <div className="space-y-2">
                  <div className="flex items-center justify-start gap-2 text-[10px] font-black text-muted-foreground">
                    <span>خواننده</span>
                    <Music2 className="h-3.5 w-3.5 text-primary/70" />
                  </div>
                  <div className="relative" ref={singerBoxRef}>
                    <button
                      type="button"
                      onClick={() => setSingerOpen((open) => !open)}
                      className="flex h-12 w-full items-center justify-between rounded-2xl border border-primary/10 bg-white px-4 text-sm font-bold text-foreground shadow-none transition-colors hover:border-primary/20"
                    >
                      <span className="truncate">{activeSingerTitle}</span>
                      <ChevronsUpDown className="h-4 w-4 text-muted-foreground" />
                    </button>

                    {singerOpen && (
                      <div className="absolute z-20 mt-2 w-full overflow-hidden rounded-[1.3rem] border border-primary/10 bg-white shadow-[0_18px_45px_rgba(31,78,95,0.12)]">
                        <div className="border-b border-primary/8 p-3">
                          <div className="relative">
                            <Search className="absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                            <Input
                              value={singerQuery}
                              onChange={(e) => setSingerQuery(e.target.value)}
                              placeholder="جستجوی خواننده..."
                              className="h-10 rounded-xl border-primary/10 bg-background pr-10 text-sm font-bold shadow-none focus-visible:ring-primary/15"
                            />
                          </div>
                        </div>

                        <div className="max-h-64 overflow-y-auto p-2 custom-scrollbar">
                          <button
                            type="button"
                            onClick={() => toggleSinger(null)}
                            className={`flex w-full items-center justify-between rounded-xl px-3 py-2.5 text-right text-sm font-bold transition-colors ${
                              singerId === null ? 'bg-primary text-white' : 'text-foreground hover:bg-primary/5'
                            }`}
                          >
                            <span>همه خوانندگان</span>
                            <Check className={`h-4 w-4 ${singerId === null ? 'opacity-100' : 'opacity-0'}`} />
                          </button>

                          {filteredSingers.length === 0 ? (
                            <div className="px-3 py-4 text-center text-sm font-bold text-muted-foreground">
                              نتیجه‌ای پیدا نشد.
                            </div>
                          ) : (
                            filteredSingers.map((singer) => {
                              const active = singerId === singer.id
                              return (
                                <button
                                  key={singer.id}
                                  type="button"
                                  onClick={() => toggleSinger(active ? null : singer.id)}
                                  className={`flex w-full items-center justify-between rounded-xl px-3 py-2.5 text-right text-sm font-bold transition-colors ${
                                    active ? 'bg-primary text-white' : 'text-foreground hover:bg-primary/5'
                                  }`}
                                >
                                  <span>{singer.name}</span>
                                  <Check className={`h-4 w-4 ${active ? 'opacity-100' : 'opacity-0'}`} />
                                </button>
                              )
                            })
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="relative z-10 overflow-hidden rounded-[1.8rem] border border-primary/10 bg-white/85 shadow-[0_18px_45px_rgba(31,78,95,0.06)] backdrop-blur-md">
        <div className="flex items-center justify-between border-b border-primary/8 px-5 py-4">
          <div className="flex items-center gap-2 text-sm font-black text-primary">
            <Filter className="h-4 w-4" />
            <span>لیست برنامه‌ها</span>
          </div>
          <div className="text-right">
            <div className="text-[11px] font-bold text-muted-foreground">فهرست برنامه‌ها با دسترسی مستقیم به صفحه جزئیات</div>
          </div>
        </div>

        <div className="w-full overflow-x-auto">
          <table className="w-full border-collapse text-right">
            <thead className="bg-primary/[0.03]">
              <tr>
                <th className="px-5 py-4 text-right text-[11px] font-black text-primary"><Hash className="h-3.5 w-3.5" /></th>
                <th className="px-5 py-4 text-right text-[11px] font-black text-primary">شماره برنامه</th>
                <th className="px-5 py-4 text-right text-[11px] font-black text-primary">عنوان برنامه</th>
                <th className="px-5 py-4 text-right text-[11px] font-black text-primary">دسته</th>
                <th className="px-5 py-4 text-center text-[11px] font-black text-primary">مشاهده</th>
              </tr>
            </thead>

            <tbody className="divide-y divide-primary/8">
              {loading ? (
                [...Array(10)].map((_, index) => (
                  <tr key={index} className="animate-pulse">
                    <td colSpan={5} className="h-16 bg-white/40" />
                  </tr>
                ))
              ) : data.rows.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-20 text-center">
                    <div className="space-y-2">
                      <div className="text-lg font-black text-primary">برنامه‌ای پیدا نشد</div>
                      <p className="text-sm font-bold text-muted-foreground">عبارت جستجو یا فیلتر را تغییر بده.</p>
                    </div>
                  </td>
                </tr>
              ) : (
                data.rows.map((program) => (
                  <tr key={program.id} className="group transition-colors hover:bg-primary/[0.035]">
                    <td className="px-5 py-4 font-mono text-[10px] font-black text-muted-foreground">{program.id}</td>
                    <td className="px-5 py-4">
                      <span className="inline-flex rounded-xl border border-primary/10 bg-primary/5 px-3 py-1.5 text-[10px] font-black text-primary">
                        {program.no}
                      </span>
                    </td>
                    <td className="px-5 py-4">
                      <div className="text-sm font-black text-foreground transition-colors group-hover:text-primary">
                        {program.title}
                      </div>
                    </td>
                    <td className="px-5 py-4">
                      <Badge variant="outline" className="rounded-full border-primary/20 bg-secondary/10 px-3 py-1 text-[10px] font-black text-primary">
                        {program.category_name}
                      </Badge>
                    </td>
                    <td className="px-5 py-4 text-center">
                      <Link to="/programs/$programId" params={{ programId: program.id.toString() }}>
                        <button className="inline-flex h-10 w-10 items-center justify-center rounded-2xl border border-primary/10 bg-primary/5 text-primary transition-all hover:bg-primary hover:text-white hover:shadow-lg hover:shadow-primary/15">
                          <ExternalLink className="h-4 w-4" />
                        </button>
                      </Link>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>

      <div className="flex flex-col gap-4 rounded-[1.4rem] border border-border/40 bg-card/50 p-4 backdrop-blur-md md:flex-row md:items-center md:justify-between">
        <Pagination className="justify-start md:order-1 order-2">
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious
                size="default"
                onClick={() => setPage((current) => Math.max(1, current - 1))}
                className={`cursor-pointer ${page === 1 ? 'pointer-events-none opacity-30 shadow-none' : ''}`}
              />
            </PaginationItem>

            {renderPagination()}

            <PaginationItem>
              <PaginationNext
                size="default"
                onClick={() => setPage((current) => Math.min(data.totalPages, current + 1))}
                className={`cursor-pointer ${page === data.totalPages ? 'pointer-events-none opacity-30 shadow-none' : ''}`}
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>

        <div className="flex flex-col items-end gap-1 md:order-2 order-1 w-full">
          <span className="text-[11px] font-bold text-muted-foreground">
            صفحه {data.page} از {data.totalPages}
          </span>
        </div>
      </div>
    </div>
  )
}
