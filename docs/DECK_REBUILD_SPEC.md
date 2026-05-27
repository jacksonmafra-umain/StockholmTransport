# Deck rebuild spec — `docs/Slides.pen`

Build manifest for the 23-slide deck. Pairs with `SLIDES_PRESENTER_NOTES.md` (which has the *spoken* content per slide). This file has the **visual recipe** so the deck can be rebuilt identically in Pencil after a restart.

> State when written: only `01 — Cover` (`USlNG`) and `02 — About Me` (`D1hHZD`) exist. Build 03→23 fresh. The Pencil `batch_design` write env was broken (I/U undefined) — rebuild once the Pencil.app is restarted.

## Design tokens (already defined in the .pen — use `$name`)

| Token | Value | Use |
|---|---|---|
| `$bg` | #000000 | slide background |
| `$fg` | #FFFFFF | primary text / white title lines |
| `$fgMuted` | #7A8C84 | captions, secondary text |
| `$amber` | #FFE600 | Kotlin-side accent, "cost/warning" emphasis |
| `$neon` | #4DFFA8 | JS-side accent, "positive/payoff" emphasis |
| `$danger` | #FF3D5C | negative/"can't"/drift emphasis |
| `$panel` | #0F1614 | card/panel fill |
| `$panelLine` | #1F2A24 | hairlines, panel strokes |
| `$ff-display` | Geist Mono | big titles (700) |
| `$ff-mono` | Geist Mono | code, labels |
| `$ff-caption` | IBM Plex Mono | kickers, footer timecodes |
| `$ff-body` | Geist | descriptive sentences |

## Frame + grid

- Each slide: `type:"frame", layout:"none", width:1920, height:1080, fill:"$bg", clip:true`.
- Grid: cols x = 0, 2020, 4040, 6060, 8080; rows y = 0, 1180, 2360, 3540, 4720.
- 01 at (0,0), 02 at (2020,0). Then row-major: 03=(4040,0), 04=(6060,0), 05=(8080,0), 06=(0,1180) … 23=(4040,4720).

## Common slide skeleton (every non-divider slide)

Children, in order (coords relative to the frame):
1. top-left label: `text` `"▶  NN · SECTION"`, `$fgMuted`, `$ff-caption`, 18, letterSpacing 2, x:80 y:68
2. top-right counter: `text` `"NN / 23"`, `$fgMuted`, `$ff-caption`, 18, letterSpacing 2, x:1740 y:68
3. hairline: `rectangle` fill `$panelLine`, width 1760, height 2, x:80 y:108
4. kicker: `text` `"// …"`, `$neon`, `$ff-caption`, 22, letterSpacing 4, x:120 y:180
5. title line(s): `text`, `$ff-display`, 96–130, 700, letterSpacing -2, lineHeight 1, x:120, y:215 (line 2 ~ +120)
6. body: panels (`$panel` fill, `$panelLine` stroke, cornerRadius 8) and/or text, y:480+
7. footer caption: `text` `"// …"`, `$fgMuted`, `$ff-caption`, 18, letterSpacing 2, x:120 y:1000

**Divider skeleton** (slides 06, 12, 19): top bar + hairline, then big act number `text` `"01"`/`"02"`/`"03"` in `$amber` `$ff-display` 380 at x:120 y:280; `"ACT"` `$fgMuted` `$ff-caption` 32 ls8 at x:540 y:300; title `$fg` `$ff-display` 180 at x:540 y:360; two neon subtitle lines `$ff-display` 88 at x:540 y:580/680; footer `"// MM:SS"`.

Two slides use an **amber hairline** (`$amber` instead of `$panelLine`) to mark demo slides: the Node demo (11) and the mic-drop demo (22). Optional touch.

## Per-slide build recipe

Format: **NN — name** · grid · kicker · title (color) · body · footer. On-screen text exact; spoken script is in the notes.

### FRAMING

