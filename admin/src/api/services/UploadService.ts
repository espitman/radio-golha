import Busboy from 'busboy'
import type { IncomingMessage } from 'http'
import sharp from 'sharp'

// User provided API Key
const API_KEY = 'UC_OC0O10sXnVbS6IpTwaKhmzhKB5LhG7sMNv5BI'

export type UploadResult = {
  url: string
  thumbnail?: string
}

export async function uploadToImgLink(req: IncomingMessage): Promise<UploadResult> {
  return new Promise((resolve, reject) => {
    const busboy = Busboy({ headers: req.headers })
    let fileFound = false

    busboy.on('file', (name, file, info) => {
      if (name !== 'file') {
        file.resume()
        return
      }

      fileFound = true
      const { filename, mimeType } = info
      console.log(`[Upload] Input file: ${filename} (${mimeType})`)

      const chunks: Buffer[] = []
      file.on('data', (chunk) => chunks.push(chunk))
      file.on('end', async () => {
        try {
          console.log(`[Upload] Converting to WebP...`)
          
          // Convert to WebP using sharp with high quality (90)
          const imageBuffer = Buffer.concat(chunks)
          const webpBuffer = await sharp(imageBuffer)
            .webp({ quality: 90 })
            .toBuffer()

          const webpFilename = filename.replace(/\.[^/.]+$/, "") + ".webp"
          const webpMimeType = 'image/webp'

          console.log(`[Upload] WebP conversion done. Original: ${imageBuffer.length} bytes -> WebP: ${webpBuffer.length} bytes`)

          // Create FormData for the forward request
          const formData = new FormData()
          // Use Uint8Array to ensure compatibility with standard Blob in Node.js
          const blob = new Blob([new Uint8Array(webpBuffer)], { type: webpMimeType })
          formData.append('file', blob, webpFilename)

          // Using authenticated v1 API as requested
          console.log(`[Upload] Forwarding to authenticated imglink.cc v1 API...`)
          
          const response = await fetch('https://imglink.cc/api/v1/upload', {
            method: 'POST',
            body: formData,
            headers: {
              'X-API-Key': API_KEY,
              'Accept': 'application/json'
            }
          })

          if (!response.ok) {
            const errorText = await response.text()
            throw new Error(`ImgLink API error: ${response.status} ${errorText}`)
          }

          const result: any = await response.json()
          console.log(`[Upload] Successfully uploaded to v1: ${result.url}`)
          
          resolve({
            url: result.url,
            thumbnail: result.thumbnail
          })
        } catch (error: any) {
          console.error(`[Upload] Fatal error: ${error.message}`)
          reject(error)
        }
      })
    })

    busboy.on('error', (error) => reject(error))
    
    busboy.on('finish', () => {
      if (!fileFound) {
        reject(new Error('No file uploaded in the "file" field'))
      }
    })

    req.pipe(busboy)
  })
}
