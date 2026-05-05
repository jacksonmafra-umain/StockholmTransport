# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Kotlin Multiplatform library that wraps the SL (Stockholm public transport) REST API and exposes ViewModels to client apps (Android, iOS, JVM, JS/Browser, Node). The library owns all networking, mapping, and state; consumers only render the `StateFlow` it emits.

API base URL and key come from `gradle.properties` (`serverHostURL`, `apiKey`) and are baked in via the `buildconfig` plugin into `com.umain.transport.config.BuildConfig`.

## Module layout (important — not what it looks like)

`settings.gradle.kts` only includes `:shared` and **renames it to `:stockholm-transport`**. So the Gradle path is `:stockholm-transport`, not `:shared` and not `:library` (despite what `README.MD` says — `PUBLISHING.md` is the accurate reference).

The directories `shared/core`, `shared/lines`, `shared/sites`, `shared/departures`, `shared/stoppoints`, `shared/authorities` look like submodules but are **not** separate Gradle projects. They are pulled into the single `:stockholm-transport` module via `kotlin.srcDirs(...)` in [shared/build.gradle.kts](shared/build.gradle.kts) (see the `commonMain` / `androidMain` / `iosMain` / `jvmMain` / `jsMain` blocks). When adding a new feature folder, you must register its `src/commonMain/kotlin` path there or it will not compile.

`build-logic/` contains convention plugins (`umain.transport.kmp.core`, `umain.transport.kmp.library`, `umain.transport.kmp.compose`) that are declared but not yet applied to `:stockholm-transport`.

## Architecture per feature

Each feature folder follows Clean Architecture:

- `presentation/` — `XxxViewModel` extending [BaseViewModel](shared/core/src/commonMain/kotlin/com/umain/transport/core/presentation/BaseViewModel.kt), exposing `StateFlow<XxxUiState>`.
- `domain/model/` and `domain/repository/` — entities and interfaces.
- `data/model/` — `XxxDto` (kotlinx-serialization) + `toDomain()` mappers.
- `data/repository/` — `XxxRepositoryImpl(httpClient: HttpClient)`, returning `DataResult<T>` and translating Ktor exceptions into `NetworkError` (see [LinesRepositoryImpl](shared/lines/src/commonMain/kotlin/com/umain/transport/lines/data/repository/LinesRepositoryImpl.kt) for the canonical pattern).
- `di/XxxModule.kt` — Koin module binding repo + ViewModel.

A new feature must (1) follow the same package layout, (2) register its `commonMain/kotlin` path in `shared/build.gradle.kts`, (3) be added to the `modules(...)` list in [shared/src/commonMain/kotlin/com/umain/transport/di/Koin.kt](shared/src/commonMain/kotlin/com/umain/transport/di/Koin.kt), and (4) be added as a `getXxxViewModel()` accessor in [JsApi.kt](shared/src/commonMain/kotlin/com/umain/transport/js/JsApi.kt) if it should be reachable from JS.

## JS export rules

JS targets generate TypeScript definitions (`generateTypeScriptDefinitions()`) and run with `-Xes-long-as-bigint`. To stay export-compatible:

- `BaseViewModel.uiState` is `@JsExport.Ignore` because `StateFlow` is not JS-compatible — JS consumers use `viewModel.subscribe(callback)` instead.
- ViewModel constructors that take a repository must annotate the constructor with `@JsExport.Ignore` (see `LinesViewModel`); Koin still uses it from Kotlin.
- The single JS entry point is the `StockholmTransportApi` object in [JsApi.kt](shared/src/commonMain/kotlin/com/umain/transport/js/JsApi.kt) — JS callers should not touch Koin directly.

The `:stockholm-transport` JS webpack output is renamed to `commonjs2` and emitted under `shared/build/js/packages/stockholm-transport/` (see the `KotlinWebpack` configure block).

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

# JS package (output dir printed by the post-build `printJsPackageDirs` task)
./gradlew :stockholm-transport:jsPublicPackageJson

# Clean (also wipes shared/build and output/, which `clean` alone doesn't always reach)
./gradlew cleanAll
```

JDK 17 toolchain (`jvmToolchainVersion=17`); Java source/target pinned to 21 in `shared/build.gradle.kts`. Android `compileSdk=34`, `minSdk=24`. iOS deployment target 14.1.

## Demos

- `demo/mobile/` — standalone Compose Multiplatform app (Android + iOS). Open as a separate Gradle project; it resolves the library from `mavenLocal()`, so re-run `:stockholm-transport:publishToMavenLocal` after every library change. iOS is wired via Xcode at `demo/mobile/iosApp/iosApp.xcworkspace`.
- `demo/node-api/` — Express server in `server.js` that imports the JS package and exposes one HTTP endpoint per feature module. It calls `viewModel.subscribe(...)` and resolves once `state.isLoading` is false, then calls `viewModel.onCleared()`.

## Conventions worth keeping

- Repositories never throw — they return `DataResult.Success` or `DataResult.Error(NetworkError)`. ViewModels translate `NetworkError` into a user-facing string in their own `toUserFriendlyMessage()` (see `LinesViewModel`); don't add UI-string mapping in repositories.
- All logging goes through `com.umain.transport.core.logging.AppLogger` (Kermit-backed, with platform-specific writers under each `*Main` source set). The Ktor `Logging` plugin is wired to it via `KtorLogger`.
- `ignoreUnknownKeys = true` is set on the JSON config — DTOs only need to declare the fields the library actually uses.
