# Stockholm Transport KMP Library

This repository contains a Kotlin Multiplatform (KMP) library that serves as the complete data and logic layer for applications simulating the Stockholm public transport system.

The library is designed to be the single source of truth, handling all data fetching, business logic, and state management. The client applications (Android, iOS, Web) are intentionally "dumb," responsible only for rendering the UI based on the data provided by this library.

## Architecture

The project strictly follows **Clean Architecture** principles to ensure a clear separation of concerns, testability, and maintainability.

-   **Presentation Layer:** Contains `ViewModels` that prepare data for the UI and manage screen state. The UI observes `StateFlows` exposed by the ViewModels.
-   **Domain Layer:** Contains the core business logic, domain models (entities), and repository interfaces (contracts).
-   **Data Layer:** Contains the repository implementations, which are responsible for fetching data from external sources (the SL Transport API) and mapping it to domain models.

### Module Structure

The library is highly modularized to promote reuse and separation of concerns.

-   `shared`: The main module that aggregates all features and includes the Compose Multiplatform UI configuration.
-   `shared/core`: Contains cross-cutting code, such as the network client (Ktor) setup and dependency injection (Koin) initialization.
-   `shared/{feature}`: Each main API entity (e.g., `lines`, `sites`, `departures`) has its own module. Each of these modules follows the Clean Architecture structure internally.

## How to Build and Run

### Android

1.  Open the project in Android Studio.
2.  Select the `androidApp` run configuration.
3.  Click 'Run' (▶️). Gradle will sync and build the app on your connected device or emulator.

### iOS

1.  First, ensure the shared framework is built. From the terminal, run:
    ```bash
    ./gradlew build
    ```
2.  Open the Xcode project at `iosApp/iosApp.xcworkspace`.
3.  Select an iOS simulator and click 'Run' (▶️) in Xcode.

### Web

1.  Build the JavaScript bundle using webpack:
    ```bash
    ./gradlew jsBrowserDevelopmentWebpack
    ```
2.  This command will create an output directory at `build/js/packages/shared/`.
3.  Copy the `webApp/src/index.html` file into this directory.
4.  Start a local web server in that directory. An easy way is using Python:
    ```bash
    cd build/js/packages/shared/
    python -m http.server
    ```
5.  Open your browser to `http://localhost:8000`.

## How to Use the Library

The library's public API is exposed through its `ViewModels`. From a client application, the flow is always the same:

1.  **Initialize Koin:** Call the `initKoin()` function at your application's entry point.
2.  **Inject the ViewModel:** Use Koin to get an instance of the desired ViewModel (e.g., `LinesViewModel`).
3.  **Observe State:** Collect the `StateFlow` (`uiState`) exposed by the ViewModel to reactively receive state updates.
4.  **Call Functions:** Invoke public methods on the ViewModel to trigger actions (e.g., `viewModel.loadLines()`).

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