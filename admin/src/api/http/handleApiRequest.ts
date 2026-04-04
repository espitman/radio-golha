import { URL } from 'url'
import { ArtistService } from '../services/ArtistService'
import { DashboardService } from '../services/DashboardService'
import { LookupService } from '../services/LookupService'
import { ProgramService } from '../services/ProgramService'

type IncomingMessage = {
  method?: string
  url?: string
}

type ServerResponse = {
  statusCode: number
  setHeader(name: string, value: string): void
  end(body?: string): void
}

function sendJson(res: ServerResponse, payload: unknown, statusCode: number = 200) {
  res.statusCode = statusCode
  res.setHeader('Content-Type', 'application/json')
  res.end(JSON.stringify(payload))
}

export async function handleApiRequest(req: IncomingMessage, res: ServerResponse) {
  const url = new URL(req.url || '/', 'http://localhost')

  if (req.method === 'GET' && url.pathname === '/api/dashboard') {
    try {
      const data = await DashboardService.getOverview()
      sendJson(res, data)
    } catch (e: any) {
      sendJson(res, { error: e.message }, 500)
    }
    return true
  }

  if (req.method === 'GET' && url.pathname.startsWith('/api/programs')) {
    const search = url.searchParams.get('search') || ''
    const page = parseInt(url.searchParams.get('page') || '1')
    const categoryId = parseInt(url.searchParams.get('categoryId') || '0')
    const singerId = parseInt(url.searchParams.get('singerId') || '0')

    try {
      const data = await ProgramService.list(
        search,
        page,
        categoryId || undefined,
        singerId || undefined
      )
      sendJson(res, data)
    } catch (e: any) {
      sendJson(res, { error: e.message }, 500)
    }
    return true
  }

  if (req.method === 'GET' && url.pathname.startsWith('/api/artists')) {
    const search = url.searchParams.get('search') || ''
    const page = parseInt(url.searchParams.get('page') || '1')
    const role = url.searchParams.get('role') || undefined

    try {
      const data = await ArtistService.list(search, page, role)
      sendJson(res, data)
    } catch (e: any) {
      sendJson(res, { error: e.message }, 500)
    }
    return true
  }

  if (req.method === 'GET' && url.pathname.startsWith('/api/orchestras')) {
    const search = url.searchParams.get('search') || ''
    const page = parseInt(url.searchParams.get('page') || '1')

    try {
      const data = await LookupService.list('orchestras', search, page)
      sendJson(res, data)
    } catch (e: any) {
      sendJson(res, { error: e.message }, 500)
    }
    return true
  }

  if (req.method === 'GET' && url.pathname.startsWith('/api/instruments')) {
    const search = url.searchParams.get('search') || ''
    const page = parseInt(url.searchParams.get('page') || '1')

    try {
      const data = await LookupService.list('instruments', search, page)
      sendJson(res, data)
    } catch (e: any) {
      sendJson(res, { error: e.message }, 500)
    }
    return true
  }

  if (req.method === 'GET' && url.pathname.startsWith('/api/modes')) {
    const search = url.searchParams.get('search') || ''
    const page = parseInt(url.searchParams.get('page') || '1')

    try {
      const data = await LookupService.list('modes', search, page)
      sendJson(res, data)
    } catch (e: any) {
      sendJson(res, { error: e.message }, 500)
    }
    return true
  }

  if (req.method === 'GET' && url.pathname.startsWith('/api/program/')) {
    const id = url.pathname.split('/').pop()

    try {
      const data = await ProgramService.getDetail(parseInt(id || '0'))
      if (!data) {
        sendJson(res, { error: 'Not found' }, 404)
        return true
      }
      sendJson(res, data)
    } catch (e: any) {
      sendJson(res, { error: e.message }, 500)
    }
    return true
  }

  return false
}