**03 — Why build it twice** · (4040,0) · kicker `// A PRODUCT MANAGER, LOOKING AT THE ROADMAP` · title `WHY BUILD IT` ($fg) / `TWICE?` ($amber), 130 · body panel (1680×230, y:540): `ANDROID → Kotlin` + `iOS → Swift` (mono 30 $fg), then `Same API integration. Same caching. Same validation. Same offline sync.` ($fgMuted body 22), then `Two teams. Two languages. One behaviour. Built twice.` ($danger mono 20). Below panel (y:812/854): `Mobile already answered this — share the logic, keep the UI native.` ($fg 26) / `This talk is about the rebuild nobody mentions: the web is the third one.` ($neon 26 600). · footer `// A KMP LIBRARY IS AN SDK WITH MULTIPLE FRONT DOORS`

**04 — We don't share the UI** · (6060,0) · kicker `// NOT FLUTTER. NOT REACT NATIVE.` · title `WE DON'T` ($fg) / `SHARE THE UI.` ($neon), 110 · body: left panel "SHARED" (neon stroke) listing `networking · repositories · caching · API models · auth · feature flags · offline sync · analytics · pagination · validation`; right panel "NATIVE, ALWAYS" (panelLine) `SwiftUI stays SwiftUI` / `Compose stays Compose` / `native gestures, navigation, a11y`. Cost/why-now strip (amber, y:812): `WHY NOW — new memory manager (no frozen objects) · suspend fun → Swift try await · Stable since 2023 · Google I/O 2024 · Cash App, Netflix, McDonald's`. · footer `// SHARE WHAT MAKES SENSE, KEEP WHAT MATTERS NATIVE`

**05 — Ship an SDK, not an API** · (8080,0) · kicker `// API vs SDK — THE HONEST PITCH` · title `SHIP AN SDK,` ($fg) / `NOT AN API.` ($neon), 110 · body: left panel "API = A CONTRACT" (amber label): `Just URLs you can call.` / `Every client re-implements fetch · parse · error states.` / `→ they drift, platform by platform.` ($danger). right panel "SDK = A LIBRARY" (neon stroke): `TypeScript types — free (.d.mts).` / `Zero fetch / parse boilerplate.` / `You don't learn Kotlin — it's an npm install.` ($neon). Cost strip (amber, y:812): `THE COST — BE HONEST · ~800 KB bundle. Dead weight on a static landing page — pays off on a real app already shipping Android + iOS.` · footer `// FRONTEND DEV: IT'S JUST AN npm PACKAGE WITH GREAT TYPES`

### ACT 1 — THE DREAM

**06 — Act 1 Divider** · (0,1180) · DIVIDER · `01` / `ACT` / `THE DREAM` / `ONE SOURCE OF TRUTH` `FOR EVERY PLATFORM.` (neon) · footer `// 05:30`

**07 — How .kt becomes .js** · (2020,1180) · kicker `// THE COMPILE PIPELINE` · title `HOW .KT` ($fg) / `BECOMES .JS.` ($neon), 120 · body: 4 panels (370×280, y:540, x:120/540/960/1380) each with step `01`–`04` ($fgMuted caption), big symbol ($ff-display 60), label (mono 20 $fg), 2 desc lines ($fgMuted body 16). Symbols+colors: `.kt`/SOURCE/commonMain+jsMain ($amber); `IR`/COMPILER/the js(IR) backend ($amber); `.js`/OUTPUT/+ .d.mts types ($neon); `V8`/RUNTIME/browser · Node ($neon). `▶` arrows ($neonDim 30) at x:500/920/1340 y:650. · footer `// AMBER = KOTLIN WORLD, NEON = JS WORLD — THE HANDOFF IS THE COMPILER`

**08 — One expect, four actuals** · (4040,1180) · kicker `// THE PLATFORM SEAM` · title `ONE expect,` ($fg) / `FOUR actuals.` ($neon), 110 · body: expect box (1100×66, neon stroke, y:440) `commonMain` + `expect fun createHttpClient(): HttpClient`; `▼ one actual per target` ($fgMuted); 4 actual boxes (500×86, y:548, x:120/606/1132/1658): `androidMain → HttpClient(OkHttp)`, `iosMain → HttpClient(Darwin)`, `jvmMain → HttpClient(CIO)`, `jsMain → HttpClient(Js)` (jsMain box neon stroke, others panelLine). · footer `// KMP PICKS THE ENGINE; YOUR CODE NEVER KNOWS`

