# Presenter notes — "Taming the Web with Kotlin Multiplatform"

**Event:** mDevCamp 2026 — 2026-06-04 · 15:35–16:20 · Cross the rave
**Total time:** 35 min talk + ~10 min Q&A
**Deck:** [docs/Slides.pen](Slides.pen)
**Repo:** [github.com/jacksonfdam](https://github.com/jacksonfdam)

## How to read this doc

Each entry: **slide title — target time on stage**. The bullets are the *only* things to actually say (kept short, talk-shaped). Demo blocks are **bold** with concrete commands. Cues at the bottom are reminders, not script.

### Pacing budget (35 minutes hard cap)

| Section            | Slides | Mins |
| ------------------ | ------ | ---- |
| Opening            | 01–03  | 4    |
| Act 1 — The Dream  | 04–08  | 9    |
| Act 2 — The Reality| 09–14  | 11   |
| Act 3 — The Payoff | 15–20  | 11   |
| **Total**          | 20     | 35   |

Slide 21 (Thesis) lives off the main path — re-show it during Q&A if someone asks "so what's the one-sentence pitch".

---

## Slide 01 — Cover  ·  0:00–0:30

> "TAMING THE WEB / WITH KMP."

- Walk on. Don't talk over the title.
- Wait for the room to settle. Read it slowly: *"Taming the web — with Kotlin Multiplatform."*
- One sentence: "Today we're taking a perfectly good mobile library and stretching it to the web."
- **Cue:** advance the moment people start nodding.

---

## Slide 02 — About Me  ·  0:30–1:30

> WHO_AM_I.log

- *"I'm a mobile engineer at Umain, mostly Android. Lately I've developed a bit of an obsession with breaking things — not just understanding how apps work, but how they fail. I've been diving into mobile security: reading, writing, some pen testing. I'm not an expert — yet. But I've learned a lot along the way, and that's what I want to share with you today."*
- Point at the GitHub / LinkedIn / Medium block. Tell them: *"All the code is in the repo, link's in the QR. Don't take notes — relax."*
- **Hard cap: 60 seconds.** Do not over-explain who you are. Let the talk do the work.

---

## Slide 03 — Act 1 / The Dream  ·  1:30–2:00

> 01 · ACT · THE DREAM · ONE SOURCE OF TRUTH FOR EVERY PLATFORM

- Beat. Read the act title.
- "Act 1: the dream we all started with."
- Move on — don't read the subtitle out loud, just let it land.

---

## Slide 04 — Q1: Why ship as JavaScript?  ·  2:00–4:00

> Why ship my mobile library as JavaScript? · Android (OkHttp) / iOS (Darwin) / JVM (CIO) / WEB (Ktor JS)

- "Why would I — a mobile engineer — care about a JavaScript build of my library? Because I already wrote the hard part once."
- Point at the right-side panel: *"Same call, four engines. Android speaks OkHttp, iOS speaks Darwin, JVM speaks CIO, the web speaks Ktor JS. KMP picks the engine; my code never knows."*
- "If I don't ship JavaScript, I have to write a Node backend that re-implements the same domain. Twice the bugs."
- **Likely follow-up from the floor:** *"Why not just `fetch()` the API directly?"* — see the **Q&A** appendix, question **A1**. Short answer to deflect on stage: *"The library isn't `httpClient.get` — that's the easy line. The library is the 200 lines around it: DTO mapping, error categorisation, retry policy, the `subscribe`/`onCleared` lifecycle contract. Without KMP, every web client reinvents that badly."*

---

## Slide 05 — One HTTP call, every platform  ·  4:00–6:00

> `httpClient.get("v1/lines")` from `LinesRepositoryImpl.kt`

- Read the green line aloud: *"`httpClient.get('v1/lines')` — that's the entire HTTP layer."*
- "Above and below: error mapping, DTO parsing, domain mapping. Same on Android, same on iOS, same in Node, same in your browser tab."
- "K2 compiler, Kotlin 2.3.21. Same module ships to all four targets unchanged."

---

## Slide 06 — Q2: What does `js(IR)` actually do?  ·  6:00–7:30

> 4 lines open the browser

- "Four lines in your gradle file. Let's read them."
- 01 — `browser()`: "Compiles JS that runs in any browser — and in Node, with a tiny webpack rewrite."
- 02 — `useEsModules()`: "Modern ESM `import` instead of UMD glue. Tree-shakeable."
- 03 — `generateTypeScriptDefinitions()`: "Auto-emitted `.d.mts`. Your TypeScript users get types for free."
- 04 — `binaries.executable()`: "Emits a runnable bundle webpack can pick up."
- "Bonus: in Kotlin 2.2 you needed `-Xes-long-as-bigint`. In 2.3 it's the default."

---

## Slide 07 — Q3: Just `@JsExport` everything?  ·  7:30–9:30

> Export the BEHAVIOR. Hide the MACHINERY. · 13 @JsExport.Ignore annotations

- Read it: *"Export the behavior. Hide the machinery."*
- "If you `@JsExport` everything, your TypeScript types blow up. `StateFlow` becomes `unknown`. `CoroutineScope` leaks into your IDE. Repository constructors expose Koin internals."
- "In this repo: 13 `@JsExport.Ignore` annotations. Each one is a small no — `you don't need to see this from the JS side`."
- "What stays exported is the **behavior**: load, subscribe, onCleared. Not the machinery."

---

## Slide 08 — DEMO 01: Node  ·  9:30–11:00  ⚠ LIVE

> KOTLIN CODE, NODE ENGINE.

**Demo steps:**
1. Switch to terminal: `cd demo/node-api && node server.js`
2. Wait for "✅ KMP Library Initialized successfully."
3. Second terminal: `curl localhost:3000/modules/lines | head -c 400`
4. Real Stockholm metro lines come back: *"Blå linjen 10, 11 — pulled from sl.se via Kotlin code running in V8."*

- **Talk over the demo:** "There is no Express middleware that knows about transport. There is no second domain model. The Node side is **fifty lines** of glue around the same `LinesViewModel` the Android app uses."
- **Cue:** if the API is slow, skip to the curl JSON pre-recorded in `docs/demo-fallback/lines.json`.

---

## Slide 09 — Act 2 / The Reality  ·  11:00–11:30

> 02 · ACT · THE REALITY · WHERE COROUTINES MEET REACT

- Beat.
- "Act 2: where it stops being a dream."

---

## Slide 10 — Q4: StateFlow → callback  ·  11:30–13:30

> `subscribe(onStateUpdate)` — pure Kotlin, zero JS-specific code

- "JavaScript can't see `StateFlow`. Mark it `@JsExport.Ignore`."
- Walk the code: *"`subscribe` takes a callback, launches a coroutine in `viewModelScope`, collects the `StateFlow`, and pushes each emission into the callback."*
- "JavaScript gets a callback. Kotlin keeps the flow. Everybody wins."
- "This is the entire bridge from Kotlin's side. **One method.**"
- **Likely follow-up:** *"Why callback? Why not expose StateFlow directly?"* (A5), *"Does subscribe work with Vue / Svelte / vanilla JS too?"* (A6).

---

## Slide 11 — Q5: Where do coroutines actually run?  ·  13:30–15:00

> Dispatchers.Main IS the microtask queue.

- "On Android, `Dispatchers.Main` is the UI thread. On iOS, it's the main run loop. On Node and in the browser…"
- Walk the diagram: *"`emit()` on the StateFlow → `queueMicrotask()` in V8 → your `setState`."*
- **Bottom caveat — say it aloud:** "If you mix coroutine flows with raw Promises, ordering is not guaranteed. Don't. Pick one."

---

## Slide 12 — Q6: useStockholmTransport hook  ·  15:00–17:00

> `subscribe + onCleared` in a `useEffect`

- "Now we plug it into React. Twelve lines of TypeScript."
- Read the code top to bottom slowly. The audience needs to **see** it.
- Highlight the green line: *"`vm.subscribe(setState)` — that's the whole bridge."*
- Highlight the amber line: *"`return () => vm.onCleared()` — that's the lifecycle contract. Skip it and React leaks subscriptions."*
- "Same pattern works for Vue, Solid, Svelte — anything with a mount/unmount story."

---

## Slide 13 — DEMO 02: React  ·  17:00–19:00  ⚠ LIVE

> SAME SDK, BROWSER UI.

**Demo steps:**
1. `cd demo/web && npm run dev`
2. Open `http://localhost:5173/lines`. Show real Stockholm metro lines rendering.
3. Open browser devtools → Network tab → point at the request: *"That's coming from Kotlin code running in your browser. No proxy, no backend."*
4. Open `useStockholmTransport.ts` in the editor — the file from slide 12, alive.

- **Talk over it:** "Same `LinesViewModel` the Android emulator subscribes to two minutes ago. The browser doesn't have its own business logic. It can't drift."
- **Cue:** if HMR hiccups, refresh the page; don't restart the dev server on stage.

---

## Slide 14 — Q7: Forget `onCleared()`, watch it leak  ·  19:00–21:00  ⚠ LIVE

> One line between a library and a leak.

**Demo steps:**
1. **Before:** comment out the cleanup return in `useStockholmTransport.ts`. Reload.
2. Open devtools → Console. Navigate `/lines → /sites → /lines → /sites` ten times fast. Show the subscription counter climbing: 1 → 2 → 4 → 12.
3. **After:** uncomment the cleanup. Reload. Repeat the navigation.
4. Counter stays at 1.

- **Talk over it:** "Coroutine scope is a lifecycle contract. Break the contract, you leak. Honor it, you don't. **One line.**"
- **This is the slide everyone screenshots.** Hold for a beat after the second counter stays flat.
- **Hard cap: 90 seconds.** Move on the moment the leak/clean contrast lands.
- **Likely follow-up from a React-savvy attendee:** *"What about StrictMode double-mounting in dev?"* — see A7. Soundbite: *"The cleanup return fires correctly between the StrictMode mount/unmount/mount cycle. The first subscription is cancelled before the second starts. You'll see two subscriptions in the network tab — one survives. That's correct behaviour, not a leak."*

---

## Slide 15 — Act 3 / The Payoff  ·  21:00–21:30

> 03 · ACT · THE PAYOFF · SHIPPING IT AS AN NPM PACKAGE

- Beat.
- "Act 3: but can we actually ship it?"

---

## Slide 16 — Q8: Can I `npm install` this?  ·  21:30–24:00

> NOT YET. And here's exactly why.

- "Honest answer: not yet. Here's the receipt."
- Walk the gap list:
  01 — *"Kotlin/JS doesn't auto-generate a publishable `package.json`."*
  02 — *"Webpack writes `stockholm-transport.js`. The package name is `StockholmTransport-stockholm-transport`. Mismatch."*
  03 — *"No `main`, no `module`, no `exports`, no `types` field."*
  04 — *"No `peerDependencies`. Ktor JS, Koin, kotlinx-coroutines all bundle into your app twice."*
  05 — *"The `npm-publish` gradle plugin is sitting in our `libs.versions.toml`, never applied."*
- "Each gap closes in roughly a day of pipeline work. None are research problems. They're just *work*."
- **Likely follow-ups from the floor:** *"Browser ESM vs npm — when do I use which?"* (A4), *"What about TypeScript types?"* (A2), *"What's the bundle size cost?"* (A3). Soundbite to redirect: *"Anywhere you'd `npm install` something, this is an npm package. The browser ESM falls out of that — drop it in via a `<script type='module'>` tag and you give up the lockfile and the types."*

---

## Slide 17 — Q9: Scope. Format. Peers.  ·  24:00–26:00

> What a real npm package looks like

- "What does *real* look like?"
- Card 1 — SCOPE: *"`@umain/stockholm-transport`. Drop the ugly `StockholmTransport-stockholm-transport` mouthful that Kotlin/JS hands you by default."*
- Card 2 — FORMAT: *"ESM for the browser, commonjs2 for Node. Both come from one bundle if you wire webpack right."*
- Card 3 — PEERDEPS: *"Hoist the heavies — Ktor JS, koin-core, kotlinx-coroutines. Bundle stays small, consumers stay sane."*
- "And the `npm-publish` gradle plugin handles all three — once we turn it on."

---

## Slide 18 — Q10: One line, three platforms  ·  26:00–27:00

> The setup for the payoff demo

- "Setup. One file: `LinesRepositoryImpl.kt`, line 25. CommonMain. The arrow points at three platforms."
- "Watch what happens when I break it."
- Don't linger — this is the run-up to the live demo.

---

## Slide 19 — DEMO 03: Three platforms, one fix  ·  27:00–30:00  ⚠ LIVE  · MIC DROP

> FIX. COMMIT. RELOAD. DROP MIC.

**Demo steps (rehearsed under 90 seconds):**
1. **Break:** `LinesRepositoryImpl.kt` line 25 — change `parameter("transport_authority_id", 1)` to `… 99`. Save.
2. `./gradlew :stockholm-transport:publishToMavenLocal :stockholm-transport:jsBrowserDistribution`
3. Switch through the three apps: Android emulator (empty list), iOS simulator (empty list), browser (empty list). Beat.
4. **Fix:** undo the line. Save.
5. `./gradlew :stockholm-transport:publishToMavenLocal :stockholm-transport:jsBrowserDistribution`
6. All three apps reload. Lines come back. Hold for the applause.

- **Talk over it:** "One line. Three platforms. Zero divergence."
- **Cue:** stopwatch this in rehearsal. If the gradle re-publish takes >40s, pre-build a delta and demo the file watcher.
- **Plan B:** pre-recorded `docs/demo-fallback/three-platforms.mp4` if anything misbehaves on stage. Don't troubleshoot live.

---

## Slide 20 — Closing: three rules  ·  30:00–34:00

> 01 · Export the BEHAVIOR. Hide the MACHINERY.
> 02 · Coroutine SCOPE = lifecycle CONTRACT.
> 03 · The SDK boundary is the SAME on every platform.

- Read each rule. Pause between them.
- "If you remember nothing else: those three."
- Point at the QR: *"All the code, all the slides, all these notes — repo's there. Find me on LinkedIn or Medium if you want to talk shop."*
- "Thank you." — beat — open Q&A.

---

## Slide 21 — Thesis (off-path)

> A KMP library is an SDK with multiple front doors.

- Use this in Q&A if someone asks for the one-sentence pitch.
- Otherwise, leave it unaddressed. The talk's narrative carries the thesis.

---

## Q&A — questions the audience will ask

A curated set with short, defensible answers. The first three are near-certain (every KMP-curious mobile-dev audience asks them); the rest cluster around npm, lifecycle, and edge cases. **Pick a max of two or three to answer fully on stage** — keep the rest as backup.

### A1. *"Why not just consume the API directly with `fetch()`? Why ship a library at all?"*

The single most-asked question. Defensible answer:

The library isn't `httpClient.get("v1/lines")` — that's one line. The library is the **200 lines around it**:

| In the library | Without it, every client builds | And |
|---|---|---|
| DTO → domain mapping with normalisation | Its own parser | They drift, subtly differently |
| `NetworkError.NoInternet / Timeout / NotFound / ServerError / Unknown` | Its own error model | Android says "Connection failed", iOS says "Network unavailable", web shows a stack trace |
| `DataResult<T> = Success \| Error` contract | Its own error-handling style | UI layers grow ad-hoc mappings |
| `BaseViewModel.subscribe(callback)` + `onCleared` | Its own state machine per framework | React, Compose, SwiftUI all bind differently |
| 13 deliberate `@JsExport.Ignore` decisions | Either nothing hidden or each consumer guesses | DX suffers |
| `ignoreUnknownKeys = true`, 15 s timeout, default `key=` query param | Configures these three different ways | Three teams, three behaviours |

And — closing argument — **the talk's mic-drop demo only works because of the library**. *"Fix one line, three platforms reload"* requires the same Kotlin code to run in V8, the JVM, and on iOS. If the web client were "just `fetch()`", that bug fix would land on two platforms; the third would stay broken until someone manually patched its separate code path.

### A2. *"What about TypeScript? Do I have to hand-roll types?"*

No — Kotlin/JS auto-emits `kotlin/StockholmTransport-stockholm-transport.d.mts` alongside the bundle (one of the four lines in `js(IR) { generateTypeScriptDefinitions() }`). TS consumers get autocompleted, typed access to `Line`, `LinesUiState`, `DataResult`, `NetworkError` — the same types the Android Compose code binds to. Hand-rolling types around `fetch()` gives you `unknown` until they drift.

### A3. *"What's the bundle size cost? Doesn't the Kotlin runtime bloat the web app?"*

Yes — the Kotlin/JS runtime is ~80 KiB and the production webpack bundle for this library is ~800 KiB. That's heavy compared to a hand-rolled fetch wrapper. **When does it stop paying off?**

- **It pays off when:** you already maintain Android + iOS, you have non-trivial domain logic, the library is one of many things in the SPA bundle, and the productivity win of shared types/contracts dominates the byte count.
- **It doesn't pay off when:** you have a single web client and the API is so simple that `fetch().then(r => r.json())` is the entire SDK; or when you're shipping to a runtime with hard size limits (Cloudflare Workers' 1 MiB compressed cap, for example). In those cases, "just `fetch()`" is genuinely the right call.

### A4. *"Browser ESM vs npm package — when do I use which?"*

Soundbite: *"Anywhere you'd `npm install` something, this is an npm package. The browser ESM file is what falls out of that — drop it in via `<script type=\"module\">` if you want, but you give up the lockfile and the `.d.mts` types."* Concrete cases:

| Consumption | When to use |
|---|---|
| `npm install @umain/stockholm-transport` in a Vite/Webpack/Next.js app | The default for any framework-based web client. Tree-shakes if your bundler can. |
| `npm install` from Node | SSR, BFFs, GraphQL gateways, webhook handlers, scheduled jobs that need SL data |
| `<script type="module">` from unpkg/jsdelivr | Static HTML pages with vanilla JS, no build pipeline, types not required |
| Cloudflare Workers / Vercel Edge | Watch the bundle size — this is where "just `fetch()`" is defensible |
| CLI tool (a CI script, an ETL job) | npm install + invoke ViewModels directly |

### A5. *"Why a callback bridge? Why not expose `StateFlow` directly?"*

Because `StateFlow` is a Kotlin-coroutines type. There's no idiomatic JS equivalent — JS doesn't have suspending functions, structured concurrency, or `Flow`. Exposing `StateFlow` to JS would either leak Kotlin internals (`uiState.collect_0`, `_state_field`) or lose the lifecycle contract.

The contract we *do* want to expose is *"give me a callback I'll fire on every state change, and a way to stop"* — that's `subscribe(onState)` + `onCleared()`. Two methods. Framework-agnostic. The React hook on Slide 12 is one line wrapping each.

### A6. *"Does `subscribe` work with anything other than React?"*

Yes — it's a callback API. It plugs into:

- **React** → `useState` + `useEffect` (Slide 12, our hook)
- **Vue 3** → `ref` + `onUnmounted`
- **Svelte** → a custom store (`writable()` + the cleanup return)
- **SolidJS** → `createSignal` + `onCleanup`
- **Vanilla JS** → just call it
- **Web Components** → `connectedCallback` / `disconnectedCallback`

The pattern is always the same: subscribe on mount, capture the cleanup, call `onCleared` on unmount.

### A7. *"What about React's StrictMode double-mounting in dev?"*

The hook returns `() => vm.onCleared()` from `useEffect`. React's strict mode mounts → unmounts → mounts again; the cleanup fires correctly between, so the first subscription is cancelled before the second starts. You'll see two subscriptions in the dev-tools network panel and one survives — that's correct behaviour, not a leak. Slide 14's leak demo shows the *opposite* case (no cleanup return), where every mount permanently leaks.

### A8. *"Can I run this on Node 22 / Bun / Deno?"*

- **Node 22** — what we test (the `demo/node-api` Dockerfile). Built-in `fetch`, native ESM. ✓
- **Bun** — should work; Bun is largely Node-compatible. Not in our CI yet.
- **Deno** — needs validation. Deno's npm shim handles most things but the Kotlin/JS runtime occasionally pokes at Node-specific globals. Treat as untested.

### A9. *"How do you handle authentication? API keys?"*

In this repo, the SL API key is baked into `BuildConfig.API_KEY` from `gradle.properties` and appended as a query param. **For a real product**, the key shouldn't ship in the JS bundle — anyone can read it. Two patterns:

1. **Backend proxy** (what we already do for the talk) — the Node API at `:3000` adds the key server-side; the JS bundle calls the proxy with no key. This is also why `./sl start` rewrites `serverHostURL` to the ngrok URL.
2. **Per-user OAuth tokens** passed in via Koin — same `subscribe` API works; `BuildConfig.API_KEY` becomes a runtime-injected dependency.

### A10. *"Can I use this with Compose Multiplatform for Web?"*

Different layer, different question. CMP/Web targets the **UI** — it compiles Compose composables to web canvas/DOM. This library is the **SDK underneath** any UI — the data, state, and error model. They compose: a CMP/Web app can `koinInject<LinesViewModel>()` and bind its `subscribe` to Compose state, exactly like the Android demo does today. Worth a slide on its own — out of scope for this 35-min talk.

### A11. *"What's the perf cost of crossing the Kotlin↔JS boundary?"*

Effectively nothing at the call site. Kotlin/JS compiles to plain JS — `vm.subscribe(cb)` is a normal JS function call. The runtime overhead is the ~80 KiB shipping cost (one-time, parse-on-load), plus a small constant per allocation because Kotlin classes are JS classes with a few extra fields. There's no FFI, no marshalling, no JSON-stringify-at-the-boundary. If your hot path is "subscribe once, render many times", the cost is identical to a hand-written JS API.

### A12. *"How do you debug Kotlin running in the browser?"*

Source maps. Kotlin/JS emits `.js.map` files alongside the bundle; Chrome devtools shows the original `.kt` source in the Sources panel and breakpoints land on Kotlin lines. `console.log` works through the `AppLogger.kt` JS actual we ship in [`shared/core/src/jsMain/.../AppLogger.kt`](../shared/core/src/jsMain/kotlin/com/umain/transport/core/logging/AppLogger.kt) — same logger the Android version uses, just routed to `console.*` on the JS side.

### A13. *"How do you version a library that publishes to four different package registries?"*

Single `libBaseVersion=1.0.0` in `gradle.properties`. The Maven publication, the JS package.json, and the iOS Package.swift all read from it. The `npm-publish` Gradle plugin (currently unwired — see Slide 17) propagates this into the npm `package.json` automatically. SemVer rules; minor bumps for new ViewModels, major for breaking changes to the public API.

### A14. *"What if the SL API changes?"*

You fix the boundary in one Kotlin file (`LinesRepositoryImpl` / `LineDto`), bump the library minor version, and ship. Consumers pin and migrate at their own pace. The cost of NOT centralising is a quadratic mess — N clients × M API changes = N×M change requests instead of M.

### A15. *"Does this scale to a team of 20?"*

The shape is healthier with KMP than without:

- **Without** — each platform team owns its own SL client. Three clients drift. PR reviewers are duplicated. New API fields have to be plumbed through three codebases.
- **With** — one library team owns the boundary; three platform teams are consumers. The boundary is a contract everyone reads. New fields land once, propagate everywhere.

The org cost: someone has to own and version the library. That's a real role, not a hidden one.

### A16. *"Why not just write a hand-rolled JS SDK that mirrors the Kotlin one?"*

Same reason you don't hand-roll a TypeScript SDK that mirrors a Java back-end: **drift**. Two SDKs is two surfaces, two test suites, two changelogs, and inevitably one of them gets out of sync within a sprint. KMP makes the JS SDK a build artefact of the Kotlin one — drift is structurally impossible.

### A17. *"What about iOS — Swift Package Manager, or CocoaPods?"*

SPM, via [Package.swift](../Package.swift) at the repo root pointing at a binary `XCFramework.zip` on GitHub Releases. CocoaPods is **not** supported and not on the roadmap. See `PUBLISHING.md` § 4.3 for the release flow (`assembleStockholmTransportXCFramework` → zip → upload → bump checksum).

---

## Pre-flight checklist (morning of)

- [ ] Charge: laptop, phone (slide remote), backup phone (timer).
- [ ] Verify `~/.m2/repository/com/umain/transport/stockholm-transport/1.0.0/` exists locally.
- [ ] Verify `demo/web/node_modules/StockholmTransport-stockholm-transport/` resolves.
- [ ] Verify Android emulator + iOS simulator both boot in under 60 seconds cold.
- [ ] Open the four required terminals in the right order: gradle / node-api / web dev / git status.
- [ ] Plug into projector 30 minutes early. Test green/black contrast on the venue projector — neon green at #4DFFA8 reads differently on cheap projectors.
- [ ] Conference Wi-Fi: fail-fast. If `curl sl.se` is slow in green-room test, switch to phone hotspot before going on.
- [ ] `docs/demo-fallback/` videos queued in QuickTime, hidden behind `⌘\``.

## Anti-goals

- Don't apologize for things being beta. *"Beta but works on stage"* > *"sorry, beta."*
- Don't read code letter-for-letter. Read the **shape**, point at the **green** lines.
- Don't run `npm install` live. Anything that pulls from the network mid-demo dies.
- Don't say "Claude" or "AI" in the talk. The work is yours; the credits are yours.

## After the talk

- Tweet the screenshot from Slide 14 (memory-leak before/after) within an hour. That's the screenshot people remember.
- Push slides + presenter notes to the repo BEFORE Q&A starts so the audience can grab the URL on the way out.
