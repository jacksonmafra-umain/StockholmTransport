# Library Publishing Guide

This document outlines the process for publishing the `StockholmTransport` library to local environments for testing, and to public/private registries for distribution.

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

## 2. Publishing to a Local Environment (for Testing)

Publishing locally is essential for testing the library from a consumer project before releasing it publicly.

### Android/JVM (to Maven Local)

This publishes the `.aar` and `.jar` artifacts to your local Maven repository (`~/.m2/repository`).

1.  **Run the publish command:**
    ```bash
    ./gradlew publishToMavenLocal
    ```

2.  **Use in a consumer project:**
    In the consumer project's `settings.gradle.kts`, add `mavenLocal()` as a repository. Then, you can declare the dependency as usual:
    ```kotlin
    // in consumer's build.gradle.kts
    implementation("com.umain.transport:stockholm-transport:1.0.0-SNAPSHOT")
    ```

### JavaScript (using NPM Link)

`npm link` is the standard way to test local NPM packages.

1.  **Build the JS package:**
    ```bash
    ./gradlew jsPublicPackageJson
    ```

2.  **Create a global symbolic link:**
    Navigate to the output directory and run `npm link`.
    ```bash
    cd shared/build/packages/js/
    npm link
    ```

3.  **Use in a consumer web project:**
    Navigate to your web project's directory and link the package.
    ```bash
    cd /path/to/your/web-project/
    npm link @your-npm-username/stockholm-transport
    ```
    Now you can `import` the library as if it were installed from NPM.

### iOS (Direct Project Dependency)

For iOS, the most straightforward way to test locally is to add the `StockholmTransport` Xcode project as a dependency to your consumer app's Xcode project directly. This avoids the need for a local framework publishing step.

## 3. Publishing to Public Registries

This section covers publishing to standard public registries.

### 3.1. Android/JVM (to Maven Central)

This publishes a production-ready `.aar` artifact.

#### Step 1: Configure Publishing Metadata

Add the following blocks to your `shared/build.gradle.kts` file to describe your library as required by Maven Central.

```kotlin
// Add this to the end of shared/build.gradle.kts

publishing {
    singleVariant("release") {
        withSourcesJar()
        withJavadocJar()
    }
}

mavenPublishing {
    coordinates("com.umain.transport", "stockholm-transport", "1.0.0")

    pom {
        name.set("Stockholm Transport KMP Library")
        description.set("A KMP library for the Stockholm public transport API.")
        url.set("https://github.com/your-username/your-repository")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("your-id")
                name.set("Your Name")
                email.set("your-email@example.com")
            }
        }
        scm {
            url.set("https://github.com/your-username/your-repository")
            connection.set("scm:git:git://github.com/your-username/your-repository.git")
            developerConnection.set("scm:git:ssh://git@github.com:your-username/your-repository.git")
        }
    }
}
```

#### Step 2: Run the Publish Command

With your secrets configured in `local.properties`, run the following command:

```bash
./gradlew publishAllPublicationsToMavenCentralRepository
```

### 3.2. JavaScript (to NPM Registry)

This publishes the library to the public NPM registry.

#### Step 1: Configure `package.json`

Customize the generated `package.json` by adding this block inside `kotlin { js(IR) { ... } }` in your `shared/build.gradle.kts`.

```kotlin
// Inside kotlin { js(IR) { ... } } in shared/build.gradle.kts
compilations.all {
    packageJson {
        name = "@your-npm-username/stockholm-transport"
        version = "1.0.0"
        main = "stockholm-transport.js"
        repository("git", "https://github.com/your-username/your-repository.git")
    }
}
```

#### Step 2: Publish to NPM

1.  **Log in to NPM** (one-time setup):
    ```bash
    npm login
    ```

2.  **Build the JS packages:**
    ```bash
    ./gradlew jsPublicPackageJson
    ```

3.  **Navigate and publish:**
    ```bash
    cd shared/build/packages/js/
    npm publish --access public
    ```

### 3.3. iOS (via Swift Package Manager)

This is the modern approach for distributing iOS frameworks, using a binary `.xcframework` hosted on GitHub Releases.

#### Step 1: Generate the `.xcframework`

Add this task to your `shared/build.gradle.kts` to create a universal framework.

```kotlin
// Add this to the end of shared/build.gradle.kts
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

val xcf = XCFramework()
kotlin.targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
    if (this.konanTarget.family.isAppleFamily) {
        binaries.framework {
            baseName = "StockholmTransport"
            xcf.add(this)
        }
    }
}

tasks.register("createXCFramework", Copy::class) {
    from(xcf.outputFile)
    into(buildDir.resolve("xcframework"))
}
```
Run the task:
```bash
./gradlew createXCFramework
```
This creates `StockholmTransport.xcframework` inside `shared/build/xcframework/`.

#### Step 2: Host the Framework

1.  Compress the `StockholmTransport.xcframework` into a `.zip` file.
2.  Create a new release on your project's GitHub page and upload this `.zip` file as a binary asset.
3.  Once uploaded, calculate the zip's checksum: `swift package compute-checksum StockholmTransport.xcframework.zip`.

#### Step 3: Create the `Package.swift` File

In a **separate Git repository**, create a `Package.swift` file. This repository will serve as the SPM package definition.

```swift
// swift-tools-version:5.3
import PackageDescription

let version = "1.0.0"
let checksum = "PASTE_YOUR_CHECKSUM_HERE"
let url = "URL_TO_YOUR_XCframework.zip_ON_GITHUB_RELEASES"

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
Developers can now add your library in Xcode using the URL of this new Git repository.

## 4. Publishing to GitHub Packages

GitHub Packages is an excellent alternative for hosting both public and private packages.

### 4.1. Android/JVM (to GitHub Packages)

#### Step 1: Configure the Repository

Add the following to your `shared/build.gradle.kts` inside the `mavenPublishing { ... }` block.

```kotlin
// Inside mavenPublishing { ... } in shared/build.gradle.kts
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/your-username/your-repository")
        credentials {
            username = System.getenv("GITHUB_ACTOR") ?: property("mavenCentralUsername") as String
            password = System.getenv("GITHUB_TOKEN") ?: property("githubToken") as String
        }
    }
}
```

#### Step 2: Publish

Ensure your `githubToken` is set in `local.properties`. Then run:

```bash
./gradlew publish
```

### 4.2. JavaScript (to GitHub Packages)

#### Step 1: Configure `.npmrc`

Create a file named `.npmrc` in the root of your project with the following content. This tells NPM to associate your package scope with the GitHub registry.

```
@your-npm-username:registry=https://npm.pkg.github.com/
//npm.pkg.github.com/:_authToken=${GITHUB_TOKEN}
```

#### Step 2: Publish

Ensure your `GITHUB_TOKEN` environment variable is set or available. The process is the same as publishing to the public registry; the `.npmrc` file will automatically redirect the request to GitHub.

1.  Build the packages:
    ```bash
    ./gradlew jsPublicPackageJson
    ```
2.  Navigate and publish:
    ```bash
    cd shared/build/packages/js/
    npm publish
    ```