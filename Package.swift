// swift-tools-version:5.9
//
// Swift Package Manager manifest for the Stockholm Transport KMP library.
//
// Consumers add this package in Xcode (File → Add Package Dependencies…)
// using the repository URL:
//
//     https://github.com/eidra-umain/stockholm-transport
//
// At release time, the values of `libraryVersion` and `libraryChecksum`
// below are rewritten by the release pipeline (see PUBLISHING.md →
// "iOS distribution via Swift Package Manager") so each tag points to the
// matching `stockholm-transport.xcframework.zip` asset on GitHub Releases.
//
// During local development, prefer the in-repo Xcode build-script
// integration (`./gradlew :stockholm-transport:embedAndSignAppleFrameworkForXcode`)
// rather than this manifest, which is the *distribution* path.

import PackageDescription

let libraryVersion = "1.0.0"
let libraryChecksum = "0000000000000000000000000000000000000000000000000000000000000000"
let libraryURL = "https://github.com/eidra-umain/stockholm-transport/releases/download/\(libraryVersion)/StockholmTransport.xcframework.zip"

let package = Package(
    name: "StockholmTransport",
    platforms: [
        .iOS(.v14),
    ],
    products: [
        .library(
            name: "StockholmTransport",
            targets: ["StockholmTransport"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "StockholmTransport",
            url: libraryURL,
            checksum: libraryChecksum
        ),
    ]
)
