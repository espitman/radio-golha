import { URL } from 'url'
import { listArtists } from '../services/ArtistService'
import { getDashboardOverview } from '../services/DashboardService'
import { listLookupItems } from '../services/LookupService'
import { getProgramDetail, listPrograms } from '../services/ProgramService'
import { getProgramSearchOptions, searchPrograms } from '../services/SearchService'
import { getIntListParam, getIntParam, getMatchModeParam, respondWithJson } from './httpUtils'

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
    const allowedSortFields = new Set(['id', 'no', 'sub_no', 'title', 'category_name'])
    const sortField = allowedSortFields.has(url.searchParams.get('sortField') || '')
      ? (url.searchParams.get('sortField') as 'id' | 'no' | 'sub_no' | 'title' | 'category_name')
      : 'no'
    const sortDirection = url.searchParams.get('sortDirection') === 'desc' ? 'desc' : 'asc'

    await respondWithJson(res, () =>
      searchPrograms({
        page,
        transcriptQuery,
        categoryIds: getIntListParam(url.searchParams.get('categoryIds')),
        modeIds: getIntListParam(url.searchParams.get('modeIds')),
        modeMatch: getMatchModeParam(url.searchParams.get('modeMatch')),
        orchestraIds: getIntListParam(url.searchParams.get('orchestraIds')),
        orchestraMatch: getMatchModeParam(url.searchParams.get('orchestraMatch')),
        instrumentIds: getIntListParam(url.searchParams.get('instrumentIds')),
        instrumentMatch: getMatchModeParam(url.searchParams.get('instrumentMatch')),
        singerIds: getIntListParam(url.searchParams.get('singerIds')),
        singerMatch: getMatchModeParam(url.searchParams.get('singerMatch')),
        poetIds: getIntListParam(url.searchParams.get('poetIds')),
        poetMatch: getMatchModeParam(url.searchParams.get('poetMatch')),
        announcerIds: getIntListParam(url.searchParams.get('announcerIds')),
        announcerMatch: getMatchModeParam(url.searchParams.get('announcerMatch')),
        composerIds: getIntListParam(url.searchParams.get('composerIds')),
        composerMatch: getMatchModeParam(url.searchParams.get('composerMatch')),
        arrangerIds: getIntListParam(url.searchParams.get('arrangerIds')),
        arrangerMatch: getMatchModeParam(url.searchParams.get('arrangerMatch')),
        performerIds: getIntListParam(url.searchParams.get('performerIds')),
        performerMatch: getMatchModeParam(url.searchParams.get('performerMatch')),
        orchestraLeaderIds: getIntListParam(url.searchParams.get('orchestraLeaderIds')),
        orchestraLeaderMatch: getMatchModeParam(url.searchParams.get('orchestraLeaderMatch')),
        sortField,
        sortDirection,
      }),
    )
    return true
  }

  if (req.method === 'GET' && url.pathname.startsWith('/api/programs')) {
    const search = url.searchParams.get('search') || ''
    const page = getIntParam(url.searchParams.get('page'), 1)
    const categoryId = getIntParam(url.searchParams.get('categoryId'), 0)
    const singerId = getIntParam(url.searchParams.get('singerId'), 0)
    const allowedSortFields = new Set(['id', 'no', 'sub_no', 'title', 'category_name'])
    const sortField = allowedSortFields.has(url.searchParams.get('sortField') || '')
      ? (url.searchParams.get('sortField') as 'id' | 'no' | 'sub_no' | 'title' | 'category_name')
      : 'no'
    const sortDirection = url.searchParams.get('sortDirection') === 'desc' ? 'desc' : 'asc'

    await respondWithJson(res, () =>
      listPrograms(
        search,
        page,
        categoryId || undefined,
        singerId || undefined,
        sortField,
        sortDirection,
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
