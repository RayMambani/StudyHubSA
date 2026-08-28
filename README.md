# StudyHub SA — Android App (Kotlin / Android Studio)

## Opening the project

1. Android Studio → **Open** → select the `android/` folder.
2. If Android Studio asks about the Gradle wrapper, let it generate one (or run
   `gradle wrapper --gradle-version 8.7` from this folder once, if you have
   Gradle installed) — the wrapper jar itself isn't included in this download.
3. Replace `app/google-services.json` with the real file from your own
   Firebase project (see below) — the build **will fail** until you do.
4. Replace `default_web_client_id` in `app/src/main/res/values/strings.xml`
   with your OAuth Web Client ID (see Google Sign-In setup below).
5. Point `BASE_URL` in `app/build.gradle.kts` at your deployed backend
   (the debug build already targets `http://10.0.2.2:4000/`, i.e. your own
   machine's `npm run dev`, when running on the emulator).

## Firebase setup (needed for push notifications)

1. [Firebase Console](https://console.firebase.google.com) → Create project.
2. Add an Android app with package name `com.studyhub.sa`.
3. Download `google-services.json` and replace the placeholder at `app/google-services.json`.
4. That's it on the client side — `StudyHubMessagingService` picks up tokens
   automatically. The **backend** needs its own service account key to
   actually send pushes — see `backend/README.md`.

## Google Sign-In (SSO) setup

1. In the same Firebase/Google Cloud project: APIs & Services → Credentials →
   **Create OAuth client ID** → type **Web application**. Copy the client ID.
2. Paste it into `default_web_client_id` in `strings.xml`.
3. Paste the *same* ID into the backend's `.env` as `GOOGLE_CLIENT_ID` — the
   server verifies tokens against it.
4. (Optional, only needed for a signed release build) also register the
   app's SHA-1 fingerprint in the Firebase console.

## How each PoE requirement maps to code

| Requirement | Where |
|---|---|
| Register / log in | `auth/LoginActivity.kt`, `auth/RegisterActivity.kt`, `data/repository/AuthRepository.kt` |
| Password encryption | Hashed server-side with bcrypt (`backend/routes/auth.js`); the JWT it returns is stored locally in `EncryptedSharedPreferences` (`data/local/SessionManager.kt`), never in plain text |
| Single sign-on (SSO) | `auth/GoogleSignInHelper.kt` (Google Sign-In SDK) → `POST /api/auth/google` |
| Change settings | `ui/settings/SettingsActivity.kt` — language, notifications toggle, theme, privacy, security rows |
| REST API + database | Full backend in `../backend/` (Node/Express + SQLite); consumed via `data/remote/ApiService.kt` |
| Offline mode + sync | `data/local/entity/OfflineNoteEntity.kt` + `AppDatabase.kt` (Room) for local-first writes, `sync/SyncWorker.kt` (WorkManager) pushes anything unsynced once `ConnectivityObserver` confirms a connection |
| Real-time push notifications | `notifications/StudyHubMessagingService.kt` (FCM receiver) + `notifications/NotificationHelper.kt`; backend triggers sends from `backend/notifications/push.js` on new resources and group messages |
| Multi-language (isiZulu + Tshivenda) | `res/values-zu/strings.xml`, `res/values-ve/strings.xml`, switched at runtime by `util/LocaleHelper.kt` via `AppCompatDelegate.setApplicationLocales` |

## What's fully wired vs. sample data

Fully wired to the REST API: register/login/Google SSO, resource library
(list/search/upload), study groups (list/create/join), group chat
(send/poll), settings (language + FCM token), offline notes (Room + sync).

Academic Calendar, Notifications and Profile screens render with sample
data so every mockup screen exists and looks right immediately — extending
them to hit a real endpoint follows the exact same
Retrofit-call-in-a-coroutine pattern used in `ResourceLibraryActivity` or
`OfflineNotesActivity`. That's a good next step once the core marked
requirements above are working end to end.

## Translations

The isiZulu and Tshivenda strings are a functional starting point so the
language switch genuinely changes the UI — have a fluent speaker of each
review them before you submit, the same way you'd proofread any translated
copy.

## Known limitation from where this was built

This project was generated in a sandboxed environment with no internet
access, so nothing here has been compiled or run against a real device —
treat it as a strong, consistent starting point rather than a
guaranteed-to-build artifact. If Android Studio flags an import or Gradle
version mismatch on first sync, it's almost always a version-alignment
fix (Kotlin/AGP/Gradle versions drift quickly) rather than a logic error.
