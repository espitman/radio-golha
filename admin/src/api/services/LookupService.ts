import { runCoreQuery } from '../rust/runCoreQuery'

export type LookupKind = 'orchestras' | 'instruments' | 'modes'

export function listLookupItems(kind: LookupKind, search: string = '', page: number = 1) {
  return runCoreQuery('admin-lookup', [kind, '--search', search, '--page', page.toString()])
}
