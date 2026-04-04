import { createFileRoute, Link } from '@tanstack/react-router'
import { LibraryBig, Music4, Workflow } from 'lucide-react'

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

function DatabaseIndex() {
  return (
    <div className="space-y-4 animate-in" dir="rtl">
      <section className="rounded-[1.8rem] border border-primary/10 bg-white/80 p-5 shadow-[0_18px_50px_rgba(31,78,95,0.06)] backdrop-blur-md">
        <div className="space-y-2 text-right">
          <h1 className="text-2xl font-black tracking-tight text-foreground">مدیریت داده‌ها</h1>
          <p className="text-[12px] font-bold text-muted-foreground">
            دسترسی سریع به فهرست ارکسترها، سازها و دستگاه‌های ثبت‌شده در آرشیو.
          </p>
        </div>
      </section>

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
