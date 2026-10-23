# SlideME Reborn (native Android MVP)

An unofficial, fan-made marketplace prototype inspired by the old SlideME experience. This is a clean-room implementation; it does not reuse decompiled code or proprietary artwork from the uploaded SlideME SAM APK.

## Implemented

- Native Java Android app; no WebView
- Minimum SDK API 29 (Android 10); compile/target SDK 35
- Bundled offline JSON catalog with local search and category filters
- Online catalog refresh over HTTPS, with a persisted cache and bundled fallback
- Uptodown discovery shortcuts for its app and game catalog, plus a search shortcut that opens a web search restricted to Uptodown pages
- App detail view with source, licence, availability, and version metadata
- HTTPS APK download with redirect limits, connection/read timeouts, a 500 MB cap, and cancellation
- SHA-256 and APK signing-certificate fingerprint verification before an APK is passed to Android's package installer
- FileProvider exposes only the app's private cache directory
- No accounts, ads, analytics, or third-party APKs bundled inside the marketplace installer

## Important current limitations

- The bundled catalog still contains demo metadata entries. They intentionally have no download URLs, so they cannot be installed.
- A SHA-256 match proves the downloaded bytes match the catalog's expected hash; it does **not** prove an APK is benign or that the catalog publisher is trustworthy.
- The app does not scan APKs or independently validate redistribution rights. Do not publish an APK entry until provenance, licence/permission, package identity, signing certificate, Android compatibility, and hash have been reviewed. Multi-signer APKs are currently rejected.
- Uptodown is currently an external discovery source, **not** an integrated catalog API/backend. The app opens Uptodown's official category pages and uses a site-restricted web search; it does not scrape Uptodown or fetch its catalog data into the native UI.
- The online catalog URL currently points at this repository's `step-1-native-mvp` branch. Change it to your maintained production catalog URL before publishing.
- A true Uptodown-backed in-app catalog requires Uptodown's explicit authorization and supported API/feed/widget integration. Its terms prohibit unauthorized automated extraction and data mining; do not add scraping or reverse-engineer private endpoints.
- The current downloader uses an indeterminate progress indicator; byte-level progress and resumable downloads are not implemented.

## Build

1. Install Android Studio and JDK 17.
2. Open the `android` directory as a project.
3. Install Android SDK Platform 35 and Build Tools 35.0.0 through SDK Manager.
4. Sync Gradle, then build the app module.
5. Test on Android 10 (API 29) and newer emulators/devices.

GitHub Actions builds a debug APK and uploads it as an artifact when the workflow succeeds. A successful CI build is not a substitute for testing on physical devices.

## Catalog format

Edit `app/src/main/assets/catalog.json`. The top-level object contains `catalogVersion`, `notice`, and an `apps` array. Each entry uses:

- `id`, `name`, `category`, `version`, `minAndroidApi`, `description`
- `license`, `status`, `sourceUrl`
- `downloadUrl`, `sha256`, and `signingCertSha256` (all required to enable the download action)

Only use HTTPS URLs. `sha256` must be the 64-character SHA-256 hex digest of the exact APK bytes. `signingCertSha256` must be the 64-character SHA-256 digest of the APK signing certificate's DER bytes (not the APK file hash). Keep metadata honest: identify placeholders and untested apps clearly. Never invent hashes or claim verification without computing it from the actual file.

## Historical APK reference

The uploaded com.slideme.sam.manager-6.36.apk is treated as a visual/behavior reference only. Do not copy decompiled code, proprietary assets, signing keys, or third-party app files into this project unless you have the necessary rights.
