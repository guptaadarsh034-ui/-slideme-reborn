# SlideME Classic (native MVP)

An unofficial, fan-made legacy Android marketplace prototype inspired by the old SlideME experience. This is a clean-room implementation; it does not reuse code or proprietary artwork from the uploaded SlideME SAM APK.

## Current scope

- Native Java Android app (no WebView)
- Minimum SDK API 15 (Android 4.0.3); intended for Android 4.0–4.4
- Offline catalog metadata bundled as JSON
- Local search and category filters
- App detail dialog with licence and status fields
- No accounts, ads, analytics, or third-party APKs bundled
- A download action appears only when metadata includes HTTPS URL plus a 64-character SHA-256

The starter catalog entries are demo metadata and have no APK download URLs.

## Build

1. Install Android Studio and JDK 11.
2. Open the android directory as a project.
3. Install Android SDK Platform 33 through SDK Manager.
4. Sync Gradle, then build the app module.
5. Test on an API 15+ emulator and preferably a real Android 4.4 device.

## Catalog format

Edit app/src/main/assets/catalog.json. Only add a download URL after confirming redistribution permission, provenance, and compatibility. Compute the SHA-256 of the exact APK before enabling a download link.

The current prototype opens a verified catalog URL in the browser; an in-app download workflow and verification of downloaded bytes are next steps.

## Uploaded historical APK

The uploaded com.slideme.sam.manager-6.36.apk is treated as a visual/behavior reference only. Do not copy decompiled code, proprietary assets, signing keys, or third-party app files into this project unless you have the necessary rights.

## Preservation and safety

- Do not bundle third-party APKs in the marketplace app.
- Do not mark an APK verified unless its hash and signing certificate have been checked.
- Do not disable TLS certificate validation for old devices.
- Label untested, withdrawn, incompatible, and metadata-only entries clearly.
- Record source, licence/permission, package name, version, SHA-256, signing-certificate fingerprint, and test results for each preserved APK.
