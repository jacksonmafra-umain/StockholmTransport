# Presenter notes — "Taming the Web with Kotlin Multiplatform"

**Event:** mDevCamp 2026 — 2026-06-04 · 15:35–16:20 · Cross the rave
**Total time:** 35 min talk + ~10 min Q&A
**Deck:** [docs/Slides.pen](Slides.pen) — 23 slides
**Repo:** github.com/jacksonfdam

> **This is a full rewrite** for the rebuilt 23-slide deck. Structure: Opening (01–02) → Framing (03–05) → Act 1 The Dream (06–11) → Act 2 The Reality (12–18) → Act 3 The Payoff (19–23). Every entry: **slide title — target time**. Bullets are the *only* things to say (talk-shaped). Demo blocks are **bold** with concrete commands.

## Pacing budget (35 minutes hard cap)

| Section              | Slides | Mins |
| -------------------- | ------ | ---- |
| Opening              | 01–02  | 1.5  |
| Framing — the why    | 03–05  | 4    |
| Act 1 — The Dream    | 06–11  | 9    |
| Act 2 — The Reality  | 12–18  | 11   |
| Act 3 — The Payoff   | 19–23  | 9.5  |
| **Total**            | 23     | 35   |

The narrative is a single arc: **stop rebuilding the same logic** (framing) → **here's how KMP reaches JS** (Act 1) → **here's where it gets hard** (Act 2) → **here's what shipping really costs** (Act 3). Demos close each act.

## KotlinConf '26 context — framing only, not on slides

Verified against the official docs; shapes how you *answer*, not what you demo.

- **Compose Multiplatform Web is Beta** (`lp.jetbrains.com/cmp-for-web`, since Sept 2025).
- **Swift Export → Alpha in Kotlin 2.4** (keynote) — future replacement for the XCFramework + ObjC bridge. We still ship XCFramework. (A14)
- **AGP 9 disallows `com.android.application` in a KMP module** — the new official structure separates library + app modules; this repo uses bypass props in the demos. (A15)
- **KMP Stable since Nov 2023; Google endorsed it at I/O 2024.** Production users on kotlinlang.org: Cash App, McDonald's, Netflix, Forbes, 9GAG, Philips. Safe to cite.
- **klibs.io shows 3,600+ libraries** — citable. Per-library: filter `js`/`native`; Apple targets hide under the "Kotlin/Native" badge.
- Don't cite as hard fact: "+25% native build perf", "18-month security policy" — keynote-only.

---

# OPENING

## Slide 01 — Cover  ·  0:00–0:30

> TAMING THE WEB / WITH KMP.

- Walk on. Don't talk over the title. Read it slowly.
- One sentence: *"Today we take a perfectly good mobile library and stretch it to the web."*
- **Cue:** advance when people start nodding.

## Slide 02 — About Me  ·  0:30–1:30

> WHO_AM_I.log

- *"Mobile engineer at Umain, mostly Android. Lately obsessed with how apps work — and how they fail. Some mobile security, some pen testing. Still learning, and that's what I want to share."*
- Point at the QR: *"All the code, slides, and these notes are in the repo. Don't take notes — relax."*
- **Hard cap: 60 seconds.** Let the talk do the work.

---

# FRAMING — the why

## Slide 03 — Why build it twice?  ·  1:30–3:00

> // A PRODUCT MANAGER, LOOKING AT THE ROADMAP · WHY BUILD IT **TWICE?**

- **The hook — tell it as a story:** *"A product manager looks at the roadmap. The Android team and the iOS team both estimated the same time for the same feature. Same behaviour, two codebases. He asks the question every engineer eventually hears: 'If both apps do exactly the same thing, why are we building the architecture twice?'"*
- Land the duplication: *"Android in Kotlin, iOS in Swift — the same API integration, caching, validation, offline sync. Written twice. And the bugs? Also twice. Android pagination breaks, then iOS breaks the same way a month later."*
- The pivot to your thesis: *"Mobile teams mostly have an answer now — Kotlin Multiplatform: share the logic, keep the UI native. This talk is about the rebuild nobody mentions. The web is the third one."*
- **Footer line:** *"A KMP library is an SDK with multiple front doors."*
- **Cue:** the room must feel the duplication pain before you offer the cure. ~90s.

## Slide 04 — We don't share the UI  ·  3:00–4:30

