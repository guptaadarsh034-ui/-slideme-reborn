# SlideME Classic (native MVP)

An unofficial, fan-made marketplace prototype inspired by the old SlideME experience. This is a clean-room implementation; it does not reuse decompiled code or proprietary artwork from the uploaded SlideME SAM APK.

## Current scope

- Native Java Android app (no WebView)
- Minimum SDK API 29 (Android 10); designed for Android 10 and newer
- Compile/target SDK 35 (Android 15 target)
- Offline catalog metadata bundled as JSON
- Local search and category filters
- App detail dialog with licence and status fields
- No accounts, ads, analytics, or third-party APKs bundled
- Demo catalog entries have no APK download URLs

## Build

1. Install Android Studio and JDK 17.
2. Open the `android` directory as a project.
3. Install Android SDK Platform 35 and Build Tools 35.0.0 through SDK Manager.
4. Sync Gradle, then build the app module.
5. Test on Android 10 (API 29) and newer emulators/devices.

GitHub Actions builds a debug APK and uploads it as an artifact when the workflow succeeds.

## Catalog format

Edit `app/src/main/assets/catalog.json`. Only add a download URL after confirming redistribution permission, provenance, and compatibility. Compute the SHA-256 of the exact APK before enabling a download link.

The current prototype does not yet implement a complete in-app download-and-hash-verification workflow. Do not label an APK verified until the downloaded bytes and signing certificate have actually been checked.

## Uploaded historical APK

The uploaded `com.slideme.sam.manager-6.36.apk` is treated as a visual/behavior reference only. Do not copy decompiled code, proprietary assets, signing keys, or third-party app files into this project unless you have the necessary rights.

## Preservation and safety

- Do not bundle third-party APKs in the marketplace app.
- Keep TLS certificate validation enabled.
- Label untested, withdrawn, incompatible, and metadata-only entries clearly.
- Record source, licence/permission, package name, version, SHA-256, signing-certificate fingerprint, and test results for each preserved APK.
