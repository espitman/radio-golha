import { createFileRoute } from '@tanstack/react-router'
import { useEffect, useMemo, useState } from 'react'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Separator } from '@/components/ui/separator'
import { Braces, FileJson2, Network, Play, TerminalSquare, Waves } from 'lucide-react'
import { API_ENDPOINTS, type ApiEndpointContract } from '@/features/api-lab/contracts'

export const Route = createFileRoute('/api-lab/')({
  component: ApiLabPage,
})

function buildRequestUrl(endpoint: ApiEndpointContract, values: Record<string, string>) {
  let path = endpoint.pathTemplate

  for (const field of endpoint.fields.filter((item) => item.location === 'path')) {
    path = path.replace(`:${field.key}`, values[field.key] || '')
  }

  const query = new URLSearchParams()
  for (const field of endpoint.fields.filter((item) => item.location === 'query')) {
    const value = values[field.key] ?? ''
    if (value !== '') query.set(field.key, value)
  }

  const queryString = query.toString()
  return queryString ? `${path}?${queryString}` : path
}

function createInitialValues(endpoint: ApiEndpointContract) {
  return Object.fromEntries(endpoint.fields.map((field) => [field.key, field.defaultValue]))
}

function ApiLabPage() {
  const [selectedId, setSelectedId] = useState(API_ENDPOINTS[0]?.id ?? '')
  const selected = useMemo(
    () => API_ENDPOINTS.find((endpoint) => endpoint.id === selectedId) ?? API_ENDPOINTS[0],
    [selectedId]
  )

  const [values, setValues] = useState<Record<string, string>>(() => createInitialValues(API_ENDPOINTS[0]))
  const [status, setStatus] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)
  const [responseText, setResponseText] = useState('')
  const [errorText, setErrorText] = useState('')
  const [lastRunUrl, setLastRunUrl] = useState('')
  const [lastDurationMs, setLastDurationMs] = useState<number | null>(null)

  useEffect(() => {
    if (!selected) return
    setValues(createInitialValues(selected))
    setStatus(null)
    setResponseText('')
    setErrorText('')
    setLastRunUrl('')
    setLastDurationMs(null)
  }, [selected])

  if (!selected) return null

  const currentUrl = buildRequestUrl(selected, values)

  const runRequest = async () => {
    const startedAt = performance.now()
    setLoading(true)
    setErrorText('')
    setStatus(null)
    setLastRunUrl(currentUrl)

    try {
      const response = await fetch(currentUrl)
      const text = await response.text()
      setLastDurationMs(Math.round(performance.now() - startedAt))
      setStatus(response.status)
      try {
        const parsed = JSON.parse(text)
        setResponseText(JSON.stringify(parsed, null, 2))
      } catch {
        setResponseText(text)
      }
      if (!response.ok) {
        setErrorText(`HTTP ${response.status}`)
      }
    } catch (error: any) {
      setStatus(null)
      setResponseText('')
      setErrorText(error?.message || 'Request failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-4 animate-in" dir="ltr">
      <section className="rounded-[1.8rem] border border-primary/10 bg-white/80 p-5 shadow-[0_18px_50px_rgba(31,78,95,0.06)] backdrop-blur-md">
        <div className="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
          <div className="space-y-2 text-left">
            <div className="flex items-center justify-start gap-3">
              <div className="flex h-12 w-12 items-center justify-center rounded-[1.4rem] bg-primary text-white shadow-lg shadow-primary/10">
                <Braces className="h-6 w-6" />
              </div>
              <div>
                <h1 className="text-2xl font-black tracking-tight text-foreground">API Lab</h1>
                <p className="text-[12px] font-bold text-muted-foreground">
                  Browse Rust core bindings, inspect request and response DTOs, and run live Admin endpoints.
                </p>
              </div>
            </div>
          </div>

          <div className="flex items-center justify-start gap-2">
            <Badge className="rounded-full border border-primary/15 bg-primary/5 px-3 py-1 text-[10px] font-black text-primary">
              {API_ENDPOINTS.length} endpoint
            </Badge>
            <Badge className="rounded-full border-none bg-primary px-3 py-1 text-[10px] font-black text-white">
              Rust Core Contract
            </Badge>
          </div>
        </div>

        <div className="mt-4 rounded-[1.4rem] border border-primary/10 bg-background/70 p-4 text-left">
          <div className="mb-2 text-[10px] font-black uppercase tracking-[0.24em] text-primary/45">Transport Note</div>
          <p className="text-[11px] font-bold leading-6 text-muted-foreground">
            The browser still talks to local <span className="font-mono text-primary">/api/*</span> routes.
            This is not a separate backend service; it is a local adapter so the browser can reach the Node-side
            <span className="font-mono text-primary"> napi-rs </span> addon and the shared Rust core.
          </p>
        </div>
      </section>

      <div className="grid gap-5 xl:grid-cols-[320px_minmax(0,1fr)]">
        <Card className="!pt-0 overflow-hidden rounded-[1.8rem] border-primary/10 bg-white/85 shadow-[0_18px_45px_rgba(31,78,95,0.06)]">
          <CardHeader className="border-b border-primary/8 bg-primary/[0.03] px-5 py-4">
            <CardTitle className="text-left text-sm font-black text-primary">Catalog</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 p-3">
            {API_ENDPOINTS.map((endpoint) => {
              const active = endpoint.id === selected.id
              return (
                <button
                  key={endpoint.id}
                  type="button"
                  onClick={() => setSelectedId(endpoint.id)}
                  className={`w-full rounded-[1.2rem] border px-4 py-3 text-left transition-all ${
                    active
                      ? 'border-primary bg-primary text-white shadow-md shadow-primary/15'
                      : 'border-primary/10 bg-background/70 text-foreground hover:border-primary/20 hover:bg-white'
                  }`}
                >
                  <div className="mb-1 flex items-center justify-between gap-3">
                    <Badge
                      className={`rounded-full px-2.5 py-1 text-[9px] font-black ${
                        active ? 'border-none bg-white/15 text-white' : 'border border-primary/15 bg-primary/5 text-primary'
                      }`}
                    >
                      {endpoint.method}
                    </Badge>
                    <span className="text-[10px] font-black uppercase tracking-[0.24em] opacity-60">
                      {endpoint.group}
                    </span>
                  </div>
                  <div className="text-sm font-black">{endpoint.label}</div>
                  <div className={`mt-1 text-[10px] font-bold ${active ? 'text-white/75' : 'text-muted-foreground'}`}>
                    {endpoint.pathTemplate}
                  </div>
                </button>
              )
            })}
          </CardContent>
        </Card>

        <div className="space-y-5">
          <Card className="!pt-0 overflow-hidden rounded-[1.8rem] border-primary/10 bg-white/85 shadow-[0_18px_45px_rgba(31,78,95,0.06)]">
            <CardHeader className="border-b border-primary/8 bg-primary/[0.03] px-5 py-4">
              <div className="flex items-center justify-between gap-4">
                <div className="text-left">
                  <div className="text-[10px] font-black uppercase tracking-[0.28em] text-primary/40">Endpoint</div>
                  <CardTitle className="mt-1 text-left text-base font-black text-primary">{selected.label}</CardTitle>
                </div>
                <Badge className="rounded-full border-none bg-primary px-3 py-1 text-[10px] font-black text-white">
                  {selected.method}
                </Badge>
              </div>
            </CardHeader>

            <CardContent className="space-y-5 p-5">
              <div className="grid gap-4 lg:grid-cols-[minmax(0,1.1fr)_minmax(0,0.9fr)]">
                <div className="space-y-4">
                  <div className="rounded-[1.4rem] border border-primary/10 bg-background/70 p-4 text-left">
                    <div className="mb-2 text-[10px] font-black uppercase tracking-[0.24em] text-primary/45">HTTP Route</div>
                    <div className="font-mono text-sm font-black text-primary">{selected.pathTemplate}</div>
                    <p className="mt-2 text-[11px] font-bold leading-6 text-muted-foreground">{selected.description}</p>
                  </div>

                  <div className="rounded-[1.4rem] border border-primary/10 bg-background/70 p-4 text-left">
                    <div className="mb-2 flex items-center justify-between">
                      <span className="text-[10px] font-black uppercase tracking-[0.24em] text-primary/45">Admin Bridge</span>
                      <TerminalSquare className="h-4 w-4 text-primary/55" />
                    </div>
                    <pre className="overflow-x-auto whitespace-pre-wrap break-words rounded-[1rem] bg-[#102a33] p-3 text-left text-[11px] font-bold text-[#d6edf2]">{selected.bridgeMethod}</pre>
                  </div>

                  <div className="rounded-[1.4rem] border border-primary/10 bg-background/70 p-4 text-left">
                    <div className="mb-2 flex items-center justify-between">
                      <span className="text-[10px] font-black uppercase tracking-[0.24em] text-primary/45">Native Addon</span>
                      <Network className="h-4 w-4 text-primary/55" />
                    </div>
                    <pre className="overflow-x-auto whitespace-pre-wrap break-words rounded-[1rem] bg-[#102a33] p-3 text-left text-[11px] font-bold text-[#d6edf2]">{selected.nativeFunction}</pre>
                  </div>
                </div>

                <div className="rounded-[1.4rem] border border-primary/10 bg-background/70 p-4">
                  <div className="mb-4 flex items-center justify-between">
                    <button
                      type="button"
                      onClick={runRequest}
                      disabled={loading}
                      className="inline-flex items-center gap-2 rounded-full bg-primary px-4 py-2 text-[11px] font-black text-white transition-all hover:bg-primary/90 disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      <Play className="h-3.5 w-3.5" />
                      {loading ? 'Running...' : 'Run'}
                    </button>
                    <div className="text-left">
                      <div className="text-[10px] font-black uppercase tracking-[0.24em] text-primary/45">Live Request</div>
                      <div className="text-[11px] font-bold text-muted-foreground">Change parameters and execute the endpoint.</div>
                    </div>
                  </div>

                  {selected.fields.length === 0 ? (
                    <div className="rounded-[1rem] border border-dashed border-primary/12 bg-white/70 px-4 py-5 text-left text-[11px] font-bold text-muted-foreground">
                      This endpoint has no input parameters.
                    </div>
                  ) : (
                    <div className="space-y-3">
                      {selected.fields.map((field) => (
                        <label key={field.key} className="block space-y-1.5 text-left">
                          <div className="flex items-center justify-between gap-3">
                            <span className="text-[11px] font-black text-foreground">
                              {field.label}
                              {field.required ? ' *' : ''}
                            </span>
                            <span className="text-[10px] font-black text-primary/65">{field.location}</span>
                          </div>

                          {field.type === 'enum' ? (
                            <select
                              value={values[field.key] ?? ''}
                              onChange={(event) => setValues((current) => ({ ...current, [field.key]: event.target.value }))}
                              className="h-11 w-full rounded-2xl border border-primary/10 bg-white px-4 text-sm font-bold text-left text-foreground shadow-none outline-none focus:border-primary/20"
                            >
                              {(field.options || []).map((option) => (
                                <option key={option.value || '__empty'} value={option.value}>
                                  {option.label}
                                </option>
                              ))}
                            </select>
                          ) : (
                            <Input
                              type={field.type === 'number' ? 'number' : 'text'}
                              value={values[field.key] ?? ''}
                              placeholder={field.placeholder}
                              onChange={(event) => setValues((current) => ({ ...current, [field.key]: event.target.value }))}
                              className="h-11 rounded-2xl border-primary/10 bg-white text-left text-sm font-bold shadow-none focus-visible:ring-primary/15"
                            />
                          )}

                          {field.help && <div className="text-[10px] font-bold text-muted-foreground">{field.help}</div>}
                        </label>
                      ))}
                    </div>
                  )}
                </div>
              </div>

              <div className="rounded-[1.4rem] border border-primary/10 bg-background/70 p-4 text-left">
                <div className="mb-2 text-[10px] font-black uppercase tracking-[0.24em] text-primary/45">Resolved Request</div>
                <div className="font-mono text-sm font-black text-primary">{currentUrl}</div>
                {lastRunUrl && (
                  <div className="mt-2 text-[11px] font-bold text-muted-foreground">
                    Last run: <span className="font-mono">{lastRunUrl}</span>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          <div className="grid gap-5 xl:grid-cols-2">
            <Card className="!pt-0 overflow-hidden rounded-[1.8rem] border-primary/10 bg-white/85 shadow-[0_18px_45px_rgba(31,78,95,0.06)]">
              <CardHeader className="border-b border-primary/8 bg-primary/[0.03] px-5 py-4">
                <CardTitle className="flex items-center justify-start gap-2 text-left text-sm font-black text-primary">
                  <FileJson2 className="h-4 w-4" />
                  Request DTO
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4 p-5">
                <pre className="overflow-x-auto whitespace-pre-wrap break-words rounded-[1.2rem] bg-[#102a33] p-4 text-left text-[11px] font-bold text-[#d6edf2]">{selected.requestDto}</pre>
                <Separator className="bg-primary/8" />
                <div>
                  <div className="mb-2 text-[10px] font-black uppercase tracking-[0.24em] text-primary/45">Example</div>
                  <pre className="overflow-x-auto whitespace-pre-wrap break-words rounded-[1.2rem] bg-background p-4 text-left text-[11px] font-bold text-foreground">{selected.requestExample}</pre>
                </div>
              </CardContent>
            </Card>

            <Card className="!pt-0 overflow-hidden rounded-[1.8rem] border-primary/10 bg-white/85 shadow-[0_18px_45px_rgba(31,78,95,0.06)]">
              <CardHeader className="border-b border-primary/8 bg-primary/[0.03] px-5 py-4">
                <CardTitle className="flex items-center justify-start gap-2 text-left text-sm font-black text-primary">
                  <Waves className="h-4 w-4" />
                  Response DTO
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4 p-5">
                <pre className="overflow-x-auto whitespace-pre-wrap break-words rounded-[1.2rem] bg-[#102a33] p-4 text-left text-[11px] font-bold text-[#d6edf2]">{selected.responseDto}</pre>
                <Separator className="bg-primary/8" />
                <div>
                  <div className="mb-2 text-[10px] font-black uppercase tracking-[0.24em] text-primary/45">Example</div>
                  <pre className="overflow-x-auto whitespace-pre-wrap break-words rounded-[1.2rem] bg-background p-4 text-left text-[11px] font-bold text-foreground">{selected.responseExample}</pre>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card className="!pt-0 overflow-hidden rounded-[1.8rem] border-primary/10 bg-white/85 shadow-[0_18px_45px_rgba(31,78,95,0.06)]">
            <CardHeader className="border-b border-primary/8 bg-primary/[0.03] px-5 py-4">
              <div className="flex items-center justify-between gap-4">
                <div className="text-left">
                  <div className="text-[10px] font-black uppercase tracking-[0.24em] text-primary/45">Live Output</div>
                  <CardTitle className="mt-1 text-left text-sm font-black text-primary">Real Response</CardTitle>
                </div>
                <div className="flex items-center gap-2">
                  {lastDurationMs !== null && (
                    <Badge className="rounded-full border border-primary/15 bg-primary/5 px-3 py-1 text-[10px] font-black text-primary">
                      {lastDurationMs}ms
                    </Badge>
                  )}
                  {status !== null && (
                    <Badge className={`rounded-full px-3 py-1 text-[10px] font-black ${status >= 200 && status < 300 ? 'border-none bg-emerald-600 text-white' : 'border-none bg-red-600 text-white'}`}>
                      HTTP {status}
                    </Badge>
                  )}
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-3 p-5">
              {errorText && (
                <div className="rounded-[1rem] border border-red-200 bg-red-50 px-4 py-3 text-left text-[11px] font-bold text-red-700">
                  {errorText}
                </div>
              )}
              <pre className="min-h-[320px] overflow-auto whitespace-pre-wrap break-words rounded-[1.2rem] bg-[#0e1f26] p-4 text-left text-[12px] font-bold text-[#d4eff5]">
                {responseText || 'No request has been executed yet.'}
              </pre>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
