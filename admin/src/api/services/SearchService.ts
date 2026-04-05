import { rustCoreClient } from '../rust/runCoreQuery'

export function getProgramSearchOptions() {
  return rustCoreClient.getProgramSearchOptions()
}

export function searchPrograms(params?: {
  transcriptQuery?: string
  page?: number
  categoryIds?: number[]
  modeIds?: number[]
  modeMatch?: 'any' | 'all'
  orchestraIds?: number[]
  orchestraMatch?: 'any' | 'all'
  instrumentIds?: number[]
  instrumentMatch?: 'any' | 'all'
  singerIds?: number[]
  singerMatch?: 'any' | 'all'
  poetIds?: number[]
  poetMatch?: 'any' | 'all'
  announcerIds?: number[]
  announcerMatch?: 'any' | 'all'
  composerIds?: number[]
  composerMatch?: 'any' | 'all'
  arrangerIds?: number[]
  arrangerMatch?: 'any' | 'all'
  performerIds?: number[]
  performerMatch?: 'any' | 'all'
  orchestraLeaderIds?: number[]
  orchestraLeaderMatch?: 'any' | 'all'
}) {
  return rustCoreClient.searchPrograms(params)
}
