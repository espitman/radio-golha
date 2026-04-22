import { createHash } from 'crypto'
import { existsSync, promises as fs } from 'fs'
import path from 'path'
import { ListBucketsCommand, PutObjectCommand, S3Client } from '@aws-sdk/client-s3'
import dotenv from 'dotenv'

const ADMIN_ROOT = process.cwd()
for (const envFile of ['.env.local', '.env']) {
  const fullPath = path.resolve(ADMIN_ROOT, envFile)
  if (existsSync(fullPath)) {
    dotenv.config({ path: fullPath, override: false })
  }
}

const REPO_ROOT = path.resolve(process.cwd(), '..')
const DB_SOURCE_PATH = path.resolve(REPO_ROOT, 'database/golha_database.db')
const RELEASE_DIR = path.resolve(REPO_ROOT, '.tmp/db-cdn-release')
const RELEASE_DB_FILENAME = 'golha_database.db'
const RELEASE_MANIFEST_FILENAME = 'database_manifest.json'
const DEFAULT_OBJECT_PREFIX = 'golha/db'

type ReleaseManifest = {
  fileName: string
  sha256: string
  sizeBytes: number
  releasedAt: string
}

export type DatabaseReleaseResult = {
  ok: true
  dbUrl: string
  manifestUrl: string
  bucket: string
  endpoint: string
  fileName: string
  sizeBytes: number
  sha256: string
  releasedAt: string
  uploadOutput: string
}

function getRequiredEnv(name: string): string {
  const value = process.env[name]?.trim()
  if (!value) {
    throw new Error(`Missing required env var: ${name}`)
  }
  return value
}

async function resolveBucketName(client: S3Client, explicitBucketName?: string): Promise<string> {
  if (explicitBucketName?.trim()) {
    return explicitBucketName.trim()
  }

  const bucketList = await client.send(new ListBucketsCommand({}))
  const names = (bucketList.Buckets || [])
    .map((item) => item.Name?.trim())
    .filter((value): value is string => Boolean(value))

  if (names.length === 1) {
    return names[0]
  }

  if (names.length === 0) {
    throw new Error('No bucket found for provided Liara object-storage key')
  }

  throw new Error('Multiple buckets found; please set LIARA_BUCKET_NAME explicitly')
}

function getPublicBaseUrl(endpoint: string, bucketName: string): string {
  const configured = process.env.LIARA_PUBLIC_BASE_URL?.trim()
  if (configured) {
    return configured.replace(/\/+$/, '')
  }
  const cleanEndpoint = endpoint.replace(/\/+$/, '')
  return `${cleanEndpoint}/${bucketName}`
}

function normalizePrefix(rawPrefix?: string): string {
  const prefix = (rawPrefix?.trim() || DEFAULT_OBJECT_PREFIX).replace(/^\/+|\/+$/g, '')
  return prefix
}

function withPrefix(prefix: string, fileName: string): string {
  return prefix ? `${prefix}/${fileName}` : fileName
}

async function prepareReleaseFiles(releasedAt: string): Promise<ReleaseManifest> {
  await fs.mkdir(RELEASE_DIR, { recursive: true })

  const sourceBuffer = await fs.readFile(DB_SOURCE_PATH)
  const sha256 = createHash('sha256').update(sourceBuffer).digest('hex')
  const releaseDbPath = path.resolve(RELEASE_DIR, RELEASE_DB_FILENAME)

  await fs.writeFile(releaseDbPath, sourceBuffer)

  const manifest: ReleaseManifest = {
    fileName: RELEASE_DB_FILENAME,
    sha256,
    sizeBytes: sourceBuffer.byteLength,
    releasedAt,
  }

  const manifestPath = path.resolve(RELEASE_DIR, RELEASE_MANIFEST_FILENAME)
  await fs.writeFile(manifestPath, JSON.stringify(manifest, null, 2), 'utf8')

  return manifest
}

export async function releaseDatabaseToLiaraCdn(): Promise<DatabaseReleaseResult> {
  const endpoint = (process.env.LIARA_ENDPOINT?.trim() || 'https://storage.iran.liara.space').replace(/\/+$/, '')
  const configuredBucketName = process.env.LIARA_BUCKET_NAME?.trim()
  const accessKeyId = getRequiredEnv('LIARA_ACCESS_KEY')
  const secretAccessKey = getRequiredEnv('LIARA_SECRET_KEY')

  const releasedAt = new Date().toISOString()
  const manifest = await prepareReleaseFiles(releasedAt)

  const client = new S3Client({
    region: 'default',
    endpoint,
    credentials: {
      accessKeyId,
      secretAccessKey,
    },
  })

  const bucketName = await resolveBucketName(client, configuredBucketName)
  const objectPrefix = normalizePrefix(process.env.LIARA_OBJECT_PREFIX)

  const dbBuffer = await fs.readFile(path.resolve(RELEASE_DIR, RELEASE_DB_FILENAME))
  const manifestBuffer = await fs.readFile(path.resolve(RELEASE_DIR, RELEASE_MANIFEST_FILENAME))
  const dbObjectKey = withPrefix(objectPrefix, RELEASE_DB_FILENAME)
  const manifestObjectKey = withPrefix(objectPrefix, RELEASE_MANIFEST_FILENAME)
  const latestObjectKey = withPrefix(objectPrefix, 'latest.json')

  await client.send(
    new PutObjectCommand({
      Bucket: bucketName,
      Key: dbObjectKey,
      Body: dbBuffer,
      ContentType: 'application/x-sqlite3',
    })
  )

  await client.send(
    new PutObjectCommand({
      Bucket: bucketName,
      Key: manifestObjectKey,
      Body: manifestBuffer,
      ContentType: 'application/json; charset=utf-8',
    })
  )

  const metadata = {
    sha256: manifest.sha256,
    releasedAt,
    source: 'admin-panel',
  }

  await client.send(
    new PutObjectCommand({
      Bucket: bucketName,
      Key: latestObjectKey,
      Body: Buffer.from(JSON.stringify(metadata, null, 2), 'utf8'),
      ContentType: 'application/json; charset=utf-8',
    })
  )

  const baseUrl = getPublicBaseUrl(endpoint, bucketName)
  const dbUrl = `${baseUrl}/${dbObjectKey}`
  const manifestUrl = `${baseUrl}/${manifestObjectKey}`

  const uploadOutput = JSON.stringify({
    uploaded: [dbObjectKey, manifestObjectKey, latestObjectKey],
    bucket: bucketName,
    endpoint,
    prefix: objectPrefix,
  })

  return {
    ok: true,
    dbUrl,
    manifestUrl,
    bucket: bucketName,
    endpoint,
    fileName: RELEASE_DB_FILENAME,
    sizeBytes: manifest.sizeBytes,
    sha256: manifest.sha256,
    releasedAt,
    uploadOutput,
  }
}
