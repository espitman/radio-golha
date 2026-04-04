import { DashboardRepository } from '../repositories/DashboardRepository'

export class DashboardService {
  static async getOverview() {
    const repo = new DashboardRepository()

    try {
      const [
        summary,
        categoryBreakdown,
        topSingers,
        topModes,
        topOrchestras,
        recentPrograms,
      ] = await Promise.all([
        repo.summary(),
        repo.categoryBreakdown(),
        repo.topSingers(),
        repo.topModes(),
        repo.topOrchestras(),
        repo.recentPrograms(),
      ])

      return {
        summary,
        categoryBreakdown,
        topSingers,
        topModes,
        topOrchestras,
        recentPrograms,
      }
    } finally {
      repo.close()
    }
  }
}
