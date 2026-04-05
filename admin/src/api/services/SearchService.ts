import { rustCoreClient } from '../rust/runCoreQuery'

export function getProgramSearchOptions() {
  return rustCoreClient.getProgramSearchOptions()
}

export function searchPrograms(params?: {
  transcriptQuery?: string
  page?: number
  categoryIds?: number[]
  modeIds?: number[]
  orchestraIds?: number[]
  instrumentIds?: number[]
  singerIds?: number[]
  poetIds?: number[]
  announcerIds?: number[]
  composerIds?: number[]
  arrangerIds?: number[]
  performerIds?: number[]
  orchestraLeaderIds?: number[]
}) {
  return rustCoreClient.searchPrograms(params)
}
