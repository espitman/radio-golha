import { ProgramRepository } from '../repositories/ProgramRepository';

export class ProgramService {
  static async list(search: string = '', page: number = 1, categoryId?: number) {
    const repo = new ProgramRepository();
    try {
      const rows = await repo.list(search, page, categoryId);
      const total = await repo.count(search, categoryId);
      const categories = await repo.categories();
      const limit = 24;
      return {
        rows,
        categories,
        total,
        page,
        totalPages: Math.ceil((total as number) / limit),
        activeCategoryId: categoryId || null,
      };
    } finally {
      repo.close();
    }
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
