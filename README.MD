# Stockholm Transport KMP Library

This repository contains a Kotlin Multiplatform (KMP) library that serves as the complete data and logic layer for applications simulating the Stockholm public transport system.

The library is designed to be the single source of truth, handling all data fetching, business logic, and state management. The client applications (Android, iOS, Web) are intentionally "dumb," responsible only for rendering the UI based on the data provided by this library.

## Architecture

The project strictly follows **Clean Architecture** principles to ensure a clear separation of concerns, testability, and maintainability.

-   **Presentation Layer:** Contains `ViewModels` that manage screen state and expose it to the UI via `StateFlow`.
-   **Domain Layer:** Contains the core business logic, domain models (entities), and repository interfaces (contracts).
-   **Data Layer:** Contains the repository implementations, which are responsible for fetching data from the SL Transport API and mapping it to domain models. It also includes a robust error handling and logging system.

### Module Structure

The library is highly modularized to promote reuse and separation of concerns.

-   `shared`: The main "umbrella" module responsible for creating the final XCFramework for iOS. It exports the APIs of all submodules.
-   `library`: A dedicated "publication" module that aggregates all other modules to create a single, clean artifact for Maven (for Android, JVM, and JS consumers).
-   `shared/core`: Contains cross-cutting code, such as the network client (Ktor) setup, dependency injection (Koin) definitions, and the multiplatform logger.
-   `shared/{feature}`: Each main API entity (e.g., `lines`, `sites`, `departures`) has its own module. Each of these modules follows the Clean Architecture structure internally.

## Dev workflow with `sl` (the project CLI)

Running the four front doors (Android, iOS, JVM, Web) from a single backend is the talk-day workflow. The repo ships an interactive CLI at [`tools/sl-cli/`](tools/sl-cli/) that orchestrates the whole loop:

```bash
# one-time setup
cd tools/sl-cli && npm install
export NGROK_AUTHTOKEN=<your-token>      # https://dashboard.ngrok.com

# from the repo root, REPL mode
./tools/sl-cli/bin/sl.js
▶ sl › start                              # boots Node API (nodemon) + ngrok, writes URL to gradle.properties
▶ sl › publish                            # rebuilds library: mavenLocal + JS bundle + iOS XCFramework
▶ sl › status                             # prints running services + current API URL
▶ sl › stop                               # stops services, restores gradle.properties
▶ sl › exit
```

Or as one-shot subcommands:

```bash
./tools/sl-cli/bin/sl.js start            # leaves services running; Ctrl+C to stop and restore
./tools/sl-cli/bin/sl.js publish --no-ios # skip the iOS XCFramework (faster)
./tools/sl-cli/bin/sl.js status
```

What `start` does, in order:
1. Spawns `nodemon` against [`demo/node-api/server.js`](demo/node-api/server.js).
2. Opens an ngrok tunnel to `:3000` via the [@ngrok/ngrok](https://www.npmjs.com/package/@ngrok/ngrok) SDK.
3. Backs up [`gradle.properties`](gradle.properties) → `gradle.properties.sl-cli.bak`.
4. Rewrites `serverHostURL="${ngrokUrl}/v1"` so any subsequent `publishToMavenLocal` bakes the public URL into `BuildConfig.API_BASE_URL`.

The Node API at `:3000` exposes two surfaces: the original `/modules` ViewModel routes (used by [Slide 08 – DEMO Node](docs/SLIDES_PRESENTER_NOTES.md)), and a passthrough proxy at `/v1/*` that forwards onto `https://transport.integration.sl.se/v1/*`. The proxy is what the rebuilt mobile and web apps consume — they think they're talking to SL, they're actually talking to your laptop.

`stop` (or `Ctrl+C` / EOF) closes the tunnel, kills nodemon, and restores `gradle.properties` from the backup.

## How to Build and Run the Demo App

The project includes a demo application (`demo/compose-app`) that consumes the library and runs on both Android and iOS.

### Prerequisites

1.  **Publish the library locally:** Before you can run the demo app, you must first publish the library to your local Maven repository. From the root directory of the `StockholmTransport` project, run:
    ```bash
    ./gradlew :stockholm-transport:publishToMavenLocal
    ```
    Or, equivalently — and with the JS bundle + iOS XCFramework rebuilt in the same pass:
    ```bash
    ./tools/sl-cli/bin/sl.js publish
    ```

### Android

1.  Open the `demo/compose-app` directory as a separate project in Android Studio.
2.  Let Gradle sync. It will find the library in your local Maven repository.
3.  Select the `composeApp` run configuration.
4.  Click 'Run' (▶️).

### iOS

1.  Open the `demo/compose-app` directory in Android Studio and sync Gradle.
2.  Open the Xcode project at `demo/compose-app/iosApp/iosApp.xcworkspace`.
3.  Select an iOS simulator and click 'Run' (▶️) in Xcode. The project is already configured to find and embed the KMP shared framework.

### Node demo (standalone)

`demo/node-api` runs on its own with `npm run dev` (nodemon) or `npm start` (plain `node`). When the sl-cli's `start` is active, the Node demo is automatically the upstream behind the ngrok URL.

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