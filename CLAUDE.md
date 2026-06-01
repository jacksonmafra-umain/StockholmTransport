# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Kotlin Multiplatform library that wraps the SL (Stockholm public transport) REST API **and** owns the realtime trip-stream layer (Ktor WebSocket against the bundled simulator). It exposes ViewModels to client apps (Android, iOS, JVM, JS/Browser, Node). The library owns all networking, mapping, and state; consumers only render the `StateFlow` it emits.

The static-SDK API base URL + key come from `gradle.properties` (`serverHostURL`, `apiKey`) and are baked in via the `buildconfig` plugin into `com.umain.transport.config.BuildConfig`. The realtime backend coordinates are passed at runtime as a [RealtimeConfig](shared/realtime/src/commonMain/kotlin/com/umain/transport/realtime/RealtimeConfig.kt) — they vary by environment (`localhost:3001` in dev, `realtime-api:3000` inside docker, an ngrok URL on talk day) so they can't live in BuildConfig.

## Module layout (important — not what it looks like)

`settings.gradle.kts` only includes `:shared` and **renames it to `:stockholm-transport`**. So the Gradle path is `:stockholm-transport`, not `:shared` and not `:library`. The publishing flow (Maven Central / SPM / npm) lives in [docs/PUBLISHING.md](docs/PUBLISHING.md).

The directories `shared/core`, `shared/lines`, `shared/sites`, `shared/departures`, `shared/stoppoints`, `shared/authorities`, `shared/realtime` look like submodules but are **not** separate Gradle projects. They are pulled into the single `:stockholm-transport` module via `kotlin.srcDirs(...)` in [shared/build.gradle.kts](shared/build.gradle.kts) (see the `commonMain` / `androidMain` / `iosMain` / `jvmMain` / `jsMain` blocks). When adding a new feature folder, you must register its `src/commonMain/kotlin` path there or it will not compile.

`build-logic/` contains convention plugins (`umain.transport.kmp.core`, `umain.transport.kmp.library`, `umain.transport.kmp.compose`) that are declared but not yet applied to `:stockholm-transport`.

## Architecture per feature

Each feature folder follows Clean Architecture:

- `presentation/` — `XxxViewModel` extending [BaseViewModel](shared/core/src/commonMain/kotlin/com/umain/transport/core/presentation/BaseViewModel.kt), exposing `StateFlow<XxxUiState>`.
- `domain/model/` and `domain/repository/` — entities and interfaces.
- `data/model/` — `XxxDto` (kotlinx-serialization) + `toDomain()` mappers.
- `data/repository/` — `XxxRepositoryImpl(httpClient: HttpClient)`, returning `DataResult<T>` and translating Ktor exceptions into `NetworkError` (see [LinesRepositoryImpl](shared/lines/src/commonMain/kotlin/com/umain/transport/lines/data/repository/LinesRepositoryImpl.kt) for the canonical pattern).
- `di/XxxModule.kt` — Koin module binding repo + ViewModel.

A new feature must (1) follow the same package layout, (2) register its `commonMain/kotlin` path in `shared/build.gradle.kts`, (3) be added to the `modules(...)` list in [shared/src/commonMain/kotlin/com/umain/transport/di/Koin.kt](shared/src/commonMain/kotlin/com/umain/transport/di/Koin.kt), and (4) be added as a `getXxxViewModel()` accessor in [JsApi.kt](shared/src/commonMain/kotlin/com/umain/transport/js/JsApi.kt) if it should be reachable from JS.

### Realtime feature module

