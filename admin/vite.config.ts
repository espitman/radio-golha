import { defineConfig } from 'vite'
import { devtools } from '@tanstack/devtools-vite'
import tsconfigPaths from 'vite-tsconfig-paths'
import { tanstackRouter } from '@tanstack/router-plugin/vite'
import viteReact from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { URL } from 'url'
import { ProgramService } from './src/api/services/ProgramService' 

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
          const url = new URL(req.url!, 'http://localhost');
          
          // API: List Programs (Cleanly routed to Service)
          if (req.method === 'GET' && url.pathname.startsWith('/api/programs')) {
            const search = url.searchParams.get('search') || '';
            const page = parseInt(url.searchParams.get('page') || '1');
            const categoryId = parseInt(url.searchParams.get('categoryId') || '0');
            const singerId = parseInt(url.searchParams.get('singerId') || '0');
            try {
                const data = await ProgramService.list(search, page, categoryId || undefined, singerId || undefined);
                res.setHeader('Content-Type', 'application/json');
                return res.end(JSON.stringify(data));
            } catch (e: any) {
                res.statusCode = 500;
                return res.end(JSON.stringify({ error: e.message }));
            }
          }

          // API: Program Detailed (Cleanly routed to Service)
          if (req.method === 'GET' && url.pathname.startsWith('/api/program/')) {
            const id = url.pathname.split('/').pop();
            try {
                const data = await ProgramService.getDetail(parseInt(id!));
                if (!data) { res.statusCode = 404; return res.end(JSON.stringify({ error: "Not found" })); }
                res.setHeader('Content-Type', 'application/json');
                return res.end(JSON.stringify(data));
            } catch (e: any) {
                res.statusCode = 500;
                return res.end(JSON.stringify({ error: e.message }));
            }
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
