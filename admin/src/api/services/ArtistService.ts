import { runCoreQuery } from '../rust/runCoreQuery'

export class ArtistService {
  static async list(search: string = '', page: number = 1, role?: string) {
    const args = ['--search', search, '--page', page.toString()]
    if (role) args.push('--role', role)
    return runCoreQuery('admin-artists', args)
  }
}
