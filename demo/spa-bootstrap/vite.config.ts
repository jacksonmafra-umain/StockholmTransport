import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// The KMP library is consumed as the SAME `file:` package the Node demo imports.
// The directory it lives in is still build/js/packages/StockholmTransport-
// stockholm-transport (Kotlin/JS pipeline output), but its package.json now
// advertises the scoped npm name `@umain/stockholm-transport` (after the
// :stockholm-transport:enhanceNpmPackageMetadata Gradle task polishes the
// auto-generated file). Vite imports by that scoped name.
//
// Kotlin/JS emits the bundle as ESM (`main` is a `.mjs`, all-relative imports)
// thanks to useEsModules(), so Vite consumes it natively — no CommonJS interop.
//
// We force the linked package through the dependency optimizer: Vite skips
// pre-bundling linked (file:) deps by default, so without this the package's
// internal .mjs graph isn't served correctly in dev. This is the only
// browser-specific wiring in the whole demo.
export default defineConfig({
  plugins: [react()],
  optimizeDeps: {
    include: ['@umain/stockholm-transport'],
  },
  server: {
    port: 5173,
    open: true,
  },
})
