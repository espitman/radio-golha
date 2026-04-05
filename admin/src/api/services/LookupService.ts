import { rustCoreClient, type LookupKind } from '../rust/runCoreQuery'

export function listLookupItems(kind: LookupKind, search: string = '', page: number = 1) {
  return rustCoreClient.listLookupItems(kind, {
    search,
    page,
  })
}