> WE DON'T SHARE THE UI. · SHARE WHAT MAKES SENSE, KEEP WHAT MATTERS NATIVE.

- **Kill the Flutter/RN confusion immediately:** *"When I say cross-platform, half of you think Flutter or React Native. This is the opposite. We do NOT share the UI. SwiftUI stays SwiftUI. Compose stays Compose. The phone still feels like the phone."*
- What IS shared (point at the list): *"networking, repositories, caching, API models, auth, feature flags, offline sync, analytics, pagination, validation. The backend-driven logic — once."*
- **Why now (the production-ready beat):** *"KMP burned people before — the old memory model, awkward Swift interop, slow builds. That's fixed. New memory manager, no more frozen objects. And Swift interop got clean: a Kotlin `suspend fun fetchProfile()` is called from Swift as `try await repository.fetchProfile()`. iOS engineers stop treating shared code as foreign."*
- Credibility: *"Stable since 2023, Google-endorsed in 2024. In production at Cash App, Netflix, McDonald's."*
- **Cue:** this is the slide that converts the skeptic who thinks KMP = Flutter. ~90s.

## Slide 05 — Ship an SDK, not an API  ·  4:30–5:30

> // API vs SDK — THE HONEST PITCH · SHIP AN SDK, NOT AN API.

- The distinction: *"An API is a contract — just URLs. An SDK is a library that wraps those URLs so nobody re-implements them."*
- Left (API): *"With a raw API, every client rebuilds fetch, parse, error states — and they drift, platform by platform."*
- Right (SDK) — **the pitch to the frontend dev in the room:** *"TypeScript types for free — the compiler emits the `.d.mts`. Zero fetch/parse boilerplate. And you don't learn Kotlin. For you, it's an `npm install`."*
- **The honesty beat (do not skip — it earns the rest of the talk):** *"The cost is real: about 800 KB. On a static landing page that's dead weight — just `fetch()`. On a real app already shipping Android and iOS, sharing the logic pays for itself."*
- **Cue:** ~60s. Land the cost honestly. (Full bundle-cost detail in A3.)

---

# ACT 1 — THE DREAM (how it works)

## Slide 06 — Act 1 Divider  ·  5:30–6:00

> 01 · ACT · THE DREAM · ONE SOURCE OF TRUTH FOR EVERY PLATFORM

- Beat. *"Act 1: the dream we all started with."* Let the subtitle land; don't read it.

## Slide 07 — How .kt becomes .js  ·  6:00–7:00

> // THE COMPILE PIPELINE · HOW .KT BECOMES .JS.

- *"Thirty seconds on the machine — skip it and the demos look like magic."*
- Walk the four panels: **`.kt` source** (commonMain + jsMain) → **IR compiler** (the `js(IR)` backend) → **`.js` + `.d.mts`** (bundle + types) → **V8** (browser or Node).
- Point at the colour: *"Amber is the Kotlin world, neon is the JS world. The handoff happens at the compiler. One frontend, one JS backend."*
- Hand off: *"And the seam that lets the same source hit four different HTTP engines? That's next."*
- **Cue:** ~60s. Don't rabbit-hole on the IR backend.

## Slide 08 — One expect, four actuals  ·  7:00–8:00

> // THE PLATFORM SEAM · ONE expect, FOUR actuals.

- *"This is `expect`/`actual` — the seam that makes one source multiplatform. One `expect fun createHttpClient()` in commonMain. Four `actual`s, one per target."*
- Walk the engine table: *"Android speaks OkHttp, iOS speaks Darwin, JVM speaks CIO, the web speaks Ktor JS. The compiler picks the actual; my code never knows."*
- *"This is the repo's real HttpClient — not a slide diagram."*
- **Cue:** ~60s.

## Slide 09 — Four lines open the browser  ·  8:00–9:30

> // WHAT DOES js(IR) ACTUALLY DO? · FOUR LINES OPEN THE BROWSER.

- "Four lines in the Gradle file. Read them."
  01 — `browser()`: *"compiles JS that runs in any browser — and Node, with a webpack rewrite."*
  02 — `useEsModules()`: *"modern ESM imports, tree-shakeable."*
  03 — `generateTypeScriptDefinitions()`: *"auto-emitted `.d.mts`. TS users get types for free."*
  04 — `binaries.executable()`: *"a runnable bundle webpack picks up."*
- Footer: *"In Kotlin 2.2 you needed `-Xes-long-as-bigint`. In 2.3 it's the default."*

