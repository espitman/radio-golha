import { rustCoreClient } from '../rust/runCoreQuery';

export function listPrograms(
  search: string = '',
  page: number = 1,
  categoryId?: number,
  singerId?: number,
  sortField: 'id' | 'no' | 'sub_no' | 'title' | 'category_name' = 'no',
  sortDirection: 'asc' | 'desc' = 'asc',
) {
  return rustCoreClient.listPrograms({
    search,
    page,
    categoryId,
    singerId,
    sortField,
    sortDirection,
  })
}

export function getProgramDetail(id: number) {
  return rustCoreClient.getProgramDetail(id)
}
