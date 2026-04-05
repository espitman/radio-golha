import { URL } from 'url'
import { listArtists } from '../services/ArtistService'
import { getDashboardOverview } from '../services/DashboardService'
import { listLookupItems } from '../services/LookupService'
import { getProgramDetail, listPrograms } from '../services/ProgramService'
import { getProgramSearchOptions, searchPrograms } from '../services/SearchService'
import { getIntListParam, getIntParam, respondWithJson } from './httpUtils'

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

  if (req.method === 'GET' && url.pathname === '/api/program-search/options') {
    await respondWithJson(res, () => getProgramSearchOptions())
    return true
  }

  if (req.method === 'GET' && url.pathname === '/api/program-search') {
    const page = getIntParam(url.searchParams.get('page'), 1)
    const transcriptQuery = url.searchParams.get('transcriptQuery') || undefined

    await respondWithJson(res, () =>
      searchPrograms({
        page,
        transcriptQuery,
        categoryIds: getIntListParam(url.searchParams.get('categoryIds')),
        modeIds: getIntListParam(url.searchParams.get('modeIds')),
        orchestraIds: getIntListParam(url.searchParams.get('orchestraIds')),
        instrumentIds: getIntListParam(url.searchParams.get('instrumentIds')),
        singerIds: getIntListParam(url.searchParams.get('singerIds')),
        poetIds: getIntListParam(url.searchParams.get('poetIds')),
        announcerIds: getIntListParam(url.searchParams.get('announcerIds')),
        composerIds: getIntListParam(url.searchParams.get('composerIds')),
        arrangerIds: getIntListParam(url.searchParams.get('arrangerIds')),
        performerIds: getIntListParam(url.searchParams.get('performerIds')),
        orchestraLeaderIds: getIntListParam(url.searchParams.get('orchestraLeaderIds')),
      }),
    )
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