[shared/realtime](shared/realtime/src/commonMain/kotlin/com/umain/transport/realtime/) is the only feature that is **runtime-configured** rather than BuildConfig-baked. Its [RealtimeConfig](shared/realtime/src/commonMain/kotlin/com/umain/transport/realtime/RealtimeConfig.kt) is passed into `initKoin(realtimeConfig = …)` (see [Koin.kt](shared/src/commonMain/kotlin/com/umain/transport/di/Koin.kt) and the JS `initializeWithRealtime(...)` bridge in [JsApi.kt](shared/src/commonMain/kotlin/com/umain/transport/js/JsApi.kt)). The data source ([TripUpdateDataSource](shared/realtime/src/commonMain/kotlin/com/umain/transport/realtime/data/remote/TripUpdateDataSource.kt)) opens a Ktor `webSocket(...)` against `/updates/{tripId}`, which means the core HttpClient must have `install(WebSockets)` — it does. After Option C, every realtime client (Android, iOS, browser, Node) binds to the same library types: `Trip`, `Vehicle`, `ActiveTrip`, `TripDisplayInfo`, `Station`. Demos stopped carrying their own copies.

## JS export rules

JS targets generate TypeScript definitions (`generateTypeScriptDefinitions()`). Kotlin 2.3 maps `Long → BigInt` by default, so the legacy `-Xes-long-as-bigint` flag is gone. To stay export-compatible:

- `BaseViewModel.uiState` is `@JsExport.Ignore` because `StateFlow` is not JS-compatible — JS consumers use `viewModel.subscribe(callback)` instead. The property is `public open` (not `protected`) so Kotlin consumers (Compose, SwiftUI, JVM tests) can still `collectAsState()` it.
- ViewModel constructors that take a repository must annotate the constructor with `@JsExport.Ignore` (see `LinesViewModel`); Koin still uses it from Kotlin.
- The single JS entry point is the `StockholmTransportApi` object in [JsApi.kt](shared/src/commonMain/kotlin/com/umain/transport/js/JsApi.kt) — JS callers should not touch Koin directly. Use `initialize()` for the static SDK or `initializeWithRealtime(httpBaseUrl, wsHost, wsPort, wsSecure)` to also wire the realtime feature.

Kotlin/JS `object` declarations export as a class with a static `getInstance()` — JS code starts with `kmp.StockholmTransportApi.getInstance()`, then calls `initialize()` / `initializeWithRealtime(...)` exactly once.

The `:stockholm-transport` JS webpack output is renamed to `commonjs2` and emitted under `build/js/packages/StockholmTransport-stockholm-transport/` (the gradle output dir name reflects both the XCFramework name and the Maven artifactId; the Node demo `Dockerfile` references that exact path). The `package.json` inside that directory is then polished by the `:stockholm-transport:enhanceNpmPackageMetadata` Gradle task to advertise the scoped npm name `@jacksonmafra-umain/stockholm-transport` plus modern `module` / `exports` / `types` / `files` fields — that's the name consumers import (`import * as kmp from '@jacksonmafra-umain/stockholm-transport'`). `./sl publish` chains `packTalkTgz` afterwards, producing `build/distributions/npm/jacksonmafra-umain-stockholm-transport-<version>.tgz` — the artefact a registry would receive.

## Common commands

All commands run from the repo root.

```bash
# Publish library to ~/.m2 (required before running the mobile demo)
./gradlew :stockholm-transport:publishToMavenLocal

# Build everything
./gradlew :stockholm-transport:build

# Tests
./gradlew :stockholm-transport:allTests          # all KMP targets
./gradlew :stockholm-transport:jvmTest           # JVM only (fastest feedback)
./gradlew :stockholm-transport:jvmTest --tests "com.umain.transport.lines.*"  # single class/package

# iOS XCFramework (used by the iosApp Xcode build script)
./gradlew :stockholm-transport:assembleStockholmTransportXCFramework

# JS package (polishes package.json + packs the npm tarball)
./gradlew :stockholm-transport:packTalkTgz
# → build/distributions/npm/jacksonmafra-umain-stockholm-transport-<version>.tgz

# Clean (also wipes shared/build and output/, which `clean` alone doesn't always reach)
./gradlew cleanAll
```

