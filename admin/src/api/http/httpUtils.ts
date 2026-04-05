type ServerResponse = {
  statusCode: number
  setHeader(name: string, value: string): void
  end(body?: string): void
}

export function sendJson(res: ServerResponse, payload: unknown, statusCode: number = 200) {
  res.statusCode = statusCode
  res.setHeader('Content-Type', 'application/json')
  res.end(JSON.stringify(payload))
}

export function getIntParam(value: string | null, fallback: number) {
  const parsed = Number.parseInt(value || '', 10)
  return Number.isFinite(parsed) ? parsed : fallback
}

export function getIntListParam(value: string | null) {
  if (!value) return []
  return value
    .split(',')
    .map((item) => Number.parseInt(item.trim(), 10))
    .filter((item) => Number.isFinite(item) && item > 0)
}

export async function respondWithJson(
  res: ServerResponse,
  loader: () => Promise<unknown>,
  options?: { notFoundWhen?: (payload: unknown) => boolean }
) {
  try {
    const payload = await loader()
    if (options?.notFoundWhen?.(payload)) {
      sendJson(res, { error: 'Not found' }, 404)
      return
    }
    sendJson(res, payload)
  } catch (error: any) {
    sendJson(res, { error: error?.message || 'Unexpected error' }, 500)
  }
}

export type { ServerResponse }