**09 — Four lines open the browser** · (6060,1180) · kicker `// WHAT DOES js(IR) ACTUALLY DO?` · title `FOUR LINES` ($fg) / `OPEN THE BROWSER.` ($amber), 96 · body: list panel (1680×420, y:540) 4 rows, each `0N` (amber mono) + `browser()` / `useEsModules()` / `generateTypeScriptDefinitions()` / `binaries.executable()` (mono $fg 28) + one-line desc ($fgMuted). · footer `// KOTLIN 2.2 NEEDED -Xes-long-as-bigint — 2.3 MADE Long→BigInt THE DEFAULT`

**10 — Export the behavior, hide the machinery** · (8080,1180) · kicker `// JUST @JsExport EVERYTHING?` · title `EXPORT` `THE BEHAVIOR.` ($neon) / `HIDE` `THE MACHINERY.` ($fg), ~88 left-stacked · right panel (560×680) "IN THIS REPO": big `13` ($amber 120) + `@JsExport.Ignore` + list `StateFlow` / `CoroutineScope` / `Repository constructors`. · footer `// subscribe(callback) IS THE BRIDGE — DETAILS IN ACT 2`

**11 — DEMO Node** · (0,2360) · ⚠ amber hairline · kicker `// DEMO 01` · title `KOTLIN CODE,` ($fg) / `NODE ENGINE.` ($neon) · body: terminal panel (1680×460) showing `$ curl localhost:3000/modules/lines` and a trimmed JSON of metro lines (mono, $neon for keys). · footer `// SAME REPOSITORY. SAME DTO. SAME ERROR MAPPING. ENGINE: KTOR JS / NODE`

### ACT 2 — THE REALITY

**12 — Act 2 Divider** · (2020,2360) · DIVIDER · `02` / `THE REALITY` / `WHERE COROUTINES` `MEET REACT.` (neon) · footer `// 12:30`

**13 — Room can't. SQLDelight can.** · (4040,2360) · kicker `// CHECK THE TARGET BEFORE YOU COMMIT` · title `ROOM CAN'T.` ($danger) / `SQLDELIGHT CAN.` ($neon), 96 · body: left panel "ROOM" (danger stroke): `Annotation-first (@Entity, @Dao).` / `Assumes a native SQLite engine.` / `Targets: JVM · Kotlin/Native.` / `✗ no JS / Wasm driver.` ($danger). right panel "SQLDELIGHT" (neon stroke): `SQL-first → typesafe Kotlin.` / `Pluggable driver per platform.` / `Web Worker driver = SQLite in Wasm.` / `✓ JS · Wasm · Native · JVM.` ($neon). Rule strip (amber, y:812): `THE RULE — Check klibs.io for the JS / Wasm badge BEFORE you commit. iOS hides under the 'Kotlin/Native' badge — don't read it as unsupported.` · footer `// ECOSYSTEM MATURES UNEVENLY — DataStore JUST ADDED JS + WASM (ALPHA)`

**14 — StateFlow → a callback** · (6060,2360) · kicker `// THE BRIDGE` · title `StateFlow →` ($fg) / `A CALLBACK.` ($neon) · body: code panel (1680×440) `fun subscribe(onStateUpdate: (T) -> Unit) {` / `  viewModelScope.launch {` / `    uiState.collect { onStateUpdate(it) }` / `  }` / `}` (mono, subscribe green-highlighted). · footer `// JS GETS A CALLBACK. KOTLIN KEEPS THE FLOW. EVERYBODY WINS.`

**15 — Where do coroutines run?** · (8080,2360) · kicker `// DISPATCHERS ON NODE & THE BROWSER` · title `Dispatchers.Main IS` ($fg) / `THE MICROTASK QUEUE.` ($neon), 80 · body: flow panel (1680×280, centered) `emit()` → `queueMicrotask() in V8` → `your setState` (mono, `▶` arrows neon). · footer `// CAVEAT — MIX WITH RAW PROMISES AND ORDERING IS NOT GUARANTEED. PICK ONE.`

