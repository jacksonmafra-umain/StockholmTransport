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

## How to Build and Run the Demo App

The project includes a demo application (`demo/compose-app`) that consumes the library and runs on both Android and iOS.

### Prerequisites

1.  **Publish the library locally:** Before you can run the demo app, you must first publish the library to your local Maven repository. From the root directory of the `StockholmTransport` project, run:
    ```bash
    ./gradlew :stockholm-transport:publishToMavenLocal
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
```

---

### **Arquivo 2: `PUBLISHING.md` (Atualizado)**

Este guia agora reflete a arquitetura de publicação final com o módulo `:library` e a configuração correta para cada plataforma.

```markdown
# Library Publishing Guide

This document outlines the process for publishing the `StockholmTransport` library to local environments for development, and to remote registries for public distribution.

## 1. Architecture Overview

The project uses a dedicated publication module (`:library`) to create a single, clean Maven artifact that aggregates all internal feature modules (`:stockholm-transport:core`, `:stockholm-transport:lines`, etc.). The `:shared` module is used to generate the unified XCFramework for iOS. This separation ensures clean metadata for all consumers.

## 2. Prerequisites: Secret Management

Before publishing to any remote registry, configure your credentials securely in the `local.properties` file (which is in `.gitignore`).

```properties
# Credentials for Maven Central (Sonatype)
mavenCentralUsername=your-sonatype-username
mavenCentralPassword=your-sonatype-password

# Credentials for signing artifacts (GPG)
signing.keyId=last-8-digits-of-your-gpg-key
signing.password=your-gpg-key-password
signing.secretKeyRingFile=/full/path/to/your/secring.gpg

