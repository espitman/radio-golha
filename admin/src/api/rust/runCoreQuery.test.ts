// @vitest-environment node

import { describe, expect, it } from 'vitest'
import { rustCoreClient } from './runCoreQuery'

describe('RustCoreClient', () => {
  it('loads dashboard data from the native addon', async () => {
    const payload = await rustCoreClient.getDashboardOverview<{
      summary: { totalPrograms: number }
      topModes: Array<{ name: string }>
    }>()

    expect(payload.summary.totalPrograms).toBeGreaterThan(1000)
    expect(payload.topModes.length).toBeGreaterThan(0)
  })

  it('loads a real program detail from the native addon', async () => {
    const payload = await rustCoreClient.getProgramDetail<{
      id: number
      title: string
      timeline: unknown[]
      transcript: Array<{ text: string }>
    } | null>(1251)

    expect(payload).not.toBeNull()
    expect(payload?.id).toBe(1251)
    expect(payload?.title).toContain('گلهای رنگارنگ')
    expect(payload?.timeline.length).toBeGreaterThan(0)
    expect(payload?.transcript.length).toBeGreaterThan(0)
  })
})
