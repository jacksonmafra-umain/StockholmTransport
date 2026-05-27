# Stockholm Transport — Feature Catalogue

A single, code-anchored map of everything the repo ships. Each section answers: *what is this, where does it live, how do I run it, what does it depend on?*

```
StockholmTransport/
├── shared/                       # KMP library — published as :stockholm-transport
│   ├── core/                     # cross-cutting: Ktor (+ WebSockets), Koin, logging, BaseViewModel, DataResult
│   ├── lines/                    # /v1/lines feature
│   ├── sites/                    # /v1/sites feature
│   ├── stoppoints/               # /v1/stop-points feature
│   ├── departures/               # /v1/sites/{id}/departures feature
│   ├── authorities/              # /v1/transport-authorities feature
│   └── realtime/                 # /api/trips/active (HTTP) + /updates/{tripId} (WS) — Option C lift
│
├── demo/
│   ├── mobile/                   # Compose Multiplatform demo of the static SDK (talk Act 1/2)
│   ├── node-api/                 # Express + KMP/JS bundle (talk Act 1 demo)
│   ├── realtime-api/             # Express + ws + MongoDB — train-position simulator
│   └── realtime-mobile/          # Compose Multiplatform app for realtime trip display
│
├── tools/sl-cli/                 # Interactive REPL: ngrok start, publish, status, stop
├── sl                            # Repo-root wrapper for the CLI
│
├── docs/
│   ├── Slides.pen                # Pencil deck for mDevCamp 2026
│   ├── SLIDES_PRESENTER_NOTES.md
│   └── FEATURES.md               # this file
│
├── Package.swift                 # SPM manifest — iOS distribution
└── docker-compose.yml            # mongo + node-api + realtime-api in one stack
```

---

## 1. The KMP library — `:stockholm-transport`

Single Maven artefact `com.umain.transport:stockholm-transport:1.0.0` shipping Android (AAR), JVM, JS (NPM-shaped), and iOS (XCFramework + SPM). Built from one Gradle module that staples six feature folders together via `kotlin.srcDirs(...)` in [shared/build.gradle.kts](../shared/build.gradle.kts).

**Per-feature stack.** Each folder follows Clean Architecture identically:

| Layer        | Lives in                                  | Responsibility                                               |
|--------------|-------------------------------------------|--------------------------------------------------------------|
| presentation | `<feature>/presentation/`                 | `XxxViewModel : BaseViewModel<XxxUiState>`                   |
| domain       | `<feature>/domain/{model,repository}/`    | Entities + repo interfaces                                   |
| data         | `<feature>/data/{model,repository}/`      | DTOs (kotlinx-serialization) + Ktor-backed repo impls        |
| di           | `<feature>/di/XxxModule.kt`               | Koin bindings (`single<Repo>`, `factoryOf(::ViewModel)`)     |

### 1.1 `core` — cross-cutting

| Artefact                                                                                                               | What it does                                                                                                          |
|------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| [BaseViewModel](../shared/core/src/commonMain/kotlin/com/umain/transport/core/presentation/BaseViewModel.kt)            | Owns a `viewModelScope` (Main + SupervisorJob). Exposes `subscribe(callback)` for JS, `onCleared()` for cleanup.       |
| [DataResult / NetworkError](../shared/core/src/commonMain/kotlin/com/umain/transport/core/data/DataResult.kt)           | Sealed result type. Repos never throw — they return `Success(T)` or `Error(NetworkError)`.                            |
| [ApiClient](../shared/core/src/commonMain/kotlin/com/umain/transport/core/network/ApiClient.kt)                         | Single `HttpClient` factory: ContentNegotiation(json), Logging, 15s timeouts, default `key=` query, base URL from `BuildConfig.API_BASE_URL`. |
| [AppLogger](../shared/core/src/commonMain/kotlin/com/umain/transport/core/logging/AppLogger.kt)                         | Kermit-backed logger with platform-specific writers (`.android`, `.ios`, `.jvm`, `.js` actuals).                       |
| [Koin module](../shared/core/src/commonMain/kotlin/com/umain/transport/core/di/Koin.kt)                                | `coreModule { single { createHttpClient() } }`.                                                                        |

### 1.2 Feature modules at a glance

| Module        | Endpoint                                            | Domain types                       | ViewModel actions                      |
|---------------|-----------------------------------------------------|------------------------------------|----------------------------------------|
| `lines`       | `GET /v1/lines?transport_authority_id={id}`         | `Line(id, name, designation, transportMode: String, authority)`  | `loadLines()`        |
| `sites`       | `GET /v1/sites`                                     | `Site(id, name, abbreviation, lat, lon)`                         | `loadSites()`        |
| `stoppoints`  | `GET /v1/stop-points`                               | `StopPoint(id, name, ...)`                                       | `loadStopPoints()`   |
| `departures`  | `GET /v1/sites/{siteId}/departures`                 | `Departure(line, destination, displayTime, ...)`                 | `loadDepartures(siteId)` |
| `authorities` | `GET /v1/transport-authorities`                     | `Authority(id, name)`                                            | `loadAuthorities()`  |
| `realtime`    | `GET /api/trips/active` + `WS /updates/{tripId}`    | `Trip`, `Vehicle`, `ActiveTrip`, `TripDisplayInfo`, `Station`, `RealtimeConfig` | `loadActiveTrips()` (selection) / `startObservingTrip(tripId)` (display) |

