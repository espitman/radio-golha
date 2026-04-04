import { runCoreQuery } from '../rust/runCoreQuery';

export function listPrograms(search: string = '', page: number = 1, categoryId?: number, singerId?: number) {
  const args = ['--search', search, '--page', page.toString()];
  if (categoryId) args.push('--category-id', categoryId.toString());
  if (singerId) args.push('--singer-id', singerId.toString());
  return runCoreQuery('admin-programs', args);
}

export function getProgramDetail(id: number) {
  return runCoreQuery('admin-program-detail', [id.toString()])
}
