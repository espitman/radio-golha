import { ArtistRepository } from '../repositories/ArtistRepository'

export class ArtistService {
  static async list(search: string = '', page: number = 1, role?: string) {
    const repo = new ArtistRepository()
    try {
      const rows = await repo.list(search, page, role)
      const total = await repo.count(search, role)
      const stats = await repo.stats()
      const limit = 24
      return {
        rows,
        stats,
        total,
        page,
        totalPages: Math.ceil((total as number) / limit),
        activeRole: role || null,
      }
    } finally {
      repo.close()
    }
  }
}
