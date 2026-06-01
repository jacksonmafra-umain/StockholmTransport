# Stockholm Transport KMP Library

This repository contains a Kotlin Multiplatform (KMP) library that serves as the complete data and logic layer for applications simulating the Stockholm public transport system.

The library is designed to be the single source of truth, handling all data fetching, business logic, and state management. The client applications (Android, iOS, Web) are intentionally "dumb," responsible only for rendering the UI based on the data provided by this library.

For a single, code-anchored map of every artefact in the repo (library modules, demos, CLI, slides, Docker, distribution channels), see [docs/FEATURES.md](docs/FEATURES.md).

More docs: publishing flow (Maven Central / SPM / npm) in [docs/PUBLISHING.md](docs/PUBLISHING.md). (The conference talk deck and presenter notes are personal talk-prep, kept locally and intentionally not tracked in this repo.)

## Architecture

The project strictly follows **Clean Architecture** principles to ensure a clear separation of concerns, testability, and maintainability.

-   **Presentation Layer:** Contains `ViewModels` that manage screen state and expose it to the UI via `StateFlow`.
-   **Domain Layer:** Contains the core business logic, domain models (entities), and repository interfaces (contracts).
-   **Data Layer:** Contains the repository implementations, which are responsible for fetching data from the SL Transport API and mapping it to domain models. It also includes a robust error handling and logging system.

### Module Structure

`settings.gradle.kts` includes a single Gradle subproject `:shared` and **renames it to `:stockholm-transport`** — that's the path you publish, build, and depend on. The `shared/<feature>` directories are *not* separate Gradle projects; they're folder slices pulled into the single module via `kotlin.srcDirs(...)` in [shared/build.gradle.kts](shared/build.gradle.kts).

-   `shared/core` — cross-cutting Ktor HttpClient (with `WebSockets` installed), Koin core module, multiplatform logger, `BaseViewModel<T>`, `DataResult` / `NetworkError`.
-   `shared/lines`, `shared/sites`, `shared/departures`, `shared/stoppoints`, `shared/authorities` — static SDK features. Each follows Clean Architecture internally and registers its repository + ViewModel in a Koin module.
-   `shared/realtime` — realtime feature module (added in the Option C lift-into-library refactor). Owns `Trip`, `Vehicle`, `ActiveTrip`, `TripDisplayInfo`, `Station`, `RealtimeConfig`, plus the `TripUpdateDataSource` (Ktor WebSocket against `/updates/{tripId}`) and the `TripViewModel` / `TripSelectionViewModel`. After Option C, the realtime mobile app and the Node demo bind to these library types directly — they no longer carry duplicates.
-   `shared/src/commonMain/kotlin/com/umain/transport/{di,js}/` — the public Koin bootstrap (`initKoin(realtimeConfig = …)`) and the `StockholmTransportApi` JS façade (`initialize` / `initializeWithRealtime` plus per-feature `getXxxViewModel()` accessors).

## Dev workflow with `sl` (the project CLI)

Running the four front doors (Android, iOS, JVM, Web) from a single backend is the talk-day workflow. The repo ships an interactive CLI at [`tools/sl-cli/`](tools/sl-cli/) that orchestrates the whole loop:

```bash
# one-time setup — run from anywhere
export NGROK_AUTHTOKEN=<your-token>      # https://dashboard.ngrok.com (free tier works)

# REPL mode, from the repo root
./sl
▶ sl › start                              # boots Node API (nodemon) + ngrok, writes URL to gradle.properties
▶ sl › publish                            # rebuilds library: mavenLocal + JS bundle + iOS XCFramework
▶ sl › up                                 # docker compose up -d (mongo + node-api + realtime-api in background)
▶ sl › ps                                 # tabulated docker compose ps
▶ sl › logs realtime-api --tail 50        # tail container logs
▶ sl › seed                               # re-seed Mongo via the realtime-api container
▶ sl › down                               # docker compose down (volume preserved)
▶ sl › clear --yes                        # destructive: down -v (wipes mongo_data)
▶ sl › status                             # ngrok + gradle URL + toolchain + docker stack table
▶ sl › stop                               # stops ngrok/nodemon, restores gradle.properties
▶ sl › exit
```

Or as one-shot subcommands:

```bash
./sl start                  # leaves services running; Ctrl+C to stop and restore
./sl publish --no-ios       # skip the iOS XCFramework (faster)
./sl up                     # boot the docker stack (force-recreates containers, keeps mongo_data)
./sl up mongo realtime-api  # realtime stack only — use this when `./sl start` already holds :3000
./sl logs realtime-api -f   # follow logs
./sl seed                   # force re-seed Mongo (upserts; needs the stack up)
./sl clear --yes            # nuke mongo_data volume (the only data-destroying command)
./sl status
```

