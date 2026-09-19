# StudyHub SA — Android App

A mobile learning and collaboration platform for South African college and
university students, built in Kotlin for Android Studio. StudyHub SA lets
students manage their academic schedule, share and download study
resources, collaborate in study groups, chat in real time, and get
AI-powered study assistance — all backed by a live REST API and database.

## Live backend

This app talks to a REST API hosted on Render:
Source for the backend lives in a separate repository:
[studyhub-sa-backend](https://github.com/RayMambani/studyhub-sa-backend)

## Features

- **Authentication** — register and log in with bcrypt-hashed passwords, plus Google Sign-In (SSO)
- **Settings** — language (English / isiZulu / Tshivenda), light/dark/system theme, notification toggle, profile privacy, account security (password change)
- **Resource Library** — upload, browse, search, download, and comment on study resources per module
- **Study Groups** — join or create groups, real-time group chat
- **Academic Calendar** — add/view events with a native date and time picker
- **Notifications** — an in-app feed of real activity (new resources, messages, group joins, reminders)
- **Offline Notes** — create, edit, and read notes fully offline (Room/SQLite); they sync automatically once back online
- **Profile** — real stats (uploads, groups, points), badges earned from actual activity, login streak, profile picture upload
- **AI Study Assist** — generate study summaries, ask academic questions, and get personalised study recommendations, powered by Google's Gemini API with an automatic local fallback if Gemini is ever unavailable
- **Multi-language support** — full UI translation into English, isiZulu, and Tshivenda

## Tech stack

- **Language:** Kotlin
- **Architecture:** Activity-based UI with Retrofit for networking, Room for local persistence, WorkManager for background sync
- **Networking:** Retrofit2 + OkHttp, talking to a Node.js/Express REST API
- **Local storage:** Room (offline notes), EncryptedSharedPreferences (auth token)
- **Auth:** JWT bearer tokens, Google Sign-In via Firebase
- **Push notifications:** Firebase Cloud Messaging
- **Image loading:** Glide
- **Min SDK:** 24 · **Target SDK:** 34

## Getting started

### 1. Clone and open

Open this folder directly in Android Studio (`File ? Open`).

### 2. Configure Firebase

1. Download your own `google-services.json` from the Firebase Console for the package name `com.studyhub.sa`
2. Place it at `app/google-services.json` (replacing the placeholder)

### 3. Configure Google Sign-In

In `app/src/main/res/values/strings.xml`, set:
```xml
<string name="default_web_client_id">YOUR_WEB_CLIENT_ID.apps.googleusercontent.com</string>
```

### 4. Point at the backend

In `app/build.gradle.kts`, both `BASE_URL` fields (one in `defaultConfig`, one in the `debug` build type) should point at:
or, for local backend testing on the emulator, `http://10.0.2.2:4000/`.

### 5. Build and run

If `gradlew`/`gradlew.bat` aren't present, generate them once via Android Studio's Gradle panel (**Tasks ? build ? wrapper**) or:
Then sync Gradle and click **Run**.

## Automated testing & CI

This repo includes a GitHub Actions workflow (`.github/workflows/android-ci.yml`)
that runs automatically on every push to `master`:
- Runs all unit tests (`./gradlew testDebugUnitTest`)
- Builds a debug APK (`./gradlew assembleDebug`)
- Uploads the built APK as a downloadable workflow artifact

Check the **Actions** tab on GitHub to see pipeline results.

## Known limitations

- The backend runs on Render's free tier, which has **no persistent disk** —
  the database and any uploaded files are wiped whenever the server
  restarts or redeploys. This is a hosting constraint, not an app bug.
- Render's free tier also spins down after 15 minutes of inactivity; the
  first request after a while can take up to ~50 seconds to respond.
- AI Study Assist calls Google's Gemini API; if Gemini is temporarily
  unavailable or a model name is retired, it automatically falls back to
  a simpler built-in response so the feature never fails outright.

## Project structure