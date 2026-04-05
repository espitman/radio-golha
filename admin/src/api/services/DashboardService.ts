import { rustCoreClient } from '../rust/runCoreQuery'

export function getDashboardOverview() {
  return rustCoreClient.getDashboardOverview()
}
