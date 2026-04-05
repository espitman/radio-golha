import { createFileRoute, Link } from '@tanstack/react-router'
import { type ReactNode, useEffect, useMemo, useRef, useState } from 'react'
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

  const stats = useMemo(() => {
    if (!data) return []
    return [
      { label: 'سگمنت‌ها', value: data.timeline?.length || 0, icon: Timer },
      { label: 'خوانندگان', value: data.singers?.length || 0, icon: Mic },
      { label: 'نوازندگان', value: data.performers?.length || 0, icon: Music },
      { label: 'شاعران', value: data.poets?.length || 0, icon: BookOpen },
    ]
  }, [data])

  const transcriptGroups = useMemo(() => {
    const verses = Array.isArray(data?.transcript) ? data.transcript : []
    const grouped = new Map<number, Array<{ segment_order: number; verse_order: number; text: string }>>()

    verses.forEach((verse: { segment_order: number; verse_order: number; text: string }) => {
      const segmentOrder = Number(verse.segment_order) || 0
      if (!grouped.has(segmentOrder)) {
        grouped.set(segmentOrder, [])
      }
      grouped.get(segmentOrder)!.push(verse)
    })

    return Array.from(grouped.entries())
      .sort((a, b) => a[0] - b[0])
      .map(([segmentOrder, items]) => ({
        segmentOrder,
        verses: items.sort((a, b) => (Number(a.verse_order) || 0) - (Number(b.verse_order) || 0)),
      }))
  }, [data])

  if (loading) {
    return <div className="p-10 text-center text-primary font-black animate-pulse text-[10px]">در حال بازیابی فایل‌های صوتی...</div>
  }

  if (!data) {
    return <div className="p-10 text-center text-red-500 font-bold">برنامه یافت نشد!</div>
  }

  return (
    <div className="space-y-5 animate-in" dir="rtl">
      <section className="relative overflow-hidden rounded-[2rem] border border-primary/10 bg-gradient-to-br from-[#0f2b34] via-primary to-[#173d49] p-5 md:p-7 text-primary-foreground shadow-[0_30px_80px_rgba(12,36,44,0.22)]">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(255,255,255,0.16),transparent_30%),radial-gradient(circle_at_bottom_left,rgba(170,207,208,0.18),transparent_28%)]" />
        <div className="absolute -top-10 left-8 h-32 w-32 rounded-full bg-secondary/20 blur-3xl" />
        <div className="absolute -bottom-16 right-10 h-40 w-40 rounded-full bg-black/10 blur-3xl" />

        <div className="relative z-10 grid gap-6 xl:grid-cols-[1.5fr_0.9fr] xl:items-end">
          <div className="space-y-5 text-right">
            <div className="flex flex-wrap items-center justify-end gap-2">
              <Badge className="rounded-full border-none bg-white/12 px-3 py-1 text-[10px] font-black text-white backdrop-blur-md">
                آرشیو تحلیلی
              </Badge>
              <Badge className="rounded-full border-none bg-secondary px-3 py-1 text-[10px] font-black text-primary">
                {data.category_name}
              </Badge>
              <span className="rounded-full border border-white/10 bg-white/6 px-3 py-1 text-[10px] font-mono text-white/50">
                REF #{data.id}
              </span>
            </div>

            <div className="space-y-3">
              <Link
                to="/programs"
                className="inline-flex items-center gap-1.5 rounded-full border border-white/10 bg-white/6 px-3 py-1.5 text-[10px] font-black text-white/60 transition-all hover:bg-white/10 hover:text-white"
              >
                بازگشت به آرشیو
                <ArrowLeft className="w-3 h-3" />
              </Link>

              <div>
                <h1 className="max-w-4xl text-3xl leading-tight font-black text-white md:text-4xl">
                  {data.title}
                </h1>
                <div className="mt-2 flex flex-wrap items-center justify-end gap-3 text-[11px] font-bold text-white/55">
                  <span className="rounded-full bg-black/10 px-3 py-1 font-mono">
                    NO: {data.sub_no ? `${data.no} ${data.sub_no}` : data.no}
                  </span>
                  <span className="inline-flex items-center gap-1.5">
                    <Clock3 className="w-3.5 h-3.5" />
                    نمای کامل برنامه و تایم‌لاین اجرایی
                  </span>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
              {stats.map((stat) => (
                <div
                  key={stat.label}
                  className="rounded-[1.4rem] border border-white/10 bg-white/8 p-3 backdrop-blur-md shadow-inner shadow-black/5"
                >
                  <div className="mb-3 flex items-center justify-between">
                    <span className="inline-flex h-10 w-10 items-center justify-center rounded-2xl bg-white/10">
                      <stat.icon className="w-4 h-4 text-secondary" />
                    </span>
                    <span className="text-2xl font-black text-white">{stat.value}</span>
                  </div>
                  <div className="text-[10px] font-black text-white/55">{stat.label}</div>
                </div>
              ))}
            </div>
          </div>

          <div className="rounded-[1.8rem] border border-white/10 bg-white/8 p-4 backdrop-blur-2xl shadow-2xl shadow-black/10">
            <div className="mb-4 flex items-center justify-between">
              <div className="space-y-1 text-right">
                <p className="text-[10px] font-black uppercase tracking-[0.24em] text-white/45">Audio Console</p>
                <h2 className="text-lg font-black text-white">پخش و ناوبری برنامه</h2>
              </div>
              <button
                onClick={() => audioRef.current?.play()}
                className="flex h-12 w-12 items-center justify-center rounded-2xl bg-secondary text-primary shadow-lg shadow-black/15 transition-transform hover:scale-105 active:scale-95"
              >
                <Play className="w-5 h-5 fill-current translate-x-[2px]" />
              </button>
            </div>

            {data.audio_url ? (
              <div className="space-y-4">
                <div className="rounded-[1.4rem] border border-white/8 bg-black/10 p-3">
                  <audio ref={audioRef} controls src={data.audio_url} className="w-full opacity-80" />
                </div>
                <div className="flex items-center justify-between rounded-[1.2rem] border border-white/8 bg-white/6 px-4 py-3 text-[11px] font-bold text-white/70">
                  <span>{new Date(currentTime * 1000).toISOString().slice(14, 19)}</span>
                  <span className="inline-flex items-center gap-2">
                    <Waves className="w-4 h-4 text-secondary" />
                    برای پرش به هر بخش روی کارت تایم‌لاین کلیک کن
                  </span>
                </div>
              </div>
            ) : (
              <div className="rounded-[1.2rem] border border-dashed border-white/15 bg-black/10 px-4 py-6 text-center text-[11px] font-bold text-white/55">
                فایل صوتی برای این برنامه ثبت نشده است.
              </div>
            )}
          </div>
        </div>
      </section>

      <div className="grid grid-cols-1 gap-5 xl:grid-cols-[380px_minmax(0,1.6fr)] xl:items-stretch">
        <div className="order-1 space-y-4 xl:order-1 xl:h-[calc(100vh-140px)]" dir="rtl">
          <Card className="h-full !pt-0 overflow-hidden rounded-[2rem] border-border/40 bg-card/55 shadow-[0_16px_45px_rgba(31,78,95,0.08)] backdrop-blur-md">
          <CardHeader className="border-b border-primary/8 bg-gradient-to-l from-primary/6 to-transparent px-5 py-4" dir="rtl">
              <CardTitle className="flex w-full items-center justify-start gap-2 text-right text-sm font-black text-primary">
                <Library className="w-4 h-4" />
                شناسنامه نهایی اثر
              </CardTitle>
            </CardHeader>

            <CardContent className="h-[calc(100%-73px)] overflow-y-auto space-y-5 px-5 py-5 text-right custom-scrollbar">
              {data.modes?.length > 0 && (
                <MetaSection title="دستگاه‌ها" icon={Waves} accentClass="bg-primary/10 text-primary">
                  <div className="flex flex-wrap justify-start gap-2">
                    {data.modes.map((mode: string) => (
                      <Badge key={mode} className="rounded-full border-none bg-primary px-3 py-1.5 text-[10px] font-black text-white">
                        {mode}
                      </Badge>
                    ))}
                  </div>
                </MetaSection>
              )}

              <MetaSection title="خوانندگان اصلی" icon={Mic} accentClass="bg-primary/10 text-primary">
                <div className="flex flex-wrap justify-start gap-2">
                  {data.singers?.length ? (
                    data.singers.map((name: string) => (
                      <Badge key={name} className="rounded-full border-none bg-primary px-3 py-1.5 text-[10px] font-black text-white">
                        {name}
                      </Badge>
                    ))
                  ) : (
                    <div className="text-[11px] font-bold text-muted-foreground">اطلاعاتی ثبت نشده است.</div>
                  )}
                </div>
              </MetaSection>

              <MetaSection title="نوازندگان اصلی" icon={Disc3} accentClass="bg-secondary/20 text-primary">
                <div className="grid gap-2">
                  {data.performers?.length ? (
                    data.performers.map((performer: any, index: number) => (
                      <div
                        key={`${performer.name}-${performer.instrument}-${index}`}
                        className="flex items-center justify-between rounded-[1.2rem] border border-border/35 bg-white/70 px-3 py-3"
                      >
                        <span className="text-[12px] font-black text-foreground">{performer.name}</span>
                        <span className="rounded-full bg-primary/6 px-2.5 py-1 text-[10px] font-black text-primary/75">
                          {performer.instrument || 'نامشخص'}
                        </span>
                      </div>
                    ))
                  ) : (
                    <div className="text-[11px] font-bold text-muted-foreground">اطلاعاتی ثبت نشده است.</div>
                  )}
                </div>
              </MetaSection>

              <Separator className="bg-primary/8" />

              {data.poets?.length > 0 && (
                <MetaSection title="شاعران" icon={BookOpen} accentClass="bg-amber-100 text-amber-800">
                  <div className="flex w-full flex-wrap justify-start gap-2 text-right">
                    {data.poets.map((name: string) => (
                      <Badge
                        key={name}
                        className="rounded-full border border-amber-200/70 bg-amber-50 px-3 py-1.5 text-[10px] font-black text-amber-900 shadow-none hover:bg-amber-100 transition-all"
                      >
                        {name}
                      </Badge>
                    ))}
                  </div>
                </MetaSection>
              )}

              {data.composers?.length > 0 && (
                <MetaSection title="آهنگساز" icon={PenTool} accentClass="bg-primary/10 text-primary">
                  <div className="rounded-[1.2rem] border border-border/35 bg-white/70 p-3 text-[12px] font-black leading-7 text-primary/85">
                    {data.composers.join('، ')}
                  </div>
                </MetaSection>
              )}

              {data.arrangers?.length > 0 && (
                <MetaSection title="تنظیم‌کننده" icon={Layout} accentClass="bg-secondary/20 text-primary">
                  <div className="rounded-[1.2rem] border border-border/35 bg-white/70 p-3 text-[12px] font-black leading-7 text-primary/80">
                    {data.arrangers.join('، ')}
                  </div>
                </MetaSection>
              )}

              {(data.announcers?.length > 0 || data.orchestras?.length > 0) && <Separator className="bg-primary/8" />}

              {data.announcers?.length > 0 && (
                <MetaSection title="گویندگان برنامه" icon={Radio} accentClass="bg-sky-100 text-sky-900">
                  <div className="flex w-full flex-wrap justify-start gap-2 text-right">
                    {data.announcers.map((name: string) => (
                      <Badge
                        key={name}
                        className="rounded-full border border-sky-200/70 bg-sky-50 px-3 py-1.5 text-[10px] font-black text-sky-900 shadow-none hover:bg-sky-100 transition-all"
                      >
                        {name}
                      </Badge>
                    ))}
                  </div>
                </MetaSection>
              )}

              {data.orchestras?.length > 0 && (
                <MetaSection title="ارکستر" icon={Library} accentClass="bg-emerald-100 text-emerald-900">
                  <div className="rounded-[1.2rem] border border-emerald-200/65 bg-emerald-50/75 p-3 text-[12px] font-black leading-7 text-emerald-950/70">
                    {data.orchestras.map((orchestra: string) => {
                      const leaders = (data.orchestra_leaders || [])
                        .filter((leader: any) => leader.orchestra === orchestra)
                        .map((leader: any) => leader.name)

                      return leaders.length
                        ? `${orchestra}، به رهبری ${leaders.join(' و ')}`
                        : orchestra
                    }).join('، ')}
                  </div>
                </MetaSection>
              )}

            </CardContent>
          </Card>
        </div>

        <Card className="order-2 !pt-0 overflow-hidden rounded-[2rem] border-primary/10 bg-white/90 shadow-[0_16px_50px_rgba(31,78,95,0.08)] backdrop-blur-md xl:order-2 xl:h-[calc(100vh-140px)]" dir="rtl">
          <CardHeader className="border-b border-primary/8 bg-gradient-to-l from-primary/4 to-transparent px-5 py-4 md:px-6" dir="rtl">
            <div className="flex items-center justify-between gap-4">
              
              <CardTitle className="flex items-center justify-end gap-2 text-right text-base font-black text-primary">
                <Timer className="w-4 h-4" />
                تایم‌لاین تحلیلی برنامه
              </CardTitle>
              <div className="text-right">
                <div className="text-[10px] font-mono uppercase tracking-[0.28em] text-primary/35">Timeline View</div>
                <div className="text-[11px] font-bold text-muted-foreground">ناوبری جزءبه‌جزء بر اساس اجرا، خوانش و شعر</div>
              </div>
            </div>
          </CardHeader>

          <CardContent className="h-[calc(100%-73px)] overflow-y-auto p-4 md:p-5 custom-scrollbar">
            <div className="relative pr-14">
              <div className="absolute right-6 top-2 bottom-2 w-[2px] rounded-full bg-gradient-to-b from-primary/20 via-primary/8 to-transparent" />

              <div className="space-y-4">
                {data.timeline.map((segment: any, index: number) => {
                  const startSeconds = timeToSeconds(segment.start_time)
                  const endSeconds = timeToSeconds(segment.end_time)
                  const isActive = currentTime >= startSeconds && (endSeconds === 0 || currentTime < endSeconds)

                  return (
                    <article
                      key={`${segment.id ?? index}-${segment.start_time}`}
                      onClick={() => seekTo(segment.start_time)}
                      className={[
                        "group relative cursor-pointer pr-6 transition-all duration-300",
                        isActive ? "translate-x-0.5" : "",
                      ].join(' ')}
                    >
                      <div
                        className={[
                          "absolute right-6 top-5 z-10 flex h-7 w-7 translate-x-1/2 items-center justify-center rounded-full border text-[9px] font-black shadow-sm transition-all",
                          isActive
                            ? "border-secondary bg-secondary text-primary shadow-[0_0_0_6px_rgba(170,207,208,0.18)]"
                            : "border-primary/20 bg-white text-primary group-hover:border-primary group-hover:bg-primary/8",
                        ].join(' ')}
                      >
                        {index + 1}
                      </div>

                      <div
                        className={[
                          "rounded-[1.5rem] border p-4 shadow-sm transition-all duration-300 md:p-5",
                          isActive
                            ? "border-primary/25 bg-gradient-to-br from-primary/[0.08] to-white shadow-[0_18px_40px_rgba(31,78,95,0.12)]"
                            : "border-border/40 bg-white/75 hover:border-primary/18 hover:bg-white hover:shadow-[0_14px_34px_rgba(31,78,95,0.08)]",
                        ].join(' ')}
                      >
                        <div className="mb-4 flex flex-wrap items-start justify-between gap-3">
                          <div className="flex items-center gap-2">
                            <div className="flex items-center gap-2 text-sm font-black text-foreground">
                              <Volume2 className={`w-4 h-4 ${isActive ? 'text-primary' : 'text-primary/30 group-hover:text-primary/70'}`} />
                              <span>{segment.mode_name || 'بخش بدون دستگاه'}</span>
                            </div>
                            {isActive && (
                              <Badge className="border-none bg-primary px-2.5 py-1 text-[9px] font-black text-white">
                                در حال پخش
                              </Badge>
                            )}
                          </div>

                          <div className="flex flex-wrap items-center gap-2">
                            <span className="rounded-xl border border-primary/10 bg-primary/6 px-2.5 py-1 text-[10px] font-mono font-black text-primary">
                              {segment.start_time}
                            </span>
                            {segment.end_time && (
                              <span className="rounded-xl border border-border/50 bg-background px-2.5 py-1 text-[10px] font-mono font-bold text-muted-foreground">
                                تا {segment.end_time}
                              </span>
                            )}
                            <span className="rounded-xl border border-secondary/25 bg-secondary/10 px-2.5 py-1 text-[10px] font-black text-foreground/80">
                              {formatDuration(segment.start_time, segment.end_time)}
                            </span>
                          </div>
                        </div>

                        <div className="grid gap-3 md:grid-cols-2 2xl:grid-cols-3">
                          {segment.singers?.length > 0 && (
                            <div className="rounded-[1.2rem] border border-primary/8 bg-primary/[0.04] p-3 text-right">
                              <div className="mb-2 flex items-center justify-start gap-2 text-[10px] font-black text-primary/70">
                                <Mic className="w-3.5 h-3.5" />
                                خواننده                                </div>
                              <div className="text-sm font-black text-primary">{segment.singers.join('، ')}</div>
                            </div>
                          )}

                          {segment.poets?.length > 0 && (
                            <div className="rounded-[1.2rem] border border-amber-200/70 bg-amber-50/70 p-3 text-right">
                              <div className="mb-2 flex items-center justify-start gap-2 text-[10px] font-black text-amber-800/80">
                                <BookOpen className="w-3.5 h-3.5" />
                                شاعر                               </div>
                              <div className="text-sm font-black text-amber-950/80">{segment.poets.join('، ')}</div>
                            </div>
                          )}

                          {segment.announcers?.length > 0 && (
                            <div className="rounded-[1.2rem] border border-sky-200/70 bg-sky-50/80 p-3 text-right">
                              <div className="mb-2 flex items-center justify-start gap-2 text-[10px] font-black text-sky-900/70">
                                <Radio className="w-3.5 h-3.5" />
                                گوینده
                              </div>
                              <div className="text-sm font-black text-sky-950/70">{segment.announcers.join('، ')}</div>
                            </div>
                          )}

                          {(segment.orchestras?.length > 0 || segment.orchestraLeaders?.length > 0) && (
                            <div className="rounded-[1.2rem] border border-emerald-200/70 bg-emerald-50/80 p-3 text-right">
                              <div className="mb-2 flex items-center justify-start gap-2 text-[10px] font-black text-emerald-900/70">
                                <Library className="w-3.5 h-3.5" />
                                ارکستر
                              </div>
                              {segment.orchestras?.length > 0 && (
                                <div className="text-sm font-black text-emerald-950/80">{segment.orchestras.join('، ')}</div>
                              )}
                              {segment.orchestraLeaders?.length > 0 && (
                                <div className="mt-2 flex flex-wrap justify-start gap-2">
                                  {segment.orchestraLeaders.map((leader: any) => (
                                    <Badge
                                      key={`${segment.id}-${leader.orchestra}-${leader.name}`}
                                      className="rounded-full border border-emerald-200/70 bg-white px-2.5 py-1 text-[9px] font-black text-emerald-950/80 shadow-none"
                                    >
                                      رهبر: {leader.name}
                                    </Badge>
                                  ))}
                                </div>
                              )}
                            </div>
                          )}
                        </div>

                        {segment.performers?.length > 0 && (
                          <div className="mt-4 rounded-[1.2rem] border border-border/45 bg-background/70 p-3.5">
                            <div className="mb-3 flex items-center justify-start gap-2 text-[10px] font-black text-muted-foreground">
                              <Music className="w-3.5 h-3.5 text-primary/70" />
                              نوازندگان
                            </div>
                            <div className="flex w-full flex-wrap justify-start gap-2 text-right">
                              {segment.performers.map((performer: any, performerIndex: number) => (
                                <span
                                  key={`${performer.name}-${performer.instrument}-${performerIndex}`}
                                  className="inline-flex items-center gap-2 rounded-full border border-secondary/25 bg-secondary/10 px-3 py-1.5 text-[11px] font-black text-foreground/85 hover:bg-secondary/20 transition-all"
                                >
                                  <span className="text-muted-foreground/80 text-[10px]">({performer.instrument || 'ساز نامشخص'})</span>
                                  <span>{performer.name}</span>
                                </span>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>
                    </article>
                  )
                })}
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card className="!pt-0 overflow-hidden rounded-[2rem] border-primary/10 bg-white/90 shadow-[0_16px_50px_rgba(31,78,95,0.08)] backdrop-blur-md" dir="rtl">
          <CardHeader className="border-b border-primary/8 bg-gradient-to-l from-primary/4 to-transparent px-5 py-4 md:px-6" dir="rtl">
            <div className="flex items-center justify-between gap-4">
              <CardTitle className="flex items-center justify-end gap-2 text-right text-base font-black text-primary">
                <BookOpen className="w-4 h-4" />
                متن برنامه
              </CardTitle>
              <div className="text-right">
                <div className="text-[10px] font-mono uppercase tracking-[0.28em] text-primary/35">Transcript</div>
                <div className="text-[11px] font-bold text-muted-foreground">ابیات برنامه</div>
              </div>
            </div>
          </CardHeader>

          <CardContent className="space-y-4 p-4 md:p-5">
            {transcriptGroups.length > 0 ? (
              transcriptGroups.map((group) => (
                <section
                  key={`transcript-segment-${group.segmentOrder}`}
                  className="rounded-[1.5rem] border border-border/40 bg-white/75 p-4 shadow-sm"
                >
                  <div className="mb-3 flex items-center justify-between gap-3">
                    <div className="text-right">
                      <div className="text-[10px] font-mono uppercase tracking-[0.24em] text-primary/35">
                        Segment {group.segmentOrder}
                      </div>
                      <div className="text-[11px] font-black text-primary/80">
                        {group.verses.length} بیت
                      </div>
                    </div>
                    <Badge className="border-none bg-primary px-2.5 py-1 text-[9px] font-black text-white">
                      بخش {group.segmentOrder}
                    </Badge>
                  </div>

                  <div className="space-y-2.5">
                    {group.verses.map((verse) => (
                      <div
                        key={`verse-${group.segmentOrder}-${verse.verse_order}`}
                        className="rounded-[1.1rem] border border-primary/8 bg-primary/[0.03] px-4 py-3 text-right"
                      >
                        <div className="mb-1 text-[10px] font-mono text-primary/45">
                          Verse {verse.verse_order}
                        </div>
                        <p className="text-[13px] font-black leading-8 text-foreground/90">
                          {verse.text}
                        </p>
                      </div>
                    ))}
                  </div>
                </section>
              ))
            ) : (
              <div className="rounded-[1.4rem] border border-dashed border-primary/20 bg-primary/[0.03] px-4 py-8 text-center text-[12px] font-black text-primary/55">
                                متنی برای این برنامه ثبت نشده است.

              </div>
            )}
          </CardContent>
        </Card>
    </div>
  )
}
