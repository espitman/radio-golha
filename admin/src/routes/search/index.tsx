import { createFileRoute, Link, useNavigate } from '@tanstack/react-router'
import { useDeferredValue, useEffect, useMemo, useRef, useState } from 'react'
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
  Check,
  ChevronDown,
  ChevronUp,
  ChevronsUpDown,
  ExternalLink,
  Hash,
  LibraryBig,
  Mic2,
  Search,
  Waves,
  X,
} from 'lucide-react'

type CategoryOption = {
  id: number
  title_fa: string
}

type SearchOption = {
  id: number
  name: string
}

type SearchOptionsResponse = {
  categories: CategoryOption[]
  singers: SearchOption[]
  poets: SearchOption[]
  announcers: SearchOption[]
  composers: SearchOption[]
  arrangers: SearchOption[]
  performers: SearchOption[]
  orchestraLeaders: SearchOption[]
  modes: SearchOption[]
  orchestras: SearchOption[]
  instruments: SearchOption[]
}

type ProgramRow = {
  id: number
  no: string | number
  sub_no?: string | null
  title: string
  category_name: string
}

type SearchResultsResponse = {
  rows: ProgramRow[]
  total: number
  page: number
  totalPages: number
}

type MatchMode = 'any' | 'all'
type SortField = 'id' | 'no' | 'sub_no' | 'title' | 'category_name'
type SortDirection = 'asc' | 'desc'
type SearchPageQuery = {
  page?: number
  sortField?: SortField
  sortDirection?: SortDirection
  transcriptQuery?: string
  categoryIds?: string
  modeIds?: string
  modeMatch?: MatchMode
  orchestraIds?: string
  orchestraMatch?: MatchMode
  instrumentIds?: string
  instrumentMatch?: MatchMode
  singerIds?: string
  singerMatch?: MatchMode
  poetIds?: string
  poetMatch?: MatchMode
  announcerIds?: string
  announcerMatch?: MatchMode
  composerIds?: string
  composerMatch?: MatchMode
  arrangerIds?: string
  arrangerMatch?: MatchMode
  performerIds?: string
  performerMatch?: MatchMode
  orchestraLeaderIds?: string
  orchestraLeaderMatch?: MatchMode
}

type MultiSelectProps = {
  label: string
  placeholder: string
  options: SearchOption[]
  selectedIds: number[]
  matchMode?: MatchMode
  onMatchModeChange?(mode: MatchMode): void
  loading?: boolean
  disabled?: boolean
  onChange(ids: number[]): void
}

const EMPTY_OPTIONS: SearchOptionsResponse = {
  categories: [],
  singers: [],
  poets: [],
  announcers: [],
  composers: [],
  arrangers: [],
  performers: [],
  orchestraLeaders: [],
  modes: [],
  orchestras: [],
  instruments: [],
}

const EMPTY_RESULTS: SearchResultsResponse = {
  rows: [],
  total: 0,
  page: 1,
  totalPages: 1,
}

export const Route = createFileRoute('/search/')({
  validateSearch: (search: Record<string, unknown>): Required<SearchPageQuery> => ({
    page: parsePage(search.page),
    sortField: parseSortField(search.sortField),
    sortDirection: parseSortDirection(search.sortDirection),
    transcriptQuery: parseString(search.transcriptQuery),
    categoryIds: parseIdsParam(search.categoryIds),
    modeIds: parseIdsParam(search.modeIds),
    modeMatch: parseMatchMode(search.modeMatch),
    orchestraIds: parseIdsParam(search.orchestraIds),
    orchestraMatch: parseMatchMode(search.orchestraMatch),
    instrumentIds: parseIdsParam(search.instrumentIds),
    instrumentMatch: parseMatchMode(search.instrumentMatch),
    singerIds: parseIdsParam(search.singerIds),
    singerMatch: parseMatchMode(search.singerMatch),
    poetIds: parseIdsParam(search.poetIds),
    poetMatch: parseMatchMode(search.poetMatch),
    announcerIds: parseIdsParam(search.announcerIds),
    announcerMatch: parseMatchMode(search.announcerMatch),
    composerIds: parseIdsParam(search.composerIds),
    composerMatch: parseMatchMode(search.composerMatch),
    arrangerIds: parseIdsParam(search.arrangerIds),
    arrangerMatch: parseMatchMode(search.arrangerMatch),
    performerIds: parseIdsParam(search.performerIds),
    performerMatch: parseMatchMode(search.performerMatch),
    orchestraLeaderIds: parseIdsParam(search.orchestraLeaderIds),
    orchestraLeaderMatch: parseMatchMode(search.orchestraLeaderMatch),
  }),
  component: SearchPage,
})

