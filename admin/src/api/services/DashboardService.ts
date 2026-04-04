import { runCoreQuery } from '../rust/runCoreQuery'

export function getDashboardOverview() {
  return runCoreQuery('admin-dashboard')
}
