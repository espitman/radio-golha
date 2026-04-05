import { rustCoreClient } from '../rust/runCoreQuery';

export function listPrograms(search: string = '', page: number = 1, categoryId?: number, singerId?: number) {
  return rustCoreClient.listPrograms({
    search,
    page,
    categoryId,
    singerId,
  })
}

export function getProgramDetail(id: number) {
  return rustCoreClient.getProgramDetail(id)
}