Every state is a JS-friendly shape — flat lists where possible, strings instead of Kotlin enums on the boundary (so `JSON.stringify` produces clean output for Node/browser consumers).

The `realtime` module is the only feature whose backend is **runtime-configured**: it takes a [RealtimeConfig](../shared/realtime/src/commonMain/kotlin/com/umain/transport/realtime/RealtimeConfig.kt) instead of `BuildConfig`-baked URLs. Pass it into `initKoin(realtimeConfig = …)` (Kotlin) or `StockholmTransportApi.initializeWithRealtime(httpBaseUrl, wsHost, wsPort, wsSecure)` (JS). The simulator URL changes per environment (`localhost:3001` in dev, `realtime-api:3000` inside docker, an ngrok URL on talk day) so it can't be a compile-time constant.

### 1.3 JS export contract

Two lines summarise the discipline:

> **Export the behavior. Hide the machinery.**

- `BaseViewModel.uiState` is `@JsExport.Ignore` — JS consumers go through `subscribe(callback)`. Visibility is `public` (not `protected`) so Kotlin/Compose/SwiftUI consumers can still `collectAsState()` directly.
- Every `XxxViewModel` constructor that takes a repository is `@JsExport.Ignore` (Koin still uses it from Kotlin).
- The single JS entrypoint is the `StockholmTransportApi` object in [JsApi.kt](../shared/src/commonMain/kotlin/com/umain/transport/js/JsApi.kt). JS callers do `kmp.StockholmTransportApi.getInstance()`, then `.initialize()` (static SDK only) or `.initializeWithRealtime(httpBaseUrl, wsHost, wsPort, wsSecure)` (also wires the realtime feature), then `.getLinesViewModel()`, `.getTripSelectionViewModel()`, etc.
- TypeScript `.d.mts` is auto-generated.

### 1.4 Build and publish

```bash
./sl publish                                  # mavenLocal + JS bundle + iOS XCFramework
./gradlew :stockholm-transport:allTests       # all KMP targets
./gradlew :stockholm-transport:jvmTest        # JVM only (fastest)
```

---

## 2. The demos

### 2.1 `demo/node-api/` — static-API demo (talk Act 1)

Express server wrapping the `:stockholm-transport` JS bundle.

| Surface                                | Use                                                   |
|----------------------------------------|-------------------------------------------------------|
| `GET /modules`                         | List the six feature ViewModels (five static + realtime). |
| `GET /modules/lines`                   | Drives the `LinesViewModel`, returns its UiState.     |
| `GET /modules/{lines\|sites\|departures\|stoppoints\|authorities}` | Same pattern for every static feature.                |
| `GET /modules/active-trips`            | Drives the realtime `TripSelectionViewModel`. Needs `./sl up` so `realtime-api` is reachable. |
| `ALL  /v1/*`                           | Passthrough proxy onto `https://transport.integration.sl.se/v1/*`. The library's `httpClient.get("v1/lines")` lands here when `serverHostURL` points at this server (the sl-cli wires that automatically). |

Run: `cd demo/node-api && npm run dev` (nodemon) or `npm start` (plain `node`). Boots in a few seconds, prints `🚀 Demo API server listening on http://localhost:3000`.

### 2.2 `demo/mobile/` — Compose Multiplatform static-SDK demo

Separate Gradle project. Resolves the library from `mavenLocal()`, so re-run `./sl publish` after every library change. iOS is wired via the Xcode workspace at `demo/mobile/iosApp/iosApp.xcworkspace` with an `embedAndSignAppleFrameworkForXcode` pre-build step — no manual XCFramework drag.

What it shows: list of transport lines / sites / departures / stop points / authorities, each as a separate Compose screen powered by its corresponding ViewModel.

### 2.3 `demo/realtime-api/` — train-position simulator

Express + `ws` + MongoDB. Imported from a sibling project and security-bumped: `node:22-alpine`, `mongo:8.0`, Express 5, Mongoose 8.18, multer 2, ws 8.18. Runs as the non-root `node` user.