## Slide 10 — Export the behavior, hide the machinery  ·  9:30–11:00

> EXPORT THE BEHAVIOR. HIDE THE MACHINERY. · 13 @JsExport.Ignore

- Read it: *"Export the behavior. Hide the machinery."*
- *"`@JsExport` everything and your TS types blow up. `StateFlow` becomes `unknown`, `CoroutineScope` leaks, repository constructors expose Koin internals."*
- *"In this repo: 13 `@JsExport.Ignore` annotations. Each is a small 'you don't need to see this from JS'. What stays exported is the behavior — load, subscribe, onCleared."*

## Slide 11 — DEMO Node  ·  11:00–12:30  ⚠ LIVE

> KOTLIN CODE, NODE ENGINE.

**Demo steps:**
1. Terminal: `cd demo/node-api && node server.js` → wait for "✅ KMP Library Initialized".
2. `curl localhost:3000/modules/lines | head -c 400` → real Stockholm metro lines.
- **Talk over it:** *"No Express middleware knows about transport. No second domain model. The Node side is fifty lines of glue around the same `LinesViewModel` the Android app uses."*
- **Plan B:** pre-recorded JSON in `docs/demo-fallback/lines.json` if the API is slow.

---

# ACT 2 — THE REALITY (where it gets hard)

## Slide 12 — Act 2 Divider  ·  12:30–13:00

> 02 · ACT · THE REALITY · WHERE COROUTINES MEET REACT

- Beat. *"Act 2: where it stops being a dream."*

## Slide 13 — Room can't. SQLDelight can.  ·  13:00–14:15

> // CHECK THE TARGET BEFORE YOU COMMIT · ROOM CAN'T. SQLDELIGHT CAN.

- *"The moment you add the JS target, your library choices change."*
- Contrast (architectural, not 'Google is slow'): *"Room is annotation-first and assumes a native SQLite engine — JVM and Native only, no JS. SQLDelight is SQL-first with a pluggable driver per platform; the Web Worker driver runs SQLite compiled to Wasm. It works on JS."*
- **The rule (they write this down):** *"Before any dependency, check klibs.io for the JS or Wasm badge. And read it carefully — all the Apple targets hide under one 'Kotlin/Native' badge. Don't misread that as 'iOS not supported'."*
- Volatility: *"This moves monthly — DataStore just added JS and Wasm in an alpha."*
- **Cue:** ~75s. This is what makes you sound like you've shipped it, not just demoed it.

## Slide 14 — StateFlow → a callback  ·  14:15–15:45

> // THE BRIDGE · subscribe(onStateUpdate)

- *"JavaScript can't see `StateFlow`. Mark it `@JsExport.Ignore`."*
- Walk it: *"`subscribe` takes a callback, launches a coroutine in `viewModelScope`, collects the `StateFlow`, pushes each emission into the callback."*
- *"JS gets a callback. Kotlin keeps the flow. One method — the whole bridge from Kotlin's side."*

## Slide 15 — Where do coroutines run?  ·  15:45–17:00

> Dispatchers.Main IS THE MICROTASK QUEUE.

- *"On Android, `Dispatchers.Main` is the UI thread. On iOS, the main run loop. On Node and the browser…"*
- Walk it: *"`emit()` on the StateFlow → `queueMicrotask()` in V8 → your `setState`."*
- **Caveat (say it aloud):** *"Mix coroutine flows with raw Promises and ordering is not guaranteed. Pick one."*

## Slide 16 — 12 lines of TypeScript  ·  17:00–18:30

> // THE REACT HOOK · subscribe + onCleared IN A useEffect

- **Pre-empt the confusion FIRST:** *"This is a Compose Multiplatform talk — why am I in React? On purpose. If I showed Compose Web here, you'd think 'Kotlin calling Kotlin, of course it works.' React is the proof: the bridge is a plain JavaScript callback. The SDK doesn't leak Kotlin."*
- The rule, one breath: *"Compose Web is for shared UI; React is for web-native — SEO, accessibility, the web ecosystem. The SDK feeds both identically."* (Matrix in A12 if pushed.)
- Read the hook: *"`useState` for the snapshot, `useEffect` to `subscribe` on mount and `onCleared` on cleanup. Twelve lines."*
- Highlight: green line `vm.subscribe(setState)` = the whole bridge; amber line `return () => vm.onCleared()` = the lifecycle contract.

