import { createFileRoute, useNavigate, Link } from '@tanstack/react-router'
import { useEffect, useState } from 'react'
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { 
  ArrowRight, 
  Save, 
  User, 
  Loader2, 
  CheckCircle2, 
  AlertCircle,
  ChevronLeft,
  ImagePlus
} from 'lucide-react'
import type { ArtistDetail } from '@/api/rust/runCoreQuery'

export const Route = createFileRoute('/artists/$id')({
  component: ArtistEdit,
})

function ArtistEdit() {
  const { id } = Route.useParams()
  const navigate = useNavigate()
  const [artist, setArtist] = useState<ArtistDetail | null>(null)
  const [name, setName] = useState('')
  const [avatarUrl, setAvatarUrl] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [isDragging, setIsDragging] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)

  useEffect(() => {
    setLoading(true)
    fetch(`/api/artist/${id}`)
      .then((res) => {
        if (!res.ok) throw new Error(`خطای ${res.status} در دریافت اطلاعات (آیدی: ${id})`)
        return res.json()
      })
      .then((data) => {
        if (!data) throw new Error('هنرمند در آرشیو یافت نشد')
        setArtist(data)
        setName(data.name)
        setAvatarUrl(data.avatar || '')
        setLoading(false)
      })
      .catch((err) => {
        setError(err.message)
        setLoading(false)
      })
  }, [id])

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!name.trim()) return

    setSaving(true)
    setError(null)
    setSuccess(false)

    try {
      const res = await fetch(`/api/artist/${id}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          name: name.trim(),
          avatar: avatarUrl.trim() || null
        }),
      })

      if (!res.ok) {
        const data = await res.json()
        throw new Error(data.error || 'خطا در ذخیره‌سازی')
      }

      setArtist(prev => prev ? ({ ...prev, name: name.trim(), avatar: avatarUrl.trim() || undefined }) : null)
      setSuccess(true)
      setTimeout(() => setSuccess(false), 3000)
    } catch (err: any) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    setUploading(true)
    setError(null)

    const formData = new FormData()
    formData.append('file', file)

    try {
      const res = await fetch('/api/upload', {
        method: 'POST',
        body: formData,
      })

      if (!res.ok) {
        const data = await res.json()
        throw new Error(data.error || 'خطا در آپلود تصویر')
      }

      const result = await res.json()
      console.log('[Upload] Server response:', result)
      
      const newUrl = result.url || result.data?.url
      if (newUrl) {
        setAvatarUrl(newUrl)
        console.log('[Upload] New avatar URL set:', newUrl)
      } else {
        throw new Error('فیلد آدرس در پاسخ سرور یافت نشد')
      }
    } catch (err: any) {
      setError(err.message)
    } finally {
      setUploading(false)
    }
  }

  const onDragOver = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragging(true)
  }

  const onDragLeave = () => {
    setIsDragging(false)
  }

  const onDrop = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragging(false)
    
    const file = e.dataTransfer.files?.[0]
    if (file && file.type.startsWith('image/')) {
      const input = document.getElementById('file-upload') as HTMLInputElement
      if (input) {
        const dataTransfer = new DataTransfer()
        dataTransfer.items.add(file)
        input.files = dataTransfer.files
        handleFileUpload({ target: input } as any)
      }
    }
  }

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center py-32 text-primary animate-in" dir="rtl">
        <Loader2 className="h-10 w-10 animate-spin opacity-40" />
        <p className="mt-4 text-sm font-bold opacity-60">در حال دریافت اطلاعات هنرمند...</p>
      </div>
    )
  }

  if (error && !artist) {
    return (
      <div className="p-12 text-center animate-in" dir="rtl">
        <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-[2rem] bg-destructive/5 text-destructive shadow-sm">
          <AlertCircle className="h-10 w-10" />
        </div>
        <h2 className="mt-6 text-2xl font-black text-foreground">مشکلی پیش آمد</h2>
        <p className="mt-2 text-sm font-medium text-muted-foreground opacity-80">{error}</p>
        <Button 
          variant="secondary" 
          className="mt-8 h-12 rounded-2xl font-black px-8"
          onClick={() => navigate({ to: '/artists' })}
        >
          <ArrowRight className="ml-2 h-4 w-4" />
          بازگشت به لیست هنرمندان
        </Button>
      </div>
    )
  }

  return (
    <div className="flex flex-col h-full gap-6 animate-in fade-in slide-in-from-bottom-3 duration-500 w-full" dir="rtl">
      {/* Header Section */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-3">
          <div className="flex h-14 w-14 items-center justify-center rounded-[1.4rem] bg-primary/10 text-primary shadow-inner">
            <User className="h-7 w-7" />
          </div>
          <div>
            <div className="flex items-center gap-2 text-[10px] font-black uppercase tracking-wider text-muted-foreground/60 mb-1">
              <Link to="/artists" className="hover:text-primary transition-colors">هنرمندان</Link>
              <ChevronLeft className="h-3 w-3" />
              <span>ویرایش جزئیات</span>
            </div>
            <h1 className="text-2xl font-black tracking-tight text-foreground">ویرایش: {artist?.name}</h1>
          </div>
        </div>
        
        <Button
          variant="ghost"
          size="sm"
          className="rounded-full font-black text-muted-foreground hover:bg-white hover:text-primary transition-all"
          onClick={() => navigate({ to: '/artists' })}
        >
          انصراف و بازگشت
          <ArrowRight className="mr-2 h-4 w-4" />
        </Button>
      </div>

      {/* Main Content Card */}
      <section className="rounded-[1.8rem] border border-primary/10 bg-white/80 backdrop-blur-md p-8 shadow-[0_18px_50px_rgba(31,78,95,0.06)]">
        <form onSubmit={handleSave} className="space-y-10">
          <div className="grid gap-10 lg:grid-cols-[1fr_240px]">
            {/* Fields Column */}
            <div className="space-y-8">
              <div className="space-y-4 text-right">
                <label htmlFor="name" className="block text-[13px] font-black text-primary/70 px-1">
                  نام هنرمند
                </label>
                <Input
                  id="name"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="مثلاً: محمدرضا شجریان"
                  className="h-16 rounded-[1.2rem] border-primary/15 bg-primary/[0.02] pr-5 text-xl font-black shadow-none focus-visible:ring-primary/20 ring-offset-background transition-all"
                />
              </div>

              <div className="space-y-4 text-right">
                <label htmlFor="avatar" className="block text-[13px] font-black text-primary/70 px-1">
                  آدرس تصویر (URL)
                </label>
                <div className="relative group">
                  <Input
                    id="avatar"
                    value={avatarUrl}
                    onChange={(e) => setAvatarUrl(e.target.value)}
                    placeholder="https://example.com/image.jpg"
                    dir="ltr"
                    className="h-16 rounded-[1.2rem] border-primary/15 bg-primary/[0.02] pr-5 text-lg font-mono shadow-none focus-visible:ring-primary/20 ring-offset-background transition-all"
                  />
                  {avatarUrl && (
                    <button 
                      type="button"
                      onClick={() => setAvatarUrl('')}
                      className="absolute left-4 top-1/2 -translate-y-1/2 h-8 w-8 rounded-xl bg-destructive/5 text-destructive opacity-0 group-hover:opacity-100 transition-all hover:bg-destructive hover:text-white"
                    >
                      ×
                    </button>
                  )}
                </div>
                <p className="px-2 text-[11px] font-bold text-muted-foreground/60 leading-relaxed">
                  لینک مستقیم به تصویر هنرمند جهت نمایش در اپلیکیشن.
                </p>
              </div>
            </div>

            {/* Avatar Preview & Upload Zone Column */}
            <div className="flex flex-col items-center gap-4">
              <div className="text-[13px] font-black text-primary/70 w-full text-center">
                پیش‌نمایش و آپلود
              </div>
              
              <div 
                className={`relative group aspect-square w-full max-w-[240px] overflow-hidden rounded-[2.2rem] border-4 transition-all duration-300 ${
                  isDragging 
                    ? 'border-primary bg-primary/10 scale-105 shadow-2xl' 
                    : 'border-white bg-primary/5 shadow-2xl shadow-primary/10'
                }`}
                onDragOver={onDragOver}
                onDragLeave={onDragLeave}
                onDrop={onDrop}
              >
                {/* Hidden File Input */}
                <input
                  type="file"
                  id="file-upload"
                  className="hidden"
                  accept="image/*"
                  onChange={handleFileUpload}
                  disabled={uploading}
                />

                {/* Current Image or Placeholder */}
                {avatarUrl ? (
                  <img 
                    key={avatarUrl}
                    src={avatarUrl} 
                    alt={name} 
                    className={`h-full w-full object-cover transition-all duration-500 ${isDragging ? 'blur-sm opacity-50' : 'animate-in fade-in zoom-in'}`}
                    onError={(e) => {
                      console.error('[Upload] Image preview error for URL:', avatarUrl);
                      const target = e.target as HTMLImageElement;
                      target.src = 'https://placehold.co/400x400/1f4e5f/ffffff?text=Error';
                    }}
                  />
                ) : (
                  <div className={`flex h-full w-full flex-col items-center justify-center transition-all ${isDragging ? 'opacity-20' : 'text-primary/20'}`}>
                    <User className="h-20 w-20" />
                    <span className="mt-2 text-[10px] font-black uppercase opacity-40">Drop Image Here</span>
                  </div>
                )}

                {/* Upload Overlay */}
                <div 
                  className={`absolute inset-0 flex flex-col items-center justify-center bg-black/40 backdrop-blur-[2px] transition-all duration-300 ${
                    uploading || isDragging ? 'opacity-100' : 'opacity-0 group-hover:opacity-100 cursor-pointer'
                  }`}
                  onClick={() => !uploading && document.getElementById('file-upload')?.click()}
                >
                  {uploading ? (
                    <div className="flex flex-col items-center gap-2 text-white">
                      <Loader2 className="h-10 w-10 animate-spin" />
                      <span className="text-[10px] font-black uppercase tracking-wider">Uploading...</span>
                    </div>
                  ) : (
                    <div className="flex flex-col items-center gap-2 text-white">
                      <div className="rounded-full bg-white/20 p-4 transition-transform group-hover:scale-110">
                        <ImagePlus className="h-8 w-8" />
                      </div>
                      <span className="text-[11px] font-black uppercase tracking-widest mt-2 px-4 py-1.5 bg-white/10 rounded-full border border-white/20">
                        {isDragging ? 'Release to Upload' : (avatarUrl ? 'Change Photo' : 'Upload Photo')}
                      </span>
                    </div>
                  )}
                </div>

                {/* Success Indicator Overlay */}
                {success && !uploading && (
                  <div className="absolute inset-0 flex items-center justify-center bg-teal-500/80 text-white animate-in zoom-in fade-in duration-300">
                    <CheckCircle2 className="h-16 w-16" />
                  </div>
                )}
              </div>

              <div className="text-[10px] font-black text-muted-foreground/40 uppercase tracking-tighter">
                Click or Drag & Drop (1:1 Aspect)
              </div>
            </div>
          </div>

          <div className="pt-8 border-t border-primary/5">
            <div className="flex flex-col gap-6 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex flex-wrap items-center gap-3 min-h-[44px]">
                {success && (
                  <div className="flex items-center gap-2 rounded-2xl bg-teal-50 px-5 py-2.5 text-[13px] font-black text-teal-600 animate-in fade-in zoom-in duration-300">
                    <CheckCircle2 className="h-4.5 w-4.5" />
                    تغییرات با موفقیت ذخیره شد
                  </div>
                )}
                {error && (
                  <div className="flex items-center gap-2 rounded-2xl bg-destructive/5 px-5 py-2.5 text-[13px] font-black text-destructive animate-in fade-in zoom-in duration-300">
                    <AlertCircle className="h-4.5 w-4.5" />
                    {error}
                  </div>
                )}
              </div>

              <Button 
                type="submit" 
                disabled={saving || !name.trim() || (name === artist?.name && avatarUrl === (artist?.avatar || ''))}
                className="h-16 min-w-[200px] rounded-[1.2rem] bg-primary px-8 text-[15px] font-black text-white shadow-xl shadow-primary/20 transition-all hover:scale-[1.02] active:scale-[0.98] disabled:opacity-30"
              >
                {saving ? (
                  <Loader2 className="ml-2 h-5 w-5 animate-spin" />
                ) : (
                  <Save className="ml-2 h-5 w-5" />
                )}
                ذخیره تغییرات نهایی
              </Button>
            </div>
          </div>
        </form>
      </section>

      {/* Footer Info Card */}
      <div className="rounded-[1.5rem] border border-primary/5 bg-primary/[0.03] p-6 text-right">
        <h3 className="text-[13px] font-black text-primary/80">راهنمای سیستم</h3>
        <ul className="mt-4 grid gap-3 sm:grid-cols-2">
          {[
            "ویرایش نام باعث به‌روزرسانی نام این هنرمند در تمام برنامه‌ها و تیتراژهای مرتبط می‌شود.",
            "از درج القاب یا توضیحات اضافی در کادر نام خودداری کنید.",
            "اگر قصد دارید هنرمند جدیدی بسازید، از دکمه «افزودن هنرمند» استفاده کنید.",
            "تغییرات بلافاصله در کل آرشیو (وب و موبایل) اعمال خواهد شد."
          ].map((text, i) => (
            <li key={i} className="flex items-start gap-2 text-[11px] font-bold text-muted-foreground/70">
              <span className="w-1.5 h-1.5 rounded-full bg-primary/20 shrink-0 mt-1.5"></span>
              <p className="flex-1">{text}</p>
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}
