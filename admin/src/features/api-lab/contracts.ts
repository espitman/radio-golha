export type ApiFieldType = 'string' | 'number' | 'enum'

export type ApiField = {
  key: string
  label: string
  type: ApiFieldType
  required?: boolean
  help?: string
  placeholder?: string
  defaultValue: string
  options?: Array<{ label: string; value: string }>
  location: 'query' | 'path'
}

export type ApiEndpointContract = {
  id: string
  label: string
  group: 'dashboard' | 'programs' | 'artists' | 'lookups'
  method: 'GET'
  pathTemplate: string
  rustCommand: string
  description: string
  fields: ApiField[]
  requestDto: string
  responseDto: string
  requestExample: string
  responseExample: string
}

export const API_ENDPOINTS: ApiEndpointContract[] = [
  {
    id: 'dashboard',
    label: 'Dashboard Overview',
    group: 'dashboard',
    method: 'GET',
    pathTemplate: '/api/dashboard',
    rustCommand: 'admin-dashboard',
    description: 'Loads the real dashboard aggregates, rankings, category breakdown, and recent programs.',
    fields: [],
    requestDto: 'No request DTO. Plain GET request with no query params.',
    responseDto: `type DashboardOverview = {
  summary: {
    totalPrograms: number
    totalArtists: number
    totalSegments: number
    totalModes: number
    programsWithAudio: number
    programsWithTimeline: number
    totalCategories: number
    totalOrchestras: number
    totalInstruments: number
  }
  categoryBreakdown: Array<{ name: string; total: number }>
  topSingers: Array<{ name: string; total: number }>
  topModes: Array<{ name: string; total: number }>
  topOrchestras: Array<{ name: string; total: number }>
  recentPrograms: Array<{
    id: number
    title: string
    category_name: string
    no: number
    sub_no: string | null
  }>
}`,
    requestExample: `GET /api/dashboard`,
    responseExample: `{
  "summary": {
    "totalPrograms": 1440,
    "totalArtists": 671,
    "totalSegments": 28577,
    "totalModes": 17,
    "programsWithAudio": 1440,
    "programsWithTimeline": 1395,
    "totalCategories": 6,
    "totalOrchestras": 2,
    "totalInstruments": 16
  },
  "categoryBreakdown": [
    { "name": "گلهای رنگارنگ", "total": 492 }
  ]
}`,
  },
  {
    id: 'programs',
    label: 'Programs List',
    group: 'programs',
    method: 'GET',
    pathTemplate: '/api/programs',
    rustCommand: 'admin-programs --search <search> --page <page> --category-id <categoryId> --singer-id <singerId>',
    description: 'Returns the paginated archive list plus singer/category filter options.',
    fields: [
      {
        key: 'search',
        label: 'Search',
        type: 'string',
        defaultValue: '',
        placeholder: 'Title, no, or sub_no',
        location: 'query',
      },
      {
        key: 'page',
        label: 'Page',
        type: 'number',
        defaultValue: '1',
        location: 'query',
      },
      {
        key: 'categoryId',
        label: 'Category ID',
        type: 'number',
        defaultValue: '',
        help: 'Optional category filter',
        location: 'query',
      },
      {
        key: 'singerId',
        label: 'Singer ID',
        type: 'number',
        defaultValue: '',
        help: 'Optional singer filter',
        location: 'query',
      },
    ],
    requestDto: `type ProgramsRequest = {
  search?: string
  page?: number
  categoryId?: number
  singerId?: number
}`,
    responseDto: `type ProgramsResponse = {
  rows: Array<{
    id: number
    title: string
    category_name: string
    no: number
    sub_no: string | null
  }>
  categories: Array<{ id: number; title_fa: string }>
  singers: Array<{ id: number; name: string }>
  total: number
  page: number
  totalPages: number
  activeCategoryId: number | null
  activeSingerId: number | null
}`,
    requestExample: `GET /api/programs?page=1&search=&categoryId=&singerId=`,
    responseExample: `{
  "rows": [
    {
      "id": 18,
      "title": "برگ سبز ۱",
      "category_name": "برگ سبز",
      "no": 1,
      "sub_no": null
    }
  ],
  "categories": [{ "id": 1, "title_fa": "گلهای جاویدان" }],
  "singers": [{ "id": 303, "name": "الهه" }]
}`,
  },
  {
    id: 'program-detail',
    label: 'Program Detail',
    group: 'programs',
    method: 'GET',
    pathTemplate: '/api/program/:id',
    rustCommand: 'admin-program-detail <id>',
    description: 'Loads the full metadata and timeline payload for a single program.',
    fields: [
      {
        key: 'id',
        label: 'Program ID',
        type: 'number',
        required: true,
        defaultValue: '1251',
        location: 'path',
      },
    ],
    requestDto: `type ProgramDetailRequest = {
  id: number
}`,
    responseDto: `type ProgramDetail = {
  id: number
  title: string
  category_name: string
  no: number
  sub_no: string | null
  audio_url: string | null
  singers: string[]
  poets: string[]
  announcers: string[]
  composers: string[]
  arrangers: string[]
  modes: string[]
  orchestras: string[]
  orchestra_leaders: Array<{ orchestra: string; name: string }>
  performers: Array<{ name: string; instrument: string | null }>
  timeline: Array<{
    id: number
    start_time: string | null
    end_time: string | null
    mode_name: string | null
    singers: string[]
    poets: string[]
    announcers: string[]
    orchestras: string[]
    orchestraLeaders: Array<{ orchestra: string; name: string }>
    performers: Array<{ name: string; instrument: string | null }>
  }>
} | null`,
    requestExample: `GET /api/program/1251`,
    responseExample: `{
  "id": 1251,
  "title": "گلهای رنگارنگ ۲۴۷",
  "category_name": "گلهای رنگارنگ",
  "orchestra_leaders": [
    { "orchestra": "ارکستر گل‌ها", "name": "جواد معروفی" }
  ]
}`,
  },
  {
    id: 'artists',
    label: 'Artists List',
    group: 'artists',
    method: 'GET',
    pathTemplate: '/api/artists',
    rustCommand: 'admin-artists --search <search> --page <page> --role <role>',
    description: 'Returns paginated artists plus aggregate role stats.',
    fields: [
      {
        key: 'search',
        label: 'Search',
        type: 'string',
        defaultValue: '',
        placeholder: 'Artist name',
        location: 'query',
      },
      {
        key: 'page',
        label: 'Page',
        type: 'number',
        defaultValue: '1',
        location: 'query',
      },
      {
        key: 'role',
        label: 'Role',
        type: 'enum',
        defaultValue: '',
        location: 'query',
        options: [
          { label: 'All roles', value: '' },
          { label: 'Singer', value: 'singer' },
          { label: 'Performer', value: 'performer' },
          { label: 'Poet', value: 'poet' },
          { label: 'Announcer', value: 'announcer' },
          { label: 'Composer', value: 'composer' },
          { label: 'Arranger', value: 'arranger' },
        ],
      },
    ],
    requestDto: `type ArtistsRequest = {
  search?: string
  page?: number
  role?: 'singer' | 'performer' | 'poet' | 'announcer' | 'composer' | 'arranger'
}`,
    responseDto: `type ArtistsResponse = {
  rows: Array<{
    id: number
    name: string
    is_singer: 0 | 1
    is_performer: 0 | 1
    is_poet: 0 | 1
    is_announcer: 0 | 1
    is_composer: 0 | 1
    is_arranger: 0 | 1
  }>
  stats: {
    total_artists: number
    singers: number
    performers: number
    poets: number
  }
  total: number
  page: number
  totalPages: number
  activeRole: string | null
}`,
    requestExample: `GET /api/artists?page=1&role=singer`,
    responseExample: `{
  "rows": [
    {
      "id": 303,
      "name": "الهه",
      "is_singer": 1,
      "is_performer": 0
    }
  ],
  "stats": {
    "total_artists": 671,
    "singers": 99
  }
}`,
  },
  {
    id: 'orchestras',
    label: 'Orchestras Lookup',
    group: 'lookups',
    method: 'GET',
    pathTemplate: '/api/orchestras',
    rustCommand: 'admin-lookup orchestras --search <search> --page <page>',
    description: 'Returns canonical orchestra rows and program counts.',
    fields: [
      {
        key: 'search',
        label: 'Search',
        type: 'string',
        defaultValue: '',
        location: 'query',
      },
      {
        key: 'page',
        label: 'Page',
        type: 'number',
        defaultValue: '1',
        location: 'query',
      },
    ],
    requestDto: `type LookupRequest = { search?: string; page?: number }`,
    responseDto: `type LookupResponse = {
  rows: Array<{ id: number; name: string; usage_count: number }>
  stats: { total_items: number; total_usage: number }
  total: number
  page: number
  totalPages: number
}`,
    requestExample: `GET /api/orchestras?page=1`,
    responseExample: `{
  "rows": [{ "id": 1, "name": "ارکستر گل‌ها", "usage_count": 1377 }],
  "stats": { "total_items": 2, "total_usage": 1440 }
}`,
  },
  {
    id: 'instruments',
    label: 'Instruments Lookup',
    group: 'lookups',
    method: 'GET',
    pathTemplate: '/api/instruments',
    rustCommand: 'admin-lookup instruments --search <search> --page <page>',
    description: 'Returns instruments and the number of programs using each one.',
    fields: [
      {
        key: 'search',
        label: 'Search',
        type: 'string',
        defaultValue: '',
        location: 'query',
      },
      {
        key: 'page',
        label: 'Page',
        type: 'number',
        defaultValue: '1',
        location: 'query',
      },
    ],
    requestDto: `type LookupRequest = { search?: string; page?: number }`,
    responseDto: `type LookupResponse = {
  rows: Array<{ id: number; name: string; usage_count: number }>
  stats: { total_items: number; total_usage: number }
  total: number
  page: number
  totalPages: number
}`,
    requestExample: `GET /api/instruments?page=1`,
    responseExample: `{
  "rows": [{ "id": 1, "name": "تار", "usage_count": 400 }],
  "stats": { "total_items": 16, "total_usage": 1800 }
}`,
  },
  {
    id: 'modes',
    label: 'Modes Lookup',
    group: 'lookups',
    method: 'GET',
    pathTemplate: '/api/modes',
    rustCommand: 'admin-lookup modes --search <search> --page <page>',
    description: 'Returns modes and distinct program counts for each one.',
    fields: [
      {
        key: 'search',
        label: 'Search',
        type: 'string',
        defaultValue: '',
        location: 'query',
      },
      {
        key: 'page',
        label: 'Page',
        type: 'number',
        defaultValue: '1',
        location: 'query',
      },
    ],
    requestDto: `type LookupRequest = { search?: string; page?: number }`,
    responseDto: `type LookupResponse = {
  rows: Array<{ id: number; name: string; usage_count: number }>
  stats: { total_items: number; total_usage: number }
  total: number
  page: number
  totalPages: number
}`,
    requestExample: `GET /api/modes?page=1`,
    responseExample: `{
  "rows": [{ "id": 1, "name": "سه گاه", "usage_count": 276 }],
  "stats": { "total_items": 17, "total_usage": 1840 }
}`,
  },
]