## Slide 17 — DEMO React  ·  18:30–20:00  ⚠ LIVE

> SAME SDK, BROWSER UI.

**Demo steps:**
1. `cd demo/web && npm run dev` → open the dev URL, show real metro lines.
2. DevTools → Network → *"that's coming from Kotlin code in your browser. No proxy, no backend."*
- **Talk over it:** *"Same `LinesViewModel` the Android emulator subscribed to two minutes ago. The browser has zero business logic. It can't drift."*
- **Optional aside:** *"CMP Web hit Beta in Sept 2025 — I could swap React for Kotlin-rendered Compose on the same `subscribe`. Same SDK, different UI engine."*
- **Cue:** if HMR hiccups, refresh; don't restart the dev server.

## Slide 18 — One line between a library and a leak  ·  20:00–22:00  ⚠ LIVE · SCREENSHOT MOMENT

> ONE LINE BETWEEN A LIBRARY AND A LEAK.

**Demo steps:**
1. **Before:** comment out the cleanup return in `useStockholmTransport.ts`. Reload.
2. DevTools → Console. Navigate `/lines → /sites` ten times fast. Subscription counter climbs: 1 → 2 → 4 → 12.
3. **After:** uncomment cleanup. Reload. Repeat. Counter stays at 1.
- **Talk over it:** *"Coroutine scope is a lifecycle contract. Break it, you leak. Honor it, you don't. One line."*
- **This is the slide everyone screenshots.** Hold a beat after the counter stays flat. **90 seconds max.** (StrictMode question → A6.)

---

# ACT 3 — THE PAYOFF (shipping)

## Slide 19 — Act 3 Divider  ·  22:00–22:30

> 03 · ACT · THE PAYOFF · SHIPPING IT

- Beat. *"Act 3: but can we actually ship it?"*

## Slide 20 — Can I npm install this? Not yet.  ·  22:30–25:00

> NOT YET. And here's exactly why. → What real distribution looks like.

- *"Honest answer: not yet. Here's the receipt."* Walk the gaps (fast — don't linger):
  *"No auto-generated `package.json`. Package-name mismatch. No `main`/`module`/`exports`/`types`. No `peerDependencies`. The `npm-publish` plugin sits unwired."*
- *"Each closes in ~a day of pipeline work. Not research — just work."*
- Pivot to what real looks like: *"Scope it `@umain/stockholm-transport`. ESM for browser, commonjs2 for Node. Hoist the heavies — Ktor JS, Koin, coroutines — into `peerDependencies`."*
- One build → four registries: *"Maven for Android/JVM, SPM for iOS, npm for web/Node. Flutter would need a thin Dart wrapper — Firebase ships exactly that pattern."*
- **Cue:** ~2.5 min. This is the trimmable slide if you're running long — collapse the gap walk.

## Slide 21 — Who's using your SDK?  ·  25:00–26:30

> // IDENTIFYING CONSUMERS · THE TOKEN PATTERN

- *"If you publish an SDK, how do you know who's using it? The token pattern — same as Firebase, Stripe, Sentry."*
- The flow: *"Consumer passes a key at init → the SDK holds it in memory → injects it on every request as a header → the backend validates and attributes the traffic."*
- **The critical caveat:** *"A token identifies the consuming *app*, not the human — that's not user auth. And you never ship a raw API key in a JS bundle; anyone can read it in the browser. Backend proxy, or a runtime-injected token via Koin."*
- **Cue:** ~90s. Conceptual — don't implement on stage.

## Slide 22 — DEMO: fix one line, three platforms  ·  26:30–29:30  ⚠ LIVE · MIC DROP

> FIX. RELOAD. DROP MIC.

**Demo steps (rehearsed < 90s):**
1. **Break:** `LinesRepositoryImpl.kt` — wrong query-param value. Save.
2. `./gradlew :stockholm-transport:publishToMavenLocal :stockholm-transport:jsBrowserDistribution`
3. Three apps empty: Android, iOS, browser. Beat.
4. **Fix:** undo. Re-publish. All three reload. Hold for applause.
- **Talk over it:** *"One line. Three platforms. Zero divergence."*
- **Plan B:** `docs/demo-fallback/three-platforms.mp4`. Don't troubleshoot live.

## Slide 23 — Closing  ·  29:30–33:00

> 01 · Export the BEHAVIOR. Hide the MACHINERY.
> 02 · Coroutine SCOPE = lifecycle CONTRACT.
> 03 · The SDK boundary is the SAME on every platform.

- Read each rule. Pause between them.
- **Callback to the opening (lands the arc):** *"Remember the PM's question — why build it twice? The 2026 answer isn't 'never build twice.' It's: share what makes sense, keep what matters native. Mobile figured that out. The web is just the next front door."*
- QR: *"All the code, slides, notes — in the repo. Find me on LinkedIn or Medium."*
- *"Thank you."* — beat — open Q&A.

---

# Q&A — questions the audience will ask

Pick two or three to answer fully; keep the rest as backup. Slide references use the new numbering.

### A1. *"Why not just `fetch()` the API? Why a library at all?"*
The library isn't `httpClient.get("v1/lines")` — that's one line. It's the 200 lines around it: DTO→domain mapping, `NetworkError` categorisation, `DataResult` contract, the `subscribe`/`onCleared` lifecycle, the 13 `@JsExport.Ignore` decisions, the JSON/timeout/key config. Without it, every client rebuilds that — badly, differently. And the mic-drop demo (slide 22) only works because the same Kotlin runs in V8, the JVM, and iOS; "just `fetch()`" would fix two platforms and leave the third broken.

### A2. *"Do I hand-roll TypeScript types?"*
No — Kotlin/JS auto-emits `.d.mts` (one of the four `js(IR)` lines). TS consumers get typed `Line`, `LinesUiState`, `DataResult`, `NetworkError` — the same types the Compose code binds to.

### A3. *"Bundle size? Doesn't the Kotlin runtime bloat the web app?"*
Yes — ~80 KiB runtime, ~800 KiB production bundle. Pays off when you already maintain Android+iOS, have non-trivial domain logic, and the productivity of shared contracts dominates the bytes. Doesn't pay off for a single simple web client or hard size limits (Cloudflare Workers' 1 MiB) — there, "just `fetch()`" is right.

