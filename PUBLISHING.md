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

The project uses a dedicated publication module (`:library`) to create a single, clean Maven artifact that aggregates all internal feature modules (`:shared:core`, `:shared:lines`, etc.). The `:shared` module is used to generate the unified XCFramework for iOS. This separation ensures clean metadata for all consumers.

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

This method directly links your KMP shared module to your Xcode project for a fast development loop.

1.  **Link the Framework to the Xcode Project:**
    - Run a Gradle build once to generate the initial framework: `./gradlew :shared:assembleStockholmTransportXCFramework`
    - Open your `demo/compose-app/iosApp/iosApp.xcworkspace` in Xcode.
    - Drag the generated `StockholmTransport.xcframework` from `shared/build/XCFrameworks/release/` into the "Frameworks, Libraries, and Embedded Content" section under your `iosApp` target's "General" tab.
    - When prompted, ensure "Copy items if needed" is **unchecked**. Set the framework's "Embed" status to **"Do Not Embed"**.

2.  **Add the Pre-build Script Action:**
    - In Xcode, go to **Product → Scheme → Edit Scheme...**.
    - Select the **Build** section and add a **New Run Script Action**.
    - Drag the script to be the very first build step.
    - Paste the following script:
      ```bash
      cd "$SRCROOT/../../" # Navigates to the root of the main project
      ./gradlew :shared:embedAndSignAppleFrameworkForXcode
      ```
    - In the **Provide build settings from** dropdown, select your main app target (`iosApp`).

Now, building in Xcode (▶️) automatically rebuilds and embeds the latest Kotlin code.

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

2.  **Build and publish:**
    ```bash
    ./gradlew :shared:jsPublicPackageJson
    cd shared/build/packages/js/
    npm publish --access public
    ```

### 4.3. iOS (via Swift Package Manager)

This approach uses a binary `.xcframework` hosted on GitHub Releases.

1.  **Assemble the Universal XCFramework:**
    ```bash
    ./gradlew :shared:assembleStockholmTransportXCFramework
    ```

2.  **Host the Framework:**
    - Compress the generated `StockholmTransport.xcframework` into a `.zip` file.
    - Create a new release on your project's GitHub page and upload the `.zip` file as a binary asset.
    - Calculate the zip's checksum: `swift package compute-checksum StockholmTransport.xcframework.zip`.

3.  **Create the `Package.swift` Manifest:**
    In a separate Git repository (e.g., `stockholm-transport-spm`), create a `Package.swift` file:

    ```swift
    // swift-tools-version:5.3
    import PackageDescription

    let version = "1.0.0" // Must match your GitHub release tag
    let checksum = "PASTE_YOUR_CHECKSUM_HERE"
    let url = "https://github.com/eidra-umain/stockholm-transport/releases/download/\(version)/StockholmTransport.xcframework.zip"

    let package = Package(
        name: "StockholmTransport",
        platforms: [.iOS(.v14)],
        products: [
            .library(name: "StockholmTransport", targets: ["StockholmTransport"])
        ],
        targets: [
            .binaryTarget(name: "StockholmTransport", url: url, checksum: checksum)
        ]
    )
    ```
    Commit, tag with the version, and push. Developers can now add your library in Xcode using the URL of this manifest repository.

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
            url = uri("https://maven.pkg.github.com/eidra-umain/stockholm-transport")
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
    # Step 1: Build the package
    ./gradlew jsPublicPackageJson

    # Step 2: Navigate to the output directory
    cd build/js/packages/shared/

    # Step 3: Publish to GitHub Packages
    npm publish
    ```

Your package will now be available on your GitHub repository's "Packages" page.