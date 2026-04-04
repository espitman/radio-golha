import { URL } from 'url'
import { listArtists } from '../services/ArtistService'
import { getDashboardOverview } from '../services/DashboardService'
import { listLookupItems } from '../services/LookupService'
import { getProgramDetail, listPrograms } from '../services/ProgramService'
import { getIntParam, respondWithJson } from './httpUtils'

type IncomingMessage = {
  method?: string
  url?: string
}

type ServerResponse = import('./httpUtils').ServerResponse

export async function handleApiRequest(req: IncomingMessage, res: ServerResponse) {
  const url = new URL(req.url || '/', 'http://localhost')

  if (req.method === 'GET' && url.pathname === '/api/dashboard') {
    await respondWithJson(res, () => getDashboardOverview())
    return true
  }

  if (req.method === 'GET' && url.pathname.startsWith('/api/programs')) {
    const search = url.searchParams.get('search') || ''
    const page = getIntParam(url.searchParams.get('page'), 1)
    const categoryId = getIntParam(url.searchParams.get('categoryId'), 0)
    const singerId = getIntParam(url.searchParams.get('singerId'), 0)

    await respondWithJson(res, () =>
      listPrograms(
        search,
        page,
        categoryId || undefined,
        singerId || undefined
      )
    )
    return true
  }

  if (req.method === 'GET' && url.pathname.startsWith('/api/artists')) {
    const search = url.searchParams.get('search') || ''
    const page = getIntParam(url.searchParams.get('page'), 1)
    const role = url.searchParams.get('role') || undefined

    await respondWithJson(res, () => listArtists(search, page, role))
    return true
  }

  if (req.method === 'GET' && url.pathname.startsWith('/api/orchestras')) {
    const search = url.searchParams.get('search') || ''
    const page = getIntParam(url.searchParams.get('page'), 1)

    await respondWithJson(res, () => listLookupItems('orchestras', search, page))
    return true
  }

  if (req.method === 'GET' && url.pathname.startsWith('/api/instruments')) {
    const search = url.searchParams.get('search') || ''
    const page = getIntParam(url.searchParams.get('page'), 1)

    await respondWithJson(res, () => listLookupItems('instruments', search, page))
    return true
  }

  if (req.method === 'GET' && url.pathname.startsWith('/api/modes')) {
    const search = url.searchParams.get('search') || ''
    const page = getIntParam(url.searchParams.get('page'), 1)

    await respondWithJson(res, () => listLookupItems('modes', search, page))
    return true
  }

  if (req.method === 'GET' && url.pathname.startsWith('/api/program/')) {
    const id = getIntParam(url.pathname.split('/').pop() || null, 0)
    await respondWithJson(res, () => getProgramDetail(id), {
      notFoundWhen: (payload) => payload == null,
    })
    return true
  }

  return false
}
