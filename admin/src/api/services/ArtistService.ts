import { rustCoreClient } from '../rust/runCoreQuery'

export function listArtists(search: string = '', page: number = 1, role?: string) {
  return rustCoreClient.listArtists({
    search,
    page,
    role,
  })
}

export function getArtistDetail(id: number) {
  return rustCoreClient.getArtistDetail(id)
}

export function updateArtist(id: number, name: string, avatar?: string | null) {
  return rustCoreClient.updateArtist(id, name, avatar)
}
