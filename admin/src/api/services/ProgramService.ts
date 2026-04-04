import { ProgramRepository } from '../repositories/ProgramRepository';

export class ProgramService {
  static async list(search: string = '', page: number = 1) {
    const repo = new ProgramRepository();
    try {
      const rows = await repo.list(search, page);
      const total = await repo.count(search);
      const limit = 24;
      return {
        rows,
        total,
        page,
        totalPages: Math.ceil((total as number) / limit)
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
