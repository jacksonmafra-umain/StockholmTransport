# Library Publishing Guide

This document outlines the process for publishing the `StockholmTransport` library to local environments for development, and to remote registries for public distribution.

## 1. Prerequisites: Secret Management

Before publishing to any remote registry, you must configure your credentials securely. **Never commit API keys, passwords, or tokens directly to your Git repository.** We use the `local.properties` file (which is already in `.gitignore`) for this purpose.

Create or edit the `local.properties` file in the project root and add the following keys, filling in your own values:

```properties
# Credentials for Maven Central (Sonatype)
mavenCentralUsername=your-sonatype-username
mavenCentralPassword=your-sonatype-password

# Credentials for signing artifacts (GPG)
# See https://central.sonatype.org/publish/requirements/gpg/ to generate your key
signing.keyId=last-8-digits-of-your-gpg-key
signing.password=your-gpg-key-password
signing.secretKeyRingFile=/full/path/to/your/secring.gpg

# Token for GitHub Packages
# Generate a Personal Access Token (PAT) with `read:packages` and `write:packages` scopes
githubToken=your_github_personal_access_token
```

## 2. Local Development & Testing

This is the recommended workflow for day-to-day development and testing the library from a consumer app.

### Android/JVM (to Maven Local)

This publishes the `.aar` and `.jar` artifacts to your local Maven repository (`~/.m2/repository`).

1.  **Run the publish command:**
    ```bash
    ./gradlew publishToMavenLocal
    ```

2.  **Use in a consumer project:**
    In the consumer project's `settings.gradle.kts`, add `mavenLocal()` as a repository. Then, you can declare the dependency:
    ```kotlin
    // in consumer's build.gradle.kts
    implementation("com.umain.transport:stockholm-transport:1.0.0-SNAPSHOT")
    ```

### iOS (Local Framework Integration for Fast Development)

This is the **recommended method for local development**. It directly links your KMP shared module to your Xcode project, providing a seamless and fast development loop.

1.  **Link the Framework to the Xcode Project:**
    - Run a Gradle build once to generate the initial framework: `./gradlew :shared:assembleStockholmTransportXCFramework`
    - Open your `iosApp/iosApp.xcworkspace` in Xcode.
    - In the Project Navigator, drag the generated `StockholmTransport.xcframework` from `shared/build/XCFrameworks/release/` into the "Frameworks, Libraries, and Embedded Content" section under your `iosApp` target's "General" tab.
    - When prompted, ensure "Copy items if needed" is **unchecked**. Set the framework's "Embed" status to **"Do Not Embed"**.

2.  **Add the Pre-build Script Action:**
    - In Xcode, go to **Product → Scheme → Edit Scheme...**.
    - Select the **Build** section and click the **+** icon to choose **New Run Script Action**.
    - Drag this new script to be the very first build step.
    - Paste the following script:
      ```bash
      # Navigates to the KMP project root and runs the task to build and embed the framework
      cd "$SRCROOT/../"
      ./gradlew :shared:embedAndSignAppleFrameworkForXcode
      ```
    - In the **Provide build settings from** dropdown, select your main app target (`iosApp`).

Now, when you build in Xcode (▶️), the script runs automatically, ensuring you always have the latest Kotlin code.

### JavaScript (using NPM Link)

1.  **Build the JS package:**
    ```bash
    ./gradlew jsPublicPackageJson
    ```

2.  **Create a global symbolic link:**
    Navigate to the correct output directory and run `npm link`.
    ```bash
    cd build/js/packages/shared/
    npm link
    ```

3.  **Use in a consumer web project:**
    Navigate to your web project's directory and link the package.
    ```bash
    cd /path/to/your/web-project/
    npm link @jacksonmafra-umain/stockholm-transport
    ```

## 3. Publishing to Remote Registries

This section covers publishing production releases.

### 3.1. Android/JVM (to Maven Central)

The required metadata is already configured in `shared/build.gradle.kts`.

**Run the Publish Command:**
With your Sonatype secrets configured in `local.properties`, run:
```bash
./gradlew publishAllPublicationsToMavenCentralRepository
```

### 3.2. JavaScript (to NPM Registry)

The `package.json` metadata is already configured in `shared/build.gradle.kts`.

1.  **Log in to NPM** (one-time setup):
    ```bash
    npm login
    ```

2.  **Build and publish:**
    ```bash
    ./gradlew jsPublicPackageJson
    cd build/js/packages/shared/
    npm publish --access public
    ```

### 3.3. iOS (via Swift Package Manager)

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