### A4. *"Browser ESM vs npm — which when?"*
Anywhere you'd `npm install`, this is an npm package. The browser ESM falls out of it — `<script type="module">` works but you give up the lockfile and types. Vite/Next/Node → npm install. Static HTML → ESM script. Edge runtimes → watch the size.

### A5. *"Why a callback bridge, not expose `StateFlow`?"*
`StateFlow` is a coroutines type — JS has no suspending functions or `Flow`. Exposing it would leak Kotlin internals or lose the lifecycle contract. `subscribe(onState)` + `onCleared()` — two methods, framework-agnostic.

### A6. *"React StrictMode double-mount in dev?"*
The hook returns `() => vm.onCleared()`. StrictMode mounts → unmounts → mounts; cleanup fires between, the first subscription is cancelled before the second starts. Two in the network panel, one survives — correct, not a leak. Slide 18 shows the opposite (no cleanup), where every mount leaks.

### A7. *"Node 22 / Bun / Deno?"*
Node 22 is what we test (built-in fetch, native ESM). Bun should work (Node-compatible), not in CI. Deno needs validation — the Kotlin/JS runtime pokes at Node globals. Treat as untested.

### A8. *"Does `subscribe` work outside React?"*
It's a callback API. React (`useState`+`useEffect`), Vue (`ref`+`onUnmounted`), Svelte (store + cleanup), Solid (`createSignal`+`onCleanup`), vanilla, Web Components (`connectedCallback`/`disconnectedCallback`). Always: subscribe on mount, capture cleanup, `onCleared` on unmount.

### A9. *"Perf cost of the Kotlin↔JS boundary?"*
Effectively nothing at the call site — `vm.subscribe(cb)` is a normal JS call. Overhead is the ~80 KiB parse-on-load plus a small per-allocation constant. No FFI, no marshalling, no stringify at the boundary.

### A10. *"Debugging Kotlin in the browser?"*
Source maps — Chrome devtools shows the `.kt` source, breakpoints land on Kotlin lines. `console.*` via the `AppLogger` JS actual — same logger as Android.

### A11. *"Can I use Compose Multiplatform for Web?"*
Yes — CMP Web is Beta (Sept 2025). Different layer, same SDK underneath: CMP Web targets the UI, this library is the data/state layer. A CMP Web app `koinInject<LinesViewModel>()` and binds `subscribe` to Compose state. We used React in the demo to prove the boundary is clean (A12).

