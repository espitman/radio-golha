import { createFileRoute, Link } from '@tanstack/react-router'
import { useState } from 'react'
import { CheckCircle2, LibraryBig, Loader2, Music4, UploadCloud, Workflow, XCircle } from 'lucide-react'

const ITEMS = [
  {
    to: '/orchestras',
    title: 'لیست ارکسترها',
    description: 'مرور و جستجوی ارکسترهای ثبت‌شده در آرشیو.',
    icon: LibraryBig,
  },
  {
    to: '/instruments',
    title: 'لیست سازها',
    description: 'فهرست کامل سازها و میزان استفاده از آن‌ها در برنامه‌ها.',
    icon: Music4,
  },
  {
    to: '/modes',
    title: 'لیست دستگاه‌ها',
    description: 'مرور دستگاه‌ها و آوازها برای کنترل داده‌های موسیقایی.',
    icon: Workflow,
  },
]

export const Route = createFileRoute('/database/')({
  component: DatabaseIndex,
})

type DatabaseReleasePayload = {
  ok: true
  dbUrl: string
  manifestUrl: string
  bucket: string
  endpoint: string
  fileName: string
  sizeBytes: number
  sha256: string
  releasedAt: string
  uploadOutput: string
}

function DatabaseIndex() {
  const [isReleasing, setIsReleasing] = useState(false)
  const [releaseResult, setReleaseResult] = useState<DatabaseReleasePayload | null>(null)
  const [releaseError, setReleaseError] = useState<string | null>(null)

  const onRelease = async () => {
    setIsReleasing(true)
    setReleaseError(null)
    setReleaseResult(null)

    try {
      const response = await fetch('/api/database/release', {
        method: 'POST',
      })
      const payload = await response.json()
      if (!response.ok) {
        throw new Error(payload?.error || 'Release failed')
      }
      setReleaseResult(payload as DatabaseReleasePayload)
    } catch (error: any) {
      setReleaseError(error?.message || 'Release failed')
    } finally {
      setIsReleasing(false)
    }
  }

  return (
    <div className="space-y-4 animate-in" dir="rtl">
      <section className="rounded-[1.8rem] border border-primary/10 bg-white/80 p-5 shadow-[0_18px_50px_rgba(31,78,95,0.06)] backdrop-blur-md">
        <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
          <div className="space-y-2 text-right">
            <h1 className="text-2xl font-black tracking-tight text-foreground">مدیریت داده‌ها</h1>
            <p className="text-[12px] font-bold text-muted-foreground">
              دسترسی سریع به فهرست ارکسترها، سازها و دستگاه‌های ثبت‌شده در آرشیو.
            </p>
          </div>

          <button
            type="button"
            onClick={onRelease}
            disabled={isReleasing}
            className="inline-flex h-11 items-center justify-center gap-2 rounded-full bg-primary px-5 text-[12px] font-black text-white transition-all hover:bg-primary/90 disabled:cursor-not-allowed disabled:opacity-65"
          >
            {isReleasing ? <Loader2 className="h-4 w-4 animate-spin" /> : <UploadCloud className="h-4 w-4" />}
            {isReleasing ? 'در حال ریلیز دیتابیس...' : 'ریلیز دیتابیس روی CDN'}
          </button>
        </div>
      </section>

      {(releaseResult || releaseError) && (
        <section className="rounded-[1.6rem] border border-primary/10 bg-white/85 p-5 shadow-[0_18px_45px_rgba(31,78,95,0.06)]">
          {releaseResult && (
            <div className="space-y-2 text-right">
              <div className="inline-flex items-center gap-2 rounded-full border border-emerald-500/20 bg-emerald-500/10 px-3 py-1 text-[11px] font-black text-emerald-700">
                <CheckCircle2 className="h-3.5 w-3.5" />
                ریلیز با موفقیت انجام شد
              </div>
              <p className="text-[12px] font-bold text-muted-foreground">
                باکت: <span className="font-black text-foreground">{releaseResult.bucket}</span>
              </p>
              <p className="text-[12px] font-bold text-muted-foreground">Endpoint: {releaseResult.endpoint}</p>
              <p className="text-[12px] font-bold text-muted-foreground">هش: {releaseResult.sha256}</p>
              <div className="space-y-1 text-[12px] font-bold">
                <a className="block text-primary underline underline-offset-4" href={releaseResult.dbUrl} target="_blank" rel="noreferrer">
                  لینک دیتابیس: {releaseResult.dbUrl}
                </a>
                <a className="block text-primary underline underline-offset-4" href={releaseResult.manifestUrl} target="_blank" rel="noreferrer">
                  لینک مانیفست: {releaseResult.manifestUrl}
                </a>
              </div>
            </div>
          )}

          {releaseError && (
            <div className="inline-flex items-center gap-2 rounded-full border border-red-500/20 bg-red-500/10 px-3 py-1 text-[11px] font-black text-red-700">
              <XCircle className="h-3.5 w-3.5" />
              {releaseError}
            </div>
          )}
        </section>
      )}

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {ITEMS.map((item) => (
          <Link
            key={item.to}
            to={item.to as '/orchestras' | '/instruments' | '/modes'}
            className="rounded-[1.6rem] border border-primary/10 bg-white/85 p-5 shadow-[0_18px_45px_rgba(31,78,95,0.06)] transition-all hover:-translate-y-0.5 hover:border-primary/20 hover:shadow-[0_18px_45px_rgba(31,78,95,0.1)]"
          >
            <div className="mb-4 flex items-center justify-between">
              <div className="flex h-12 w-12 items-center justify-center rounded-[1.3rem] bg-primary text-white shadow-lg shadow-primary/10">
                <item.icon className="h-5 w-5" />
              </div>
              <div className="text-right">
                <h2 className="text-base font-black text-foreground">{item.title}</h2>
              </div>
            </div>
            <p className="text-[12px] font-bold leading-6 text-muted-foreground">{item.description}</p>
          </Link>
        ))}
      </section>
    </div>
  )
}
