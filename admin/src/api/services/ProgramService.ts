import { ProgramRepository } from '../repositories/ProgramRepository';
import { runCoreQuery } from '../rust/runCoreQuery';

export class ProgramService {
  static async list(search: string = '', page: number = 1, categoryId?: number, singerId?: number) {
    const args = ['--search', search, '--page', page.toString()];
    if (categoryId) args.push('--category-id', categoryId.toString());
    if (singerId) args.push('--singer-id', singerId.toString());
    return runCoreQuery('admin-programs', args);
  }

  static async getDetail(id: number) {
    const repo = new ProgramRepository();
    try {
      const detail = await repo.getDetail(id);
      return detail;
    } finally {
      repo.close();
    }
  }
}
