import { createFileRoute } from '@tanstack/react-router'
import { useEffect, useMemo, useState } from 'react'
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
import {
  Mic,
  Music2,
  PenTool,
  Radio,
  Search,
  Shapes,
  Sparkles,
  UserRound,
  Users,
} from 'lucide-react'

type ArtistRow = {
  id: number
  name: string
  is_singer: number
  is_performer: number
  is_poet: number
  is_announcer: number
  is_composer: number
  is_arranger: number
}

type ArtistsResponse = {
  rows: ArtistRow[]
  stats: {
    total_artists: number
    singers: number
    performers: number
    poets: number
  }
  total: number
  page: number
  totalPages: number
  activeRole: string | null
}

const ROLE_OPTIONS = [
  { id: null, label: 'همه نقش‌ها', icon: Shapes },
  { id: 'singer', label: 'خواننده', icon: Mic },
  { id: 'performer', label: 'نوازنده', icon: Music2 },
  { id: 'poet', label: 'شاعر', icon: PenTool },
  { id: 'announcer', label: 'گوینده', icon: Radio },
  { id: 'composer', label: 'آهنگساز', icon: Sparkles },
  { id: 'arranger', label: 'تنظیم‌کننده', icon: UserRound },
]

export const Route = createFileRoute('/artists/')({
  component: ArtistsList,
})

function roleBadges(artist: ArtistRow) {
  const badges = []
  if (artist.is_singer) badges.push('خواننده')
  if (artist.is_performer) badges.push('نوازنده')
  if (artist.is_poet) badges.push('شاعر')
  if (artist.is_announcer) badges.push('گوینده')
  if (artist.is_composer) badges.push('آهنگساز')
  if (artist.is_arranger) badges.push('تنظیم‌کننده')
  return badges
}

