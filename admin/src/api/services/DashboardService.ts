import { runCoreQuery } from '../rust/runCoreQuery'

export class DashboardService {
  static async getOverview() {
    return runCoreQuery('admin-dashboard')
  }
}