function parseString(value: unknown) {
  return typeof value === 'string' ? value : ''
}

function parsePage(value: unknown) {
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : 1
}

function parseIdsParam(value: unknown) {
  return typeof value === 'string'
    ? value
        .split(',')
        .map((item) => item.trim())
        .filter((item) => item.length > 0 && Number.isFinite(Number(item)))
        .join(',')
    : ''
}

function parseIdList(value: string) {
  return value
    .split(',')
    .map((item) => Number(item))
    .filter((item) => Number.isFinite(item))
}

function parseMatchMode(value: unknown): MatchMode {
  return value === 'all' ? 'all' : 'any'
}

function parseSortField(value: unknown): SortField {
  return value === 'id' || value === 'no' || value === 'sub_no' || value === 'title' || value === 'category_name'
    ? value
    : 'no'
}

function parseSortDirection(value: unknown): SortDirection {
  return value === 'desc' ? 'desc' : 'asc'
}

function MultiSelectFilter({
  label,
  placeholder,
  options,
  selectedIds,
  matchMode = 'any',
  onMatchModeChange,
  loading = false,
  disabled = false,
  onChange,
}: MultiSelectProps) {
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState('')
  const rootRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (!rootRef.current?.contains(event.target as Node)) {
        setOpen(false)
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  useEffect(() => {
    if (!open) {
      setQuery('')
    }
  }, [open])

  const filteredOptions = useMemo(() => {
    const normalized = query.trim()
    if (!normalized) return options
    return options.filter((option) => option.name.includes(normalized))
  }, [options, query])

  const selectedNames = useMemo(() => {
    const names = options
      .filter((option) => selectedIds.includes(option.id))
      .map((option) => ({ id: option.id, name: option.name }))
    return names
  }, [options, selectedIds])

  const toggleId = (id: number) => {
    if (selectedIds.includes(id)) {
      onChange(selectedIds.filter((item) => item !== id))
      return
    }
    onChange([...selectedIds, id])
  }

  return (
    <div className={`relative space-y-2.5 ${open ? 'z-[80]' : 'z-10'}`} ref={rootRef}>
      <div className="flex min-h-7 items-center justify-between gap-3">
        <div className="text-right text-[10px] font-black text-muted-foreground">{label}</div>
        {onMatchModeChange ? (
          <div className="flex items-center gap-1">
            <button
              type="button"
              disabled={disabled}
              onClick={() => onMatchModeChange('all')}
              className={`rounded-full px-2.5 py-1 text-[10px] font-black transition-colors ${
                matchMode === 'all'
                  ? 'bg-primary text-white'
                  : 'bg-primary/5 text-primary hover:bg-primary/10'
              } ${disabled ? 'cursor-not-allowed opacity-60' : ''}`}
            >
              همه
            </button>
            <button
              type="button"
              disabled={disabled}
              onClick={() => onMatchModeChange('any')}
              className={`rounded-full px-2.5 py-1 text-[10px] font-black transition-colors ${
                matchMode === 'any'
                  ? 'bg-primary text-white'
                  : 'bg-primary/5 text-primary hover:bg-primary/10'
              } ${disabled ? 'cursor-not-allowed opacity-60' : ''}`}
            >
              هرکدام
            </button>
          </div>
        ) : (
          <div className="h-6" />
        )}
      </div>

      <button
        type="button"
        disabled={disabled}
        onClick={() => setOpen((value) => !value)}
        className={`flex min-h-12 w-full items-center justify-between rounded-2xl border border-primary/10 bg-white px-4 py-2 text-sm font-bold text-foreground transition-colors hover:border-primary/20 ${
          disabled ? 'cursor-not-allowed opacity-60' : ''
        }`}
      >
        <span className="truncate text-right">
          {loading
            ? 'در حال بارگذاری...'
            : selectedNames.length > 0
              ? `${selectedNames.length} انتخاب شده`
              : placeholder}
        </span>
        <ChevronsUpDown className="h-4 w-4 text-muted-foreground" />
      </button>

      {selectedNames.length > 0 && (
        <div className="flex min-h-8 flex-wrap content-start justify-start gap-1.5">
          {selectedNames.map((item) => (
            <Badge
              key={item.id}
              variant="outline"
              className="rounded-full border-primary/15 bg-primary/5 px-2 py-1 text-[10px] font-black text-primary"
            >
              <button
                type="button"
                onClick={() => toggleId(item.id)}
                className="ml-1 inline-flex h-4 w-4 items-center justify-center rounded-full text-primary/70 transition-colors hover:bg-primary/10 hover:text-primary"
                aria-label={`حذف ${item.name}`}
              >
                <X className="h-3 w-3" />
              </button>
              <span>{item.name}</span>
            </Badge>
          ))}
        </div>
      )}

      {selectedNames.length === 0 && (
        <div className="min-h-8" />
      )}

      {open && (
        <div className="absolute inset-x-0 top-full z-[90] mt-2 min-w-full overflow-hidden rounded-[1.3rem] border border-primary/10 bg-white shadow-[0_18px_45px_rgba(31,78,95,0.12)]">
          <div className="border-b border-primary/8 p-3">
            <div className="relative">
              <Search className="absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder={placeholder}
                disabled={loading}
                className="h-10 rounded-xl border-primary/10 bg-background pr-10 text-sm font-bold shadow-none focus-visible:ring-primary/15"
              />
            </div>
          </div>

          <div className="max-h-72 overflow-y-auto p-2 custom-scrollbar">
            {loading ? (
              <div className="px-3 py-4 text-center text-sm font-bold text-muted-foreground">
                در حال بارگذاری گزینه‌ها...
              </div>
            ) : filteredOptions.length === 0 ? (
              <div className="px-3 py-4 text-center text-sm font-bold text-muted-foreground">
                نتیجه‌ای پیدا نشد.
              </div>
            ) : (
              filteredOptions.map((option) => {
                const active = selectedIds.includes(option.id)
                return (
                  <button
                    key={option.id}
                    type="button"
                    onClick={() => toggleId(option.id)}
                    className={`flex w-full items-center justify-between rounded-xl px-3 py-2.5 text-right text-sm font-bold transition-colors ${
                      active ? 'bg-primary text-white' : 'text-foreground hover:bg-primary/5'
                    }`}
                  >
                    <span>{option.name}</span>
                    <Check className={`h-4 w-4 ${active ? 'opacity-100' : 'opacity-0'}`} />
                  </button>
                )
              })
            )}
          </div>
        </div>
      )}
    </div>
  )
}

function SearchPage() {
  const search = Route.useSearch()
  const navigate = useNavigate({ from: Route.fullPath })
  const [options, setOptions] = useState<SearchOptionsResponse>(EMPTY_OPTIONS)
  const [results, setResults] = useState<SearchResultsResponse>(EMPTY_RESULTS)
  const [loadingOptions, setLoadingOptions] = useState(true)
  const [loadingResults, setLoadingResults] = useState(true)
  const [optionsError, setOptionsError] = useState(false)
  const [transcriptQuery, setTranscriptQuery] = useState(search.transcriptQuery)
  const deferredTranscriptQuery = useDeferredValue(transcriptQuery)

  const page = search.page
  const sortField = search.sortField
  const sortDirection = search.sortDirection
  const categoryIds = useMemo(() => parseIdList(search.categoryIds), [search.categoryIds])
  const modeIds = useMemo(() => parseIdList(search.modeIds), [search.modeIds])
  const orchestraIds = useMemo(() => parseIdList(search.orchestraIds), [search.orchestraIds])
  const instrumentIds = useMemo(() => parseIdList(search.instrumentIds), [search.instrumentIds])
  const singerIds = useMemo(() => parseIdList(search.singerIds), [search.singerIds])
  const poetIds = useMemo(() => parseIdList(search.poetIds), [search.poetIds])
  const announcerIds = useMemo(() => parseIdList(search.announcerIds), [search.announcerIds])
  const composerIds = useMemo(() => parseIdList(search.composerIds), [search.composerIds])
  const arrangerIds = useMemo(() => parseIdList(search.arrangerIds), [search.arrangerIds])
  const performerIds = useMemo(() => parseIdList(search.performerIds), [search.performerIds])
  const orchestraLeaderIds = useMemo(() => parseIdList(search.orchestraLeaderIds), [search.orchestraLeaderIds])
  const modeMatch = search.modeMatch
  const orchestraMatch = search.orchestraMatch
  const instrumentMatch = search.instrumentMatch
  const singerMatch = search.singerMatch
  const poetMatch = search.poetMatch
  const announcerMatch = search.announcerMatch
  const composerMatch = search.composerMatch
  const arrangerMatch = search.arrangerMatch
  const performerMatch = search.performerMatch
  const orchestraLeaderMatch = search.orchestraLeaderMatch

  const updateSearch = (
    updater: (prev: Required<SearchPageQuery>) => Required<SearchPageQuery>,
    replace = false,
  ) => {
    navigate({
      search: (prev) => updater(prev as Required<SearchPageQuery>),
      replace,
    })
  }

  const setIdsParam = (key: keyof SearchPageQuery, ids: number[]) => {
    updateSearch((prev) => ({
      ...prev,
      page: 1,
      [key]: ids.length ? ids.join(',') : '',
    }))
  }

  const setMatchParam = (key: keyof SearchPageQuery, mode: MatchMode) => {
    updateSearch((prev) => ({
      ...prev,
      page: 1,
      [key]: mode,
    }))
  }

  useEffect(() => {
    setTranscriptQuery(search.transcriptQuery)
  }, [search.transcriptQuery])

  useEffect(() => {
    fetch('/api/program-search/options')
      .then((res) => {
        if (!res.ok) {
          throw new Error('Failed to load options')
        }
        return res.json()
      })
      .then((payload) => {
        setOptions({
          ...EMPTY_OPTIONS,
          ...payload,
          categories: Array.isArray(payload?.categories) ? payload.categories : [],
          singers: Array.isArray(payload?.singers) ? payload.singers : [],
          poets: Array.isArray(payload?.poets) ? payload.poets : [],
          announcers: Array.isArray(payload?.announcers) ? payload.announcers : [],
          composers: Array.isArray(payload?.composers) ? payload.composers : [],
          arrangers: Array.isArray(payload?.arrangers) ? payload.arrangers : [],
          performers: Array.isArray(payload?.performers) ? payload.performers : [],
          orchestraLeaders: Array.isArray(payload?.orchestraLeaders) ? payload.orchestraLeaders : [],
          modes: Array.isArray(payload?.modes) ? payload.modes : [],
          orchestras: Array.isArray(payload?.orchestras) ? payload.orchestras : [],
          instruments: Array.isArray(payload?.instruments) ? payload.instruments : [],
        })
        setOptionsError(false)
        setLoadingOptions(false)
      })
      .catch(() => {
        setOptions(EMPTY_OPTIONS)
        setOptionsError(true)
        setLoadingOptions(false)
      })
  }, [])

  useEffect(() => {
    if (deferredTranscriptQuery !== search.transcriptQuery) {
      updateSearch((prev) => ({
        ...prev,
        page: 1,
        transcriptQuery: deferredTranscriptQuery.trim(),
      }), true)
    }
  }, [deferredTranscriptQuery, search.transcriptQuery])

  useEffect(() => {
    setLoadingResults(true)
    const params = new URLSearchParams({
      page: page.toString(),
      sortField,
      sortDirection,
    })

    if (search.transcriptQuery.trim()) params.set('transcriptQuery', search.transcriptQuery.trim())
    if (categoryIds.length) params.set('categoryIds', categoryIds.join(','))
    if (modeIds.length) {
      params.set('modeIds', modeIds.join(','))
      params.set('modeMatch', modeMatch)
    }
    if (orchestraIds.length) {
      params.set('orchestraIds', orchestraIds.join(','))
      params.set('orchestraMatch', orchestraMatch)
    }
    if (instrumentIds.length) {
      params.set('instrumentIds', instrumentIds.join(','))
      params.set('instrumentMatch', instrumentMatch)
    }
    if (singerIds.length) {
      params.set('singerIds', singerIds.join(','))
      params.set('singerMatch', singerMatch)
    }
    if (poetIds.length) {
      params.set('poetIds', poetIds.join(','))
      params.set('poetMatch', poetMatch)
    }
    if (announcerIds.length) {
      params.set('announcerIds', announcerIds.join(','))
      params.set('announcerMatch', announcerMatch)
    }
    if (composerIds.length) {
      params.set('composerIds', composerIds.join(','))
      params.set('composerMatch', composerMatch)
    }
    if (arrangerIds.length) {
      params.set('arrangerIds', arrangerIds.join(','))
      params.set('arrangerMatch', arrangerMatch)
    }
    if (performerIds.length) {
      params.set('performerIds', performerIds.join(','))
      params.set('performerMatch', performerMatch)
    }
    if (orchestraLeaderIds.length) {
      params.set('orchestraLeaderIds', orchestraLeaderIds.join(','))
      params.set('orchestraLeaderMatch', orchestraLeaderMatch)
    }

    fetch(`/api/program-search?${params.toString()}`)
      .then((res) => res.json())
      .then((payload) => {
        setResults({
          ...EMPTY_RESULTS,
          ...payload,
          rows: Array.isArray(payload?.rows) ? payload.rows : [],
        })
        setLoadingResults(false)
      })
      .catch(() => setLoadingResults(false))
  }, [
    page,
    search.transcriptQuery,
    categoryIds,
    modeIds,
    modeMatch,
    orchestraIds,
    orchestraMatch,
    instrumentIds,
    instrumentMatch,
    singerIds,
    singerMatch,
    poetIds,
    poetMatch,
    announcerIds,
    announcerMatch,
    composerIds,
    composerMatch,
    arrangerIds,
    arrangerMatch,
    performerIds,
    performerMatch,
    orchestraLeaderIds,
    orchestraLeaderMatch,
    sortField,
    sortDirection,
  ])

  const activeFilterCount = [
    categoryIds.length,
    modeIds.length,
    orchestraIds.length,
    instrumentIds.length,
    singerIds.length,
    poetIds.length,
    announcerIds.length,
    composerIds.length,
    arrangerIds.length,
    performerIds.length,
    orchestraLeaderIds.length,
  ].reduce((sum, value) => sum + value, 0)

  const categorySearchOptions = useMemo<SearchOption[]>(
    () => options.categories.map((item) => ({ id: item.id, name: item.title_fa })),
    [options.categories],
  )

  const resetAll = () => {
    setTranscriptQuery('')
    navigate({
      search: {
        page: 1,
        sortField: 'no',
        sortDirection: 'asc',
        transcriptQuery: '',
        categoryIds: '',
        modeIds: '',
        modeMatch: 'any',
        orchestraIds: '',
        orchestraMatch: 'any',
        instrumentIds: '',
        instrumentMatch: 'any',
        singerIds: '',
        singerMatch: 'any',
        poetIds: '',
        poetMatch: 'any',
        announcerIds: '',
        announcerMatch: 'any',
        composerIds: '',
        composerMatch: 'any',
        arrangerIds: '',
        arrangerMatch: 'any',
        performerIds: '',
        performerMatch: 'any',
        orchestraLeaderIds: '',
        orchestraLeaderMatch: 'any',
      },
    })
  }

  const toggleSort = (field: SortField) => {
    if (sortField === field) {
      updateSearch((prev) => ({
        ...prev,
        page: 1,
        sortDirection: prev.sortDirection === 'asc' ? 'desc' : 'asc',
      }))
      return
    }
    updateSearch((prev) => ({
      ...prev,
      page: 1,
      sortField: field,
      sortDirection: 'asc',
    }))
  }

  const renderSortIcon = (field: SortField) => {
    if (sortField !== field) {
      return <ChevronsUpDown className="h-3.5 w-3.5 opacity-45" />
    }
    return sortDirection === 'asc'
      ? <ChevronUp className="h-3.5 w-3.5" />
      : <ChevronDown className="h-3.5 w-3.5" />
  }

  const renderPagination = () => {
    const items = []
    const total = results.totalPages || 1

    items.push(
      <PaginationItem key="first">
        <PaginationLink
          size="default"
          onClick={() => updateSearch((prev) => ({ ...prev, page: 1 }))}
          isActive={page === 1}
          className="cursor-pointer"
        >
          1
        </PaginationLink>
      </PaginationItem>,
    )

    if (page > 3) {
      items.push(<PaginationItem key="ellipsis-start"><PaginationEllipsis /></PaginationItem>)
    }

    for (let i = Math.max(2, page - 1); i <= Math.min(total - 1, page + 1); i++) {
      if (i === 1 || i === total) continue
      items.push(
        <PaginationItem key={i}>
          <PaginationLink
            size="default"
            onClick={() => updateSearch((prev) => ({ ...prev, page: i }))}
            isActive={page === i}
            className="cursor-pointer"
          >
            {i}
          </PaginationLink>
        </PaginationItem>,
      )
    }

    if (page < total - 2) {
      items.push(<PaginationItem key="ellipsis-end"><PaginationEllipsis /></PaginationItem>)
    }

    if (total > 1) {
      items.push(
        <PaginationItem key="last">
          <PaginationLink
            size="default"
            onClick={() => updateSearch((prev) => ({ ...prev, page: total }))}
            isActive={page === total}
            className="cursor-pointer"
          >
            {total}
          </PaginationLink>
        </PaginationItem>,
      )
    }

    return items
  }

  return (
    <div className="space-y-4 animate-in" dir="rtl">
      <section className="relative z-40 overflow-visible rounded-[1.8rem] border border-primary/10 bg-white/85 p-5 shadow-[0_18px_50px_rgba(31,78,95,0.06)] backdrop-blur-md">
        <div className="space-y-5">
          <div className="flex flex-col gap-3 xl:flex-row xl:items-start xl:justify-between">
            <div className="space-y-2 text-right">
              <div className="flex items-center justify-start gap-3">
                <div className="flex h-12 w-12 items-center justify-center rounded-[1.4rem] bg-primary text-white shadow-lg shadow-primary/10">
                  <Search className="h-6 w-6" />
                </div>
                <div>
                  <h1 className="text-2xl font-black tracking-tight text-foreground">جست‌وجوی پیشرفته برنامه‌ها</h1>
                  <p className="text-[12px] font-bold text-muted-foreground">
                    فیلترهای ساخت‌یافته با منطق AND، و جست‌وجوی متنی فقط روی متن برنامه
                  </p>
                </div>
              </div>
            </div>

            <div className="flex flex-wrap items-center justify-end gap-2">
              <Badge className="rounded-full border border-primary/15 bg-primary/5 px-3 py-1 text-[10px] font-black text-primary">
                {activeFilterCount} انتخاب ساخت‌یافته
              </Badge>
              <Badge className="rounded-full border-none bg-primary px-3 py-1 text-[10px] font-black text-white">
                {results.total} نتیجه
              </Badge>
            </div>
          </div>

          <div className="rounded-[1.5rem] border border-primary/10 bg-background/70 p-4">
            <div className="space-y-5">
              <div className="space-y-3">
                <div className="flex items-center justify-start gap-2 text-[11px] font-black text-primary">
                  <Waves className="h-4 w-4" />
                  جست‌وجو در متن برنامه
                </div>
                <div className="relative">
                  <Search className="absolute right-4 top-1/2 h-5 w-5 -translate-y-1/2 text-primary/50" />
                  <Input
                    value={transcriptQuery}
                    onChange={(event) => setTranscriptQuery(event.target.value)}
                    placeholder="بخشی از شعر یا متن برنامه را وارد کن..."
                    className="h-14 rounded-[1.3rem] border-primary/10 bg-white pr-12 text-right text-base font-black shadow-none focus-visible:ring-primary/15"
                  />
                </div>
              </div>

              <div className="grid gap-4 xl:grid-cols-4 lg:grid-cols-3 md:grid-cols-2">
                <MultiSelectFilter label="دسته برنامه" placeholder="انتخاب دسته..." options={categorySearchOptions} selectedIds={categoryIds} loading={loadingOptions} disabled={optionsError} onChange={(ids) => setIdsParam('categoryIds', ids)} />
                <MultiSelectFilter label="دستگاه" placeholder="انتخاب دستگاه..." options={options.modes} selectedIds={modeIds} matchMode={modeMatch} onMatchModeChange={(mode) => setMatchParam('modeMatch', mode)} loading={loadingOptions} disabled={optionsError} onChange={(ids) => setIdsParam('modeIds', ids)} />
                <MultiSelectFilter label="ارکستر" placeholder="انتخاب ارکستر..." options={options.orchestras} selectedIds={orchestraIds} matchMode={orchestraMatch} onMatchModeChange={(mode) => setMatchParam('orchestraMatch', mode)} loading={loadingOptions} disabled={optionsError} onChange={(ids) => setIdsParam('orchestraIds', ids)} />
                <MultiSelectFilter label="ساز" placeholder="انتخاب ساز..." options={options.instruments} selectedIds={instrumentIds} matchMode={instrumentMatch} onMatchModeChange={(mode) => setMatchParam('instrumentMatch', mode)} loading={loadingOptions} disabled={optionsError} onChange={(ids) => setIdsParam('instrumentIds', ids)} />
                <MultiSelectFilter label="خواننده" placeholder="انتخاب خواننده..." options={options.singers} selectedIds={singerIds} matchMode={singerMatch} onMatchModeChange={(mode) => setMatchParam('singerMatch', mode)} loading={loadingOptions} disabled={optionsError} onChange={(ids) => setIdsParam('singerIds', ids)} />
                <MultiSelectFilter label="شاعر" placeholder="انتخاب شاعر..." options={options.poets} selectedIds={poetIds} matchMode={poetMatch} onMatchModeChange={(mode) => setMatchParam('poetMatch', mode)} loading={loadingOptions} disabled={optionsError} onChange={(ids) => setIdsParam('poetIds', ids)} />
                <MultiSelectFilter label="گوینده" placeholder="انتخاب گوینده..." options={options.announcers} selectedIds={announcerIds} matchMode={announcerMatch} onMatchModeChange={(mode) => setMatchParam('announcerMatch', mode)} loading={loadingOptions} disabled={optionsError} onChange={(ids) => setIdsParam('announcerIds', ids)} />
                <MultiSelectFilter label="آهنگساز" placeholder="انتخاب آهنگساز..." options={options.composers} selectedIds={composerIds} matchMode={composerMatch} onMatchModeChange={(mode) => setMatchParam('composerMatch', mode)} loading={loadingOptions} disabled={optionsError} onChange={(ids) => setIdsParam('composerIds', ids)} />
                <MultiSelectFilter label="تنظیم‌کننده" placeholder="انتخاب تنظیم‌کننده..." options={options.arrangers} selectedIds={arrangerIds} matchMode={arrangerMatch} onMatchModeChange={(mode) => setMatchParam('arrangerMatch', mode)} loading={loadingOptions} disabled={optionsError} onChange={(ids) => setIdsParam('arrangerIds', ids)} />
                <MultiSelectFilter label="نوازنده" placeholder="انتخاب نوازنده..." options={options.performers} selectedIds={performerIds} matchMode={performerMatch} onMatchModeChange={(mode) => setMatchParam('performerMatch', mode)} loading={loadingOptions} disabled={optionsError} onChange={(ids) => setIdsParam('performerIds', ids)} />
                <MultiSelectFilter label="رهبر ارکستر" placeholder="انتخاب رهبر..." options={options.orchestraLeaders} selectedIds={orchestraLeaderIds} matchMode={orchestraLeaderMatch} onMatchModeChange={(mode) => setMatchParam('orchestraLeaderMatch', mode)} loading={loadingOptions} disabled={optionsError} onChange={(ids) => setIdsParam('orchestraLeaderIds', ids)} />
              </div>

              {optionsError && (
                <div className="rounded-2xl border border-destructive/20 bg-destructive/5 px-4 py-3 text-right text-[11px] font-bold text-destructive">
                  بارگذاری گزینه‌های جست‌وجو ناموفق بود. یک بار صفحه را refresh کن یا dev server را دوباره اجرا کن.
                </div>
              )}

              <div className="flex justify-end">
                <button
                  type="button"
                  onClick={resetAll}
                  className="rounded-full bg-primary/5 px-4 py-2 text-[11px] font-black text-primary transition-all hover:bg-primary/10"
                >
                  پاک کردن همه فیلترها
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="relative z-0 overflow-visible rounded-[1.8rem] border border-primary/10 bg-white/85 shadow-[0_18px_45px_rgba(31,78,95,0.06)] backdrop-blur-md">
        <div className="flex items-center justify-between border-b border-primary/8 px-5 py-4">
          <div className="flex items-center gap-2 text-sm font-black text-primary">
            <LibraryBig className="h-4 w-4" />
            <span>نتایج</span>
          </div>
          <div className="text-right text-[11px] font-bold text-muted-foreground">
            {loadingOptions ? 'در حال بارگذاری فیلترها...' : 'نتیجه بر اساس فیلترهای ساخت‌یافته و متن برنامه'}
          </div>
        </div>

        <div className="w-full overflow-x-auto">
          <table className="w-full border-collapse text-right">
            <colgroup>
              <col className="w-[96px]" />
              <col className="w-[120px]" />
              <col className="w-[120px]" />
              <col />
              <col className="w-[140px]" />
              <col className="w-[96px]" />
            </colgroup>
            <thead className="bg-primary/[0.03]">
              <tr>
                <th className="px-5 py-4 text-center text-[11px] font-black text-primary">
                  <button type="button" onClick={() => toggleSort('id')} className="inline-flex items-center justify-center gap-1.5">
                    <Hash className="h-3.5 w-3.5" />
                    {renderSortIcon('id')}
                  </button>
                </th>
                <th className="px-3 py-4 text-center text-[11px] font-black text-primary whitespace-nowrap">
                  <button type="button" onClick={() => toggleSort('no')} className="inline-flex items-center justify-center gap-1.5">
                    <span>شماره برنامه</span>
                    {renderSortIcon('no')}
                  </button>
                </th>
                <th className="px-3 py-4 text-center text-[11px] font-black text-primary whitespace-nowrap">
                  <button type="button" onClick={() => toggleSort('sub_no')} className="inline-flex items-center justify-center gap-1.5">
                    <span>ساب‌نامبر</span>
                    {renderSortIcon('sub_no')}
                  </button>
                </th>
                <th className="px-5 py-4 text-right text-[11px] font-black text-primary">
                  <button type="button" onClick={() => toggleSort('title')} className="inline-flex items-center gap-1.5">
                    {renderSortIcon('title')}
                    <span>عنوان برنامه</span>
                  </button>
                </th>
                <th className="px-5 py-4 text-center text-[11px] font-black text-primary">
                  <button type="button" onClick={() => toggleSort('category_name')} className="inline-flex items-center justify-center gap-1.5">
                    <span>دسته</span>
                    {renderSortIcon('category_name')}
                  </button>
                </th>
                <th className="px-5 py-4 text-center text-[11px] font-black text-primary">مشاهده</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-primary/8">
              {loadingResults ? (
                [...Array(10)].map((_, index) => (
                  <tr key={index} className="animate-pulse">
                    <td colSpan={6} className="h-16 bg-white/40" />
                  </tr>
                ))
              ) : results.rows.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-20 text-center">
                    <div className="space-y-2">
                      <div className="text-lg font-black text-primary">نتیجه‌ای پیدا نشد</div>
                      <p className="text-sm font-bold text-muted-foreground">فیلترها را کمتر کن یا متن برنامه را تغییر بده.</p>
                    </div>
                  </td>
                </tr>
              ) : (
                results.rows.map((program) => (
                  <tr key={program.id} className="group transition-colors hover:bg-primary/[0.035]">
                    <td className="px-5 py-4 text-center font-mono text-[10px] font-black text-muted-foreground">{program.id}</td>
                    <td className="px-3 py-4">
                      <div className="flex justify-center">
                        <span className="inline-flex rounded-xl border border-primary/10 bg-primary/5 px-3 py-1.5 text-[10px] font-black text-primary">
                          {program.no}
                        </span>
                      </div>
                    </td>
                    <td className="px-3 py-4">
                      <div className="flex justify-center">
                        {program.sub_no ? (
                          <Badge variant="outline" className="rounded-full border-primary/20 bg-secondary/10 px-2.5 py-1 text-[10px] font-black text-primary">
                            {program.sub_no}
                          </Badge>
                        ) : (
                          <span className="text-[10px] font-bold text-muted-foreground/50">-</span>
                        )}
                      </div>
                    </td>
                    <td className="px-5 py-4">
                      <div className="text-sm font-black text-foreground transition-colors group-hover:text-primary">
                        {program.title}
                      </div>
                    </td>
                    <td className="px-5 py-4 text-center">
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
        <Pagination className="order-2 justify-start md:order-1">
          <PaginationContent>
            <PaginationItem>
                <PaginationPrevious
                size="default"
                onClick={() => updateSearch((prev) => ({ ...prev, page: Math.max(1, prev.page - 1) }))}
                className={`cursor-pointer ${page === 1 ? 'pointer-events-none opacity-30 shadow-none' : ''}`}
              />
            </PaginationItem>
            {renderPagination()}
            <PaginationItem>
              <PaginationNext
                size="default"
                onClick={() => updateSearch((prev) => ({ ...prev, page: Math.min(results.totalPages, prev.page + 1) }))}
                className={`cursor-pointer ${page === results.totalPages ? 'pointer-events-none opacity-30 shadow-none' : ''}`}
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>

        <div className="order-1 flex w-full flex-col items-end gap-1 md:order-2">
          <span className="text-[11px] font-bold text-muted-foreground">
            صفحه {results.page} از {results.totalPages}
          </span>
        </div>
      </div>
    </div>
  )
}
