# SlideME Reborn / SlideME Classic

An unofficial, fan-made project for discovering software compatible with Android devices. It is not affiliated with the original SlideME operators, Google, or the developers of listed apps.

## Project parts

- Existing lightweight website: repository root (kept separate from the native app).
- Native Android app: the `android/` directory.
- Clean-room implementation inspired by the general marketplace workflow. The uploaded SAM APK is a historical reference only; decompiled code, proprietary artwork, signing keys, and bundled third-party files are not copied.

## Native Android MVP

- Java + native Android Views; no WebView
- Minimum Android API 29 (Android 10+); compile/target SDK 35
- Offline bundled catalog, local search, category filters, and app detail views
- HTTPS online catalog refresh with persisted cache and offline fallback
- HTTPS APK download, size cap, cancellation, SHA-256 verification, and pinned signing-certificate fingerprint check before installer handoff
- No accounts, ads, analytics, or third-party APKs bundled in the marketplace installer

**Not yet a production marketplace:** the catalog currently contains demo metadata only, with no APK downloads configured. No app should be listed for download until provenance, redistribution permission, package identity, signing-certificate fingerprint, file hash, and Android compatibility have been reviewed. Hash/signature checks do not prove an app is benign.

## Build

1. Install Android Studio and JDK 17.
2. Open the `android/` directory as an Android Studio project.
3. Install Android SDK Platform 35 and Build Tools 35.0.0.
4. Sync Gradle and build the `app` module.
5. Test on Android 10 (API 29) and newer devices/emulators.

GitHub Actions builds a debug APK and uploads it as an artifact when the workflow succeeds. CI success does not replace physical-device testing.

## Catalog and preservation policy

The bundled catalog is `android/app/src/main/assets/catalog.json`. See `android/README.md` for the schema and current limitations. Only use HTTPS URLs and include the exact APK SHA-256 plus the SHA-256 fingerprint of its signing certificate. Never invent hashes or assume abandoned software is free to redistribute.

## Safety

- Do not bundle third-party APKs in the marketplace installer.
- Keep TLS certificate validation enabled.
- Clearly label metadata-only, untested, incompatible, withdrawn, or unverified entries.
- Keep a record of source, licence/permission, package name, version, SHA-256, signing-certificate fingerprint, and test results.
