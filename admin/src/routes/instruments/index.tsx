import { createFileRoute } from '@tanstack/react-router'
import { LookupListPage } from '@/components/LookupListPage'

export const Route = createFileRoute('/instruments/')({
  component: InstrumentsPage,
})

function InstrumentsPage() {
  return (
    <LookupListPage
      endpoint="/api/instruments"
      title="آرشیو سازها"
      description="فهرست سازهای ثبت‌شده در آرشیو برای مرور، جستجو و کنترل داده‌ها."
      singularLabel="ساز"
      usageLabel="تعداد استفاده"
    />
  )
}