| Surface                                       | Use                                                                          |
|-----------------------------------------------|------------------------------------------------------------------------------|
| `GET /api/lines?mode=metro`                   | Lines filtered by transport mode.                                            |
| `GET /api/sites`                              | Paginated stops.                                                             |
| `GET /api/stops/:id/board`                    | Live timetable for a stop.                                                   |
| `GET /api/lines/:code/sites`                  | Stops served by a line code (e.g. `10`).                                     |
| `GET /api/lines/id/:objectId/sites`           | Stops served by a line by Mongo ObjectId.                                    |
| `GET /api/vehicles`                           | All current vehicle positions (GeoJSON).                                     |
| `GET /api/trips/active`                       | Active simulated trips — drives the picker in `realtime-mobile`.             |
| `POST /api/admin/import-trafiklab`            | Re-seed from Trafiklab OpenAPI snapshots in `data/`.                         |
| `WS /updates/{tripId}`                        | Live stream of `WebSocketMessage(currentStop, nextThreeStops, finalDestination)` for a trip. |

Run standalone: `cd demo/realtime-api && docker compose up`.

The simulator engine (`application/SimulationEngine.js`) advances vehicles between stops on a fixed tick and pushes the new position out via `wss.clientsByTrip` — every connected mobile/web client sees the same trip in lockstep.

#### Seeding + auto-start (zero-touch first boot)

`docker compose up` is enough — the realtime-api container's command is `npm run docker:start`, which is `node scripts/bootstrap.js && npm run dev`. Bootstrap is **idempotent** (counts the `Stop` collection; no-ops if already populated):

1. **Pass 1** — `scripts/seed-from-trafiklab.js` reads `data/{lines,sites,stop-points,departures,transport-authorities}.json` validated against `openapi.json` and inserts `Stop` / `Line` / `Timetable` / `Vehicle` documents.
2. **Pass 2** — `scripts/seed-routes-to-lines.js` walks the inline `routesData` (Pendeltåg, Tunnelbana, Tvärbanan, …) and writes the ordered station list onto each `Line.stops`, which is what `SimulationEngine.startTrip` advances through.

Then `presentation/server.js` boots and on `listen` calls:

- `new VehicleSimulator().start()` — interval tick (default 5 s) that walks every running `Vehicle` along its `Timetable.stopTimes`, interpolating GeoJSON position between current and next stop.
- `autoStartDemoTrips()` — picks one `Line` per transport mode whose `stops` is non-empty and `SimulationEngine.startTrip()`s each one. The mobile/web clients see them immediately at `GET /api/trips/active`.

Force a re-seed: `docker compose down -v && docker compose up`.

### 2.4 `demo/realtime-mobile/` — Compose Multiplatform realtime app

After **Option C** this app stopped carrying its own data / domain / repository / ViewModel duplicates. It depends on `:stockholm-transport` and pulls everything from the library:

- `TripViewModel`, `TripSelectionViewModel`, `RealtimeConfig` from `com.umain.transport.realtime.{presentation,*}`.
- Domain types `Trip`, `Vehicle`, `ActiveTrip`, `TripDisplayInfo`, `Station` from `com.umain.transport.realtime.domain.model`.
- The Ktor `HttpClient` with `WebSockets` installed comes from `coreModule` — no `ktor-client-*` deps in this app's `build.gradle.kts`.

Two screens (Material 3, no map yet — cards only):

1. `TripSelectionScreen` — `koinInject<TripSelectionViewModel>()`, calls `loadActiveTrips()` on first composition, renders the resulting `state.activeTrips`.
2. `TripScreen` — `koinInject<TripViewModel>()`, calls `startObservingTrip(tripId)` on first composition, renders `state.displayInfo` (`Station` typed, station names accessed via `.name`).

DI bootstrap delegates to the library: [AppModule.kt](../demo/realtime-mobile/composeApp/src/commonMain/kotlin/com/umain/transport/realtime/di/AppModule.kt) builds a `RealtimeConfig` from `BuildConfig.SERVER_HOST_URL` / `SERVER_HOST` / `SERVER_PORT` and passes it into `com.umain.transport.di.initKoin(realtimeConfig = …)`.

Stack: Kotlin 2.3.21, AGP 9.1.1, Compose 1.10.3, Koin 4.2.1 + Koin Compiler Plugin 1.0.0-RC2, JDK 21. Targets Android (minSdk 30, compileSdk 36) and iOS (arm64 / sim arm64 / x64). AGP 9 needs `android.builtInKotlin=false` + `android.newDsl=false` to coexist with `com.android.application` + the KMP plugin — set in [demo/realtime-mobile/gradle.properties](../demo/realtime-mobile/gradle.properties).

---

## 3. The `sl` CLI

REPL + one-shot CLI that orchestrates the four-platform demo loop. See [tools/sl-cli/README.md](../tools/sl-cli/README.md) for the full command reference; the headline:

| Command   | What it does |
|-----------|-------------|
| `sl start` | `nodemon demo/node-api/server.js`, open ngrok tunnel on `:3000`, write `serverHostURL=${ngrokUrl}/v1` into `gradle.properties` (with backup). |
| `sl publish [--no-ios]` | `:stockholm-transport:publishToMavenLocal` + `jsBrowserDistribution` + `assembleStockholmTransportXCFramework` + `npm install` in `demo/node-api`. Auto-detects `ANDROID_HOME` and a JDK 21 `JAVA_HOME`. |
| `sl status` | Shows: Node API up/down, ngrok URL, current `serverHostURL`, detected toolchain paths, pending backup. |
| `sl stop` | Closes the tunnel, `SIGTERM`s nodemon, restores `gradle.properties` from the `.sl-cli.bak` backup. |

Two entrypoints, same dispatcher:

- `./sl …` from anywhere in the repo (the wrapper `cd`s itself, auto-installs CLI deps on first run).
- `npm link` inside `tools/sl-cli/` for a real PATH-resident `sl` command (the `gemini`/`claude` experience).

---

## 4. Docker — the unified stack

[docker-compose.yml](../docker-compose.yml) at the repo root runs all three services together:

```bash
./sl publish      # produce the JS bundle node-api needs
docker compose up
```

| Service        | Port | Image / build context                | Notes                                     |
|----------------|------|--------------------------------------|-------------------------------------------|
| `mongo`        | 27017 | `mongo:8.0`                          | Volume-backed; healthcheck via `mongosh`. |
| `node-api`     | 3000 | `demo/node-api/Dockerfile` (node:22-alpine) | Static SDK demo. No DB dep.               |
| `realtime-api` | 3001 | `demo/realtime-api/Dockerfile` (node:22-alpine) | Train-position simulator + Mongo dep.     |

`docker compose up mongo realtime-api` runs only the realtime side. `docker compose down -v` wipes the seeded simulator state cleanly.

---

## 5. Distribution surfaces

### 5.1 Android / JVM (Maven)

Coordinate `com.umain.transport:stockholm-transport:1.0.0`. Local-dev: `./sl publish` populates `~/.m2`. Public publishing flow (Maven Central + GitHub Packages) is documented in [PUBLISHING.md](../PUBLISHING.md) §4.

### 5.2 iOS (Swift Package Manager)

[Package.swift](../Package.swift) at the repo root declares a `StockholmTransport` library wrapping a binary `XCFramework` zip on GitHub Releases. Consumers add this repo URL in Xcode → *Add Package Dependencies…*. CocoaPods is **not** supported.

For local iOS development, use the build-script integration: in Xcode → Scheme → Build → Pre-actions, run

```bash
cd "$SRCROOT/../../"
./gradlew :stockholm-transport:embedAndSignAppleFrameworkForXcode
```

— and Xcode rebuilds and embeds the latest Kotlin output every time you `⌘B`.

### 5.3 JavaScript (npm)

Kotlin/JS emits a generated `package.json` under `build/js/packages/StockholmTransport-stockholm-transport/`, with `main`, `module`, and `types: kotlin/StockholmTransport-stockholm-transport.d.mts`. The Node demo consumes it via `file:` dep; full publishing to a scoped name like `@umain/stockholm-transport` is the next step (deferred — needs the `npm-publish` Gradle plugin wired in).

---

## 6. Talk artefacts

For the mDevCamp 2026 conference talk (2026-06-04, 35 min):

- **Deck** — [docs/Slides.pen](Slides.pen). 21 cyberpunk-styled slides in a 3-act narrative (Dream → Reality → Payoff), opened in Pencil.
- **Presenter notes** — [docs/SLIDES_PRESENTER_NOTES.md](SLIDES_PRESENTER_NOTES.md). Per-slide spoken script, demo cues with command sequences and timing caps, pre-flight checklist, anti-goals.

---

## 7. What's still ahead

Tracked in the repo's todo list (and called out in [PUBLISHING.md](../PUBLISHING.md) where they're publish-related):

- ~~`shared/realtime/` library module — promote the train-positions data layer from `demo/realtime-mobile/` into the library proper.~~ **Done (Option C):** `Trip`, `Vehicle`, `ActiveTrip`, `TripDisplayInfo`, `Station`, `RealtimeConfig`, `TripUpdateDataSource`, `TripRepository(Impl)`, `TripViewModel`, `TripSelectionViewModel` now live in `:stockholm-transport`. Realtime mobile + node-api both bind to library types.
- `demo/web/` React + Vite app — the talk's Act 2 centerpiece (`useStockholmTransport` hook + memory-leak before/after route).
- `npm-publish` Gradle plugin — actually publish a scoped `@umain/stockholm-transport` to npm with `peerDependencies` for Ktor/Koin/coroutines.
- Auth on `/api/admin/import-trafiklab` in the realtime API.
- Map rendering inside `realtime-mobile/TripScreen` (currently stops at Material3 cards).