`up` force-recreates containers (`--force-recreate --remove-orphans`) and self-heals stale `container_name` conflicts, so it won't fail with "name is already in use"; the `mongo_data` volume is always preserved. **Caveat:** `./sl start` runs the Node API on host `:3000`, and the dockerised `node-api` maps the same port — so run the full `./sl up` only when `start` is *not* active; otherwise use `./sl up mongo realtime-api`.

The `./sl` wrapper auto-runs `npm install` in `tools/sl-cli/` the first time it's invoked, so there's no separate setup step.

For a real global `sl` command (the `gemini`/`claude` experience):

```bash
cd tools/sl-cli && npm link
# now you can run `sl ...` from anywhere
sl start
sl publish
```

What `start` does, in order:
1. Spawns `nodemon` against [`demo/node-api/server.js`](demo/node-api/server.js).
2. Opens an ngrok tunnel to `:3000` via the [@ngrok/ngrok](https://www.npmjs.com/package/@ngrok/ngrok) SDK.
3. Backs up [`gradle.properties`](gradle.properties) → `gradle.properties.sl-cli.bak`.
4. Rewrites `serverHostURL="${ngrokUrl}/v1"` so any subsequent `publishToMavenLocal` bakes the public URL into `BuildConfig.API_BASE_URL`.

The Node API at `:3000` exposes three surfaces:

1. `/modules/{lines,sites,departures,stoppoints,authorities}` — static-SDK ViewModel routes used by the talk's Act 1 demo.
2. `/modules/active-trips` — added after Option C; drives the library's realtime `TripSelectionViewModel` against the simulator. Requires the docker stack running (`./sl up`) so `realtime-api` is reachable.
3. `/v1/*` — passthrough proxy to `https://transport.integration.sl.se/v1/*`. The rebuilt mobile and web apps think they're talking to SL; they're actually talking to your laptop.

`stop` (or `Ctrl+C` / EOF) closes the tunnel, kills nodemon, and restores `gradle.properties` from the backup.

## How to Build and Run the Demo App

The project includes a demo application (`demo/mobile`) that consumes the library and runs on both Android and iOS.

### Prerequisites

1.  **Publish the library locally:** Before you can run the demo app, you must first publish the library to your local Maven repository. From the root directory of the `StockholmTransport` project, run:
    ```bash
    ./gradlew :stockholm-transport:publishToMavenLocal
    ```
    Or, equivalently — and with the JS bundle + iOS XCFramework rebuilt in the same pass:
    ```bash
    ./sl publish
    ```

### Android

1.  Open the `demo/mobile` directory as a separate project in Android Studio.
2.  Let Gradle sync. It will find the library in your local Maven repository.
3.  Select the `composeApp` run configuration.
4.  Click 'Run' (▶️).

### iOS

1.  Open the `demo/mobile` directory in Android Studio and sync Gradle.
2.  Open the Xcode project at `demo/mobile/iosApp/iosApp.xcworkspace`.
3.  Select an iOS simulator and click 'Run' (▶️) in Xcode. The project is already configured to find and embed the KMP shared framework.

### Node demo (standalone)

`demo/node-api` runs on its own with `npm run dev` (nodemon) or `npm start` (plain `node`). When the sl-cli's `start` is active, the Node demo is automatically the upstream behind the ngrok URL. The server reroutes the library's baked `*.ngrok` URL to its own `localhost:3000/v1` proxy, so `/modules/*` returns real SL data **even with no live tunnel** — only real mobile devices need the ngrok URL.

### Web demo (browser SPA)

[`demo/spa-bootstrap`](demo/spa-bootstrap/) is a Vite + React + TypeScript app that consumes the **same** `@jacksonmafra-umain/stockholm-transport` JS package the Node demo does — via the same `file:` dependency. It carries no business logic: five routes each call `useStockholmTransport(factory, loader)`, which wraps the library's `subscribe`/`onCleared` verbatim. The `/leak` route is the memory-leak teaching demo (a live subscription counter that climbs without `onCleared()` and stays flat with it).

```bash
./sl publish                 # build the JS package the app depends on
cd demo/spa-bootstrap && npm install && npm run dev   # http://localhost:5173
```

The browser talks to the same node-api the phones use, but a small dev shim reroutes the baked ngrok URL to `localhost:3000` (skipping ngrok's CORS-less browser interstitial); node-api serves permissive CORS so the cross-origin request succeeds. Like the Node demo, it needs no live tunnel. See [`demo/spa-bootstrap/README.md`](demo/spa-bootstrap/README.md).

## Realtime train-positions stack

A second pair of demos sits alongside the static-API ones — for live train positions streamed over WebSocket:

- **[`demo/realtime-api/`](demo/realtime-api/)** — Express + `ws` + MongoDB simulator. Tick-driven `SimulationEngine` advances vehicles between stops and pushes `WebSocketMessage(currentStop, nextThreeStops, finalDestination)` over `WS /updates/{tripId}`. HTTP routes (`/api/lines`, `/api/sites`, `/api/trips/active`, etc.) feed selection screens. Bumped to `node:22-alpine`, `mongo:8.0`, Express 5, Mongoose 8.18, `multer 2`, `ws 8.18.3` and runs as a non-root container user.
- **[`demo/realtime-mobile/`](demo/realtime-mobile/)** — Compose Multiplatform app (Android + iOS). After Option C this app **does not** carry its own Ktor / Serialization / data classes / repository / ViewModels — it depends on `:stockholm-transport` and consumes `TripViewModel`, `TripSelectionViewModel`, `RealtimeConfig`, and the domain types from the library. Two screens: `TripSelectionScreen` (picks an active trip) and `TripScreen` (subscribes to `/updates/{tripId}` and renders the rolling station info). Package `com.umain.transport.realtime.*`.

Run the realtime stack on its own:

```bash
docker compose up mongo realtime-api
# realtime-api on :3001, mongo on :27017
```

Or the full unified stack (static + realtime + DB) — see the next section.

## Docker — unified stack

[`docker-compose.yml`](docker-compose.yml) at the repo root brings up everything any client could want to consume:

| Service        | Port  | Image / build context                         |
|----------------|-------|------------------------------------------------|
| `mongo`        | 27017 | `mongo:8.0` (volume-backed, healthchecked)    |
| `node-api`     | 3000  | [`demo/node-api/Dockerfile`](demo/node-api/Dockerfile)   |
| `realtime-api` | 3001  | [`demo/realtime-api/Dockerfile`](demo/realtime-api/Dockerfile) |

```bash
./sl publish              # build the JS bundle node-api needs
docker compose up         # all three services
docker compose up mongo realtime-api    # realtime only
docker compose down -v    # tear down + wipe Mongo volume
```

Both Dockerfiles use `node:22-alpine` and run under the unprivileged `node` user. Production images skip dev dependencies (`npm install --omit=dev`).

## How to Use the Library

The library's public API is exposed through its `ViewModels`. From a client application, the flow is always the same:

1.  **Add the Dependency:** Add the library's Maven coordinate to your `build.gradle.kts` or `libs.versions.toml`.
    ```kotlin
    implementation("com.umain.transport:stockholm-transport:1.0.0")
    ```
2.  **Initialize Koin:** Call the `initKoin()` function at your application's entry point.
3.  **Inject the ViewModel:** Use Koin to get an instance of the desired ViewModel (e.g., `koinInject<LinesViewModel>()`).
4.  **Observe State:** Collect the `StateFlow` (`uiState`) exposed by the ViewModel to reactively receive state updates.
5.  **Call Functions:** Invoke public methods on the ViewModel to trigger actions (e.g., `viewModel.loadLines()`).

**Example (in a Jetpack Compose Composable):**

```kotlin
@Composable
fun MyScreen(viewModel: LinesViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLines()
    }

    // Render your UI based on the uiState
    if (uiState.isLoading) {
        // Show a loading indicator
    } else {
        // Show the list of lines
    }
}
```

### Realtime feature usage

The realtime feature ships in the same artifact, but takes its simulator coordinates at runtime via `RealtimeConfig`:

```kotlin
import com.umain.transport.di.initKoin
import com.umain.transport.realtime.RealtimeConfig

initKoin(
    realtimeConfig = RealtimeConfig(
        httpBaseUrl = "http://localhost:3001",
        wsHost      = "localhost",
        wsPort      = 3001,
        wsSecure    = false,
    ),
)
// Now: koinInject<TripSelectionViewModel>() / koinInject<TripViewModel>()
```

JS / Node consumers go through the wrapper:

```js
import * as kmp from '@jacksonmafra-umain/stockholm-transport';
const api = kmp.StockholmTransportApi.getInstance();
api.initializeWithRealtime('http://localhost:3001', 'localhost', 3001, false);
const tripPicker = api.getTripSelectionViewModel();
tripPicker.subscribe(state => { if (!state.isLoading) render(state.activeTrips); });
tripPicker.loadActiveTrips();
```