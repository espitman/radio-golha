import { createFileRoute } from '@tanstack/react-router'
import { LookupListPage } from '@/components/LookupListPage'

export const Route = createFileRoute('/modes/')({
  component: ModesPage,
})

function ModesPage() {
  return (
    <LookupListPage
      endpoint="/api/modes"
      title="آرشیو دستگاه‌ها"
      description="فهرست دستگاه‌ها و آوازهای ثبت‌شده در آرشیو برای جستجو و کنترل داده‌ها."
      singularLabel="دستگاه"
      usageLabel="تعداد استفاده"
    />
  )
}
