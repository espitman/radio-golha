import { rustCoreClient } from '../rust/runCoreQuery'

export function listArtists(search: string = '', page: number = 1, role?: string) {
  return rustCoreClient.listArtists({
    search,
    page,
    role,
  })
}