function ArtistsList() {
  const [data, setData] = useState<ArtistsResponse>({
    rows: [],
    stats: {
      total_artists: 0,
      singers: 0,
      performers: 0,
      poets: 0,
    },
    total: 0,
    page: 1,
    totalPages: 1,
    activeRole: null,
  })
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(1)
  const [role, setRole] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    const params = new URLSearchParams({
      search,
      page: page.toString(),
    })
    if (role) params.set('role', role)

    fetch(`/api/artists?${params.toString()}`)
      .then((res) => res.json())
      .then((response) => {
        if (response?.rows) setData(response)
        setLoading(false)
      })
      .catch(() => setLoading(false))
  }, [search, page, role])

  const activeRoleLabel = useMemo(() => {
    return ROLE_OPTIONS.find((item) => item.id === role)?.label || 'همه نقش‌ها'
  }, [role])

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
              <div className="flex items-center justify-end gap-3">
                <div className="flex h-12 w-12 items-center justify-center rounded-[1.4rem] bg-primary text-white shadow-lg shadow-primary/10">
                  <Users className="h-6 w-6" />
                </div>
                <div>
                  <h1 className="text-2xl font-black tracking-tight text-foreground">آرشیو هنرمندان</h1>
                  <p className="text-[12px] font-bold text-muted-foreground">
                    مرور خوانندگان، نوازندگان، شاعران و دیگر نقش‌های ثبت‌شده در آرشیو
                  </p>
                </div>
              </div>
            </div>

            <div className="flex items-center justify-end gap-2">
              <Badge className="rounded-full border border-primary/15 bg-primary/5 px-3 py-1 text-[10px] font-black text-primary">
                {activeRoleLabel}
              </Badge>
              <Badge className="rounded-full border-none bg-primary px-3 py-1 text-[10px] font-black text-white">
                {data.total} هنرمند
              </Badge>
            </div>
          </div>

          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            <div className="rounded-[1.3rem] border border-primary/10 bg-background/70 p-4 text-right">
              <div className="mb-2 text-[10px] font-black text-muted-foreground">کل هنرمندان</div>
              <div className="text-2xl font-black text-primary">{data.stats.total_artists}</div>
            </div>
            <div className="rounded-[1.3rem] border border-primary/10 bg-background/70 p-4 text-right">
              <div className="mb-2 text-[10px] font-black text-muted-foreground">خوانندگان</div>
              <div className="text-2xl font-black text-primary">{data.stats.singers}</div>
            </div>
            <div className="rounded-[1.3rem] border border-primary/10 bg-background/70 p-4 text-right">
              <div className="mb-2 text-[10px] font-black text-muted-foreground">نوازندگان</div>
              <div className="text-2xl font-black text-primary">{data.stats.performers}</div>
            </div>
            <div className="rounded-[1.3rem] border border-primary/10 bg-background/70 p-4 text-right">
              <div className="mb-2 text-[10px] font-black text-muted-foreground">شاعران</div>
              <div className="text-2xl font-black text-primary">{data.stats.poets}</div>
            </div>
          </div>

          <div className="rounded-[1.5rem] border border-primary/10 bg-background/70 p-4">
            <div className="grid gap-4 xl:grid-cols-[minmax(0,340px)_1fr] xl:items-start">
              <div className="relative">
                <Search className="absolute right-4 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="جستجوی نام هنرمند..."
                  value={search}
                  onChange={(e) => {
                    setSearch(e.target.value)
                    setPage(1)
                  }}
                  className="h-12 rounded-2xl border-primary/10 bg-white pr-11 text-sm font-bold shadow-none focus-visible:ring-primary/15"
                />
              </div>

              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <button
                    type="button"
                    onClick={() => {
                      setRole(null)
                      setPage(1)
                    }}
                    className={`rounded-full px-3 py-1.5 text-[10px] font-black transition-all ${
                      role === null ? 'bg-primary text-white' : 'bg-primary/5 text-primary hover:bg-primary/10'
                    }`}
                  >
                    پاک کردن فیلتر
                  </button>
                  <div className="flex items-center gap-2 text-[11px] font-black text-muted-foreground">
                    <Shapes className="h-4 w-4 text-primary/70" />
                    فیلتر نقش هنرمندان
                  </div>
                </div>

                <div className="flex flex-wrap justify-end gap-2">
                  {ROLE_OPTIONS.map((item) => {
                    const active = role === item.id
                    return (
                      <button
                        key={item.label}
                        type="button"
                        onClick={() => {
                          setRole(active ? null : item.id)
                          setPage(1)
                        }}
                        className={`inline-flex items-center gap-2 rounded-full border px-4 py-2 text-[11px] font-black transition-all ${
                          active
                            ? 'border-primary bg-primary text-white shadow-md shadow-primary/15'
                            : 'border-primary/15 bg-white text-primary hover:border-primary/30 hover:bg-primary/5'
                        }`}
                      >
                        <item.icon className="h-3.5 w-3.5" />
                        {item.label}
                      </button>
                    )
                  })}
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="overflow-hidden rounded-[1.8rem] border border-primary/10 bg-white/85 shadow-[0_18px_45px_rgba(31,78,95,0.06)] backdrop-blur-md">
        <div className="flex items-center justify-between border-b border-primary/8 px-5 py-4">
          <div className="flex items-center gap-2 text-sm font-black text-primary">
            <Users className="h-4 w-4" />
            <span>لیست هنرمندان</span>
          </div>
          <div className="text-right">
            <div className="text-[11px] font-bold text-muted-foreground">هنرمندان ثبت‌شده همراه با نقش‌های آن‌ها در آرشیو</div>
          </div>
        </div>

        <div className="w-full overflow-x-auto">
          <table className="w-full border-collapse text-right">
            <thead className="bg-primary/[0.03]">
              <tr>
                <th className="px-5 py-4 text-[11px] font-black text-primary">شناسه</th>
                <th className="px-5 py-4 text-[11px] font-black text-primary">نام هنرمند</th>
                <th className="px-5 py-4 text-center text-[11px] font-black text-primary">نقش‌ها</th>
              </tr>
            </thead>

            <tbody className="divide-y divide-primary/8">
              {loading ? (
                [...Array(12)].map((_, index) => (
                  <tr key={index} className="animate-pulse">
                    <td colSpan={3} className="h-16 bg-white/40" />
                  </tr>
                ))
              ) : data.rows.length === 0 ? (
                <tr>
                  <td colSpan={3} className="px-6 py-20 text-center">
                    <div className="space-y-2">
                      <div className="text-lg font-black text-primary">هنرمندی پیدا نشد</div>
                      <p className="text-sm font-bold text-muted-foreground">عبارت جستجو یا فیلتر نقش را تغییر بده.</p>
                    </div>
                  </td>
                </tr>
              ) : (
                data.rows.map((artist) => (
                  <tr key={artist.id} className="group transition-colors hover:bg-primary/[0.035]">
                    <td className="px-5 py-4">
                      <span className="inline-flex rounded-xl border border-primary/10 bg-primary/5 px-3 py-1.5 text-[10px] font-mono font-black text-primary">
                        {artist.id}
                      </span>
                    </td>
                    <td className="px-5 py-4">
                      <div className="text-sm font-black text-foreground transition-colors group-hover:text-primary">
                        {artist.name}
                      </div>
                    </td>
                    <td className="px-5 py-4">
                      <div className="flex flex-wrap justify-center gap-2">
                        {roleBadges(artist).map((label) => (
                          <Badge
                            key={`${artist.id}-${label}`}
                            variant="outline"
                            className="rounded-full border-primary/20 bg-white px-3 py-1 text-[10px] font-black text-primary"
                          >
                            {label}
                          </Badge>
                        ))}
                      </div>
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
            {data.total} TOTAL ARTISTS FOUND
          </span>
          <span className="text-[11px] font-bold text-muted-foreground">
            صفحه {data.page} از {data.totalPages}
          </span>
        </div>
      </div>
    </div>
  )
}
