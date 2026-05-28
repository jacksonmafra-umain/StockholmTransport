import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
// Static SPA. No KMP package or special interop — just React + the cyberpunk
// theme tokens from the slide deck. Vercel auto-detects Vite; no extra config.
export default defineConfig({
    plugins: [react()],
    server: {
        port: 5174, // 5173 is taken by demo/spa-bootstrap
        open: true,
    },
});
