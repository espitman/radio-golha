import { createFileRoute } from '@tanstack/react-router'
import { LookupListPage } from '@/components/LookupListPage'

export const Route = createFileRoute('/orchestras/')({
  component: OrchestrasPage,
})

function OrchestrasPage() {
  return (
    <LookupListPage
      endpoint="/api/orchestras"
      title="آرشیو ارکسترها"
      description="فهرست ارکسترهای ثبت‌شده در آرشیو برای مرور و کنترل داده‌ها."
      singularLabel="ارکستر"
      usageLabel="تعداد برنامه"
    />
  )
}
