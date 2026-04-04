import { LookupRepository, type LookupKind } from '../repositories/LookupRepository'

export class LookupService {
  static async list(kind: LookupKind, search: string = '', page: number = 1) {
    const repo = new LookupRepository()
    try {
      const rows = await repo.list(kind, search, page)
      const total = await repo.count(kind, search)
      const stats = await repo.stats(kind)
      const limit = 24
      return {
        rows,
        stats,
        total,
        page,
        totalPages: Math.ceil((total as number) / limit),
      }
    } finally {
      repo.close()
    }
  }
}
