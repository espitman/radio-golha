import { runCoreQuery } from '../rust/runCoreQuery'

type LookupKind = 'orchestras' | 'instruments' | 'modes'

export class LookupService {
  static async list(kind: LookupKind, search: string = '', page: number = 1) {
    return runCoreQuery('admin-lookup', [kind, '--search', search, '--page', page.toString()])
  }
}