### A12. *"CMP Web vs React — when?"*
SDK is UI-agnostic; this is a UI choice. **CMP Web** when: team lives in Compose, want pixel-identical UI, internal tool (no SEO), one UI codebase. **React/Vue/Svelte** when: web is the product, you need SSR/SEO/a11y, web designers, the web ecosystem, edge size limits, or existing React expertise. One-liner: *CMP Web is UI shared; React is web-native; the SDK runs on both.* Real split: public web → React/Next; internal dashboard → CMP Web; mobile → CMP. Same library under all three.

### A13. *"Authentication? API keys?"*
SL key is baked into `BuildConfig.API_KEY` and sent as a query param. For a real product the key shouldn't ship in the JS bundle. Two patterns: (1) backend proxy adds it server-side (what `./sl start` + ngrok does); (2) per-user OAuth tokens injected via Koin — same `subscribe` API. Token identifies the app, not the user (slide 21).

### A14. *"iOS — SPM or CocoaPods?"*
SPM, via `Package.swift` pointing at an XCFramework zip on GitHub Releases. No CocoaPods. **Looking ahead:** KotlinConf '26 announced Swift Export → Alpha in Kotlin 2.4 — native idiomatic Swift bindings, no ObjC bridge. Not production yet; we stay on XCFramework.

### A15. *"AGP 9 — doesn't it break `com.android.application` + KMP?"*
Yes — AGP 9 no longer supports that combo in one module. The new official structure separates library and app modules. The library here already follows it; the demo apps still use the combo with bypass props (`android.builtInKotlin=false`, `android.newDsl=false`) — officially temporary, migration on the roadmap.

### A16. *"Is this production-ready? KMP burned people before."*
It did — say why it's different. Old pain: frozen-object memory model, awkward Swift interop, slow builds. What changed: new memory manager (no freezing), clean interop (`suspend fun` → Swift `try await`). Proof: Stable 2023, Google I/O 2024, Cash App/Netflix/McDonald's. The honest framing: *KMP stopped promising "one codebase forever" and started saying "share what makes sense, keep what matters native" — that realism is why engineers trust it now.* Caveat: animation/graphics-heavy apps with little logic won't benefit; KMP pays off when business complexity ≫ UI complexity.

### A17. *"Versioning across four registries?"*
Single `libBaseVersion` in `gradle.properties`; Maven, npm `package.json`, and `Package.swift` all read from it. SemVer — minor for new ViewModels, major for breaking API changes.

### A18. *"What if the SL API changes?"*
Fix one Kotlin file (`LinesRepositoryImpl` / `LineDto`), bump minor, ship. Consumers migrate at their pace. Not centralising costs you N clients × M changes.

### A19. *"Does it scale to a team of 20?"*
Healthier with KMP: one library team owns the boundary (a contract everyone reads); platform teams are consumers. Without it, three drifting clients and duplicated reviews. Org cost: someone owns and versions the library — a real, visible role.

### A20. *"Why not hand-roll a parallel JS SDK?"*
Drift. Two SDKs = two surfaces, two test suites, two changelogs — out of sync within a sprint. KMP makes the JS SDK a build artefact of the Kotlin one; drift is structurally impossible.

---

## Pre-flight checklist (morning of)

- [ ] Charge: laptop, slide remote, backup timer.
- [ ] `~/.m2/.../stockholm-transport/1.0.0/` exists locally.
- [ ] `demo/web/node_modules/StockholmTransport-stockholm-transport/` resolves.
- [ ] Android emulator + iOS simulator boot cold in < 60s.
- [ ] Four terminals open in order: gradle / node-api / web dev / git status.
- [ ] Projector 30 min early — test neon `#4DFFA8` contrast on the venue projector.
- [ ] Wi-Fi fail-fast: if `curl sl.se` is slow in green-room, switch to phone hotspot.
- [ ] `docs/demo-fallback/` videos queued in QuickTime behind `⌘\``.

## Anti-goals

- Don't apologise for beta. *"Beta but works on stage"* > *"sorry, beta."*
- Don't read code letter-for-letter. Read the **shape**, point at the **green** lines.
- Don't run `npm install` live. Network mid-demo dies.
- Don't say "Claude" or "AI" in the talk. The work is yours.

## After the talk

- Tweet the slide-18 screenshot (memory-leak before/after) within an hour — the one people remember.
- Push slides + notes to the repo before Q&A ends so the audience grabs the URL on the way out.