# Token for GitHub Packages
githubToken=your_github_personal_access_token
```

## 3. Local Development & Testing

This is the recommended workflow for day-to-day development and testing the library from a consumer app.

### 3.0 One-shot publish via the project CLI (recommended)

`./sl publish` runs the three publish steps below in sequence and refreshes the Node demo's `file:` dependency at the end:

```bash
./sl publish           # full chain (Android/JVM/JS + iOS XCFramework)
./sl publish --no-ios  # skip the Apple build for faster iteration
```

The remainder of section 3 documents the underlying Gradle tasks the CLI calls.

### 3.1 Local Docker stack for the demo backends

For backend-side development (the realtime simulator's MongoDB schema, the static `/v1/*` proxy, etc.), bring everything up via the unified compose file:

```bash
./sl publish                        # build the Kotlin/JS bundle node-api consumes
docker compose up                   # mongo + node-api (:3000) + realtime-api (:3001)
docker compose up mongo realtime-api  # realtime stack only
```

`demo/realtime-api/` and `demo/node-api/` each ship their own `Dockerfile` (both `node:22-alpine`, run as the unprivileged `node` user). The realtime simulator's standalone compose lives at [demo/realtime-api/docker-compose.yml](../demo/realtime-api/docker-compose.yml) for the case where you want to iterate on it without building `node-api` too.

### Android/JVM/JS (to Maven Local)

This publishes a single, unified artifact to your local Maven repository (`~/.m2/repository`).

1.  **Run the publish command:**
    ```bash
    ./gradlew :stockholm-transport:publishToMavenLocal
    ```

2.  **Use in a consumer project:**
    In the consumer project's `settings.gradle.kts`, add `mavenLocal()` as a repository. Then, declare the dependency:
    ```kotlin
    // in consumer's build.gradle.kts or libs.versions.toml
    implementation("com.umain.transport:stockholm-transport:1.0.0")
    ```

### iOS (Local Framework Integration)

For day-to-day development against an Xcode project, the modern direct-integration pattern is the recommended approach — no manual `xcframework` drag, no CocoaPods. The Kotlin Gradle plugin's `embedAndSignAppleFrameworkForXcode` task injects the framework at build time via a single pre-build script.

In Xcode (Product → Scheme → Edit Scheme… → Build → Pre-actions → New Run Script Action), add **as the first build step**:

```bash
cd "$SRCROOT/../../"   # Repo root
./gradlew :stockholm-transport:embedAndSignAppleFrameworkForXcode
```

In the **Provide build settings from** dropdown, select the iOS app target so `$CONFIGURATION`, `$ARCHS`, etc. are forwarded to Gradle.

That is the entire setup. Building (▶️) in Xcode now rebuilds and embeds the latest Kotlin output every time. The `demo/mobile/iosApp/iosApp.xcodeproj` in this repo is already wired this way — open it as a reference.

## 4. Publishing to Remote Registries

### 4.1. Android/JVM/JS (to Maven Central)

The required metadata is configured in the `:library` module.

**Run the Publish Command:**
With your Sonatype secrets configured in `local.properties`, run:
```bash
./gradlew :stockholm-transport:publishAllPublicationsToMavenCentralRepository
```

### 4.2. JavaScript (to NPM Registry)

The `package.json` metadata is configured in `shared/build.gradle.kts`.

1.  **Log in to NPM** (one-time setup):
    ```bash
    npm login
    ```

2.  **Build the polished package and inspect the tarball:**
    ```bash
    ./gradlew :stockholm-transport:packTalkTgz
    ls build/distributions/npm/   # → umain-stockholm-transport-<version>.tgz
    ```

    `packTalkTgz` wraps two steps: `enhanceNpmPackageMetadata` rewrites the Kotlin/JS auto-generated `package.json` with the scoped `@umain/stockholm-transport` name plus modern `module` / `exports` / `types` / `files` fields, then `npm pack` produces an installable tarball.

3.  **(Optional) Publish to npmjs.org:**
    ```bash
    npm publish build/distributions/npm/umain-stockholm-transport-<version>.tgz --access public
    ```

    The `org.danilopianini.npm.publish` Gradle plugin is now applied in [`shared/build.gradle.kts`](../shared/build.gradle.kts) and registers its own `npmPublish` task chain — but it requires `binaries.library()`, while this module uses `binaries.executable()` (which it needs for the Node demo's webpack bundle). The pragmatic path is the `packTalkTgz` + `npm publish` flow above; the plugin sits applied to satisfy the talk's "wire the plugin" gesture and to surface registry-config DSL if a future split to a library target lands.

    Consumers `npm install @umain/stockholm-transport` and `import * as kmp from '@umain/stockholm-transport'`; the package is type-safe thanks to the auto-emitted `.d.mts`.

### 4.3. iOS (via Swift Package Manager)

SPM is the **canonical iOS distribution path** for this library. CocoaPods is not supported and is not on the roadmap.

A `Package.swift` lives at the **root of this repository** ([Package.swift](../Package.swift)) — there is no separate shim repo to maintain. Consumers just add the package URL in Xcode (`File → Add Package Dependencies…`):

```
https://github.com/jacksonmafra-umain/StockholmTransport
```

…and select the version they need. The manifest declares a `binaryTarget` that points to the matching `stockholm-transport.xcframework.zip` on GitHub Releases.

Releasing a new SPM-consumable version is three Gradle/git steps:

1.  **Assemble the universal XCFramework:**
    ```bash
    ./gradlew :stockholm-transport:assembleStockholmTransportXCFramework
    ```
    Output: `shared/build/XCFrameworks/release/StockholmTransport.xcframework`.

2.  **Zip and upload to GitHub Releases:**
    ```bash
    cd shared/build/XCFrameworks/release
    zip -r StockholmTransport.xcframework.zip StockholmTransport.xcframework
    swift package compute-checksum StockholmTransport.xcframework.zip
    ```
    Create the release on GitHub, attach the `.zip`, copy the checksum.

3.  **Update `Package.swift` and tag:**
    Set `libraryVersion` and `libraryChecksum` in [Package.swift](../Package.swift) to match the release. Commit and push the tag — Xcode will resolve the package from the tag.

> Tip: this whole sequence belongs in a `release.yml` GitHub Action so tagging triggers the upload + manifest rewrite automatically. That workflow is not yet wired in this repo.

## 4. Publishing to GitHub Packages (Alternative)

GitHub Packages is an excellent alternative to Maven Central and NPM, especially for private packages or for keeping all your project's artifacts in one place.

### Android/JVM

This process publishes your `.aar` and other Maven artifacts directly to your GitHub repository's package registry.

1.  **Configure the Repository:**
    Add the following `repositories` block inside the `mavenPublishing { ... }` block in your `shared/build.gradle.kts` file.

    ```kotlin
    // Inside mavenPublishing { ... } in shared/build.gradle.kts
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/jacksonmafra-umain/StockholmTransport")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: "jacksonmafra-umain"
                password = System.getenv("GITHUB_TOKEN") ?: property("githubToken") as String
            }
        }
    }
    ```
    *Note: `System.getenv("GITHUB_ACTOR")` and `System.getenv("GITHUB_TOKEN")` are standard environment variables used in GitHub Actions for CI/CD. The configuration falls back to your local properties for manual publishing.*

2.  **Publish:**
    Ensure your `githubToken` is set in `local.properties`. Then, run the specific publish task for this repository. The task name is generated from the repository name (`GitHubPackages`).

    ```bash
    ./gradlew publishAllPublicationsToGitHubPackagesRepository
    ```

### JavaScript

This process publishes your JavaScript package to the GitHub Packages NPM registry, scoped to your user or organization.

1.  **Configure `.npmrc`:**
    Create a file named `.npmrc` in the root of your project. This file tells NPM that any package under the `@jacksonmafra-umain` scope should be published to and installed from the GitHub registry.

    ```
    @jacksonmafra-umain:registry=https://npm.pkg.github.com/
    ```

2.  **Authenticate with GitHub Packages:**
    You need to log in to the GitHub NPM registry. This is a separate login from the public NPM registry.

    ```bash
    npm login --registry=https://npm.pkg.github.com
    ```
    -   **Username:** Enter your GitHub username (`jacksonmafra-umain`).
    -   **Password:** Enter your Personal Access Token (PAT) that you created with `write:packages` scope. **Do not use your GitHub password.**
    -   **Email:** Enter your public GitHub email address.

3.  **Build and Publish:**
    The process is the same as for the public registry. The `.npmrc` file will automatically redirect the `publish` command to GitHub.

    ```bash
    # Step 1: Build the package (webpack output dir matches Maven artifactId)
    ./gradlew :stockholm-transport:jsBrowserDistribution

    # Step 2: Navigate to the output directory
    cd build/js/packages/StockholmTransport-stockholm-transport/

    # Step 3: Publish to GitHub Packages
    npm publish
    ```

Your package will now be available on your GitHub repository's "Packages" page.