**16 — 12 lines of TypeScript** · (0,3540) · kicker `// THE REACT HOOK · WHY REACT, NOT COMPOSE WEB` · title `12 LINES` ($fg) / `OF TYPESCRIPT.` ($neon) · body: code panel (1680×460) the `useStockholmTransport` hook — highlight `vm.subscribe(setState)` ($neon) and `return () => vm.onCleared()` ($amber). · footer `// REACT — NOT COMPOSE WEB — ON PURPOSE: THE CALLBACK IS PLAIN JS. PROOF THE SDK DOESN'T LEAK KOTLIN.`

**17 — DEMO React** · (2020,3540) · kicker `// DEMO 02` · title `SAME SDK,` ($fg) / `BROWSER UI.` ($neon) · body: browser-frame panel (1680×460) mock of the lines list rendering. · footer `// SAME VIEWMODEL THE PHONES SUBSCRIBE TO. ZERO BUSINESS LOGIC IN THE BROWSER.`

**18 — One line between a library and a leak** · (4040,3540) · kicker `// FORGET onCleared(), WATCH IT LEAK` · title `ONE LINE BETWEEN` ($fg) / `A LIBRARY AND A LEAK.` ($danger), 80 · body: before/after split (1680×480) — left "WITHOUT cleanup" counter `1 → 2 → 4 → 12` ($danger); right "WITH cleanup" counter `1 → 1 → 1` ($neon). · footer `// THIS IS THE SCREENSHOT EVERYONE TWEETS — KEEP IT 90 SECONDS, MAX.`

### ACT 3 — THE PAYOFF

**19 — Act 3 Divider** · (6060,3540) · DIVIDER · `03` / `THE PAYOFF` / `SHIPPING IT.` (neon) · footer `// 22:00`

**20 — Can I npm install this? Not yet.** · (8080,3540) · kicker `// CAN I npm install THIS?` · title `NOT YET.` ($amber) / `HERE'S WHY.` ($fg) · body: left gap list (5 items, $fgMuted): no package.json · name mismatch · no main/module/exports/types · no peerDependencies · npm-publish plugin unwired. right "WHAT REAL LOOKS LIKE" (neon): `@umain/stockholm-transport` · ESM + commonjs2 · peerDeps hoisted · one build → Maven · SPM · npm · (Flutter = Dart wrapper). · footer `// EACH GAP CLOSES IN ~A DAY OF PIPELINE WORK`

**21 — Who's using your SDK?** · (0,4720) · kicker `// IDENTIFYING CONSUMERS · THE TOKEN PATTERN` · title `WHO'S USING` ($fg) / `YOUR SDK?` ($neon) · body: 4-step flow (1680×280) `init(apiKey)` → `SDK stores it` → `injects header` → `backend validates`; below, examples row `FIREBASE · STRIPE pk_/sk_ · SENTRY DSN` ($fgMuted). Warning strip (amber): `TOKEN ≠ USER AUTH — identifies the app, not the human. Never ship a raw key in the JS bundle.` · footer `// BACKEND PROXY, OR RUNTIME-INJECTED TOKEN VIA KOIN`

**22 — DEMO: fix one line, three platforms** · (2020,4720) · ⚠ amber hairline · kicker `// DEMO 03 · MIC DROP` · title `FIX ONE LINE.` ($fg) / `THREE PLATFORMS.` ($neon) · body: steps panel (1680×340) `BREAK → publish → 3 empty → FIX → publish → 3 reload` (mono). · footer `// PLAN B: PRE-RECORDED CAPTURE IF VENUE WI-FI DIES`

**23 — Closing** · (4040,4720) · kicker `// THREE RULES TO TAKE HOME` · body: rules panel (1680×340) `01 · Export the BEHAVIOR. Hide the MACHINERY.` / `02 · Coroutine SCOPE = lifecycle CONTRACT.` / `03 · The SDK boundary is the SAME on every platform.` (mono, numbers amber). Callback line (y:880, neon): `Why build it twice? Share what makes sense, keep what matters native. The web is just the next front door.` QR slot bottom-right. · footer `// Q&A — 33:00`

## Build order (when Pencil recovers)

1. Framing 03–05 (highest narrative value — the new coherent opening).
2. Act 1 dividers + 07–11.
3. Act 2 12–18.
4. Act 3 19–23.
5. Screenshot each act group to verify; fix text overflow (keep mono code lines < ~70 chars at the given font sizes).
6. After the deck: confirm presenter-notes timecodes still match; adjust in rehearsal.