JDK 21 toolchain end-to-end (`jvmToolchainVersion=21` in `gradle.properties`, Java source/target pinned to 21 in `shared/build.gradle.kts`). Android `compileSdk=36`. Kotlin 2.3.21 + AGP 9.1.1 + Gradle 9.3.1 + Koin 4.2.1 with the [io.insert-koin.compiler.plugin](https://insert-koin.io/) compile-time plugin (not KSP).

## Demos

- `demo/mobile/` — standalone Compose Multiplatform app (Android + iOS) consuming the static feature ViewModels. Open as a separate Gradle project; it resolves the library from `mavenLocal()`, so re-run `:stockholm-transport:publishToMavenLocal` after every library change. iOS is wired via Xcode at `demo/mobile/iosApp/iosApp.xcworkspace`.
- `demo/realtime-mobile/` — Compose Multiplatform app exercising the realtime feature. Imports `TripViewModel`, `TripSelectionViewModel`, `RealtimeConfig`, and the domain types from `:stockholm-transport`; carries no Ktor / Serialization / data-class duplicates of its own (Option C lifted them into the library). Wired via [AppModule.kt](demo/realtime-mobile/composeApp/src/commonMain/kotlin/com/umain/transport/realtime/di/AppModule.kt) which delegates to `com.umain.transport.di.initKoin(realtimeConfig = …)`.
- `demo/node-api/` — Express server in `server.js` that imports the JS package and exposes one HTTP endpoint per feature module. It calls `viewModel.subscribe(...)` and resolves once `state.isLoading` is false, then calls `viewModel.onCleared()`. After Option C it also exposes `/modules/active-trips` driven by `TripSelectionViewModel`. Also sends permissive CORS headers so the browser SPA can reach the `/v1` passthrough cross-origin. `server.js` patches `globalThis.fetch` to rewrite the baked `*.ngrok` URL to `http://localhost:3000`, so the library's own static-SDK calls hit this server's `/v1` proxy directly — the `/modules/*` demo works even when no ngrok tunnel is live (which dead URL is baked no longer matters). Mobile clients still use the real tunnel.
- `demo/spa-bootstrap/` — Vite + React + TypeScript browser SPA consuming the **same** `@jacksonmafra-umain/stockholm-transport` JS package via the same `file:` dep as the Node demo. No business logic: each route uses `useStockholmTransport(factory, loader)` (in `src/hooks/useStockholmTransport.ts`), which wraps the library's `subscribe`/`onCleared`. The `/leak` route is the memory-leak demo (slide 19 after the new Case Study slide pushed numbering). Run `./sl publish` first, then `cd demo/spa-bootstrap && npm install && npm run dev`. TS types come from the hand-written `src/types/kmp.d.ts` shim, kept as a fallback while consumers migrate to the auto-emitted `.d.mts` that now ships with the polished `package.json`.
- `demo/realtime-api/` — Express + ws + MongoDB simulator that produces the `/updates/{tripId}` stream and the `/api/trips/active` snapshot the library's realtime feature consumes. Brought up via `./sl up` (docker compose).

## Conventions worth keeping

- Repositories never throw — they return `DataResult.Success` or `DataResult.Error(NetworkError)`. ViewModels translate `NetworkError` into a user-facing string in their own `toUserFriendlyMessage()` (see `LinesViewModel`); don't add UI-string mapping in repositories.
- All logging goes through `com.umain.transport.core.logging.AppLogger` (Kermit-backed, with platform-specific writers under each `*Main` source set). The Ktor `Logging` plugin is wired to it via `KtorLogger`.
- `ignoreUnknownKeys = true` and `coerceInputValues = true` are set on the JSON config ([ApiClient.kt](shared/core/src/commonMain/kotlin/com/umain/transport/core/network/ApiClient.kt)) — DTOs only declare the fields the library uses, and a present-but-null value degrades to its default. The SL API also *omits* fields on some records (e.g. sites without `lat/lon`, departure `stop_point`s without `gid/lat/lon`), so any field that isn't read by the domain mapper is declared optional (nullable + default); mappers fall back to `0.0`/`""`. A missing required field fails the whole list, so keep non-essential DTO fields optional.
