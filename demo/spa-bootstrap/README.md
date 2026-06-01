# `demo/spa-bootstrap` — the KMP library in a browser tab

A Vite + React + TypeScript single-page app that consumes the **exact same**
`@umain/stockholm-transport` JS package the Node demo imports — and the same
ViewModels Android and iOS bind to. There is **no business logic in this app**:
it subscribes to the library's ViewModels and renders their state.

This is the app behind the talk's Act 2 (the React hook + the memory-leak demo)
and the web third of the Act 3 "fix one line, three platforms" finale.

## What it shows

- **Five feature screens** — Lines, Sites, Departures (Slussen), Stop Points,
  Authorities — each one route, each `useStockholmTransport(factory, loader)`.
- **`useStockholmTransport`** ([src/hooks/useStockholmTransport.ts](src/hooks/useStockholmTransport.ts)) —
  the whole bridge in a dozen lines: `subscribe(setState)` on mount,
  `onCleared()` on unmount. It wraps the library's API verbatim; it adds nothing.
- **The Leak** ([src/views/LeakDemo.tsx](src/views/LeakDemo.tsx)) — two widgets that
  differ by one line. The header shows a live subscription counter: mount/unmount
  the leaky widget and it only climbs; the fixed widget snaps it back to flat.

## Prerequisites

The app depends on the built JS package via a `file:` path
(`../../build/js/packages/StockholmTransport-stockholm-transport` — the
directory the Kotlin/JS pipeline writes; the package metadata inside it
advertises the scoped npm name `@umain/stockholm-transport`), exactly like
the Node demo. So build the library first, from the repo root:

```bash
./sl start      # boot Node API + ngrok, bake the public URL into BuildConfig
./sl publish    # builds the JS bundle + polishes package.json + packs the .tgz
                #   → build/js/packages/StockholmTransport-stockholm-transport/
                #   → build/distributions/npm/umain-stockholm-transport-<v>.tgz
```

`./sl start` also runs the Node API, which is the `/v1` passthrough this app
calls (and which now sends CORS headers so the browser isn't blocked).

## Run

```bash
cd demo/spa-bootstrap
npm install
npm run dev        # http://localhost:5173
```

> **After every `./sl publish`, restart `npm run dev`.** The dev script runs
> `vite --force` so it re-optimizes the linked KMP package on each start —
> otherwise Vite serves a cached copy and you'd see stale library behaviour.
> If data still looks stale, make sure only one dev server is running (a stray
> `vite` holding :5173 sends new ones to :5174): `pkill -f vite` then `npm run dev`.

## Talk-day routes

| Route          | Slide | What to show                                              |
| -------------- | ----- | --------------------------------------------------------- |
| `/lines`       | 17    | Same metro lines the phones render, pulled by Kotlin in V8 |
| `/leak`        | 18    | Toggle the widgets; watch the live counter climb vs. flat  |

## Notes / sharp edges

- **ESM dep:** Kotlin/JS emits the package as ESM (`main` is a `.mjs`,
  `useEsModules()`), so Vite consumes it natively. The only wiring is
  `optimizeDeps.include` in [vite.config.ts](vite.config.ts), which forces the
  *linked* package and its bare `@js-joda/core` dep through Vite's optimizer.
- **`ws` is Node-only but unused in the browser:** the generated `package.json`
  lists `ws`, but it's never statically imported by the bundle — Ktor's JS client
  uses the browser's native WebSocket. The static-feature routes (which this demo
  uses) never touch it. (Realtime-over-WS in the browser would need verification.)
- **A Kotlin `List<T>` is NOT a JS array.** `state.lines` has no `.map`, no
  `.length`, no `[Symbol.iterator]`. Kotlin/JS exposes `asJsReadonlyArrayView()`
  to get a real array — see [src/kt.ts](src/kt.ts) (`ktArray`), which every view
  funnels its list through. This is the kind of interop seam the talk is about.
- **`@js-joda/core`** is a transitive dep of `kotlinx-datetime` that Vite's
  optimizer pulls in; it's listed in this demo's `package.json` so the dev server
  resolves it cleanly.
- **ngrok interstitial → localhost shim.** The library bundle has the public
  ngrok URL baked into `BuildConfig`. ngrok's *free-tier browser interstitial*
  answers browser requests with an HTML warning page that has **no CORS headers**,
  so the SPA's `fetch` is blocked before it reaches node-api. Since the browser is
  on the same machine as node-api, [src/devProxy.ts](src/devProxy.ts) rewrites any
  `*.ngrok(-free).app|io` origin to `http://localhost:3000` (imported first in
  `main.tsx`, before any KMP fetch). Mobile is untouched — it still uses the real
  tunnel. To keep the browser on the public URL instead, disable the interstitial
  (paid domain / ngrok traffic policy) or send `ngrok-skip-browser-warning`.
- **Types:** [src/types/kmp.d.ts](src/types/kmp.d.ts) hand-declares the package's
  surface as a TypeScript module fallback. The generated `package.json` now
  includes a `types` field (after `enhanceNpmPackageMetadata`), so once the
  consuming code is fully type-checked against the auto-generated `.d.mts` the
  shim can be deleted.
- **CORS:** handled by the Node demo's `/v1` proxy. If you point the library at a
  different upstream, make sure it sends `Access-Control-Allow-Origin`.
