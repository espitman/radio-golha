import { defineConfig } from 'vite'
import { devtools } from '@tanstack/devtools-vite'
import tsconfigPaths from 'vite-tsconfig-paths'
import { tanstackRouter } from '@tanstack/router-plugin/vite'
import viteReact from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { handleApiRequest } from './src/api/http/handleApiRequest'

export default defineConfig({
  plugins: [
    devtools(),
    tsconfigPaths({ projects: ['./tsconfig.json'] }),
    tailwindcss(),
    tanstackRouter({ target: 'react', autoCodeSplitting: true }),
    viteReact(),
    {
      name: 'sqlite-api',
      configureServer(server) {
        server.middlewares.use(async (req, res, next) => {
          if (await handleApiRequest(req, res)) {
            return
          }
          next();
        });
      }
    }
  ],
  server: {
    port: 3336,
  },
})
