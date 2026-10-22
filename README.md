# SlideME Reborn / SlideME Classic

An unofficial fan-made project for discovering software compatible with older Android devices. It is not affiliated with the original SlideME operators, Google, or the developers of listed apps.

## Project parts

- Existing lightweight website: repository root.
- Native Android MVP: the android/ directory.
- The native app is a clean-room implementation inspired by the era and general marketplace workflow. The uploaded SAM APK is a reference for historical behavior only; its code, proprietary artwork, signing key, and bundled third-party files are not copied.

## Native MVP status

The first implementation step is on branch step-1-native-mvp.

- Java + native Android Views (no WebView)
- Minimum Android API 15 (Android 4.0.3); intended for Android 4.0–4.4
- Offline catalog metadata, search, categories, and detail dialog
- No accounts, ads, analytics, or third-party APKs bundled
- Starter catalog entries are demo metadata only; no APK downloads are currently configured
- GitHub Actions workflow builds a debug APK and uploads it as an artifact

## Build the Android app

1. Install Android Studio and JDK 11.
2. Open the android/ directory as an Android Studio project.
3. Install Android SDK Platform 33.
4. Sync Gradle and run the app module.
5. Test on Android 4.0.3–4.4 emulators and, if possible, physical devices.

## Catalog and preservation policy

The offline catalog is at android/app/src/main/assets/catalog.json. Add entries only with reliable provenance. Before linking or hosting a package, verify that redistribution is permitted, document the source and licence, check the package signature, calculate the SHA-256 hash, and test compatibility. Do not treat an old or abandoned app as automatically free to redistribute.

The current prototype is an offline browsing shell. External downloads, byte-level hash verification, Internet Archive integration, and the online catalog refresh are later steps and are not yet implemented.

## Safety

- Do not bundle third-party APKs in the marketplace installation.
- Do not claim a download is verified until the client actually verifies the downloaded bytes and package signature.
- Do not disable TLS certificate validation to support old devices.
- Mark unknown, untested, incompatible, and withdrawn entries clearly.
- Keep a record of source, permission/licence, package name, version, SHA-256, signing-certificate fingerprint, and test results.

Built for legacy Android preservation.
